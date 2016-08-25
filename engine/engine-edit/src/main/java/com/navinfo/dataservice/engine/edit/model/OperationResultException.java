package com.navinfo.dataservice.engine.edit.model;

public class OperationResultException extends Exception{
    public OperationResultException(String message) {
        super(message);
    }

    public OperationResultException(String message, Throwable cause) {
        super(message, cause);
    }

    public OperationResultException(Throwable cause) {
        super(cause);
    }
}
