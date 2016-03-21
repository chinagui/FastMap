package com.navinfo.dataservice.expcore.exception;

public class ExportTargetInitException extends Exception {
    public ExportTargetInitException(String message) {
        super(message);
    }

    public ExportTargetInitException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExportTargetInitException(Throwable cause) {
        super(cause);
    }
}
