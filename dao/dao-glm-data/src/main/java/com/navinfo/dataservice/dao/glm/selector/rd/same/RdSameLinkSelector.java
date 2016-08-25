package com.navinfo.dataservice.dao.glm.selector.rd.same;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import oracle.sql.STRUCT;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameLink;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameLinkPart;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.vividsolutions.jts.geom.Geometry;

public class RdSameLinkSelector extends AbstractSelector {

	private Connection conn;

	/**
	 * @param cls
	 * @param conn
	 */
	public RdSameLinkSelector(Connection conn) {

		super(RdSameLink.class, conn);

		this.conn = conn;
	}

	public Geometry getMainLinkGeometry(int linkPid, String tableName,
			boolean isLock) throws Exception {

		StringBuilder sb = new StringBuilder("select geometry from "
				+ tableName + " where link_pid = :1 and u_record!=2");

		if (isLock) {

			sb.append(" for update nowait");
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {

			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setInt(1, linkPid);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {

				return GeoTranslator.struct2Jts((STRUCT) resultSet
						.getObject("geometry"));
			}
		} catch (Exception e) {

			throw e;

		} finally {

			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);
		}

		return null;
	}

	public RdSameLinkPart loadLinkPartByLink(int linkPid, String tableName,
			boolean isLock) throws Exception {

		String strSql = "SELECT * FROM RD_SAMELINK_PART WHERE LINK_PID = :1 AND TABLE_NAME = :2 AND U_RECORD != 2";

		if (isLock) {

			strSql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {

			pstmt = conn.prepareStatement(strSql);

			pstmt.setInt(1, linkPid);

			pstmt.setString(2, tableName.toUpperCase());

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {

				RdSameLinkPart linkPart = new RdSameLinkPart();

				ReflectionAttrUtils.executeResultSet(linkPart, resultSet);

				return linkPart;
			}
		} catch (Exception e) {

			throw e;

		} finally {

			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);
		}

		return null;
	}
}
