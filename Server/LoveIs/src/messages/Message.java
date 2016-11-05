package messages;

import javax.json.JsonException;
import javax.json.JsonObject;

public class Message {
    public static final String USER_REGISTRATION_REQ = "userRegReq";
    public static final String USER_LOGIN_REQ = "userLoginReq";
    public static final String SEND_TEXT = "text";
    public static final String SEND_FILE = "file";

    public static final String USER_RES = "userRes";

    public static final String USER_UPDATE = "userUpdate";

    public static final String ERROR_MSG = "error";

    protected JsonObject jsonObject;
    protected String type;

    public Message(String type, JsonObject jsonObject) {
        this.type = type;
        this.jsonObject = jsonObject;
    }

    public String getType() {
        return type;
    }

    public boolean has(String key) {
        try {
            jsonObject.isNull(key);
            return true;
        } catch (JsonException e) {
            return false;
        }
    }

    public void setJsonObject(JsonObject jsonObject) {
        this.jsonObject = jsonObject;
    }
    @Override
    public String toString() {
        return jsonObject.toString();
    }
}
