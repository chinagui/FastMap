package com.navinfo.dataservice.diff.exception;

/** 
 * @ClassName: DiffException 
 * @author Xiao Xiaowen 
 * @date 2015-11-30 下午1:58:50 
 * @Description: TODO
 *  
 */
public class InitException extends Exception {
    public InitException(String message) {
        super(message);
    }

    public InitException(String message, Throwable cause) {
        super(message, cause);
    }

    public InitException(Throwable cause) {
        super(cause);
    }
}
