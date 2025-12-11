package common.messages;

public class PlayPauseMessage extends Message {

    private int sessionID;
    private String range;

    /**
     * Constructor creates a new PLAY/PAUSE message from parameters
     */
    public PlayPauseMessage(String type, String header, int cseq, int sessionID) {
        super(type, header, cseq);
        this.sessionID = sessionID;
    }

    /**
     * Constructor creates a new PLAY/PAUSE message from parameters
     */
    public PlayPauseMessage(String type, String header, int cseq, int sessionID, String range) {
        super(type, header, cseq);

        this.sessionID = sessionID;
        this.range = range;
    }

    /**
     * Constructor creates a new PLAY/PAUSE message from a message string
     */
    public PlayPauseMessage(String messageString) {
        super(messageString);

        if (this.getType() != "PLAY" && this.getType() != "PAUSE") {
            throw new IllegalArgumentException("Invalid message type for PlayPauseMessage: " + this.getType());
        }

        String[] lines = messageString.split("\r\n");

        // Extract sessionID and range if present
        for (String line : lines) {
            // Look for Session
            if (line.startsWith("Session: ")) {
                this.sessionID = Integer.parseInt(line.split(" ")[1]);
                // Look for Range
            } else if (line.startsWith("Range: ")) {
                this.range = line.substring(7);
            }
        }

        // Validate required fields
        if (this.sessionID == 0) {
            throw new IllegalArgumentException("Session ID is required in PLAY/PAUSE message");
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
     * Get range
     *
     * @return String of range
     */
    public String getRange() {
        return range;
    }

    /**
     * Override toString to include session ID and range
     */
    @Override
    public String toString() {
        return super.toString();
    }
}
