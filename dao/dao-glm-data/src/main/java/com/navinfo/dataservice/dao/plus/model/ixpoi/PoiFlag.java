package com.navinfo.dataservice.dao.plus.model.ixpoi;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;

/** 
* @ClassName:  IxPoiAddress 
* @author code generator
* @date 2016-11-18 11:27:08 
* @Description: TODO
*/
public class PoiFlag extends BasicRow {
	protected long pid;
	
	protected int verRecord;
	
	protected int srcRecord;
	
	protected int srcNameCh;
	
	protected int srcAddress;
	
	protected int srcTelephone;
	
	protected int srcCoordinate;
	
	protected int srcNameEng;
	
	protected int srcNamePor;
	
	protected int fieldVerified;
	
	protected int refreshCycle;
	
	protected String refreshDate;
	
	public PoiFlag (long objPid){
		super(objPid);
		setPid(objPid);
	}
	
	public long getPid() {
		return pid;
	}
	public void setPid(long pid) {
		this.pid = pid;
	}

	public int getVerRecord() {
		return verRecord;
	}
	public void setVerRecord(int verRecord) {
		this.verRecord = verRecord;
	}



	public int getSrcRecord() {
		return srcRecord;
	}
	public void setSrcRecord(int srcRecord) {
		this.srcRecord = srcRecord;
	}

	public int getSrcNameCh() {
		return srcNameCh;
	}
	public void setSrcNameCh(int srcNameCh) {
		this.srcNameCh = srcNameCh;
	}

	public int getSrcAddress() {
		return srcAddress;
	}
	public void setSrcAddress(int srcAddress) {
		this.srcAddress = srcAddress;
	}

	public int getSrcTelephone() {
		return srcTelephone;
	}
	public void setSrcTelephone(int srcTelephone) {
		this.srcTelephone = srcTelephone;
	}

	public int getSrcCoordinate() {
		return srcCoordinate;
	}
	public void setSrcCoordinate(int srcCoordinate) {
		this.srcCoordinate = srcCoordinate;
	}

	public int getSrcNameEng() {
		return srcNameEng;
	}
	public void setSrcNameEng(int srcNameEng) {
		this.srcNameEng = srcNameEng;
	}

	public int getSrcNamePor() {
		return srcNamePor;
	}
	public void setSrcNamePor(int srcNamePor) {
		this.srcNamePor = srcNamePor;
	}
	
	public int getFieldVerified() {
		return fieldVerified;
	}
	public void setFieldVerified(int fieldVerified) {
		this.fieldVerified = fieldVerified;
	}

	public int getRefreshCycle() {
		return refreshCycle;
	}
	public void setRefreshCycle(int refreshCycle) {
		this.refreshCycle = refreshCycle;
	}
	
	public String getRefreshDate() {
		return refreshDate;
	}
	public void setRefreshDate(String refreshDate) {
		this.refreshDate = refreshDate;
	}



	@Override
	public String tableName() {
		return "POI_FLAG";
	}
	
	public static final String PID = "PID";
	public static final String VER_RECORD = "VER_RECORD";
	public static final String SRC_RECORD = "SRC_RECORD";
	public static final String SRC_NAME_CH = "SRC_NAME_CH";
	public static final String SRC_ADDRESS = "SRC_ADDRESS";
	public static final String SRC_TELEPHONE = "SRC_TELEPHONE";
	public static final String SRC_COORDINATE = "SRC_COORDINATE";
	public static final String SRC_NAME_ENG = "SRC_NAME_ENG";
	public static final String SRC_NAME_POR = "SRC_NAME_POR";
	public static final String FIELD_VERIFIED = "FIELD_VERIFIED";
	public static final String RELIABILITY = "RELIABILITY";
	public static final String REFRESH_CYCLE = "REFRESH_CYCLE";
	public static final String REFRESH_DATE = "REFRESH_DATE";
	
}
