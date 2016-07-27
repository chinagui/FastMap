/**
 * 
 */
package com.navinfo.dataservice.dao.glm.selector.rd.trafficsignal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.rd.trafficsignal.RdTrafficsignal;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;

/** 
* @ClassName: RdTrafficsignalSelector 
* @author Zhang Xiaolong
* @date 2016年7月20日 下午7:34:21 
* @Description: TODO
*/
public class RdTrafficsignalSelector implements ISelector {

	private Connection conn;

	public RdTrafficsignalSelector(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {
		RdTrafficsignal rdTrafficsignal = new RdTrafficsignal();

		StringBuilder sb = new StringBuilder(
				"select * from " + rdTrafficsignal.tableName() + " where pid = :1 and u_record !=2");

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
				ReflectionAttrUtils.executeResultSet(rdTrafficsignal, resultSet);
			}
		} catch (Exception e) {

			throw e;

		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}

		return rdTrafficsignal;
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {
		return null;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock) throws Exception {
		return null;
	}
	
	/**
	 * 根据nodePid查询交通信号等（1:1）
	 * @param id
	 * @param isLock
	 * @return
	 * @throws Exception
	 */
	public IRow loadByNodeId(int id, boolean isLock) throws Exception {
		RdTrafficsignal rdTrafficsignal = new RdTrafficsignal();

		StringBuilder sb = new StringBuilder(
				"select * from " + rdTrafficsignal.tableName() + " where node_pid = :1 and u_record !=2");

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
				ReflectionAttrUtils.executeResultSet(rdTrafficsignal, resultSet);
			}
			else
			{
				return null;
			}
		} catch (Exception e) {

			throw e;

		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}

		return rdTrafficsignal;
	}
	
	/**
	 * 根据LinkPid查询交通信号灯（1:1）
	 * @param id
	 * @param isLock
	 * @return
	 * @throws Exception
	 */
	public RdTrafficsignal loadByLinkPid(int id, boolean isLock) throws Exception {
		RdTrafficsignal rdTrafficsignal = new RdTrafficsignal();

		StringBuilder sb = new StringBuilder(
				"select * from " + rdTrafficsignal.tableName() + " where link_pid = :1 and u_record !=2");

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
				ReflectionAttrUtils.executeResultSet(rdTrafficsignal, resultSet);
			}
			else
			{
				return null;
			}
		} catch (Exception e) {

			throw e;

		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}

		return rdTrafficsignal;
	}
}
