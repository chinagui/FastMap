/**
 * 
 */
package com.navinfo.dataservice.dao.glm.selector.rd.variablespeed;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.variablespeed.RdVariableSpeed;
import com.navinfo.dataservice.dao.glm.model.rd.variablespeed.RdVariableSpeedVia;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.navicommons.database.sql.DBUtils;

/**
 * @ClassName: RdVariableSpeedSelector
 * @author Zhang Xiaolong
 * @date 2016年8月15日 下午9:13:52
 * @Description: TODO
 */
public class RdVariableSpeedSelector extends AbstractSelector {

	/**
	 * @param cls
	 * @param conn
	 */
	public RdVariableSpeedSelector(Connection conn) {
		super(RdVariableSpeed.class, conn);
	}

	/**
	 * 根据进入线、进入点、退出现查询可变限速
	 * 
	 * @param inLinkPid
	 * @param nodePid
	 * @param outLinkPid
	 * @param isLock
	 * @return
	 * @throws Exception
	 */
	public RdVariableSpeed loadByInLinkNodeOutLinkPid(int inLinkPid, int nodePid, int outLinkPid, boolean isLock)
			throws Exception {
		RdVariableSpeed rdVariableSpeed = null;
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			StringBuilder sb = new StringBuilder(
					"select * from RD_VARIABLE_SPEED where in_link_pid =:1 and node_pid = :2 and out_link_pid = :3 and u_record !=2");
			if (isLock) {
				sb.append(" for update nowait");
			}
			pstmt = getConn().prepareStatement(sb.toString());

			pstmt.setInt(1, inLinkPid);

			pstmt.setInt(2, nodePid);

			pstmt.setInt(3, outLinkPid);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {
				rdVariableSpeed = new RdVariableSpeed();
				ReflectionAttrUtils.executeResultSet(rdVariableSpeed, resultSet);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return rdVariableSpeed;
	}

	/**
	 * 根据进入线或者进入点或者退出线查询可变限速
	 * 
	 * @param inLinkPid
	 *            进入线pid
	 * @param nodePid
	 *            进入点pid
	 * @param outLinkPid
	 *            退出线pid
	 * @param isLock
	 *            是否加锁
	 * @return 可变限速集合
	 * @throws Exception
	 */
	public List<RdVariableSpeed> loadRdVariableSpeedByParam(Integer inLinkPid, Integer nodePid, Integer outLinkPid,
			boolean isLock) throws Exception {
		List<RdVariableSpeed> rdVariableSpeeds = new ArrayList<>();
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			StringBuilder sb = new StringBuilder("select * from RD_VARIABLE_SPEED where u_record !=2");

			if (inLinkPid != null) {
				sb.append(" and in_link_pid =" + inLinkPid);
			}
			if (nodePid != null) {
				sb.append(" and node_pid =" + nodePid);
			}
			if (outLinkPid != null) {
				sb.append(" and out_link_pid =" + outLinkPid);
			}
			if (isLock) {
				sb.append(" for update nowait");
			}
			pstmt = getConn().prepareStatement(sb.toString());

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				RdVariableSpeed rdVariableSpeed = new RdVariableSpeed();
				ReflectionAttrUtils.executeResultSet(rdVariableSpeed, resultSet);
				List<IRow> vias = this.loadRowsByClassParentId(RdVariableSpeedVia.class, rdVariableSpeed.getPid(),
						isLock, "seq_num");
				rdVariableSpeed.setVias(vias);
				rdVariableSpeeds.add(rdVariableSpeed);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return rdVariableSpeeds;
	}

	/**
	 * 根据进入线或者进入点或者退出线查询可变限速
	 * 
	 * @param inLinkPid
	 *            进入线pid
	 * @param nodePid
	 *            进入点pid
	 * @param outLinkPid
	 *            退出线pid
	 * @param isLock
	 *            是否加锁
	 * @return 可变限速集合
	 * @throws Exception
	 */
	public List<RdVariableSpeed> loadRdVariableSpeedByLinkPid(int linkPid, boolean isLock) throws Exception {
		List<RdVariableSpeed> rdVariableSpeeds = new ArrayList<>();
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			StringBuilder sb = new StringBuilder("select * from RD_VARIABLE_SPEED where u_record !=2 and (in_link_pid = :1 or out_link_pid = :2)");
			if (isLock) {
				sb.append(" for update nowait");
			}
			pstmt = getConn().prepareStatement(sb.toString());
			
			pstmt.setInt(1, linkPid);
			
			pstmt.setInt(2, linkPid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				RdVariableSpeed rdVariableSpeed = new RdVariableSpeed();
				ReflectionAttrUtils.executeResultSet(rdVariableSpeed, resultSet);
				rdVariableSpeeds.add(rdVariableSpeed);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return rdVariableSpeeds;
	}

	/**
	 * 根据接续线查询可变限速对象
	 * 
	 * @param viaLinkPid
	 * @param isLock
	 * @return
	 * @throws Exception
	 */
	public List<RdVariableSpeed> loadRdVariableSpeedByViaLinkPid(Integer viaLinkPid, boolean isLock) throws Exception {
		List<RdVariableSpeed> rdVariableSpeeds = new ArrayList<>();
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			StringBuilder sb = new StringBuilder(
					"select a.* from rd_variable_speed a,rd_variable_speed_via b where a.VSPEED_PID = b.VSPEED_PID ");

			if (viaLinkPid != null) {
				sb.append(" and b.link_pid =" + viaLinkPid + " and a.u_record !=2 and b.u_record !=2");
			}
			if (isLock) {
				sb.append(" for update nowait");
			}
			pstmt = getConn().prepareStatement(sb.toString());

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				RdVariableSpeed rdVariableSpeed = new RdVariableSpeed();
				ReflectionAttrUtils.executeResultSet(rdVariableSpeed, resultSet);
				List<IRow> vias = this.loadRowsByClassParentId(RdVariableSpeedVia.class, rdVariableSpeed.getPid(),
						isLock, "seq_num");
				rdVariableSpeed.setVias(vias);
				rdVariableSpeeds.add(rdVariableSpeed);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return rdVariableSpeeds;
	}
	
	/**
	 * 根据接续线查询接续线对象
	 * 
	 * @param viaLinkPid
	 * @param isLock
	 * @return
	 * @throws Exception
	 */
	public List<RdVariableSpeedVia> loadRdVariableSpeedVia(Integer viaLinkPid, boolean isLock) throws Exception {
		List<RdVariableSpeedVia> rdVariableSpeeds = new ArrayList<>();
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			StringBuilder sb = new StringBuilder(
					"select * from rd_variable_speed_via where link_pid = :1 and u_record !=2");
			if (isLock) {
				sb.append(" for update nowait");
			}
			pstmt = getConn().prepareStatement(sb.toString());
			
			pstmt.setInt(1, viaLinkPid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				RdVariableSpeedVia rdVariableSpeedVia = new RdVariableSpeedVia();
				ReflectionAttrUtils.executeResultSet(rdVariableSpeedVia, resultSet);
				rdVariableSpeeds.add(rdVariableSpeedVia);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return rdVariableSpeeds;
	}
}
