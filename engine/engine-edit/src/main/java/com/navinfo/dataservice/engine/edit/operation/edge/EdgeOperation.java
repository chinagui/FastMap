package com.navinfo.dataservice.engine.edit.operation.edge;

import com.navinfo.dataservice.bizcommons.service.DbMeshInfoUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.*;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;
import com.navinfo.dataservice.engine.edit.operation.Transaction;
import com.navinfo.dataservice.engine.edit.utils.Constant;
import com.navinfo.dataservice.engine.edit.utils.NodeOperateUtils;
import com.vividsolutions.jts.geom.Geometry;
import net.sf.json.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;

import java.sql.Connection;
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

    private Map<Integer, AbstractProcess> abstractProcesses = new LinkedHashMap<>();

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
            case MOVE:
                excuteMove(edge);
                return;
        }

        excuteAdd(edge);
        excuteModify(edge);

        handleCrf(edge);

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
            Set<Integer> dbIds = EdgeUtil.calcDbIds(iRow);
            for (Integer dbId : dbIds) {
                edge.insert(dbId, iRow, ObjStatus.INSERT);
            }
            edge.setGlobalDbIds(dbIds);
        }
        for (List<IRow> roots : edge.getAddedData().values()) {
            EdgeUtil.extentions(roots, added);
        }
    }

    public void excuteModify(EdgeResult edge) throws Exception{
        List<IRow> modified = edge.getSourceResult().getUpdateObjects();

        for (IRow iRow : modified) {
            Set<Integer> sourceDb = EdgeUtil.calcDbIds(iRow);
            Set<Integer> newDb = EdgeUtil.calcNewDbIds(iRow);
            Map<Integer, ObjStatus> diffDb = EdgeUtil.diffDb(sourceDb, newDb);

            for (Map.Entry<Integer, ObjStatus> entry : diffDb.entrySet()) {
                IRow currentRow = iRow;
                if (entry.getValue().equals(ObjStatus.INSERT)) {
                    currentRow = NodeOperateUtils.clone(iRow);
                }
                edge.insert(entry.getKey(), currentRow, entry.getValue());
            }

            edge.setGlobalDbIds(sourceDb);
            edge.setGlobalDbIds(newDb);
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
            Set<Integer> dbIds = EdgeUtil.calcDbIds(iRow);
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
                        AbstractCommand abstractCommand = TransactionFactory.generateCommand(transaction, json.toString());
                        AbstractProcess abstractProcess = transaction.createProcess(abstractCommand);
                        abstractProcesses.put(entry.getKey(), abstractProcess);
                        abstractProcess.run();
                        Result result = abstractProcess.getResult();
                        transaction.recordData(abstractProcess, result);
                    } catch (Exception e) {
                        LOGGER.error("run edge database has error..", e.fillInStackTrace());
                        throw e;
                    }
                }
            }
            excuteCrf(entry.getKey(), edge);
        }
    }

    public void excuteMove(EdgeResult edge) throws Exception {
        List<IRow> updated = edge.getSourceResult().getUpdateObjects();

        for (IRow iRow : updated) {
            Set<Integer> dbIds = EdgeUtil.calcDbIds(iRow);
            for (Integer dbId : dbIds) {
                edge.insert(dbId, iRow, ObjStatus.UPDATE);
            }
        }

        Map<Integer, Result> conversion = edge.conversion();
        for (Map.Entry<Integer, Result> entry : conversion.entrySet()) {
            if (CollectionUtils.isEmpty(entry.getValue().getUpdateObjects())) {
                excute(edge, entry.getKey(), entry.getValue());
            } else {
                for (IRow iRow : entry.getValue().getUpdateObjects()) {
                    if (Constant.NODE_TYPES.containsKey(iRow.objType()) && iRow.changedFields().containsKey("geometry")) {
                        JSONObject json = new JSONObject();
                        json.put("command", edge.getOperType());
                        json.put("dbId", entry.getKey());
                        json.put("subtaskId", edge.getSubtaskId());
                        json.put("type", iRow.objType());
                        json.put("objId", iRow.parentPKValue());

                        JSONObject data = new JSONObject();
                        Geometry geometry = GeoTranslator.geojson2Jts((JSONObject) iRow.changedFields().get("geometry"));
                        data.put("longitude", geometry.getCoordinate().x);
                        data.put("latitude", geometry.getCoordinate().y);
                        json.put("data", data);

                        AbstractCommand abstractCommand = TransactionFactory.generateCommand(transaction, json.toString());
                        AbstractProcess abstractProcess = transaction.createProcess(abstractCommand);
                        abstractProcesses.put(entry.getKey(), abstractProcess);
                        abstractProcess.run();
                        Result result = abstractProcess.getResult();
                        transaction.recordData(abstractProcess, result);
                    }
                }
            }
        }
    }

    public void excuteCrf(Integer dbId, EdgeResult edge) throws Exception{
        Result result = new Result();
        for (IRow iRow : edge.getSourceResult().getAddObjects()) {
            if (Constant.CRF_TYPES.contains(iRow.objType())) {
                result.insertObject(iRow, ObjStatus.INSERT, iRow.parentPKValue());
            }
        }
        for (IRow iRow : edge.getSourceResult().getUpdateObjects()) {
            if (Constant.CRF_TYPES.contains(iRow.objType())) {
                result.insertObject(iRow, ObjStatus.UPDATE, iRow.parentPKValue());
            }
        }
        for (IRow iRow : edge.getSourceResult().getDelObjects()) {
            if (Constant.CRF_TYPES.contains(iRow.objType())) {
                result.insertObject(iRow, ObjStatus.DELETE, iRow.parentPKValue());
            }
        }
        if (EdgeUtil.isEmptyResult(result)) {
            return;
        } else {
            excute(edge, dbId, result);
        }
    }

    public void excute(EdgeResult edge, Integer dbId, Result result) throws Exception{
        try {
            AbstractProcess abstractProcess = loadAbstractProcess(edge, dbId);
            transaction.recordData(abstractProcess, result);
        } catch (Exception e) {
            LOGGER.error("", e.fillInStackTrace());
            throw e;
        }
    }

    public void excute(EdgeResult edge) throws Exception{
        for (Map.Entry<Integer, Result> entry : edge.conversion().entrySet()) {
            try {
                AbstractProcess abstractProcess = loadAbstractProcess(edge, entry.getKey());
                transaction.recordData(abstractProcess, entry.getValue());
            } catch (Exception e) {
                LOGGER.error("", e.fillInStackTrace());
                throw e;
            }
        }
    }

    private AbstractProcess loadAbstractProcess(EdgeResult edge, Integer dbId) throws Exception {
        AbstractProcess abstractProcess;
        if (abstractProcesses.containsKey(dbId)) {
            abstractProcess = abstractProcesses.get(dbId);
        } else {
            JSONObject request = JSONObject.fromObject(edge.getRequest().toString());
            request.element("dbId", dbId);
            AbstractCommand command = TransactionFactory.generateCommand(transaction, request.toString());
            abstractProcess = transaction.createProcess(command);
            abstractProcesses.put(dbId, abstractProcess);
        }
        return abstractProcess;
    }

    private AbstractProcess loadAbstractProcess(EdgeResult edge, Integer dbId, String req) throws Exception {
        AbstractProcess abstractProcess;
        if (abstractProcesses.containsKey(dbId)) {
            abstractProcess = abstractProcesses.get(dbId);
        } else {
            JSONObject request = JSONObject.fromObject(req);
            request.element("dbId", dbId);
            AbstractCommand command = TransactionFactory.generateCommand(transaction, request.toString());
            abstractProcess = transaction.createProcess(command);
            abstractProcesses.put(dbId, abstractProcess);
        }
        return abstractProcess;
    }

    /**
     * Getter method for property <tt>abstractProcesses</tt>.
     *
     * @return property value of abstractProcesses
     */
    public Map<Integer, AbstractProcess> getAbstractProcesses() {
        return abstractProcesses;
    }

    public void addAbstractProcess(Integer dbId, AbstractProcess abstractProcess) {
        abstractProcesses.put(dbId, abstractProcess);
    }

    public AbstractProcess getSourceProcess(EdgeResult edge) {
        return abstractProcesses.get(edge.getSourceDb());
    }

    /**
     * 计算本次操作跨大区CRF影响
     * @param edge
     */
    public void handleCrf(EdgeResult edge) throws Exception {
        if (CollectionUtils.isNotEmpty(edge.getGlobalDbIds())) {
            for (Map.Entry<Integer, Result> entry : edge.conversion().entrySet()) {
                excuteCrf(entry.getKey(), edge);
            }
        } else {
            Set<Integer> dbIds = EdgeUtil.calcCrfDbIds(edge, getSourceProcess(edge).getConn());
            for (Integer dbId : dbIds) {
                excuteCrf(dbId, edge);
            }
        }
    }
}
