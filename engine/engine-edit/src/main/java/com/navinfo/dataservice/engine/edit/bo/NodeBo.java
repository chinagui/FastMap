package com.navinfo.dataservice.engine.edit.bo;

import com.vividsolutions.jts.geom.Geometry;


public abstract class NodeBo extends AbstractBo{
	public abstract Geometry getGeometry();
	public abstract void setGeometry(Geometry geo);
}
