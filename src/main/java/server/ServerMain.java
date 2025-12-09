package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class ServerMain {
    private static int PORT = 8189;

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        System.out.println(
                "Select desired option:\n" +
                        "1. Use default PORT 8189\n" +
                        "2. Input your own PORT"
        );
        int answer = 0;
        while (true) {
            String line = input.nextLine();
            try {
                answer = Integer.parseInt(line.trim());
                if (answer == 1 || answer == 2) {
                    break;
                } else {
                    System.out.println("Invalid input. Try again:");
                }
            } catch (Exception e) {
                System.out.println("Invalid input. Try again:");
            }
        }
        switch (answer) {
            case 1:
                break;
            case 2:
                while (true) {
                    try {
                        System.out.print("Input desired PORT (value between 1 and 65535): ");
                        String line = input.nextLine();
                        int portCandidate = Integer.parseInt(line.trim());
                        if (portCandidate < 1 || portCandidate > 65535) {
                            System.out.println("Port number must be between 1 and 65535");
                            continue;
                        }
                        PORT = portCandidate;
                        break;
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input");
                    }
                }
                break;
        }
        System.out.println("Server started on port " + PORT + "...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Client connected: " + socket.getInetAddress());
                new Thread(new ClientHandler(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}