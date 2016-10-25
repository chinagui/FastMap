package com.navinfo.dataservice.engine.edit.utils.batch;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.model.lu.LuFace;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.lu.LuFaceSelector;
import com.navinfo.dataservice.engine.edit.utils.GeoRelationUtils;
import com.vividsolutions.jts.geom.Geometry;

/**
 * @author zhangyt
 * @Title: LuFaceOpearteUtils.java
 * @Description: 用于更新RdLink的Urban属性
 * @date: 2016年8月16日 下午7:08:22
 * @version: v1.0
 */
public class UrbanBatchUtils extends BaseBatchUtils {

    private final static Integer IS_URBAN = 1;

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
    public static void updateUrban(RdLink link, Geometry geometry, Connection conn) throws Exception {
        // 将link几何缩小100000倍，根据link几何查找与之相关的BUA面
        Geometry linkGeometry = null == geometry ? shrink(link.getGeometry()) : shrink(geometry);
        List<LuFace> faces = new LuFaceSelector(conn).loadRelateFaceByGeometry(linkGeometry);
        // 如关联面数量为空或大于一暂不做处理
        if (faces.isEmpty() || faces.size() > 1) {
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
            if (isInBoundary(conn, link.getsNodePid(), faceGeometry)) {
                if (isSameNode(conn, link.getsNodePid()))
                    link.changedFields().put("urban", IS_URBAN);
                // 判断是否终点处于ring组成线上
            } else if (isInBoundary(conn, link.geteNodePid(), faceGeometry))
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

}
