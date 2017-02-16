package com.navinfo.dataservice.engine.edit.utils.batch;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.vividsolutions.jts.geom.Point;
import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFace;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkZone;
import com.navinfo.dataservice.dao.glm.selector.ad.zone.ZoneFaceSelector;
import com.navinfo.dataservice.engine.edit.utils.GeoRelationUtils;
import com.vividsolutions.jts.geom.Geometry;

import static org.apache.hadoop.yarn.webapp.hamlet.HamletSpec.Scope.row;

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
        Geometry faceGeometry = shrink(zoneFace.getGeometry());
        // 判断link与zoneFace的关系
        // link在zoneFace内部
        if (isContainOrCover(linkGeometry, faceGeometry)) {
            // 新增或原link没有linkZone子数据时直接添加新的linkZone
            if (null == geometry || link.getZones().isEmpty()) {
                linkZone = createLinkZone(link, faceRegionId, 0);
                result.insertObject(linkZone, ObjStatus.INSERT, link.pid());
                linkZone = createLinkZone(link, faceRegionId, 1);
                result.insertObject(linkZone, ObjStatus.INSERT, link.pid());
            } else {
                int side = 0;
                // 修行时如果原有linkZone数据将原有regionId更新
                for (IRow row : link.getZones()) {
                    linkZone = (RdLinkZone) row;
                    side = linkZone.getSide();
                    if (faceRegionId != linkZone.getRegionId()) {
                        linkZone.changedFields().put("regionId", faceRegionId);
                        result.insertObject(linkZone, ObjStatus.UPDATE, linkZone.parentPKValue());
                    }
                }
                if (link.getZones().size() == 1) {
                    linkZone = createLinkZone(link, faceRegionId, 1 - side);
                    result.insertObject(linkZone, ObjStatus.INSERT, link.pid());
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
                    linkZone = createLinkZone(link, faceRegionId, 1);
                    result.insertObject(linkZone, ObjStatus.INSERT, linkZone.parentPKValue());
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
                    linkZone = createLinkZone(link, faceRegionId, 0);
                    result.insertObject(linkZone, ObjStatus.INSERT, linkZone.parentPKValue());
                }
            }
        } else {

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
        RdLinkSelector selector = new RdLinkSelector(conn);
        Geometry faceGeometry = GeoTranslator.transform(face.getGeometry(), 0.00001, 5);
        // 删除时将面内link的zone清空
        if (null == geometry) {
            List<RdLink> links = selector.loadLinkByFaceGeo(faceGeometry, true);
            for (RdLink link : links) {
                for (IRow row : link.getZones())
                    result.insertObject(row, ObjStatus.DELETE, row.parentPKValue());
            }
            return;
        }
        List<Integer> deleteLinkPids = new ArrayList<>();
        // 修形时对面内新增link赋zone属性
        geometry = GeoTranslator.transform(geometry, 0.00001, 5);
        List<RdLink> links = selector.loadLinkByDiffGeo(geometry, faceGeometry, true);
        for (RdLink link : links) {
            if (deleteLinkPids.contains(link.pid()))
                link.getZones().clear();
            Geometry linkGeometry = GeoTranslator.transform(link.getGeometry(), 0.00001, 5);
            RdLinkZone linkZone = null;
            // 获取关联face的regionId
            int faceRegionId = face.getRegionId();
            // 判断link与zoneFace的关系
            // link在zoneFace内部
            if (isContainOrCover(linkGeometry, geometry)) {
                // 新增或原link没有linkZone子数据时直接添加新的linkZone
                if (link.getZones().isEmpty()) {
                    linkZone = createLinkZone(link, faceRegionId, 0);
                    result.insertObject(linkZone, ObjStatus.INSERT, link.pid());
                    linkZone = createLinkZone(link, faceRegionId, 1);
                    result.insertObject(linkZone, ObjStatus.INSERT, link.pid());
                } else {
                    int side = 0;
                    // 修行时如果原有linkZone数据将原有regionId更新
                    for (IRow row : link.getZones()) {
                        linkZone = (RdLinkZone) row;
                        side = linkZone.getSide();
                        if (faceRegionId != linkZone.getRegionId()) {
                            linkZone.changedFields().put("regionId", faceRegionId);
                            result.insertObject(linkZone, ObjStatus.UPDATE, linkZone.parentPKValue());
                        }
                    }
                    if (link.getZones().size() == 1) {
                        linkZone = createLinkZone(link, faceRegionId, 1 - side);
                        result.insertObject(linkZone, ObjStatus.INSERT, link.pid());
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
                        linkZone = createLinkZone(link, faceRegionId, 1);
                        result.insertObject(linkZone, ObjStatus.INSERT, linkZone.parentPKValue());
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
                        linkZone = createLinkZone(link, faceRegionId, 0);
                        result.insertObject(linkZone, ObjStatus.INSERT, linkZone.parentPKValue());
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
        Geometry linkGeometry = shrink(link.getGeometry());
        RdLinkZone linkZone = null;
        if (null == zoneFace)
            return;
        // 获取关联face的regionId
        int faceRegionId = zoneFace.getRegionId();
        Geometry faceGeometry = shrink(zoneFace.getGeometry());
        // 判断link与zoneFace的关系
        // link在zoneFace内部
        if (isContainOrCover(linkGeometry, faceGeometry)) {
            if (CollectionUtils.isNotEmpty(link.getZones())) {
                // 修行时如果原有linkZone数据将原有regionId更新
                for (IRow row : link.getZones()) {
                    linkZone = (RdLinkZone) row;
                    if (faceRegionId == linkZone.getRegionId()) {
                        result.insertObject(linkZone, ObjStatus.DELETE, linkZone.parentPKValue());
                    }
                }
            }
            // link在zoneFace组成线上
        } else if (GeoRelationUtils.Boundary(linkGeometry, faceGeometry)) {
            // 不存在该regionId的linkZone数据时新增一条
            for (IRow row : link.getZones()) {
                linkZone = (RdLinkZone) row;
                if (linkZone.getRegionId() == faceRegionId) {
                    result.insertObject(linkZone, ObjStatus.DELETE, linkZone.parentPKValue());
                }
            }
        }
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
    public static void setZoneID(RdLink link, ZoneFace zoneFace, Connection conn, Result result) throws Exception {
        Geometry linkGeometry = shrink(link.getGeometry());
        RdLinkZone linkZone = null;
        if (null == zoneFace)
            return;
        // 获取关联face的regionId
        int faceRegionId = zoneFace.getRegionId();
        Geometry faceGeometry = shrink(zoneFace.getGeometry());
        // 判断link与zoneFace的关系
        // link在zoneFace内部
        if (isContainOrCover(linkGeometry, faceGeometry)) {
            // 新增或原link没有linkZone子数据时直接添加新的linkZone
            if (link.getZones().isEmpty()) {
                linkZone = createLinkZone(link, faceRegionId, 1);
                result.insertObject(linkZone, ObjStatus.INSERT, linkZone.parentPKValue());
            } else {
                // 修行时如果原有linkZone数据将原有regionId更新
                for (IRow row : link.getZones()) {
                    linkZone = (RdLinkZone) row;
                    if (faceRegionId != linkZone.getRegionId()) {
                        linkZone.changedFields().put("regionId", faceRegionId);
                        result.insertObject(linkZone, ObjStatus.UPDATE, linkZone.parentPKValue());
                    }
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
                    linkZone = createLinkZone(link, faceRegionId, 1);
                    result.insertObject(linkZone, ObjStatus.INSERT, linkZone.parentPKValue());
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
                    linkZone = createLinkZone(link, faceRegionId, 0);
                    result.insertObject(linkZone, ObjStatus.INSERT, linkZone.parentPKValue());
                }
            }
        } else {

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
                Geometry geo = GeoTranslator.transform(face.getGeometry(), 0.00001, 5);
                if (point.coveredBy(geo)) {
                    return face;
                }
            }
        }
        ZoneFace zoneFace = faces.get(0);
        return zoneFace;
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
