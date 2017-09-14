package com.navinfo.dataservice.dao.glm.iface;


import com.alibaba.fastjson.JSONObject;

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
     * 源请求大区库
     */
    private Integer sourceDb;

    /**
     * 请求参数
     */
    private JSONObject request;

    /**
     * 子任务号
     */
    private Integer subtaskId;

    /**
     * 源请求操作结果
     */
    private Result sourceResult;

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

    public EdgeResult(String request) {
        this.request = JSONObject.parseObject(request);

        this.hasSourceDb = false;
        this.operType = Enum.valueOf(OperType.class, this.request.getString("command"));
        this.objType = Enum.valueOf(ObjType.class, this.request.getString("type"));

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
        return !(objType.equals(ObjType.RDINTER) || objType.equals(ObjType.RDROAD) || objType.equals(ObjType.RDOBJECT)) || hasSourceDb;
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

    /**
     * Getter method for property <tt>sourceResult</tt>.
     *
     * @return property value of sourceResult
     */
    public Result getSourceResult() {
        return sourceResult;
    }

    /**
     * Setter method for property <tt>sourceResult</tt>.
     *
     * @param sourceResult value to be assigned to property sourceResult
     */
    public void setSourceResult(Result sourceResult) {
        this.sourceResult = sourceResult;
    }

    /**
     * Getter method for property <tt>subtaskId</tt>.
     *
     * @return property value of subtaskId
     */
    public Integer getSubtaskId() {
        return subtaskId;
    }

    /**
     * Setter method for property <tt>subtaskId</tt>.
     *
     * @param subtaskId value to be assigned to property subtaskId
     */
    public void setSubtaskId(Integer subtaskId) {
        this.subtaskId = subtaskId;
    }

    /**
     * Getter method for property <tt>request</tt>.
     *
     * @return property value of request
     */
    public JSONObject getRequest() {
        return request;
    }

    /**
     * Getter method for property <tt>sourceDb</tt>.
     *
     * @return property value of sourceDb
     */
    public Integer getSourceDb() {
        return sourceDb;
    }
}
