package com.navinfo.dataservice.dao.glm.model.poi.index;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkLimit;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkName;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

/**
 * POI基础信息表
 * 
 * @author zhangxiaolong
 *
 */
public class IxPoi implements IObj {

	private int pid;

	private String kindCode;

	private Geometry geometry;

	private double xGuide;

	private double yGuide;

	private int linkPid;

	private int side;

	private int nameGroupid;

	private int roadFlag;

	private int pmeshId;

	private int adminReal;

	private int importance;

	private String chain;

	private String airportCode;

	private int accessFlag;

	private int open24h;

	private String meshId5k;

	private int meshId;

	private int regionId;

	private String postCode;

	private String difGroupid;

	private int editFlag;

	private String reserved;

	private int state;

	private String fieldState;

	private String label;

	private int type;

	private int addressFlag;

	private String exPriority;

	private String editionFlag;

	private String poiMemo;

	private String oldBlockcode;

	private String oldName;

	private String oldAddress;

	private String oldKind;

	private String poiNum;

	private String log;

	private int taskId;

	private String dataVersion;

	private int fieldTaskId;

	private int verifiedFlag;

	private String collectTime;

	private int geoAdjustFlag;

	private int fullAttrFlag;

	private double oldXGuide;

	private double oldYGuide;

	private String rowId;

	private Map<String, Object> changedFields = new HashMap<String, Object>();

	private List<IRow> addresses = new ArrayList<IRow>();

	public Map<String, IxPoiAddress> addressMap = new HashMap<String, IxPoiAddress>();

	private List<IRow> audioes = new ArrayList<IRow>();

	public Map<String, IxPoiAudio> audioMap = new HashMap<String, IxPoiAudio>();

	private List<IRow> contacts = new ArrayList<IRow>();

	public Map<String, IxPoiContact> contactMap = new HashMap<String, IxPoiContact>();

	private List<IRow> entryImages = new ArrayList<IRow>();

	public Map<String, IxPoiEntryimage> entryImageMap = new HashMap<String, IxPoiEntryimage>();

	private List<IRow> flags = new ArrayList<IRow>();

	public Map<String, IxPoiFlag> flagMap = new HashMap<String, IxPoiFlag>();

	private List<IRow> icons = new ArrayList<IRow>();

	public Map<String, IxPoiIcon> iconMap = new HashMap<String, IxPoiIcon>();

	private List<IRow> names = new ArrayList<IRow>();

	public Map<String, IxPoiName> nameMap = new HashMap<String, IxPoiName>();

	private List<IRow> parents = new ArrayList<IRow>();

	public Map<String, IXPoiParent> parentMap = new HashMap<String, IXPoiParent>();

	private List<IRow> photoes = new ArrayList<IRow>();

	public Map<String, IxPoiPhoto> photoMap = new HashMap<String, IxPoiPhoto>();

	private List<IRow> videoes = new ArrayList<IRow>();

	public Map<String, IXPoiVideo> videoMap = new HashMap<String, IXPoiVideo>();

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public String getKindCode() {
		return kindCode;
	}

	public void setKindCode(String kindCode) {
		this.kindCode = kindCode;
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}

	public double getxGuide() {
		return xGuide;
	}

	public void setxGuide(double xGuide) {
		this.xGuide = xGuide;
	}

	public double getyGuide() {
		return yGuide;
	}

	public void setyGuide(double yGuide) {
		this.yGuide = yGuide;
	}

	public int getLinkPid() {
		return linkPid;
	}

	public void setLinkPid(int linkPid) {
		this.linkPid = linkPid;
	}

	public int getSide() {
		return side;
	}

	public void setSide(int side) {
		this.side = side;
	}

	public int getNameGroupid() {
		return nameGroupid;
	}

	public void setNameGroupid(int nameGroupid) {
		this.nameGroupid = nameGroupid;
	}

	public int getRoadFlag() {
		return roadFlag;
	}

	public void setRoadFlag(int roadFlag) {
		this.roadFlag = roadFlag;
	}

	public int getPmeshId() {
		return pmeshId;
	}

	public void setPmeshId(int pmeshId) {
		this.pmeshId = pmeshId;
	}

	public int getAdminReal() {
		return adminReal;
	}

	public void setAdminReal(int adminReal) {
		this.adminReal = adminReal;
	}

	public int getImportance() {
		return importance;
	}

	public void setImportance(int importance) {
		this.importance = importance;
	}

	public String getChain() {
		return chain;
	}

	public void setChain(String chain) {
		this.chain = chain;
	}

	public String getAirportCode() {
		return airportCode;
	}

	public void setAirportCode(String airportCode) {
		this.airportCode = airportCode;
	}

