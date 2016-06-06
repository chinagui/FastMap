package com.navinfo.dataservice.dao.glm.model.poi.deep;

import java.lang.reflect.Field;
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
 * 索引:POI 深度信息(重大事件类)
 * @author zhaokk
 *
 */
public class IxPoiEvent implements IObj {

	private int pid;
	private String eventName  ;//事件名称
	private String evenNameEng  ;//事件名称英文名称 
	private String evnetKind;//事件类型 
	private String evnetKindEng;//英文事件类型  
	
	private String evnetDesc;//事件描述 
	private String evnetDEscEng;//英文事件描述 
	
	private String startDate;//事件开始日期
	private String endDate ;//事件结束日期
	
	private String detailTime;//事件详细时间 
	private String detailTimeEng;//事件详细时间 英文
	private String city ;//所属城市
	private String poiPid  ;//关联 POI
	private String photoName ;//照片名称 
	private String memo;
	private String reserved;//预留字段
	private int mesh;
	private String rowId;
	
	

	public int getMesh() {
		return mesh;
	}

	
	public String getRowId() {
		return rowId;
	}
 
    private Map<String, Object> changedFields = new HashMap<String, Object>();   
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
		return "ix_poi_event";
	}
	

	

	

	public String getPoiPid() {
		return poiPid;
	}


	public void setPoiPid(String poiPid) {
		this.poiPid = poiPid;
	}


	public String getMemo() {
		return memo;
	}


	public void setMemo(String memo) {
		this.memo = memo;
	}


	public String getReserved() {
		return reserved;
	}


	public void setReserved(String reserved) {
		this.reserved = reserved;
	}


	@Override
	public ObjStatus status() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	

	
	@Override
	public void setStatus(ObjStatus os) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ObjType objType() {
		return ObjType.IXPOIEVENT;
	}

	@Override
	public void copy(IRow row) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<String, Object> changedFields() {
		// TODO Auto-generated method stub
		return this.changedFields;
	}

	@Override
	public String parentPKName() {
		// TODO Auto-generated method stub
		return "event_id";
	}

	@Override
	public int parentPKValue() {
		// TODO Auto-generated method stub
		return this.getPid();
	}

	@Override
	public String parentTableName() {
		// TODO Auto-generated method stub
		return "ix_poi_event";
	}

	public Map<String, Object> getChangedFields() {
		return changedFields;
	}

	public void setChangedFields(Map<String, Object> changedFields) {
		this.changedFields = changedFields;
	}

	@Override
	public List<List<IRow>> children() {
		return null;
	}

	@Override
	public boolean fillChangeFields(JSONObject json) throws Exception {
		Iterator keys = json.keys();

		while (keys.hasNext()) {
			String key = (String) keys.next();

			if (json.get(key) instanceof JSONArray) {
				continue;
			} else {
				if ( !"objStatus".equals(key)) {
					
					Field field = this.getClass().getDeclaredField(key);
					
					field.setAccessible(true);
					
					Object objValue = field.get(this);
					
					String oldValue = null;
					
					if (objValue == null){
						oldValue = "null";
					}else{
						oldValue = String.valueOf(objValue);
					}
					
					String newValue = json.getString(key);
					
					if (!newValue.equals(oldValue)){
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
		
		if (changedFields.size() >0){
			return true;
		}else{
			return false;
		}

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
	public int mesh() {
		// TODO Auto-generated method stub
		return this.mesh;
	}

	@Override
	public void setMesh(int mesh) {
		this.mesh = mesh;
		
	}

	@Override
	public List<IRow> relatedRows() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int pid() {
		// TODO Auto-generated method stub
		return this.getPid();
	}

	@Override
	public String primaryKey() {
		return "event_id";
	}


}
