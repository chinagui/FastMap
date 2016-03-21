package com.navinfo.navicommons.net.http;

import org.apache.commons.lang.StringUtils;

import com.navinfo.navicommons.exception.ServiceException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * 
 * @author liuqing HTTP接口返回值
 */
@XStreamAlias("result")
public class Response {

	public static Response SUCCESS = new Response(1, "success");// 成功
	public static Response FAIL = new Response(0);// 失败
	public static Response WRONG_PARAMETER = new Response(100);// 参数错误
	public static Response DUPLICATE = new Response(200,"重复请求");// 重复请求
	public static Response NO_RECORD = new Response(101);// 记录不存在
	public static Response NO_SPACE = new Response(102);// 没有足够的切磁盘

	public boolean continueExecute() {
		return this.code == SUCCESS.getCode();
	}

	public  boolean isNull(String value, String message) {
		if (StringUtils.isBlank(value)) {
			this.setCode(WRONG_PARAMETER.code);
			this.setDesc(message);
            return true;
		}
		return false;
	}

	public  boolean isAllNull(String message,String... values) {
		int i = 0;
		for (String value : values) {
			if (StringUtils.isBlank(value)) {
				i++;
			}
		}
		if (i == values.length) {
			this.setCode(WRONG_PARAMETER.code);
			this.setDesc(message);
            return true;
		}
		return false;
	}

	public  Response markException(Exception e) {
		if(e instanceof ServiceException)
        {
            ServiceException serviceException = (ServiceException)e;
            if(serviceException.getErrorCode() > 0)
                this.setCode(serviceException.getErrorCode());
            else
                this.setCode(FAIL.code);
        }
        else
            this.setCode(FAIL.code);
        this.setDesc(e.getMessage());
		return this;
	}

    public  Response markException(String errMsg) {
        this.setCode(FAIL.code);
        this.setDesc(errMsg);
        return this;
    }

    public Response markWrongParameter(String desc)
    {
        this.setCode(WRONG_PARAMETER.code);
        this.setDesc(desc);
        return this;
    }


    public  Response markDuplicate() {
        this.setCode(DUPLICATE.code);
        this.setDesc(DUPLICATE.desc);
        return this;
    }

	public Response(int code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public Response(int code) {
		this.code = code;
	}

	public int getSequence() {
		return sequence;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	public int getCode() {
		return code;
	}

    public static Response getDefaultResponse()
    {
        return new Response(SUCCESS.code, SUCCESS.desc);
    }


	public void setCode(int code)
    {
        this.code = code;
    }


	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	
	private int code;// 接口返回代码，默认0，表示成功
	private String desc;// 接口返回描述
	private int sequence;// 对应DMS系统各个任务号
}
