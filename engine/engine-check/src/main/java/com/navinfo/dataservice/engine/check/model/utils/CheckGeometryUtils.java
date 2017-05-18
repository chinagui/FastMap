package com.navinfo.dataservice.engine.check.model.utils;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLink;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLinkKind;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneNode;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildlink;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildnode;
import com.navinfo.dataservice.dao.glm.model.lc.LcLink;
import com.navinfo.dataservice.dao.glm.model.lc.LcNode;
import com.navinfo.dataservice.dao.glm.model.lu.LuLink;
import com.navinfo.dataservice.dao.glm.model.lu.LuLinkKind;
import com.navinfo.dataservice.dao.glm.model.lu.LuNode;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLink;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwNode;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameLinkPart;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameNodePart;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.dataservice.dao.glm.selector.ad.zone.ZoneNodeSelector;
import com.vividsolutions.jts.geom.Geometry;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

/**
 * @Title: CheckGeometryUtils
 * @Package: com.navinfo.dataservice.engine.check.model.utils
 * @Description: ${TODO}
 * @Author: Crayeres
 * @Date: 05/09/17
 * @Version: V1.0
 */
public class CheckGeometryUtils {

    private CheckGeometryUtils(){
    }

    /**
     * 获取对象几何
     * @param row 对象
     * @return 对象几何
     */
    public static Geometry getGeometry(IRow row) {
        Geometry geometry = null;
        switch (row.objType()) {
            case LUNODE:
                geometry = ((LuNode) row).getGeometry(); break;
            case LULINK:
                geometry = ((LuLink) row).getGeometry(); break;
            case LCNODE:
                geometry = ((LcNode) row).getGeometry(); break;
            case LCLINK:
                geometry = ((LcLink) row).getGeometry(); break;
            case CMGBUILDNODE:
                geometry = ((CmgBuildnode) row).getGeometry(); break;
            case CMGBUILDLINK:
                geometry = ((CmgBuildlink) row).getGeometry(); break;
            case RWNODE:
                geometry = ((RwNode) row).getGeometry(); break;
            case RWLINK:
                geometry = ((RwLink) row).getGeometry(); break;
            case ADLINK:
                geometry = ((AdLink) row).getGeometry(); break;
            case ZONELINK:
                geometry = ((ZoneLink) row).getGeometry(); break;
            default:
                geometry = null;
        }
        return geometry;
    }

    /**
     * 判断集合不包含
     * @param objTypes 集合对象
     * @param objType 检查对象
     * @return 不包含时返回TRUE, 包含时返回FALSE
     */
    public static boolean notContains(List<ObjType> objTypes, ObjType objType) {
        return !objTypes.contains(objType);
    }

    /**
     * 获取同一点中的主点
     * @param parts 同一点信息
     * @param conn 数据库连接
     * @return 主点信息集合（获取失败时返回空集合）
     * @throws Exception 获取主点失败
     */
    public static List<RdSameNodePart> getMainNode(List<IRow> parts, Connection conn) throws Exception {
        List<RdSameNodePart> mainParts = new ArrayList<>();

        double minSeq = 0d;
        for (IRow row : parts) {
            RdSameNodePart part = (RdSameNodePart) row;

            double currentSeq = getNodeTableSeq(part.getTableName().toUpperCase(), part.getNodePid(), conn);

            if (0 == minSeq || currentSeq < minSeq) {
                minSeq = currentSeq;
                mainParts.clear();
                mainParts.add(part);
            } else if(currentSeq == minSeq) {
                mainParts.add(part);
            }
        }

        return mainParts;
    }

    /**
     * 获取同一点对象对应的优先级
     * @param tableName 对象表名称
     * @param nodePid 对象主键
     * @param conn 数据库连接
     * @return 同一点优先级
     * @throws Exception
     */
    private static double getNodeTableSeq(String tableName, int nodePid, Connection conn) throws Exception {
        double seq;
        switch (ReflectionAttrUtils.getObjTypeByTableName(tableName)) {
            case RDNODE: seq = 1d; break;
            case ADNODE: seq = 2d; break;
            case RWNODE: seq = 3d; break;
            case ZONENODE: seq = getZoneNodeTableSeq(nodePid, conn); break;
            case LUNODE: seq = 5d; break;
            default: seq = 0d;
        }

        return seq;
    }

    /**
     * 区分KDZONE/AOIZONE的优先级
     * @param nodePid 对象主键
     * @param conn 数据库连接
     * @return ZoneNode同一点优先级
     * @throws Exception
     */
    private static double getZoneNodeTableSeq(int nodePid, Connection conn) throws Exception {
        ZoneNode node = (ZoneNode) new ZoneNodeSelector(conn).loadById(nodePid, false);
        return node.getKind() == 3 ? 4.0d : 4.5d;
    }

    /**
     * 获取同一线中的主对象
     * @param parts 同一线对象
     * @param conn 数据库连接
     * @return 同一线主对象
     * @throws Exception
     */
    public static RdSameLinkPart getMainLink(List<IRow> parts, Connection conn) throws Exception {
        RdSameLinkPart mainPart = null;

        double minSeq = 0d;
        for (IRow row : parts) {
            RdSameLinkPart part = (RdSameLinkPart) row;

            double currentSeq = getLinkTableSeq(part.getTableName().toUpperCase(), part.getLinkPid(), conn);

            if (0 == minSeq || currentSeq < minSeq) {
                minSeq = currentSeq;
                mainPart = part;
            } else if(currentSeq == minSeq) {
                mainPart = part;
            }
        }

        return mainPart;
    }

    /**
     * 获取同一线对象对应的优先级
     * @param tableName 对象表名称
     * @param linkPid 对象主键
     * @param conn 数据库连接
     * @return 同一线对象优先级
     * @throws Exception
     */
    private static double getLinkTableSeq(String tableName, int linkPid, Connection conn) throws Exception {
        double seq;
        switch (ReflectionAttrUtils.getObjTypeByTableName(tableName)) {
            case RDLINK: seq = 1d; break;
            case ADLINK: seq = 2d; break;
            case RWLINK: seq = 3d; break;
            case ZONELINK: seq = getZoneLinkTableSeq(linkPid, conn); break;
            case LULINK: seq = getLuLinkTableSeq(linkPid, conn); break;
            default: seq = 0d;
        }

        return seq;
    }

    /**
     * 区分KDZONE/AOIZONE的优先级
     * @param linkPid 对象主键
     * @param conn 数据库连接
     * @return ZoneLink同一线优先级
     * @throws Exception
     */
    private static double getZoneLinkTableSeq(int linkPid, Connection conn) throws Exception {
        List<IRow> kinds = new AbstractSelector(ZoneLinkKind.class, conn).loadRowsByParentId(linkPid, false);
        for (IRow row : kinds) {
            ZoneLinkKind kind = (ZoneLinkKind) row;
            if (3 == kind.getKind()) {
                return 4.0d;
            }
        }
        return 4.5d;
    }

    /**
     * 区分BUA线与其他线的优先级
     * @param linkPid 对象主键
     * @param conn 数据库连接
     * @return LuLink同一线优先级
     * @throws Exception
     */
    private static double getLuLinkTableSeq(int linkPid, Connection conn) throws Exception {
        List<IRow> kinds = new AbstractSelector(LuLinkKind.class, conn).loadRowsByParentId(linkPid, false);
        for (IRow row : kinds) {
            LuLinkKind kind = (LuLinkKind) row;
            if (21 == kind.getKind()) {
                return 5.0d;
            }
        }
        return 5.5d;
    }

}
