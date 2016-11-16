package com.navinfo.dataservice.engine.editplus.model;

/** 
 * @ClassName: AbstractNodeMesh
 * @author xiaoxiaowen4127
 * @date 2016年8月18日
 * @Description: AbstractNodeMesh.java
 */
public abstract class AbstractNodeMesh extends BasicRow {

	public AbstractNodeMesh(long objPid) {
		super(objPid);
	}

	protected long nodePid;
	protected int meshId;
	public long getNodePid() {
		return nodePid;
	}
	public void setNodePid(long nodePid) {
		this.nodePid = nodePid;
	}
	public int getMeshId() {
		return meshId;
	}
	public void setMeshId(int meshId) {
		this.meshId = meshId;
	}

}
