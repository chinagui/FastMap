package com.navinfo.dataservice.engine.editplus.glm;

/** 
 * @ClassName: NonObjPidException 
 * @author Xiao Xiaowen 
 * @date 2015-12-18 上午9:50:07 
 * @Description: TODO
 */
public class NonObjPidException extends Exception {

	private static final long serialVersionUID = 1L;

	public NonObjPidException(String message) {
        super(message);
    }

    public NonObjPidException(String message, Throwable cause) {
        super(message, cause);
    }

    public NonObjPidException(Throwable cause) {
        super(cause);
    }
}
