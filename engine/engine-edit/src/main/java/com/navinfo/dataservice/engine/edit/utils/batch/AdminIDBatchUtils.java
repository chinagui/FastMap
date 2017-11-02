package com.navinfo.dataservice.engine.edit.utils.batch;

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
            Geometry linkGeometry = null == geometry ? transform(loadGeometry(row)) : transform(geometry);
            // TODO 临时方案不处理长度大于4000的几何图形，后期以存储过程代替
            if (linkGeometry.getCoordinates().length > 200)
                return;

            // 获取RdLink关联的AdFace
            AdFace face = loadAdFace(conn, linkGeometry);
            if (null == face) {
                if (link.getLeftRegionId() != 0)
                    link.changedFields().put("leftRegionId", 0);
                if (link.getRightRegionId() != 0)
                    link.changedFields().put("rightRegionId", 0);
                return;
            }
            // 获取AdFace的regionId
            Integer regionId = face.getRegionId();
            Geometry faceGeometry = transform(face.getGeometry());

            // 判断RdLink与AdFace的关系
            // RdLink包含在AdFace内
            if (isContainOrCover(linkGeometry, faceGeometry)) {
                // 新增时赋regionId
                if (null == geometry) {
                    link.setLeftRegionId(regionId);
                    link.setRightRegionId(regionId);
                } else {
                    // 修行时修改regionId
                    if (link.getLeftRegionId() != regionId) {
                        link.changedFields().put("leftRegionId", regionId);
                    }
                    if (link.getRightRegionId() != regionId) {
                        link.changedFields().put("rightRegionId", regionId);
                    }
                }
                // RdLink处在AdFace组成线上
            } else if (GeoRelationUtils.Boundary(linkGeometry, faceGeometry)) {
                // RdLink在AdFace的右边
                if (GeoRelationUtils.IsLinkOnLeftOfRing(linkGeometry, faceGeometry)) {
                    // 新增时为RightRegionId赋值
                    if (null == geometry) {
                        link.setRightRegionId(regionId);
                        // 修改时修改RightRegionId值
                    } else if (link.getRightRegionId() != regionId) {
                        link.changedFields().put("rightRegionId", regionId);
                    }
                } else {
                    // 新增时为LeftRegionId赋值
                    if (null == geometry) {
                        link.setLeftRegionId(regionId);
                        // 修改时修改LeftRegionId值
                    } else if (link.getLeftRegionId() != regionId) {
                        link.changedFields().put("leftRegionId", regionId);
                    }
                }
            } else {
                // 其他情况暂不处理
            }
        } else {
            Geometry g = transform(null == geometry ? loadGeometry(row) : geometry);
            // TODO 临时方案不处理长度大于4000的几何图形，后期以存储过程代替
            if (g.getCoordinates().length > 200)
                return;
            // 获取关联AdFace
            AdFace face = loadAdFace(conn, g);
            if (null == face)
                return;
            Geometry faceGeometry = transform(face.getGeometry());
            // 判断row是否处于AdFace内部
            if ("POINT".equalsIgnoreCase(g.getGeometryType())) {
                int faceRegionId = face.getRegionId();
                // 新增row时赋值
                if (null == geometry) {
                    setRegionId(row, faceRegionId);
                } else {
                    // 修形时修改regionId
                    row.changedFields().put("regionId", faceRegionId);
                }
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
                if (point.coveredBy(transform(face.getGeometry()))) {
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
        Geometry faceGeometry = transform(face.getGeometry());
        // TODO 临时方案不处理长度大于4000的几何图形，后期以存储过程代替
        if (faceGeometry.getCoordinates().length > 200)
            return;
        // 删除时将面内link的regionId清空
        if (null == geometry) {
            List<RdLink> links = selector.loadLinkByFaceGeo(faceGeometry, true);
            for (RdLink link : links) {
                if (link.getLeftRegionId() != 0)
                    link.changedFields().put("leftRegionId", 0);
                if (link.getRightRegionId() != 0)
                    link.changedFields().put("rightRegionId", 0);
                result.insertObject(link, ObjStatus.UPDATE, link.pid());
            }
            return;
        }
        // TODO 临时方案不处理长度大于4000的几何图形，后期以存储过程代替
        if (geometry.getCoordinates().length > 200)
            return;
        Map<Integer, RdLink> modified = new HashMap<>();
        geometry = transform(geometry);

        Geometry p1 = transform(faceGeometry.getCentroid());
        Geometry p2 = transform(geometry.getCentroid());
        if (p1.equals(p2))
            return;

        // 修形时对面内新增link赋regionId
        List<RdLink> links = selector.loadLinkByDiffGeo(geometry, faceGeometry, true);
        for (RdLink link : links) {
            int regionId = face.getRegionId();
            Geometry linkGeometry = transform(link.getGeometry());
            // 判断RdLink与AdFace的关系
            // RdLink包含在AdFace内
            if (isContainOrCover(linkGeometry, geometry)) {
                // 修行时修改regionId
                if (link.getLeftRegionId() != regionId) {
                    link.changedFields().put("leftRegionId", regionId);
                }
                if (link.getRightRegionId() != regionId) {
                    link.changedFields().put("rightRegionId", regionId);
                }
                modified.put(link.pid(), link);
                // RdLink处在AdFace组成线上
            } else if (GeoRelationUtils.Boundary(linkGeometry, geometry)) {
                // RdLink在AdFace的右边
                if (GeoRelationUtils.IsLinkOnLeftOfRing(linkGeometry, geometry)) {
                    // 修改时修改RightRegionId值
                    if (link.getRightRegionId() != regionId) {
                        link.changedFields().put("rightRegionId", regionId);
                        modified.put(link.pid(), link);
                    }
                } else {
                    // 修改时修改LeftRegionId值
                    if (link.getLeftRegionId() != regionId) {
                        link.changedFields().put("leftRegionId", regionId);
                        modified.put(link.pid(), link);
                    }
                }
            } else {
                // 其他情况暂不处理
            }
        }
        // 修形时删除原面内link的regionId
        if (null != faceGeometry) {
            links = selector.loadLinkByDiffGeo(faceGeometry, geometry, true);
            for (RdLink link : links) {
                if (link.getLeftRegionId() != 0) {
                    link.changedFields().put("leftRegionId", 0);
                }
                if (link.getRightRegionId() != 0) {
                    link.changedFields().put("rightRegionId", 0);
                }
                modified.put(link.pid(), link);
            }
        }
        for (RdLink link : modified.values()) {
            if (isDeleteLink(result, link.pid())) {
                continue;
            }

            result.insertObject(link, ObjStatus.UPDATE, link.pid());
        }
    }
}
