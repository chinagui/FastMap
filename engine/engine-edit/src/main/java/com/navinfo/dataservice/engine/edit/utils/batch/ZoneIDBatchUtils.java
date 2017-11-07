package com.navinfo.dataservice.engine.edit.utils.batch;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdmin;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFace;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkZone;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdAdminSelector;
import com.navinfo.dataservice.dao.glm.selector.ad.zone.ZoneFaceSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.edit.utils.GeoRelationUtils;
import com.vividsolutions.jts.geom.Geometry;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhangyt
 * @Title: ZoneIDBatchUtils.java
 * @Description: RdLink赋ZoneID
 * @date: 2016年8月17日 下午2:52:23
 * @version: v1.0
 */
public class ZoneIDBatchUtils extends BaseBatchUtils {

    private static Logger logger = LoggerRepos.getLogger(ZoneIDBatchUtils.class);

    public ZoneIDBatchUtils() {
    }

    /**
     * 在RdLink新增、修行时为link的regionId赋值
     *
     * @param link     新增、修形的link
     * @param geometry 修形后的geometry，新增时传入null
     * @param conn     数据库连接
     * @param result   结果集
     * @throws Exception
     */
    public static void updateZoneID(RdLink link, Geometry geometry, Connection conn, Result result) throws Exception {
        // 获取与link相关的ZoneFace
        Geometry linkGeometry = geometry == null ? transform(link.getGeometry()) : transform(geometry);
        List<ZoneFace> faces = loadZoneFace(conn, linkGeometry);

        for (IRow iRow : link.getZones()) {
            int linkRegionId = ((RdLinkZone) iRow).getRegionId();

            if (!hasMatchFace(faces, linkRegionId)) {
                result.insertObject(iRow, ObjStatus.DELETE, link.pid());
            }
        }

        AdAdminSelector selector = new AdAdminSelector(conn);
        for (ZoneFace face : faces) {
            int faceRegionId = face.getRegionId();

            double type = 0;
            try {
                type = ((AdAdmin)selector.loadById(faceRegionId, false)).getAdminType();
            } catch (Exception e) {
                logger.error(String.format("load admin error [regionId : %s]", faceRegionId));
                logger.error(e.getMessage(), e.fillInStackTrace());
            }

            Geometry faceGeometry = transform(face.getGeometry());

            excuteData(faceRegionId, type, faceGeometry, link, linkGeometry, result);
        }
    }

    /**
     * 判断是否存在linkRegionId对应的face
     * @param faces 所有关联面
     * @param linkRegionId 已存在的RdLinkZone的regionId
     * @return
     */
    private static boolean hasMatchFace(List<ZoneFace> faces, int linkRegionId) {
        for (ZoneFace face : faces) {
            int faceRegionId = face.getRegionId();
            if (faceRegionId == linkRegionId) {
                return true;
            }
        }
        return false;
    }

    /**
     * 筛选所有regionId=faceRegionId的RdLinkZone记录
     * @param zones link包含的所有RdLinkZone
     * @param faceRegionId 当前面的regionId
     * @return
     */
    private static List<IRow> alreadyExistsRegion(List<IRow> zones, int faceRegionId) {
        List<IRow> rows = new ArrayList<>();
        for (IRow iRow : zones) {
            int linkRegionId = ((RdLinkZone)iRow).getRegionId();
            if (linkRegionId == faceRegionId) {
                rows.add(iRow);
            }
        }
        return rows;
    }

