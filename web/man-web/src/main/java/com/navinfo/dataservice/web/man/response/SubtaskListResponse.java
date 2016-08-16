package com.navinfo.dataservice.web.man.response;

import com.navinfo.dataservice.web.man.page.SubtaskListPage;
import com.wordnik.swagger.annotations.ApiModelProperty;

/** 
 * @ClassName: SubtaskListResponse
 * @author songdongyan
 * @date 2016年8月3日
 * @Description: SubtaskListResponse.java
 */
public class SubtaskListResponse extends Response {

	/**
	 * 
	 */
	public SubtaskListResponse() {
		// TODO Auto-generated constructor stub
	}
	
	@ApiModelProperty(position = 1, required = true, value = "数据")
	public SubtaskListPage data;

	public void SetData(SubtaskListPage data){
    	this.data = data;
    }
    public SubtaskListPage GetData(){
    	return data;
    }

    public SubtaskListResponse(int errcode,String errmsg,SubtaskListPage data){
    	this.errcode = errcode;
    	this.errmsg = errmsg;
    	this.data = data;
    }
    

}
