package com.navinfo.navicommons.exception;

/**
 * Service层公用的Exception.
 *
 * 继承自RuntimeException, 从由Spring管理事务的函数中抛出时会触发事务回滚.
 *
 * @author calvin
 */
public class ServiceException extends RuntimeException {

	private static final long serialVersionUID = 1401593546385403720L;
    private int errorCode;

	public ServiceException() {
		super();
	}

	public ServiceException(String message) {
		super(message);
	}

	public ServiceException(Throwable cause) {
		super(cause);
	}

	public ServiceException(String message, Throwable cause) {
		super(message, cause);
	}

    public int getErrorCode() {
        return errorCode;
    }

    public ServiceException(int errorCode,String message,Throwable cause)
    {
        super(message,cause);
        this.errorCode = errorCode;
    }
    public ServiceException(int errorCode,String message)
    {
        super(message);
        this.errorCode = errorCode;
    }
}
