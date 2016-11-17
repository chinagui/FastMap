package com.navinfo.dataservice.engine.editplus.bo;

public class CommandCreateException extends Exception{

	private static final long serialVersionUID = 1L;

	public CommandCreateException(String message) {
        super(message);
    }

    public CommandCreateException(String message, Throwable cause) {
        super(message, cause);
    }

    public CommandCreateException(Throwable cause) {
        super(cause);
    }
}
