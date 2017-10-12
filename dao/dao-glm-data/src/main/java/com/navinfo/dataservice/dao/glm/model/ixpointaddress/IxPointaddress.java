package com.navinfo.dataservice.dao.glm.model.ixpointaddress;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.util.SerializeUtils;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.vividsolutions.jts.geom.Geometry;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

/**
 * IxPointaddress基础信息表
 * 
 * @author zl
 * 
 */
public class IxPointaddress implements IObj {
	private Logger logger = Logger.getLogger(IxPointaddress.class);
	// Pa号码
	private int pid;

	// 显示坐标
	private Geometry geometry;

	// 引导X坐标
	private double xGuide = 0;

	// 引导Y坐标
	private double yGuide = 0;

	// 
	private int guideLinkPid = 0;
	
	// 
	private int locateLinkPid = 0;
	
	// 
	private int locateNameGroupid = 0;

	private int guideLinkSide = 0;
	
	private int locateLinkSide = 0;
	
	private int srcPid = 0;
	
	private int meshId = 0;
	
	// 区划号码
	private int regionId = 0 ;
	
	// 编辑标识
	private int editFlag = 1;
	
	//
	private String idcode ;
	
	private String dprName ;
	
	private String dpName ;
	
	private String operator ;
	
	private String memoire ;
	
	private String dpfName ;
	
	private String posterId ;
	
	private int addressFlag ;
	
	private String verifed = "F";
	
	private String memo ;
	
	private String reserved ;
	
	private String srcType ;
	
	// 外业LOG
	private String log;

	// 任务编号
	private int taskId = 0;

	// 数据采集版本
	private String dataVersion;

	// 外业任务编号
	private int fieldTaskId = 0;
	// 记录状态
	private int state = 0;
	
	// 行记录ID
	private String rowId;

	// 更新记录*
	private int uRecord;
	// POI等级

	// 更新时间
	private String uDate;
	
	private Map<String, Object> changedFields = new HashMap<String, Object>();
	//**************************

