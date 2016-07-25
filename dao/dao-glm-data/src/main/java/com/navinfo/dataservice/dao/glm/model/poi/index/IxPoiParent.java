package com.navinfo.dataservice.dao.glm.model.poi.index;

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


/**
 * POI父子关系父表
 * @author luyao
 *
 */
public class IxPoiParent implements IObj {
	
	private String rowId;
	
	private int pid;
	
	private int parentPoiPid;//父POI号码
	
	private int tenantFlag=0;//有无租户
	
	private String memo;//备注信息
	
	
	
	private int uRecord=0;
	
	private String uDate;
	
	private Map<String, Object> changedFields = new HashMap<String, Object>();
	
	private List<IRow> poiChildrens = new ArrayList<IRow>();
	
	public Map<String, IxPoiChildren> poiChildrenMap = new HashMap<String, IxPoiChildren>();

	public IxPoiParent() {

	}
	
	public int getPid() {
		return pid;
	}

	public void setPid(int Pid) {
		this.pid = Pid;
	}

	public int getParentPoiPid() {
		return parentPoiPid;
	}

	public void setParentPoiPid(int parentPoiPid) {
		this.parentPoiPid = parentPoiPid;
	}

	public int getTenantFlag() {
		return tenantFlag;
	}

	public void setTenantFlag(int tenantFlag) {
		this.tenantFlag = tenantFlag;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public List<IRow> getPoiChildrens() {
		return poiChildrens;
	}

	public void setPoiChildrens(List<IRow> poiChildrens) {
		this.poiChildrens = poiChildrens;
	}
	
	@Override
	public String rowId() {
		return rowId;
	}
	
	public String getRowId() {
		return rowId;
	}

	@Override
	public void setRowId(String rowId) {
		this.rowId = rowId;
	}


	public int getuRecord() {
		return uRecord;
	}

	public void setuRecord(int uRecord) {
		this.uRecord = uRecord;
	}

	public String getuDate() {
		return uDate;
	}

	public void setuDate(String uDate) {
		this.uDate = uDate;
	}

	@Override
	public String tableName() {

		return "ix_poi_parent";
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
		return ObjType.IXPOIPARENT;
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
		return "group_id";
	}

	@Override
	public int parentPKValue() {
		return this.getPid();
	}

	@Override
	public String parentTableName() {
		return "ix_poi_parent";
	}

	@Override
	public List<List<IRow>> children() {
		List<List<IRow>> children = new ArrayList<List<IRow>>();
		
		children.add(this.getPoiChildrens());
		
		return children;
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

	@Override
	public JSONObject Serialize(ObjLevel objLevel) throws Exception {
		return JSONObject.fromObject(this, JsonUtils.getStrConfig());		
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
	public List<IRow> relatedRows() {
		
		return null;
	}

	@Override
	public int pid() {
		return this.getPid();
	}

	@Override
	public String primaryKey() {
		return "group_id";
	}

}
