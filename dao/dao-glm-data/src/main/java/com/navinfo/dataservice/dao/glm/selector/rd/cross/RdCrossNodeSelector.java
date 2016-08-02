package com.navinfo.dataservice.dao.glm.selector.rd.cross;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.exception.DataNotFoundException;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossNode;

public class RdCrossNodeSelector implements ISelector {

	private static Logger logger = Logger.getLogger(RdCrossNodeSelector.class);

	private Connection conn;

	public RdCrossNodeSelector(Connection conn) {
		this.conn = conn;

	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {

		RdCrossNode node = new RdCrossNode();

		String sql = "select * from " + node.tableName() + " where row_id=(:1)";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, rowId);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {

				node.setNodePid(resultSet.getInt("node_pid"));

				node.setPid(resultSet.getInt("pid"));

				node.setIsMain(resultSet.getInt("is_main"));

				node.setRowId(resultSet.getString("row_id"));
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

		List<IRow> rows = new ArrayList<IRow>();

		String sql = "select * from rd_cross_node where pid=:1 and u_record!=:2";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, id);

			pstmt.setInt(2, 2);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				RdCrossNode node = new RdCrossNode();

				node.setNodePid(resultSet.getInt("node_pid"));

				node.setPid(resultSet.getInt("pid"));

				node.setIsMain(resultSet.getInt("is_main"));

				node.setRowId(resultSet.getString("row_id"));

				rows.add(node);
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

		return rows;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {

		return null;
	}
	
	/**
	 * 根据nodePid查询路口
	 * @param nodePid
	 * @param isLock
	 * @return
	 * @throws Exception
	 */
	public IRow loadByNodeId(int nodePid,boolean isLock) throws Exception
	{
		RdCrossNode node = new RdCrossNode();

		String sql = "select * from " + node.tableName() + " where node_pid=(:1) and u_record !=2 ";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, nodePid);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {

				node.setNodePid(resultSet.getInt("node_pid"));

				node.setPid(resultSet.getInt("pid"));

				node.setIsMain(resultSet.getInt("is_main"));

				node.setRowId(resultSet.getString("row_id"));
			} else {
				throw new DataNotFoundException("Node:"+nodePid+" 不是路口组成点");
			}
		} catch (Exception e) {
			logger.error("根据nodePid："+nodePid+" 查询路口出错");
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
}
