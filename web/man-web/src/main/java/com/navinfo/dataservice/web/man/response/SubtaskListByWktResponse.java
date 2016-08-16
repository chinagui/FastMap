package com.navinfo.dataservice.web.man.response;

import java.util.List;

import com.navinfo.dataservice.api.man.model.subtask.SubtaskListByWkt;
import com.wordnik.swagger.annotations.ApiModelProperty;

/** 
 * @ClassName: SubtaskListByWktResponse
 * @author songdongyan
 * @date 2016年8月3日
 * @Description: SubtaskListByWktResponse.java
 */
public class SubtaskListByWktResponse extends Response {

	/**
	 * 
	 */
	public SubtaskListByWktResponse() {
		// TODO Auto-generated constructor stub
	}

	@ApiModelProperty(position = 1, required = true, value = "数据")
	public List<SubtaskListByWkt> data;

	public void SetData(List<SubtaskListByWkt> data){
    	this.data = data;
    }
    public List<SubtaskListByWkt> GetData(){
    	return data;
    }

    public SubtaskListByWktResponse(int errcode,String errmsg,List<SubtaskListByWkt> data){
    	this.errcode = errcode;
    	this.errmsg = errmsg;
    	this.data = data;
    }
}
