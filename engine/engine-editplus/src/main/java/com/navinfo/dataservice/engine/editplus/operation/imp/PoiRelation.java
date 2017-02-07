package com.navinfo.dataservice.engine.editplus.operation.imp;

/** 
 * @ClassName: PoiRelation
 * @author xiaoxiaowen4127
 * @date 2016年11月25日
 * @Description: PoiRelation.java
 */
public class PoiRelation {
	protected long pid;
	//*******zl 2017.01.20 *********
	protected String fid;
	//******************************
	protected long fatherPid;
	protected String fatherFid;
	protected long samePid = 0L;
	protected String sameFid;
	protected PoiRelationType poiRelationType;
	
	
	public PoiRelationType getPoiRelationType() {
		return poiRelationType;
	}
	public void setPoiRelationType(PoiRelationType poiRelationType) {
		this.poiRelationType = poiRelationType;
	}
	
	public long getPid() {
		return pid;
	}
	public void setPid(long pid) {
		this.pid = pid;
	}
	//*******zl 2017.01.20 *********
	public String getFid() {
		return fid;
	}
	public void setFid(String fid) {
		this.fid = fid;
	}
	//******************************
	public long getFatherPid() {
		return fatherPid;
	}
	public void setFatherPid(long fatherPid) {
		this.fatherPid = fatherPid;
	}
	public String getFatherFid() {
		return fatherFid;
	}
	public void setFatherFid(String fatherFid) {
		this.fatherFid = fatherFid;
	}
	public long getSamePid() {
		return samePid;
	}
	public void setSamePid(long samePid) {
		this.samePid = samePid;
	}
	public String getSameFid() {
		return sameFid;
	}
	public void setSameFid(String sameFid) {
		this.sameFid = sameFid;
	}
}
