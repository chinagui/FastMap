/**
 * 
 */
package com.navinfo.dataservice.dao.glm.model.rd.variablespeed;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.IVia;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;

/** 
* @ClassName: RdVariableSpeedIA 
* @author Zhang Xiaolong
* @date 2016年8月15日 下午5:17:19 
* @Description: TODO
*/
public class RdVariableSpeedVia implements IRow,IVia{

	private int vspeedPid ;//可变限速号码

	private int linkPid;//LINK 号码幅号码 

	private int seqNum =1 ;//LINK 序号;
	private String rowId;
	
	protected ObjStatus status;

	private Map<String, Object> changedFields = new HashMap<String, Object>();

	@Override
	public JSONObject Serialize(ObjLevel objLevel) throws Exception {
//		return JSONObject.fromObject(this, JsonUtils.getStrConfig());
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

			if (!"objStatus".equals(key)) {

				Field f = this.getClass().getDeclaredField(key);

				f.setAccessible(true);

				f.set(this, json.get(key));

			}

		}
		return true;
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
		return "rd_variable_speed_via";
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
		return ObjType.RDVARIABLESPEEDVIA;
	}

	@Override
	public void copy(IRow row) {
	}

	@Override
	public Map<String, Object> changedFields() {
		return changedFields;
	}

	@Override
	public String parentPKName() {
		return "vspeed_pid";
	}

	@Override
	public int parentPKValue() {
		return this.getVspeedPid();
	}

	@Override
	public String parentTableName() {
		return "rd_variable_speed";
	}

	@Override
	public List<List<IRow>> children() {
		return null;
	}

	@Override
	public boolean fillChangeFields(JSONObject json) throws Exception {
		@SuppressWarnings("rawtypes")
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
		return 0;
	}

	@Override
	public void setMesh(int mesh) {
	}

	public int getLinkPid() {
		return linkPid;
	}

	public void setLinkPid(int linkPid) {
		this.linkPid = linkPid;
	}

	@Override
	public int getSeqNum() {
		return seqNum;
	}

	@Override
	public void setSeqNum(int seqNum) {
		this.seqNum = seqNum;
	}

	public String getRowId() {
		return rowId;
	}

	public int getVspeedPid() {
		return vspeedPid;
	}

	public void setVspeedPid(int vspeedPid) {
		this.vspeedPid = vspeedPid;
	}
	
}
