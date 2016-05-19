package com.navinfo.dataservice.dao.glm.selector.ad.geo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLinkMesh;
import com.vividsolutions.jts.geom.Geometry;

import oracle.sql.STRUCT;

public class AdLinkSelector implements ISelector {

	private static Logger logger = Logger.getLogger(AdFaceSelector.class);

	private Connection conn;

	public AdLinkSelector(Connection conn) {
		super();
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {
		AdLink adLink = new AdLink();

		StringBuilder sb = new StringBuilder(
				 "select * from " + adLink.tableName() + " WHERE link_pid = :1 and  u_record !=2");

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
				adLink.setPid(id);

				adLink.setsNodePid(resultSet.getInt("s_node_pid"));

				adLink.seteNodePid(resultSet.getInt("e_node_pid"));

				adLink.setKind(resultSet.getInt("kind"));
				
				adLink.setForm(resultSet.getInt("form"));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				Geometry geometry = GeoTranslator.struct2Jts(struct, 100000, 0);

				adLink.setGeometry(geometry);

				adLink.setLength(resultSet.getDouble("length"));

				adLink.setScale(resultSet.getInt("scale"));

				adLink.setEditFlag(resultSet.getInt("edit_flag"));
				
				adLink.setRowId(resultSet.getString("row_id"));

				// 获取AD_LINK对应的关联数据
				// ad_link_mesh
				List<IRow> forms = new AdLinkMeshSelector(conn).loadRowsByParentId(id, isLock);
				
				adLink.setMeshes(forms);

				for (IRow row : adLink.getMeshes()) {
					AdLinkMesh mesh = (AdLinkMesh) row;

					adLink.meshMap.put(mesh.rowId(), mesh);
				}

				return adLink;
			} else {

				throw new Exception("对应AD_LINK不存在!");
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
	public List<AdLink> loadByNodePid(int nodePid, boolean isLock)
			throws Exception {

		List<AdLink> links = new ArrayList<AdLink>();

		StringBuilder sb = new StringBuilder(
				"select * from ad_link where (s_node_pid = :1 or e_node_pid = :2) and u_record!=2");

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
				AdLink adLink = new AdLink();

				adLink.setPid(resultSet.getInt("link_pid"));
				adLink.setsNodePid(resultSet.getInt("s_node_pid"));
				adLink.seteNodePid(resultSet.getInt("e_node_pid"));
	            adLink.setKind(resultSet.getInt("kind"));
				adLink.setForm(resultSet.getInt("form"));
				STRUCT struct = (STRUCT) resultSet.getObject("geometry");
				Geometry geometry = GeoTranslator.struct2Jts(struct, 100000, 0);
				adLink.setGeometry(geometry);
				adLink.setLength(resultSet.getInt("length"));
				adLink.setScale(resultSet.getInt("scale"));
				adLink.setEditFlag(resultSet.getInt("edit_flag"));
				adLink.setRowId(resultSet.getString("row_id"));
				List<IRow> forms = new AdLinkMeshSelector(conn).loadRowsByParentId(adLink.getPid(), isLock);
				
				//loadRowsByParentId已经查询了mesh,是否可以不做设置
				for (IRow row : forms) {
					row.setMesh(adLink.getMesh());
				}

				adLink.setMeshes(forms);

				for (IRow row : adLink.getMeshes()) {
					AdLinkMesh mesh = (AdLinkMesh) row;

					adLink.meshMap.put(mesh.rowId(), mesh);
				}
				links.add(adLink);
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


	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {
		return null;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock) throws Exception {
		return null;
	}

}
