package com.navinfo.dataservice.commons.exception;

/**
 * 
* @ClassName: DataNotChangeException 
* @author Zhang Xiaolong
* @date 2016年7月25日 上午10:32:12 
* @Description: TODO
 */
public class DataNotChangeException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public DataNotChangeException() {
        super();
    }

    public DataNotChangeException(String message) {
        super(message);
    }

    public DataNotChangeException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataNotChangeException(Throwable cause) {
        super(cause);
    }

}