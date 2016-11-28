package com.navinfo.dataservice.engine.editplus.operation.imp;

import java.util.Set;

/** 
 * @ClassName: PoiRelation
 * @author xiaoxiaowen4127
 * @date 2016年11月25日
 * @Description: PoiRelation.java
 */
public class PoiRelation {
	protected long pid;
	protected long fatherPid;
	protected String fatherFid;
	protected Set<Long> sonPids;
	protected Set<String> sonFids;
	protected long samePid;
	protected String sameFid;
	public long getPid() {
		return pid;
	}
	public void setPid(long pid) {
		this.pid = pid;
	}
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
	public Set<Long> getSonPids() {
		return sonPids;
	}
	public void setSonPids(Set<Long> sonPids) {
		this.sonPids = sonPids;
	}
	public Set<String> getSonFids() {
		return sonFids;
	}
	public void setSonFids(Set<String> sonFids) {
		this.sonFids = sonFids;
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
