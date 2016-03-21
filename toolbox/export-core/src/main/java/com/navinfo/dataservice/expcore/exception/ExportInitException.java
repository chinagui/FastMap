package com.navinfo.dataservice.expcore.exception;

public class ExportInitException extends Exception {
    public ExportInitException(String message) {
        super(message);
    }

    public ExportInitException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExportInitException(Throwable cause) {
        super(cause);
    }
}
