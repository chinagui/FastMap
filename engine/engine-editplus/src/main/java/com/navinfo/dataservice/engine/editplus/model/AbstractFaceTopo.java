package com.navinfo.dataservice.engine.editplus.model;

import com.navinfo.dataservice.engine.editplus.glm.NonObjPidException;

/** 
 * @ClassName: AbstractFaceTopo
 * @author xiaoxiaowen4127
 * @date 2016年8月18日
 * @Description: AbstractFaceTopo.java
 */
public abstract class AbstractFaceTopo extends BasicRow {

	public AbstractFaceTopo(long objPid) {
		super(objPid);
	}
	protected long facePid;
	protected int seqNum;
	protected long linkPid;
	public long getFacePid() {
		return facePid;
	}
	public void setFacePid(long facePid) {
		this.facePid = facePid;
	}
	public int getSeqNum() {
		return seqNum;
	}
	public void setSeqNum(int seqNum) {
		this.seqNum = seqNum;
	}
	public long getLinkPid() {
		return linkPid;
	}
	public void setLinkPid(long linkPid) {
		this.linkPid = linkPid;
	}

}
