package com.navinfo.dataservice.impcore.exception;

/** 
* @ClassName: LockException 
* @author Xiao Xiaowen 
* @date 2016年6月7日 下午7:29:24 
* @Description: TODO
*  
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
