package com.navinfo.dataservice.engine.editplus.model;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.engine.editplus.model.obj.BasicObj;
import com.vividsolutions.jts.geom.Geometry;

/** 
 * @ClassName: AbstractNode
 * @author xiaoxiaowen4127
 * @date 2016年8月18日
 * @Description: AbstractNode.java
 */
public abstract class AbstractNode extends BasicRow {

	public AbstractNode(long objPid) {
		super(objPid);
		// TODO Auto-generated constructor stub
	}
	protected long nodePid;
	protected Geometry geometry;
	protected List<BasicRow> meshes = new ArrayList<BasicRow>();
	
	public long getNodePid() {
		return nodePid;
	}
	public void setNodePid(long nodePid) {
		this.nodePid = nodePid;
	}
	public Geometry getGeometry() {
		return geometry;
	}
	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}
	public List<BasicRow> getMeshes() {
		return meshes;
	}
	public void setMeshes(List<BasicRow> meshes) {
		this.meshes = meshes;
	}

}
