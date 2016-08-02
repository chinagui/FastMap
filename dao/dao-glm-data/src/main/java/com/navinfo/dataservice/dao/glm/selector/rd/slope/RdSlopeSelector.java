package com.navinfo.dataservice.dao.glm.selector.rd.slope;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.model.rd.slope.RdSlope;

import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;

public class RdSlopeSelector extends AbstractSelector {

	private Connection conn;

	public RdSlopeSelector(Connection conn) {
		super(conn);
		this.conn = conn;
		this.setCls(RdSlope.class);
	}
}
