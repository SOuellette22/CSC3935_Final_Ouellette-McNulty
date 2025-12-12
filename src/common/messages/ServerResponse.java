package common.messages;

public class ServerResponse extends Message {

    /// Required variables ///
    private int code;
    private String message;
    private int cseq;

    /// Optional variables can be added here ///
    private String options;
    private int sessionId;
    private String transport;
    private String contentType;
    private int contentLength;
    private String body;

    /**
     * Constructor for ServerResponse using ResponseBuilder
     *
     * @param builder This is the ResponseBuilder object
     */
    public ServerResponse(ResponseBuilder builder) {
        super("RTSP/1.0", builder.code + " " + builder.message, builder.cseq);
        this.code = builder.code;
        this.message = builder.message;
        this.cseq = builder.cseq;
        this.options = builder.options;
        this.sessionId = builder.sessionId;
        this.transport = builder.transport;
        this.contentType = builder.contentType;
        this.contentLength = builder.contentLength;
        this.body = builder.body;
    }

    /**
     * Constructor for ServerResponse from message string
     *
     * @param messageString This is the full message string
     */
    public ServerResponse(String messageString) {
        super(messageString);

        if (!getType().equals("RTSP/1.0")) {
            throw new IllegalArgumentException("Invalid RTSP response message");
        }

        // Further parsing of the messageString to extract code, message, and optional fields can be added here
        String[] lines = messageString.split("\r\n");

        // First line: RTSP/1.0 <code> <message>
        String[] firstLineParts = lines[0].split(" ", 3);
        this.code = Integer.parseInt(firstLineParts[1]);
        this.message = firstLineParts[2];
        this.cseq = Integer.parseInt(lines[1].split(" ")[1]);

        // Additional parsing for optional fields can be implemented here
        for (String line : lines) {
            if (line.startsWith("Public: ")) { // Options header
                this.options = line.substring(9);

            } else if (line.startsWith("Session: ")) { // Session header
                this.sessionId = Integer.parseInt(line.substring(9));

            } else if (line.startsWith("Transport: ")) { // Transport header
                this.transport = line.substring(11);

            } else if (line.startsWith("Content-Type: ")) { // Content-Type header
                this.contentType = line.substring(14);

            } else if (line.startsWith("Content-Length: ")) { // Content-Length header
                this.contentLength = Integer.parseInt(line.substring(16));

            } else if (line.isEmpty()) { // Body starts after this line
                // The body starts after an empty line
                int bodyIndex = messageString.indexOf("\r\n\r\n") + 5;
                if (bodyIndex < messageString.length()) {
                    this.body = messageString.substring(bodyIndex);
                }
            }
        }
    }

    /**
     * Get response code
     *
     * @return int response code
     */
    public int getCode() {
        return code;
    }

    /**
     * Get response message
     *
     * @return String response message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get CSeq
     *
     * @return int CSeq
     */
    public int getCseq() {
        return cseq;
    }

    /**
     * Get options
     *
     * @return String options
     */
    public String getOptions() {
        return options;
    }

    /**
     * Get session ID
     *
     * @return int session ID
     */
    public int getSessionId() {
        return sessionId;
    }

    /**
     * Get transport
     *
     * @return String transport
     */
    public String getTransport() {
        return transport;
    }

    /**
     * Get content type
     *
     * @return String content type
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Get content length
     *
     * @return int content length
     */
    public int getContentLength() {
        return contentLength;
    }

    /**
     * Get body
     *
     * @return String body
     */
    public String getBody() {
        return body;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RTSP/1.0 ").append(code).append(" ").append(message).append("\r\n");
        sb.append("CSeq: ").append(cseq).append("\r\n");

        if (options != null) {
            sb.append("Public: ").append(options).append("\r\n");
        }
        if (sessionId != 0) {
            sb.append("Session: ").append(sessionId).append("\r\n");
        }
        if (transport != null) {
            sb.append("Transport: ").append(transport).append("\r\n");
        }
        if (contentType != null) {
            sb.append("Content-Type: ").append(contentType).append("\r\n");
        }
        if (contentLength != 0) {
            sb.append("Content-Length: ").append(contentLength).append("\r\n");
        }
        sb.append("\r\n");
        if (body != null) {
            sb.append(body).append("\r\n");
        }

        return sb.toString();
    }

    /**
     * Builder class for ServerResponse
     */
    public static class ResponseBuilder {
        private int code;
        private String message;
        private int cseq;

        private String options;
        private int sessionId;
        private String transport;
        private String contentType;
        private int contentLength;
        private String body;

