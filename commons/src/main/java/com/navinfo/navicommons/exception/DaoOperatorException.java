package com.navinfo.navicommons.exception;

/**
 * User: liuqing
 * Date: 2010-10-29
 * Time: 8:53:28
 */
public class DaoOperatorException extends RuntimeException {
    public DaoOperatorException() {
        super();
    }

    public DaoOperatorException(String message) {
        super(message);
    }

    public DaoOperatorException(String message, Throwable cause) {
        super(message, cause);
    }

    public DaoOperatorException(Throwable cause) {
        super(cause);
    }


}
