package common;

import java.io.Serializable;

public class CommandMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private MessageType type;
    private String content;

    public CommandMessage(MessageType type, String content) {
        this.type = type;
        this.content = content;
    }

    public MessageType getType() { return type; }
    public String getContent() { return content; }
}