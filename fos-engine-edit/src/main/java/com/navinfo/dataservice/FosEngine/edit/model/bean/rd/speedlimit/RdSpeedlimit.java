package com.navinfo.dataservice.FosEngine.edit.model.bean.rd.speedlimit;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;
import net.sf.json.util.JSONUtils;

import com.navinfo.dataservice.FosEngine.comm.geom.GeoTranslator;
import com.navinfo.dataservice.FosEngine.comm.util.JsonUtils;
import com.navinfo.dataservice.FosEngine.edit.model.IObj;
import com.navinfo.dataservice.FosEngine.edit.model.IRow;
import com.navinfo.dataservice.FosEngine.edit.model.ObjLevel;
import com.navinfo.dataservice.FosEngine.edit.model.ObjStatus;
import com.navinfo.dataservice.FosEngine.edit.model.ObjType;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.node.RdNodeForm;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.node.RdNodeMesh;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.node.RdNodeName;
import com.vividsolutions.jts.geom.Geometry;

public class RdSpeedlimit implements IObj {

	private int pid;

	private int linkPid;

	private int direct;
	
	private int speedValue;
	
	private int speedType;
	
	private int tollgateFlag;
	
	private int speedDependent;
	
	private int speedFlag;
	
	private int limitSrc=1;
	
	private String timeDomain;
	
	private int captureFlag;
	
	private String descript;
	
	public int getLinkPid() {
		return linkPid;
	}

	public void setLinkPid(int linkPid) {
		this.linkPid = linkPid;
	}

	public int getDirect() {
		return direct;
	}

	public void setDirect(int direct) {
		this.direct = direct;
	}

	public int getSpeedValue() {
		return speedValue;
	}

	public void setSpeedValue(int speedValue) {
		this.speedValue = speedValue;
	}

	public int getSpeedType() {
		return speedType;
	}

	public void setSpeedType(int speedType) {
		this.speedType = speedType;
	}

	public int getTollgateFlag() {
		return tollgateFlag;
	}

	public void setTollgateFlag(int tollgateFlag) {
		this.tollgateFlag = tollgateFlag;
	}

	public int getSpeedDependent() {
		return speedDependent;
	}

	public void setSpeedDependent(int speedDependent) {
		this.speedDependent = speedDependent;
	}

	public int getSpeedFlag() {
		return speedFlag;
	}

	public void setSpeedFlag(int speedFlag) {
		this.speedFlag = speedFlag;
	}

	public int getLimitSrc() {
		return limitSrc;
	}

	public void setLimitSrc(int limitSrc) {
		this.limitSrc = limitSrc;
	}

	public String getTimeDomain() {
		return timeDomain;
	}

	public void setTimeDomain(String timeDomain) {
		this.timeDomain = timeDomain;
	}

	public int getCaptureFlag() {
		return captureFlag;
	}

	public void setCaptureFlag(int captureFlag) {
		this.captureFlag = captureFlag;
	}

	public String getDescript() {
		return descript;
	}

	public void setDescript(String descript) {
		this.descript = descript;
	}

	public int getMeshId() {
		return meshId;
	}

	public void setMeshId(int meshId) {
		this.meshId = meshId;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getCkStatus() {
		return ckStatus;
	}

	public void setCkStatus(int ckStatus) {
		this.ckStatus = ckStatus;
	}

	public int getAdjaFlag() {
		return adjaFlag;
	}

	public void setAdjaFlag(int adjaFlag) {
		this.adjaFlag = adjaFlag;
	}

	public int getRecStatusIn() {
		return recStatusIn;
	}

	public void setRecStatusIn(int recStatusIn) {
		this.recStatusIn = recStatusIn;
	}

	public int getRecStatusOut() {
		return recStatusOut;
	}

	public void setRecStatusOut(int recStatusOut) {
		this.recStatusOut = recStatusOut;
	}

	public String getTimeDescript() {
		return timeDescript;
	}

	public void setTimeDescript(String timeDescript) {
		this.timeDescript = timeDescript;
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}

	public String getLaneSpeedValue() {
		return laneSpeedValue;
	}

	public void setLaneSpeedValue(String laneSpeedValue) {
		this.laneSpeedValue = laneSpeedValue;
	}

	private int meshId;
	
	private int status=7;
	
	private int ckStatus;
	
	private int adjaFlag;
	
	private int recStatusIn;
	
	private int recStatusOut;
	
	private String timeDescript;
	
	private Geometry geometry;
	
	private String laneSpeedValue;
	
	private String rowId;

	private Map<String, Object> changedFields = new HashMap<String, Object>();
	
	public RdSpeedlimit() {

	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	@Override
	public String tableName() {

		return "rd_speedlimit";
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

		return ObjType.RDSPEEDLIMIT;
	}

	@Override
	public JSONObject Serialize(ObjLevel objLevel) {
		
		return JSONObject.fromObject(this,JsonUtils.getStrConfig());
	}

	@Override
	public boolean Unserialize(JSONObject json) throws Exception {

		Iterator keys = json.keys();

		while (keys.hasNext()) {
			String key = (String) keys.next();

			JSONArray ja = null;

			if ("geometry".equals(key)) {

				Geometry jts = GeoTranslator.geojson2Jts(json.getJSONObject(key), 100000, 0);

				this.setGeometry(jts);

			} else {
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
	public void copy(IRow row) {

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
	public String primaryKey() {

		return "pid";
	}

	@Override
	public int primaryValue() {

		return this.getPid();
	}

	@Override
	public List<List<IRow>> children() {

		List<List<IRow>> children = new ArrayList<List<IRow>>();


		return children;
	}

	@Override
	public String primaryTableName() {

		return "rd_speedlimit";
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

			if (json.get(key) instanceof JSONArray) {
				continue;
			}  else if ("geometry".equals(key)) {
				changedFields.put(key, json.getJSONObject(key));
			}  else {
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
						changedFields.put(key, json.get(key));
						
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
	
}
