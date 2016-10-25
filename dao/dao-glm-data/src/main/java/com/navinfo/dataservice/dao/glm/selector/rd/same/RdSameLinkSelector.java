package com.navinfo.dataservice.dao.glm.selector.rd.same;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameLink;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameLinkPart;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.vividsolutions.jts.geom.Geometry;

import oracle.sql.STRUCT;

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

	public Geometry getMainLinkGeometry(int linkPid, String tableName, boolean isLock) throws Exception {

		StringBuilder sb = new StringBuilder(
				"select geometry from " + tableName + " where link_pid = :1 and u_record!=2");

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

				return GeoTranslator.struct2Jts((STRUCT) resultSet.getObject("geometry"));
			}
		} catch (Exception e) {

			throw e;

		} finally {

			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);
		}

		return null;
	}

	public RdSameLinkPart loadLinkPartByLink(int linkPid, String tableName, boolean isLock) throws Exception {

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

	/**
	 * 根据nodePid和表名称查询同一线
	 * 
	 * @param nodePid
	 *            node点pid
	 * @param tableName
	 *            node点类型的link表名
	 * @return 同一线集合
	 * @author zhangxiaolong
	 * @throws Exception
	 */
	public List<RdSameLink> loadSameLinkByNodeAndTableName(int nodePid, String tableName, boolean isLock)
			throws Exception {
		List<RdSameLink> sameLinkList = new ArrayList<>();

		String sql = "with tmp1 as ( select link_pid from " + tableName + " where s_node_pid = " + nodePid
				+ " or e_node_pid = " + nodePid
				+ " and u_record !=2), tmp2 as (select group_id from  RD_SAMELINK_PART a,tmp1 where a.link_pid = tmp1.link_pid and  a.table_name = '"
				+ tableName
				+ "' and a.u_record !=2) select a.* from RD_SAMELINK a,tmp2 where a.group_id = tmp2.group_id and a.u_record !=2";

		if (isLock) {

			sql += " for update nowait";
		}
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {

			pstmt = conn.prepareStatement(sql);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				RdSameLink sameLink = new RdSameLink();
				ReflectionAttrUtils.executeResultSet(sameLink, resultSet);
				List<IRow> parts = loadRowsByClassParentId(RdSameLinkPart.class, sameLink.getPid(), isLock,"");
				sameLink.setParts(parts);
				sameLinkList.add(sameLink);
			}
		} catch (Exception e) {

			throw e;

		} finally {

			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);
		}
		return sameLinkList;
	}
}
