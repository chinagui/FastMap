package com.navinfo.dataservice.engine.edit.utils.batch;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.lu.LuFace;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.lu.LuFaceSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.edit.utils.GeoRelationUtils;
import com.vividsolutions.jts.geom.Geometry;

import java.sql.Connection;
import java.util.List;

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
        // 将link几何缩小100000倍，根据link几何查找与之相关的BUA面
        Geometry linkGeometry = null == geometry ? shrink(link.getGeometry()) : shrink(geometry);
        List<LuFace> faces = new LuFaceSelector(conn).loadRelateFaceByGeometry(linkGeometry);
        // 如关联面数量为空或大于一暂不做处理
        if (faces.isEmpty() || faces.size() > 1) {
            link.changedFields().put("urban", IS_NOT_URBAN);
            return;
        }
        // 取出与link关联的唯一面几何
        Geometry faceGeometry = shrink(faces.get(0).getGeometry());
        // 判断link是否完全包含于该面
        if (GeoRelationUtils.Interior(linkGeometry, faceGeometry)) {
            if (null != geometry)
                link.changedFields().put("urban", IS_URBAN);
            else
                link.setUrban(IS_URBAN);
            // 判断link是否包含于面内并有一个端点处于面组成线上
        } else if (GeoRelationUtils.InteriorAnd1Intersection(linkGeometry, faceGeometry)) {
            // 判断是否起点处于ring组成线上
            if (isInBoundary(conn, link.getsNodePid(), faceGeometry, result)) {
                if (isSameNode(conn, link.getsNodePid()))
                    link.changedFields().put("urban", IS_URBAN);
                // 判断是否终点处于ring组成线上
            } else if (isInBoundary(conn, link.geteNodePid(), faceGeometry, result))
                if (isSameNode(conn, link.geteNodePid()))
                    link.changedFields().put("urban", IS_URBAN);

            // 判断link是否包含于面内并且两个端点处于面组成线上
        } else if (GeoRelationUtils.InteriorAnd2Intersection(linkGeometry, faceGeometry)) {
            // 判断两个端点是否属于同一点
            if (isSameNode(conn, link.getsNodePid(), link.geteNodePid()))
                link.changedFields().put("urban", IS_URBAN);
        } else {
            // 其余情况暂不作处理
        }
    }

    /**
     * 新增、修改Face时更新Link信息
     *
     * @param faceGeometry 修形前面几何
     * @param geometry     修形后面几何(删除时传入null)
     * @param conn         数据库链接
     * @param result       结果集
     * @throws Exception
     */
    public static void updateUrban(Geometry faceGeometry, Geometry geometry, Connection conn, Result result) throws Exception {
        RdLinkSelector selector = new RdLinkSelector(conn);
        // 删除面时,原面内Link的Urban赋0
        if (null == geometry) {
            List<RdLink> links = selector.loadLinkByFaceGeo(faceGeometry, true);
            for (RdLink link : links) {
                link.changedFields().put("urban", IS_NOT_URBAN);
                result.insertObject(link, ObjStatus.UPDATE, link.pid());
            }
            return;
        }
        // 修形面时,新几何内link的Urban赋1
        List<RdLink> links = selector.loadLinkByFaceGeo(geometry, true);
        for (RdLink link : links) {
            Geometry linkGeometry = GeoTranslator.transform(link.getGeometry(), 0.00001, 5);
            // 判断link是否完全包含于该面
            if (GeoRelationUtils.Interior(linkGeometry, geometry)) {
                link.changedFields().put("urban", IS_URBAN);
                // 判断link是否包含于面内并有一个端点处于面组成线上
            } else if (GeoRelationUtils.InteriorAnd1Intersection(linkGeometry, geometry)) {
                // 判断是否起点处于ring组成线上
                if (isInBoundary(conn, link.getsNodePid(), geometry, result)) {
                    if (isSameNode(conn, link.getsNodePid()))
                        link.changedFields().put("urban", IS_URBAN);
                    // 判断是否终点处于ring组成线上
                } else if (isInBoundary(conn, link.geteNodePid(), geometry, result))
                    if (isSameNode(conn, link.geteNodePid()))
                        link.changedFields().put("urban", IS_URBAN);

                // 判断link是否包含于面内并且两个端点处于面组成线上
            } else if (GeoRelationUtils.InteriorAnd2Intersection(linkGeometry, geometry)) {
                // 判断两个端点是否属于同一点
                if (isSameNode(conn, link.getsNodePid(), link.geteNodePid()))
                    link.changedFields().put("urban", IS_URBAN);
            } else {
                // 其余情况暂不作处理
            }
            result.insertObject(link, ObjStatus.UPDATE, link.pid());
        }
        // 修形面时,原几何与新几何差分后内link的Urban赋0
        if (null != faceGeometry) {
            Geometry diffGeo = faceGeometry.difference(geometry);
            links = selector.loadLinkByFaceGeo(diffGeo, true);
            for (RdLink link : links) {
                link.changedFields().put("urban", IS_NOT_URBAN);
                result.insertObject(link, ObjStatus.UPDATE, link.pid());
            }
        }
    }
}
