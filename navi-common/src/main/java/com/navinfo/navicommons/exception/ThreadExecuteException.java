package com.navinfo.navicommons.exception;

/**
 * User: liuqing
 * Date: 2010-9-3
 * Time: 15:32:19
 */
public class ThreadExecuteException extends RuntimeException {
    public ThreadExecuteException() {
        super();
    }

    public ThreadExecuteException(String message) {
        super(message);
    }

    public ThreadExecuteException(String message, Throwable cause) {
        super(message, cause);
    }

    public ThreadExecuteException(Throwable cause) {
        super(cause);
    }

    
}
