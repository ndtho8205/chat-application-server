package messages;

import utils.ErrorCode;

import javax.json.Json;

public class ErrorMsg extends Message {

    public ErrorMsg(ErrorCode errorCode) {
        super(Message.ERROR_MSG, Json.createObjectBuilder()
                .add("type", Message.ERROR_MSG)
                .add("errorCode", errorCode.getCode())
                .add("errorDescription", errorCode.getDescription())
                .build()
        );
    }
}