	public int getAccessFlag() {
		return accessFlag;
	}

	public void setAccessFlag(int accessFlag) {
		this.accessFlag = accessFlag;
	}

	public int getOpen24h() {
		return open24h;
	}

	public void setOpen24h(int open24h) {
		this.open24h = open24h;
	}

	public String getMeshId5k() {
		return meshId5k;
	}

	public void setMeshId5k(String meshId5k) {
		this.meshId5k = meshId5k;
	}

	public int getMeshId() {
		return meshId;
	}

	public void setMeshId(int meshId) {
		this.meshId = meshId;
	}

	public int getRegionId() {
		return regionId;
	}

	public void setRegionId(int regionId) {
		this.regionId = regionId;
	}

	public String getPostCode() {
		return postCode;
	}

	public void setPostCode(String postCode) {
		this.postCode = postCode;
	}

	public String getDifGroupid() {
		return difGroupid;
	}

	public void setDifGroupid(String difGroupid) {
		this.difGroupid = difGroupid;
	}

	public int getEditFlag() {
		return editFlag;
	}

	public void setEditFlag(int editFlag) {
		this.editFlag = editFlag;
	}

	public String getReserved() {
		return reserved;
	}

	public void setReserved(String reserved) {
		this.reserved = reserved;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getFieldState() {
		return fieldState;
	}

	public void setFieldState(String fieldState) {
		this.fieldState = fieldState;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getAddressFlag() {
		return addressFlag;
	}

	public void setAddressFlag(int addressFlag) {
		this.addressFlag = addressFlag;
	}

	public String getExPriority() {
		return exPriority;
	}

	public void setExPriority(String exPriority) {
		this.exPriority = exPriority;
	}

	public String getEditionFlag() {
		return editionFlag;
	}

	public void setEditionFlag(String editionFlag) {
		this.editionFlag = editionFlag;
	}

	public String getPoiMemo() {
		return poiMemo;
	}

	public void setPoiMemo(String poiMemo) {
		this.poiMemo = poiMemo;
	}

	public String getOldBlockcode() {
		return oldBlockcode;
	}

	public void setOldBlockcode(String oldBlockcode) {
		this.oldBlockcode = oldBlockcode;
	}

	public String getOldName() {
		return oldName;
	}

	public void setOldName(String oldName) {
		this.oldName = oldName;
	}

	public String getOldAddress() {
		return oldAddress;
	}

	public void setOldAddress(String oldAddress) {
		this.oldAddress = oldAddress;
	}

	public String getOldKind() {
		return oldKind;
	}

	public void setOldKind(String oldKind) {
		this.oldKind = oldKind;
	}

	public String getPoiNum() {
		return poiNum;
	}

	public void setPoiNum(String poiNum) {
		this.poiNum = poiNum;
	}

	public String getLog() {
		return log;
	}

	public void setLog(String log) {
		this.log = log;
	}

	public int getTaskId() {
		return taskId;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

	public String getDataVersion() {
		return dataVersion;
	}

	public void setDataVersion(String dataVersion) {
		this.dataVersion = dataVersion;
	}

	public int getFieldTaskId() {
		return fieldTaskId;
	}

	public void setFieldTaskId(int fieldTaskId) {
		this.fieldTaskId = fieldTaskId;
	}

	public int getVerifiedFlag() {
		return verifiedFlag;
	}

	public void setVerifiedFlag(int verifiedFlag) {
		this.verifiedFlag = verifiedFlag;
	}

	public String getCollectTime() {
		return collectTime;
	}

	public void setCollectTime(String collectTime) {
		this.collectTime = collectTime;
	}

	public int getGeoAdjustFlag() {
		return geoAdjustFlag;
	}

	public void setGeoAdjustFlag(int geoAdjustFlag) {
		this.geoAdjustFlag = geoAdjustFlag;
	}

	public int getFullAttrFlag() {
		return fullAttrFlag;
	}

	public void setFullAttrFlag(int fullAttrFlag) {
		this.fullAttrFlag = fullAttrFlag;
	}

	public double getOldXGuide() {
		return oldXGuide;
	}

	public void setOldXGuide(double oldXGuide) {
		this.oldXGuide = oldXGuide;
	}

	public double getOldYGuide() {
		return oldYGuide;
	}

	public void setOldYGuide(double oldYGuide) {
		this.oldYGuide = oldYGuide;
	}

	public Map<String, Object> getChangedFields() {
		return changedFields;
	}

	public void setChangedFields(Map<String, Object> changedFields) {
		this.changedFields = changedFields;
	}

	public List<IRow> getAddresses() {
		return addresses;
	}

	public void setAddresses(List<IRow> addresses) {
		this.addresses = addresses;
	}

	public List<IRow> getAudioes() {
		return audioes;
	}

	public void setAudioes(List<IRow> audioes) {
		this.audioes = audioes;
	}

	public List<IRow> getContacts() {
		return contacts;
	}

	public void setContacts(List<IRow> contacts) {
		this.contacts = contacts;
	}

	public List<IRow> getEntryImages() {
		return entryImages;
	}

	public void setEntryImages(List<IRow> entryImages) {
		this.entryImages = entryImages;
	}

	public List<IRow> getFlags() {
		return flags;
	}

	public void setFlags(List<IRow> flags) {
		this.flags = flags;
	}

	public List<IRow> getIcons() {
		return icons;
	}

	public void setIcons(List<IRow> icons) {
		this.icons = icons;
	}

	public List<IRow> getNames() {
		return names;
	}

	public void setNames(List<IRow> names) {
		this.names = names;
	}

	public List<IRow> getParents() {
		return parents;
	}

	public void setParents(List<IRow> parents) {
		this.parents = parents;
	}

	public List<IRow> getPhotoes() {
		return photoes;
	}

	public void setPhotoes(List<IRow> photoes) {
		this.photoes = photoes;
	}

	public List<IRow> getVideoes() {
		return videoes;
	}

	public void setVideoes(List<IRow> videoes) {
		this.videoes = videoes;
	}

	public String getRowId() {
		return rowId;
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
		return "ix_poi";
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
		return ObjType.IXPOI;
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
		return "pid";
	}

	@Override
	public int parentPKValue() {
		return this.pid;
	}

	@Override
	public String parentTableName() {
		return "ix_poi";
	}

	@Override
	public List<List<IRow>> children() {
		List<List<IRow>> children = new ArrayList<List<IRow>>();
		children.add(this.getAddresses());
		children.add(this.getAudioes());
		children.add(this.getContacts());
		children.add(this.getEntryImages());
		children.add(this.getFlags());
		children.add(this.getIcons());
		children.add(this.getNames());
		children.add(this.getParents());
		children.add(this.getPhotoes());
		children.add(this.getVideoes());
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
			} else if ("geometry".equals(key)) {

				JSONObject geojson = json.getJSONObject(key);

				String wkt = Geojson.geojson2Wkt(geojson.toString());

				String oldwkt = GeoTranslator.jts2Wkt(geometry, 0.00001, 5);

				if (!wkt.equals(oldwkt)) {
					double length = GeometryUtils.getLinkLength(GeoTranslator.geojson2Jts(geojson));

					changedFields.put("length", length);

					changedFields.put(key, json.getJSONObject(key));
				}
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
		return this.meshId;
	}

	@Override
	public void setMesh(int mesh) {
		this.meshId = mesh;
	}

	@Override
	public JSONObject Serialize(ObjLevel objLevel) throws Exception {
		JsonConfig jsonConfig = Geojson.geoJsonConfig(0.00001, 5);

		JSONObject json = JSONObject.fromObject(this, jsonConfig);

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
				case "addresses":
					addresses.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						RdLinkForm row = new RdLinkForm();

						row.Unserialize(jo);

						addresses.add(row);
					}

					break;
				case "audioes":

					audioes.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						RdLinkLimit row = new RdLinkLimit();

						row.Unserialize(jo);

						audioes.add(row);
					}

					break;

				case "entryImages":

					entryImages.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						RdLinkName row = new RdLinkName();

						row.Unserialize(jo);

						entryImages.add(row);
					}

					break;
				case "flags":

					flags.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						RdLinkName row = new RdLinkName();

						row.Unserialize(jo);

						flags.add(row);
					}

					break;
				case "icons":

					icons.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						RdLinkName row = new RdLinkName();

						row.Unserialize(jo);

						icons.add(row);
					}

					break;
				case "names":

					names.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						RdLinkName row = new RdLinkName();

						row.Unserialize(jo);

						names.add(row);
					}

					break;
				case "parents":

					parents.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						RdLinkName row = new RdLinkName();

						row.Unserialize(jo);

						parents.add(row);
					}

					break;
				case "photoes":

					photoes.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						RdLinkName row = new RdLinkName();

						row.Unserialize(jo);

						photoes.add(row);
					}

					break;
				case "videoes":

					videoes.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						RdLinkName row = new RdLinkName();

						row.Unserialize(jo);

						videoes.add(row);
					}
					break;
				default:
					break;
				}

			} else if ("geometry".equals(key)) {

				Geometry jts = GeoTranslator.geojson2Jts(json.getJSONObject(key), 100000, 0);

				this.setGeometry(jts);

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
		return this.pid;
	}

	@Override
	public String primaryKey() {
		return "pid";
	}

}