	@Override
	public String rowId() {
		return this.rowId;
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
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

	public int getGuideLinkPid() {
		return guideLinkPid;
	}

	public void setGuideLinkPid(int guideLinkPid) {
		this.guideLinkPid = guideLinkPid;
	}

	public int getLocateLinkPid() {
		return locateLinkPid;
	}

	public void setLocateLinkPid(int locateLinkPid) {
		this.locateLinkPid = locateLinkPid;
	}

	public int getLocateNameGroupid() {
		return locateNameGroupid;
	}

	public void setLocateNameGroupid(int locateNameGroupid) {
		this.locateNameGroupid = locateNameGroupid;
	}

	public int getGuideLinkSide() {
		return guideLinkSide;
	}

	public void setGuideLinkSide(int guideLinkSide) {
		this.guideLinkSide = guideLinkSide;
	}

	public int getLocateLinkSide() {
		return locateLinkSide;
	}

	public void setLocateLinkSide(int locateLinkSide) {
		this.locateLinkSide = locateLinkSide;
	}

	public int getSrcPid() {
		return srcPid;
	}

	public void setSrcPid(int srcPid) {
		this.srcPid = srcPid;
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

	public int getEditFlag() {
		return editFlag;
	}

	public void setEditFlag(int editFlag) {
		this.editFlag = editFlag;
	}

	public String getIdcode() {
		return idcode;
	}

	public void setIdcode(String idcode) {
		this.idcode = idcode;
	}

	public String getDprName() {
		return dprName;
	}

	public void setDprName(String dprName) {
		this.dprName = dprName;
	}

	public String getDpName() {
		return dpName;
	}

	public void setDpName(String dpName) {
		this.dpName = dpName;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getMemoire() {
		return memoire;
	}

	public void setMemoire(String memoire) {
		this.memoire = memoire;
	}

	public String getDpfName() {
		return dpfName;
	}

	public void setDpfName(String dpfName) {
		this.dpfName = dpfName;
	}

	public String getPosterId() {
		return posterId;
	}

	public void setPosterId(String posterId) {
		this.posterId = posterId;
	}

	public int getAddressFlag() {
		return addressFlag;
	}

	public void setAddressFlag(int addressFlag) {
		this.addressFlag = addressFlag;
	}

	public String getVerifed() {
		return verifed;
	}

	public void setVerifed(String verifed) {
		this.verifed = verifed;
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

	public String getSrcType() {
		return srcType;
	}

	public void setSrcType(String srcType) {
		this.srcType = srcType;
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

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
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

	public Map<String, Object> getChangedFields() {
		return changedFields;
	}

	public void setChangedFields(Map<String, Object> changedFields) {
		this.changedFields = changedFields;
	}

	public String getRowId() {
		return rowId;
	}

	@Override
	public void setRowId(String rowId) {
		this.rowId = rowId;
	}

	@Override
	public String tableName() {
		return "IX_POINTADDRESS";
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
		return ObjType.IXPOINTADDRESS;
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
		return "IX_POINTADDRESS";
	}

	@Override
	public List<List<IRow>> children() {
//		List<List<IRow>> children = new ArrayList<List<IRow>>();
//		return children;
		return null;
	}

	@Override
	public boolean fillChangeFields(JSONObject json) throws Exception {
		@SuppressWarnings("rawtypes")
		Iterator keys = json.keys();
		logger.info("newJson:"+json);
		logger.info("oldpa:"+this.Serialize(null));
		while (keys.hasNext()) {
			String key = (String) keys.next();

			if (json.get(key) instanceof JSONArray) {
				continue;
			} else if ("geometry".equals(key)) {

				JSONObject geojson = json.getJSONObject(key);

				String wkt = Geojson.geojson2Wkt(geojson.toString());

				String oldwkt = GeoTranslator.jts2Wkt(geometry, 0.00001, 5);

				if (!wkt.equals(oldwkt)) {
					// double length =
					// GeometryUtils.getLinkLength(GeoTranslator.geojson2Jts(geojson));
					//
					// changedFields.put("length", length);

					changedFields.put(key, json.getJSONObject(key));
				}
			} else {
				if (!"objStatus".equals(key)) {
					logger.info("key:"+key);
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
		
		if (oldValue instanceof Double) {
			newValue = Double.parseDouble(newValue.toString());
		}
		
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
		if (objLevel == ObjLevel.HISTORY) {
			/*json.remove("rawFields");
			json.remove("status");
			json.remove("poiEditStatus");
			json.remove("sameFid");
			json.remove("freshVerified");
			json.remove("evaluPlan");*/
		}

		return json;
	}

	@Override
	public boolean Unserialize(JSONObject json) throws Exception {
		@SuppressWarnings("rawtypes")
		Iterator keys = json.keys();

		while (keys.hasNext()) {
			String key = (String) keys.next();

//			JSONArray ja = null;

			/*if (json.get(key) instanceof JSONArray) {

				switch (key) {
				
					
				default:
					break;
				}

			} else*/ if ("geometry".equals(key)) {

				Geometry jts = GeoTranslator.geojson2Jts(json.getJSONObject(key), 100000, 0);

				this.setGeometry(jts);

			} else {

				if (!"objStatus".equals(key)) {
					Field f = this.getClass().getDeclaredField(key);

					f.setAccessible(true);

					f.set(this, SerializeUtils.convert(json.get(key), f.getType()));

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

	@Override
	public Map<Class<? extends IRow>, List<IRow>> childList() {
//		Map<Class<? extends IRow>, List<IRow>> childMap = new HashMap<>();
//		
//		return childMap; 
		
		return null;
	}

	@Override
	public Map<Class<? extends IRow>, Map<String, ?>> childMap() {
//		Map<Class<? extends IRow>, Map<String, ?>> childMap = new HashMap<Class<? extends IRow>, Map<String, ?>>();
//		return childMap;
		return null;
	}

}
