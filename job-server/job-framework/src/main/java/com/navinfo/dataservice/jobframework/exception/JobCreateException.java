package com.navinfo.dataservice.jobframework.exception;

/** 
 * @ClassName: JobRuntimeException 
 * @author Xiao Xiaowen 
 * @date 2016-1-15 下午2:24:22 
 * @Description: TODO
 */
public class JobCreateException extends Exception {

    public JobCreateException(String message) {
        super(message);
    }

    public JobCreateException(String message, Throwable cause) {
        super(message, cause);
    }

    public JobCreateException(Throwable cause) {
        super(cause);
    }
}
