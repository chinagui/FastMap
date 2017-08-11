package com.navinfo.dataservice.dao.glm.selector.rd.gsc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGscLink;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.dataservice.dao.glm.selector.rd.cross.RdCrossSelector;
import com.navinfo.navicommons.database.sql.DBUtils;

public class RdGscLinkSelector extends AbstractSelector {

	private static Logger logger = Logger.getLogger(RdCrossSelector.class);

	private Connection conn;

	public RdGscLinkSelector(Connection conn) {
		super(conn);
		this.conn = conn;
		this.setCls(RdGscLink.class);
	}
	
	public List<IRow> loadRowsByParentIdAndLinkId(int parentId,int linkId, boolean isLock) throws Exception {
		List<IRow> rows = new ArrayList<IRow>();

		String sql = "select * from rd_gsc_link where pid=:1 and u_record!=:2 and link_pid = :3";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setInt(1, parentId);

			pstmt.setInt(2, 2);
			
			pstmt.setInt(3, linkId);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				RdGscLink link = new RdGscLink();

				ReflectionAttrUtils.executeResultSet(link, resultSet);

				rows.add(link);
			}
		} catch (Exception e) {

			throw e;

		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}

		return rows;
	}

	public List<RdGscLink> loadByLinkId(int linkId, boolean isLock) throws Exception {
		List<RdGscLink> rows = new ArrayList<RdGscLink>();

		String sql = "select * from rd_gsc_link where u_record!=:1 and link_pid = :2";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setInt(1, 2);

			pstmt.setInt(2, linkId);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				RdGscLink link = new RdGscLink();

				ReflectionAttrUtils.executeResultSet(link, resultSet);

				rows.add(link);
			}
		} catch (Exception e) {

			throw e;

		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}

		return rows;
	}
}
