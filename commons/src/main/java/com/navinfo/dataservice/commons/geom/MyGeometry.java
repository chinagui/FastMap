package com.navinfo.dataservice.commons.geom;

import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class MyGeometry {
	private Geometry geo;
	private List<Coordinate> SENodes;
	public Geometry getGeo() {
		return geo;
	}
	public void setGeo(Geometry geo) {
		this.geo = geo;
	}
	public List<Coordinate> getSENodes() {
		return SENodes;
	}
	public void setSENodes(List<Coordinate> sENodes) {
		SENodes = sENodes;
	}
}
