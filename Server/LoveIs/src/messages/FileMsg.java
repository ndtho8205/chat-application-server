package server.messages;


import javax.json.JsonObject;

public class FileMsg extends Message {
    private String fromFriend;
    private String toFriend;
    private String fileName;
    private long fileSize;
    private boolean anyStatus;
    private boolean status;

    public FileMsg(JsonObject jsonObject) {
        super(SEND_FILE, jsonObject);
        anyStatus = jsonObject.containsKey("status");
        toFriend = !anyStatus ? jsonObject.getString("toFriend") : null;
        fromFriend = anyStatus ? jsonObject.getString("fromFriend") : null;
        status = anyStatus && jsonObject.getBoolean("status");
        fileName = jsonObject.getString("fileName");
        fileSize = jsonObject.getInt("fileSize");
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getFromFriend() {
        return fromFriend;
    }

    public String getToFriend() {
        return toFriend;
    }

    public void setToFriend(String toFriend) {
        this.toFriend = toFriend;
    }

    public boolean getStatus() {
        return status;
    }

    public boolean haveStatus() {
        return anyStatus;
    }
}
