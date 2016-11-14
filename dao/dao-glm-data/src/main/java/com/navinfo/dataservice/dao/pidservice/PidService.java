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

/*	public synchronized static PidService getInstance() throws Exception {
		if (pidService == null) {
			pidService = new PidService();
		}
		return pidService;
	}
*/
	/**
	 * 申請link_pid
	 */
	public synchronized int applyLinkPid() throws Exception {

		return applyPid(PidSequenceName.rdLinkPidName);
	}

	/**
	 * 申請node_pid
	 */
	public synchronized int applyNodePid() throws Exception {
		return applyPid(PidSequenceName.rdNodePidName);
	}

	/**
	 * 申請rd_restriciton pid
	 */
	public synchronized int applyRestrictionPid() throws Exception {
		return applyPid(PidSequenceName.rdRestrictPidName);
	}

	/**
	 * 申請rd_restriction_detail pid
	 */
	public synchronized int applyRestrictionDetailPid() throws Exception {
		return applyPid(PidSequenceName.rdRestrictDetailPidName);

	}

	/**
	 * 申請node_name_id
	 */
	public synchronized int applyNodeNameId() throws Exception {
		return applyPid(PidSequenceName.rdNodeNamePidName);

	}

	/**
	 * 申請rdspeedlimit pid
	 */
	public synchronized int applySpeedLimitPid() throws Exception {
		return applyPid(PidSequenceName.rdSpeedLimitPidName);

	}

	public synchronized int applyLaneConnexityPid() throws Exception {
		return applyPid(PidSequenceName.rdLaneConnexityPidName);
	}

	public synchronized int applyLaneTopologyPid() throws Exception {
		return applyPid(PidSequenceName.rdLaneTopologyPidName);

	}

	public synchronized int applyRdCrossPid() throws Exception {
		return applyPid(PidSequenceName.rdCrossPidName);

	}

	public synchronized int applyRdCrossNameId() throws Exception {
		return applyPid(PidSequenceName.rdCrossNamePidName);
	}

	public synchronized int applyBranchPid() throws Exception {
		return applyPid(PidSequenceName.rdBranchPidName);

	}

	public synchronized int applyBranchDetailId() throws Exception {
		return applyPid(PidSequenceName.rdBranchDetailPidName);

	}

	public synchronized int applyBranchNameId() throws Exception {
		return applyPid(PidSequenceName.rdBranchNamePidName);
	}

	public synchronized int applyRdSignasreal() throws Exception {
		return applyPid(PidSequenceName.rdSignasrealPidName);

	}

	public synchronized int applyBranchSchematic() throws Exception {
		return applyPid(PidSequenceName.rdSchematicPidName);

	}

	public synchronized int applyRdSignboard() throws Exception {
		return applyPid(PidSequenceName.rdSignboardPidName);

	}

	public synchronized int applyRdSignboardName() throws Exception {
		return applyPid(PidSequenceName.rdSignboardNamePidName);
	}

	public synchronized int applyCkExceptionId() throws Exception {
		return applyPid(PidSequenceName.ckExceptionPidName);

	}

	/**
	 * 申请rd_trafficsignal
	 */
	public synchronized int applyRdTrafficsignalPid() throws Exception {
		return applyPid(PidSequenceName.rdTrafficsignalPidName);

	}

	/**
	 * 申请 顺行pid
	 *
	 * @return
	 * @throws Exception
	 */
	public synchronized int applyRdDirectroutePid() throws Exception {
		return applyPid(PidSequenceName.rdDirectrouteName);

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
		return applyPid(PidSequenceName.adNodePidName);
	}

	/**
	 * 申请rw_node_pid
	 */
	public synchronized int applyRwNodePid() throws Exception {
		return applyPid(PidSequenceName.rwNodePidName);
	}

	/**
	 * 申请rw_link_pid
	 */
	public synchronized int applyRwLinkPid() throws Exception {
		return applyPid(PidSequenceName.rwLinkPidName);
	}

	/**
	 * 申请ad_link_pid
	 */
	public synchronized int applyAdLinkPid() throws Exception {
		return applyPid(PidSequenceName.adLinkPidName);

	}

	/**
	 * 申请ad_face_pid
	 */
	public synchronized int applyAdFacePid() throws Exception {
		return applyPid(PidSequenceName.adFacePidName);

	}

	/**
	 * 申请RD_VARIABLE_SPEED
	 */
	public synchronized int applyRdVariableSpeedPid() throws Exception {
		return applyPid(PidSequenceName.rdVariableSpeedName);

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
		return applyPid(PidSequenceName.poiNamePidName);

	}

	/**
	 * 申请PoiAddressId
	 */
	public synchronized int applyPoiAddressId() throws Exception {
		return applyPid(PidSequenceName.poiAddressPidName);

	}

	/**
	 * 申请PoiGroupId
	 */
	public synchronized int applyPoiGroupId() throws Exception {
		return applyPid(PidSequenceName.poiGroupPidName);

	}

	/**
	 * 申请PoiGasstationId
	 */
	public synchronized int applyPoiGasstationId() throws Exception {
		return applyPid(PidSequenceName.poiGasstationPidName);

	}

	/**
	 * 申请PoiParkingsId
	 */
	public synchronized int applyPoiParkingsId() throws Exception {
		return applyPid(PidSequenceName.poiParkingsPidName);
	}

	/**
	 * 申请PoiHotelId
	 */
	public synchronized int applyPoiHotelId() throws Exception {
		return applyPid(PidSequenceName.poiHotelIdPidName);
	}

	/**
	 * 申请PoiFoodId
	 */
	public synchronized int applyPoiFoodId() throws Exception {
		return applyPid(PidSequenceName.poiFoodIdPidName);
	}

	/**
	 * 申请PoiIconId
	 */
	public synchronized int applyPoiIconId() throws Exception {
		return applyPid(PidSequenceName.poiIconIdPidName);
	}

	/**
	 * 申请PoiAttractionId
	 */
	public synchronized int applyPoiAttractionId() throws Exception {
		return applyPid(PidSequenceName.poiAttractionPidName);

	}

	/**
	 * 申请PoiRestaurantId
	 */
	public synchronized int applyPoiRestaurantId() throws Exception {
		return applyPid(PidSequenceName.poiRestaurantPidName);

	}

	/**
	 * 申请zone_node_pid
	 */
	public synchronized int applyZoneNodePid() throws Exception {
		return applyPid(PidSequenceName.zoneNodePidName);

	}

	/**
	 * 申请zone_link_pid
	 */
	public synchronized int applyZoneLinkPid() throws Exception {
		return applyPid(PidSequenceName.zoneLinkPidName);

	}

	/**
	 * 申请rd_lane_pid
	 */
	public synchronized int applyRdLanePid() throws Exception {
		return applyPid(PidSequenceName.rdLanePidName);

	}

	/**
	 * 申请rd_lane_topo_pid
	 */
	public synchronized int applyRdLaneTopoPid() throws Exception {
		return applyPid(PidSequenceName.rdLanetopoPidName);

	}

	/**
	 * 申请zone_face_pid
	 */
	public synchronized int applyZoneFacePid() throws Exception {
		return applyPid(PidSequenceName.zoneFacePidName);
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
		return applyPid(PidSequenceName.rdNameIdPidName);
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
	 * 申请lu_face_name_pid
	 */
	public synchronized int applyLuFaceNamePid() throws Exception {
		return this.applyPid(PidSequenceName.luFaceNamePidName);
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
		return this.applyPid(PidSequenceName.rdWarninginfoPidName);
	}

	/**
	 * 申请rd_slope
	 */
	public synchronized int applyRdSlopePid() throws Exception {
		return this.applyPid(PidSequenceName.rdSlopePidName);
	}

	/**
	 *
	 * 申请applyRdGate
	 */
	public synchronized int applyRdGate() throws Exception {
		return applyPid(PidSequenceName.rdGatePidName);
	}

	/**
	 * 申请lc_node_pid
	 */
	public synchronized int applyLcNodePid() throws Exception {
		return this.applyPid(PidSequenceName.lcNodePidName);
	}

	/**
	 * 申请lc_link_pid
	 */
	public synchronized int applyLcLinkPid() throws Exception {
		return this.applyPid(PidSequenceName.lcLinkPidName);
	}

	/**
	 * 申请lc_face_pid
	 */
	public synchronized int applyLcFacePid() throws Exception {
		return this.applyPid(PidSequenceName.lcFacePidName);
	}

	/**
	 * 申请lc_face_name_pid
	 */
	public synchronized int applyLcFaceNamePid() throws Exception {
		return this.applyPid(PidSequenceName.lcFaceNamePidName);
	}

	/**
	 * 申请rd_se
	 */
	public synchronized int applyRdSePid() throws Exception {
		return this.applyPid(PidSequenceName.rdSePidName);
	}

	/**
	 * 申请rd_inter
	 */
	public synchronized int applyRdInterPid() throws Exception {
		return this.applyPid(PidSequenceName.rdInterName);
	}

	/**
	 * 申请rd_object
	 */
	public synchronized int applyRdObjectPid() throws Exception {
		return this.applyPid(PidSequenceName.rdObjectName);
	}

	/**
	 * 申请rd_speedbump
	 */
	public synchronized int applyRdSpeedbumpPid() throws Exception {
		return this.applyPid(PidSequenceName.rdSpeedbumpName);
	}

	/**
	 * 申请rd_samenode
	 */
	public synchronized int applyRdSameNodePid() throws Exception {
		return this.applyPid(PidSequenceName.rdSamenodeName);
	}

	/**
	 * 申请rd_samelink
	 */
	public synchronized int applyRdSameLinkPid() throws Exception {
		return this.applyPid(PidSequenceName.rdSamelinkName);
	}

	/**
	 * 申请rd_tollgate
	 */
	public synchronized int applyRdTollgatePid() throws Exception {
		return this.applyPid(PidSequenceName.rdTollgate);
	}

	/**
	 * 申请rd_tollgate_name
	 */
	public synchronized int applyRdTollgateNamePid() throws Exception {
		return this.applyPid(PidSequenceName.rdTollgateName);
	}

	/**
	 * 申请rd_voiceguide
	 *
	 * @return
	 * @throws Exception
	 */
	public synchronized int applyRdVoiceguidePid() throws Exception {
		return applyPid(PidSequenceName.rdVoiceguideName);
	}

	/**
	 * 申请rd_voiceguide_detail
	 *
	 * @return
	 * @throws Exception
	 */
	public synchronized int applyRdVoiceguideDetailPid() throws Exception {
		return applyPid(PidSequenceName.rdVoiceguideDetailName);
	}

	/**
	 * 申请IX_SAMEPOI
	 *
	 * @return
	 * @throws Exception
	 */
	public synchronized int applySamepoiPid() throws Exception {
		return applyPid(PidSequenceName.samePoiPidName);
	}
}
