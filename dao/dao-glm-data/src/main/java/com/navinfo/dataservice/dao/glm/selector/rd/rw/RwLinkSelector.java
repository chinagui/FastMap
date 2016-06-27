package com.navinfo.dataservice.dao.glm.selector.rd.rw;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLink;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLinkName;
import com.vividsolutions.jts.geom.Geometry;

import oracle.sql.STRUCT;

public class RwLinkSelector implements ISelector {

	private Connection conn;

	public RwLinkSelector(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {
		RwLink rwLink = new RwLink();

		StringBuilder sb = new StringBuilder(
				"select * from " + rwLink.tableName() + " where link_pid = :1 and u_record !=2");

		if (isLock) {
			sb.append(" for update nowait");
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setInt(1, id);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {
				
				setAttr(rwLink, resultSet);

				List<IRow> names = new RwLinkNameSelector(conn).loadRowsByParentId(id, isLock);

				rwLink.setNames(names);

				for (IRow row : rwLink.getNames()) {
					RwLinkName obj = (RwLinkName) row;

					rwLink.linkNameMap.put(obj.rowId(), obj);
				}
			}
			else
			{
				throw new Exception("对应RWLINK: "+id+" 不存在!");
			}
		} catch (Exception e) {

			throw e;

		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
			} catch (Exception e) {

			}

			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (Exception e) {

			}

		}

		return rwLink;
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {
		return null;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock) throws Exception {
		return null;
	}

	public List<RwLink> loadByNodePid(int nodePid, boolean isLock) throws Exception {

		List<RwLink> links = new ArrayList<RwLink>();

		StringBuilder sb = new StringBuilder(
				"select * from rw_link where (s_node_pid = :1 or e_node_pid = :2) and u_record!=2");

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
				RwLink rwLink = new RwLink();

				setAttr(rwLink, resultSet);

				// 获取LINK对应的关联数据 rd_link_name
				List<IRow> names = new RwLinkNameSelector(conn).loadRowsByParentId(rwLink.getPid(), isLock);

				rwLink.setNames(names);

				links.add(rwLink);
			}
		} catch (Exception e) {

			throw e;

		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
			} catch (Exception e) {

			}

			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (Exception e) {

			}

		}

		return links;

	}

	private void setAttr(RwLink rwLink, ResultSet resultSet) throws Exception {
		rwLink.setPid(resultSet.getInt("link_pid"));

		rwLink.setFeaturePid(resultSet.getInt("feature_pid"));

		rwLink.setsNodePid(resultSet.getInt("s_node_pid"));

		rwLink.seteNodePid(resultSet.getInt("e_node_pid"));

		rwLink.setKind(resultSet.getInt("kind"));

		rwLink.setForm(resultSet.getInt("form"));

		rwLink.setLength(resultSet.getDouble("length"));

		STRUCT struct = (STRUCT) resultSet.getObject("geometry");

		Geometry geometry = GeoTranslator.struct2Jts(struct, 100000, 0);

		rwLink.setGeometry(geometry);

		rwLink.setMeshId(resultSet.getInt("mesh_id"));

		rwLink.setScale(resultSet.getInt("scale"));

		rwLink.setDetailFlag(resultSet.getInt("detail_flag"));

		rwLink.setEditFlag(resultSet.getInt("edit_flag"));

		rwLink.setColor(resultSet.getString("color"));

		rwLink.setRowId(resultSet.getString("row_id"));
	}
}
