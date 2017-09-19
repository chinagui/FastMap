package com.navinfo.dataservice.engine.limit.datahub.exception;

/** 
 * @ClassName: AbstractDb 
 * @author Xiao Xiaowen 
 * @date 2015-11-30 下午1:58:50 
 * @Description: TODO
 *  
 */
public class DataHubException extends Exception {
    public DataHubException(String message) {
        super(message);
    }

    public DataHubException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataHubException(Throwable cause) {
        super(cause);
    }
}
