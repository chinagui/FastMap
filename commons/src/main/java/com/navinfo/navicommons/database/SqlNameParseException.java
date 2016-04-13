package com.navinfo.navicommons.database;

/**
 * User: liuqing
 * Date: 2010-9-3
 * Time: 15:32:19
 */
public class SqlNameParseException extends RuntimeException {
    public SqlNameParseException() {
        super();
    }

    public SqlNameParseException(String message) {
        super(message);
    }

    public SqlNameParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public SqlNameParseException(Throwable cause) {
        super(cause);
    }

    
}
