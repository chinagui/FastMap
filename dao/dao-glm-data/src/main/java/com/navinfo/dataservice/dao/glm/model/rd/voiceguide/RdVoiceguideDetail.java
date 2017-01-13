package com.navinfo.dataservice.dao.glm.model.rd.voiceguide;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;

public class RdVoiceguideDetail implements IObj {

	private String rowId;

	private int pid;

	private int voiceguidePid;// 语音引导

	private int outLinkPid;// 退出link

	private int guideCode = 0;// 语音代码

	private int guideType = 0;// 语音类型

	private int processFlag = 1;// 处理标志

	private int relationshipType = 1;// 关系类型
	
	protected ObjStatus status;

	private Map<String, Object> changedFields = new HashMap<String, Object>();

	private List<IRow> vias = new ArrayList<IRow>();

	public Map<String, RdVoiceguideVia> directrouteViaMap = new HashMap<String, RdVoiceguideVia>();

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public int getVoiceguidePid() {
		return voiceguidePid;
	}

	public void setVoiceguidePid(int voiceguidePid) {
		this.voiceguidePid = voiceguidePid;
	}

	public int getOutLinkPid() {
		return outLinkPid;
	}

	public void setOutLinkPid(int outLinkPid) {
		this.outLinkPid = outLinkPid;
	}

	public int getGuideCode() {
		return guideCode;
	}

	public void setGuideCode(int guideCode) {
		this.guideCode = guideCode;
	}

	public int getGuideType() {
		return guideType;
	}

	public void setGuideType(int guideType) {
		this.guideType = guideType;
	}

	public int getProcessFlag() {
		return processFlag;
	}

	public void setProcessFlag(int processFlag) {
		this.processFlag = processFlag;
	}

	public int getRelationshipType() {
		return relationshipType;
	}

	public void setRelationshipType(int relationshipType) {
		this.relationshipType = relationshipType;
	}

	public List<IRow> getVias() {
		return vias;
	}

	public void setVias(List<IRow> vias) {
		this.vias = vias;
	}

	public String getRowId() {
		return rowId;
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
	public String tableName() {
		return "rd_voiceguide_detail";
	}

	@Override
	public ObjStatus status() {
		// TODO Auto-generated method stub
		return status;
	}

	@Override
	public void setStatus(ObjStatus os) {
		// TODO Auto-generated method stub
		status = os;
	}

	@Override
	public ObjType objType() {
		return ObjType.RDVOICEGUIDEDETAIL;
	}

	@Override
	public void copy(IRow row) {
		// TODO Auto-generated method stub

	}

	@Override
	public Map<String, Object> changedFields() {
		return changedFields;
	}

	@Override
	public String parentPKName() {
		return "voiceguide_pid";
	}

	@Override
	public int parentPKValue() {
		return this.voiceguidePid;
	}

	@Override
	public String parentTableName() {
		return "rd_voiceguide";
	}

	@Override
	public List<List<IRow>> children() {

		List<List<IRow>> children = new ArrayList<List<IRow>>();

		children.add(this.vias);

		return children;
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

						if (value instanceof String) {
							changedFields.put(key, newValue.replace("'", "''"));
						} else {
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
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setMesh(int mesh) {
		// TODO Auto-generated method stub

	}

	@Override
	public JSONObject Serialize(ObjLevel objLevel) throws Exception {
		//return JSONObject.fromObject(this, JsonUtils.getStrConfig());
		JSONObject json = JSONObject.fromObject(this,JsonUtils.getStrConfig());
		
		if (objLevel == ObjLevel.HISTORY) {
			json.remove("status");
		}
		return json;
	}

	@Override
	public boolean Unserialize(JSONObject json) throws Exception {
		@SuppressWarnings("rawtypes")
		Iterator keys = json.keys();

		while (keys.hasNext()) {

			String key = (String) keys.next();

			JSONArray ja = null;

			if (json.get(key) instanceof JSONArray) {
				switch (key) {
				case "vias":
					vias.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						RdVoiceguideVia row = new RdVoiceguideVia();

						row.Unserialize(jo);

						vias.add(row);
					}

					break;
				}
			} else if (!"objStatus".equals(key)) {

				Field f = this.getClass().getDeclaredField(key);

				f.setAccessible(true);

				f.set(this, json.get(key));
			}
		}
		return true;
	}

	@Override
	public List<IRow> relatedRows() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int pid() {
		return this.pid;
	}

	@Override
	public String primaryKey() {
		return "detail_id";
	}

	@Override
	public Map<Class<? extends IRow>, List<IRow>> childList() {

		Map<Class<? extends IRow>, List<IRow>> childMap = new HashMap<>();

		childMap.put(RdVoiceguideVia.class, this.vias);

		return childMap;
	}

	@Override
	public Map<Class<? extends IRow>, Map<String, ?>> childMap() {

		Map<Class<? extends IRow>, Map<String, ?>> childMap = new HashMap<Class<? extends IRow>, Map<String, ?>>();

		childMap.put(RdVoiceguideVia.class, directrouteViaMap);

		return childMap;
	}

}
