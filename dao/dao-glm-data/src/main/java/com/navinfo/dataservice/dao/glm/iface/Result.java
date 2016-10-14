package com.navinfo.dataservice.dao.glm.iface;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 操作结果
 */
public class Result implements ISerializable {

	private int primaryPid;
	
	private OperStage operStage = OperStage.DayEdit;

	public OperStage getOperStage() {
		return operStage;
	}

	public void setOperStage(OperStage operStage) {
		this.operStage = operStage;
	}

	public int getPrimaryPid() {
		return primaryPid;
	}

	public void setPrimaryPid(int primaryPid) {
		this.primaryPid = primaryPid;
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
	
	private List<Integer> listAddIRowObPid = new ArrayList<>();
	
	private List<Integer> listUpdateIRowObPid = new ArrayList<>();
	
	private List<Integer> listDelIRowObPid = new ArrayList<>();

	private JSONArray checkResults = new JSONArray();

	private JSONArray logs = new JSONArray();

	public JSONArray getCheckResults() {
		return checkResults;
	}

	public void setCheckResults(JSONArray checkResults) {
		this.checkResults = checkResults;
	}

	/**
	 * 添加对象到结果列表
	 * 
	 * @param row
	 *            对象
	 * @param os
	 *            对象状态
	 * @param topParentPid
	 *            最顶级父表的pid,用来给前台做定位用
	 * 
	 */
	public void insertObject(IRow row, ObjStatus os, int topParentPid) {

		row.setStatus(os);

		JSONObject json = new JSONObject();

		json.put("type", row.objType());

		if (row instanceof IObj) {
			IObj obj = (IObj) row;

			if (obj.parentTableName().equals(obj.tableName())) {
				// 该表没有父
				json.put("pid", obj.pid());

				json.put("childPid", "");
			} else {
				// 该表有父表
				json.put("pid", topParentPid);

				json.put("childPid", obj.pid());
			}
		} else {
			json.put("pid", topParentPid);

			if (row.parentPKValue() == topParentPid) {
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
			listAddIRowObPid.add(topParentPid);
			json.put("op", "新增");
			break;
		case DELETE:
			listDelIRow.add(row);
			listDelIRowObPid.add(topParentPid);
			json.put("op", "删除");
			break;
		case UPDATE:
			listUpdateIRow.add(row);
			listUpdateIRowObPid.add(topParentPid);
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
	
	public List<Integer> getListAddIRowObPid() {
		return listAddIRowObPid;
	}

	public List<Integer> getListUpdateIRowObPid() {
		return listUpdateIRowObPid;
	}

	public List<Integer> getListDelIRowObPid() {
		return listDelIRowObPid;
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
		BranchResultHandle.handleResult(listAddIRow,logs);
		return logs.toString();

	}
	
	public void add(Result result) {
		this.listAddIRow.addAll(result.getAddObjects());
		this.listUpdateIRow.addAll(result.getUpdateObjects());
		this.listDelIRow.addAll(result.getDelObjects());
	}
}
