package me.ihqqq.identity_service.exception;

public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error"),
    INVALID_KEY(1001, "Invalid key"),
    USER_EXISTED(1002, "User already existed"),
    USERNAME_INVALID(1003, "Username must be at least 3 characters long"),
    INVALID_PASSWORD(1004, "Password must be at least 8 characters long")
    ;
    private int code;
    private String message;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
