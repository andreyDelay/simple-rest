package filters.urlconstants;

public enum URLPatterns {
    USERS_AND_ANY_OF_ACCOUNTS_FILES_EVENTS("^*(/users)/?(events|files|accounts)/?"),
    EXPERIMENT("^*(/)(users|events|files|accounts)/?");

    private String urlPattern;
    URLPatterns(String pattern) {
        this.urlPattern = pattern;
    }
    public String getPattern() {
        return urlPattern;
    }

}
