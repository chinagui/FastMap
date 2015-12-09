package com.navinfo.navicommons.exception;

/**
 * User: liuqing
 * Date: 2010-9-3
 * Time: 15:32:19
 */
public class ConfigParseException extends RuntimeException {
    public ConfigParseException() {
        super();
    }

    public ConfigParseException(String message) {
        super(message);
    }

    public ConfigParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigParseException(Throwable cause) {
        super(cause);
    }

    
}
