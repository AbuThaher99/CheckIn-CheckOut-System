module com.example.wo {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires kernel;
    requires io;
    requires layout;
    requires itextpdf;


    opens com.example.wo to javafx.fxml;
    exports com.example.wo;
}