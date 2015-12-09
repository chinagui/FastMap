package com.navinfo.navicommons.resource;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2010-11-15
 */
public class ResourceLockException extends RuntimeException 
{
    public ResourceLockException() {
        super();
    }

    public ResourceLockException(String message) {
        super(message);
    }

    public ResourceLockException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResourceLockException(Throwable cause) {
        super(cause);
    }
}
