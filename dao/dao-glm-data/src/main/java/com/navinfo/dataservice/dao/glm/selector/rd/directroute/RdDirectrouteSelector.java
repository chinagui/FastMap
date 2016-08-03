package com.navinfo.dataservice.dao.glm.selector.rd.directroute;

import java.sql.Connection;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.glm.model.rd.directroute.RdDirectroute;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;

public class RdDirectrouteSelector extends AbstractSelector {

	private static Logger logger = Logger
			.getLogger(RdDirectrouteSelector.class);

	private Connection conn;

	public RdDirectrouteSelector(Connection conn) {
		super(conn);
		this.conn = conn;
		this.setCls(RdDirectroute.class);
	}

}
