package com.navinfo.dataservice.impcore.flushbylog;

/** 
 * @ClassName: FlushObjStatInfo
 * @author xiaoxiaowen4127
 * @date 2017年9月6日
 * @Description: FlushObjStatInfo.java
 */
public class FlushObjStatInfo {
	protected String objName = "";
	protected int total=0;
	protected int success=0;
	public FlushObjStatInfo(String objName){
		this.objName=objName;
	}
	public String getObjName() {
		return objName;
	}
	public void setObjName(String objName) {
		this.objName = objName;
	}
	public int getTotal() {
		return total;
	}
	public void setTotal(int total) {
		
		this.total = total;
	}
	public int getSuccess() {
		return success;
	}
	public void setSuccess(int success) {
		this.success = success;
	}
	
	public int hashCode(){
		return objName.hashCode();
	}
	public boolean equals(Object anObject){
		if(anObject==null)return false;
		if(anObject instanceof FlushObjStatInfo
				&&objName.equals(((FlushObjStatInfo) anObject).objName)){
			return true;
		}else{
			return false;
		}
	}
	
}