    /**
     * face新增、修改、删除时维护面内属性
     *
     * @param face     修形前面几何
     * @param geometry 修形后的geometry（删除时传入null）
     * @param conn     数据库连接
     * @param result   结果集
     * @throws Exception
     */
    public static void updateZoneID(ZoneFace face, Geometry geometry, Connection conn, Result result) throws Exception {
        Geometry oldGeometry = transform(face.getGeometry());

        RdLinkSelector selector = new RdLinkSelector(conn);

        if (null == geometry) {
            List<RdLink> links = selector.loadLinkByFaceGeo(oldGeometry, true);
            for (RdLink link : links) {
                for (IRow iRow : alreadyExistsRegion(link.getZones(), face.getRegionId())) {
                    result.insertObject(iRow, ObjStatus.DELETE, link.pid());
                }
            }
        } else {
            int faceRegionId = face.getRegionId();

            double type = 0;
            try {
                type = ((AdAdmin)selector.loadById(faceRegionId, false)).getAdminType();
            } catch (Exception e) {
                logger.error(String.format("load admin error [regionId : %s]", faceRegionId));
                logger.error(e.getMessage(), e.fillInStackTrace());
            }

            Geometry newGeometry = transform(geometry);
            List<RdLink> links = selector.loadLinkByFaceGeo(newGeometry, true);

            for (RdLink link : links) {
                Geometry linkGeometry = transform(link.getGeometry());

                excuteData(faceRegionId, type, newGeometry, link, linkGeometry, result);


            }
        }
    }

    private static void excuteData(int faceRegionId, double type, Geometry newGeometry, RdLink link, Geometry linkGeometry, Result result) {
        List<IRow> existsRegion = alreadyExistsRegion(link.getZones(), faceRegionId);
        // 判断link与zoneFace的关系
        // link在zoneFace内部
        if (isContainOrCover(linkGeometry, newGeometry)) {
            if (!hasSide(existsRegion, 0)) {
                RdLinkZone leftZone = createLinkZoneWithType(link, faceRegionId, 0, type);
                result.insertObject(leftZone, ObjStatus.INSERT, link.pid());
            }
            if (!hasSide(existsRegion, 1)) {
                RdLinkZone rightZone = createLinkZoneWithType(link, faceRegionId, 1, type);
                result.insertObject(rightZone, ObjStatus.INSERT, link.pid());
            }

        // link在zoneFace组成线上
        } else if (GeoRelationUtils.Boundary(linkGeometry, newGeometry)) {
            // link在zoneFace的右边
            if (GeoRelationUtils.IsLinkOnLeftOfRing(linkGeometry, newGeometry)) {
                if (!hasSide(existsRegion, 1)) {
                    RdLinkZone rightZone = createLinkZoneWithType(link, faceRegionId, 1, type);
                    result.insertObject(rightZone, ObjStatus.INSERT, link.pid());
                }
            } else {
                if (!hasSide(existsRegion, 0)) {
                    RdLinkZone leftZone = createLinkZoneWithType(link, faceRegionId, 0, type);
                    result.insertObject(leftZone, ObjStatus.INSERT, link.pid());
                }

            }
        } else {
            for (IRow iRow : existsRegion) {
                result.insertObject(iRow, ObjStatus.DELETE, link.pid());
            }
        }
    }

