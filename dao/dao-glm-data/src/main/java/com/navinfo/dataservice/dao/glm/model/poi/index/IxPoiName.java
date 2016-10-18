package com.navinfo.dataservice.dao.glm.model.poi.index;

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

/**
 * POI名称表
 * 
 * @author zhangxiaolong
 * 
 */
public class IxPoiName implements IObj {

	private int pid;

	private int poiPid;// POI号码

	private int nameGroupid = 1;// 名称组号

	private String langCode;// 语言代码

	private int nameClass = 1;// 名称分类

	private int nameType = 1;// 名称内容

	private String name;// 名称内容

	private String namePhonetic;// 名称发音

	private String keywords;// 关键字

	private String nidbPid;// 现有PID

	private String rowId;

	private int uRecord = 0;

	private String uDate;

	private Map<String, Object> changedFields = new HashMap<String, Object>();

	private List<IRow> nameTones = new ArrayList<IRow>();

	private List<IRow> nameFlags = new ArrayList<IRow>();

	public Map<String, IxPoiNameFlag> nameFlagMap = new HashMap<String, IxPoiNameFlag>();

	public Map<String, IxPoiNameTone> nameToneMap = new HashMap<String, IxPoiNameTone>();

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public int getPoiPid() {
		return poiPid;
	}

	public void setPoiPid(int poiPid) {
		this.poiPid = poiPid;
	}

	public int getNameGroupid() {
		return nameGroupid;
	}

	public void setNameGroupid(int nameGroupid) {
		this.nameGroupid = nameGroupid;
	}

	public String getLangCode() {
		return langCode;
	}

	public void setLangCode(String langCode) {
		this.langCode = langCode;
	}

	public int getNameClass() {
		return nameClass;
	}

	public void setNameClass(int nameClass) {
		this.nameClass = nameClass;
	}

	public int getNameType() {
		return nameType;
	}

	public void setNameType(int nameType) {
		this.nameType = nameType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNamePhonetic() {
		return namePhonetic;
	}

	public void setNamePhonetic(String namePhonetic) {
		this.namePhonetic = namePhonetic;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public String getNidbPid() {
		return nidbPid;
	}

	public void setNidbPid(String nidbPid) {
		this.nidbPid = nidbPid;
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

	public List<IRow> getNameTones() {
		return nameTones;
	}

	public void setNameTones(List<IRow> nameTones) {
		this.nameTones = nameTones;
	}

	public List<IRow> getNameFlags() {
		return nameFlags;
	}

	public void setNameFlags(List<IRow> nameFlags) {
		this.nameFlags = nameFlags;
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
		return "ix_poi_name";
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
		return ObjType.IXPOINAME;
	}

	@Override
	public void copy(IRow row) {
		IxPoiName nameSource = (IxPoiName) row;

		this.setName(nameSource.getName());

		this.setPoiPid(nameSource.getPoiPid());

		this.setNameGroupid(nameSource.getNameGroupid());

		this.setNameClass(nameSource.getNameClass());

		this.setNameType(nameSource.getNameType());

		this.setRowId(nameSource.getRowId());
	}

	@Override
	public Map<String, Object> changedFields() {
		return changedFields;
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
		List<List<IRow>> children = new ArrayList<List<IRow>>();
		children.add(this.nameTones);
		children.add(this.nameFlags);
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

		if (changedFields.size() > 0)

		{
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
		JSONObject json = JSONObject.fromObject(this, JsonUtils.getStrConfig());

		if (objLevel == ObjLevel.HISTORY) {
			json.remove("name");
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

				case "nameTones":
					nameTones.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						IxPoiNameTone row = new IxPoiNameTone();

						row.Unserialize(jo);

						nameTones.add(row);
					}

					break;
				case "nameFlags":
					nameFlags.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						IxPoiNameFlag row = new IxPoiNameFlag();

						row.Unserialize(jo);

						nameFlags.add(row);
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
	public int pid() {
		return this.getPid();
	}

	@Override
	public String primaryKey() {
		return "name_id";
	}

	@Override
	public Map<Class<? extends IRow>, List<IRow>> childList() {
		Map<Class<? extends IRow>, List<IRow>> childMap = new HashMap<>();

		childMap.put(IxPoiNameTone.class, nameTones);

		childMap.put(IxPoiNameFlag.class, nameFlags);

		return childMap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.navinfo.dataservice.dao.glm.iface.IObj#childMap()
	 */
	@Override
	public Map<Class<? extends IRow>, Map<String, ?>> childMap() {
		Map<Class<? extends IRow>, Map<String, ?>> childMap = new HashMap<Class<? extends IRow>, Map<String, ?>>();

		childMap.put(IxPoiNameTone.class, nameToneMap);

		childMap.put(IxPoiNameFlag.class, nameFlagMap);

		return childMap;
	}

}
