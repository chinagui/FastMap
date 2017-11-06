package com.navinfo.dataservice.engine.edit.utils.batch;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameNode;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameNodePart;
import com.navinfo.dataservice.dao.glm.selector.rd.node.RdNodeSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.same.RdSameNodeSelector;
import com.navinfo.dataservice.engine.edit.utils.Constant;
import com.navinfo.dataservice.engine.edit.utils.GeoRelationUtils;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Geometry;
import org.json.JSONException;

import java.lang.reflect.Method;
import java.sql.Connection;

/**
 * @author zhangyt
 * @Title: BaseBatchUtils.java
 * @Description: TODO
 * @date: 2016年8月17日 下午2:28:10
 * @version: v1.0
 */
public class BaseBatchUtils {

    /**
     * 判断是否同一点
     *
     * @param conn
     * @param nodePids
     * @return
     * @throws Exception
     */
    protected static boolean isSameNode(Connection conn, Result result, Integer... nodePids) throws Exception {
        boolean flag = true;
        IRow row = null;
        for (Integer nodePid : nodePids) {
            row = new RdSameNodeSelector(conn).loadByNodePidAndTableName(nodePid, "RD_NODE", false);
            if (null == row) {
                boolean addFlag = false;
                for (IRow addRow : result.getAddObjects()) {
                    if (addRow instanceof RdSameNode) {
                        RdSameNode node = (RdSameNode) addRow;
                        for (IRow p : node.getParts()) {
                            RdSameNodePart part = (RdSameNodePart) p;
                            if (part.getNodePid() == nodePid && "RD_NODE".equalsIgnoreCase(part.getTableName())) {
                                addFlag = true;
                                break;
                            }
                        }
                        if (addFlag)
                            break;
                    }
                }
                if (!addFlag) {
                    flag = false;
                    break;
                }
            }
        }
        return flag;
    }

    protected static boolean isMeshNode (Connection conn, Result result, int nodePid) throws Exception {
        RdNode node = loadRdNode(conn, result, nodePid);
        if (null == node) {
            return false;
        }
        Geometry nodeGeo = transform(node.getGeometry());
        if (MeshUtils.isPointAtMeshBorder(nodeGeo.getCoordinate().x, nodeGeo.getCoordinate().y)) {
            return true;
        }
        return false;
    }

    private static RdNode loadRdNode(Connection conn, Result result, int nodePid) {
        RdNode node = null;
        try {
            node = (RdNode) new RdNodeSelector(conn).loadById(nodePid, false);
        } catch (Exception e) {
            for (IRow row : result.getAddObjects()) {
                if (row instanceof RdNode) {
                    RdNode n = (RdNode) row;
                    if (nodePid == n.pid()) {
                        node = n;
                    }
                }
            }
        }
        return node;
    }

    /**
     * 将传入几何缩小100000倍后返回
     *
     * @param g
     * @return
     * @throws JSONException
     */
    protected static Geometry transform(Geometry g) throws JSONException {
        return GeoTranslator.transform(g, Constant.BASE_SHRINK, Constant.BASE_PRECISION);
    }

    /**
     * 判断点是否处于ring的组成线上
     *
     * @param conn
     * @param nodePid
     * @param faceGeometry
     * @return
     * @throws Exception
     */
    protected static boolean isInBoundary(Connection conn, Integer nodePid, Geometry faceGeometry, Result result) throws Exception {
        RdNode node = loadRdNode(conn, result, nodePid);
        if (null == node)
            return false;
        // 获取边界线几何
        Geometry intersection = faceGeometry.getBoundary().intersection(transform(node.getGeometry()));
        return !intersection.isEmpty();
    }

    /**
     * 包含在ring内部，不考虑是否有交点在线上
     *
     * @param linkGeometry
     * @param faceGeometry
     * @return
     */
    protected static boolean isContainOrCover(Geometry linkGeometry, Geometry faceGeometry) {
        return GeoRelationUtils.Interior(linkGeometry, faceGeometry) || GeoRelationUtils.InteriorAnd1Intersection
                (linkGeometry, faceGeometry) || GeoRelationUtils.InteriorAnd2Intersection(linkGeometry, faceGeometry);
    }

    /**
     * 获取IRow对象Geometry属性
     *
     * @param row
     * @return
     * @throws Exception
     */
    protected static Geometry loadGeometry(IRow row) throws Exception {
        Class<?> clazz;
        try {
            clazz = Class.forName(row.getClass().getName());
            Method method = clazz.getMethod("getGeometry");
            return (Geometry) method.invoke(row);
        } catch (Exception e) {
            throw new Exception("PID为" + row.parentPKValue() + "的" + row.getClass().getSimpleName() +
                    "对象没有找到Geometry属性");
        }
    }

    /**
     * IRow对象设置RegionId
     *
     * @param row
     * @param regionId
     * @throws Exception
     */
    protected static void setRegionId(IRow row, Integer regionId) throws Exception {
        try {
            Method[] methods = row.getClass().getDeclaredMethods();
            for (Method method : methods) {
                if ("setRegionId".equals(method.getName())) {
                    method.invoke(row, regionId);
                    break;
                }
            }
        } catch (Exception e) {
            throw new Exception("PID为" + row.parentPKValue() + "的" + row.getClass().getSimpleName() + "对象设置RegionId失败");
        }
    }

    /**
     * 排除已删除数据
     * @param result 结果集
     * @param linkPid 验证LinkPid
     * @return true: 已删除，false: 未删除
     */
    protected static boolean isDeleteLink(Result result, int linkPid) {
        boolean isDelete = false;
        for (IRow row : result.getDelObjects()) {
            if (!(row instanceof RdLink)) {
                continue;
            }
            RdLink link = (RdLink) row;
            if (linkPid == link.pid()) {
                isDelete = true;
            }
        }
        return isDelete;
    }
}
