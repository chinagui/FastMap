package com.navinfo.dataservice.dao.pidservice;

import java.sql.Connection;

import org.apache.commons.dbutils.DbUtils;

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

		return applyPid(PidSequenceName.linkPidName);
	}

	/**
	 * 申請node_pid
	 */
	public synchronized int applyNodePid() throws Exception {
		return applyPid(PidSequenceName.nodePidName);
	}

	/**
	 * 申請rd_restriciton pid
	 */
	public synchronized int applyRestrictionPid() throws Exception {
		return applyPid(PidSequenceName.restrictPidName);
	}

	/**
	 * 申請rd_restriction_detail pid
	 */
	public synchronized int applyRestrictionDetailPid() throws Exception {
		return applyPid(PidSequenceName.restrictDetailPidName);

	}

	/**
	 * 申請node_name_id
	 */
	public synchronized int applyNodeNameId() throws Exception {
		return applyPid(PidSequenceName.nodeNameIdName);

	}

	/**
	 * 申請rdspeedlimit pid
	 */
	public synchronized int applySpeedLimitPid() throws Exception {
		return applyPid(PidSequenceName.speedLimitPidName);

	}

	public synchronized int applyLaneConnexityPid() throws Exception {
		return applyPid(PidSequenceName.laneConnexityPidName);
	}

	public synchronized int applyLaneTopologyPid() throws Exception {
		return applyPid(PidSequenceName.laneTopologyPidName);

	}

	public synchronized int applyRdCrossPid() throws Exception {
		return applyPid(PidSequenceName.crossPidName);

	}

	public synchronized int applyRdCrossNameId() throws Exception {
		return applyPid(PidSequenceName.crossNameIdName);
	}

	public synchronized int applyBranchPid() throws Exception {
		return applyPid(PidSequenceName.branchPidName);

	}

	public synchronized int applyBranchDetailId() throws Exception {
		return applyPid(PidSequenceName.branchDetailIdName);

	}

	public synchronized int applyBranchNameId() throws Exception {
		return applyPid(PidSequenceName.branchNameIdName);
	}

	public synchronized int applyRdSignasreal() throws Exception {
		return applyPid(PidSequenceName.signasrealIdName);

	}

	public synchronized int applyBranchSchematic() throws Exception {
		return applyPid(PidSequenceName.schematicIdName);

	}

	public synchronized int applyRdSignboard() throws Exception {
		return applyPid(PidSequenceName.signboardIdName);

	}

	public synchronized int applyRdSignboardName() throws Exception {
		return applyPid(PidSequenceName.signboardNameIdName);
	}

	public synchronized int applyCkExceptionId() throws Exception {
		return applyPid(PidSequenceName.ckExceptionIdName);

	}
	
	/**
	 * 申请rd_trafficsignal
	 */
	public synchronized int applyRdTrafficsignalPid() throws Exception {
		return applyPid(PidSequenceName.rdTrafficsignal);

	}
	
	/**
	 * 申请ad_admin_pid
	 */
	public synchronized int applyAdAdminPid() throws Exception {
		return applyPid(PidSequenceName.adAdminPidName);

	}

	/**
	 * 申请ad_admin_name_pid
	 */
	public synchronized int applyAdAdminNamePid() throws Exception {
		return applyPid(PidSequenceName.adAdminNamePidName);

	}

	/**
	 * 申请ad_node_pid
	 */
	public synchronized int applyAdNodePid() throws Exception {
		return applyPid(PidSequenceName.adAdminNodeName);
	}

	/**
	 * 申请rw_node_pid
	 */
	public synchronized int applyRwNodePid() throws Exception {
		return applyPid(PidSequenceName.rwNodeName);
	}

	/**
	 * 申请rw_link_pid
	 */
	public synchronized int applyRwLinkPid() throws Exception {
		return applyPid(PidSequenceName.rwLinkName);
	}

	/**
	 * 申请ad_link_pid
	 */
	public synchronized int applyAdLinkPid() throws Exception {
		return applyPid(PidSequenceName.adAdminLinkName);

	}

	/**
	 * 申请ad_face_pid
	 */
	public synchronized int applyAdFacePid() throws Exception {
		return applyPid(PidSequenceName.adAdminFaceName);

	}

	/**
	 * 申请rtic代码
	 * 
	 * @return
	 * @throws Exception
	 */
	public synchronized int applyRticCode() throws Exception {
		return applyPid(PidSequenceName.rticCodeName);

	}

	/**
	 * 申请applyAdAdminGroupPid
	 */
	public synchronized int applyAdAdminGroupPid() throws Exception {
		return applyPid(PidSequenceName.adAdminGroupPidName);

	}

	/**
	 * 申请applyRdGscPid
	 */
	public synchronized int applyRdGscPid() throws Exception {
		return applyPid(PidSequenceName.rdGscPidName);
	}

	/**
	 * 申请PoiPid
	 */
	public synchronized int applyPoiPid() throws Exception {
		return applyPid(PidSequenceName.poiPidName);

	}

	/**
	 * 申请PoiNameId
	 */
	public synchronized int applyPoiNameId() throws Exception {
		return applyPid(PidSequenceName.poiNameIdName);

	}

	/**
	 * 申请PoiAddressId
	 */
	public synchronized int applyPoiAddressId() throws Exception {
		return applyPid(PidSequenceName.poiAddressIdName);

	}

	/**
	 * 申请PoiGroupId
	 */
	public synchronized int applyPoiGroupId() throws Exception {
		return applyPid(PidSequenceName.poiGroupIdName);

	}

	/**
	 * 申请PoiGasstationId
	 */
	public synchronized int applyPoiGasstationId() throws Exception {
		return applyPid(PidSequenceName.poiGasstationIdName);

	}

	/**
	 * 申请PoiParkingsId
	 */
	public synchronized int applyPoiParkingsId() throws Exception {
		return applyPid(PidSequenceName.poiParkingsIdName);
	}

	/**
	 * 申请PoiHotelId
	 */
	public synchronized int applyPoiHotelId() throws Exception {
		return applyPid(PidSequenceName.poiHotelIdName);
	}

	/**
	 * 申请PoiFoodId
	 */
	public synchronized int applyPoiFoodId() throws Exception {
		return applyPid(PidSequenceName.poiFoodIdName);
	}

	/**
	 * 申请PoiIconId
	 */
	public synchronized int applyPoiIconId() throws Exception {
		return applyPid(PidSequenceName.poiIconIdName);
	}

	/**
	 * 申请PoiAttractionId
	 */
	public synchronized int applyPoiAttractionId() throws Exception {
		return applyPid(PidSequenceName.poiAttractionIdName);

	}

	/**
	 * 申请PoiRestaurantId
	 */
	public synchronized int applyPoiRestaurantId() throws Exception {
		return applyPid(PidSequenceName.poiRestaurantIdName);

	}

	/**
	 * 申请zone_node_pid
	 */
	public synchronized int applyZoneNodePid() throws Exception {
		return applyPid(PidSequenceName.ZoneNodeName);

	}

	/**
	 * 申请zone_link_pid
	 */
	public synchronized int applyZoneLinkPid() throws Exception {
		return applyPid(PidSequenceName.ZoneLinkName);

	}

	/**
	 * 申请zone_face_pid
	 */
	public synchronized int applyZoneFacePid() throws Exception {
		return applyPid(PidSequenceName.ZoneFaceName);
	}

	private int applyPid(final String pidSeqName) throws Exception {
		Connection conn = null;

		int pid = 0;
		try {
			conn = PidServicePool.getInstance().getConnection();

			conn.setAutoCommit(false);

			String pidRange = PidServiceUtils.getPidRange(conn, pidSeqName);

			if (pidRange != null) {
				PidRangeCombine prc = PidServiceUtils.applyPid(pidRange);

				if (prc.getPid() != -1) {
					PidServiceUtils.updatePidRange(conn, pidSeqName, prc.getPidRange());

					pid = prc.getPid();
				} else {
					// 剩餘範圍不足,需要從ID分配器搬運新的PID
					pid = PidServiceUtils.transportPid(conn, 5000, pidSeqName);
				}
			} else {
				// 不存在對應的序列,報錯且拋出異常

				pid = PidServiceUtils.transportPid(conn, 5000, pidSeqName);
			}

		} catch (Exception e) {

			throw e;

		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}

		return pid;
	}

	/**
	 * 申請rd_name pid
	 */
	public synchronized int applyRdNamePid() throws Exception {
		return applyPid(PidSequenceName.rdNameIdName);
	}

	/**
	 * 申请lu_node_pid
	 */
	public synchronized int applyLuNodePid() throws Exception {
		return this.applyPid(PidSequenceName.luNodePidName);
	}

	/**
	 * 申请lu_link_pid
	 */
	public synchronized int applyLuLinkPid() throws Exception {
		return this.applyPid(PidSequenceName.luLinkPidName);
	}

	/**
	 * 申请lu_face_pid
	 */
	public synchronized int applyLuFacePid() throws Exception {
		return this.applyPid(PidSequenceName.luFacePidName);
	}

	/**
	 * 申请rd_electroniceye
	 */
	public synchronized int applyElectroniceyePid() throws Exception {
		return this.applyPid(PidSequenceName.rdElectroniceyePidName);
	}

	/**
	 * 申请rd_eleceye_pair
	 */
	public synchronized int applyEleceyePairPid() throws Exception {
		return this.applyPid(PidSequenceName.rdEleceyePairPidName);
	}
	
	/**
	 * 申请rd_warninginfo
	 */
	public synchronized int applyRdWarninginfoPid() throws Exception {
		return this.applyPid(PidSequenceName.rdWarninginfo);
	}
	/**
	 * 申请rd_slope
	 */
	public synchronized int applyRdSlopePid() throws Exception {
		return this.applyPid(PidSequenceName.rdSlopeName);
	}
}