        /**
         * Constructor for ResponseBuilder
         *
         * @param code This is the response code
         * @param cseq This is the CSeq of the request being responded to
         */
        public ResponseBuilder(int code, int cseq) {
            this.code = code;
            this.cseq = cseq;

            // Map code to message
            switch (code) {
                case 100:
                    this.message = "Continue";
                    break;
                case 200:
                    this.message = "OK";
                    break;
                case 201:
                    this.message = "Created";
                    break;
                case 250:
                    this.message = "Low on Storage Space";
                    break;
                case 300:
                    this.message = "Multiple Choices";
                    break;
                case 301:
                    this.message = "Moved Permanently";
                    break;
                case 302:
                    this.message = "Moved Temporarily";
                    break;
                case 303:
                    this.message = "See Other";
                    break;
                case 304:
                    this.message = "Not Modified";
                    break;
                case 305:
                    this.message = "Use Proxy";
                    break;
                case 400:
                    this.message = "Bad Request";
                    break;
                case 401:
                    this.message = "Unauthorized";
                    break;
                case 402:
                    this.message = "Payment Required";
                    break;
                case 403:
                    this.message = "Forbidden";
                    break;
                case 404:
                    this.message = "Not Found";
                    break;
                case 405:
                    this.message = "Method Not Allowed";
                    break;
                case 406:
                    this.message = "Not Acceptable";
                    break;
                case 407:
                    this.message = "Proxy Authentication Required";
                    break;
                case 408:
                    this.message = "Request Time-out";
                    break;
                case 410:
                    this.message = "Gone";
                    break;
                case 411:
                    this.message = "Length Required";
                    break;
                case 412:
                    this.message = "Precondition Failed";
                    break;
                case 413:
                    this.message = "Request Entity Too Large";
                    break;
                case 414:
                    this.message = "Request-URI Too Large";
                    break;
                case 415:
                    this.message = "Unsupported Media Type";
                    break;
                case 451:
                    this.message = "Parameter Not Understood";
                    break;
                case 452:
                    this.message = "Conference Not Found";
                    break;
                case 453:
                    this.message = "Not Enough Bandwidth";
                    break;
                case 454:
                    this.message = "Session Not Found";
                    break;
                case 455:
                    this.message = "Method Not Valid in This State";
                    break;
                case 456:
                    this.message = "Header Field Not Valid for Resource";
                    break;
                case 457:
                    this.message = "Invalid Range";
                    break;
                case 458:
                    this.message = "Parameter Is Read-Only";
                    break;
                case 459:
                    this.message = "Aggregate operation not allowed";
                    break;
                case 460:
                    this.message = "Only aggregate operation allowed";
                    break;
                case 461:
                    this.message = "Unsupported transport";
                    break;
                case 462:
                    this.message = "Destination unreachable";
                    break;
                case 500:
                    this.message = "Internal Server Error";
                    break;
                case 501:
                    this.message = "Not Implemented";
                    break;
                case 502:
                    this.message = "Bad Gateway";
                    break;
                case 503:
                    this.message = "Service Unavailable";
                    break;
                case 504:
                    this.message = "Gateway Time-out";
                    break;
                case 505:
                    this.message = "RTSP Version not supported";
                    break;
                case 551:
                    this.message = "Option not supported";
                    break;
                default:
                    this.message = "Unknown";
                    break;
            }

        }

        /**
         * Set options
         *
         * @param options This is the options string
         * @return ResponseBuilder
         */
        public ResponseBuilder setOptions(String options) {
            this.options = options;
            return this;
        }

        /**
         * Set session ID
         *
         * @param sessionId This is the session ID
         * @return ResponseBuilder
         */
        public ResponseBuilder setSessionId(int sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        /**
         * Set transport
         *
         * @param transport This is the transport string
         * @return ResponseBuilder
         */
        public ResponseBuilder setTransport(String transport) {
            this.transport = transport;
            return this;
        }

        /**
         * Set content type
         *
         * @param contentType This is the content type string
         * @return ResponseBuilder
         */
        public ResponseBuilder setContentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        /**
         * Set content length
         *
         * @param contentLength This is the content length
         * @return ResponseBuilder
         */
        public ResponseBuilder setContentLength(int contentLength) {
            this.contentLength = contentLength;
            return this;
        }

        /**
         * Set body
         *
         * @param body This is the body string
         * @return ResponseBuilder
         */
        public ResponseBuilder setBody(String body) {
            this.body = body;
            return this;
        }

        /**
         * Build the ServerResponse
         *
         * @return ServerResponse
         */
        public ServerResponse build() {
            return new ServerResponse(this);
        }
    }
}
