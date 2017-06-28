package com.navinfo.dataservice.engine.man.subtask;

import com.vividsolutions.jts.geom.Geometry;

public class SubtaskRefer {
	private int id;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getBlockId() {
		return blockId;
	}
	public void setBlockId(int blockId) {
		this.blockId = blockId;
	}
	public Geometry getGeometry() {
		return geometry;
	}
	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}
	private int blockId;
	private Geometry geometry;
}
