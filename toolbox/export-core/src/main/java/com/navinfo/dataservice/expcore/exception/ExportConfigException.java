package com.navinfo.dataservice.expcore.exception;

public class ExportConfigException extends Exception {
    public ExportConfigException(String message) {
        super(message);
    }

    public ExportConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExportConfigException(Throwable cause) {
        super(cause);
    }
}
