module FN_Kleinprojekt {
    requires javafx.controls;
    requires javafx.fxml;

    opens FN_Kleinprojekt to javafx.fxml;
    exports FN_Kleinprojekt;
}
