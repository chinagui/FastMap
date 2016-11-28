package com.navinfo.dataservice.dao.plus.model.ixpoi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;

import net.sf.json.JSONObject;

/** 
* @ClassName:  IxPoiPhoto 
* @author code generator
* @date 2016-11-18 11:28:24 
* @Description: TODO
*/
public class IxPoiPhoto extends BasicRow {
	protected long poiPid ;
	protected long photoId ;
	protected String pid ;
	protected String status ;
	protected String memo ;
	protected int tag ;
	
	public IxPoiPhoto (long objPid){
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
	public long getPhotoId() {
		return photoId;
	}
	public void setPhotoId(long photoId) {
		if(this.checkValue("PHOTO_ID",this.photoId,photoId)){
			this.photoId = photoId;
		}
	}
	public String getPid() {
		return pid;
	}
	public void setPid(String pid) {
		if(this.checkValue("PID",this.pid,pid)){
			this.pid = pid;
		}
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		if(this.checkValue("STATUS",this.status,status)){
			this.status = status;
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
	public int getTag() {
		return tag;
	}
	public void setTag(int tag) {
		if(this.checkValue("TAG",this.tag,tag)){
			this.tag = tag;
		}
	}
	
	@Override
	public String tableName() {
		return "IX_POI_PHOTO";
	}
}
