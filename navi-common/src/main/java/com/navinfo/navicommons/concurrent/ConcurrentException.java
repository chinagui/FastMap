package com.navinfo.navicommons.concurrent;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2010-8-31
 */
public class ConcurrentException extends Exception 
{
    public ConcurrentException() {
        super();
    }

    public ConcurrentException(String message) {
        super(message);
    }

    public ConcurrentException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConcurrentException(Throwable cause) {
        super(cause);
    }
}
