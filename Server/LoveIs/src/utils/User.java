package utils;

import messages.Message;
import messages.TextMsg;

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
    private final String username;
    private final int userid;
    public final Session session;
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

    public void onlineState(String friendname, User friendUser, Boolean online) {
        friendsList.put(friendname, friendUser);
        //friendUser.session.getBasicRemote().sendObject();
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
                    friend.onlineState(username, this, true);
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

    public void handleLogoutReq() {

    }

    public Message handleChat(User toUser, TextMsg textMsg) {
        JsonObject jsonObject = Json.createObjectBuilder()
                .add("type", Message.SEND_TEXT)
                .add("time", textMsg.getTimeString())
                .add("messType", "peer")
                .add("fromFriend", username)
                .add("text", textMsg.getText())
                .build();
        System.out.println(toUser.getUsername());
        System.out.println(jsonObject.toString());
        return new Message(Message.SEND_TEXT, jsonObject);
    }

}
