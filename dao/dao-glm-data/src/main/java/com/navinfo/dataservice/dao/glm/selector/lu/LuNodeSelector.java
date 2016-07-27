package com.navinfo.dataservice.dao.glm.selector.lu;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.lu.LuNode;
import com.navinfo.dataservice.dao.glm.model.lu.LuNodeMesh;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.vividsolutions.jts.geom.Geometry;

import oracle.sql.STRUCT;

public class LuNodeSelector implements ISelector{
	
	private Logger logger = Logger.getLogger(LuNodeSelector.class);
	
	private Connection conn;
	
	public LuNodeSelector(Connection conn){
		super();
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {
		LuNode luNode = new LuNode();

		StringBuilder sb = new StringBuilder(
				"select * from " + luNode.tableName() + " WHERE node_pid = :1 and  u_record !=2");

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
//				this.setAttr(resultSet, luNode, isLock);
				ReflectionAttrUtils.executeResultSet(luNode, resultSet);
				this.setChildren(resultSet, luNode, isLock);
				return luNode;
			} else {

				throw new Exception("对应LU_NODE不存在!");
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
		return null;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock)
			throws Exception {
		return null;
	}
	
	private void setAttr(ResultSet resultSet, LuNode luNode, boolean isLock) throws Exception{
		luNode.setPid(resultSet.getInt("node_pid"));

		luNode.setForm(resultSet.getInt("form"));

		STRUCT struct = (STRUCT) resultSet.getObject("geometry");

		Geometry geometry = GeoTranslator.struct2Jts(struct, 100000, 0);

		luNode.setGeometry(geometry);

		luNode.setEditFlag(resultSet.getInt("edit_flag"));
		
		luNode.setRowId(resultSet.getString("row_id"));

		List<IRow> forms = new LuNodeMeshSelector(conn).loadRowsByParentId(luNode.pid(), isLock);

		luNode.setMeshes(forms);

		for (IRow row : luNode.getMeshes()) {
			LuNodeMesh mesh = (LuNodeMesh) row;

			luNode.meshMap.put(mesh.rowId(), mesh);
		}
		
	}
	
	private void setChildren(ResultSet resultSet, LuNode luNode, boolean isLock) throws Exception{

		List<IRow> forms = new LuNodeMeshSelector(conn).loadRowsByParentId(luNode.pid(), isLock);

		luNode.setMeshes(forms);

		for (IRow row : luNode.getMeshes()) {
			LuNodeMesh mesh = (LuNodeMesh) row;

			luNode.meshMap.put(mesh.rowId(), mesh);
		}
		
	}
	
	// 加载盲端节点
		public List<LuNode> loadEndLuNodeByLinkPid(int linkPid, boolean isLock)
				throws Exception {

			List<LuNode> nodes = new ArrayList<LuNode>();
			String sql ="with tmp1 as  (select s_node_pid, e_node_pid from lu_link where link_pid = :1), tmp2 as  (select b.link_pid, s_node_pid     from lu_link b    where exists (select null from tmp1 a where a.s_node_pid = b.s_node_pid) and b.u_record!=2   union all   select b.link_pid, e_node_pid     from lu_link b    where exists (select null from tmp1 a where a.s_node_pid = b.e_node_pid) and b.u_record!=2) , tmp3 as  (select b.link_pid, s_node_pid as e_node_pid     from lu_link b    where exists (select null from tmp1 a where a.e_node_pid = b.s_node_pid) and b.u_record!=2   union all   select b.link_pid, e_node_pid     from lu_link b    where exists (select null from tmp1 a where a.e_node_pid = b.e_node_pid) and b.u_record!=2), tmp4 as  (select s_node_pid pid from tmp2 group by s_node_pid having count(*) = 1), tmp5 as  (select e_node_pid pid from tmp3 group by e_node_pid having count(*) = 1), tmp6 as  (select pid from tmp4 union select pid from tmp5) select *   from lu_node a  where exists (select null from tmp6 b where a.node_pid = b.pid) and a.u_record!=2";
			
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

					LuNode luNode = new LuNode();

//					this.setAttr(resultSet, luNode, isLock);
					ReflectionAttrUtils.executeResultSet(luNode, resultSet);
					this.setChildren(resultSet, luNode, isLock);

					nodes.add(luNode);
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

}
