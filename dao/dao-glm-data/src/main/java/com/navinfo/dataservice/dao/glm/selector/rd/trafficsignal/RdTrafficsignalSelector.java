/**
 *
 */
package com.navinfo.dataservice.dao.glm.selector.rd.trafficsignal;

import com.navinfo.dataservice.dao.glm.model.rd.trafficsignal.RdTrafficsignal;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import org.apache.commons.dbutils.DbUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author Zhang Xiaolong
 * @ClassName: RdTrafficsignalSelector
 * @date 2016年7月20日 下午7:34:21
 * @Description: TODO
 */
public class RdTrafficsignalSelector extends AbstractSelector {

	private Connection conn;

	public RdTrafficsignalSelector(Connection conn) {
		super(conn);
		this.conn = conn;
		this.setCls(RdTrafficsignal.class);
	}

	/**
	 * 根据nodePid查询交通信号等（1:1）
	 *
	 * @param ids
	 * @param isLock
	 * @return
	 * @throws Exception
	 */
	public List<RdTrafficsignal> loadByNodeId(boolean isLock, int... ids) throws Exception {
		List<RdTrafficsignal> rows = new ArrayList<RdTrafficsignal>();

		StringBuilder idParameter = new StringBuilder();

		for (int id : ids) {
			idParameter.append(id + ",");
		}

		idParameter.deleteCharAt(idParameter.lastIndexOf(","));

		StringBuilder sb = new StringBuilder(
				"select * from rd_trafficsignal where node_pid in(" + idParameter.toString() + ") and u_record !=2");

		if (isLock) {
			sb.append(" for update nowait");
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				RdTrafficsignal rdTrafficsignal = new RdTrafficsignal();
				ReflectionAttrUtils.executeResultSet(rdTrafficsignal, resultSet);
				rows.add(rdTrafficsignal);
			}
		} catch (Exception e) {

			throw e;

		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}

		return rows;
	}

	/**
	 * 根据LinkPid查询交通信号灯（1:1）
	 *
	 * @param ids
	 * @param isLock
	 * @return
	 * @throws Exception
	 */
	public List<RdTrafficsignal> loadByLinkPid(boolean isLock, Integer... ids) throws Exception {

		List<RdTrafficsignal> rows = new ArrayList<RdTrafficsignal>();

		StringBuilder idParameter = new StringBuilder();

		for (int id : ids) {
			idParameter.append(id + ",");
		}

		idParameter.deleteCharAt(idParameter.lastIndexOf(","));

		StringBuilder sb = new StringBuilder(
				"select * from rd_trafficsignal where link_pid in (" + idParameter.toString() + ") and u_record !=2");

		if (isLock) {
			sb.append(" for update nowait");
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				RdTrafficsignal rdTrafficsignal = new RdTrafficsignal();
				ReflectionAttrUtils.executeResultSet(rdTrafficsignal, resultSet);
				rows.add(rdTrafficsignal);
			}
		} catch (Exception e) {

			throw e;

		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}

		return rows;
	}

	/**
	 * 根据一个信号灯pid查询路口处所有信号灯（包括复合路口）
	 *
	 * @param pid
	 * @param isLock
	 * @return
	 * @throws Exception
	 */
	public List<RdTrafficsignal> loadByTrafficPid(boolean isLock, int pid) throws Exception {
		List<RdTrafficsignal> rows = new ArrayList<RdTrafficsignal>();

		StringBuilder sb = new StringBuilder(
				"WITH tmp1 AS ( SELECT a.pid FROM rd_cross_node a,RD_TRAFFICSIGNAL b WHERE b.pid = :1 AND a.node_pid = b.node_pid GROUP BY a.pid ), tmp2 AS ( SELECT a.node_pid FROM rd_cross_node a,tmp1 WHERE a.pid = tmp1.pid GROUP BY a.node_pid ) SELECT a.* FROM RD_TRAFFICSIGNAL a,tmp2 WHERE a.node_pid = tmp2.node_pid");

//		if (isLock) {
//			sb.append(" for update nowait");
//		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setInt(1, pid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				RdTrafficsignal rdTrafficsignal = new RdTrafficsignal();
				ReflectionAttrUtils.executeResultSet(rdTrafficsignal, resultSet);
				rows.add(rdTrafficsignal);
			}
		} catch (Exception e) {

			throw e;

		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}

		return rows;
	}

	/**
	 * 根据nodePid查询交通信号
	 *
	 * @param nodePids
	 * @param isLock
	 * @return
	 * @throws Exception
	 */
	public List<RdTrafficsignal> loadByNodePids(Collection<Integer> nodePids, boolean isLock) throws Exception {
		List<RdTrafficsignal> rows = new ArrayList<RdTrafficsignal>();
		if (nodePids.isEmpty())
			return rows;
		Iterator<Integer> it = nodePids.iterator();
		StringBuffer buffer = new StringBuffer();
		while (it.hasNext()) {
			buffer.append(it.next()).append(",");
		}
		String inter = "''";
		if (buffer.length() > 0)
			inter = buffer.substring(1, buffer.length() - 1);
		StringBuilder sb = new StringBuilder(
				"select * from rd_trafficsignal where node_pid in(" + inter + ") and u_record !=2");
		if (isLock) {
			sb.append(" for update nowait");
		}
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			pstmt = conn.prepareStatement(sb.toString());
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				RdTrafficsignal rdTrafficsignal = new RdTrafficsignal();
				ReflectionAttrUtils.executeResultSet(rdTrafficsignal, resultSet);
				rows.add(rdTrafficsignal);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
		return rows;
	}
}
