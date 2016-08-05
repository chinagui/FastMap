package com.navinfo.dataservice.dao.glm.selector.rd.directroute;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.glm.model.rd.directroute.RdDirectroute;
import com.navinfo.dataservice.dao.glm.model.rd.directroute.RdDirectrouteVia;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.navicommons.database.sql.DBUtils;

public class RdDirectrouteSelector extends AbstractSelector {

	private static Logger logger = Logger
			.getLogger(RdDirectrouteSelector.class);

	private Connection conn;

	public RdDirectrouteSelector(Connection conn) {
		super(conn);
		this.conn = conn;
		this.setCls(RdDirectroute.class);
	}

	public RdDirectroute loadByLinkNodeLink(int inLinkPid, int nodePid,
			int outLinkPid, boolean isLock) throws Exception {

		RdDirectroute directroute = new RdDirectroute();

		String sql = "select a.* from rd_directroute a where a.in_link_pid=:1 and a.node_pid=:2 and a.out_link_pid=:3 and a.u_record!=2";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, inLinkPid);

			pstmt.setInt(2, nodePid);

			pstmt.setInt(3, outLinkPid);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {
				ReflectionAttrUtils.executeResultSet(directroute, resultSet);
				
				directroute.setVias( new AbstractSelector(RdDirectrouteVia.class,conn).loadRowsByParentId(directroute.getPid(), isLock));
				
			} else {
				return null;
			}
		} catch (Exception e) {

			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return directroute;

	}

}
