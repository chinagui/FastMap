package com.navinfo.dataservice.datalock.exception;

/** 
 * @ClassName: LockException 
 * @author Xiao Xiaowen 
 * @date 2015-12-18 上午9:50:07 
 * @Description: TODO
 */
public class LockException extends Exception {
    public LockException(String message) {
        super(message);
    }

    public LockException(String message, Throwable cause) {
        super(message, cause);
    }

    public LockException(Throwable cause) {
        super(cause);
    }
}
