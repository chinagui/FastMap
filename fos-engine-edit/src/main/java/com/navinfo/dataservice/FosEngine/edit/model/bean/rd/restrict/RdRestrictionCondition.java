package com.navinfo.dataservice.FosEngine.edit.model.bean.rd.restrict;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.FosEngine.edit.model.IRow;
import com.navinfo.dataservice.FosEngine.edit.model.ObjLevel;
import com.navinfo.dataservice.FosEngine.edit.model.ObjStatus;
import com.navinfo.dataservice.FosEngine.edit.model.ObjType;
import com.navinfo.dataservice.commons.util.JsonUtils;

public class RdRestrictionCondition implements IRow {
	
	private int mesh;

	private String rowId;

	private int detailId;

	private String timeDomain;

	private int vehicle;

	private int resTrailer;

	private double resWeigh;

	private double resAxleLoad;

	private int resAxleCount;

	private int resOut;

	private Map<String, Object> changedFields = new HashMap<String, Object>();

	public RdRestrictionCondition() {

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

	public int getDetailId() {
		return detailId;
	}

	public void setDetailId(int detailId) {
		this.detailId = detailId;
	}

	public int getVehicle() {
		return vehicle;
	}

	public void setVehicle(int vehicle) {
		this.vehicle = vehicle;
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

				f.set(this, json.get(key));

			}
		}

		return true;
	}

	@Override
	public String tableName() {

		return "rd_restriction_condition";
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

		return ObjType.RDRESTRICTIONCONDITION;
	}

	@Override
	public void copy(IRow row) {

		RdRestrictionCondition condition = (RdRestrictionCondition) row;

		this.timeDomain = condition.timeDomain;

		this.vehicle = condition.vehicle;

		this.resTrailer = condition.resTrailer;

		this.resWeigh = condition.resWeigh;

		this.resAxleCount = condition.resAxleCount;

		this.resAxleLoad = condition.resAxleLoad;

		this.resOut = condition.resOut;
		
		this.mesh = condition.mesh();

	}

	@Override
	public Map<String, Object> changedFields() {

		return changedFields;
	}

	@Override
	public String parentPKName() {

		return "detail_id";
	}

	@Override
	public int parentPKValue() {

		return this.getDetailId();
	}

	@Override
	public List<List<IRow>> children() {

		return null;
	}

	@Override
	public String parentTableName() {

		return "rd_restriction_detail";
	}

	@Override
	public String rowId() {

		return this.getRowId();
	}

	@Override
	public boolean fillChangeFields(JSONObject json) throws Exception {
		
		Iterator keys = json.keys();

		while (keys.hasNext()) {
			String key = (String) keys.next();

			JSONArray ja = null;

			if (json.get(key) instanceof JSONArray) {
				continue;
			}  else {
				if (!"objStatus".equals(key)) {
					
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
	public int mesh() {
		// TODO Auto-generated method stub
		return mesh;
	}

	@Override
	public void setMesh(int mesh) {
		// TODO Auto-generated method stub
		this.mesh=mesh;
	}
}
