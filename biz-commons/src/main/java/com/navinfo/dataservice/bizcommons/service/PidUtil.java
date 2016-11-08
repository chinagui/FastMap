package com.navinfo.dataservice.bizcommons.service;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;

/**
 * @author xiaoxiaowen4127
 * @ClassName: PidUtil
 * @date 2016年9月5日
 * @Description: PidUtil.java
 */
public class PidUtil {
    protected Logger log = LoggerRepos.getLogger(this.getClass());
    private volatile static PidUtil instance;

    public static PidUtil getInstance() {
        if (instance == null) {
            synchronized (PidUtil.class) {
                if (instance == null) {
                    instance = new PidUtil();
                }
            }
        }
        return instance;
    }

    private PidUtil() {
    }

    private int applyPid(String tableName) throws Exception {
        return (int) PidService.getInstance().applyPid(tableName, 1);
    }

    /**
     * 申請link_pid
     */
    public int applyLinkPid() throws Exception {

        return applyPid("RD_LINK");
    }

    /**
     * 申請node_pid
     */
    public int applyNodePid() throws Exception {
        return applyPid("RD_NODE");
    }

    /**
     * 申請rd_restriciton pid
     */
    public int applyRestrictionPid() throws Exception {
        return applyPid("RD_RESTRICTION");
    }

    /**
     * 申請rd_restriction_detail pid
     */
    public int applyRestrictionDetailPid() throws Exception {
        return applyPid("RD_RESTRICTION_DETAIL");

    }

    /**
     * 申請node_name_id
     */
    public int applyNodeNameId() throws Exception {
        return applyPid("RD_NODE_NAME");

    }

    /**
     * 申請rdspeedlimit pid
     */
    public int applySpeedLimitPid() throws Exception {
        return applyPid("RD_SPEEDLIMIT");

    }

    public int applyLaneConnexityPid() throws Exception {
        return applyPid("RD_LANE_CONNEXITY");
    }

    public int applyLaneTopologyPid() throws Exception {
        return applyPid("RD_LANE_TOPOLOGY");

    }

    public int applyRdCrossPid() throws Exception {
        return applyPid("RD_CROSS");

    }

    public int applyRdCrossNameId() throws Exception {
        return applyPid("RD_CROSS_NAME");
    }

    public int applyBranchPid() throws Exception {
        return applyPid("RD_BRANCH");

    }

    public int applyBranchDetailId() throws Exception {
        return applyPid("RD_BRANCH_DETAIL");

    }

    public int applyBranchNameId() throws Exception {
        return applyPid("RD_BRANCH_NAME");
    }

    public int applyRdSignasreal() throws Exception {
        return applyPid("RD_SIGNASREAL");

    }

    public int applyBranchSchematic() throws Exception {
        return applyPid("RD_BRANCH_SCHEMATIC");

    }

    public int applyRdSignboard() throws Exception {
        return applyPid("RD_SIGNBOARD");

    }

    public int applyRdSignboardName() throws Exception {
        return applyPid("RD_SIGNBOARD_NAME");
    }

    public int applyCkExceptionId() throws Exception {
        return applyPid("CK_EXCEPTION");

    }

    /**
     * 申请rd_trafficsignal
     */
    public int applyRdTrafficsignalPid() throws Exception {
        return applyPid("RD_TRAFFICSIGNAL");

    }

    /**
     * 申请 顺行pid
     *
     * @return
     * @throws Exception
     */
    public int applyRdDirectroutePid() throws Exception {
        return applyPid("RD_DIRECTROUTE");

    }

    /**
     * 申请ad_admin_pid
     */
    public int applyAdAdminPid() throws Exception {
        return applyPid("AD_ADMIN");

    }

    /**
     * 申请ad_admin_name_pid
     */
    public int applyAdAdminNamePid() throws Exception {
        return applyPid("AD_ADMIN_NAME");

    }

    /**
     * 申请ad_node_pid
     */
    public int applyAdNodePid() throws Exception {
        return applyPid("AD_NODE");
    }

