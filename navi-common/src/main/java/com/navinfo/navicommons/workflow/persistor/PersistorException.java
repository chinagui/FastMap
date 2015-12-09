package com.navinfo.navicommons.workflow.persistor;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2010-8-10
 */
public class PersistorException extends RuntimeException
{
    public PersistorException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public PersistorException(Throwable cause)
    {
        super(cause);
    }

    public PersistorException(String message)
    {
        super(message);
    }

    public PersistorException()
    {
        super();
    }
}
