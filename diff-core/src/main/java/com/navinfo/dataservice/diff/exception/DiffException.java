package com.navinfo.dataservice.diff.exception;

/** 
 * @ClassName: DiffException 
 * @author Xiao Xiaowen 
 * @date 2015-11-30 下午1:58:50 
 * @Description: TODO
 *  
 */
public class DiffException extends Exception {
    public DiffException(String message) {
        super(message);
    }

    public DiffException(String message, Throwable cause) {
        super(message, cause);
    }

    public DiffException(Throwable cause) {
        super(cause);
    }
}
