package com.navinfo.dataservice.web.man.response;

import com.navinfo.dataservice.web.man.page.SubtaskListByUserPage;
import com.wordnik.swagger.annotations.ApiModelProperty;

/** 
 * @ClassName: SubtaskListByUserResponse
 * @author songdongyan
 * @date 2016年8月3日
 * @Description: SubtaskListByUserResponse.java
 */
public class SubtaskListByUserResponse extends Response {

	/**
	 * 
	 */
	public SubtaskListByUserResponse() {
		// TODO Auto-generated constructor stub
	}
	
	@ApiModelProperty(position = 1, required = true, value = "数据")
	public SubtaskListByUserPage data;

	public void SetData(SubtaskListByUserPage data){
    	this.data = data;
    }
    public SubtaskListByUserPage GetData(){
    	return data;
    }

    public SubtaskListByUserResponse(int errcode,String errmsg,SubtaskListByUserPage data){
    	this.errcode = errcode;
    	this.errmsg = errmsg;
    	this.data = data;
    }
    

}
