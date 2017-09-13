package com.navinfo.dataservice.dao.glm.iface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Title: EdgeResult
 * @Package: com.navinfo.dataservice.engine.edit.operation
 * @Description: 大区接边数据
 * @Author: Crayeres
 * @Date: 9/13/2017
 * @Version: V1.0
 */
public class EdgeResult{

    /**
     * 本次请求大区库
     */
    private Integer sourceDb;

    /**
     * 本次请求是否涉及源大区库
     */
    private boolean hasSourceDb;

    /**
     * 本次请求对应操作类型
     */
    private OperType operType;

    /**
     * 本次请求对应要素类型
     */
    private ObjType objType;

    public EdgeResult(OperType operType, ObjType objType) {
        this.hasSourceDb = false;
        this.operType = operType;
        this.objType = objType;

        this.addedData = new ConcurrentHashMap<>();
        this.modifiedData = new ConcurrentHashMap<>();
        this.deletedData = new ConcurrentHashMap<>();
    }

    /**
     * 大区库对应新增数据
     */
    private Map<Integer, List<IRow>> addedData;

    /**
     * 大区库对应修改数据
     */
    private Map<Integer, List<IRow>> modifiedData;

    /**
     * 大区库对应删除数据
     */
    private Map<Integer, List<IRow>> deletedData;

    public void insert(Integer dbId, IRow row, ObjStatus status) {
        if (dbId.equals(sourceDb)) {
            hasSourceDb = true;
            return;
        }

        switch (status) {
            case INSERT: insertData(dbId, row, addedData); break;
            case UPDATE: insertData(dbId, row, modifiedData); break;
            case DELETE: insertData(dbId, row, deletedData); break;
        }
    }

    private void insertData(Integer dbId, IRow row, Map<Integer, List<IRow>> map) {
        if (map.containsKey(dbId)) {
            List<IRow> rows = map.get(dbId);
            rows.add(row);
        } else {
            List<IRow> rows = new ArrayList<>();
            rows.add(row);
            map.put(dbId, rows);
        }
    }

    /**
     * Getter method for property <tt>addedData</tt>.
     *
     * @return property value of addedData
     */
    public Map<Integer, List<IRow>> getAddedData() {
        return addedData;
    }

    /**
     * Getter method for property <tt>modifiedData</tt>.
     *
     * @return property value of modifiedData
     */
    public Map<Integer, List<IRow>> getModifiedData() {
        return modifiedData;
    }

    /**
     * Getter method for property <tt>deletedData</tt>.
     *
     * @return property value of deletedData
     */
    public Map<Integer, List<IRow>> getDeletedData() {
        return deletedData;
    }

    /**
     * Getter method for property <tt>operType</tt>.
     *
     * @return property value of operType
     */
    public OperType getOperType() {
        return operType;
    }

    /**
     * Getter method for property <tt>objType</tt>.
     *
     * @return property value of objType
     */
    public ObjType getObjType() {
        return objType;
    }

    /**
     * Getter method for property <tt>objType</tt>.
     *
     * @return property value of objType
     */
    public boolean hasSourceDb() {
        return hasSourceDb;
    }

    /**
     * Setter method for property <tt>sourceDb</tt>.
     *
     * @param sourceDb value to be assigned to property sourceDb
     */
    public void setSourceDb(Integer sourceDb) {
        this.sourceDb = sourceDb;
    }

    public Map<Integer, Result> conversion() {
        Map<Integer, Result> map = new HashMap<>();

        for (Map.Entry<Integer, List<IRow>> entry : this.getAddedData().entrySet()) {
            Result result = new Result();
            result.getAddObjects().addAll(entry.getValue());
            result.getListAddIRowObPid().addAll(loadPids(entry.getValue()));
            map.put(entry.getKey(), result);
        }

        for (Map.Entry<Integer, List<IRow>> entry : this.getModifiedData().entrySet()) {
            Result result;
            if (map.containsKey(entry.getKey())) {
                result = map.get(entry.getKey());
            } else {
                result = new Result();
            }
            result.getUpdateObjects().addAll(entry.getValue());
            result.getListUpdateIRowObPid().addAll(loadPids(entry.getValue()));
            map.put(entry.getKey(), result);
        }

        for (Map.Entry<Integer, List<IRow>> entry : this.getDeletedData().entrySet()) {
            Result result;
            if (map.containsKey(entry.getKey())) {
                result = map.get(entry.getKey());
            } else {
                result = new Result();
            }
            result.getDelObjects().addAll(entry.getValue());
            result.getListDelIRowObPid().addAll(loadPids(entry.getValue()));
            map.put(entry.getKey(), result);
        }

        return map;
    }

    private List<Integer> loadPids(List<IRow> rows) {
        List<Integer> pids = new ArrayList<>();
        for (IRow row : rows) {
            if (row instanceof IObj) {
                pids.add(((IObj) row).pid());
            } else {
                pids.add(row.parentPKValue());
            }
        }
        return pids;
    }
}
