package common;

public enum MessageType {
    INPUT,      // Command sent from Client to Server
    OUTPUT,     // Standard output from Server to Client
    ERROR,      // Error output from Server to Client
    CONNECT,    // Connection initiation
    DISCONNECT  // Connection termination
}