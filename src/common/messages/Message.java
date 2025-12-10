package common.messages;

import merrimackutil.json.JSONSerializable;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

import java.io.InvalidObjectException;

public class Message implements JSONSerializable {

    private String type;
    private String header;
    private int cseq;

    /**
     * Constructor creates a new message from parameters
     */
    public Message (String type, String header, int cseq) {
        this.type = type;
        this.header = header;
        this.cseq = cseq;
    }

    /**
     * Constructor creates a new message object from JSONobject by deserializing it
     *
     * @param JSONMessage JSONObject representing the message
     */
    public Message(JSONObject JSONMessage) {
        try {
            deserialize(JSONMessage);
        } catch (InvalidObjectException e) {
            throw new IllegalArgumentException(e);
        }
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

    /**
     * Deserialize JSONType into Message object
     *
     * @param jsonType JSONType to deserialize
     * @throws InvalidObjectException if jsonType is not a valid Message representation
     */
    @Override
    public void deserialize(JSONType jsonType) throws InvalidObjectException {

        if (!(jsonType instanceof JSONObject)) {
            throw new InvalidObjectException("Expected JSONObject for Message deserialization");
        }

        JSONObject obj = (JSONObject) jsonType;

        obj.checkValidity(new String[]{"type", "header", "cseq"});

        this.type = obj.getString("type");
        this.header = obj.getString("header");
        this.cseq = obj.getInt("cseq");
    }

    /**
     * Serialize Message object into JSONType
     *
     * @return JSONType representation of Message
     */
    @Override
    public JSONObject toJSONType() {
        JSONObject obj = new JSONObject();
        obj.put("type", type);
        obj.put("header", header);
        obj.put("cseq", cseq);
        return obj;
    }
}
