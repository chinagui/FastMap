package com.navinfo.dataservice.engine.limit.glm.iface;

import com.navinfo.dataservice.dao.glm.iface.*;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.*;

/**
 * 操作结果
 */
public class Result implements ISerializable, Cloneable {

    private String primaryId;

    private OperStage operStage = OperStage.DayEdit;

    public OperStage getOperStage() {
        return operStage;
    }

    public void setOperStage(OperStage operStage) {
        this.operStage = operStage;
    }

    public String getPrimaryId() {
        return primaryId;
    }

    public void setPrimaryId(String primaryPid) {
        this.primaryId = primaryPid;
    }

    /**
     * 新增对象列表
     */
    private List<IRow> listAddIRow = new ArrayList<IRow>();

    /**
     * 删除对象集合
     */
    private List<IRow> listDelIRow = new ArrayList<IRow>();

    /**
     * 修改对象列表
     */
    private List<IRow> listUpdateIRow = new ArrayList<IRow>();

    private JSONArray checkResults = new JSONArray();

    private JSONArray logs = new JSONArray();

    HashMap<String, TreeMap<Integer, TreeMap<Integer, IVia>>> viaMap = null;

    Map<String, TreeMap<Integer, IVia>> nextViaMap = null;

    public JSONArray getCheckResults() {
        return checkResults;
    }

    public void setCheckResults(JSONArray checkResults) {
        this.checkResults = checkResults;
    }

    /**
     * 添加对象到结果列表
     *
     * @param row          对象
     * @param os           对象状态
     * @param topParentId 最顶级父表的pid,用来给前台做定位用
     */
    public void insertObject(IRow row, ObjStatus os, String topParentId) {

        row.setStatus(os);

        JSONObject json = new JSONObject();

        json.put("type", row.objType());

        if (row instanceof com.navinfo.dataservice.dao.glm.iface.IObj) {
            IObj obj = (IObj) row;
        } else {
            json.put("id", topParentId);

            if (row.parentPKValue() == topParentId) {
                // 该表是二级子表
                json.put("childPid", "");
            } else {
                // 该表是三级子表
                json.put("childPid", row.parentPKValue());
            }
        }

        switch (os) {
            case INSERT:
                listAddIRow.add(row);
                json.put("op", "新增");
                break;
            case DELETE:
                listDelIRow.add(row);
                json.put("op", "删除");
                break;
            case UPDATE:
                listUpdateIRow.add(row);
                json.put("op", "修改");
                break;
            default:
                break;
        }
        logs.add(json);

    }

    /**
     * @return 新增对象列表
     */
    public List<IRow> getAddObjects() {
        return listAddIRow;
    }

    /**
     * @return 删除对象列表
     */
    public List<IRow> getDelObjects() {
        return listDelIRow;
    }

    /**
     * @return 修改对象列表
     */
    public List<IRow> getUpdateObjects() {
        return listUpdateIRow;
    }

    @Override
    public JSONObject Serialize(ObjLevel objLevel) {

        return null;
    }

    @Override
    public boolean Unserialize(JSONObject json) throws Exception {

        return false;
    }

    public void clear() {
        this.listAddIRow.clear();
        this.listDelIRow.clear();
        this.listUpdateIRow.clear();
    }

    /**
     * @return 操作结果信息
     */
    public String getLogs() {

        return logs.toString();

    }

    public void add(Result result) {
        this.listAddIRow.addAll(result.getAddObjects());
        this.listUpdateIRow.addAll(result.getUpdateObjects());
        this.listDelIRow.addAll(result.getDelObjects());
    }

}
