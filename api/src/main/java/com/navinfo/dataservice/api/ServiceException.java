package com.navinfo.dataservice.api;

/** 
 * @ClassName: LockException 
 * @author Xiao Xiaowen 
 * @date 2015-12-18 上午9:50:07 
 * @Description: TODO
 */
public class ServiceException extends Exception {
    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceException(Throwable cause) {
        super(cause);
    }
}
