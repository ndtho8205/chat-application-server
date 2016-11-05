package utils;

public enum ErrorCode {
    OK(0, "Success"),

    REGISTRATION_FAILED(10, "Registration failed."),
    VALIDATION_FAILED(11, "The username or password for Love Is is incorrect."),
    ALREADY_CONFIRMED(12, "This account has already been confirmed.");



    private final int code;
    private final String description;

    ErrorCode(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return code + ": " + description;
    }
}
