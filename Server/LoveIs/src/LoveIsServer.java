import decoder.MessageDecoder;
import encoder.MessageEncoder;
import messages.*;
import utils.ErrorCode;
import utils.User;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

@ServerEndpoint(
        value = "/chat",
        decoders = {MessageDecoder.class},
        encoders = {MessageEncoder.class}
)

public class LoveIsServer {
    private static final UPLOAD_DIR = "/home/ndtho8205/fileUpload/";

    private static final Logger LOGGER = Logger.getLogger(LoveIsServer.class.getName());
    private static final ConcurrentHashMap<String, User> sessionMap = new ConcurrentHashMap<>();

    private FileChannel fileChannel = null;
    private long fileSize;
    private long uploadedFileSize = 0;
    private Path storage;

    public LoveIsServer() {
        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) {
            try {
                Files.createDirectory(dir.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.storage = dir.toPath();
    }

    static void sendMessage(Session session, Message message) {
        try {
            session.getBasicRemote().sendObject(message);
        } catch (IOException | EncodeException e) {
            e.printStackTrace();
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        LOGGER.log(Level.INFO, "Opened connection: {0}", session.getId());
        LOGGER.log(Level.INFO, "Connected: " + session.getId());
        System.out.println();

        try {
            session.getBasicRemote().sendText("Connected!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose(Session session) {
        LOGGER.log(Level.INFO, "Closed connection: {0}", session.getId());
        if (session.getUserProperties().containsKey("username")) {
            String username = (String) session.getUserProperties().get("username");
            User user = sessionMap.get(username);

            user.handleLogoutReq();
            sessionMap.remove(username);
        }
    }

    @OnMessage
    public void onMessage(ByteBuffer msg, Session session) {
        if (fileChannel == null)
            return;
        LOGGER.log(Level.INFO, "ByteBuffer: ", session.getId());

        while (msg.hasRemaining()) {
            try {
                int res = fileChannel.write(msg, uploadedFileSize);
                uploadedFileSize += res;
                System.out.println("File size: " + uploadedFileSize);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println(uploadedFileSize);
        if (uploadedFileSize == fileSize)
            try {
                fileChannel.close();
                LOGGER.log(Level.INFO, "Send file done!");
                //session.getUserProperties().get("username");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                fileChannel = null;
            }

    }

    @OnMessage
    public void onMessage(Message message, Session session) throws IOException, EncodeException {
        synchronized (sessionMap) {
            LOGGER.log(Level.INFO, "Message: " + session.getId() + "\n\t" + message);
            switch (message.getType()) {
                case Message.USER_REGISTRATION_REQ:
                case Message.USER_LOGIN_REQ:
                    UserMsg userMsg = (UserMsg) message;
                    if (session.getUserProperties().containsKey("username") | sessionMap.containsKey(userMsg.getUsername())) {
                        LOGGER.log(Level.SEVERE, "USER_REQ", "Account has already been confirmed.");
                        sendMessage(session, new ErrorMsg(ErrorCode.ALREADY_CONFIRMED));
                        return;
                    }

                    ErrorMsg userValidate = userMsg.validate();
                    if (userValidate == null) {
                        String username = userMsg.getUsername();
                        int userid = userMsg.getId();
                        session.getUserProperties().put("username", username);
                        session.getUserProperties().put("userId", Integer.toString(userid));

                        User user = new User(username, userid, session, sessionMap);
                        sessionMap.put(username, user);

                        sendMessage(session, user.handleLoginReq());
                    } else {
                        sendMessage(session, userValidate);
                    }
                    System.out.println();

                    break;
                case Message.SEND_TEXT:
                    TextMsg textMsg = (TextMsg) message;
                    if (!session.getUserProperties().containsKey("username")) return;
                    String fromUsername = (String) session.getUserProperties().get("username");
                    User fromUser = sessionMap.get(fromUsername);
                    User toUser = sessionMap.get(textMsg.getToUser());
                    textMsg.saveDatabase(fromUser.getUserid(), toUser.getUserid());
                    sendMessage(toUser.session, fromUser.handleChat(toUser, textMsg));
                    break;
                case Message.SEND_FILE:
                    FileMsg fileMsg = (FileMsg) message;
                    Path path = Paths.get(storage.normalize().toString(), fileMsg.getFileName());
                    fileChannel = FileChannel.open(path, EnumSet.of(
                            StandardOpenOption.TRUNCATE_EXISTING,
                            StandardOpenOption.WRITE,
                            StandardOpenOption.CREATE
                    ));
                    fileSize = fileMsg.getFileSize();
                    System.out.println("Fixeddddd: "+ fileSize);
                    uploadedFileSize = 0;
                    break;
            }
        }
    }
}
