package server.utils;

import server.LoveIsServer;
import server.messages.FileMsg;
import server.messages.Message;
import server.messages.TextMsg;
import server.messages.UserUpdate;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.sql.rowset.CachedRowSet;
import javax.websocket.Session;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class User {

    private static DataAccessHelper dataAccessHelper = new DataAccessHelper();

    public final Session session;
    private final String username;
    private final int userid;

    private Map<String, User> friendsList = new HashMap<>();

    public User(String username, int userid, Session session, ConcurrentHashMap<String, User> sessionMap) {
        this.username = username;
        this.userid = userid;
        this.session = session;
        initFriendsList(sessionMap);
    }

    public int getUserid() {
        return userid;
    }

    public String getUsername() {
        return username;
    }

    public void handleOnlineState(boolean state) {
        friendsList.entrySet().stream().filter(entry -> entry.getValue() != null).forEach(entry -> entry.getValue().handleOnlineState(this, state));
    }

    public void handleOnlineState(User friend, boolean state) {
        if (state)
            friendsList.put(friend.getUsername(), friend);
        else
            friendsList.put(friend.getUsername(), null);
        LoveIsServer.sendMessage(this.session, UserUpdate.updateOnlineState(friend.getUsername(), state));
    }

    private void initFriendsList(ConcurrentHashMap<String, User> sessionMap) {
        String select = "SELECT id_user1, id_user2 FROM LoveIsSchema.friend_list WHERE ";
        String where = "id_user1 = '" + userid + "' OR " + "id_user2 = '" + userid + "';";
        String sql = select + where;
        CachedRowSet data = dataAccessHelper.select(sql);
        try {
            while (data != null && data.next()) {
                String friendname = dataAccessHelper.getValue("username",
                        "users", "id = '" + data.getString((Integer.parseInt(data.getString(1)) == userid) ? 2 : 1) + "';");
                User friend = (sessionMap.containsKey(friendname)) ? sessionMap.get(friendname) : null;
                if (friend != null)
                    friend.handleOnlineState(this, true);
                friendsList.put(friendname, friend);

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Message handleLoginReq() {
        JsonArrayBuilder friendArray = Json.createArrayBuilder();
        friendsList.keySet().forEach(
                x -> friendArray.add(
                        Json.createObjectBuilder()
                                .add("friendname", x)
                                .add("online", friendsList.get(x) != null)
                                .build()
                )
        );
        JsonObject jsonObject = Json.createObjectBuilder()
                .add("type", Message.USER_RES)
                .add("sessionId", session.getId())
                .add("friendList", friendArray.build())
                .build();

        return new Message(Message.USER_RES, jsonObject);
    }

    public Message handleChat(TextMsg textMsg) {
        JsonObject jsonObject = Json.createObjectBuilder()
                .add("type", Message.SEND_TEXT)
                .add("time", textMsg.getTimeString())
                .add("messType", "peer")
                .add("fromFriend", username)
                .add("text", textMsg.getText())
                .build();
        return new Message(Message.SEND_TEXT, jsonObject);
    }

    public Message handleTransferFile(FileMsg fileMsg) {
        JsonObject jsonObject = Json.createObjectBuilder()
                .add("type", Message.SEND_FILE)
                .add("fileName", fileMsg.getFileName())
                .add("fileSize", fileMsg.getFileSize())
                .add("fromFriend", username)
                .build();
        return new Message(Message.SEND_FILE, jsonObject);
    }

    public Message handleAllowTransferFile(FileMsg fileMsg) {
        JsonObject jsonObject = Json.createObjectBuilder()
                .add("type", Message.SEND_FILE)
                .add("fileName", fileMsg.getFileName())
                .add("fileSize", fileMsg.getFileSize())
                .add("toFriend", username)
                .add("status", fileMsg.getStatus())
                .build();
        return new Message(Message.SEND_FILE, jsonObject);
    }
}
