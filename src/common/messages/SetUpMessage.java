package common.messages;

import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

import java.io.InvalidObjectException;

public class SetUpMessage extends Message {

    private String transport;
    private int sessionId;

    /**
     * Constructor creates a new message from parameters
     */
    public SetUpMessage(String type, String header, int cseq, String transport, int sessionId) {
        super(type, header, cseq);
        this.sessionId = sessionId;
        this.transport = transport;
    }

    /**
     * Constructor creates a new message from parameters without session ID.
     *
     * This is really only for client-side SETUP messages
     */
    public SetUpMessage(String type, String header, int cseq, String transport) {
        super(type, header, cseq);
        this.sessionId = -1; // Indicates no session ID assigned yet
        this.transport = transport;
    }

    /**
     * Constructor creates a new message object from JSONobject by deserializing it
     *
     * @param JSONMessage JSONObject representing the message
     */
    public SetUpMessage(JSONObject JSONMessage) {
        super(JSONMessage);

        if (!this.getType().equals("SETUP")) {
            throw new IllegalArgumentException("Invalid message type for SetUpMessage: " + this.getType());
        }
    }

    /**
     * Get message transport
     *
     * @return String of message transport
     */
    public String getTransport() {
        return transport;
    }

    /**
     * Get message session ID
     *
     * @return int of message session ID
     */
    public int getSessionId() {
        return sessionId;
    }

    /**
     * Deserializes SetUpMessage from JSONObject
     *
     * @param jsonType JSONObject to deserialize
     * @throws InvalidObjectException
     */
    @Override
    public void deserialize(JSONType jsonType) throws InvalidObjectException {
        super.deserialize(jsonType);

        JSONObject obj = (JSONObject) jsonType;

        obj.checkValidity(new String[]{"transport"});

        // Session ID is only from the server
        if (obj.containsKey("session-id")) {
            this.sessionId = obj.getInt("session-id");
        }

        this.transport = obj.getString("transport");
    }

    /**
     * Serializes SetUpMessage to JSONObject
     *
     * @return JSONObject representing the SetUpMessage
     */
    @Override
    public JSONObject toJSONType() {
        JSONObject obj = super.toJSONType();

        obj.put("transport", this.transport);

        // Only include session ID if it has been assigned
        if (this.sessionId == -1) {
            obj.put("session-id", this.sessionId);
        }

        return obj;
    }
}
