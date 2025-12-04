module org.example.remote_windows_console {
    requires javafx.controls;
    requires javafx.fxml;

    exports client;
    exports common;
    exports server;

    opens client to javafx.fxml;
}