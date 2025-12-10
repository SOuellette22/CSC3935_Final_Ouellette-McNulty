package common.messages;

import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

import java.io.InvalidObjectException;

public class OptionMessage extends Message {

    private String options;

    /**
     * Constructor creates a new message from parameters
     */
    public OptionMessage(String type, String header, int cseq, String optoins) {
        super(type, header, cseq);
        this.options = optoins;
    }

    /**
     * Constructor creates a new message object from JSONobject by deserializing it
     *
     * @param JSONMessage JSONObject representing the message
     */
    public OptionMessage(JSONObject JSONMessage) {
        super(JSONMessage);

        // Validate message type
        if (!this.getType().equals("OPTION")) {
            throw new IllegalArgumentException("Invalid message type for OptionMessage: " + this.getType());
        }

    }

    /**
     * Get message options
     *
     * @return String of message options
     */
    public String getOptions() {
        return options;
    }

    @Override
    public void deserialize(JSONType jsonType) throws InvalidObjectException {
        super.deserialize(jsonType);

        JSONObject obj = (JSONObject) jsonType;

        if (obj.containsKey("options")) {
            this.options = obj.getString("options");
        }

    }

    @Override
    public JSONObject toJSONType() {
        JSONObject obj = super.toJSONType();

        obj.put("options", this.options);

        return obj;
    }
}
