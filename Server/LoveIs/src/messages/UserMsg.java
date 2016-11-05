package server.messages;

import server.utils.DataAccessHelper;
import server.utils.ErrorCode;

import javax.json.JsonObject;

public class UserMsg extends Message {
    private static DataAccessHelper dataAccessHelper = new DataAccessHelper();
    private String username;
    private String password;
    private int id = -1;

    public UserMsg(JsonObject jsonObject) {
        super(jsonObject.getString("type"), jsonObject);
        username = jsonObject.getString("username");
        password = jsonObject.getString("password");
        if (this.type.equals(USER_REGISTRATION_REQ))
            saveDatabase();
        this.id = readId();
    }

    private int readId() {
        String where = "username = '" + username + "' and password = '" + password + "';";
        String id = dataAccessHelper.getValue("id", "users", where);
        return (id == null) ? -1 : Integer.parseInt(id);
    }

    private boolean saveDatabase() {
        //TODO: `users` table just have 2 columns: `username`, `password`
        return dataAccessHelper.insertUser(username, password);
    }

    public ErrorMsg validate() {
        return (id == -1) ? new ErrorMsg(
                (type.equals(USER_REGISTRATION_REQ)) ? ErrorCode.REGISTRATION_FAILED : ErrorCode.VALIDATION_FAILED
        ) : null;
    }

    public String getUsername() {
        return username;
    }

    public int getId() {
        return id;
    }
}
