package com.navinfo.dataservice.engine.edit.utils.batch;

import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.lu.LuFace;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.lu.LuFaceSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.edit.utils.GeoRelationUtils;
import com.vividsolutions.jts.geom.Geometry;
import org.apache.commons.collections.CollectionUtils;
import org.json.JSONException;

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
    public static void updateUrban(RdLink link, Geometry geometry, Connection conn, Result result) throws Exception {
        // 初始化Link的Urban值
        if (null == geometry) {
            link.setUrban(IS_NOT_URBAN);
        }
        // 将link几何缩小100000倍，根据link几何查找与之相关的BUA面
        Geometry linkGeometry = null == geometry ? transform(link.getGeometry()) : transform(geometry);
        List<LuFace> faces = new LuFaceSelector(conn).loadRelateFaceByGeometry(linkGeometry);
        // 如关联面数量为空或大于一暂不做处理
        if (CollectionUtils.isEmpty(faces)) {
            link.changedFields().put("urban", IS_NOT_URBAN);
        } else {
            // 取出与link关联的唯一面几何
            for (LuFace face : faces) {
                Geometry faceGeometry = transform(face.getGeometry());

                // 判断link是否完全包含于该面
                if (GeoRelationUtils.Interior(linkGeometry, faceGeometry)) {
                    if (null != geometry && link.getUrban() != IS_URBAN) {
                        link.changedFields().put("urban", IS_URBAN);
                    } else {
                        link.setUrban(IS_URBAN);
                    }
                    break;
                    // 判断link是否包含于面内并有一个端点处于面组成线上
                } else if (GeoRelationUtils.InteriorAnd1Intersection(linkGeometry, faceGeometry)) {
                    // 判断是否起点处于ring组成线上
                    if (isInBoundary(conn, link.getsNodePid(), faceGeometry, result)) {
                        if (isSameNode(conn, result, link.getsNodePid()) && link.getUrban() != IS_URBAN) {
                            link.changedFields().put("urban", IS_URBAN);
                            break;
                        } else if (isMeshNode(conn, result, link.getsNodePid())) {
                            if (null != geometry && link.getUrban() != IS_URBAN) {
                                link.changedFields().put("urban", IS_URBAN);
                            } else {
                                link.setUrban(IS_URBAN);
                            }
                            break;
                        }
                        // 判断是否终点处于ring组成线上
                    } else if (isInBoundary(conn, link.geteNodePid(), faceGeometry, result))
                        if (isSameNode(conn, result, link.geteNodePid()) && link.getUrban() != IS_URBAN) {
                            link.changedFields().put("urban", IS_URBAN);
                            break;
                        } else if (isMeshNode(conn, result, link.geteNodePid())) {
                            if (null != geometry && link.getUrban() != IS_URBAN) {
                                link.changedFields().put("urban", IS_URBAN);
                            } else {
                                link.setUrban(IS_URBAN);
                            }
                            break;
                        }
                    // 判断link是否包含于面内并且两个端点处于面组成线上
                } else if (GeoRelationUtils.InteriorAnd2Intersection(linkGeometry, faceGeometry)) {
                    // 判断两个端点是否属于同一点
                    if (isSameNode(conn, result, link.getsNodePid(), link.geteNodePid()) && link.getUrban() != IS_URBAN) {
                        link.changedFields().put("urban", IS_URBAN);
                        break;
                    } else if (isMeshNode(conn, result, link.getsNodePid()) && isMeshNode(conn, result, link.geteNodePid())) {
                        if (null != geometry && link.getUrban() != IS_URBAN) {
                            link.changedFields().put("urban", IS_URBAN);
                        } else {
                            link.setUrban(IS_URBAN);
                        }
                        break;
                    }
                } else {
                    // 其余情况暂不作处理
                    if (null != geometry) {
                        link.changedFields().put("urban", IS_NOT_URBAN);
                    } else {
                        link.setUrban(IS_NOT_URBAN);
                    }
                }
            }
        }
    }

    /**
     * 新增、修改Face时更新Link信息
     *
     * @param oldGeometry 修形前面几何(新增时与geometry传入同一对象）
     * @param newGeometry 修形后面几何(删除时传入null)
     * @param conn         数据库链接
     * @param result      结果集
     * @throws Exception
     */
    public static void updateUrban(Geometry oldGeometry, Geometry newGeometry, Connection conn, Result result) throws Exception {
        Geometry faceGeometry = transform(oldGeometry);

        RdLinkSelector selector = new RdLinkSelector(conn);
        // 删除面时,原面内Link的Urban赋0
        if (null == newGeometry) {
            List<RdLink> links = selector.loadLinkByFaceGeo(faceGeometry, false);
            for (RdLink link : links) {
                Geometry linkGeometry = transform(link.getGeometry());
                if (GeoRelationUtils.Interior(linkGeometry, faceGeometry)) {
                    link.changedFields().put("urban", IS_NOT_URBAN);
                    result.insertObject(link, ObjStatus.UPDATE, link.pid());
                }
            }
        } else {
            List<RdLink> links;
            Map<Integer, RdLink> deleteMaps = new HashMap<>();
            // 修形面时,原几何内link的Urban赋0
            if (null != faceGeometry && !faceGeometry.difference(newGeometry).isEmpty()) {
                links = selector.loadLinkByFaceGeo(faceGeometry, false);
                for (RdLink link : links) {
                    if (GeoRelationUtils.Interior(transform(link.getGeometry()), faceGeometry)) {
                        link.changedFields().put("urban", IS_NOT_URBAN);
                        deleteMaps.put(link.pid(), link);
                    }
                }
            }

            Map<Integer, RdLink> addMaps = new HashMap<>();
            // 修形面时,新几何内link的Urban赋1
            links = selector.loadLinkByFaceGeo(newGeometry, false);
            for (RdLink link : links) {
                Geometry linkGeometry = transform(link.getGeometry());
                // 判断link是否完全包含于该面
                if (GeoRelationUtils.Interior(linkGeometry, newGeometry)) {
                    link.changedFields().put("urban", IS_URBAN);
                    addMaps.put(link.pid(), link);
                    // 判断link是否包含于面内并有一个端点处于面组成线上
                } else if (GeoRelationUtils.InteriorAnd1Intersection(linkGeometry, newGeometry)) {
                    // 判断是否起点处于ring组成线上
                    if (isInBoundary(conn, link.getsNodePid(), newGeometry, result)) {
                        if (isSameNode(conn, result, link.getsNodePid())) {
                            link.changedFields().put("urban", IS_URBAN);
                            addMaps.put(link.pid(), link);
                        } else if (isMeshNode(conn, result, link.getsNodePid())) {
                            link.changedFields().put("urban", IS_URBAN);
                            addMaps.put(link.pid(), link);
                        }
                        // 判断是否终点处于ring组成线上
                    } else if (isInBoundary(conn, link.geteNodePid(), newGeometry, result))
                        if (isSameNode(conn, result, link.geteNodePid())) {
                            link.changedFields().put("urban", IS_URBAN);
                            addMaps.put(link.pid(), link);
                        } else if (isMeshNode(conn, result, link.geteNodePid())) {
                            link.changedFields().put("urban", IS_URBAN);
                            addMaps.put(link.pid(), link);
                        }
                    // 判断link是否包含于面内并且两个端点处于面组成线上
                } else if (GeoRelationUtils.InteriorAnd2Intersection(linkGeometry, newGeometry)) {
                    // 判断两个端点是否属于同一点
                    if (isSameNode(conn, result, link.getsNodePid(), link.geteNodePid())) {
                        link.changedFields().put("urban", IS_URBAN);
                        addMaps.put(link.pid(), link);
                    } else if (isMeshNode(conn, result, link.getsNodePid()) && isMeshNode(conn, result, link.geteNodePid())) {
                        link.changedFields().put("urban", IS_URBAN);
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
                result.insertObject(link, ObjStatus.UPDATE, link.pid());
            }
        }
    }
}
