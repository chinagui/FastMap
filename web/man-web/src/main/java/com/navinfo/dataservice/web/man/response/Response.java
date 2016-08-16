package com.navinfo.dataservice.web.man.response;

import com.wordnik.swagger.annotations.ApiModelProperty;

/** 
 * @ClassName: Response
 * @author songdongyan
 * @date 2016年8月3日
 * @Description: Response.java
 */
public class Response {

	/**
	 * 
	 */

	public Response() {
		// TODO Auto-generated constructor stub
	}
	
	@ApiModelProperty(position = 1, required = true, value = "错误码：0 执行成功；-1 执行失败")
	public Integer errcode;
	@ApiModelProperty(position = 1, required = true, value = "错误信息")
	public String errmsg;
    
    public void SetErrorCode(int errcode){
    	this.errcode = errcode;
    }
    public int GetErrorCode(){
    	return errcode;
    }
    
    public void SetErrorMessage(String errorMessage){
    	this.errmsg = errorMessage;
    }
    public String GetErrorMessage(){
    	return errmsg;
    }

}
