package com.navinfo.navicommons.exception;

/** 
* @ClassName: ServiceRtException 
* @author Xiao Xiaowen 
* @date 2016年6月6日 下午3:19:05 
* @Description: TODO
*/
public class ServiceRtException  extends RuntimeException {

	private static final long serialVersionUID = 1401593546385403720L;
    private int errorCode;

	public ServiceRtException() {
		super();
	}

	public ServiceRtException(String message) {
		super(message);
	}

	public ServiceRtException(Throwable cause) {
		super(cause);
	}

	public ServiceRtException(String message, Throwable cause) {
		super(message, cause);
	}

    public int getErrorCode() {
        return errorCode;
    }

    public ServiceRtException(int errorCode,String message,Throwable cause)
    {
        super(message,cause);
        this.errorCode = errorCode;
    }
    public ServiceRtException(int errorCode,String message)
    {
        super(message);
        this.errorCode = errorCode;
    }
}
