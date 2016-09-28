/**
 * 
 */
package com.navinfo.dataservice.dao.glm.selector.rd.trafficsignal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.dao.glm.model.rd.trafficsignal.RdTrafficsignal;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;

/**
 * @ClassName: RdTrafficsignalSelector
 * @author Zhang Xiaolong
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
	 * @param id
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
				"select * from rd_trafficsignal where node_pid in("+idParameter.toString()+") and u_record !=2");

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
	 * @param id
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
				"select * from rd_trafficsignal where link_pid in ("+idParameter.toString()+") and u_record !=2");

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
	 * 根据一个信号灯pid查询路口处所有信号灯
	 * 
	 * @param id
	 * @param isLock
	 * @return
	 * @throws Exception
	 */
	public List<RdTrafficsignal> loadByTrafficPid(boolean isLock,int pid) throws Exception {
		List<RdTrafficsignal> rows = new ArrayList<RdTrafficsignal>();
		
		StringBuilder sb = new StringBuilder(
				"select * from rd_trafficsignal where node_pid in(select node_pid from rd_trafficsignal where pid = :1) and u_record !=2");

		if (isLock) {
			sb.append(" for update nowait");
		}


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
}
