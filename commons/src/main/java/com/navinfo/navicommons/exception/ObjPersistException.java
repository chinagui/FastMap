package com.navinfo.navicommons.exception;

/**
 * 
* @ClassName: ObjPersistException 
* @author Xiao Xiaowen 
* @date 2017年8月21日 下午2:53:00 
* @Description: TODO
 */
public class ObjPersistException extends Exception {
    public ObjPersistException(String message) {
        super(message);
    }

    public ObjPersistException(String message, Throwable cause) {
        super(message, cause);
    }

    public ObjPersistException(Throwable cause) {
        super(cause);
    }
}
