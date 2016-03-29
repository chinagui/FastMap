package com.navinfo.dataservice.jobframework.exception;

/** 
 * @ClassName: JobRuntimeException 
 * @author Xiao Xiaowen 
 * @date 2016-1-15 下午2:24:22 
 * @Description: TODO
 */
public class JobTypeNotFoundException extends Exception {

    public JobTypeNotFoundException(String message) {
        super(message);
    }

    public JobTypeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public JobTypeNotFoundException(Throwable cause) {
        super(cause);
    }
}
