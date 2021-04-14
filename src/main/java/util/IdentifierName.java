package util;

public enum IdentifierName {
    USER_ID("user_id"),
    ACCOUNT_ID("account_id"),
    FILE_ID("file_id"),
    EVENT_ID("event_id");

    private String endPoint;

    IdentifierName(String endPoint) {
        this.endPoint = endPoint;
    }

    public String getKeyName() {
        return endPoint;
    }
}
