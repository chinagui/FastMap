package com.navinfo.dataservice.engine.editplus.glm;

/** 
 * @ClassName: ServiceException 
 * @author Xiao Xiaowen 
 * @date 2015-12-18 上午9:50:07 
 * @Description: TODO
 */
public class GlmTableNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public GlmTableNotFoundException(String message) {
        super(message);
    }

    public GlmTableNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public GlmTableNotFoundException(Throwable cause) {
        super(cause);
    }
}
