package server.messages;

import javax.json.Json;

public class UserUpdate {
    private static String messageType = Message.USER_UPDATE;

    public static Message updateOnlineState(String friendName, boolean state) {
        return new Message(messageType, Json.createObjectBuilder()
                .add("type", messageType)
                .add("friendname", friendName)
                .add("online", state)
                .build()
        );
    }

    public static Message updateStatus(String friendName, String status) {
        return new Message(messageType, Json.createObjectBuilder()
                .add("type", messageType)
                .add("friendname", friendName)
                .add("status", status)
                .build()
        );
    }

}
