package server;

import common.CommandMessage;
import common.MessageType;

import java.io.*;
import java.nio.charset.Charset;

public class StreamGobbler implements Runnable {
    private InputStream is;
    private MessageType type;
    private ObjectOutputStream out;
    private Charset charset;

    public StreamGobbler(InputStream is, MessageType type, ObjectOutputStream out, Charset charset) {
        this.is = is;
        this.type = type;
        this.out = out;
        this.charset = charset;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, charset))) {
            String line;
            while ((line = reader.readLine()) != null) {
                synchronized (out) {
                    out.writeObject(new CommandMessage(type, line + "\n"));
                    out.flush();
                }
            }
        } catch (IOException e) {}
    }
}