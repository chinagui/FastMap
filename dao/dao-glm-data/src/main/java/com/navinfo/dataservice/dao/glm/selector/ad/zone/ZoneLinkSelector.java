package com.navinfo.dataservice.dao.glm.selector.ad.zone;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import oracle.sql.STRUCT;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLinkMesh;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLink;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLinkKind;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLinkMesh;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneNode;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneNodeMesh;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdLinkMeshSelector;
import com.vividsolutions.jts.geom.Geometry;


/**
 * ZONE:Link  查询接口
 * @author zhaokk
 *
 */
public class ZoneLinkSelector implements ISelector  {
	
	private static Logger logger = Logger.getLogger(ZoneLinkSelector.class);

	private Connection conn;

	public ZoneLinkSelector(Connection conn) {
		super();
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {
		ZoneLink link = new ZoneLink();

		StringBuilder sb = new StringBuilder(
				"select * from " + link.tableName() + " WHERE link_pid = :1 and  u_record !=2");

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
				link.setPid(id);
				STRUCT struct = (STRUCT) resultSet.getObject("geometry");
				Geometry geometry = GeoTranslator.struct2Jts(struct, 100000, 0);
				link.setGeometry(geometry);
				link.setEditFlag(resultSet.getInt("edit_flag"));
				link.setLength(resultSet.getDouble("length"));
				link.setsNodePid(resultSet.getInt("s_node_pid"));
				link.seteNodePid(resultSet.getInt("e_node_pid"));
				link.setRowId(resultSet.getString("row_id"));
				link.setScale(resultSet.getInt("scale"));
				// 获取Zone_Node对应的关联数据

				// zone_link_mesh
				List<IRow> meshes = new ZoneLinkMeshSelector(conn).loadRowsByParentId(id, isLock);

				link.setMeshes(meshes);

				for (IRow row : link.getMeshes()) {
					ZoneLinkMesh mesh = (ZoneLinkMesh) row;

					link.meshMap.put(mesh.rowId(), mesh);
				}
				// zone_link_kind
				List<IRow> kinds = new ZoneLinkKindSelector(conn).loadRowsByParentId(id, isLock);

				link.setKinds(kinds);

				for (IRow row : link.getKinds()) {
					ZoneLinkKind kind = (ZoneLinkKind) row;

					link.kindMap.put(kind.rowId(), kind);
				}
				return link;
			} else {

				throw new Exception("对应ZONE_Link不存在!");
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

				zoneLink.setPid(resultSet.getInt("link_pid"));
				zoneLink.setsNodePid(resultSet.getInt("s_node_pid"));
				zoneLink.seteNodePid(resultSet.getInt("e_node_pid"));
				STRUCT struct = (STRUCT) resultSet.getObject("geometry");
				Geometry geometry = GeoTranslator.struct2Jts(struct, 100000, 0);
				zoneLink.setGeometry(geometry);
				zoneLink.setLength(resultSet.getInt("length"));
				zoneLink.setScale(resultSet.getInt("scale"));
				zoneLink.setEditFlag(resultSet.getInt("edit_flag"));
				zoneLink.setRowId(resultSet.getString("row_id"));
				List<IRow> forms = new ZoneLinkMeshSelector(conn).loadRowsByParentId(zoneLink.getPid(), isLock);
				List<IRow> kinds = new ZoneLinkKindSelector(conn).loadRowsByParentId(zoneLink.getPid(), isLock);

				for (IRow row : forms) {
					ZoneLinkMesh mesh = (ZoneLinkMesh) row;

					zoneLink.meshMap.put(mesh.rowId(), mesh);
				}
				for (IRow row : kinds) {
					ZoneLinkKind kind = (ZoneLinkKind) row;
					zoneLink.kindMap.put(kind.rowId(), kind);
				}
				links.add(zoneLink);
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
