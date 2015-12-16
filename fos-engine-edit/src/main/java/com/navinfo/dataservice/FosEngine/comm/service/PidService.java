package com.navinfo.dataservice.FosEngine.comm.service;

import java.sql.Connection;
import java.sql.SQLException;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.FosEngine.comm.constant.PropConstant;
import com.navinfo.dataservice.FosEngine.comm.db.ConfigLoader;
import com.navinfo.dataservice.FosEngine.comm.db.DBOraclePool;
import com.navinfo.dataservice.FosEngine.comm.exception.DataNotFoundException;
import com.navinfo.dataservice.FosEngine.comm.util.PidServiceUtils;

public class PidService {

	private static Logger logger = Logger.getLogger(PidService.class);

	public static class PidRangeCombine {

		private int pid;

		private String pidRange;

		public int getPid() {
			return pid;
		}

		public void setPid(int pid) {
			this.pid = pid;
		}

		public String getPidRange() {
			return pidRange;
		}

		public void setPidRange(String pidRange) {
			this.pidRange = pidRange;
		}

	}

	/**
	 * 採用單例模式來保證PID唯一性和安全性
	 */
	private static PidService pidService = null;

	private DBOraclePool pool;

	private PidService() throws Exception {
		JSONObject config = ConfigLoader.getConfig();

		JSONObject jsonConnMsg = new JSONObject();

		jsonConnMsg.put("ip", config.getString(PropConstant.pidDbIp));

		jsonConnMsg.put("port", config.getInt(PropConstant.pidDbPort));

		jsonConnMsg.put("serviceName",
				config.getString(PropConstant.pidDbServiceName));

		jsonConnMsg.put("username",
				config.getString(PropConstant.pidDbUsername));

		jsonConnMsg.put("password",
				config.getString(PropConstant.pidDbPassword));

		pool = new DBOraclePool(jsonConnMsg);

	}

	public synchronized static PidService getInstance() throws Exception {
		if (pidService == null) {
			pidService = new PidService();
		}
		return pidService;
	}

	/**
	 * 申請link_pid
	 */
	public synchronized int applyLinkPid() throws Exception {

		Connection conn = null;

		int pid = 0;
		try {
			conn = pool.getConnection();

			conn.setAutoCommit(false);

			String pidRange = PidServiceUtils.getPidRange(conn,
					PidSequenceName.linkPidName);

			if (pidRange != null) {
				PidRangeCombine prc = PidServiceUtils.applyPid(pidRange);

				if (prc.getPid() != -1) {
					PidServiceUtils.updatePidRange(conn,
							PidSequenceName.linkPidName, prc.getPidRange());

					pid = prc.getPid();
				} else {
					// 剩餘範圍不足,需要從ID分配器搬運新的PID
					pid = PidServiceUtils.transportPid(conn, 5000,
							PidSequenceName.linkPidName);
				}
			} else {
				// 不存在對應的序列,報錯且拋出異常

				throw new DataNotFoundException(null);
			}

		} catch (Exception e) {

			throw e;

		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
			}
		}

		return pid;

	}

	/**
	 * 申請node_pid
	 */
	public synchronized int applyNodePid() throws Exception {

		Connection conn = null;

		int pid = 0;
		try {
			conn = pool.getConnection();

			conn.setAutoCommit(false);

			String pidRange = PidServiceUtils.getPidRange(conn,
					PidSequenceName.nodePidName);

			if (pidRange != null) {
				PidRangeCombine prc = PidServiceUtils.applyPid(pidRange);

				if (prc.getPid() != -1) {
					PidServiceUtils.updatePidRange(conn,
							PidSequenceName.nodePidName, prc.getPidRange());

					pid = prc.getPid();
				} else {
					// 剩餘範圍不足,需要從ID分配器搬運新的PID
					pid = PidServiceUtils.transportPid(conn, 5000,
							PidSequenceName.nodePidName);
				}
			} else {
				// 不存在對應的序列,報錯且拋出異常
				throw new DataNotFoundException(null);
			}

		} catch (Exception e) {

			throw e;

		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
			}
		}

		return pid;

	}

	/**
	 * 申請rd_restriciton pid
	 */
	public synchronized int applyRestrictionPid() throws Exception {

		Connection conn = null;

		int pid = 0;
		try {
			conn = pool.getConnection();

			conn.setAutoCommit(false);

			String pidRange = PidServiceUtils.getPidRange(conn,
					PidSequenceName.restrictPidName);

			if (pidRange != null) {
				PidRangeCombine prc = PidServiceUtils.applyPid(pidRange);

				if (prc.getPid() != -1) {
					PidServiceUtils.updatePidRange(conn,
							PidSequenceName.restrictPidName, prc.getPidRange());

					pid = prc.getPid();
				} else {
					// 剩餘範圍不足,需要從ID分配器搬運新的PID
					pid = PidServiceUtils.transportPid(conn, 5000,
							PidSequenceName.restrictPidName);
				}
			} else {
				// 不存在對應的序列,報錯且拋出異常
				throw new DataNotFoundException(null);
			}

		} catch (Exception e) {

			throw e;

		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
			}
		}

		return pid;

	}
	
	/**
	 * 申請rd_restriction_detail pid
	 */
	public synchronized int applyRestrictionDetailPid() throws Exception {

		Connection conn = null;

		int pid = 0;
		try {
			conn = pool.getConnection();

			conn.setAutoCommit(false);

			String pidRange = PidServiceUtils.getPidRange(conn,
					PidSequenceName.restrictDetailPidName);

			if (pidRange != null) {
				PidRangeCombine prc = PidServiceUtils.applyPid(pidRange);

				if (prc.getPid() != -1) {
					PidServiceUtils.updatePidRange(conn,
							PidSequenceName.restrictDetailPidName, prc.getPidRange());

					pid = prc.getPid();
				} else {
					// 剩餘範圍不足,需要從ID分配器搬運新的PID
					pid = PidServiceUtils.transportPid(conn, 5000,
							PidSequenceName.restrictDetailPidName);
				}
			} else {
				// 不存在對應的序列,報錯且拋出異常
				throw new DataNotFoundException(null);
			}

		} catch (Exception e) {

			throw e;

		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
			}
		}

		return pid;

	}
	
	/**
	 * 申請node_name_id
	 */
	public synchronized int applyNodeNameId() throws Exception {

		Connection conn = null;

		int pid = 0;
		try {
			conn = pool.getConnection();

			conn.setAutoCommit(false);

			String pidRange = PidServiceUtils.getPidRange(conn,
					PidSequenceName.nodeNameIdName);

			if (pidRange != null) {
				PidRangeCombine prc = PidServiceUtils.applyPid(pidRange);

				if (prc.getPid() != -1) {
					PidServiceUtils.updatePidRange(conn,
							PidSequenceName.nodeNameIdName, prc.getPidRange());

					pid = prc.getPid();
				} else {
					// 剩餘範圍不足,需要從ID分配器搬運新的PID
					pid = PidServiceUtils.transportPid(conn, 5000,
							PidSequenceName.nodeNameIdName);
				}
			} else {
				// 不存在對應的序列,報錯且拋出異常
				throw new DataNotFoundException(null);
			}

		} catch (Exception e) {

			throw e;

		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
			}
		}

		return pid;

	}

}