    /**
     * 申请rw_node_pid
     */
    public int applyRwNodePid() throws Exception {
        return applyPid("RW_NODE");
    }

    /**
     * 申请rw_link_pid
     */
    public int applyRwLinkPid() throws Exception {
        return applyPid("RW_LINK");
    }

    /**
     * 申请ad_link_pid
     */
    public int applyAdLinkPid() throws Exception {
        return applyPid("AD_LINK");

    }

    /**
     * 申请ad_face_pid
     */
    public int applyAdFacePid() throws Exception {
        return applyPid("AD_FACE");

    }

    /**
     * 申请RD_VARIABLE_SPEED
     */
    public int applyRdVariableSpeedPid() throws Exception {
        return applyPid("RD_VARIABLE_SPEED");

    }

    /**
     * 申请rtic代码
     *
     * @return
     * @throws Exception
     */
    public int applyRticCode() throws Exception {
        return applyPid("RTIC_CODE");

    }

    /**
     * 申请applyAdAdminGroupPid
     */
    public int applyAdAdminGroupPid() throws Exception {
        return applyPid("AD_ADMIN_GROUP");

    }

    /**
     * 申请applyRdGscPid
     */
    public int applyRdGscPid() throws Exception {
        return applyPid("RD_GSC");
    }

    /**
     * 申请PoiPid
     */
    public int applyPoiPid() throws Exception {
        return applyPid("IX_POI");

    }

    /**
     * 申请PoiNameId
     */
    public int applyPoiNameId() throws Exception {
        return applyPid("IX_POI_NAME");

    }

    /**
     * 申请PoiAddressId
     */
    public int applyPoiAddressId() throws Exception {
        return applyPid("IX_POI_ADDRESS");

    }

    /**
     * 申请PoiParentGroupId
     */
    public int applyPoiGroupId() throws Exception {
        return applyPid("IX_POI_PARENT");

    }

    /**
     * 申请PoiGasstationId
     */
    public int applyPoiGasstationId() throws Exception {
        return applyPid("IX_POI_GASSTATION");

    }

    /**
     * 申请PoiParkingsId
     */
    public int applyPoiParkingsId() throws Exception {
        return applyPid("IX_POI_PARKING");
    }

    /**
     * 申请PoiHotelId
     */
    public int applyPoiHotelId() throws Exception {
        return applyPid("IX_POI_HOTEL");
    }

    /**
     * 申请PoiIconId
     */
    public int applyPoiIconId() throws Exception {
        return applyPid("IX_POI_ICON");
    }

    /**
     * 申请PoiAttractionId
     */
    public int applyPoiAttractionId() throws Exception {
        return applyPid("IX_POI_ATTRACTION");

    }

    /**
     * 申请PoiRestaurantId
     */
    public int applyPoiRestaurantId() throws Exception {
        return applyPid("IX_POI_RESTAURANT");

    }

    /**
     * 申请zone_node_pid
     */
    public int applyZoneNodePid() throws Exception {
        return applyPid("ZONE_NODE");

    }

    /**
     * 申请zone_link_pid
     */
    public int applyZoneLinkPid() throws Exception {
        return applyPid("ZONE_LINK");

    }

    /**
     * 申请rd_lane_pid
     */
    public int applyRdLanePid() throws Exception {
        return applyPid("RD_LANE");

    }

    /**
     * 申请rd_lane_topo_pid
     */
    public int applyRdLaneTopoPid() throws Exception {
        return applyPid("RD_LANE_TOPOLOGY");

    }

    /**
     * 申请zone_face_pid
     */
    public int applyZoneFacePid() throws Exception {
        return applyPid("ZONE_FACE");
    }


    /**
     * 申請rd_name pid
     */
    public int applyRdNamePid() throws Exception {
        return applyPid("RD_NAME");
    }

    /**
     * 申请lu_node_pid
     */
    public int applyLuNodePid() throws Exception {
        return this.applyPid("LU_NODE");
    }

