package com.navinfo.dataservice.engine.limit.commons.exception;

/**
 * Created by IntelliJ IDEA.
 * User: liuqing
 * Date: 11-5-9
 * Time: 上午10:53
 * To change this template use File | Settings | File Templates.
 */
public class DataSourceException extends RuntimeException {
    public DataSourceException() {
        super();
    }

    public DataSourceException(String message) {
        super(message);
    }

    public DataSourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataSourceException(Throwable cause) {
        super(cause);
    }

}