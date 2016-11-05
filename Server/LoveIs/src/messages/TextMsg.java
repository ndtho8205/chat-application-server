package messages;

import utils.DataAccessHelper;

import javax.json.JsonObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TextMsg extends Message {
    private static DataAccessHelper dataAccessHelper = new DataAccessHelper();
    private String toUser;
    private String messType;
    private Date time;
    private String text;

    public TextMsg(JsonObject jsonObject) {
        super(Message.SEND_TEXT, jsonObject);
        this.toUser = jsonObject.getString("toFriend");
        this.messType = jsonObject.getString("messType");
        try {
            this.time = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).parse(jsonObject.getString("time"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        this.text = jsonObject.getString("text");
    }

    public String getToUser() {
        return toUser;
    }

    public String getTimeString() {
        return (new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(time));
    }

    public Date getDateTime() {
        return time;
    }
    public String getText() {
        return text;
    }

    public boolean saveDatabase(int fromid, int toid) {
        return dataAccessHelper.insertMessage(fromid, toid, messType, getDateTime(), text);
    }


//    private int readId() {
//        //String where = "username = '" + username + "' and password = '" + password + "';";
//        //String id = dataAccessHelper.getValue("id", "users", where);
//        //return (id == null) ? -1 : Integer.parseInt(id);
//    }
//
//    public boolean validate() {
//        //return (id != -1);
//    }
}
