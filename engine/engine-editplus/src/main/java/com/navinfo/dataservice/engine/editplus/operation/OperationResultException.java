package com.navinfo.dataservice.engine.editplus.operation;

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
