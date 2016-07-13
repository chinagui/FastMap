package com.navinfo.dataservice.dao.glm.selector.lu;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import oracle.sql.STRUCT;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.lu.LuLink;
import com.navinfo.dataservice.dao.glm.model.lu.LuLinkMesh;
import com.vividsolutions.jts.geom.Geometry;

public class LuLinkSelector implements ISelector {
	
	private Logger logger = Logger.getLogger(LuLinkSelector.class);
	
	private Connection conn;
	
	public LuLinkSelector(Connection conn) {
		super();
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {
		LuLink luLink = new LuLink();

		StringBuilder sb = new StringBuilder(
				 "select * from " + luLink.tableName() + " WHERE link_pid = :1 and  u_record !=2");

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
				setAttr(isLock, luLink, resultSet);

				return luLink;
			} else {

				throw new Exception("对应LU_LINK不存在!");
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
	}

	private void setAttr(boolean isLock, LuLink luLink, ResultSet resultSet)
			throws SQLException, Exception {
		luLink.setPid(resultSet.getInt("link_pid"));

		luLink.setsNodePid(resultSet.getInt("s_node_pid"));

		luLink.seteNodePid(resultSet.getInt("e_node_pid"));

		STRUCT struct = (STRUCT) resultSet.getObject("geometry");

		Geometry geometry = GeoTranslator.struct2Jts(struct, 100000, 0);

		luLink.setGeometry(geometry);

		luLink.setLength(resultSet.getDouble("length"));


		luLink.setEditFlag(resultSet.getInt("edit_flag"));
		
		luLink.setRowId(resultSet.getString("row_id"));

		List<IRow> forms = new LuLinkMeshSelector(conn).loadRowsByParentId(luLink.pid(), isLock);
		
		luLink.setMeshes(forms);

		for (IRow row : luLink.getMeshes()) {
			LuLinkMesh mesh = (LuLinkMesh) row;

			luLink.meshMap.put(mesh.rowId(), mesh);
		}
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public List<LuLink> loadByNodePid(int nodePid, boolean isLock)
			throws Exception {

		List<LuLink> links = new ArrayList<LuLink>();

		StringBuilder sb = new StringBuilder(
				"select * from lu_link where (s_node_pid = :1 or e_node_pid = :2) and u_record!=2");

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
				LuLink luLink = new LuLink();

				luLink.setPid(resultSet.getInt("link_pid"));
				luLink.setsNodePid(resultSet.getInt("s_node_pid"));
				luLink.seteNodePid(resultSet.getInt("e_node_pid"));
				STRUCT struct = (STRUCT) resultSet.getObject("geometry");
				Geometry geometry = GeoTranslator.struct2Jts(struct, 100000, 0);
				luLink.setGeometry(geometry);
				luLink.setLength(resultSet.getInt("length"));
				luLink.setEditFlag(resultSet.getInt("edit_flag"));
				luLink.setRowId(resultSet.getString("row_id"));
				List<IRow> forms = new LuLinkMeshSelector(conn).loadRowsByParentId(luLink.getPid(), isLock);
				
				//loadRowsByParentId已经查询了mesh,是否可以不做设置
				for (IRow row : forms) {
					row.setMesh(luLink.mesh());
				}

				luLink.setMeshes(forms);

				for (IRow row : luLink.getMeshes()) {
					LuLinkMesh mesh = (LuLinkMesh) row;

					luLink.meshMap.put(mesh.rowId(), mesh);
				}
				links.add(luLink);
				}
			}catch (Exception e) {

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
}
