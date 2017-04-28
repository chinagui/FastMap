package com.navinfo.dataservice.control.service;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.engine.editplus.operation.imp.ErrorLog;

/** 
 * @ClassName: UploadResult
 * @author xiaoxiaowen4127
 * @date 2017年4月24日
 * @Description: UploadResult.java
 */
public class UploadResult {

	private int success=0;
	
	private List<ErrorLog> fail=new ArrayList<ErrorLog>();
	
	public void addSuccess(){
		success++;
	}
	public void addSuccess(int successNum){
		this.success+=successNum;
	}
	public void addFail(ErrorLog errLog){
		fail.add(errLog);
	}
	
	public void addResults(int successNum,List<ErrorLog> failList){
		this.success+=successNum;
		this.fail.addAll(failList);
	}
}
