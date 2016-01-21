package com.navinfo.dataservice.FosEngine.edit.model.selector.rd.node;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import oracle.sql.STRUCT;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.FosEngine.edit.model.IRow;
import com.navinfo.dataservice.FosEngine.edit.model.ISelector;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.node.RdNode;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.node.RdNodeForm;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.node.RdNodeMesh;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.node.RdNodeName;
import com.navinfo.dataservice.commons.exception.DataNotFoundException;
import com.navinfo.dataservice.commons.geom.GeoTranslator;

public class RdNodeSelector implements ISelector {

	private static Logger logger = Logger.getLogger(RdNodeSelector.class);

	private Connection conn;

	public RdNodeSelector(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {

		RdNode node = new RdNode();

		String sql = "select * from " + node.tableName() + " where node_pid=:1";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, id);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {

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
				
				RdNodeMeshSelector mesh = new RdNodeMeshSelector(conn);

				node.setMeshes(mesh.loadRowsByParentId(id, isLock));
				
				for (IRow row : node.getMeshes()) {
					RdNodeMesh obj = (RdNodeMesh) row;

					node.meshMap.put(obj.rowId(), obj);
					
					node.setMesh(obj.getMeshId());
				}

				RdNodeNameSelector name = new RdNodeNameSelector(conn);
				
				List<IRow> names =name.loadRowsByParentId(id, isLock);
				
				for(IRow row : names){
					row.setMesh(node.mesh());
				}
				
				node.setNames(names);
				
				for (IRow row : node.getNames()) {
					RdNodeName obj = (RdNodeName) row;

					node.nameMap.put(obj.rowId(), obj);
				}

				RdNodeFormSelector form = new RdNodeFormSelector(conn);
				
				List<IRow> forms =form.loadRowsByParentId(id, isLock);
				
				for(IRow row : forms){
					row.setMesh(node.mesh());
				}

				node.setForms(forms);
				
				for (IRow row : node.getForms()) {
					RdNodeForm obj = (RdNodeForm) row;

					node.formMap.put(obj.rowId(), obj);
				}

			} else {
				
				throw new DataNotFoundException("数据不存在");
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

		return node;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock)
			throws Exception {

		return null;
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {

		return null;
	}

	// 加载盲端节点
	public List<RdNode> loadEndRdNodeByLinkPid(int linkPid, boolean isLock)
			throws Exception {

		List<RdNode> nodes = new ArrayList<RdNode>();

		String sql = "select a.*   from rd_node a  where a.node_pid in   "
				+ " ( select d.node_pid   from (select a.node_pid, count(1) count   from rd_link c, rd_node a  where (c.e_node_pid = a.node_pid or c.s_node_pid = a.node_pid) and c.u_record!=2 and exists (select null  from rd_link b      "
				+ "where (a.node_pid = b.e_node_pid or   a.node_pid = b.s_node_pid)   and b.link_pid = :1)  group by a.node_pid) d  where d.count = 1)";

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
				
				RdNodeMeshSelector mesh = new RdNodeMeshSelector(conn);

				node.setMeshes(mesh.loadRowsByParentId(node.getPid(),
						isLock));
				
				for(IRow row : node.getMeshes()){
					
					node.setMesh(row.mesh());
				}

				RdNodeNameSelector name = new RdNodeNameSelector(conn);
				
				List<IRow> names = name.loadRowsByParentId(node.getPid(), isLock);
				
				for(IRow row : names){
					row.setMesh(node.mesh());
				}
				
				node.setNames(names);

				RdNodeFormSelector form = new RdNodeFormSelector(conn);
				
				List<IRow> forms = form.loadRowsByParentId(node.getPid(), isLock);
				
				for(IRow row : forms){
					row.setMesh(node.mesh());
				}

				node.setForms(forms);

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
				
				RdNodeMeshSelector mesh = new RdNodeMeshSelector(conn);

				node.setMeshes(mesh.loadRowsByParentId(node.getPid(),
						isLock));
				
				for(IRow row : node.getMeshes()){
					
					node.setMesh(row.mesh());
				}

				RdNodeNameSelector name = new RdNodeNameSelector(conn);
				
				List<IRow> names = name.loadRowsByParentId(node.getPid(), isLock);
				
				for(IRow row : names){
					row.setMesh(node.mesh());
				}
				
				node.setNames(names);

				RdNodeFormSelector form = new RdNodeFormSelector(conn);
				
				List<IRow> forms = form.loadRowsByParentId(node.getPid(), isLock);
				
				for(IRow row : forms){
					row.setMesh(node.mesh());
				}

				node.setForms(forms);

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

}