    /**
     * 判断左、右侧是否已存在对应RdLinkZone记录
     * @param rows
     * @param side 0：左侧；1：右侧
     * @return
     */
    private static boolean hasSide(List<IRow> rows, int side) {
        for (IRow iRow : rows) {
            RdLinkZone zone = (RdLinkZone) iRow;
            if (side == zone.getSide()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 在线批处理删除ZoneFace内部的ZoneId
     *
     * @param link     ZoneFace内部的link
     * @param zoneFace 修形后的geometry，新增时传入null
     * @param conn     数据库连接
     * @param result   结果集
     * @throws Exception
     */

    public static void deleteZoneID(RdLink link, ZoneFace zoneFace, Connection conn, Result result) throws Exception {

        if (null == zoneFace) {

            return;
        }

        Geometry linkGeometry = transform(link.getGeometry());

        Geometry faceGeometry = transform(zoneFace.getGeometry());

        boolean isContainOrCover = isContainOrCover(linkGeometry, faceGeometry);

        boolean isBoundary = false;

        if (!isContainOrCover) {

            isBoundary = GeoRelationUtils.Boundary(linkGeometry, faceGeometry);
        }

        for (IRow row : link.getZones()) {

            RdLinkZone linkZone = (RdLinkZone) row;

            if (zoneFace.getRegionId() != linkZone.getRegionId()) {

                continue;
            }

            if (isContainOrCover || isBoundary) {

                result.insertObject(linkZone, ObjStatus.DELETE, linkZone.parentPKValue());
            }
        }
    }

    /**
     * 在线批处理ZoneFace内部的ZoneId
     */
    public static void setZoneID(RdLink link, Geometry faceGeo, int regionId, int type, Result result)
            throws Exception {

        Geometry linkGeometry = transform(link.getGeometry());

        boolean isBoundaryRight = false;//边界右

        boolean isBoundaryLeft = false;//边界左

        boolean isContainOrCover = isContainOrCover(linkGeometry, faceGeo);//面内

        if (!isContainOrCover && GeoRelationUtils.Boundary(linkGeometry, faceGeo)) {

            isBoundaryRight = GeoRelationUtils.IsLinkOnLeftOfRing(linkGeometry, faceGeo);

            isBoundaryLeft = !isBoundaryRight;
        }

        // 不在面内、不在边界左、不在边界右
        if (!isContainOrCover && !isBoundaryRight && !isBoundaryLeft) {

            return;
        }

        RdLinkZone linkZoneLeft = null;

        RdLinkZone linkZoneRight = null;

        for (IRow row : link.getZones()) {

            RdLinkZone linkZone = (RdLinkZone) row;

            if (regionId == linkZone.getRegionId() && type == linkZone.getType() && linkZone.getSide() == 0) {

                linkZoneLeft = linkZone;
            }
            if (regionId == linkZone.getRegionId() && type == linkZone.getType() && linkZone.getSide() == 1) {

                linkZoneRight = linkZone;
            }
        }

        //for (IRow row : link.getZones()) {
        //
        //    RdLinkZone linkZone = (RdLinkZone) row;
        //
        //    //不处理其他面关联的linkzone
        //    if (type != linkZone.getType() && regionId != linkZone.getRegionId()) {
        //
        //        continue;
        //    }
        //
        //    String rowId = linkZone.getRowId();
        //
        //    if (linkZoneLeft != null && rowId.equals(linkZoneLeft.getRowId()) && (isBoundaryLeft || isContainOrCover)) {
        //
        //        continue;
        //    }
        //
        //    if (linkZoneRight != null && rowId.equals(linkZoneRight.getRowId()) && (isBoundaryRight || isContainOrCover)) {
        //
        //        continue;
        //    }
        //
        //    result.insertObject(linkZone, ObjStatus.DELETE, linkZone.parentPKValue());
        //}

        if (linkZoneLeft == null && (isBoundaryLeft || isContainOrCover)) {

            linkZoneLeft = createLinkZone(link, regionId, 0);

            linkZoneLeft.setType(type);

            result.insertObject(linkZoneLeft, ObjStatus.INSERT, linkZoneLeft.parentPKValue());
        }

        if (linkZoneRight == null && (isBoundaryRight || isContainOrCover)) {

            linkZoneRight = createLinkZone(link, regionId, 1);

            linkZoneRight.setType(type);

            result.insertObject(linkZoneRight, ObjStatus.INSERT, linkZoneRight.parentPKValue());
        }
    }

    // 根据linkGeometry获取相关联的ZoneFace，没有时返回Null
    private static List<ZoneFace> loadZoneFace(Connection conn, Geometry linkGeometry) throws Exception {
        List<ZoneFace> faces = new ZoneFaceSelector(conn).loadRelateFaceByGeometry(linkGeometry);
        return faces;
    }

    // 创建linkZone对象并返回
    private static RdLinkZone createLinkZoneWithType(RdLink link, int faceRegionId, int side, double type) {
        RdLinkZone linkZone;
        linkZone = new RdLinkZone();
        linkZone.setLinkPid(link.pid());
        linkZone.setRegionId(faceRegionId);
        linkZone.setSide(side);
        if (type == 3) {
            linkZone.setType(3);
        } else if (type == 8) {
            linkZone.setType(2);
        } else if (type == 9) {
            linkZone.setType(1);
        }
        return linkZone;
    }


    // 创建linkZone对象并返回
    private static RdLinkZone createLinkZone(RdLink link, int faceRegionId, int side) {
        RdLinkZone linkZone;
        linkZone = new RdLinkZone();
        linkZone.setLinkPid(link.pid());
        linkZone.setRegionId(faceRegionId);
        linkZone.setSide(side);
        return linkZone;
    }

}
