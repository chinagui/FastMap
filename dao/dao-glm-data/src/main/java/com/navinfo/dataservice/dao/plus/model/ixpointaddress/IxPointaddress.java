package com.navinfo.dataservice.dao.plus.model.ixpointaddress;

import com.navinfo.dataservice.dao.plus.model.basic.AbstractIx;

/** 
* @ClassName:  IxPointaddress 
* @author code generator
* @date 2017-09-15 06:55:29 
* @Description: TODO
*/
public class IxPointaddress extends AbstractIx {
	private long guideLinkPid = 0;
	private long locateLinkPid = 0;
	private long locateNameGroupid = 0;
	private int guideLinkSide = 0;
	private int locateLinkSide = 0;
	private long srcPid = 0;
	private long regionId ;
	private int editFlag = 1;
	private String idcode ;
	private String dprName ;
	private String dpName ;
	private String operator ;
	private String memoire ;
	private String dpfName ;
	private String posterId ;
	private int addressFlag ;
	private String verifed = "F";
	private String log ;
	private String memo ;
	private String reserved ;
	private long taskId ;
	private String srcType ;
	private String dataVersion ;
	private long fieldTaskId ;
	private int state ;
	
	public IxPointaddress (long objPid){
		super(objPid);
	}
	public long getGuideLinkPid() {
		return guideLinkPid;
	}
	public void setGuideLinkPid(long guideLinkPid) {
		if(this.checkValue("GUIDE_LINK_PID", this.guideLinkPid, guideLinkPid)){
			this.guideLinkPid = guideLinkPid;
		}
	}
	public long getLocateLinkPid() {
		return locateLinkPid;
	}
	public void setLocateLinkPid(long locateLinkPid) {
		if(this.checkValue("LOCATE_LINK_PID", this.locateLinkPid, locateLinkPid)){
			this.locateLinkPid = locateLinkPid;
		}
	}
	public long getLocateNameGroupid() {
		return locateNameGroupid;
	}
	public void setLocateNameGroupid(long locateNameGroupid) {
		if(this.checkValue("LOCATE_NAME_GROUPID", this.locateNameGroupid, locateNameGroupid)){
			this.locateNameGroupid = locateNameGroupid;
		}
	}
	public int getGuideLinkSide() {
		return guideLinkSide;
	}
	public void setGuideLinkSide(int guideLinkSide) {
		if(this.checkValue("GUIDE_LINK_SIDE", this.guideLinkSide, guideLinkSide)){
			this.guideLinkSide = guideLinkSide;
		}
	}
	public int getLocateLinkSide() {
		return locateLinkSide;
	}
	public void setLocateLinkSide(int locateLinkSide) {
		if(this.checkValue("LOCATE_LINK_SIDE", this.locateLinkSide, locateLinkSide)){
			this.locateLinkSide = locateLinkSide;
		}
	}
	public long getSrcPid() {
		return srcPid;
	}
	public void setSrcPid(long srcPid) {
		if(this.checkValue("SRC_PID", this.srcPid, srcPid)){
			this.srcPid = srcPid;
		}
	}
	public long getRegionId() {
		return regionId;
	}
	public void setRegionId(long regionId) {
		if(this.checkValue("REGION_ID", this.regionId, regionId)){
			this.regionId = regionId;
		}
	}
	public int getEditFlag() {
		return editFlag;
	}
	public void setEditFlag(int editFlag) {
		if(this.checkValue("EDIT_FLAG", this.editFlag, editFlag)){
			this.editFlag = editFlag;
		}
	}
	public String getIdcode() {
		return idcode;
	}
	public void setIdcode(String idcode) {
		if(this.checkValue("IDCODE", this.idcode, idcode)){
			this.idcode = idcode;
		}
	}
	public String getDprName() {
		return dprName;
	}
	public void setDprName(String dprName) {
		if(this.checkValue("DPR_NAME", this.dprName, dprName)){
			this.dprName = dprName;
		}
	}
	public String getDpName() {
		return dpName;
	}
	public void setDpName(String dpName) {
		if(this.checkValue("DP_NAME", this.dpName, dpName)){
			this.dpName = dpName;
		}
	}
	public String getOperator() {
		return operator;
	}
	public void setOperator(String operator) {
		if(this.checkValue("OPERATOR", this.operator, operator)){
			this.operator = operator;
		}
	}
	public String getMemoire() {
		return memoire;
	}
	public void setMemoire(String memoire) {
		if(this.checkValue("MEMOIRE", this.memoire, memoire)){
			this.memoire = memoire;
		}
	}
	public String getDpfName() {
		return dpfName;
	}
	public void setDpfName(String dpfName) {
		if(this.checkValue("DPF_NAME", this.dpfName, dpfName)){
			this.dpfName = dpfName;
		}
	}
	public String getPosterId() {
		return posterId;
	}
	public void setPosterId(String posterId) {
		if(this.checkValue("POSTER_ID", this.posterId, posterId)){
			this.posterId = posterId;
		}
	}
	public int getAddressFlag() {
		return addressFlag;
	}
	public void setAddressFlag(int addressFlag) {
		if(this.checkValue("ADDRESS_FLAG", this.addressFlag, addressFlag)){
			this.addressFlag = addressFlag;
		}
	}
	public String getVerifed() {
		return verifed;
	}
	public void setVerifed(String verifed) {
		if(this.checkValue("VERIFED", this.verifed, verifed)){
			this.verifed = verifed;
		}
	}
	public String getLog() {
		return log;
	}
	public void setLog(String log) {
		if(this.checkValue("LOG", this.log, log)){
			this.log = log;
		}
	}
	public String getMemo() {
		return memo;
	}
	public void setMemo(String memo) {
		if(this.checkValue("MEMO", this.memo, memo)){
			this.memo = memo;
		}
	}
	public String getReserved() {
		return reserved;
	}
	public void setReserved(String reserved) {
		if(this.checkValue("RESERVED", this.reserved, reserved)){
			this.reserved = reserved;
		}
	}
	public long getTaskId() {
		return taskId;
	}
	public void setTaskId(long taskId) {
		if(this.checkValue("TASK_ID", this.taskId, taskId)){
			this.taskId = taskId;
		}
	}
	public String getSrcType() {
		return srcType;
	}
	public void setSrcType(String srcType) {
		if(this.checkValue("SRC_TYPE", this.srcType, srcType)){
			this.srcType = srcType;
		}
	}
	public String getDataVersion() {
		return dataVersion;
	}
	public void setDataVersion(String dataVersion) {
		if(this.checkValue("DATA_VERSION", this.dataVersion, dataVersion)){
			this.dataVersion = dataVersion;
		}
	}
	public long getFieldTaskId() {
		return fieldTaskId;
	}
	public void setFieldTaskId(long fieldTaskId) {
		if(this.checkValue("FIELD_TASK_ID", this.fieldTaskId, fieldTaskId)){
			this.fieldTaskId = fieldTaskId;
		}
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		if(this.checkValue("STATE", this.state, state)){
			this.state = state;
		}
	}

	@Override
	public String tableName() {
		// TODO Auto-generated method stub
		return "IX_POINTADDRESS";
	}
	
	public static final String GEOMETRY = "GEOMETRY";

	
	public static final String DPR_NAME = "DPR_NAME";
	public static final String DP_NAME = "DP_NAME";
	public static final String LINK_PID = "LINK_PID";
	public static final String X_GUIDE = "X_GUIDE";
	public static final String Y_GUIDE = "Y_GUIDE";
	public static final String MEMOIRE = "MEMOIRE";
	public static final String GEOMETRY = "GEOMETRY";
	public static final String MEMO = "MEMO";
	public static final String IDCODE = "IDCODE";
}
