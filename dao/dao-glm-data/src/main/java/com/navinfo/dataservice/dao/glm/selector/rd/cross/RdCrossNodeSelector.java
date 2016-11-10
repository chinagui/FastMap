package com.navinfo.dataservice.dao.glm.selector.rd.cross;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.log4j.Logger;

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
}
