package com.navinfo.dataservice.engine.edit.model;

/** 
 * @ClassName: AbstractLinkMesh
 * @author xiaoxiaowen4127
 * @date 2016年8月18日
 * @Description: AbstractLinkMesh.java
 */
public abstract class AbstractLinkMesh extends BasicRow{
	protected long linkPid;
	protected int meshId;
	public long getLinkPid() {
		return linkPid;
	}
	public void setLinkPid(long linkPid) {
		this.linkPid = linkPid;
	}
	public int getMeshId() {
		return meshId;
	}
	public void setMeshId(int meshId) {
		this.meshId = meshId;
	}
}
