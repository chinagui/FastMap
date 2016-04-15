package com.navinfo.navicommons.exception;

/**
 * User: liuqing
 * Date: 2010-10-29
 * Time: 8:53:28
 */
public class DAOException extends RuntimeException {
    public DAOException() {
        super();
    }

    public DAOException(String message) {
        super(message);
    }

    public DAOException(String message, Throwable cause) {
        super(message, cause);
    }

    public DAOException(Throwable cause) {
        super(cause);
    }


}
