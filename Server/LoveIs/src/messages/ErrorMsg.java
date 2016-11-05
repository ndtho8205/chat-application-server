package server.messages;

import server.utils.ErrorCode;

import javax.json.Json;

public class ErrorMsg extends Message {
    private int errorCode;
    private String errorDescription;

    public ErrorMsg(ErrorCode errorCode) {
        super(ERROR_MSG, Json.createObjectBuilder()
                .add("type", ERROR_MSG)
                .add("errorCode", errorCode.getCode())
                .add("errorDescription", errorCode.getDescription())
                .build()
        );
        this.errorCode = errorCode.getCode();
        errorDescription = errorCode.getDescription();
    }


    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorDescription() {
        return errorDescription;
    }
}
