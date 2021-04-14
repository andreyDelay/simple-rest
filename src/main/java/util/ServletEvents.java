package util;

public enum ServletEvents {
    FILE_DELETED("File was successfully deleted from repo!"),
    FILE_UPDATED("File was successfully updated!"),
    FILE_UPLOADED("File was successfully uploaded to repo!"),
    FILE_UPDATE_ERROR("File not updated, something went wrong!"),
    FILE_UPLOAD_ERROR("File not uploaded, something went wrong!"),
    FILE_DELETE_ERROR("File not deleted, something went wrong!"),
    FILE_NOT_FOUND("File not fount in the repo directory!"),
    ID_ERROR("Error! Check if you sent required identifiers!"),

    ACCOUNT_UPDATED("Account was successfully updated!"),
    ACCOUNT_UPDATE_ERROR("Account wasn't updated, something went wrong!"),
    ACCOUNT_CREATED("Account created!"),
    ACCOUNT_USER_CREATION_ERROR("Error, check if you sent required parameters!\n" +
                                        "Required parameters:\n" +
                                        "1. login\n" +
                                        "2. username\n" +
                                        "3. surname\n" +
                                        "4. age"),
    ACCOUNT_DELETED("Account was deleted!"),
    ACCOUNT_DELETE_ERROR("Error, account wasn't deleted!"),

    USER_UPDATED("Success! Your personal data was updated!"),
    ACCOUNT_NOT_FOUND("Such user wasn't found in database!");


    private String operation;

    ServletEvents(String operation) {
        this.operation = operation;
    }
}
