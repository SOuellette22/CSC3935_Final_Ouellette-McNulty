package common.messages;

public class RecordMessage extends Message{

    private int sessionID;
    private String range;

    /**
     * Constructor creates a new RECORD message for parameters
     */
    public RecordMessage(String header, int cseq, int sessionID) {
        super("RECORD", header, cseq);
        this.sessionID = sessionID;
    }

    public RecordMessage(String header, int cseq, int sessionID, String range) {
        super("RECORD", header, cseq);
        this.sessionID = sessionID;
        this.range = range;
    }

    /**
     * Constructor that creates a new RECORD message from a message string
     */
    public RecordMessage(String messageString) {
        super(messageString);

        if (!(this.getType().equals("RECORD"))) {
            throw new IllegalArgumentException("Invalid message type for RecordMessage" + this.getType());
        }

        String[] lines = messageString.split("\r\n");

        for (String line : lines) {
            if (line.startsWith("Session: ")) {
                this.sessionID = Integer.parseInt(line.split(" ")[1]);
            } else if (line.startsWith("Range: ")) {
                this.range = line.substring(7);
            }
        }

        // Validate required fields
        if (this.sessionID == 0) {
            throw new IllegalArgumentException("Session ID is required in Record message");
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
        if (messageString != null) {
            return messageString;
        } else {
            String baseString = super.toString() + "Session: " + sessionID + "\r\n";
            if (range != null && !range.isEmpty()) {
                baseString += "Range: " + range + "\r\n";
            }
            return baseString + "\r";
        }
    }

}
