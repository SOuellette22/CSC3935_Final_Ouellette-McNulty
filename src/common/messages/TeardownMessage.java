package common.messages;

public class TeardownMessage extends Message {

    private int sessionID;

    /**
     * Constructor creates a new TEARDOWN message from parameters
     *
     * @param header    The header of the message
     * @param cseq      The sequence number
     * @param sessionID The session identifier
     */
    public TeardownMessage(String header, int cseq, int sessionID) {
        super("TEARDOWN", header, cseq);
        this.sessionID = sessionID;
    }

    /**
     * Constructor creates a new TEARDOWN message from a message string
     *
     * @param messageString The raw RTSP TEARDOWN message
     */
    public TeardownMessage(String messageString) {
        super(messageString);

        if (!(this.getType().equals("TEARDOWN"))) {
            throw new IllegalArgumentException("Invalid message type for TeardownMessage: " + this.getType());
        }

        String[] lines = messageString.split("\r\n");

        // Extract sessionID if present
        for (String line : lines) {
            if (line.startsWith("Session: ")) {
                this.sessionID = Integer.parseInt(line.split(" ")[1]);
            }
        }

        // Validate required fields
        if (this.sessionID == 0) {
            throw new IllegalArgumentException("Session ID is required in TEARDOWN message");
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
     * Override toString to include session ID
     */
    @Override
    public String toString() {
        if (messageString != null) {
            return messageString;
        } else {
            String baseString = super.toString() + "Session: " + sessionID + "\r\n";
            return baseString + "\r";
        }
    }
}
