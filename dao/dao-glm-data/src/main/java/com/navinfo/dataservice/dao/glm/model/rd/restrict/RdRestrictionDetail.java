package com.navinfo.dataservice.dao.glm.model.rd.restrict;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class RdRestrictionDetail implements IObj {
	
	private int pid;

	private int restricPid;

	private int outLinkPid;

	private int flag = 2;

	private int type = 1;

	private int restricInfo;

	private int relationshipType = 1;

	private List<IRow> conditions = new ArrayList<IRow>();

	private List<IRow> vias = new ArrayList<IRow>();
	
	private String rowId;

	private Map<String, Object> changedFields = new HashMap<String, Object>();

	public Map<String, RdRestrictionCondition> conditionMap = new HashMap<String, RdRestrictionCondition>();
	
	public Map<String, RdRestrictionVia> viaMap = new HashMap<String, RdRestrictionVia>();

	//outNodePid不属于模型字段，使用protected修饰符。
	protected int outNodePid;

	public int igetOutNodePid() {
		return outNodePid;
	}

	public void isetOutNodePid(int outNodePid) {
		this.outNodePid = outNodePid;
	}
	
	public String getRowId() {
		return rowId;
	}

	public RdRestrictionDetail() {

	}

	public List<IRow> getConditions() {
		return conditions;
	}

	public void setConditions(List<IRow> conditions) {
		this.conditions = conditions;
	}

	public List<IRow> getVias() {
		return vias;
	}

	public void setVias(List<IRow> vias) {
		this.vias = vias;
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public int getRestricPid() {
		return restricPid;
	}

	public void setRestricPid(int restricPid) {
		this.restricPid = restricPid;
	}

	public int getOutLinkPid() {
		return outLinkPid;
	}

	public void setOutLinkPid(int outLinkPid) {
		this.outLinkPid = outLinkPid;
	}

	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getRestricInfo() {
		return restricInfo;
	}

	public void setRestricInfo(int restricInfo) {
		this.restricInfo = restricInfo;
	}

	public int getRelationshipType() {
		return relationshipType;
	}

	public void setRelationshipType(int relationshipType) {
		this.relationshipType = relationshipType;
	}

	@Override
	public String tableName() {

		return "rd_restriction_detail";
	}

	@Override
	public ObjStatus status() {

		return null;
	}

	@Override
	public void setStatus(ObjStatus os) {

	}

	@Override
	public ObjType objType() {

		return ObjType.RDRESTRICTIONDETAIL;
	}

	@Override
	public JSONObject Serialize(ObjLevel objLevel) {

		return JSONObject.fromObject(this, JsonUtils.getStrConfig());
	}

	@Override
	public boolean Unserialize(JSONObject json) throws Exception {

		Iterator keys = json.keys();

		while (keys.hasNext()) {
			String key = (String) keys.next();

			JSONArray ja = null;

			if (json.get(key) instanceof JSONArray) {

				switch (key) {
				case "conditions":
					conditions.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						RdRestrictionCondition row = new RdRestrictionCondition();

						row.Unserialize(jo);

						conditions.add(row);
					}

					break;
				case "vias":
					vias.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						RdRestrictionVia row = new RdRestrictionVia();

						row.Unserialize(jo);

						vias.add(row);
					}

					break;
				default:
					break;
				}

			} else {
				if (!"objStatus".equals(key)) {

					Field f = this.getClass().getDeclaredField(key);

					f.setAccessible(true);

					f.set(this, json.get(key));

				}
			}
		}

		return true;
	}

	@Override
	public List<IRow> relatedRows() {

		return null;
	}

	@Override
	public void copy(IRow row) {

		RdRestrictionDetail detail = (RdRestrictionDetail) row;

		this.outLinkPid = detail.outLinkPid;

		this.flag = detail.flag;

		this.restricInfo = detail.restricInfo;

		this.type = detail.type;

		this.relationshipType = detail.relationshipType;

		this.conditions = new ArrayList<IRow>();
		
		for (IRow condition : detail.conditions) {

			RdRestrictionCondition condCopy = new RdRestrictionCondition();

			condCopy.setDetailId(this.getPid());

			condCopy.copy(condition);

			this.conditions.add(condCopy);
		}

		this.vias = new ArrayList<IRow>();

		for (IRow via : detail.vias) {

			RdRestrictionVia viaCopy = new RdRestrictionVia();

			viaCopy.setDetailId(this.getPid());

			viaCopy.copy(via);

			this.vias.add(viaCopy);
		}
	}

	@Override
	public Map<String, Object> changedFields() {

		return changedFields;
	}

	@Override
	public int pid() {

		return this.getPid();
	}

	@Override
	public String parentPKName() {

		return "restric_pid";
	}

	@Override
	public int parentPKValue() {

		return this.getRestricPid();
	}

	@Override
	public List<List<IRow>> children() {

		List<List<IRow>> children = new ArrayList<List<IRow>>();

		children.add(this.getConditions());

		children.add(this.getVias());

		return children;
	}

	@Override
	public String parentTableName() {

		return "rd_restriction";
	}

	@Override
	public String rowId() {

		return rowId;
	}

	@Override
	public void setRowId(String rowId) {

		this.rowId = rowId;
	}

	@Override
	public boolean fillChangeFields(JSONObject json) throws Exception {

		Iterator keys = json.keys();

		while (keys.hasNext()) {
			String key = (String) keys.next();

			JSONArray ja = null;

			if (json.get(key) instanceof JSONArray) {
				continue;
			} else {
				if (!"objStatus".equals(key)) {

					Field field = RdRestrictionDetail.class
							.getDeclaredField(key);

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

	@Override
	public String primaryKey() {
		return "detail_id";
	}

	@Override
	public Map<Class<? extends IRow>, List<IRow>> childList() {
		Map<Class<? extends IRow>,List<IRow>> childList = new HashMap<Class<? extends IRow>, List<IRow>>();
		childList.put(RdRestrictionCondition.class, conditions);
		childList.put(RdRestrictionVia.class, vias);
		return childList;
	}

	@Override
	public Map<Class<? extends IRow>,Map<String,?>> childMap() {
		Map<Class<? extends IRow>,Map<String,?>> childMap = new HashMap<Class<? extends IRow>,Map<String,?>>();
		childMap.put(RdRestrictionCondition.class, conditionMap);
		childMap.put(RdRestrictionVia.class, viaMap);
		return childMap;
	}
}
