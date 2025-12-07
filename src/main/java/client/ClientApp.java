package client;

import common.CommandMessage;
import common.MessageType;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Optional;

public class ClientApp extends Application {

    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Socket socket;
    private TextArea consoleArea;
    private TextField inputField;

    private String HOST;
    private int PORT;

    private String[] requestHostAndPort () {
        Dialog<String[]> dialog = new Dialog<>();

        dialog.setTitle("Сonnection to the server");
        dialog.setHeaderText("Enter server IP-address (or host) and port");

        ButtonType connectionButton = new ButtonType("Connect", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancellationButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(connectionButton, cancellationButton);

        TextField hostField = new TextField("localhost");
        hostField.setFont(Font.font("consolas",14));

        TextField portField = new TextField("8189");
        portField.setFont(Font.font("consolas",14));

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setPadding(new Insets(20, 20, 20, 20));

        Label hostLabel = new Label("IP_address/hostname");
        hostLabel.setFont(Font.font("consolas",14));

        Label portLabel = new Label("Port");
        portLabel.setFont(Font.font("consolas",14));

        grid.add(hostLabel, 0, 0);
        grid.add(hostField, 1, 0);
        grid.add(portLabel, 0, 1);
        grid.add(portField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button == connectionButton) {
                return new String[]{hostField.getText().trim(), portField.getText().trim()};
            }
            return null;
        });

        Optional<String[]> result = dialog.showAndWait();
        return result.orElse(null);
    }

    private boolean isValidHost(String host) {
        if (host.isEmpty())
            return false;
        try {
            InetAddress.getByName(host); // accepts IPv4, IPv6 and DNS names
            return true;
        } catch (UnknownHostException e) {
            return false;
        }
    }

    @Override
    public void start(Stage stage) {
        String[] requestResult=requestHostAndPort();
        if (requestResult==null) {
            Platform.exit();
            return;
        }

        if (isValidHost(requestResult[0])) {
            HOST=requestResult[0];
        }
        else {
            throw new IllegalArgumentException("Invalid host format: must be IP address or hostname.");
        }

        int port;
        try {
            port = Integer.parseInt(requestResult[1]);
            if (port < 1 || port > 65535) {
                throw new IllegalArgumentException("Invalid port format: must be a number between 1 and 65535.");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid port format: must be a number between 1 and 65535.", e);
        }
        PORT = port;

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
                            consoleArea.appendText("ERROR: " + msg.getContent());
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