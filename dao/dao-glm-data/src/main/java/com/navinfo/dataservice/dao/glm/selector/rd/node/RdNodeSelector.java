package com.navinfo.dataservice.dao.glm.selector.rd.node;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeForm;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeMesh;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeName;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;

import oracle.sql.STRUCT;

public class RdNodeSelector extends AbstractSelector {

	private static Logger logger = Logger.getLogger(RdNodeSelector.class);

	private Connection conn;

	public RdNodeSelector(Connection conn) {
		super(conn);
		this.conn = conn;
		this.setCls(RdNode.class);
	}

	// 加载盲端节点
	public List<RdNode> loadEndRdNodeByLinkPid(int linkPid, boolean isLock)
			throws Exception {

		List<RdNode> nodes = new ArrayList<RdNode>();

		String sql ="with tmp1 as  (select s_node_pid, e_node_pid from rd_link where link_pid = :1), tmp2 as  (select b.link_pid, s_node_pid     from rd_link b    where exists (select null from tmp1 a where a.s_node_pid = b.s_node_pid) and b.u_record!=2   union all   select b.link_pid, e_node_pid     from rd_link b    where exists (select null from tmp1 a where a.s_node_pid = b.e_node_pid) and b.u_record!=2) , tmp3 as  (select b.link_pid, s_node_pid as e_node_pid     from rd_link b    where exists (select null from tmp1 a where a.e_node_pid = b.s_node_pid) and b.u_record!=2   union all   select b.link_pid, e_node_pid     from rd_link b    where exists (select null from tmp1 a where a.e_node_pid = b.e_node_pid) and b.u_record!=2), tmp4 as  (select s_node_pid pid from tmp2 group by s_node_pid having count(*) = 1), tmp5 as  (select e_node_pid pid from tmp3 group by e_node_pid having count(*) = 1), tmp6 as  (select pid from tmp4 union select pid from tmp5) select *   from rd_node a  where exists (select null from tmp6 b where a.node_pid = b.pid) and a.u_record!=2";
		
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

				RdNode node = new RdNode();

				node.setPid(resultSet.getInt("node_pid"));

				node.setKind(resultSet.getInt("kind"));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				node.setGeometry(GeoTranslator.struct2Jts(struct, 100000, 0));

				node.setAdasFlag(resultSet.getInt("adas_flag"));

				node.setEditFlag(resultSet.getInt("edit_flag"));

				node.setDifGroupid(resultSet.getString("dif_groupid"));

				node.setSrcFlag(resultSet.getInt("src_flag"));

				node.setDigitalLevel(resultSet.getInt("digital_level"));

				node.setReserved(resultSet.getString("reserved"));

				node.setRowId(resultSet.getString("row_id"));

				setChildData(node,isLock);

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

	public List<RdNode> loadRdNodeByLinkPid(int linkPid, boolean isLock)
			throws Exception {

		List<RdNode> nodes = new ArrayList<RdNode>();

		String sql = "select a.* from rd_node a where exists(select null from rd_link b where (b.e_node_pid=a.node_pid or b.s_node_pid=a.node_pid) and b.link_pid=:1)";

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

				RdNode node = new RdNode();

				node.setPid(resultSet.getInt("node_pid"));

				node.setKind(resultSet.getInt("kind"));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				node.setGeometry(GeoTranslator.struct2Jts(struct, 100000, 0));

				node.setAdasFlag(resultSet.getInt("adas_flag"));

				node.setEditFlag(resultSet.getInt("edit_flag"));

				node.setDifGroupid(resultSet.getString("dif_groupid"));

				node.setSrcFlag(resultSet.getInt("src_flag"));

				node.setDigitalLevel(resultSet.getInt("digital_level"));

				node.setReserved(resultSet.getString("reserved"));

				node.setRowId(resultSet.getString("row_id"));

				setChildData(node,isLock);

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

	
	public int loadRdLinkCountOnNode(int nodePid)
			throws Exception {

		String sql = "select count(1) count from rd_link a where a.s_node_pid=:1 or a.e_node_pid=:2";
		
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
	 * 设置子表数据
	 * @param node
	 * @param isLock
	 * @throws Exception
	 */
	private void setChildData(RdNode node,boolean isLock) throws Exception
	{
		// 获取Node对应的关联数据
		node.setMeshes(new AbstractSelector(RdNodeMesh.class,conn).loadRowsByParentId(node.getPid(), isLock));

		List<IRow> names = new AbstractSelector(RdNodeName.class,conn).loadRowsByParentId(node.getPid(),
				isLock);

		for (IRow row : names) {
			row.setMesh(node.mesh());
		}

		node.setNames(names);

		List<IRow> forms = new AbstractSelector(RdNodeForm.class,conn).loadRowsByParentId(node.getPid(),
				isLock);

		for (IRow row : forms) {
			row.setMesh(node.mesh());
		}

		node.setForms(forms);
	}

}