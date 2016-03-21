package com.navinfo.dataservice.expcore.exception;

/** 
 * @ClassName: ExportInputException 
 * @author Xiao Xiaowen 
 * @date 2015-11-2 上午11:44:41 
 * @Description: TODO
 *  
 */
public class ExportInputException extends Exception{
    public ExportInputException(String message) {
        super(message);
    }

    public ExportInputException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExportInputException(Throwable cause) {
        super(cause);
    }
}
