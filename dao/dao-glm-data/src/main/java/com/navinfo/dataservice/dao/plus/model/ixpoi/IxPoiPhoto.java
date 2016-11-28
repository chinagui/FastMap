package com.navinfo.dataservice.dao.plus.model.ixpoi;

import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;

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
	
	public static final String POI_PID = "POI_PID";
	public static final String PHOTO_ID = "PHOTO_ID";
	public static final String PID = "PID";
	public static final String STATUS = "STATUS";
	public static final String MEMO = "MEMO";
	public static final String TAG = "TAG";

}
