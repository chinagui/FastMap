package com.navinfo.dataservice.dao.glm.selector.ad.geo;

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
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNodeMesh;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class AdNodeSelector implements ISelector {

	private static Logger logger = Logger.getLogger(AdFaceSelector.class);

	private Connection conn;

	public AdNodeSelector(Connection conn) {
		super();
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {
		AdNode adNode = new AdNode();

		StringBuilder sb = new StringBuilder(
				"select * from " + adNode.tableName() + " WHERE node_pid = :1 and  u_record !=2");

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
				adNode.setPid(id);

				adNode.setKind(resultSet.getInt("kind"));

				adNode.setForm(resultSet.getInt("form"));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				Geometry geometry = GeoTranslator.struct2Jts(struct, 100000, 0);

				adNode.setGeometry(geometry);

				adNode.setEditFlag(resultSet.getInt("edit_flag"));
				
				adNode.setRowId(resultSet.getString("row_id"));

				// 获取AD_Node对应的关联数据

				// ad_node_mesh
				List<IRow> forms = new AdNodeMeshSelector(conn).loadRowsByParentId(id, isLock);

				adNode.setMeshes(forms);

				for (IRow row : adNode.getMeshes()) {
					AdNodeMesh mesh = (AdNodeMesh) row;

					adNode.meshMap.put(mesh.rowId(), mesh);
				}

				return adNode;
			} else {

				throw new Exception("对应AD_NODE不存在!");
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
	// 加载盲端节点
	public List<AdNode> loadEndAdNodeByLinkPid(int linkPid, boolean isLock)
			throws Exception {

		List<AdNode> nodes = new ArrayList<AdNode>();
		String sql ="with tmp1 as  (select s_node_pid, e_node_pid from ad_link where link_pid = :1), tmp2 as  (select b.link_pid, s_node_pid     from ad_link b    where exists (select null from tmp1 a where a.s_node_pid = b.s_node_pid) and b.u_record!=2   union all   select b.link_pid, e_node_pid     from ad_link b    where exists (select null from tmp1 a where a.s_node_pid = b.e_node_pid) and b.u_record!=2) , tmp3 as  (select b.link_pid, s_node_pid as e_node_pid     from ad_link b    where exists (select null from tmp1 a where a.e_node_pid = b.s_node_pid) and b.u_record!=2   union all   select b.link_pid, e_node_pid     from ad_link b    where exists (select null from tmp1 a where a.e_node_pid = b.e_node_pid) and b.u_record!=2), tmp4 as  (select s_node_pid pid from tmp2 group by s_node_pid having count(*) = 1), tmp5 as  (select e_node_pid pid from tmp3 group by e_node_pid having count(*) = 1), tmp6 as  (select pid from tmp4 union select pid from tmp5) select *   from ad_node a  where exists (select null from tmp6 b where a.node_pid = b.pid) and a.u_record!=2";
		
		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setInt(1, linkPid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				AdNode node = new AdNode();

				node.setPid(resultSet.getInt("node_pid"));
				
				node.setRowId(resultSet.getString("row_id"));

				node.setKind(resultSet.getInt("kind"));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				Geometry geometry = GeoTranslator.struct2Jts(struct);

				Coordinate point = geometry.getCoordinate();

				node.setMesh(Integer.parseInt(MeshUtils.lonlat2Mesh(point.x,
						point.y)));

				node.setGeometry(GeoTranslator.struct2Jts(struct, 100000, 0));

				node.setRowId(resultSet.getString("row_id"));

				AdNodeMeshSelector mesh = new AdNodeMeshSelector(conn);

				node.setMeshes(mesh.loadRowsByParentId(node.getPid(), isLock));

				nodes.add(node);
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

		return nodes;
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {
		return null;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock) throws Exception {
		return null;
	}
	public int loadAdLinkCountOnNode(int nodePid)
			throws Exception {

		String sql = "select count(1) count from ad_link a where a.s_node_pid=:1 or a.e_node_pid=:2";
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setInt(1, nodePid);
			
			pstmt.setInt(2, nodePid);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {
				return resultSet.getInt("count");
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
		
		return 0;
	}

}
