package com.navinfo.dataservice.dao.plus.model.ixpointaddress;

import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;

/** 
* @ClassName:  IxPointaddressFlag 
* @author code generator
* @date 2017-09-18 02:02:07 
* @Description: TODO
*/
public class IxPointaddressFlag extends BasicRow{
	private long pid ;
	private String flagCode ;
	
	public IxPointaddressFlag (long objPid){
		super(objPid);
		setPid(objPid);
	}
	public long getPid(){
		return pid;
	}
	public void setPid(long pid){
		if(this.checkValue("PID", this.pid, pid)){
			this.pid = pid;
		}
	}
	public String getFlagCode() {
		return flagCode;
	}
	public void setFlagCode(String flagCode) {
		if(this.checkValue("FLAG_CODE", this.flagCode, flagCode)){
			this.flagCode = flagCode;
		}
	}

	@Override
	public String tableName() {
		// TODO Auto-generated method stub
		return "IX_POINTADDRESS_FLAG";
	}
	
}
