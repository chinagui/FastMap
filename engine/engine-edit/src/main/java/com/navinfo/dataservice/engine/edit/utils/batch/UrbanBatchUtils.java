package com.navinfo.dataservice.engine.edit.utils.batch;

import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.lu.LuFace;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.lu.LuFaceSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.edit.utils.GeoRelationUtils;
import com.navinfo.navicommons.geo.computation.GeometryTypeName;
import com.vividsolutions.jts.geom.Geometry;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author zhangyt
 * @Title: LuFaceOpearteUtils.java
 * @Description: 用于更新RdLink的Urban属性
 * @date: 2016年8月16日 下午7:08:22
 * @version: v1.0
 */
public class UrbanBatchUtils extends BaseBatchUtils {

    private final static Integer IS_URBAN = 1;

    private final static Integer IS_NOT_URBAN = 0;

    public UrbanBatchUtils() {
    }

    /**
     * 更新link的urban字段
     *
     * @param link     更新urban字段的线
     * @param conn     数据库连接
     * @param geometry 修形时传入修形后的几何形状，新增时传入null
     * @throws Exception
     */
    // 目前仅处理RdLink新增时的urban属性
    public static void updateUrban(RdLink link, Geometry geometry, Connection conn, Result result) throws Exception {
        // 初始化Link的Urban值
        if (null == geometry) {
            link.setUrban(IS_NOT_URBAN);
        }
        // 将link几何缩小100000倍，根据link几何查找与之相关的BUA面
        Geometry linkGeometry = null == geometry ? shrink(link.getGeometry()) : shrink(geometry);
        // TODO 临时方案不处理长度大于4000的几何图形，后期以存储过程代替
        if (linkGeometry.getCoordinates().length > 200)
            return;
        List<LuFace> faces = new LuFaceSelector(conn).loadRelateFaceByGeometry(linkGeometry);
        // 如关联面数量为空或大于一暂不做处理
        if (faces.isEmpty()) {
            if (link.getUrban() != IS_NOT_URBAN) {
                link.changedFields().put("urban", IS_NOT_URBAN);
            }
            return;
        }
        // 取出与link关联的唯一面几何
        Geometry faceGeometry = null;
        if (1 == faces.size()) {
            faceGeometry = shrink(faces.get(0).getGeometry());
        } else {
            for (LuFace face : faces) {
                faceGeometry = shrink(face.getGeometry());
                Geometry intersection = linkGeometry.intersection(faceGeometry);
                if (!GeometryTypeName.POINT.equals(intersection.getGeometryType())) {
                    break;
                }
            }
        }
        // 判断link是否完全包含于该面
        if (GeoRelationUtils.Interior(linkGeometry, faceGeometry)) {
            if (null != geometry && link.getUrban() != IS_URBAN) {
                link.changedFields().put("urban", IS_URBAN);
            } else {
                link.setUrban(IS_URBAN);
            }
            // 判断link是否包含于面内并有一个端点处于面组成线上
        } else if (GeoRelationUtils.InteriorAnd1Intersection(linkGeometry, faceGeometry)) {
            // 判断是否起点处于ring组成线上
            if (isInBoundary(conn, link.getsNodePid(), faceGeometry, result)) {
                if (isSameNode(conn, result, link.getsNodePid()) && link.getUrban() != IS_URBAN) {
                    link.changedFields().put("urban", IS_URBAN);
                } else if (isMeshNode(conn, result, link.getsNodePid())) {
                    if (null != geometry && link.getUrban() != IS_URBAN) {
                        link.changedFields().put("urban", IS_URBAN);
                    } else {
                        link.setUrban(IS_URBAN);
                    }
                }
                // 判断是否终点处于ring组成线上
            } else if (isInBoundary(conn, link.geteNodePid(), faceGeometry, result))
                if (isSameNode(conn, result, link.geteNodePid()) && link.getUrban() != IS_URBAN) {
                    link.changedFields().put("urban", IS_URBAN);
                }  else if (isMeshNode(conn, result, link.geteNodePid())) {
                    if (null != geometry && link.getUrban() != IS_URBAN) {
                        link.changedFields().put("urban", IS_URBAN);
                    } else {
                        link.setUrban(IS_URBAN);
                    }
                }
            // 判断link是否包含于面内并且两个端点处于面组成线上
        } else if (GeoRelationUtils.InteriorAnd2Intersection(linkGeometry, faceGeometry)) {
            // 判断两个端点是否属于同一点
            if (isSameNode(conn, result, link.getsNodePid(), link.geteNodePid()) && link.getUrban() != IS_URBAN) {
                link.changedFields().put("urban", IS_URBAN);
            } else if (isMeshNode(conn, result, link.getsNodePid()) && isMeshNode(conn, result, link.geteNodePid())) {
                if (null != geometry && link.getUrban() != IS_URBAN) {
                    link.changedFields().put("urban", IS_URBAN);
                } else {
                    link.setUrban(IS_URBAN);
                }
            }
        } else {
            // 其余情况暂不作处理
        }
    }

