package com.navinfo.dataservice.web.man.response;

import java.util.List;

import com.navinfo.dataservice.api.man.model.subtask.SubtaskListByUser;
import com.wordnik.swagger.annotations.ApiModelProperty;

/** 
 * @ClassName: SubtaskQueryResponse
 * @author songdongyan
 * @date 2016年8月3日
 * @Description: SubtaskQueryResponse.java
 */
public class SubtaskQueryResponse extends Response {

	/**
	 * 
	 */
	public SubtaskQueryResponse() {
		// TODO Auto-generated constructor stub
	}

	@ApiModelProperty(position = 1, required = true, value = "数据")
	public SubtaskListByUser data;

	public void SetData(SubtaskListByUser data){
    	this.data = data;
    }
    public SubtaskListByUser GetData(){
    	return data;
    }

    public SubtaskQueryResponse(int errcode,String errmsg,SubtaskListByUser data){
    	this.errcode = errcode;
    	this.errmsg = errmsg;
    	this.data = data;
    }
}
