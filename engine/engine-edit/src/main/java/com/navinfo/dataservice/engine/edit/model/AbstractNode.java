package com.navinfo.dataservice.engine.edit.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.vividsolutions.jts.geom.Geometry;

/** 
 * @ClassName: AbstractNode
 * @author xiaoxiaowen4127
 * @date 2016年8月18日
 * @Description: AbstractNode.java
 */
public abstract class AbstractNode extends BasicObj {
	
	protected Geometry geometry;
	protected List<BasicRow> meshes = new ArrayList<BasicRow>();
	
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
