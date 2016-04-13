package com.navinfo.navicommons.exception;

/**
 * User: liuqing
 * Date: 2010-9-3
 * Time: 15:32:19
 */
public class DMSException extends RuntimeException {
    public DMSException() {
        super();
    }

    public DMSException(String message) {
        super(message);
    }

    public DMSException(String message, Throwable cause) {
        super(message, cause);
    }

    public DMSException(Throwable cause) {
        super(cause);
    }

    
}
