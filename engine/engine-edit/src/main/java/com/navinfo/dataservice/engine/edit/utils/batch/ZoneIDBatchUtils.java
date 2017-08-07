package com.navinfo.dataservice.engine.edit.utils.batch;

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
import com.vividsolutions.jts.geom.Point;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author zhangyt
 * @Title: ZoneIDBatchUtils.java
 * @Description: RdLink赋ZoneID
 * @date: 2016年8月17日 下午2:52:23
 * @version: v1.0
 */
public class ZoneIDBatchUtils extends BaseBatchUtils {

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
        Geometry linkGeometry = geometry == null ? shrink(link.getGeometry()) : shrink(geometry);
        // TODO 临时方案不处理长度大于4000的几何图形，后期以存储过程代替
        if (linkGeometry.getCoordinates().length > 200)
            return;
        RdLinkZone linkZone = null;
        // 获取与link相关的ZoneFace
        ZoneFace zoneFace = loadZoneFace(conn, linkGeometry);
        if (null == zoneFace) {
            for (IRow row : link.getZones())
                result.insertObject(row, ObjStatus.DELETE, row.parentPKValue());
            return;
        }
        // 获取关联face的regionId
        int faceRegionId = zoneFace.getRegionId();
        double type = 0;
        try {
            type = ((AdAdmin) new AdAdminSelector(conn).loadById(faceRegionId, false)).getAdminType();
        } catch (Exception e) {
        }

        List<RdLinkZone> additional = new ArrayList<>();
        List<RdLinkZone> modified = new ArrayList<>();

        Geometry faceGeometry = shrink(zoneFace.getGeometry());
        // 判断link与zoneFace的关系
        // link在zoneFace内部
        if (isContainOrCover(linkGeometry, faceGeometry)) {
            // 新增或原link没有linkZone子数据时直接添加新的linkZone
            if (null == geometry || link.getZones().isEmpty()) {
                linkZone = createLinkZoneWithType(link, faceRegionId, 0, type);
                //result.insertObject(linkZone, ObjStatus.INSERT, link.pid());
                additional.add(linkZone);
                linkZone = createLinkZoneWithType(link, faceRegionId, 1, type);
                //result.insertObject(linkZone, ObjStatus.INSERT, link.pid());
                additional.add(linkZone);
            } else {
                // 修行时如果原有linkZone数据将原有regionId更新
                for (IRow row : link.getZones()) {
                    linkZone = (RdLinkZone) row;
                    if (faceRegionId != linkZone.getRegionId()) {
                        linkZone.changedFields().put("regionId", faceRegionId);
                        //result.insertObject(linkZone, ObjStatus.UPDATE, linkZone.parentPKValue());
                        modified.add(linkZone);
                    }
                }
                if (link.getZones().size() == 1) {
                    int existingSide = ((RdLinkZone) link.getZones()).getSide();
                    linkZone = createLinkZoneWithType(link, faceRegionId, ~existingSide, type);
                    //result.insertObject(linkZone, ObjStatus.INSERT, link.pid());
                    additional.add(linkZone);
                }
            }
            // link在zoneFace组成线上
        } else if (GeoRelationUtils.Boundary(linkGeometry, faceGeometry)) {
            // link在zoneFace的右边
            if (GeoRelationUtils.IsLinkOnLeftOfRing(linkGeometry, faceGeometry)) {
                // 不存在该regionId的linkZone数据时新增一条
                boolean isCreate = true;
                for (IRow row : link.getZones()) {
                    linkZone = (RdLinkZone) row;
                    if (linkZone.getRegionId() == faceRegionId && linkZone.getSide() == 1) {
                        isCreate = false;
                    }
                }
                if (isCreate) {
                    linkZone = createLinkZoneWithType(link, faceRegionId, 1, type);
                    //result.insertObject(linkZone, ObjStatus.INSERT, linkZone.parentPKValue());
                    additional.add(linkZone);
                }
            } else {
                // link在zoneFace的左边
                // 不存在该regionId的linkZone数据时新增一条
                boolean isCreate = true;
                for (IRow row : link.getZones()) {
                    linkZone = (RdLinkZone) row;
                    if (linkZone.getRegionId() != faceRegionId && linkZone.getSide() == 0) {
                        isCreate = false;
                    }
                }
                if (isCreate) {
                    linkZone = createLinkZoneWithType(link, faceRegionId, 0, type);
                    //result.insertObject(linkZone, ObjStatus.INSERT, linkZone.parentPKValue());
                    additional.add(linkZone);
                }
            }
        } else {

        }

