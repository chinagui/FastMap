package com.navinfo.dataservice.commons.exception;

/** 
 * @ClassName: LockException 
 * @author Xiao Xiaowen 
 * @date 2015-12-18 上午9:50:07 
 * @Description: TODO
 */
public class ConnectionException extends Exception {
    public ConnectionException(String message) {
        super(message);
    }

    public ConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConnectionException(Throwable cause) {
        super(cause);
    }
}
