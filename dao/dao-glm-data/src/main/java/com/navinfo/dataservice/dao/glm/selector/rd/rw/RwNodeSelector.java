package com.navinfo.dataservice.dao.glm.selector.rd.rw;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwNode;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwNodeMesh;

import oracle.sql.STRUCT;

/**
 * 铁路点查询类
 * @author zhangxiaolong
 *
 */
public class RwNodeSelector implements ISelector {

	private Connection conn;

	public RwNodeSelector(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {
		RwNode rwNode = new RwNode();

		StringBuilder sb = new StringBuilder(
				"select * from " + rwNode.tableName() + " where node_pid = :1 and u_record !=2");

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

				setAttr(rwNode, resultSet);

				List<IRow> meshes = new RwNodeMeshSelector(conn).loadRowsByParentId(id, isLock);

				rwNode.setMeshes(meshes);

				for (IRow row : rwNode.getMeshes()) {
					RwNodeMesh obj = (RwNodeMesh) row;

					rwNode.meshMap.put(obj.rowId(), obj);
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

		return rwNode;
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {
		return null;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock) throws Exception {
		return null;
	}

	// 加载盲端节点
	public List<RwNode> loadEndRdNodeByLinkPid(int linkPid, boolean isLock) throws Exception {

		List<RwNode> nodes = new ArrayList<RwNode>();

		String sql = "with tmp1 as  (select s_node_pid, e_node_pid from rw_link where "
				+ "link_pid = :1), tmp2 as  (select b.link_pid, s_node_pid     from rw_link b "
				+ "   where exists (select null from tmp1 a where a.s_node_pid = b.s_node_pid) "
				+ "and b.u_record!=2   union all   select b.link_pid, e_node_pid     from "
				+ "rw_link b    where exists (select null from tmp1 a where a.s_node_pid = "
				+ "b.e_node_pid) and b.u_record!=2) , tmp3 as  (select b.link_pid, s_node_pid "
				+ "as e_node_pid     from rw_link b    where exists (select null from tmp1 a "
				+ "where a.e_node_pid = b.s_node_pid) and b.u_record!=2   union all   "
				+ "select b.link_pid, e_node_pid     from rw_link b    where exists (select "
				+ "null from tmp1 a where a.e_node_pid = b.e_node_pid) and b.u_record!=2), "
				+ "tmp4 as  (select s_node_pid pid from tmp2 group by s_node_pid having "
				+ "count(*) = 1), tmp5 as  (select e_node_pid pid from tmp3 group by e_node_pid "
				+ "having count(*) = 1), tmp6 as  (select pid from tmp4 union select pid from "
				+ "tmp5) select *   from rw_node a  where exists (select null from tmp6 b "
				+ "where a.node_pid = b.pid) and a.u_record!=2";

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

				RwNode node = new RwNode();

				setAttr(node, resultSet);

				RwNodeMeshSelector meshSelector = new RwNodeMeshSelector(conn);
				
				List<IRow> meshes = meshSelector.loadRowsByParentId(node.getPid(), isLock);
				
				node.setMeshes(meshes);
				
				for (IRow row : meshes) {
					RwNodeMesh mesh = (RwNodeMesh) row;

					node.meshMap.put(mesh.rowId(), mesh);
				}
				
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

	public int loadRwLinkCountOnNode(int nodePid) throws Exception {

		String sql = "select count(1) count from rw_link a where a.s_node_pid=:1 or a.e_node_pid=:2";

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
	
	/**
	 * 设置属性
	 * @param node node对象
	 * @param resultSet 结果集
	 * @throws Exception
	 */
	private void setAttr(RwNode node,ResultSet resultSet) throws Exception
	{
		node.setPid(resultSet.getInt("node_pid"));

		node.setKind(resultSet.getInt("kind"));
		
		node.setForm(resultSet.getInt("form"));

		STRUCT struct = (STRUCT) resultSet.getObject("geometry");

		node.setGeometry(GeoTranslator.struct2Jts(struct, 100000, 0));

		node.setEditFlag(resultSet.getInt("edit_flag"));

		node.setRowId(resultSet.getString("row_id"));
	}
}
