package com.navinfo.navicommons.exception;

/**
 * User: liuqing
 * Date: 2010-9-3
 * Time: 15:32:19
 */
public class PropertiesParseException extends RuntimeException {
    public PropertiesParseException() {
        super();
    }

    public PropertiesParseException(String message) {
        super(message);
    }

    public PropertiesParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public PropertiesParseException(Throwable cause) {
        super(cause);
    }

    
}
