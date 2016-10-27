package com.navinfo.dataservice.dao.glm.model.poi.index;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;

/**
 * POI联系方式表
 * @author zhangxiaolong
 *
 */
public class IxPoiContact implements IRow {
	private Logger logger = Logger.getLogger(IxPoiContact.class);
	
	private int poiPid;//POI号码
	
	private int contactType=1;//联系方式类型
	
	private String contact;//联系方式
	
	private int contactDepart=0;//联系服务部门
	
	private int priority=1;//优先选择
	
	private String rowId;
	
	private int uRecord=0;
	
	private String uDate;
	
	private Map<String, Object> changedFields = new HashMap<String, Object>();
	
	public int getPoiPid() {
		return poiPid;
	}

	public void setPoiPid(int poiPid) {
		this.poiPid = poiPid;
	}

	public int getContactType() {
		return contactType;
	}

	public void setContactType(int contactType) {
		this.contactType = contactType;
	}

	public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	public int getContactDepart() {
		return contactDepart;
	}

	public void setContactDepart(int contactDepart) {
		this.contactDepart = contactDepart;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public String getRowId() {
		return rowId;
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
	public JSONObject Serialize(ObjLevel objLevel) throws Exception {
		JSONObject json = JSONObject.fromObject(this, JsonUtils.getStrConfig());

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
		return this.rowId;
	}

	@Override
	public void setRowId(String rowId) {
		this.rowId = rowId;
	}

	@Override
	public String tableName() {
		return "ix_poi_contact";
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
		return ObjType.IXPOICONTACT;
	}

	@Override
	public void copy(IRow row) {
	}

	@Override
	public Map<String, Object> changedFields() {
		return this.changedFields;
	}

	@Override
	public String parentPKName() {
		return "POI_PID";
	}

	@Override
	public int parentPKValue() {
		return this.getPoiPid();
	}

	@Override
	public String parentTableName() {
		return "ix_poi";
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
					String newValue = json.getString(key);
					if("null".equalsIgnoreCase(newValue))newValue=null;
					logger.info("objValue:"+objValue);
					logger.info("newValue:"+newValue);
					if (!isEqualsString(objValue,newValue)) {
						logger.info("isEqualsString:false");
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
	
	private static boolean isEqualsString(Object oldValue,Object newValue){
		if(null==oldValue&&null==newValue)
			return true;
		if(StringUtils.isEmpty(oldValue)&&StringUtils.isEmpty(newValue)){
			return true;
		}
		if(oldValue==null&&newValue!=null){
			return false;
		}
		if(oldValue!=null&&newValue==null){
			return false;
		}
		return oldValue.toString().equals(newValue.toString());
	}

	@Override
	public int mesh() {
		return 0;
	}

	@Override
	public void setMesh(int mesh) {
	}

}
