package com.navinfo.dataservice.engine.edit.utils.batch;

import java.sql.Connection;
import java.util.List;

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
        if (null == zoneFace)
            return;
        // 获取关联face的regionId
        int faceRegionId = zoneFace.getRegionId();
        Geometry faceGeometry = shrink(zoneFace.getGeometry());
        // 判断link与zoneFace的关系
        // link在zoneFace内部
        if (isContainOrCover(linkGeometry, faceGeometry)) {
            // 新增或原link没有linkZone子数据时直接添加新的linkZone
            if (null == geometry || link.getZones().isEmpty()) {
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
        if (faces.isEmpty() || faces.size() > 1) {
            return null;
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
