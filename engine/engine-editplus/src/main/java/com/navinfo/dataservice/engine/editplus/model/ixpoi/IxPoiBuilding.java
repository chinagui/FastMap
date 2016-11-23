package com.navinfo.dataservice.engine.editplus.model.ixpoi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.engine.editplus.model.BasicRow;

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
}
