package com.navinfo.dataservice.commons.job;

/** 
 * @ClassName: JobException 
 * @author Xiao Xiaowen 
 * @date 2016-1-15 下午2:22:51 
 * @Description: TODO
 */
public class JobException extends Exception {
    public JobException(String message) {
        super(message);
    }

    public JobException(String message, Throwable cause) {
        super(message, cause);
    }

    public JobException(Throwable cause) {
        super(cause);
    }
}
