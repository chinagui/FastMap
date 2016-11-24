package com.navinfo.dataservice.engine.editplus.glm;

/** 
 * @ClassName: NonGeoPidException 
 * @author Xiao Xiaowen 
 * @date 2015-12-18 上午9:50:07 
 * @Description: TODO
 */
public class NonGeoPidException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public NonGeoPidException(String message) {
        super(message);
    }

    public NonGeoPidException(String message, Throwable cause) {
        super(message, cause);
    }

    public NonGeoPidException(Throwable cause) {
        super(cause);
    }
}
