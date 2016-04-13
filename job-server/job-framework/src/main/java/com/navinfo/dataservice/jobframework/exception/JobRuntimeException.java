package com.navinfo.dataservice.jobframework.exception;

/** 
 * @ClassName: JobRuntimeException 
 * @author Xiao Xiaowen 
 * @date 2016-1-15 下午2:24:22 
 * @Description: TODO
 */
public class JobRuntimeException extends RuntimeException {

    public JobRuntimeException(String message) {
        super(message);
    }

    public JobRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public JobRuntimeException(Throwable cause) {
        super(cause);
    }
}
