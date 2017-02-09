package com.navinfo.dataservice.dao.glm.selector.rd.cross;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.exception.DataNotFoundException;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossNode;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.navicommons.database.sql.DBUtils;

public class RdCrossNodeSelector extends AbstractSelector {

	private static Logger logger = Logger.getLogger(RdCrossNodeSelector.class);

	private Connection conn;

	public RdCrossNodeSelector(Connection conn) {
		super(conn);
		this.conn = conn;
		this.setCls(RdCrossNode.class);
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
		RdCrossNode node = null;

		String sql = "select * from rd_cross_node where node_pid=(:1) and u_record !=2 ";

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
				node = new RdCrossNode();
				
				ReflectionAttrUtils.executeResultSet(node, resultSet);
			}
		} catch (Exception e) {
			logger.error("根据nodePid："+nodePid+" 查询路口出错");
			throw e;

		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return node;
	}	
	
	/**
	 * 根据node所关联路口组成的所有nodePid
	 * @param nodePid
	 * @param isLock
	 * @return
	 * @throws Exception
	 */
	public List<Integer> getCrossNodePidByNode(int nodePid)
			throws Exception {

		List<Integer> nodePids = new ArrayList<Integer>();

		String sql = "SELECT N2.NODE_PID FROM RD_CROSS_NODE N1, RD_CROSS_NODE N2, RD_CROSS C WHERE N1.NODE_PID = :1 AND N1.PID = C.PID AND N2.PID = C.PID AND N1.U_RECORD <> 2 AND N2.U_RECORD <> 2 AND C.U_RECORD <> 2";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {

			pstmt = this.conn.prepareStatement(sql);

			pstmt.setInt(1, nodePid);

			resultSet = pstmt.executeQuery();
			
			
			while (resultSet.next()) {

				nodePids.add(resultSet.getInt("NODE_PID"));
			}
		} catch (Exception e) {

			throw e;

		} finally {

			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);
		}

		return nodePids;
	}
}
