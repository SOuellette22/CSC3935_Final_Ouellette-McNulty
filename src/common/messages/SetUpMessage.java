package common.messages;

public class SetUpMessage extends Message {

    private int sessionID;
    private String transport;

    /**
     * Constructor creates a new SETUP message from parameters for the server
     */
    public SetUpMessage(String type, String header, int cseq, int sessionID, String transport) {
        super(type, header, cseq);
        this.sessionID = sessionID;
        this.transport = transport;
    }

    /**
     * Constructor creates a new SETUP message from a message string for the client
     */
    public SetUpMessage(String type, String header, int cseq, String transport) {
        super(type, header, cseq);
        this.transport = transport;
    }

    public SetUpMessage(String messageString) {
        super(messageString);

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
        return super.toString();
    }
}
