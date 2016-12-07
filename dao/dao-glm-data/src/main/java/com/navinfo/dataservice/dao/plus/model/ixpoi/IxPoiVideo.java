package com.navinfo.dataservice.dao.plus.model.ixpoi;

import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;

/** 
* @ClassName:  IxPoiVideo 
* @author code generator
* @date 2016-11-18 11:32:17 
* @Description: TODO
*/
public class IxPoiVideo extends BasicRow {
	protected long poiPid ;
	protected long videoId ;
	protected String pid ;
	protected String status ;
	protected String memo ;
	protected int tag ;

	public IxPoiVideo (long objPid){
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
	public long getVideoId() {
		return videoId;
	}
	public void setVideoId(long videoId) {
		if(this.checkValue("VIDEO_ID",this.videoId,videoId)){
			this.videoId = videoId;
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
		return "IX_POI_VIDEO";
	}
	
	public static final String POI_PID = "POI_PID";
	public static final String VIDEO_ID = "VIDEO_ID";
	public static final String PID = "PID";
	public static final String STATUS = "STATUS";
	public static final String MEMO = "MEMO";
	public static final String TAG = "TAG";

}
