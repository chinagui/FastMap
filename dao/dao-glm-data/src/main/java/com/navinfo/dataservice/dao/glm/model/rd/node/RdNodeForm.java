package com.navinfo.dataservice.dao.glm.model.rd.node;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;

public class RdNodeForm implements IRow {
	
	private int nodePid;

	private int formOfWay = 1;

	private int auxiFlag;

	private String rowId;
	
	protected ObjStatus status;

	private Map<String, Object> changedFields = new HashMap<String, Object>();

	public String getRowId() {
		return rowId;
	}

	public void setRowId(String rowId) {
		this.rowId = rowId;
	}

	public RdNodeForm() {

	}

	public int getNodePid() {
		return nodePid;
	}

	public void setNodePid(int nodePid) {
		this.nodePid = nodePid;
	}

	public int getFormOfWay() {
		return formOfWay;
	}

	public void setFormOfWay(int formOfWay) {
		this.formOfWay = formOfWay;
	}

	public int getAuxiFlag() {
		return auxiFlag;
	}

	public void setAuxiFlag(int auxiFlag) {
		this.auxiFlag = auxiFlag;
	}

	@Override
	public JSONObject Serialize(ObjLevel objLevel) {

		//return JSONObject.fromObject(this,JsonUtils.getStrConfig());
		JSONObject json = JSONObject.fromObject(this, JsonUtils.getStrConfig());
		if (objLevel == ObjLevel.HISTORY) {
			json.remove("status");
		}
		return json;
	}

	@Override
	public boolean Unserialize(JSONObject json) throws Exception {

		Iterator keys = json.keys();

		while (keys.hasNext()) {

			String key = (String) keys.next();

			if (!"objStatus".equals(key)) {

				Field f = this.getClass().getDeclaredField(key);

				f.setAccessible(true);

				f.set(this, json.get(key));
			}

		}
		return true;
	}

	@Override
	public String tableName() {

		return "rd_node_form";
	}

	@Override
	public ObjStatus status() {

		return status;
	}

	@Override
	public void setStatus(ObjStatus os) {
		status = os;
	}

	@Override
	public ObjType objType() {

		return ObjType.RDNODEFORM;
	}

	@Override
	public void copy(IRow row) {

		RdNodeForm form = (RdNodeForm) row;

		this.formOfWay = form.formOfWay;

		this.auxiFlag = form.auxiFlag;
		
	}

	@Override
	public Map<String, Object> changedFields() {

		return changedFields;
	}

	@Override
	public String parentPKName() {

		return "node_pid";
	}

	@Override
	public int parentPKValue() {

		return this.getNodePid();
	}

	@Override
	public List<List<IRow>> children() {

		return null;
	}

	@Override
	public String parentTableName() {

		return "rd_node";
	}

	@Override
	public String rowId() {

		return rowId;
	}

	@Override
	public boolean fillChangeFields(JSONObject json) throws Exception {

		Iterator keys = json.keys();

		while (keys.hasNext()) {
			String key = (String) keys.next();

			if (json.get(key) instanceof JSONArray) {
				continue;
			} else {
				if (!"objStatus".equals(key)) {

					Field field = this.getClass().getDeclaredField(key);

					field.setAccessible(true);

					Object objValue = field.get(this);

					String oldValue = null;

					if (objValue == null) {
						oldValue = "null";
					} else {
						oldValue = String.valueOf(objValue);
					}

					String newValue = json.getString(key);

					if (!newValue.equals(oldValue)) {
						Object value = json.get(key);
						
						if(value instanceof String){
							changedFields.put(key, newValue.replace("'", "''"));
						}
						else{
							changedFields.put(key, value);
						}

					}

				}
			}
		}

		if (changedFields.size() > 0) {
			return true;
		} else {
			return false;
		}

	}

	@Override
	public int mesh() {
		return 0;
	}

	@Override
	public void setMesh(int mesh) {
	}

}
