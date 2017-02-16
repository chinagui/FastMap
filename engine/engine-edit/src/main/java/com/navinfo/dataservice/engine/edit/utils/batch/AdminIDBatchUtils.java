package com.navinfo.dataservice.engine.edit.utils.batch;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdFaceSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.edit.utils.GeoRelationUtils;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhangyt
 * @Title: AdminIDBatchUtils.java
 * @Description: RdLink赋RegionId
 * @date: 2016年8月17日 下午2:52:23
 * @version: v1.0
 */
public class AdminIDBatchUtils extends BaseBatchUtils {

    public AdminIDBatchUtils() {
    }

    /**
     * 新增RdLink、POI等赋RegionId
     *
     * @param row      新增数据
     * @param geometry 修形后的几何，如新增则传入null
     * @param conn     数据库连接
     * @throws Exception
     */
    public static void updateAdminID(IRow row, Geometry geometry, Connection conn) throws Exception {
        if (row instanceof RdLink) {
            RdLink link = (RdLink) row;
            Geometry linkGeometry = null == geometry ? shrink(loadGeometry(row)) : shrink(geometry);
            // 获取RdLink关联的AdFace
            AdFace face = loadAdFace(conn, linkGeometry);
            if (null == face) {
                link.changedFields().put("leftRegionId", 0);
                link.changedFields().put("rightRegionId", 0);
                return;
            }
            // 获取AdFace的regionId
            Integer regionId = face.getRegionId();
            Geometry faceGeometry = shrink(face.getGeometry());

            // 判断RdLink与AdFace的关系
            // RdLink包含在AdFace内
            if (isContainOrCover(linkGeometry, faceGeometry)) {
                // 新增时赋regionId
                if (null == geometry) {
                    link.setLeftRegionId(regionId);
                    link.setRightRegionId(regionId);
                } else {
                    // 修行时修改regionId
                    if (link.getLeftRegionId() != regionId)
                        link.changedFields().put("leftRegionId", regionId);
                    if (link.getRightRegionId() != regionId)
                        link.changedFields().put("rightRegionId", regionId);
                }
                // RdLink处在AdFace组成线上
            } else if (GeoRelationUtils.Boundary(linkGeometry, faceGeometry)) {
                // RdLink在AdFace的右边
                if (GeoRelationUtils.IsLinkOnLeftOfRing(linkGeometry, faceGeometry)) {
                    // 新增时为RightRegionId赋值
                    if (null == geometry)
                        link.setRightRegionId(regionId);
                        // 修改时修改RightRegionId值
                    else if (link.getRightRegionId() != regionId)
                        link.changedFields().put("rightRegionId", regionId);
                } else {
                    // 新增时为LeftRegionId赋值
                    if (null == geometry)
                        link.setLeftRegionId(regionId);
                        // 修改时修改LeftRegionId值
                    else if (link.getLeftRegionId() != regionId)
                        link.changedFields().put("leftRegionId", regionId);
                }
            } else {
                // 其他情况暂不处理
            }
        } else {
            Geometry g = shrink(null == geometry ? loadGeometry(row) : geometry);
            // 获取关联AdFace
            AdFace face = loadAdFace(conn, g);
            if (null == face)
                return;
            Geometry faceGeometry = shrink(face.getGeometry());
            // 判断row是否处于AdFace内部
            if ("POINT".equalsIgnoreCase(g.getGeometryType())) {
                int faceRegionId = face.getRegionId();
                // 新增row时赋值
                if (null == geometry)
                    setRegionId(row, faceRegionId);
                else
                    // 修形时修改regionId
                    row.changedFields().put("regionId", faceRegionId);
            }
        }
    }

    // 获取AdFace，没有关联Face时返回Null
    private static AdFace loadAdFace(Connection conn, Geometry linkGeometry) throws Exception {
        List<AdFace> faces = new AdFaceSelector(conn).loadRelateFaceByGeometry(linkGeometry);
        if (faces.isEmpty())
            return null;
        if (faces.size() > 1) {
            Point point = linkGeometry.getCentroid();
            for (AdFace face : faces) {
                Geometry geo = GeoTranslator.transform(face.getGeometry(), 0.00001, 5);
                if (point.coveredBy(geo)) {
                    return face;
                }
            }
        }

        AdFace adFace = faces.get(0);
        return adFace;
    }

    /**
     * face新增、修改、删除时维护面内link属性
     *
     * @param face     修形前面几何
     * @param geometry 修形后面几何(删除时传入null)
     * @param conn     数据库链接
     * @throws Exception
     */
    public static void updateAdminID(AdFace face, Geometry geometry, Connection conn, Result result) throws Exception {
        RdLinkSelector selector = new RdLinkSelector(conn);
        Geometry faceGeometry = GeoTranslator.transform(face.getGeometry(), 0.00001, 5);
        // 删除时将面内link的regionId清空
        if (null == geometry) {
            List<RdLink> links = selector.loadLinkByFaceGeo(faceGeometry, true);
            for (RdLink link : links) {
                link.changedFields().put("leftRegionId", 0);
                link.changedFields().put("rightRegionId", 0);
                result.insertObject(link, ObjStatus.UPDATE, link.pid());
            }
            return;
        }
        Map<Integer, RdLink> maps = new HashMap<>();
        geometry = GeoTranslator.transform(geometry, 0.00001, 5);

        Geometry p1 = GeoTranslator.transform(faceGeometry.getCentroid(), 0.00001, 5);
        Geometry p2 = GeoTranslator.transform(geometry.getCentroid(), 0.00001, 5);
        if (p1.equals(p2))
            return;

        // 修形时对面内新增link赋regionId
        List<RdLink> links = selector.loadLinkByDiffGeo(geometry, faceGeometry, true);
        for (RdLink link : links) {
            int regionId = face.getRegionId();
            Geometry linkGeometry = GeoTranslator.transform(link.getGeometry(), 0.00001, 5);
            // 判断RdLink与AdFace的关系
            // RdLink包含在AdFace内
            if (isContainOrCover(linkGeometry, geometry)) {
                // 修行时修改regionId
                link.changedFields().put("leftRegionId", regionId);
                link.changedFields().put("rightRegionId", regionId);
                // RdLink处在AdFace组成线上
            } else if (GeoRelationUtils.Boundary(linkGeometry, geometry)) {
                // RdLink在AdFace的右边
                if (GeoRelationUtils.IsLinkOnLeftOfRing(linkGeometry, geometry)) {
                    // 修改时修改RightRegionId值
                    link.changedFields().put("rightRegionId", regionId);
                } else {
                    // 修改时修改LeftRegionId值
                    link.changedFields().put("leftRegionId", regionId);
                }
            } else {
                // 其他情况暂不处理
            }
            maps.put(link.pid(), link);
        }
        // 修形时删除原面内link的regionId
        if (null != faceGeometry) {
            links = selector.loadLinkByDiffGeo(faceGeometry, geometry, true);
            for (RdLink link : links) {
                link.changedFields().put("leftRegionId", 0);
                link.changedFields().put("rightRegionId", 0);
                maps.put(link.pid(), link);
            }
        }
        for (RdLink link : maps.values()) {
            result.insertObject(link, ObjStatus.UPDATE, link.pid());
        }
    }
}
