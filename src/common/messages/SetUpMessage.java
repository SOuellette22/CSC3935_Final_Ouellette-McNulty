package common.messages;

public class SetUpMessage extends Message {

    private int sessionID;
    private String transport;

    /**
     * Constructor creates a new SETUP message from parameters for the server
     */
    public SetUpMessage(String header, int cseq, int sessionID, String transport) {
        super("SETUP", header, cseq);
        this.sessionID = sessionID;
        this.transport = transport;
    }

    /**
     * Constructor creates a new SETUP message from a message string for the client
     */
    public SetUpMessage( String header, int cseq, String transport) {
        super("SETUP", header, cseq);
        this.transport = transport;
    }

    /**
     * Constructor creates a new SETUP message from a message string
     */
    public SetUpMessage(String messageString) {
        super(messageString);

        if (!(this.getType().equals("SETUP"))) {
            throw new IllegalArgumentException("Invalid message type for SetUpMessage: " + this.getType());
        }

        String[] lines = messageString.split("\r\n");

        // Extract sessionID if present
        for (String line : lines) {
            // Look for Session if present
            if (line.startsWith("Session: ")) {
                this.sessionID = Integer.parseInt(line.split(" ")[1]);

            // Look for Transport
            } else if (line.startsWith("Transport: ")) {
                this.transport = line.substring(11);
            }
        }

        // Validate required fields
        if (this.transport == null) {
            throw new IllegalArgumentException("Transport field is required in SETUP message");
        }
    }

    /**
     * Get session ID
     *
     * @return int of session ID
     */
    public int getSessionID() {
        return sessionID;
    }

    /**
     * Get transport
     *
     * @return String of transport
     */
    public String getTransport() {
        return transport;
    }

    /**
     * Override toString to include sessionID and transport
     */
    @Override
    public String toString() {
        // If messageString is already constructed, return it
        if (messageString != null) {
            return messageString;
        }

        // Otherwise, construct it
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        if (sessionID != 0) {
            sb.append("Session: ").append(sessionID).append("\r\n");
        }
        sb.append("Transport: ").append(transport).append("\r\n");

        return sb.toString() + "\r\n";

    }
}
