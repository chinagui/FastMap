package com.navinfo.dataservice.control.dealership.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

/** 
* @ClassName:  IxDealershipHistory 
* @author code generator
* @date 2017-05-27 03:28:03 
* @Description: TODO
*/
public class IxDealershipHistory  {
	private int historyId ;
	private int resultId ;
	private String fieldName ;
	private int uRecord ;
	private String oldValue ;
	private String newValue ;
	private String uDate ;
	private int userId ;
	
	public IxDealershipHistory (){
	}
	
	public IxDealershipHistory (int historyId ,int resultId,String fieldName,int uRecord,String oldValue,String newValue,String uDate,int userId){
		this.historyId=historyId ;
		this.resultId=resultId ;
		this.fieldName=fieldName ;
		this.uRecord=uRecord ;
		this.oldValue=oldValue ;
		this.newValue=newValue ;
		this.uDate=uDate ;
		this.userId=userId ;
	}
	public int getHistoryId() {
		return historyId;
	}
	public void setHistoryId(int historyId) {
		this.historyId = historyId;
	}
	public int getResultId() {
		return resultId;
	}
	public void setResultId(int resultId) {
		this.resultId = resultId;
	}
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	public int getURecord() {
		return uRecord;
	}
	public void setURecord(int uRecord) {
		this.uRecord = uRecord;
	}
	public String getOldValue() {
		return oldValue;
	}
	public void setOldValue(String oldValue) {
		this.oldValue = oldValue;
	}
	public String getNewValue() {
		return newValue;
	}
	public void setNewValue(String newValue) {
		this.newValue = newValue;
	}
	public String getUDate() {
		return uDate;
	}
	public void setUDate(String uDate) {
		this.uDate = uDate;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "IxDealershipHistory [historyId=" + historyId +",resultId="+resultId+",fieldName="+fieldName+",uRecord="+uRecord+",oldValue="+oldValue+",newValue="+newValue+",uDate="+uDate+",userId="+userId+"]";
	}

	
	
}
