package com.navinfo.dataservice.control.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.engine.editplus.operation.imp.ErrorLog;

/** 
 * @ClassName: UploadResult
 * @author xiaoxiaowen4127
 * @date 2017年4月24日
 * @Description: UploadResult.java
 */
public class UploadResult implements Serializable{
	
	private int total = 0;

	private int success = 0;
	
	private List<ErrorLog> fail = new ArrayList<ErrorLog>();
	
	private List<ErrorLog> warnPc = new ArrayList<ErrorLog>();
	
	private List<ErrorLog> warnSp = new ArrayList<ErrorLog>();
	
	private List<RegionUploadResult> regionResults = new ArrayList<RegionUploadResult>();
	
	public int getTotal() {
		return total;
	}
	public void setTotal(int total) {
		this.total = total;
	}
	public int getSuccess() {
		return success;
	}
	public void addSuccess(){
		success++;
	}
	public void addSuccess(int successNum){
		this.success+=successNum;
	}
	public void addFail(ErrorLog errLog){
		fail.add(errLog);
	}
	
	public List<ErrorLog> getFail() {
		return fail;
	}
	public void addResults(int successNum,List<ErrorLog> failList){
		this.success+=successNum;
		this.fail.addAll(failList);
	}
	public List<ErrorLog> getWarnPc(){
		return warnPc;
	}
	public List<ErrorLog> getWarnSp(){
		return warnSp;
	}
	public void addWarnPcs(List<ErrorLog> wPcs){
		this.warnPc.addAll(wPcs);
	}
	public void addWarnSps(List<ErrorLog> wSps){
		this.warnSp.addAll(wSps);
	}
	public List<RegionUploadResult> getRegionResults() {
		return regionResults;
	}
	public void addRegionResult(RegionUploadResult result) {
		this.regionResults.add(result);
	}
	public void addResultResults(Collection<RegionUploadResult> results){
		this.regionResults.addAll(results);
	}
}
