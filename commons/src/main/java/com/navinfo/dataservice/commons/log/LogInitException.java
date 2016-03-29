package com.navinfo.dataservice.commons.log;

/**
 * Created by IntelliJ IDEA.
 * User: liuqing
 * Date: 11-5-9
 * Time: 上午10:53
 * To change this template use File | Settings | File Templates.
 */
public class LogInitException extends RuntimeException {
    public LogInitException() {
        super();
    }

    public LogInitException(String message) {
        super(message);
    }

    public LogInitException(String message, Throwable cause) {
        super(message, cause);
    }

    public LogInitException(Throwable cause) {
        super(cause);
    }

}