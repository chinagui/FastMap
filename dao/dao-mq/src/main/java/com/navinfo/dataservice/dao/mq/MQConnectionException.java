package com.navinfo.dataservice.dao.mq;

/** 
 * @ClassName: LockException 
 * @author Xiao Xiaowen 
 * @date 2015-12-18 上午9:50:07 
 * @Description: TODO
 */
public class MQConnectionException extends Exception {
    public MQConnectionException(String message) {
        super(message);
    }

    public MQConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public MQConnectionException(Throwable cause) {
        super(cause);
    }
}
