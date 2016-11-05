package server;

import server.decoder.MessageDecoder;
import server.encoder.MessageEncoder;
import server.messages.*;
import server.utils.ErrorCode;
import server.utils.User;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.*;
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

    private static final Logger LOGGER = Logger.getLogger(LoveIsServer.class.getName());
    private static final ConcurrentHashMap<String, User> sessionMap = new ConcurrentHashMap<>();

    private static FileMsg _fileMsg;
    private static FileChannel fileChannel = null;
    private static long fileSize;
    private static long uploadedFileSize = 0;
    private static Path storage;

/*
* ByteArrayOutputStream buffer = new ByteArrayOutputStream();
* onMessage(ByteBuffer byteBuffer, boolean complete) {
*   buffer.write(byteBuffer.array());
*   if (complete) {
*      FileOutputStream fos = null;
*      fos = new FileOutputStream("path to file");
*      fos.write(buffer.toByteArray());
*   }
* */
    public static void sendMessage(Session session, Message message) {
        try {
            session.getBasicRemote().sendObject(message);
            LOGGER.log(Level.INFO, "MESSAGE: {0}\n\tSENDED TO: {1}", new Object[]{message, session.getUserProperties().get("username")});
        } catch (IOException | EncodeException e) {
            LOGGER.log(Level.WARNING, "SENDING MESSAGE ERROR: {0}", e.getMessage());
        }
    }

    private void sendFile() {
        LOGGER.log(Level.INFO, "========================================> SEND FILE STARTED: {0} -> {1}",
                new Object[]{_fileMsg.getFromFriend(), _fileMsg.getToFriend()});
        Path path = Paths.get(storage.normalize().toString(), _fileMsg.getFileName());
        File file = new File(path.toUri());
        RandomAccessFile randomAccessFile = null;

        Session receiver = sessionMap.get(_fileMsg.getToFriend()).session;

        try {
            randomAccessFile = new RandomAccessFile(file, "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        assert randomAccessFile != null;
        FileChannel fileChannel = randomAccessFile.getChannel();
        ByteBuffer buffer;
        try {
            long fileSize = fileChannel.size();
            long loop = (fileSize / 1024);
            buffer = ByteBuffer.allocate(1024);
            int i = 0;
            if (loop != 0) {
                while (fileChannel.read(buffer) > 0) {
                    ++i;
                    buffer.flip();
                    receiver.getAsyncRemote().sendBinary(buffer);
//                    buffer.rewind();
//                    receiver.getAsyncRemote().sendBinary(buffer, sendResult -> LOGGER.log(Level.INFO, "ASYNC SEND HANDLER: {0}", sendResult.isOK()));
                    LOGGER.log(Level.INFO, "SENDED SIZE: {0} * 1024", i);
                    buffer.clear();
                    if (i == loop) break;
                }
            }
            buffer = ByteBuffer.allocate((int) (fileSize - 1024 * loop));
            while (fileChannel.read(buffer) > 0) {
                System.out.println();
                buffer.flip();
                receiver.getAsyncRemote().sendBinary(buffer);
//                buffer.rewind();
//                receiver.getAsyncRemote().sendBinary(buffer, sendResult -> LOGGER.log(Level.INFO, "ASYNC SEND HANDLER: {0}", sendResult.isOK()));
                LOGGER.log(Level.INFO, "SENDED SIZE: {0}", (fileSize - 1024 * loop));
                buffer.clear();
            }
            LOGGER.log(Level.INFO, "SEND FILE DONE: {0}", fileSize);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "SEND FILE ERROR: {0}", e.getMessage());
        }
    }

    private void uploadFilePreparation() {
        File dir = new File("/home/ndtho8205/fileUpload/");
        if (!dir.exists()) {
            try {
                Files.createDirectory(dir.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        storage = dir.toPath();
    }

    @OnOpen
    public void onOpen(Session session) {
        LOGGER.log(Level.INFO, "CONNECTED: {0}", session.getId());
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        String username = null;
        if (session.getUserProperties().containsKey("username")) {
            username = (String) session.getUserProperties().get("username");
            User user = sessionMap.get(username);
            if (user != null)
                user.handleOnlineState(false);
            sessionMap.remove(username);
        }
        LOGGER.log(Level.INFO, "CONNECTION CLOSED: {0}\n\tUSER: {1}\n\tCLOSE REASON: {2} - {3}",
                new Object[]{session.getId(), username, reason.getCloseCode(), reason.getReasonPhrase()});
    }

    @OnMessage
    public void onMessage(InputStream is) {
        LOGGER.log(Level.INFO, "========================================> UPLOAD FILE STARTED");
        int count;
        byte[] buff = new byte[1500];
        try {
            if (fileChannel == null) return;

            while (is.available() > 0) {
                count = +is.read(buff);
                ByteBuffer buffer = ByteBuffer.wrap(buff, 0, count);
                try {
                    uploadedFileSize += fileChannel.write(buffer, uploadedFileSize);
                    buffer.clear();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                LOGGER.log(Level.INFO, "UPLOADED SIZE: {0} - {1}", new Object[]{count, uploadedFileSize});
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (uploadedFileSize == fileSize) {
            LOGGER.log(Level.INFO, "FILE UPLOAD DONE: {0}", fileSize);
            try {
                fileChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            fileChannel = null;
            sendFile();
            _fileMsg = null;
        } else
            LOGGER.log(Level.WARNING, "FILE UPLOAD INCOMPLETE");
    }

//    @OnMessage
//    public void onMessage(ByteBuffer msg, Session session) {
//        if (fileChannel == null)
//            return;
//
//        while (msg.hasRemaining()) {
//            try {
//                int res = fileChannel.write(msg, uploadedFileSize);
//                uploadedFileSize += res;
//                System.out.println("File size: " + uploadedFileSize);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        System.out.println(uploadedFileSize);
//        if (uploadedFileSize == fileSize)
//            try {
//                fileChannel.close();
//                LOGGER.log(Level.INFO, "Send file done!");
//                //session.getUserProperties().get("username");
//            } catch (IOException e) {
//                e.printStackTrace();
//            } finally {
//                fileChannel = null;
//            }
//
//    }

    @OnMessage
    public void onMessage(Message message, Session session) throws IOException, EncodeException {
        synchronized (sessionMap) {
            LOGGER.log(Level.INFO, "RECEIVE MESSAGE: {0}\n\tFROM: {1}",
                    new Object[]{message, session.getUserProperties().get("username")});
            User fromUser = null;
            User toUser = null;

            switch (message.getType()) {
                case Message.USER_REGISTRATION_REQ:
                case Message.USER_LOGIN_REQ:
                    UserMsg userMsg = (UserMsg) message;

                    if (session.getUserProperties().containsKey("username") | sessionMap.containsKey(userMsg.getUsername())) {
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

                    break;
                case Message.SEND_TEXT:
                    TextMsg textMsg = (TextMsg) message;

                    if (!session.getUserProperties().containsKey("username")) return;

                    String fromUsername = (String) session.getUserProperties().get("username");
                    fromUser = sessionMap.get(fromUsername);
                    toUser = sessionMap.containsKey(textMsg.getToUser()) ? sessionMap.get(textMsg.getToUser()) : null;
                    textMsg.saveDatabase(fromUser.getUserid(), toUser != null ? toUser.getUserid() : -1);
                    if (toUser != null)
                        sendMessage(toUser.session, fromUser.handleChat(textMsg));

                    break;
                case Message.SEND_FILE:
                    FileMsg fileMsg = (FileMsg) message;

                    if (!session.getUserProperties().containsKey("username")) return;

                    if (fileMsg.haveStatus()) {
                        fromUser = sessionMap.get(fileMsg.getFromFriend());
                        toUser = sessionMap.get(String.valueOf(session.getUserProperties().get("username")));
                        if (fileMsg.getStatus()) {
                            fileMsg.setToFriend(toUser.getUsername());
                            _fileMsg = fileMsg;
                            uploadFilePreparation();
                            Path path = Paths.get(storage.normalize().toString(), fileMsg.getFileName());
                            fileChannel = FileChannel.open(path, EnumSet.of(
                                    StandardOpenOption.TRUNCATE_EXISTING,
                                    StandardOpenOption.WRITE,
                                    StandardOpenOption.CREATE
                            ));
                            fileSize = fileMsg.getFileSize();
                            uploadedFileSize = 0;
                        }
                        sendMessage(fromUser.session, toUser.handleAllowTransferFile(fileMsg));
                    } else {
                        fromUser = sessionMap.get(String.valueOf(session.getUserProperties().get("username")));
                        toUser = sessionMap.get(fileMsg.getToFriend());
                        sendMessage(toUser.session, fromUser.handleTransferFile(fileMsg));
                    }

                    break;
            }
        }
    }
}
