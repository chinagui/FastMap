package com.navinfo.dataservice.engine.editplus.model.obj;

/** 
 * @ClassName: NonGeoPidException 
 * @author Xiao Xiaowen 
 * @date 2015-12-18 上午9:50:07 
 * @Description: TODO
 */
public class WrongOperationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public WrongOperationException(String message) {
        super(message);
    }

    public WrongOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public WrongOperationException(Throwable cause) {
        super(cause);
    }
}
