package com.navinfo.dataservice.engine.edit.operation.edge;

import com.navinfo.dataservice.bizcommons.service.DbMeshInfoUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.*;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;
import com.navinfo.dataservice.engine.edit.operation.Transaction;
import com.navinfo.dataservice.engine.edit.utils.Constant;
import com.navinfo.dataservice.engine.edit.utils.GeometryUtils;
import com.navinfo.dataservice.engine.edit.utils.NodeOperateUtils;
import com.vividsolutions.jts.geom.Geometry;
import net.sf.json.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;

import java.util.*;

/**
 * @Title: EdgeOperation
 * @Package: com.navinfo.dataservice.engine.edit.operation
 * @Description: ${TODO}
 * @Author: Crayeres
 * @Date: 2017/9/13
 * @Version: V1.0
 */
public class EdgeOperation {

    private static final Logger LOGGER = Logger.getLogger(EdgeOperation.class);

    private List<AbstractProcess> abstractProcesses = new ArrayList<>();

    private static Set<ObjType> objTypes = new HashSet<>();

    static {
        objTypes.addAll(Constant.NODE_TYPES.keySet());
        objTypes.addAll(Constant.LINK_TYPES.keySet());
        objTypes.addAll(Constant.FACE_TYPES.keySet());
        objTypes.addAll(Constant.CRF_TYPES);
    }

    public void handleEdge(EdgeResult edge) throws Exception{
        if (!objTypes.contains(edge.getObjType())) {
            return;
        }

        switch (edge.getOperType()) {
            case DELETE:
                excuteDelete(edge);
                return;
        }

        excuteAdd(edge);
        excuteModify(edge);

        EdgeUtil.handleCrf(edge);

        excute(edge);
    }

    private Transaction transaction;

    /**
     * Setter method for property <tt>transaction</tt>.
     *
     * @param transaction value to be assigned to property transaction
     */
    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public void excuteAdd(EdgeResult edge) throws Exception{
        List<IRow> added = edge.getSourceResult().getAddObjects();

        for (IRow iRow : added) {
            Set<Integer> dbIds = EdgeUtil.calcDbIds(iRow, edge.getSourceDb(), added);
            for (Integer dbId : dbIds) {
                edge.insert(dbId, iRow, ObjStatus.INSERT);
            }
        }
        for (List<IRow> roots : edge.getAddedData().values()) {
            EdgeUtil.extentions(roots, added);
        }
    }

    public void excuteModify(EdgeResult edge) throws Exception{
        List<IRow> modified = edge.getSourceResult().getUpdateObjects();

        for (IRow iRow : modified) {
            Set<Integer> dbIds = EdgeUtil.calcDbIds(iRow, edge.getSourceDb(), modified);
            Set<Integer> diffDbIds = new HashSet<>();
            if (iRow.changedFields().containsKey("geometry")) {
                try {
                    Geometry geometry = GeoTranslator.geojson2Jts((JSONObject) iRow.changedFields().get("geomtry"));
                    diffDbIds = DbMeshInfoUtil.calcDbIds(geometry);
                    CollectionUtils.removeAll(diffDbIds, dbIds);
                } catch (JSONException e) {
                    LOGGER.error("failed to obtain geometry changes..", e.fillInStackTrace());
                    throw e;
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
        for (List<IRow> roots : edge.getAddedData().values()) {
            EdgeUtil.extentions(roots, modified);
        }
        for (List<IRow> roots : edge.getModifiedData().values()) {
            EdgeUtil.extentions(roots, modified);
        }
    }

    public void excuteDelete(EdgeResult edge) throws Exception {
        List<IRow> deleted = edge.getSourceResult().getDelObjects();

        for (IRow iRow : deleted) {
            Set<Integer> dbIds = EdgeUtil.calcDbIds(iRow, edge.getSourceDb(), deleted);
            for (Integer dbId : dbIds) {
                edge.insert(dbId, iRow, ObjStatus.DELETE);
            }
        }
        Map<Integer, Result> conversion = edge.conversion();
        for (Map.Entry<Integer, Result> entry : conversion.entrySet()) {
            for (IRow iRow : entry.getValue().getDelObjects()) {
                if (Constant.NODE_TYPES.containsKey(iRow.objType())) {
                    JSONObject json = new JSONObject();
                    json.put("dbId", entry.getKey());
                    json.put("command", edge.getOperType());
                    json.put("subtaskId", edge.getSubtaskId());
                    json.put("type", edge.getObjType());
                    json.put("objId", iRow.parentPKValue());
                    try {
                        TransactionFactory.generateCommand(transaction, json.toString());
                    } catch (Exception e) {
                        LOGGER.error("error is run edge db..", e.fillInStackTrace());
                        throw e;
                    }
                }
            }
            excuteCrf(entry, edge);
        }
    }

    public void excuteCrf(Map.Entry<Integer, Result> entry, EdgeResult edge) throws Exception{
        Result result = new Result();
        for (IRow iRow : entry.getValue().getAddObjects()) {
            if (Constant.CRF_TYPES.contains(iRow.objType())) {
                result.insertObject(iRow, ObjStatus.INSERT, iRow.parentPKValue());
            }
        }
        for (IRow iRow : entry.getValue().getUpdateObjects()) {
            if (Constant.CRF_TYPES.contains(iRow.objType())) {
                result.insertObject(iRow, ObjStatus.UPDATE, iRow.parentPKValue());
            }
        }
        for (IRow iRow : entry.getValue().getDelObjects()) {
            if (Constant.CRF_TYPES.contains(iRow.objType())) {
                result.insertObject(iRow, ObjStatus.DELETE, iRow.parentPKValue());
            }
        }
        try {
            AbstractCommand command = TransactionFactory.generateCommand(transaction, edge.getRequest().put("dbId", entry.getKey()).toString());
            AbstractProcess process = transaction.createProcess(command);
            abstractProcesses.add(process);
            transaction.recordData(process, result);
        } catch (Exception e) {
            LOGGER.error("", e.fillInStackTrace());
            throw e;
        }
    }

    public void excute(EdgeResult edge) throws Exception{
        for (Map.Entry<Integer, Result> entry : edge.conversion().entrySet()) {
            try {
                AbstractCommand command = TransactionFactory.generateCommand(transaction, edge.getRequest().put("dbId", entry.getKey()).toString());

                AbstractProcess process = transaction.createProcess(command);
                abstractProcesses.add(process);
                transaction.recordData(process, entry.getValue());
            } catch (Exception e) {
                LOGGER.error("", e.fillInStackTrace());
                throw e;
            }
        }
    }

    /**
     * Getter method for property <tt>abstractProcesses</tt>.
     *
     * @return property value of abstractProcesses
     */
    public List<AbstractProcess> getAbstractProcesses() {
        return abstractProcesses;
    }
}