    /**
     * 新增、修改Face时更新Link信息
     *
     * @param faceGeometry 修形前面几何(新增时与geometry传入同一对象）
     * @param geometry     修形后面几何(删除时传入null)
     * @param conn         数据库链接
     * @param result       结果集
     * @throws Exception
     */
    public static void updateUrban(Geometry faceGeometry, Geometry geometry, Connection conn, Result result) throws
            Exception {
        // TODO 临时方案不处理长度大于4000的几何图形，后期以存储过程代替
        if (faceGeometry.getCoordinates().length > 200)
            return;
        RdLinkSelector selector = new RdLinkSelector(conn);
        // 删除面时,原面内Link的Urban赋0
        if (null == geometry) {
            List<RdLink> links = selector.loadLinkByFaceGeo(faceGeometry, true);
            for (RdLink link : links) {
                if (link.getUrban() != IS_NOT_URBAN) {
                    link.changedFields().put("urban", IS_NOT_URBAN);
                    result.insertObject(link, ObjStatus.UPDATE, link.pid());
                }
            }
            return;
        }
        // TODO 临时方案不处理长度大于4000的几何图形，后期以存储过程代替
        if (geometry.getCoordinates().length > 200)
            return;
        List<RdLink> links = null;
        Map<Integer, RdLink> deleteMaps = new HashMap<>();
        // 修形面时,原几何内link的Urban赋0
        if (null != faceGeometry && null != geometry && !faceGeometry.difference(geometry).isEmpty()) {
            links = selector.loadLinkByFaceGeo(faceGeometry, true);
            for (RdLink link : links) {
                if (link.getUrban() != IS_NOT_URBAN) {
                    link.changedFields().put("urban", IS_NOT_URBAN);
                    deleteMaps.put(link.pid(), link);
                }
            }
        }

        Map<Integer, RdLink> addMaps = new HashMap<>();
        // 修形面时,新几何内link的Urban赋1
        links = selector.loadLinkByFaceGeo(geometry, true);
        for (RdLink link : links) {
            Geometry linkGeometry = shrink(link.getGeometry());
            // 判断link是否完全包含于该面
            if (GeoRelationUtils.Interior(linkGeometry, geometry)) {
                if (link.getUrban() != IS_URBAN) {
                    link.changedFields().put("urban", IS_URBAN);
                }
                addMaps.put(link.pid(), link);
                // 判断link是否包含于面内并有一个端点处于面组成线上
            } else if (GeoRelationUtils.InteriorAnd1Intersection(linkGeometry, geometry)) {
                // 判断是否起点处于ring组成线上
                if (isInBoundary(conn, link.getsNodePid(), geometry, result)) {
                    if (isSameNode(conn, result, link.getsNodePid())) {
                        if (link.getUrban() != IS_URBAN) {
                            link.changedFields().put("urban", IS_URBAN);
                        }
                        addMaps.put(link.pid(), link);
                    } else if (isMeshNode(conn, result, link.getsNodePid())) {
                        if (link.getUrban() != IS_URBAN) {
                            link.changedFields().put("urban", IS_URBAN);
                        }
                        addMaps.put(link.pid(), link);
                    }
                    // 判断是否终点处于ring组成线上
                } else if (isInBoundary(conn, link.geteNodePid(), geometry, result))
                    if (isSameNode(conn, result, link.geteNodePid())) {
                        if (link.getUrban() != IS_URBAN) {
                            link.changedFields().put("urban", IS_URBAN);
                        }
                        addMaps.put(link.pid(), link);
                    } else if (isMeshNode(conn, result, link.geteNodePid())) {
                        if (link.getUrban() != IS_URBAN) {
                            link.changedFields().put("urban", IS_URBAN);
                        }
                        addMaps.put(link.pid(), link);
                    }

                // 判断link是否包含于面内并且两个端点处于面组成线上
            } else if (GeoRelationUtils.InteriorAnd2Intersection(linkGeometry, geometry)) {
                // 判断两个端点是否属于同一点
                if (isSameNode(conn, result, link.getsNodePid(), link.geteNodePid())) {
                    if (link.getUrban() != IS_URBAN) {
                        link.changedFields().put("urban", IS_URBAN);
                    }
                    addMaps.put(link.pid(), link);
                } else if (isMeshNode(conn, result, link.getsNodePid()) && isMeshNode(conn, result, link.geteNodePid())) {
                    if (link.getUrban() != IS_URBAN) {
                        link.changedFields().put("urban", IS_URBAN);
                    }
                    addMaps.put(link.pid(), link);
                }
            } else {
                // 其余情况暂不作处理
            }
        }
        Set<Integer> addPid = addMaps.keySet();
        for (Entry<Integer, RdLink> entry : deleteMaps.entrySet()) {
            if (isDeleteLink(result, entry.getKey())) {
                continue;
            }

            if (addPid.contains((Object) entry.getKey())) {
                continue;
            }
            result.insertObject(entry.getValue(), ObjStatus.UPDATE, entry.getValue().pid());
        }

        for (RdLink link : addMaps.values()) {
            if (isDeleteLink(result, link.pid())) {
                continue;
            }

            if (link.getUrban() == IS_URBAN) {
                continue;
            }
            result.insertObject(link, ObjStatus.UPDATE, link.pid());
        }
    }
}
