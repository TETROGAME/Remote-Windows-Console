package server;

import common.CommandMessage;
import common.MessageType;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private Process process;
    private BufferedWriter processWriter;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

            ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/k", "chcp 65001");

            process = pb.start();

            Charset consoleCharset = Charset.forName("UTF-8");

            processWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), consoleCharset));

            new Thread(new StreamGobbler(process.getInputStream(), MessageType.OUTPUT, out, consoleCharset)).start();
            new Thread(new StreamGobbler(process.getErrorStream(), MessageType.ERROR, out, consoleCharset)).start();

            while (true) {
                CommandMessage msg = (CommandMessage) in.readObject();
                if (msg.getType() == MessageType.INPUT) {
                    processWriter.write(msg.getContent());
                    processWriter.newLine();
                    processWriter.flush();
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Client disconnected.");
        } finally {
            cleanup();
        }
    }

    private void cleanup() {
        try {
            if (process != null) process.destroy();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}