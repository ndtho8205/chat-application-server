package messages;


import javax.json.JsonObject;

public class FileMsg extends Message {
    private String toFriend;
    private String fileName;
    private long fileSize;

    public FileMsg(JsonObject jsonObject) {
        super(Message.SEND_FILE, jsonObject);
        toFriend = jsonObject.getString("toFriend");
        fileName = jsonObject.getString("fileName");
        fileSize = jsonObject.getInt("fileSize");
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getToFriend() {
        return toFriend;
    }
}