        executeResult(result, additional, modified);
    }

    /**
     * 将RDLINKZONE操作结果加入结果集
     *
     * @param result     结果集
     * @param additional 需要新增的RDLINKZONE
     * @param modified   需要修改的RDLINKZONE
     */
    private static void executeResult(Result result, List<RdLinkZone> additional, List<RdLinkZone> modified) {
        Iterator<RdLinkZone> additionalIterator = additional.iterator();
        Iterator<RdLinkZone> modifiedIterator = modified.iterator();

        while (additionalIterator.hasNext()) {
            RdLinkZone zone = additionalIterator.next();
            if (isDeleteLink(result, zone.getLinkPid())) {
                continue;
            }
            result.insertObject(zone, ObjStatus.INSERT, zone.getLinkPid());
        }
        while (modifiedIterator.hasNext()) {
            RdLinkZone zone = modifiedIterator.next();
            if (isDeleteLink(result, zone.getLinkPid())) {
                continue;
            }
            result.insertObject(zone, ObjStatus.UPDATE, zone.getLinkPid());
        }
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
        // TODO 临时方案不处理长度大于4000的几何图形，后期以存储过程代替
        if (face.getGeometry().getCoordinates().length > 200)
            return;
        RdLinkSelector selector = new RdLinkSelector(conn);
        Geometry faceGeometry = shrink(face.getGeometry());
        // 删除时将面内link的zone清空
        if (null == geometry) {
            List<RdLink> links = selector.loadLinkByFaceGeo(faceGeometry, true);
            for (RdLink link : links) {
                for (IRow row : link.getZones())
                    result.insertObject(row, ObjStatus.DELETE, row.parentPKValue());
            }
            return;
        }
        // TODO 临时方案不处理长度大于4000的几何图形，后期以存储过程代替
        if (geometry.getCoordinates().length > 200)
            return;

        List<RdLinkZone> additional = new ArrayList<>();
        List<RdLinkZone> modified = new ArrayList<>();

        List<Integer> deleteLinkPids = new ArrayList<>();
        // 修形时对面内新增link赋zone属性
        geometry = shrink(geometry);
        List<RdLink> links = selector.loadLinkByDiffGeo(geometry, faceGeometry, true);
        for (RdLink link : links) {
            if (deleteLinkPids.contains(link.pid()))
                link.getZones().clear();
            Geometry linkGeometry = shrink(link.getGeometry());
            RdLinkZone linkZone = null;
            // 获取关联face的regionId
            int faceRegionId = 0;
            double type = 0;
            try {
                if (0 != faceRegionId && faceRegionId != face.getRegionId())
                    type = ((AdAdmin) new AdAdminSelector(conn).loadById(faceRegionId, false)).getAdminType();
            } catch (Exception e) {
            }
            faceRegionId = face.getRegionId();
            // 判断link与zoneFace的关系
            // link在zoneFace内部
            if (isContainOrCover(linkGeometry, geometry)) {
                // 新增或原link没有linkZone子数据时直接添加新的linkZone
                if (link.getZones().isEmpty()) {
                    linkZone = createLinkZoneWithType(link, faceRegionId, 0, type);
                    additional.add(linkZone);
                    linkZone = createLinkZoneWithType(link, faceRegionId, 1, type);
                    additional.add(linkZone);
                } else {
                    // 修行时如果原有linkZone数据将原有regionId更新
                    for (IRow row : link.getZones()) {
                        linkZone = (RdLinkZone) row;
                        if (faceRegionId != linkZone.getRegionId()) {
                            linkZone.changedFields().put("regionId", faceRegionId);
                            modified.add(linkZone);
                        }
                    }
                    if (link.getZones().size() == 1) {
                        int existingSide = ((RdLinkZone) link.getZones()).getSide();
                        linkZone = createLinkZoneWithType(link, faceRegionId, ~existingSide, type);
                        additional.add(linkZone);
                    }
                }
                // link在zoneFace组成线上
            } else if (GeoRelationUtils.Boundary(linkGeometry, geometry)) {
                // link在zoneFace的右边
                if (GeoRelationUtils.IsLinkOnLeftOfRing(linkGeometry, geometry)) {
                    // 不存在该regionId的linkZone数据时新增一条
                    boolean isCreate = true;
                    for (IRow row : link.getZones()) {
                        linkZone = (RdLinkZone) row;
                        if (linkZone.getRegionId() == faceRegionId && linkZone.getSide() == 1) {
                            isCreate = false;
                        }
                    }
                    if (isCreate) {
                        linkZone = createLinkZoneWithType(link, faceRegionId, 1, type);
                        additional.add(linkZone);
                    }
                } else {
                    // link在zoneFace的左边
                    // 不存在该regionId的linkZone数据时新增一条
                    boolean isCreate = true;
                    for (IRow row : link.getZones()) {
                        linkZone = (RdLinkZone) row;
                        if (linkZone.getRegionId() != faceRegionId && linkZone.getSide() == 0) {
                            isCreate = false;
                        }
                    }
                    if (isCreate) {
                        linkZone = createLinkZoneWithType(link, faceRegionId, 0, type);
                        additional.add(linkZone);
                    }
                }
            } else {
            }
        }
        // 修形时删除原面内zone属性
        if (null != faceGeometry) {
            links = selector.loadLinkByDiffGeo(faceGeometry, geometry, true);
            for (RdLink link : links) {
                Iterator<IRow> iterator = link.getZones().iterator();
                while (iterator.hasNext()) {
                    IRow row = iterator.next();
                    result.insertObject(row, ObjStatus.DELETE, row.parentPKValue());
                }
                deleteLinkPids.add(link.pid());
            }
        }

        executeResult(result, additional, modified);
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

        Geometry linkGeometry = shrink(link.getGeometry());

        Geometry faceGeometry = shrink(zoneFace.getGeometry());

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

        Geometry linkGeometry = shrink(link.getGeometry());

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

        for (IRow row : link.getZones()) {

            RdLinkZone linkZone = (RdLinkZone) row;

            //不处理其他面关联的linkzone
            if (type != linkZone.getType() && regionId != linkZone.getRegionId()) {

                continue;
            }

            String rowId = linkZone.getRowId();

            if (linkZoneLeft != null && rowId.equals(linkZoneLeft.getRowId()) && (isBoundaryLeft || isContainOrCover)) {

                continue;
            }

            if (linkZoneRight != null && rowId.equals(linkZoneRight.getRowId()) && (isBoundaryRight || isContainOrCover)) {

                continue;
            }

            result.insertObject(linkZone, ObjStatus.DELETE, linkZone.parentPKValue());
        }

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
    private static ZoneFace loadZoneFace(Connection conn, Geometry linkGeometry) throws Exception {
        List<ZoneFace> faces = new ZoneFaceSelector(conn).loadRelateFaceByGeometry(linkGeometry);
        if (faces.isEmpty())
            return null;
        if (faces.size() > 1) {
            Point point = linkGeometry.getCentroid();
            for (ZoneFace face : faces) {
                if (point.coveredBy(shrink(face.getGeometry()))) {
                    return face;
                }
            }
        }
        ZoneFace zoneFace = faces.get(0);
        return zoneFace;
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
