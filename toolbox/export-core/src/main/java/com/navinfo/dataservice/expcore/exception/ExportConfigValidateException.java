package com.navinfo.dataservice.expcore.exception;

/** 
 * @ClassName: ExportConfigValidateException 
 * @author Xiao Xiaowen 
 * @date 2015-10-26 下午3:12:58 
 * @Description: TODO
 *  
 */
public class ExportConfigValidateException extends Exception {
    public ExportConfigValidateException(String message) {
        super(message);
    }

    public ExportConfigValidateException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExportConfigValidateException(Throwable cause) {
        super(cause);
    }
}
