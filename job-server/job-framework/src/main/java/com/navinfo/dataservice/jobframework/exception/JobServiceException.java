package com.navinfo.dataservice.jobframework.exception;

/** 
 * @ClassName: LockException 
 * @author Xiao Xiaowen 
 * @date 2015-12-18 上午9:50:07 
 * @Description: TODO
 */
public class JobServiceException extends Exception {
    public JobServiceException(String message) {
        super(message);
    }

    public JobServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public JobServiceException(Throwable cause) {
        super(cause);
    }
}
