package FN_Kleinprojekt;

import java.io.IOException;
import javafx.fxml.FXML;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.*;
import javax.crypto.spec.*;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

public class HomescreenController {
	
    @FXML
    private StackPane root;

    @FXML
    private Rectangle dropTarget;

    @FXML
    private TextField pathInput;
    
    @FXML
    private TextField passwordInput;
    
    @FXML
    private Label passwordStrength;
    
    @FXML
    private Button go;
    
    @FXML
    private Label pathOutput;
    
    public void initialize() {
        // Set up drag and drop handling for the Rectangle
        dropTarget.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        dropTarget.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                File file = db.getFiles().get(0);
                pathInput.setText(file.getAbsolutePath());
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }
    
    String path;
    SecretKey secretKey;
    String passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$";

    @FXML
    private void confirm() throws IOException {
    	path = pathInput.getText();
        passwordInput.setEditable(true);
    }
    
    public void encryptFileU() throws Exception {
    	
        String password = passwordInput.getText();
        
        boolean isStrongPassword = password.matches(passwordPattern);
        
        passwordStrength.setText(isStrongPassword ? "Password ist stark genug." : "Password ist nicht stark genug.");
        
        if (isStrongPassword) {
	        SecureRandom random = new SecureRandom();
	        byte[] salt = new byte[16];
	        random.nextBytes(salt);
	
	        int iterations = 10000;
	        int keyLength = 128;
	        KeySpec spec = new PBEKeySpec(passwordInput.getText().toCharArray(), salt, iterations, keyLength);
	        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
	        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
	        secretKey = new SecretKeySpec(keyBytes, "AES");
	
	        byte[] fileContent = Files.readAllBytes(Paths.get(path));
	
	        Cipher cipher = Cipher.getInstance("AES");
	        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
	
	        byte[] encryptedContent = cipher.doFinal(fileContent);
	
	        FileOutputStream outputStream = new FileOutputStream(path);
	        outputStream.write(salt);
	        outputStream.write(encryptedContent);
	        outputStream.close();
	
	        pathOutput.setText("Verschlüsselte Datei hier gespeichert: " + path);
	    }
    }
    
    public void decryptFileU() throws Exception {
    	try {
            byte[] encryptedContent = Files.readAllBytes(Paths.get(path));

            byte[] salt = Arrays.copyOfRange(encryptedContent, 0, 16);

            int iterations = 10000;
            int keyLength = 128;
            KeySpec spec = new PBEKeySpec(passwordInput.getText().toCharArray(), salt, iterations, keyLength);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] keyBytes = factory.generateSecret(spec).getEncoded();
            secretKey = new SecretKeySpec(keyBytes, "AES");

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            byte[] decryptedContent = cipher.doFinal(encryptedContent, 16, encryptedContent.length - 16);

            FileOutputStream outputStream = new FileOutputStream(path);
            outputStream.write(decryptedContent);
            outputStream.close();

            passwordStrength.setText("Passwort richtig");
            pathOutput.setText("Entschlüsselte Datei hier gespeichert: " + path);
    	}catch(Exception e){
    		passwordStrength.setText("Passwort falsch");
    	}
        
    }
}