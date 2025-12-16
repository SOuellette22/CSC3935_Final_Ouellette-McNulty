package common.messages;

import java.util.Arrays;

public class Message {

    protected String messageString;

    private String type;
    private String header;
    private int cseq;

    /**
     * Constructor creates a new message from parameters
     */
    public Message(String type, String header, int cseq) {
        this.type = type;
        this.header = header;
        this.cseq = cseq;
    }

    /**
     * Constructor creates a new message from a message string
     */
    public Message(String messageString) {
        this.messageString = messageString;

        // Parse the message string to extract type, header, and cseq
        String[] lines = messageString.split("\r\n");

        // First line: TYPE SERVER_ADDRESS RTSP/1.0
        String[] firstLineParts = lines[0].split(" ");
        this.type = firstLineParts[0];
        this.header = firstLineParts[1];

        // Second line: CSeq: <number>
        this.cseq = Integer.parseInt(lines[1].split(" ")[1]);
    }

    /**
     * Get message type
     *
     * @return String of message type
     */
    public String getType() {
        return type;
    }

    /**
     * Get message header
     *
     * @return String of message header
     */
    public String getHeader() {
        return header;
    }

    /**
     * Get message cseq
     *
     * @return int of message cseq
     */
    public int getCseq() {
        return cseq;
    }

    @Override
    public String toString() {
        return type + " " + header + " RTSP/1.0\r\n" +
                "CSeq: " + cseq + "\r\n";
    }
}
