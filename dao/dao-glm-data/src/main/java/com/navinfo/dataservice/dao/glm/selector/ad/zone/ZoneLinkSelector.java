package com.navinfo.dataservice.dao.glm.selector.ad.zone;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.dao.glm.iface.IRow;

import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLink;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLinkKind;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLinkMesh;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.navicommons.database.sql.DBUtils;

;

/**
 * ZONE:Link 查询接口
 * 
 * @author zhaokk
 */
public class ZoneLinkSelector extends AbstractSelector {

	private Connection conn;

	public ZoneLinkSelector(Connection conn) {
		super(conn);
		this.conn = conn;
		this.setCls(ZoneLink.class);
	}

	public List<ZoneLink> loadByNodePid(int nodePid, boolean isLock)
			throws Exception {

		List<ZoneLink> links = new ArrayList<ZoneLink>();

		StringBuilder sb = new StringBuilder(
				"select * from zone_link where (s_node_pid = :1 or e_node_pid = :2) and u_record!=2");

		if (isLock) {
			sb.append(" for update nowait");
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setInt(1, nodePid);

			pstmt.setInt(2, nodePid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				ZoneLink zoneLink = new ZoneLink();

				ReflectionAttrUtils.executeResultSet(zoneLink, resultSet);
				List<IRow> meshes = new AbstractSelector(ZoneLinkMesh.class,
						conn).loadRowsByParentId(zoneLink.getPid(), isLock);
				List<IRow> kinds = new AbstractSelector(ZoneLinkKind.class,
						conn).loadRowsByParentId(zoneLink.getPid(), isLock);

				for (IRow row : meshes) {
					ZoneLinkMesh mesh = (ZoneLinkMesh) row;
					zoneLink.meshMap.put(mesh.rowId(), mesh);
				}
				zoneLink.setMeshes(meshes);
				for (IRow row : kinds) {
					ZoneLinkKind kind = (ZoneLinkKind) row;
					zoneLink.kindMap.put(kind.rowId(), kind);
				}
				links.add(zoneLink);
				zoneLink.setKinds(kinds);
			}
		} catch (Exception e) {

			throw e;

		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);

		}

		return links;

	}

	/***
	 * 加载联通link不考虑方向
	 * 
	 * @param linkPid
	 * @param nodePidDir
	 * @param isLock
	 * @return
	 * @throws Exception
	 */

	public List<ZoneLink> loadTrackLinkNoDirect(int linkPid, int nodePidDir,
			boolean isLock) throws Exception {
		List<ZoneLink> list = new ArrayList<ZoneLink>();
		StringBuilder sb = new StringBuilder();
		sb.append(" select rl.* from zone_link rl  where (rl.s_node_pid = :1 or rl.e_node_pid = :2) and rl.link_pid <> :3 and rl.u_record !=2 ");
		if (isLock) {
			sb.append(" for update nowait");
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setInt(1, nodePidDir);
			pstmt.setInt(2, nodePidDir);
			pstmt.setInt(3, linkPid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				ZoneLink zoneLink = new ZoneLink();
				ReflectionAttrUtils.executeResultSet(zoneLink, resultSet);
				list.add(zoneLink);

			}
			return list;
		} catch (Exception e) {

			throw e;

		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
	}

	/*
	 * 仅加载LINK的pid
	 */
	public List<Integer> loadLinkPidByNodePid(int nodePid, boolean isLock)
			throws Exception {

		List<Integer> links = new ArrayList<Integer>();

		StringBuilder sb = new StringBuilder(
				"select link_pid from ZONE_LINK where (s_node_pid = :1 or e_node_pid = :2) and u_record!=2");

		if (isLock) {
			sb.append(" for update nowait");
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setInt(1, nodePid);

			pstmt.setInt(2, nodePid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				int value = resultSet.getInt("link_pid");

				links.add(value);
			}
		} catch (Exception e) {

			throw e;

		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}

		return links;

	}

}
