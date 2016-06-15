package com.navinfo.dataservice.dao.pidservice;

import java.sql.Connection;
import java.sql.SQLException;

public class PidService {

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
			conn = PidServicePool.getInstance().getConnection();

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

				pid = PidServiceUtils.transportPid(conn, 5000,
						PidSequenceName.linkPidName);
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
			conn = PidServicePool.getInstance().getConnection();

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
				pid = PidServiceUtils.transportPid(conn, 5000,
						PidSequenceName.nodePidName);
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
			conn = PidServicePool.getInstance().getConnection();

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
				pid = PidServiceUtils.transportPid(conn, 5000,
						PidSequenceName.restrictPidName);
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
			conn = PidServicePool.getInstance().getConnection();

			conn.setAutoCommit(false);

			String pidRange = PidServiceUtils.getPidRange(conn,
					PidSequenceName.restrictDetailPidName);

			if (pidRange != null) {
				PidRangeCombine prc = PidServiceUtils.applyPid(pidRange);

				if (prc.getPid() != -1) {
					PidServiceUtils.updatePidRange(conn,
							PidSequenceName.restrictDetailPidName,
							prc.getPidRange());

					pid = prc.getPid();
				} else {
					// 剩餘範圍不足,需要從ID分配器搬運新的PID
					pid = PidServiceUtils.transportPid(conn, 5000,
							PidSequenceName.restrictDetailPidName);
				}
			} else {
				// 不存在對應的序列,報錯且拋出異常
				pid = PidServiceUtils.transportPid(conn, 5000,
						PidSequenceName.restrictDetailPidName);
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
			conn = PidServicePool.getInstance().getConnection();

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
				pid = PidServiceUtils.transportPid(conn, 5000,
						PidSequenceName.nodeNameIdName);
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
	 * 申請rdspeedlimit pid
	 */
	public synchronized int applySpeedLimitPid() throws Exception {

		Connection conn = null;

		int pid = 0;
		try {
			conn = PidServicePool.getInstance().getConnection();

			conn.setAutoCommit(false);

			String pidRange = PidServiceUtils.getPidRange(conn,
					PidSequenceName.speedLimitPidName);

			if (pidRange != null) {
				PidRangeCombine prc = PidServiceUtils.applyPid(pidRange);

				if (prc.getPid() != -1) {
					PidServiceUtils.updatePidRange(conn,
							PidSequenceName.speedLimitPidName,
							prc.getPidRange());

					pid = prc.getPid();
				} else {
					// 剩餘範圍不足,需要從ID分配器搬運新的PID
					pid = PidServiceUtils.transportPid(conn, 5000,
							PidSequenceName.speedLimitPidName);
				}
			} else {
				// 不存在對應的序列,報錯且拋出異常
				pid = PidServiceUtils.transportPid(conn, 5000,
						PidSequenceName.speedLimitPidName);
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

	public synchronized int applyLaneConnexityPid() throws Exception {

		Connection conn = null;

		int pid = 0;
		try {
			conn = PidServicePool.getInstance().getConnection();

			conn.setAutoCommit(false);

			String pidRange = PidServiceUtils.getPidRange(conn,
					PidSequenceName.laneConnexityPidName);

			if (pidRange != null) {
				PidRangeCombine prc = PidServiceUtils.applyPid(pidRange);

				if (prc.getPid() != -1) {
					PidServiceUtils.updatePidRange(conn,
							PidSequenceName.laneConnexityPidName,
							prc.getPidRange());

					pid = prc.getPid();
				} else {
					// 剩餘範圍不足,需要從ID分配器搬運新的PID
					pid = PidServiceUtils.transportPid(conn, 5000,
							PidSequenceName.laneConnexityPidName);
				}
			} else {
				// 不存在對應的序列,報錯且拋出異常
				pid = PidServiceUtils.transportPid(conn, 5000,
						PidSequenceName.laneConnexityPidName);
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

	public synchronized int applyLaneTopologyPid() throws Exception {

		Connection conn = null;

		int pid = 0;
		try {
			conn = PidServicePool.getInstance().getConnection();

			conn.setAutoCommit(false);

			String pidRange = PidServiceUtils.getPidRange(conn,
					PidSequenceName.laneTopologyPidName);

			if (pidRange != null) {
				PidRangeCombine prc = PidServiceUtils.applyPid(pidRange);

				if (prc.getPid() != -1) {
					PidServiceUtils.updatePidRange(conn,
							PidSequenceName.laneTopologyPidName,
							prc.getPidRange());

					pid = prc.getPid();
				} else {
					// 剩餘範圍不足,需要從ID分配器搬運新的PID
					pid = PidServiceUtils.transportPid(conn, 5000,
							PidSequenceName.laneTopologyPidName);
				}
			} else {
				// 不存在對應的序列,報錯且拋出異常
				pid = PidServiceUtils.transportPid(conn, 5000,
						PidSequenceName.laneTopologyPidName);
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

	public synchronized int applyRdCrossPid() throws Exception {

		Connection conn = null;

		int pid = 0;
		try {
			conn = PidServicePool.getInstance().getConnection();

			conn.setAutoCommit(false);

			String pidRange = PidServiceUtils.getPidRange(conn,
					PidSequenceName.crossPidName);

			if (pidRange != null) {
				PidRangeCombine prc = PidServiceUtils.applyPid(pidRange);

				if (prc.getPid() != -1) {
					PidServiceUtils.updatePidRange(conn,
							PidSequenceName.crossPidName, prc.getPidRange());

					pid = prc.getPid();
				} else {
					// 剩餘範圍不足,需要從ID分配器搬運新的PID
					pid = PidServiceUtils.transportPid(conn, 5000,
							PidSequenceName.crossPidName);
				}
			} else {
				// 不存在對應的序列,報錯且拋出異常
				pid = PidServiceUtils.transportPid(conn, 5000,
						PidSequenceName.crossPidName);
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

	public synchronized int applyRdCrossNameId() throws Exception {

		Connection conn = null;

		int pid = 0;
		try {
			conn = PidServicePool.getInstance().getConnection();

			conn.setAutoCommit(false);

			String pidRange = PidServiceUtils.getPidRange(conn,
					PidSequenceName.crossNameIdName);

			if (pidRange != null) {
				PidRangeCombine prc = PidServiceUtils.applyPid(pidRange);

				if (prc.getPid() != -1) {
					PidServiceUtils.updatePidRange(conn,
							PidSequenceName.crossNameIdName, prc.getPidRange());

					pid = prc.getPid();
				} else {
					// 剩餘範圍不足,需要從ID分配器搬運新的PID
					pid = PidServiceUtils.transportPid(conn, 5000,
							PidSequenceName.crossNameIdName);
				}
			} else {
				// 不存在對應的序列,報錯且拋出異常
				pid = PidServiceUtils.transportPid(conn, 5000,
						PidSequenceName.crossNameIdName);
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

	public synchronized int applyBranchPid() throws Exception {

		Connection conn = null;

		int pid = 0;
		try {
			conn = PidServicePool.getInstance().getConnection();

			conn.setAutoCommit(false);

			String pidRange = PidServiceUtils.getPidRange(conn,
					PidSequenceName.branchPidName);

			if (pidRange != null) {
				PidRangeCombine prc = PidServiceUtils.applyPid(pidRange);

				if (prc.getPid() != -1) {
					PidServiceUtils.updatePidRange(conn,
							PidSequenceName.branchPidName, prc.getPidRange());

					pid = prc.getPid();
				} else {
					// 剩餘範圍不足,需要從ID分配器搬運新的PID
					pid = PidServiceUtils.transportPid(conn, 5000,
							PidSequenceName.branchPidName);
				}
			} else {
				// 不存在對應的序列,報錯且拋出異常
				pid = PidServiceUtils.transportPid(conn, 5000,
						PidSequenceName.branchPidName);
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

	public synchronized int applyBranchDetailId() throws Exception {

		Connection conn = null;

		int pid = 0;
		try {
			conn = PidServicePool.getInstance().getConnection();

			conn.setAutoCommit(false);

			String pidRange = PidServiceUtils.getPidRange(conn,
					PidSequenceName.branchDetailIdName);

			if (pidRange != null) {
				PidRangeCombine prc = PidServiceUtils.applyPid(pidRange);

				if (prc.getPid() != -1) {
					PidServiceUtils.updatePidRange(conn,
							PidSequenceName.branchDetailIdName,
							prc.getPidRange());

					pid = prc.getPid();
				} else {
					// 剩餘範圍不足,需要從ID分配器搬運新的PID
					pid = PidServiceUtils.transportPid(conn, 5000,
							PidSequenceName.branchDetailIdName);
				}
			} else {
				// 不存在對應的序列,報錯且拋出異常
				pid = PidServiceUtils.transportPid(conn, 5000,
						PidSequenceName.branchDetailIdName);
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

	public synchronized int applyBranchNameId() throws Exception {

		Connection conn = null;

		int pid = 0;
		try {
			conn = PidServicePool.getInstance().getConnection();

			conn.setAutoCommit(false);

			String pidRange = PidServiceUtils.getPidRange(conn,
					PidSequenceName.branchNameIdName);

			if (pidRange != null) {
				PidRangeCombine prc = PidServiceUtils.applyPid(pidRange);

				if (prc.getPid() != -1) {
					PidServiceUtils
							.updatePidRange(conn,
									PidSequenceName.branchNameIdName,
									prc.getPidRange());

					pid = prc.getPid();
				} else {
					// 剩餘範圍不足,需要從ID分配器搬運新的PID
					pid = PidServiceUtils.transportPid(conn, 5000,
							PidSequenceName.branchNameIdName);
				}
			} else {
				// 不存在對應的序列,報錯且拋出異常
				pid = PidServiceUtils.transportPid(conn, 5000,
						PidSequenceName.branchNameIdName);
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

	public synchronized int applyCkExceptionId() throws Exception {

		Connection conn = null;

		int pid = 0;
		try {
			conn = PidServicePool.getInstance().getConnection();

			conn.setAutoCommit(false);

			String pidRange = PidServiceUtils.getPidRange(conn,
					PidSequenceName.ckExceptionIdName);

			if (pidRange != null) {
				PidRangeCombine prc = PidServiceUtils.applyPid(pidRange);

				if (prc.getPid() != -1) {
					PidServiceUtils.updatePidRange(conn,
							PidSequenceName.ckExceptionIdName,
							prc.getPidRange());

					pid = prc.getPid();
				} else {
					// 剩餘範圍不足,需要從ID分配器搬運新的PID
					pid = PidServiceUtils.transportPid(conn, 5000,
							PidSequenceName.ckExceptionIdName);
				}
			} else {
				// 不存在對應的序列,報錯且拋出異常
				pid = PidServiceUtils.transportPid(conn, 5000,
						PidSequenceName.ckExceptionIdName);
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
	 * 申请ad_admin_pid
	 */
	public synchronized int applyAdAdminPid() throws Exception {

		Connection conn = null;

		int pid = 0;
		try {
			conn = PidServicePool.getInstance().getConnection();

			conn.setAutoCommit(false);

			String pidRange = PidServiceUtils.getPidRange(conn,
					PidSequenceName.adAdminPidName);

			if (pidRange != null) {
				PidRangeCombine prc = PidServiceUtils.applyPid(pidRange);

				if (prc.getPid() != -1) {
					PidServiceUtils.updatePidRange(conn,
							PidSequenceName.adAdminPidName, prc.getPidRange());

					pid = prc.getPid();
				} else {
					// 剩餘範圍不足,需要從ID分配器搬運新的PID
					pid = PidServiceUtils.transportPid(conn, 5000,
							PidSequenceName.adAdminPidName);
				}
			} else {
				// 不存在對應的序列,報錯且拋出異常

				pid = PidServiceUtils.transportPid(conn, 5000,
						PidSequenceName.adAdminPidName);
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
	 * 申请ad_admin_name_pid
	 */
	public synchronized int applyAdAdminNamePid() throws Exception {

		Connection conn = null;

		int pid = 0;
		try {
			conn = PidServicePool.getInstance().getConnection();

			conn.setAutoCommit(false);

			String pidRange = PidServiceUtils.getPidRange(conn,
					PidSequenceName.adAdminNamePidName);

			if (pidRange != null) {
				PidRangeCombine prc = PidServiceUtils.applyPid(pidRange);

				if (prc.getPid() != -1) {
					PidServiceUtils.updatePidRange(conn,
							PidSequenceName.adAdminNamePidName,
							prc.getPidRange());

					pid = prc.getPid();
				} else {
					// 剩餘範圍不足,需要從ID分配器搬運新的PID
					pid = PidServiceUtils.transportPid(conn, 5000,
							PidSequenceName.adAdminNamePidName);
				}
			} else {
				// 不存在對應的序列,報錯且拋出異常

				pid = PidServiceUtils.transportPid(conn, 5000,
						PidSequenceName.adAdminNamePidName);
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
	 * 申请ad_node_pid
	 */
	public synchronized int applyAdNodePid() throws Exception {

		Connection conn = null;

		int pid = 0;
		try {
			conn = PidServicePool.getInstance().getConnection();

			conn.setAutoCommit(false);

			String pidRange = PidServiceUtils.getPidRange(conn,
					PidSequenceName.adAdminNodeName);

			if (pidRange != null) {
				PidRangeCombine prc = PidServiceUtils.applyPid(pidRange);

				if (prc.getPid() != -1) {
					PidServiceUtils.updatePidRange(conn,
							PidSequenceName.adAdminNodeName, prc.getPidRange());

					pid = prc.getPid();
				} else {
					// 剩餘範圍不足,需要從ID分配器搬運新的PID
					pid = PidServiceUtils.transportPid(conn, 5000,
							PidSequenceName.adAdminNodeName);
				}
			} else {
				// 不存在對應的序列,報錯且拋出異常

				pid = PidServiceUtils.transportPid(conn, 5000,
						PidSequenceName.adAdminNodeName);
			}

			System.out.println(conn + "----------------------");

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
	 * 申请ad_link_pid
	 */
	public synchronized int applyAdLinkPid() throws Exception {

		Connection conn = null;

		int pid = 0;
		try {
			conn = PidServicePool.getInstance().getConnection();

			conn.setAutoCommit(false);

			String pidRange = PidServiceUtils.getPidRange(conn,
					PidSequenceName.adAdminLinkName);

			if (pidRange != null) {
				PidRangeCombine prc = PidServiceUtils.applyPid(pidRange);

				if (prc.getPid() != -1) {
					PidServiceUtils.updatePidRange(conn,
							PidSequenceName.adAdminLinkName, prc.getPidRange());

					pid = prc.getPid();
				} else {
					// 剩餘範圍不足,需要從ID分配器搬運新的PID
					pid = PidServiceUtils.transportPid(conn, 5000,
							PidSequenceName.adAdminLinkName);
				}
			} else {
				// 不存在對應的序列,報錯且拋出異常

				pid = PidServiceUtils.transportPid(conn, 5000,
						PidSequenceName.adAdminLinkName);
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
	 * 申请ad_face_pid
	 */
	public synchronized int applyAdFacePid() throws Exception {

		Connection conn = null;

		int pid = 0;
		try {
			conn = PidServicePool.getInstance().getConnection();

			conn.setAutoCommit(false);

			String pidRange = PidServiceUtils.getPidRange(conn,
					PidSequenceName.adAdminFaceName);

			if (pidRange != null) {
				PidRangeCombine prc = PidServiceUtils.applyPid(pidRange);

				if (prc.getPid() != -1) {
					PidServiceUtils.updatePidRange(conn,
							PidSequenceName.adAdminFaceName, prc.getPidRange());

					pid = prc.getPid();
				} else {
					// 剩餘範圍不足,需要從ID分配器搬運新的PID
					pid = PidServiceUtils.transportPid(conn, 5000,
							PidSequenceName.adAdminFaceName);
				}
			} else {
				// 不存在對應的序列,報錯且拋出異常

				pid = PidServiceUtils.transportPid(conn, 5000,
						PidSequenceName.adAdminFaceName);
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
	 * 申请rtic代码
	 * 
	 * @return
	 * @throws Exception
	 */
	public synchronized int applyRticCode() throws Exception {

		Connection conn = null;

		int pid = 0;
		try {
			conn = PidServicePool.getInstance().getConnection();

			conn.setAutoCommit(false);

			String pidRange = PidServiceUtils.getPidRange(conn,
					PidSequenceName.rticCodeName);

			if (pidRange != null) {
				PidRangeCombine prc = PidServiceUtils.applyPid(pidRange);

				if (prc.getPid() != -1) {
					PidServiceUtils.updatePidRange(conn,
							PidSequenceName.rticCodeName, prc.getPidRange());

					pid = prc.getPid();
				} else {
					// 剩餘範圍不足,需要從ID分配器搬運新的PID
					pid = PidServiceUtils.transportPid(conn, 5000,
							PidSequenceName.rticCodeName);
				}
			} else {
				// 不存在對應的序列,報錯且拋出異常

				pid = PidServiceUtils.transportPid(conn, 5000,
						PidSequenceName.rticCodeName);
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
	 * 申请applyAdAdminGroupPid
	 */
	public synchronized int applyAdAdminGroupPid() throws Exception {

		Connection conn = null;

		int pid = 0;
		try {
			conn = PidServicePool.getInstance().getConnection();

			conn.setAutoCommit(false);

			String pidRange = PidServiceUtils.getPidRange(conn,
					PidSequenceName.adAdminGroupPidName);

			if (pidRange != null) {
				PidRangeCombine prc = PidServiceUtils.applyPid(pidRange);

				if (prc.getPid() != -1) {
					PidServiceUtils.updatePidRange(conn,
							PidSequenceName.adAdminGroupPidName,
							prc.getPidRange());

					pid = prc.getPid();
				} else {
					// 剩餘範圍不足,需要從ID分配器搬運新的PID
					pid = PidServiceUtils.transportPid(conn, 5000,
							PidSequenceName.adAdminGroupPidName);
				}
			} else {
				// 不存在對應的序列,報錯且拋出異常

				pid = PidServiceUtils.transportPid(conn, 5000,
						PidSequenceName.adAdminGroupPidName);
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
	 * 申请applyRdGscPid
	 */
	public synchronized int applyRdGscPid() throws Exception {

		Connection conn = null;

		int pid = 0;
		try {
			conn = PidServicePool.getInstance().getConnection();

			conn.setAutoCommit(false);

			String pidRange = PidServiceUtils.getPidRange(conn,
					PidSequenceName.rdGscPidName);

			if (pidRange != null) {
				PidRangeCombine prc = PidServiceUtils.applyPid(pidRange);

				if (prc.getPid() != -1) {
					PidServiceUtils.updatePidRange(conn,
							PidSequenceName.rdGscPidName, prc.getPidRange());

					pid = prc.getPid();
				} else {
					// 剩餘範圍不足,需要從ID分配器搬運新的PID
					pid = PidServiceUtils.transportPid(conn, 5000,
							PidSequenceName.rdGscPidName);
				}
			} else {
				// 不存在對應的序列,報錯且拋出異常

				pid = PidServiceUtils.transportPid(conn, 5000,
						PidSequenceName.rdGscPidName);
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
	 * 申请PoiPid
	 */
	public synchronized int applyPoiPid() throws Exception {

		Connection conn = null;

		int pid = 0;
		try {
			conn = PidServicePool.getInstance().getConnection();

			conn.setAutoCommit(false);

			String pidRange = PidServiceUtils.getPidRange(conn,
					PidSequenceName.poiPidName);

			if (pidRange != null) {
				PidRangeCombine prc = PidServiceUtils.applyPid(pidRange);

				if (prc.getPid() != -1) {
					PidServiceUtils.updatePidRange(conn,
							PidSequenceName.poiPidName, prc.getPidRange());

					pid = prc.getPid();
				} else {
					// 剩餘範圍不足,需要從ID分配器搬運新的PID
					pid = PidServiceUtils.transportPid(conn, 5000,
							PidSequenceName.poiPidName);
				}
			} else {
				// 不存在對應的序列,報錯且拋出異常

				pid = PidServiceUtils.transportPid(conn, 5000,
						PidSequenceName.poiPidName);
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
	 * 申请PoiNameId
	 */
	public synchronized int applyPoiNameId() throws Exception {

		Connection conn = null;

		int pid = 0;
		try {
			conn = PidServicePool.getInstance().getConnection();

			conn.setAutoCommit(false);

			String pidRange = PidServiceUtils.getPidRange(conn,
					PidSequenceName.poiNameIdName);

			if (pidRange != null) {
				PidRangeCombine prc = PidServiceUtils.applyPid(pidRange);

				if (prc.getPid() != -1) {
					PidServiceUtils.updatePidRange(conn,
							PidSequenceName.poiNameIdName, prc.getPidRange());

					pid = prc.getPid();
				} else {
					// 剩餘範圍不足,需要從ID分配器搬運新的PID
					pid = PidServiceUtils.transportPid(conn, 5000,
							PidSequenceName.poiNameIdName);
				}
			} else {
				// 不存在對應的序列,報錯且拋出異常

				pid = PidServiceUtils.transportPid(conn, 5000,
						PidSequenceName.poiNameIdName);
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
	 * 申请PoiAddressId
	 */
	public synchronized int applyPoiAddressId() throws Exception {

		Connection conn = null;

		int pid = 0;
		try {
			conn = PidServicePool.getInstance().getConnection();

			conn.setAutoCommit(false);

			String pidRange = PidServiceUtils.getPidRange(conn,
					PidSequenceName.poiAddressIdName);

			if (pidRange != null) {
				PidRangeCombine prc = PidServiceUtils.applyPid(pidRange);

				if (prc.getPid() != -1) {
					PidServiceUtils.updatePidRange(conn,
							PidSequenceName.poiAddressIdName, prc.getPidRange());

					pid = prc.getPid();
				} else {
					// 剩餘範圍不足,需要從ID分配器搬運新的PID
					pid = PidServiceUtils.transportPid(conn, 5000,
							PidSequenceName.poiAddressIdName);
				}
			} else {
				// 不存在對應的序列,報錯且拋出異常

				pid = PidServiceUtils.transportPid(conn, 5000,
						PidSequenceName.poiAddressIdName);
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
	 * 申请PoiGroupId
	 */
	public synchronized int applyPoiGroupId() throws Exception {

		Connection conn = null;

		int pid = 0;
		try {
			conn = PidServicePool.getInstance().getConnection();

			conn.setAutoCommit(false);

			String pidRange = PidServiceUtils.getPidRange(conn,
					PidSequenceName.poiGroupIdName);

			if (pidRange != null) {
				PidRangeCombine prc = PidServiceUtils.applyPid(pidRange);

				if (prc.getPid() != -1) {
					PidServiceUtils.updatePidRange(conn,
							PidSequenceName.poiGroupIdName, prc.getPidRange());

					pid = prc.getPid();
				} else {
					// 剩餘範圍不足,需要從ID分配器搬運新的PID
					pid = PidServiceUtils.transportPid(conn, 5000,
							PidSequenceName.poiGroupIdName);
				}
			} else {
				// 不存在對應的序列,報錯且拋出異常

				pid = PidServiceUtils.transportPid(conn, 5000,
						PidSequenceName.poiGroupIdName);
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
	 * 申请PoiGasstationId
	 */
	public synchronized int applyPoiGasstationId() throws Exception {

		Connection conn = null;

		int pid = 0;
		try {
			conn = PidServicePool.getInstance().getConnection();

			conn.setAutoCommit(false);

			String pidRange = PidServiceUtils.getPidRange(conn,
					PidSequenceName.poiGasstationIdName);

			if (pidRange != null) {
				PidRangeCombine prc = PidServiceUtils.applyPid(pidRange);

				if (prc.getPid() != -1) {
					PidServiceUtils.updatePidRange(conn,
							PidSequenceName.poiGasstationIdName, prc.getPidRange());

					pid = prc.getPid();
				} else {
					// 剩餘範圍不足,需要從ID分配器搬運新的PID
					pid = PidServiceUtils.transportPid(conn, 5000,
							PidSequenceName.poiGasstationIdName);
				}
			} else {
				// 不存在對應的序列,報錯且拋出異常

				pid = PidServiceUtils.transportPid(conn, 5000,
						PidSequenceName.poiGasstationIdName);
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
	 * 申请PoiParkingsId
	 */
	public synchronized int applyPoiParkingsId() throws Exception {

		Connection conn = null;

		int pid = 0;
		try {
			conn = PidServicePool.getInstance().getConnection();

			conn.setAutoCommit(false);

			String pidRange = PidServiceUtils.getPidRange(conn,
					PidSequenceName.poiParkingsIdName);

			if (pidRange != null) {
				PidRangeCombine prc = PidServiceUtils.applyPid(pidRange);

				if (prc.getPid() != -1) {
					PidServiceUtils.updatePidRange(conn,
							PidSequenceName.poiParkingsIdName, prc.getPidRange());

					pid = prc.getPid();
				} else {
					// 剩餘範圍不足,需要從ID分配器搬運新的PID
					pid = PidServiceUtils.transportPid(conn, 5000,
							PidSequenceName.poiParkingsIdName);
				}
			} else {
				// 不存在對應的序列,報錯且拋出異常

				pid = PidServiceUtils.transportPid(conn, 5000,
						PidSequenceName.poiParkingsIdName);
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
	 * 申请PoiHotelId
	 */
	public synchronized int applyPoiHotelId() throws Exception {

		Connection conn = null;

		int pid = 0;
		try {
			conn = PidServicePool.getInstance().getConnection();

			conn.setAutoCommit(false);

			String pidRange = PidServiceUtils.getPidRange(conn,
					PidSequenceName.poiHotelIdName);

			if (pidRange != null) {
				PidRangeCombine prc = PidServiceUtils.applyPid(pidRange);

				if (prc.getPid() != -1) {
					PidServiceUtils.updatePidRange(conn,
							PidSequenceName.poiHotelIdName, prc.getPidRange());

					pid = prc.getPid();
				} else {
					// 剩餘範圍不足,需要從ID分配器搬運新的PID
					pid = PidServiceUtils.transportPid(conn, 5000,
							PidSequenceName.poiHotelIdName);
				}
			} else {
				// 不存在對應的序列,報錯且拋出異常

				pid = PidServiceUtils.transportPid(conn, 5000,
						PidSequenceName.poiHotelIdName);
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
	 * 申请PoiFoodId
	 */
	public synchronized int applyPoiFoodId() throws Exception {

		Connection conn = null;

		int pid = 0;
		try {
			conn = PidServicePool.getInstance().getConnection();

			conn.setAutoCommit(false);

			String pidRange = PidServiceUtils.getPidRange(conn,
					PidSequenceName.poiFoodIdName);

			if (pidRange != null) {
				PidRangeCombine prc = PidServiceUtils.applyPid(pidRange);

				if (prc.getPid() != -1) {
					PidServiceUtils.updatePidRange(conn,
							PidSequenceName.poiFoodIdName, prc.getPidRange());

					pid = prc.getPid();
				} else {
					// 剩餘範圍不足,需要從ID分配器搬運新的PID
					pid = PidServiceUtils.transportPid(conn, 5000,
							PidSequenceName.poiFoodIdName);
				}
			} else {
				// 不存在對應的序列,報錯且拋出異常

				pid = PidServiceUtils.transportPid(conn, 5000,
						PidSequenceName.poiFoodIdName);
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
