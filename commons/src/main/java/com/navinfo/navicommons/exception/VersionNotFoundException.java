package com.navinfo.navicommons.exception;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2011-1-11
 */
public class VersionNotFoundException extends Exception
{
    public VersionNotFoundException() {
        super();
    }

    public VersionNotFoundException(String message) {
        super(message);
    }

    public VersionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public VersionNotFoundException(Throwable cause) {
        super(cause);
    }
}
