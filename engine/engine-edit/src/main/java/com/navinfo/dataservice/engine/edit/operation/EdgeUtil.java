package com.navinfo.dataservice.engine.edit.operation;

import com.navinfo.dataservice.bizcommons.service.DbMeshInfoUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.EdgeResult;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.engine.edit.utils.GeometryUtils;
import com.navinfo.dataservice.engine.edit.utils.NodeOperateUtils;
import com.vividsolutions.jts.geom.Geometry;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @Title: EdgeUtil
 * @Package: com.navinfo.dataservice.engine.edit.operation
 * @Description: ${TODO}
 * @Author: Crayeres
 * @Date: 2017/9/13
 * @Version: V1.0
 */
public class EdgeUtil {

    private static final Logger LOGGER = Logger.getLogger(EdgeUtil.class);

    private EdgeUtil() {

    }

    public static void excuteAdd(EdgeResult edge, List<IRow> added) {
        for (IRow iRow : added) {
            if (iRow instanceof IObj) {
                Geometry geometry = GeometryUtils.loadGeometry(iRow);
                Set<Integer> dbIds = DbMeshInfoUtil.calcDbIds(geometry);
                for (Integer dbId : dbIds) {
                    edge.insert(dbId, iRow, ObjStatus.INSERT);
                }
            }
        }
        for (List<IRow> roots : edge.getAddedData().values()) {
            extentions(roots, added);
        }
    }

    public static void excuteModify(EdgeResult edge, List<IRow> modified) {
        for (IRow iRow : modified) {
            if (iRow instanceof IObj) {
                Geometry geometry = GeometryUtils.loadGeometry(iRow);
                Set<Integer> dbIds = DbMeshInfoUtil.calcDbIds(geometry);

                Set<Integer> diffDbIds = new HashSet<>();
                if (iRow.changedFields().containsKey("geometry")) {
                    try {
                        geometry = GeoTranslator.geojson2Jts((JSONObject) iRow.changedFields().get("geomtry"));
                        diffDbIds = DbMeshInfoUtil.calcDbIds(geometry);
                        CollectionUtils.removeAll(diffDbIds, dbIds);
                    } catch (JSONException e) {
                        LOGGER.error("failed to obtain geometry changes..", e.fillInStackTrace());
                    }
                }

                for (Integer dbId : dbIds) {
                    edge.insert(dbId, iRow, ObjStatus.UPDATE);
                }
                for (Integer diffDbId : diffDbIds) {
                    IRow clone = NodeOperateUtils.clone(iRow);
                    edge.insert(diffDbId, clone, ObjStatus.INSERT);
                }
            }
        }
        for (List<IRow> roots : edge.getAddedData().values()) {
            extentions(roots, modified);
        }
        for (List<IRow> roots : edge.getModifiedData().values()) {
            extentions(roots, modified);
        }
    }

    public static void excuteDelete(EdgeResult edge, List<IRow> deleted) {
        for (IRow iRow : deleted) {
            if (iRow instanceof IObj) {
                Geometry geometry = GeometryUtils.loadGeometry(iRow);
                Set<Integer> dbIds = DbMeshInfoUtil.calcDbIds(geometry);
                for (Integer dbId : dbIds) {
                    edge.insert(dbId, iRow, ObjStatus.DELETE);
                }
            }
        }
        for (List<IRow> roots : edge.getDeletedData().values()) {
            extentions(roots, deleted);
        }
    }

    private static void extentions(List<IRow> roots, List<IRow> iRows) {
        List<IRow> subRows = new ArrayList<>();

        for (IRow iRow : iRows) {
            if (iRow instanceof IObj) {
                continue;
            }

            if (containsParent(roots, iRow)) {
                subRows.add(iRow);
            }
        }
        roots.addAll(subRows);
    }

    private static boolean containsParent(List<IRow> roots, IRow row) {
        for (IRow iRow : roots) {
            if (iRow instanceof IObj) {
                IObj root = (IObj) iRow;
                if (row.parentPKName().equals(root.tableName()) && (root.pid() == row.parentPKValue())) {
                    return true;
                }
            }
        }
        return false;
    }
}
