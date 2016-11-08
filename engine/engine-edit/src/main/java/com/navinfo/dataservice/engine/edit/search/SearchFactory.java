package com.navinfo.dataservice.engine.edit.search;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.search.*;

/**
 * 查询工厂
 */
public class SearchFactory {

    private Connection conn;

    public SearchFactory(Connection conn) {
        this.conn = conn;
    }

    /**
     * 创建查询类
     *
     * @param ot 对象类型
     * @return
     */
    public ISearch createSearch(ObjType ot) {

        switch (ot) {
            case RDLINK:
                return new RdLinkSearch(conn);
            case RDRESTRICTION:
                return new RdRestrictionSearch(conn);
            case RDCROSS:
                return new RdCrossSearch(conn);
            case RDNODE:
                return new RdNodeSearch(conn);
            case RDLANECONNEXITY:
                return new RdLaneConnexitySearch(conn);
            case RDSPEEDLIMIT:
                return new RdSpeedlimitSearch(conn);
            case RDLINKSPEEDLIMIT:
                return new RdLinkSpeedLimitSearch(conn);
            case RDBRANCH:
                return new RdBranchSearch(conn);
            case RDLINKINTRTIC:
                return new RdLinkIntRticSearch(conn);
            case RDLINKRTIC:
                return new RdLinkRticSearch(conn);
            case RDGSC:
                return new RdGscSearch(conn);
            case ADLINK:
                return new AdLinkSearch(conn);
            case ADFACE:
                return new AdFaceSearch(conn);
            case ADNODE:
                return new AdNodeSearch(conn);
            case RWLINK:
                return new RwLinkSearch(conn);
            case RWNODE:
                return new RwNodeSearch(conn);
            case ADADMIN:
                return new AdAdminSearch(conn);
            case IXPOI:
                return new IxPoiSearch(conn);
            case ZONENODE:
                return new ZoneNodeSearch(conn);
            case ZONELINK:
                return new ZoneLinkSearch(conn);
            case ZONEFACE:
                return new ZoneFaceSearch(conn);
            case LUNODE:
                return new LuNodeSearch(conn);
            case LULINK:
                return new LuLinkSearch(conn);
            case LUFACE:
                return new LuFaceSearch(conn);
            case RDTRAFFICSIGNAL:
                return new RdTrafficsignalSearch(conn);
            case RDELECTRONICEYE:
                return new RdElectroniceyeSearch(conn);
            case RDWARNINGINFO:
                return new RdWarninginfoSearch(conn);
            case RDSLOPE:
                return new RdSlopeSearch(conn);
            case RDGATE:
                return new RdGateSearch(conn);
            case RDINTER:
                return new RdInterSearch(conn);
            case LCNODE:
                return new LcNodeSearch(conn);
            case LCLINK:
                return new LcLinkSearch(conn);
            case LCFACE:
                return new LcFaceSearch(conn);
            case RDSE:
                return new RdSeSearch(conn);
            case RDSPEEDBUMP:
                return new RdSpeedbumpSearch(conn);
            case RDSAMENODE:
                return new RdSameNodeSearch(conn);
            case RDSAMELINK:
                return new RdSameLinkSearch(conn);
            case RDDIRECTROUTE:
                return new RdDirectrouteSearch(conn);
            case RDTOLLGATE:
                return new RdTollgateSearch(conn);
            case RDOBJECT:
                return new RdObjectSearch(conn);
            case RDROAD:
                return new RdRoadSearch(conn);
            case RDVOICEGUIDE:
                return new RdVoiceguideSearch(conn);
            case RDVARIABLESPEED:
                return new RdVariableSpeedSearch(conn);
            case RDLANE:
                return new RdLaneSearch(conn);
            case IXSAMEPOI:
                return new IxSamepoiSearch(conn);
            case RDHGWGLIMIT:
                return new RdHgwgLimitSearch(conn);
            default:
                return null;
        }
    }
}
