package com.navinfo.dataservice.engine.edit.bo;

import java.util.List;

import com.vividsolutions.jts.geom.Geometry;

public abstract class FaceBo extends AbstractBo{

	public abstract Geometry getGeometry();
	public abstract void setGeometry(Geometry geo);

	public LinkBreakResult breakoff(List<Geometry> list) throws Exception{
		
		
			
		return null;
	}
	
}
