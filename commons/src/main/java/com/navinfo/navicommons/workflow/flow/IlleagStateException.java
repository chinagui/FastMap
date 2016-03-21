package com.navinfo.navicommons.workflow.flow;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2010-8-13
 */
public class IlleagStateException extends Exception 
{
    public IlleagStateException() {
        super();
    }

    public IlleagStateException(String message) {
        super(message);
    }

    public IlleagStateException(String message, Throwable cause) {
        super(message, cause);
    }

    public IlleagStateException(Throwable cause) {
        super(cause);
    }
}
