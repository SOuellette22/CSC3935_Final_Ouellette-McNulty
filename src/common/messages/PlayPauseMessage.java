package common.messages;

import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

import java.io.InvalidObjectException;

public class PlayPauseMessage extends Message {

    private int sessionId;
    /**
     * RTP-Info if sent from the server in a PLAY message or Range if sent from the client in a PLAY message
     */
    private String range_RTPInfo;

    /**
     * Constructor creates a new message from parameters.
     */
    public PlayPauseMessage(String type, String header, int cseq, int sessionId, String range_RTPInfo) {
        super(type, header, cseq);
        this.sessionId = sessionId;
        this.range_RTPInfo = range_RTPInfo;
    }

    /**
     * Constructor creates a new message from parameters without RTP-Info/Range.
     *
     * This is only if the range is not included in play or is a pause messages
     */
    public PlayPauseMessage(String type, String header, int cseq, int sessionId) {
        super(type, header, cseq);
        this.sessionId = sessionId;
        this.range_RTPInfo = null;
    }

    /**
     * Get message session ID
     *
     * @return int of message session ID
     */
    public PlayPauseMessage(JSONObject JSONMessage) {
        super(JSONMessage);

        if (!this.getType().equals("PLAY") && !this.getType().equals("PAUSE")) {
            throw new IllegalArgumentException("Invalid message type for PlayPauseMessage: " + this.getType());
        }
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
     * Get message RTP-Info/Range.
     *
     * @return RTP-Info if sent from the server in a PLAY message or Range if sent from the client in a PLAY message
     */
    public String getRange_RTPInfo() {
        return range_RTPInfo;
    }

    /**
     * Deserializes PlayPauseMessage from JSONObject
     *
     * @param jsonType JSONObject to deserialize
     * @throws InvalidObjectException
     */
    @Override
    public void deserialize(JSONType jsonType) throws InvalidObjectException {
        super.deserialize(jsonType);

        JSONObject obj = (JSONObject) jsonType;

        obj.checkValidity(new String[]{"session-id"});

        // RTP-Info or Range is optional
        if (obj.containsKey("range_RTPInfo")) {
            this.range_RTPInfo = obj.getString("range_RTPInfo");
        }

        this.sessionId = obj.getInt("session-id");

    }

    /**
     * Serializes PlayPauseMessage to JSONObject
     *
     * @return JSONObject representing the PlayPauseMessage
     */    @Override
    public JSONObject toJSONType() {
        JSONObject obj = super.toJSONType();

        obj.put("session-id", sessionId);

        if (range_RTPInfo != null) {
            obj.put("range_RTPInfo", range_RTPInfo);
        }

        return obj;
    }
}
