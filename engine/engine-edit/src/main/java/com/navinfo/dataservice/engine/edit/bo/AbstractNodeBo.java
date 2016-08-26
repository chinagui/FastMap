package com.navinfo.dataservice.engine.edit.bo;

import com.vividsolutions.jts.geom.Geometry;
import com.navinfo.dataservice.engine.edit.model.AbstractNode;


public abstract class AbstractNodeBo extends AbstractBo{
	public Geometry getGeometry(){
		return ((AbstractNode)getObj()).getGeometry();
	}
	public void setGeometry(Geometry geo){
		if(getObj().checkValue("GEOMETRY", getGeometry(), geo)){
			((AbstractNode)getObj()).setGeometry(geo);
		}
	}
}
