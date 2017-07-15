package com.navinfo.dataservice.dao.plus.model.ixpoi;

import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;

/** 
* @ClassName:  IxPoiAddress 
* @author code generator
* @date 2016-11-18 11:27:08 
* @Description: TODO
*/
public class IxPoiFlagMethod extends BasicRow {
	protected long poiPid;
	
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
	
	protected int uRecord;
	
	protected String uFields;
	
	protected String uDate;
	
	
	public IxPoiFlagMethod (long objPid){
		super(objPid);
		setPoiPid(objPid);
	}
	

	public long getPoiPid() {
		return poiPid;
	}

	public void setPoiPid(long poiPid) {
		if(this.checkValue("POI_PID",this.poiPid,poiPid)){
			this.poiPid = poiPid;
		}
	}

	public int getVerRecord() {
		return verRecord;
	}

	public void setVerRecord(int verRecord) {
		if(this.checkValue("VER_RECORD",this.verRecord,verRecord)){
			this.verRecord = verRecord;
		}
	}

	public int getSrcRecord() {
		return srcRecord;
	}

	public void setSrcRecord(int srcRecord) {
		if(this.checkValue("SRC_RECORD",this.srcRecord,srcRecord)){
			this.srcRecord = srcRecord;
		}
	}

	public int getSrcNameCh() {
		return srcNameCh;
	}

	public void setSrcNameCh(int srcNameCh) {
		if(this.checkValue("SRC_NAME_CH",this.srcNameCh,srcNameCh)){
			this.srcNameCh = srcNameCh;
		}
	}

	public int getSrcAddress() {
		return srcAddress;
	}

	public void setSrcAddress(int srcAddress) {
		if(this.checkValue("SRC_ADDRESS",this.srcAddress,srcAddress)){
			this.srcAddress = srcAddress;
		}
	}

	public int getSrcTelephone() {
		return srcTelephone;
	}

	public void setSrcTelephone(int srcTelephone) {
		if(this.checkValue("SRC_TELEPHONE",this.srcTelephone,srcTelephone)){
			this.srcTelephone = srcTelephone;
		}
	}

	public int getSrcCoordinate() {
		return srcCoordinate;
	}

	public void setSrcCoordinate(int srcCoordinate) {
		if(this.checkValue("SRC_COORDINATE",this.srcCoordinate,srcCoordinate)){
			this.srcCoordinate = srcCoordinate;
		}
	}

	public int getSrcNameEng() {
		return srcNameEng;
	}

	public void setSrcNameEng(int srcNameEng) {
		if(this.checkValue("SRC_NAME_ENG",this.srcNameEng,srcNameEng)){
			this.srcNameEng = srcNameEng;
		}
	}

	public int getSrcNamePor() {
		return srcNamePor;
	}

	public void setSrcNamePor(int srcNamePor) {
		if(this.checkValue("SRC_NAME_POR",this.srcNamePor,srcNamePor)){
			this.srcNamePor = srcNamePor;
		}
	}

	public int getFieldVerified() {
		return fieldVerified;
	}

	public void setFieldVerified(int fieldVerified) {
		if(this.checkValue("FIELD_VERIFIED",this.fieldVerified,fieldVerified)){
			this.fieldVerified = fieldVerified;
		}
	}

	public int getRefreshCycle() {
		return refreshCycle;
	}

	public void setRefreshCycle(int refreshCycle) {
		if(this.checkValue("REFRESH_CYCLE",this.refreshCycle,refreshCycle)){
			this.refreshCycle = refreshCycle;
		}
	}

	public String getRefreshDate() {
		return refreshDate;
	}

	public void setRefreshDate(String refreshDate) {
		if(this.checkValue("REFRESH_DATE",this.refreshDate,refreshDate)){
			this.refreshDate = refreshDate;
		}
	}

	public int getuRecord() {
		return uRecord;
	}

	public void setuRecord(int uRecord) {
		if(this.checkValue("U_RECORD",this.uRecord,uRecord)){
			this.uRecord = uRecord;
		}
	}

	public String getuFields() {
		return uFields;
	}

	public void setuFields(String uFields) {
		if(this.checkValue("U_FIELDS",this.uFields,uFields)){
			this.uFields = uFields;
		}
	}

	public String getuDate() {
		return uDate;
	}

	public void setuDate(String uDate) {
		if(this.checkValue("U_DATE",this.uDate,uDate)){
			this.uDate = uDate;
		}
	}


	@Override
	public String tableName() {
		return "IX_POI_FLAG_METHOD";
	}
	
	public static final String POI_PID = "POI_PID";
	public static final String VER_RECORD = "VER_RECORD";
	public static final String SRC_RECORD = "SRC_RECORD";
	public static final String SRC_NAME_CH = "SRC_NAME_CH";
	public static final String SRC_ADDRESS = "SRC_ADDRESS";
	public static final String SRC_TELEPHONE = "SRC_TELEPHONE";
	public static final String SRC_COORDINATE = "SRC_COORDINATE";
	public static final String SRC_NAME_ENG = "SRC_NAME_ENG";
	public static final String SRC_NAME_POR = "SRC_NAME_POR";
	public static final String FIELD_VERIFIED = "FIELD_VERIFIED";
	public static final String REFRESH_CYCLE = "REFRESH_CYCLE";
	public static final String REFRESH_DATE = "REFRESH_DATE";
	public static final String U_RECORD = "U_RECORD";
	public static final String U_FIELDS = "U_FIELDS";
	public static final String U_DATE = "U_DATE";
}
