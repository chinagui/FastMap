package com.navinfo.dataservice.web.man.response;

/** 
 * @ClassName: NullResponse
 * @author songdongyan
 * @date 2016年8月3日
 * @Description: NullResponse.java
 */
public class NullResponse extends Response {

	/**
	 * 
	 */
	public NullResponse() {
		// TODO Auto-generated constructor stub
	}

	public Object data = null;

	public void SetData(Object data){
    	this.data = data;
    }
    public Object GetData(){
    	return data;
    }

    public NullResponse(int errcode,String errmsg,Object data){
    	this.errcode = errcode;
    	this.errmsg = errmsg;
    	this.data = data;
    }
}
