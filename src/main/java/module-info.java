module main.sprint5 {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;

    opens main.sprint5 to javafx.fxml;
    exports main.sprint5;
}