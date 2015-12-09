package com.navinfo.navicommons.workflow.spi;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2010-8-10
 */
public class ProcessException extends Exception 
{
    public ProcessException() {
        super();
    }

    public ProcessException(String message) {
        super(message);
    }

    public ProcessException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProcessException(Throwable cause) {
        super(cause);
    }
}
