package common.messages;

public class DataMessage extends Message {

    private int sessionID;
    private String payload;

    /**
     * Constructor creates a new DATA message from parameters
     *
     * @param header    The header of the message
     * @param cseq      The sequence number
     * @param sessionID The is the session ID
     * @param payload   The data payload (audio/video chunk or metadata)
     */
    public DataMessage(String header, int cseq, int sessionID, String payload) {
        super("DATA", header, cseq);
        this.sessionID = sessionID;
        this.payload = payload;
    }

    /**
     * Constructor creates a new DATA message from a message string
     *
     * @param messageString The raw RTSP DATA message
     */
    public DataMessage(String messageString) {
        super(messageString);

        if (!(this.getType().equals("DATA"))) {
            throw new IllegalArgumentException("Invalid message type for DataMessage: " + this.getType());
        }

        String[] lines = messageString.split("\r\n");

        // Extract sessionID and payload if present
        for (String line : lines) {
            if (line.startsWith("Session: ")) {
                this.sessionID = Integer.parseInt(line.split(" ")[1]);
            } else if (line.startsWith("Payload: ")) {
                this.payload = line.substring(9);
            }
        }

        // Validate required fields
        if (this.sessionID == 0) {
            throw new IllegalArgumentException("Session ID is required in DATA message");
        }
        if (this.payload == null) {
            throw new IllegalArgumentException("Payload is required in DATA message");
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
     * Get payload
     *
     * @return String of payload
     */
    public String getPayload() {
        return payload;
    }

    /**
     * Override toString to include session ID and payload
     */
    @Override
    public String toString() {
        if (messageString != null) {
            return messageString;
        } else {
            String baseString = super.toString() +
                    "Session: " + sessionID + "\r\n" +
                    "Payload: " + payload + "\r\n";
            return baseString + "\r";
        }
    }
}
