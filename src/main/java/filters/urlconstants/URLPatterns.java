package filters.urlconstants;

public enum URLPatterns {
    //В итоге класс нормально так и не применился после значительных изменений
    USERS_AND_ANY_OF_ACCOUNTS_FILES_EVENTS("^(/rest/users)/?(events|files|accounts)/?"),
    //**************************************************************
/*    FILES_FILES_ID("^(/rest/files)/?([1-9]+(\\d+)?)?/?"),*/
    //**************************************************************/*    ACCOUNTS_ACCOUNTS_ID("^(/rest/accounts)/?([1-9]+(\\d+)?)?/?"),*/
    //**************************************************************
/*    EVENTS_EVENTS_ID("^(/rest/events)/?([1-9]+(\\d+)?)?/?"),*/
    SINGLE_ENTITY("^(/rest/)(users|files|events|accounts)/?");

    private String urlPattern;

    URLPatterns(String pattern) {
        this.urlPattern = pattern;
    }

    public String getPattern() {
        return urlPattern;
    }

}
