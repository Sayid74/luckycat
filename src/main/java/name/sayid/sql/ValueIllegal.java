package name.sayid.sql;

public class ValueIllegal extends Exception{
    public ValueIllegal() {
    }

    public ValueIllegal(String message) {
        super(message);
    }

    public ValueIllegal(String message, Throwable cause) {
        super(message, cause);
    }

    public ValueIllegal(Throwable cause) {
        super(cause);
    }

    public ValueIllegal(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
