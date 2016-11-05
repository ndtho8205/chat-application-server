package server.decoder;

import server.messages.FileMsg;
import server.messages.Message;
import server.messages.TextMsg;
import server.messages.UserMsg;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageDecoder implements Decoder.Text<Message> {

    private static final Logger LOGGER = Logger.getLogger(MessageDecoder.class.getName());

    @Override
    public Message decode(String s) throws DecodeException {
        try {
            JsonObject jsonObject = Json.createReader(new StringReader(s)).readObject();
            Message msg = null;

            switch (jsonObject.getString("type")) {
                case Message.USER_REGISTRATION_REQ:
                case Message.USER_LOGIN_REQ:
                    msg = new UserMsg(jsonObject);
                    break;
                case Message.SEND_TEXT:
                    msg = new TextMsg(jsonObject);
                    break;
                case Message.SEND_FILE:
                    msg = new FileMsg(jsonObject);
                    break;
            }

            return msg;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "DECODE ERROR: {0}", e.getMessage());
            return null;
        }
    }

    @Override
    public boolean willDecode(String s) {
        try {
            Json.createReader(new StringReader(s)).readObject();
            return true;
        } catch (JsonException e) {
            LOGGER.log(Level.WARNING, "DECODE ERROR: {0}", e.getMessage());
            return false;
        }
    }

    @Override
    public void init(EndpointConfig endpointConfig) {

    }

    @Override
    public void destroy() {

    }
}
