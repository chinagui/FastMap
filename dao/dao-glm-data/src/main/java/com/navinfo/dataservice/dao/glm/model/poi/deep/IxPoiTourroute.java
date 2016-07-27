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
 * 索引:POI 深度信息(旅游线路类)
 * 
 * @author zhaokk
 *
 */
public class IxPoiTourroute implements IObj {

	private int pid;
	private String tourName;// 旅游路线名称
	private String tourNameEng;// 旅游路线英文名称
	private String tourIntr;// 路线介绍
	private String tourIntrEng;// 路线英文介绍
	private String tourType;// 路线种类
	private String tourTypeEng;// 英文版路线种类
	private double tourX = 0;// 路线引导点 X 坐标
	private double tourY = 0;// 路线引导点 Y 坐标
	private double tourLen;// 路线总长度
	private String trailTime;// 路线耗时
	private String visitTime;// 游览耗时
	private String poiPid;// 途径 POI
	private String memo;
	private String reserved;// 预留字段

	private int travelguideFlag = 0;// 是 否 属 于travel guide所需 POI
	private String rowId;

	// 更新时间
	private String uDate;

	public String getuDate() {
		return uDate;
	}

	public void setuDate(String uDate) {
		this.uDate = uDate;
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
		return "ix_poi_tourroute";
	}

	public String getTourName() {
		return tourName;
	}

	public void setTourName(String tourName) {
		this.tourName = tourName;
	}

	public String getTourNameEng() {
		return tourNameEng;
	}

	public void setTourNameEng(String tourNameEng) {
		this.tourNameEng = tourNameEng;
	}

	public String getTourIntr() {
		return tourIntr;
	}

	public void setTourIntr(String tourIntr) {
		this.tourIntr = tourIntr;
	}

	public String getTourIntrEng() {
		return tourIntrEng;
	}

	public void setTourIntrEng(String tourIntrEng) {
		this.tourIntrEng = tourIntrEng;
	}

	public String getTourType() {
		return tourType;
	}

	public void setTourType(String tourType) {
		this.tourType = tourType;
	}

	public String getTourTypeEng() {
		return tourTypeEng;
	}

	public void setTourTypeEng(String tourTypeEng) {
		this.tourTypeEng = tourTypeEng;
	}

	public double getTourX() {
		return tourX;
	}

	public void setTourX(double tourX) {
		this.tourX = tourX;
	}

	public double getTourY() {
		return tourY;
	}

	public void setTourY(double tourY) {
		this.tourY = tourY;
	}

	public double getTourLen() {
		return tourLen;
	}

	public void setTourLen(double tourLen) {
		this.tourLen = tourLen;
	}

	public String getTrailTime() {
		return trailTime;
	}

	public void setTrailTime(String trailTime) {
		this.trailTime = trailTime;
	}

	public String getVisitTime() {
		return visitTime;
	}

	public void setVisitTime(String visitTime) {
		this.visitTime = visitTime;
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
	}

	@Override
	public ObjType objType() {
		return ObjType.IXPOITOURROUTE;
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
		return "tour_id";
	}

	@Override
	public int parentPKValue() {
		return this.getPid();
	}

	@Override
	public String parentTableName() {
		// TODO Auto-generated method stub
		return "ix_poi_tourroute";
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

	public int getTravelguideFlag() {
		return travelguideFlag;
	}

	public void setTravelguideFlag(int travelguideFlag) {
		this.travelguideFlag = travelguideFlag;
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
		return 0;
	}

	@Override
	public void setMesh(int mesh) {
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
		return "tour_id";
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.dao.glm.iface.IRow#childMap()
	 */
	@Override
	public Map<Class<? extends IRow>, List<IRow>> childMap() {
		// TODO Auto-generated method stub
		return null;
	}

}
