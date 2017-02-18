package com.navinfo.dataservice.dao.glm.model.rd.link;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;

public class RdLinkLimitTruck implements IRow {
	
	private String rowId;

	private int resTrailer;
	
	private double resWeigh;
	
	private double resAxleLoad;
	
	private int resAxleCount;
	
	private int resOut;
	
	private int limitDir;

	private String timeDomain;

	private int linkPid;

	private Map<String, Object> changedFields = new HashMap<String, Object>();

	public int getLinkPid() {
		return linkPid;
	}

	public void setLinkPid(int linkPid) {
		this.linkPid = linkPid;
	}

	public RdLinkLimitTruck() {

	}


	public int getResTrailer() {
		return resTrailer;
	}

	public void setResTrailer(int resTrailer) {
		this.resTrailer = resTrailer;
	}

	public double getResWeigh() {
		return resWeigh;
	}

	public void setResWeigh(double resWeigh) {
		this.resWeigh = resWeigh;
	}

	public double getResAxleLoad() {
		return resAxleLoad;
	}

	public void setResAxleLoad(double resAxleLoad) {
		this.resAxleLoad = resAxleLoad;
	}

	public int getResAxleCount() {
		return resAxleCount;
	}

	public void setResAxleCount(int resAxleCount) {
		this.resAxleCount = resAxleCount;
	}

	public int getResOut() {
		return resOut;
	}

	public void setResOut(int resOut) {
		this.resOut = resOut;
	}

	public int getLimitDir() {
		return limitDir;
	}

	public void setLimitDir(int limitDir) {
		this.limitDir = limitDir;
	}

	public String getTimeDomain() {
		return timeDomain;
	}

	public void setTimeDomain(String timeDomain) {
		this.timeDomain = timeDomain;
	}

	public String getRowId() {
		return rowId;
	}

	public void setRowId(String rowId) {
		this.rowId = rowId;
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

			if (!"objStatus".equals(key)) {

				Field f = this.getClass().getDeclaredField(key);
				f.setAccessible(true);
				
				 if(f.getType() ==Double.TYPE){
					 f.set(this, json.getDouble(key));
	                }else{
	                	f.set(this, json.get(key));
	                }
				
			}

		}
		return true;
	}

	@Override
	public String tableName() {

		return "rd_link_limit_truck";
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

		return ObjType.RDLINKLIMITTRUNK;
	}

	@Override
	public void copy(IRow row) {

		RdLinkLimitTruck sourceLimit = (RdLinkLimitTruck) row;

		this.setLimitDir(sourceLimit.getLimitDir());

		this.setRowId(sourceLimit.getRowId());

		this.setTimeDomain(sourceLimit.getTimeDomain());

		this.setResTrailer(sourceLimit.getResTrailer());
		
		this.setResWeigh(sourceLimit.getResWeigh());
		
		this.setResAxleLoad(sourceLimit.getResAxleLoad());
		
		this.setResAxleCount(sourceLimit.getResAxleCount());
		
		this.setResOut(sourceLimit.getResOut());
		
	}

	@Override
	public Map<String, Object> changedFields() {

		return changedFields;
	}

	@Override
	public String parentPKName() {

		return "link_pid";
	}

	@Override
	public int parentPKValue() {

		return this.getLinkPid();
	}

	@Override
	public List<List<IRow>> children() {

		return null;
	}

	@Override
	public String parentTableName() {

		return "rd_link";
	}

	@Override
	public String rowId() {

		return rowId;
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
	public static void main(String[] args) throws Exception {
		 	String parameter="{\"command\":\"UPDATE\",\"dbId\":17,\"type\":\"RDLINK\",\"objId\":202001016,\"data\":{\"limitTrucks\":[{\"linkPid\":202001016,\"limitDir\":0,\"timeDomain\":\"\",\"resTrailer\":0,\"resWeigh\":99.9,\"resAxleLoad\":99.9,\"resAxleCount\":0,\"resOut\":2,\"objStatus\":\"INSERT\"}],\"rowId\":\"7FCDC34C681C419086C8B682C87F831F\",\"pid\":202001016,\"objStatus\":\"UPDATE\"}}";
		
		 com.alibaba.fastjson.JSONObject fastJson = com.alibaba.fastjson.JSONObject
	                .parseObject(parameter);
	 //{"linkPid":202001016,"limitDir":0,"timeDomain":"","resTrailer":0,"resWeigh":99.9,"resAxleLoad":99.9,"resAxleCount":0,"resOut":2,"objStatus":"INSERT"}
		
		
		 JSONObject json = JsonUtils.fastJson2netJson(fastJson);
		 JSONObject contents =json.getJSONObject("data");
		 JSONArray arr = contents.getJSONArray("limitTrucks");
		  JSONObject content = arr.getJSONObject(0);
		
			 RdLinkLimitTruck obj = new RdLinkLimitTruck();
	         obj.Unserialize(content);
	         System.out.println(obj.getResAxleLoad());
	        
	        
		
	}
}
