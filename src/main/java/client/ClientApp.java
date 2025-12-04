package client;

import common.CommandMessage;
import common.MessageType;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;

public class ClientApp extends Application {

    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Socket socket;
    private TextArea consoleArea;
    private TextField inputField;

    private final String HOST = "localhost";
    private final int PORT = 8189;

    @Override
    public void start(Stage stage) {
        consoleArea = new TextArea();
        consoleArea.setEditable(false);
        consoleArea.setStyle("-fx-control-inner-background: black; -fx-text-fill: white;");
        consoleArea.setFont(Font.font("Consolas", 14));
        consoleArea.setPrefHeight(500);

        inputField = new TextField();
        inputField.setPromptText("Enter command...");
        inputField.setOnAction(e -> sendCommand());

        VBox root = new VBox(10, consoleArea, inputField);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, 800, 600);
        stage.setTitle("Remote Windows Console");
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> disconnect());
        stage.show();

        connect();
    }

    private void connect() {
        new Thread(() -> {
            try {
                socket = new Socket(HOST, PORT);
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());

                Platform.runLater(() -> consoleArea.appendText("Connected to server.\n"));

                while (true) {
                    CommandMessage msg = (CommandMessage) in.readObject();
                    Platform.runLater(() -> {
                        if (msg.getType() == MessageType.ERROR) {
                            consoleArea.appendText("ERR: " + msg.getContent());
                        } else {
                            consoleArea.appendText(msg.getContent());
                        }
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> consoleArea.appendText("Connection lost: " + e.getMessage() + "\n"));
            }
        }).start();
    }

    private void sendCommand() {
        String text = inputField.getText();
        if (text.isEmpty()) return;
        inputField.clear();

        try {
            if (out != null) {
                out.writeObject(new CommandMessage(MessageType.INPUT, text));
                out.flush();
            }
        } catch (IOException e) {
            consoleArea.appendText("Failed to send command.\n");
        }
    }

    private void disconnect() {
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}