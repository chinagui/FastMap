package com.navinfo.dataservice.engine.edit.operation.edge;

import com.alibaba.fastjson.JSONObject;
import com.navinfo.dataservice.bizcommons.service.DbMeshInfoUtil;
import com.navinfo.dataservice.dao.glm.iface.EdgeResult;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.engine.edit.utils.Constant;
import com.navinfo.dataservice.engine.edit.utils.GeometryUtils;
import com.vividsolutions.jts.geom.Geometry;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @Title: EdgeCheck
 * @Package: com.navinfo.dataservice.engine.edit.operation.edge
 * @Description: ${TODO}
 * @Author: Crayeres
 * @Date: 9/18/2017
 * @Version: V1.0
 */
public class EdgeCheck {

    private EdgeCheck() {
    }

    public static void assertOperation(EdgeResult edge, EdgeOperation operation) throws Exception{
        switch (edge.getOperType()) {
            case REPAIR: assertRepairOperation(edge, operation); break;
            case DELETE: assertDeleteOperation(edge, operation); break;
        }
    }

    private static void assertDeleteOperation(EdgeResult edge, EdgeOperation operation) throws Exception {
        JSONObject json = edge.getRequest();
        if (Constant.LINK_TYPES.containsKey(edge.getObjType()) || Constant.NODE_TYPES.containsKey(edge.getObjType())) {
            if (json.containsKey("objId")) {
                int objId = json.getIntValue("objId");
                for (IRow row : edge.getSourceResult().getDelObjects()) {
                    for (Map.Entry<ObjType, Class<? extends IRow>> entry : Constant.NODE_TYPES.entrySet()) {
                        if (!row.objType().equals(entry.getKey()) || (row.parentPKValue() == objId)) {
                            continue;
                        }

                        IRow node = new AbstractSelector(entry.getValue(), operation.getSourceProcess(edge).getConn()).
                                loadById(row.parentPKValue(), false);

                        Geometry geometry = GeometryUtils.loadGeometry(node);
                        Set<Integer> dbIds = DbMeshInfoUtil.calcDbIds(geometry);
                        if (dbIds.size() > 1) {
                            throw new Exception("该操作会导致跨大区Node删除，请优先执行跨大区Node删除操作!");
                        }
                    }
                }
            }
        }
    }

    private static void assertRepairOperation(EdgeResult edge, EdgeOperation operation) throws Exception {
        JSONObject json = edge.getRequest();
        for (Map.Entry<ObjType, Class<? extends IRow>> entry : Constant.OBJ_TYPE_CLASS_MAP.entrySet()) {
            if (!entry.getKey().equals(edge.getObjType())) {
                continue;
            }
            if (!json.containsKey("data") || !json.getJSONObject("data").containsKey("catchInfos")) {
                return;
            }

            Iterator<Object> iterator = json.getJSONObject("data").getJSONArray("catchInfos").iterator();
            while (iterator.hasNext()) {
                JSONObject obj = (JSONObject) iterator.next();
                if (obj.containsKey("nodePid")) {
                    IRow node = new AbstractSelector(entry.getValue(), operation.getSourceProcess(edge).getConn()).
                            loadById(obj.getIntValue("nodePid"), false);

                    Set<Integer> dbIds = DbMeshInfoUtil.calcDbIds(GeometryUtils.loadGeometry(node));
                    if (dbIds.size() > 1) {
                        throw new Exception("不允分离大区库接边Node!");
                    }
                }
            }
        }
    }
}
