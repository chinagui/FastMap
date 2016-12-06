package com.navinfo.dataservice.dao.log;

/**
 * 
 * 履历日出品表
 * 
 * @author zhaokk
 * 
 */
public class LogDayRelease {

	private String opId;// 主键 对应 log_operation.op_id
	private int relPoiSta = 0;// POI 出品状态，0 ：否 1：是

	private String relPoiDt;// POI出品时间

	private int relAllSta = 0;// POI_ROAD 出品状态，0 ：否 1：是

	private String relAllDt;// POI_ROAD出品时间

	private int relPoiLock = 0;// POI 出品锁 0 ：否 1：是

	private int relAllLock = 0; // POI+ROAD 出品锁 0 ：否 1：是

	public LogDayRelease(String opId) {
		this.opId = opId;

	}

	public String tableName() {
		return "log_day_release";
	}

	public String getOpId() {
		return opId;
	}

	public void setOpId(String opId) {
		this.opId = opId;
	}

	public int getRelPoiSta() {
		return relPoiSta;
	}

	public void setRelPoiSta(int relPoiSta) {
		this.relPoiSta = relPoiSta;
	}

	public String getRelPoiDt() {
		return relPoiDt;
	}

	public void setRelPoiDt(String relPoiDt) {
		this.relPoiDt = relPoiDt;
	}

	public int getRelAllSta() {
		return relAllSta;
	}

	public void setRelAllSta(int relAllSta) {
		this.relAllSta = relAllSta;
	}

	public String getRelAllDt() {
		return relAllDt;
	}

	public void setRelAllDt(String relAllDt) {
		this.relAllDt = relAllDt;
	}

	public int getRelPoiLock() {
		return relPoiLock;
	}

	public void setRelPoiLock(int relPoiLock) {
		this.relPoiLock = relPoiLock;
	}

	public int getRelAllLock() {
		return relAllLock;
	}

	public void setRelAllLock(int relAllLock) {
		this.relAllLock = relAllLock;
	}
}
