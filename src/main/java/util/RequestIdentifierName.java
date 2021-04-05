package util;

public enum RequestIdentifierName {
    USER_ID("user_id"),
    ACCOUNT_ID("account_id"),
    FILE_ID("file_id"),
    EVENT_ID("event_id");

    private String endPoint;

    RequestIdentifierName(String endPoint) {
        this.endPoint = endPoint;
    }

    public String getKeyName() {
        return endPoint;
    }
}
