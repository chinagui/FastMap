package com.navinfo.dataservice.dao.plus.operation;

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
