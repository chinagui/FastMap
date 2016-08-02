package com.navinfo.dataservice.dao.glm.selector.ad.zone;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFace;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFaceTopo;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;

import oracle.sql.STRUCT;
/**
 * ZONE:Face查询接口
 * @author zhaokk
 *
 */
public class ZoneFaceSelector extends AbstractSelector {

	private static Logger logger = Logger.getLogger(ZoneFaceSelector.class);

	private Connection conn;

	public ZoneFaceSelector(Connection conn) {
		super(conn);
		this.conn = conn;
		this.setCls(ZoneFace.class);
	}

	/**
	 * 获取此ZONELINK上行政取区划面拓扑关系
	 * @param linkPid
	 * @param isLock
	 * @return
	 * @throws Exception
	 */
	public List<ZoneFace> loadZoneFaceByLinkId(int linkPid, boolean isLock)
			throws Exception {
		List<ZoneFace> faces = new ArrayList<ZoneFace>();
		StringBuilder bf = new StringBuilder();
        bf.append("select b.* from zone_face b where b.face_pid in (select a.face_pid ");
        bf.append("  FROM zone_face a, zone_face_topo t");
        bf.append(" WHERE     a.u_record != 2  AND T.u_record != 2");
        bf.append("  AND a.face_pid = t.face_pid");
        bf.append(" AND t.link_pid = :1 group by a.face_pid)");
		if (isLock) {
			 bf.append( " for update nowait");
		}
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(bf.toString());

			pstmt.setInt(1, linkPid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				
				ZoneFace face = new ZoneFace();

				face.setPid(resultSet.getInt("face_pid"));
				
				face.setRegionId(resultSet.getInt("region_id"));
				
				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				face.setGeometry(GeoTranslator.struct2Jts(struct, 100000, 0));
				
				face.setArea(resultSet.getDouble("area"));
				
				face.setPerimeter(resultSet.getDouble("perimeter"));
				
				face.setMeshId(resultSet.getInt("mesh_id"));
				
				face.setEditFlag(resultSet.getInt("edit_flag"));
				
				face.setRowId(resultSet.getString("row_id"));

				faces.add(face);
				List<IRow> zoneFaceTopo = new AbstractSelector(ZoneFaceTopo.class,conn).loadRowsByParentId(face.getPid(), isLock);

				for (IRow row : zoneFaceTopo) {
					row.setMesh(face.mesh());
				}
				face.setFaceTopos(zoneFaceTopo);

				for (IRow row : zoneFaceTopo) {
					ZoneFaceTopo obj = (ZoneFaceTopo) row;

					face.zoneFaceTopoMap.put(obj.rowId(), obj);
				}
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

		return faces;
	}
	public List<ZoneFace> loadZoneFaceByNodeId(int nodePid, boolean isLock)
			throws Exception {

		List<ZoneFace> faces = new ArrayList<ZoneFace>();

		StringBuilder  builder = new StringBuilder();
		builder.append(" SELECT b.* from zone_face b where b.face_pid in (select a.face_pid ");
		builder.append(" FROM zone_face a, zone_face_topo t, zone_link l ");
		builder.append(" WHERE a.u_record != 2 and t.u_record != 2 and l.u_record != 2 ");
		builder.append(" AND a.face_pid = t.face_pid");
		builder.append(" AND t.link_pid = l.link_pid");
		builder.append(" AND (l.s_node_pid = :1 OR l.e_node_pid = :2) group by a.face_pid)");
		
		if (isLock) {
			builder.append(" for update nowait");
		}
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(builder.toString());

			pstmt.setInt(1, nodePid);
			
			pstmt.setInt(2, nodePid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				
				ZoneFace face = new ZoneFace();

				face.setPid(resultSet.getInt("face_pid"));
				
				face.setRegionId(resultSet.getInt("region_id"));
				
				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				face.setGeometry(GeoTranslator.struct2Jts(struct, 100000, 0));
				
				face.setArea(resultSet.getDouble("area"));
				
				face.setPerimeter(resultSet.getDouble("perimeter"));
				
				face.setMeshId(resultSet.getInt("mesh_id"));
				
				face.setEditFlag(resultSet.getInt("edit_flag"));
				
				face.setRowId(resultSet.getString("row_id"));

				faces.add(face);
				
				// Zone_face_topo
				List<IRow> adFaceTopo = new ZoneFaceTopoSelector(conn).loadRowsByParentId(face.getPid(), isLock);

				for (IRow row : adFaceTopo) {
					row.setMesh(face.mesh());
				}

				face.setFaceTopos(adFaceTopo);

				for (IRow row : adFaceTopo) {
					ZoneFaceTopo obj = (ZoneFaceTopo) row;

					face.zoneFaceTopoMap.put(obj.rowId(), obj);
				}
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

		return faces;
	}
	
	
}
