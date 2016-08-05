/**
 * 
 */
package com.navinfo.dataservice.dao.glm.selector.rd.crf;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInter;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.navicommons.database.sql.DBUtils;

/**
 * @ClassName: RdInterSelector
 * @author Zhang Xiaolong
 * @date 2016年8月5日 下午2:11:13
 * @Description: TODO
 */
public class RdInterSelector extends AbstractSelector {

	private Connection conn = null;

	/**
	 * @param cls
	 * @param conn
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public RdInterSelector(Connection conn) {
		super(conn);
		this.setCls(RdInter.class);
		this.conn = conn;
	}

	/**
	 * 更具nodepid查询crf交叉点pid
	 * @param nodePidsStr
	 * @param isLock
	 * @return
	 * @throws Exception
	 */
	public List<Integer> loadInterPidByNodePid(String nodePidsStr, boolean isLock) throws Exception {
		List<Integer> interPidList = new ArrayList<>();
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			StringBuilder sb = new StringBuilder(
					"select pid from rd_inter_node where node_pid in(" + nodePidsStr + ") and u_record !=2");
			if (isLock) {
				sb.append(" for update nowait");
			}
			pstmt = conn.prepareStatement(sb.toString());

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				interPidList.add(resultSet.getInt("pid"));
			}
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return interPidList;
	}
}
