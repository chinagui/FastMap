package com.navinfo.dataservice.dao.plus.model.ixpoi;

import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;

/** 
* @ClassName:  IxPoiBuilding 
* @author code generator
* @date 2016-11-18 11:33:32 
* @Description: TODO
*/
public class IxPoiBuilding extends BasicRow {
	protected long poiPid ;
	protected String floorUsed ;
	protected String floorEmpty ;
	protected String memo ;
	
	public IxPoiBuilding (long objPid){
		super(objPid);
	}
	
	public long getPoiPid() {
		return poiPid;
	}
	public void setPoiPid(long poiPid) {
		if(this.checkValue("POI_PID",this.poiPid,poiPid)){
			this.poiPid = poiPid;
		}
	}
	public String getFloorUsed() {
		return floorUsed;
	}
	public void setFloorUsed(String floorUsed) {
		if(this.checkValue("FLOOR_USED",this.floorUsed,floorUsed)){
			this.floorUsed = floorUsed;
		}
	}
	public String getFloorEmpty() {
		return floorEmpty;
	}
	public void setFloorEmpty(String floorEmpty) {
		if(this.checkValue("FLOOR_EMPTY",this.floorEmpty,floorEmpty)){
			this.floorEmpty = floorEmpty;
		}
	}
	public String getMemo() {
		return memo;
	}
	public void setMemo(String memo) {
		if(this.checkValue("MEMO",this.memo,memo)){
			this.memo = memo;
		}
	}
	
	@Override
	public String tableName() {
		return "IX_POI_BUILDING";
	}
	
	public static final String POI_PID = "POI_PID";
	public static final String FLOOR_USED = "FLOOR_USED";
	public static final String FLOOR_EMPTY = "FLOOR_EMPTY";
	public static final String MEMO = "MEMO";

}