    /**
     * 申请lu_link_pid
     */
    public int applyLuLinkPid() throws Exception {
        return this.applyPid("LU_LINK");
    }

    /**
     * 申请lu_face_pid
     */
    public int applyLuFacePid() throws Exception {
        return this.applyPid("LU_FACE");
    }

    /**
     * 申请lu_face_name_pid
     */
    public int applyLuFaceNamePid() throws Exception {
        return this.applyPid("LU_FACE_NAME");
    }

    /**
     * 申请rd_electroniceye
     */
    public int applyElectroniceyePid() throws Exception {
        return this.applyPid("RD_ELECTRONICEYE");
    }

    /**
     * 申请rd_eleceye_pair
     */
    public int applyEleceyePairPid() throws Exception {
        return this.applyPid("RD_ELECEYE_PAIR");
    }

    /**
     * 申请rd_warninginfo
     */
    public int applyRdWarninginfoPid() throws Exception {
        return this.applyPid("RD_WARNINGINFO");
    }

    /**
     * 申请rd_slope
     */
    public int applyRdSlopePid() throws Exception {
        return this.applyPid("RD_SLOPE");
    }

    /**
     * 申请applyRdGate
     */
    public int applyRdGate() throws Exception {
        return applyPid("RD_GATE");
    }

    /**
     * 申请lc_node_pid
     */
    public int applyLcNodePid() throws Exception {
        return this.applyPid("LC_NODE");
    }

    /**
     * 申请lc_link_pid
     */
    public int applyLcLinkPid() throws Exception {
        return this.applyPid("LC_LINK");
    }

    /**
     * 申请lc_face_pid
     */
    public int applyLcFacePid() throws Exception {
        return this.applyPid("LC_FACE");
    }

    /**
     * 申请lc_face_name_pid
     */
    public int applyLcFaceNamePid() throws Exception {
        return this.applyPid("LC_FACE_NAME");
    }

    /**
     * 申请rd_se
     */
    public int applyRdSePid() throws Exception {
        return this.applyPid("RD_SE");
    }

    /**
     * 申请rd_inter
     */
    public int applyRdInterPid() throws Exception {
        return this.applyPid("RD_INTER");
    }

    /**
     * 申请rd_object
     */
    public int applyRdObjectPid() throws Exception {
        return this.applyPid("RD_OBJECT");
    }

    /**
     * 申请rd_speedbump
     */
    public int applyRdSpeedbumpPid() throws Exception {
        return this.applyPid("RD_SPEEDBUMP");
    }

    /**
     * 申请rd_samenode
     */
    public int applyRdSameNodePid() throws Exception {
        return this.applyPid("RD_SAMENODE");
    }

    /**
     * 申请rd_samelink
     */
    public int applyRdSameLinkPid() throws Exception {
        return this.applyPid("RD_SAMELINK");
    }

    /**
     * 申请rd_tollgate
     */
    public int applyRdTollgatePid() throws Exception {
        return this.applyPid("RD_TOLLGATE");
    }

    /**
     * 申请rd_tollgate_name
     */
    public int applyRdTollgateNamePid() throws Exception {
        return this.applyPid("RD_TOLLGATE_NAME");
    }

    /**
     * 申请rd_voiceguide
     *
     * @return
     * @throws Exception
     */
    public int applyRdVoiceguidePid() throws Exception {
        return applyPid("RD_VOICEGUIDE");
    }

    /**
     * 申请rd_voiceguide_detail
     *
     * @return
     * @throws Exception
     */
    public int applyRdVoiceguideDetailPid() throws Exception {
        return applyPid("RD_VOICEGUIDE_DETAIL");
    }

    /**
     * 申请IX_SAMEPOI
     *
     * @return
     * @throws Exception
     */
    public int applySamepoiPid() throws Exception {
        return applyPid("IX_SAMEPOI");
    }

    /**
     * 申请RD_HGWG_LIMIT
     *
     * @return
     * @throws Exception
     */
    public int applyRdHgwgLimitPid() throws Exception {
        return applyPid("RD_HGWG_LIMIT");
    }

}
