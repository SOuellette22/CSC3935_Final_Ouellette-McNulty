package common.messages;

public class SetUpMessage extends Message {

    private String transport;

    /**
     * Constructor creates a new SETUP message from parameters
     *
     * @param header This is the header of the message
     * @param cseq This is the cseq of the message
     * @param transport This is the transport of the message
     */
    public SetUpMessage(String header, int cseq, String transport) {
        super("SETUP", header, cseq);
        this.transport = transport;
    }

    /**
     * Constructor creates a new SETUP message from a message string
     *
     * @param messageString This is the message string
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
            if (line.startsWith("Transport: ")) {
                this.transport = line.substring(11);
            }
        }

        // Validate required fields
        if (this.transport == null) {
            throw new IllegalArgumentException("Transport field is required in SETUP message");
        }
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
        sb.append("Transport: ").append(transport).append("\r\n");

        return sb.toString() + "\r";

    }
}
