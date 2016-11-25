package com.navinfo.dataservice.engine.editplus.imp;

import java.util.Set;

/** 
 * @ClassName: PoiRelation
 * @author xiaoxiaowen4127
 * @date 2016年11月25日
 * @Description: PoiRelation.java
 */
public class PoiRelation {
	protected long pid;
	protected long father;
	protected Set<Long> sons;
	protected long samePid;
	public long getPid() {
		return pid;
	}
	public void setPid(long pid) {
		this.pid = pid;
	}
	public long getFather() {
		return father;
	}
	public void setFather(long father) {
		this.father = father;
	}
	public Set<Long> getSons() {
		return sons;
	}
	public void setSons(Set<Long> sons) {
		this.sons = sons;
	}
	public long getSamePid() {
		return samePid;
	}
	public void setSamePid(long samePid) {
		this.samePid = samePid;
	}
}
