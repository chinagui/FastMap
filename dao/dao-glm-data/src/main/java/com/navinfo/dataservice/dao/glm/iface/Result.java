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
	
	private JSONArray checkResults = new JSONArray();
	
	

	public JSONArray getCheckResults() {
		return checkResults;
	}

	public void setCheckResults(JSONArray checkResults) {
		this.checkResults = checkResults;
	}

	/**
	 * 添加对象到结果列表
	 * 
	 * @param io
	 *            对象
	 * @param os
	 *            对象状态
	 */
	public void insertObject(IRow io, ObjStatus os) {

		switch (os) {
		case INSERT:
			listAddIRow.add(io);
			break;
		case DELETE:
			listDelIRow.add(io);
			break;
		case UPDATE:
			listUpdateIRow.add(io);
			break;
		default:
			break;
		}
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
	
	public void clear(){
		this.listAddIRow.clear();
		this.listDelIRow.clear();
		this.listUpdateIRow.clear();
	}

	/**
	 * @return 操作结果信息
	 */
	public String getLogs() {

		JSONArray array = new JSONArray();

		for (IRow row : this.getAddObjects()) {
			if (row instanceof IObj) {

				IObj obj = (IObj) row;

				JSONObject json = new JSONObject();

				json.put("pid", obj.pid());

				json.put("type", obj.objType());

				json.put("op", "新增");

				array.add(json);
			}else if (row instanceof IRow){

				JSONObject json = new JSONObject();

				json.put("rowId", row.rowId());

				json.put("type", row.objType());

				json.put("op", "新增");

				array.add(json);
			}
		}

		for (IRow row : this.getDelObjects()) {
			if (row instanceof IObj) {

				IObj obj = (IObj) row;

				JSONObject json = new JSONObject();

				json.put("pid", obj.pid());

				json.put("type", obj.objType());

				json.put("op", "删除");

				array.add(json);
			}else if (row instanceof IRow){

				JSONObject json = new JSONObject();

				json.put("rowId", row.rowId());

				json.put("type", row.objType());

				json.put("op", "删除");

				array.add(json);
			}
		}

		for (IRow row : this.getUpdateObjects()) {
			if (row instanceof IObj) {

				IObj obj = (IObj) row;

				JSONObject json = new JSONObject();

				json.put("pid", obj.pid());

				json.put("type", obj.objType());

				json.put("op", "修改");

				array.add(json);
			}else if (row instanceof IRow){

				JSONObject json = new JSONObject();

				json.put("rowId", row.rowId());

				json.put("type", row.objType());

				json.put("op", "修改");

				array.add(json);
			}
		}

		return array.toString();
	}
}
