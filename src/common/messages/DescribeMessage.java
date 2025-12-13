package common.messages;

public class DescribeMessage extends Message{
    
    private String accept;

    /**
     * Constructor creates a new DESCRIBE message from parameters
     *
     * @param header This is the header of the message
     * @param cseq This is the cseq of the message
     * @param accept This is the accepted content
     */
    public DescribeMessage(String header, int cseq, String accept) {
        super("DESCRIBE", header, cseq);
        this.accept = accept;
    }

    /**
     * Constructor creates a new DESCRIBE message from a message string
     * 
     * @param messageString This is the messageString
     */
    public DescribeMessage(String messageString) {
        super(messageString);

        if (!(this.getType().equals("DESCRIBE"))) {
            throw new IllegalArgumentException("Invalid message type for DescribeMessage: " + this.getType());
        }

        String[] lines = messageString.split("\r\n");

        for (String line : lines) {
            if (line.startsWith("Accept: ")) {
                this.accept = line.substring(8).trim();
            }
        }

        //Validate required fields
        if (this.accept == null || this.accept.isEmpty()) {
            throw new IllegalArgumentException("Accept field is required in DESCRIBE message");
        }
    }

    /**
     * Get accept header
     * 
     * @return String of accept header
     */
    public String getAccept() {
        return accept;
    }

    /**
     * Override toString to include accept header
     */
    @Override
    public String toString() {
        if (messageString != null) {
            return messageString;
        } else {
            String baseString = super.toString() + "Accept: " + accept + "\r\n";
            return baseString + "\r";
        }
    }
}
