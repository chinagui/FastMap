package com.navinfo.dataservice.dao.glm.model.rd.lane;

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
 * 车道表
 * 
 * @author zhaokaikai
 * 
 */
public class RdLane implements IObj {

	private String rowId;

	private int pid;

	private int linkPid; // LINK 号码
	private int laneNum = 1;// 车道总数
	private int travelFlag = 0; // 通行标志 0 右侧通行(大陆) 1 左侧通行(港澳)
	private int seqNum = 1;// 从 1 开始递增编号注:大陆:从左到右;港澳:从右到左
	private int laneForming = 0; // 0 不应用1 车道形成(Lane Forming)2 车道结束(Lane
									// Ending)3 车 道 形 成 & 结 束 (Lane
									// Forming&Ending)
	private int laneDir = 1;// 1 无2 顺方向3 逆方向
	private int laneType = 1;// 车道类型
	private String arrowDir = "9";// 转向箭头
	private int laneMark = 0;// 车道标线
	private int width = 0;// 车道宽度
	private int restrictHeight = 0;// 车道限高
	private int transitionArea = 0;// 交换区域
	private int fromMaxSpeed = 0;// 顺向最高限速
	private int toMaxSpeed = 0;// 逆向最高限速
	private int fromMinSpeed = 0;// 顺向最低限速
	private int toMinSpeed = 0;// 逆向最低限速
	private int elecEye = 0; // 电子眼
	private int laneDivider = 0;// 车道分隔带
	private int centerDivider = 0; // 中央分隔带
	private int speedFlag = 0; // 车道限速来源标识
	private int srcFlag = 1;// 车道来源

	private List<IRow> conditions = new ArrayList<IRow>();
	
	protected ObjStatus laneStatus;

	private Map<String, Object> changedFields = new HashMap<String, Object>();
	public Map<String, RdLaneCondition> conditionMap = new HashMap<>();

	public int getLinkPid() {
		return linkPid;
	}

	public void setLinkPid(int linkPid) {
		this.linkPid = linkPid;
	}

	public int getLaneNum() {
		return laneNum;
	}

	public void setLaneNum(int laneNum) {
		this.laneNum = laneNum;
	}

	public int getTravelFlag() {
		return travelFlag;
	}

	public void setTravelFlag(int travelFlag) {
		this.travelFlag = travelFlag;
	}

	public int getSeqNum() {
		return seqNum;
	}

	public void setSeqNum(int seqNum) {
		this.seqNum = seqNum;
	}

	public int getLaneForming() {
		return laneForming;
	}

	public void setLaneForming(int laneForming) {
		this.laneForming = laneForming;
	}

	public int getLaneDir() {
		return laneDir;
	}

	public void setLaneDir(int laneDir) {
		this.laneDir = laneDir;
	}

	public int getLaneType() {
		return laneType;
	}

	public void setLaneType(int laneType) {
		this.laneType = laneType;
	}

	public String getArrowDir() {
		return arrowDir;
	}

	public void setArrowDir(String arrowDir) {
		this.arrowDir = arrowDir;
	}

	public int getLaneMark() {
		return laneMark;
	}

	public void setLaneMark(int laneMark) {
		this.laneMark = laneMark;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getRestrictHeight() {
		return restrictHeight;
	}

	public void setRestrictHeight(int restrictHeight) {
		this.restrictHeight = restrictHeight;
	}

	public int getTransitionArea() {
		return transitionArea;
	}

	public void setTransitionArea(int transitionArea) {
		this.transitionArea = transitionArea;
	}

	public int getFromMaxSpeed() {
		return fromMaxSpeed;
	}

	public void setFromMaxSpeed(int fromMaxSpeed) {
		this.fromMaxSpeed = fromMaxSpeed;
	}

	public int getToMaxSpeed() {
		return toMaxSpeed;
	}

	public void setToMaxSpeed(int toMaxSpeed) {
		this.toMaxSpeed = toMaxSpeed;
	}

	public int getFromMinSpeed() {
		return fromMinSpeed;
	}

	public void setFromMinSpeed(int fromMinSpeed) {
		this.fromMinSpeed = fromMinSpeed;
	}

	public int getToMinSpeed() {
		return toMinSpeed;
	}

	public void setToMinSpeed(int toMinSpeed) {
		this.toMinSpeed = toMinSpeed;
	}

	public int getElecEye() {
		return elecEye;
	}

	public void setElecEye(int elecEye) {
		this.elecEye = elecEye;
	}

	public int getLaneDivider() {
		return laneDivider;
	}

	public void setLaneDivider(int laneDivider) {
		this.laneDivider = laneDivider;
	}

	public int getCenterDivider() {
		return centerDivider;
	}

	public void setCenterDivider(int centerDivider) {
		this.centerDivider = centerDivider;
	}

	public int getSpeedFlag() {
		return speedFlag;
	}

	public void setSpeedFlag(int speedFlag) {
		this.speedFlag = speedFlag;
	}

	public int getSrcFlag() {
		return srcFlag;
	}

	public void setSrcFlag(int srcFlag) {
		this.srcFlag = srcFlag;
	}

	public RdLane() {

	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	@Override
	public String tableName() {

		return "rd_lane";
	}

	@Override
	public ObjStatus status() {
		return laneStatus;
	}

	@Override
	public void setStatus(ObjStatus os) {
		this.laneStatus = os;
	}

	@Override
	public ObjType objType() {

		return ObjType.RDLANE;
	}

	@Override
	public JSONObject Serialize(ObjLevel objLevel) throws Exception {
		
		JSONObject json = JSONObject.fromObject(this, JsonUtils.getStrConfig());
		
		if(objLevel == ObjLevel.HISTORY)
		{
			json.remove("laneStatus");
		}
		
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
	public List<IRow> relatedRows() {

		return null;
	}

	@Override
	public void copy(IRow row) {
		RdLane lane = (RdLane) row;
		this.setArrowDir(lane.getArrowDir());
		this.setCenterDivider(lane.getCenterDivider());
		this.setElecEye(lane.getElecEye());
		this.setFromMaxSpeed(lane.getFromMaxSpeed());
		this.setFromMinSpeed(lane.getFromMinSpeed());
		this.setLaneDir(lane.getLaneDir());
		this.setLaneDivider(lane.getLaneDivider());
		this.setLaneForming(lane.getLaneForming());
		this.setLaneMark(lane.getLaneMark());
		this.setLaneNum(lane.getLaneNum());
		this.setLaneType(lane.getLaneType());
		this.setLinkPid(lane.getLinkPid());
		this.setRestrictHeight(lane.getRestrictHeight());
		this.setSeqNum(lane.getSeqNum());
		this.setSpeedFlag(lane.getSpeedFlag());
		this.setSrcFlag(lane.getSrcFlag());
		this.setToMaxSpeed(lane.getToMaxSpeed());
		this.setToMinSpeed(lane.getToMinSpeed());
		this.setTransitionArea(lane.getTransitionArea());
		this.setTravelFlag(lane.getTravelFlag());
		this.setWidth(lane.getWidth());
		List<IRow> conditions = new ArrayList<IRow>();
		for (IRow fs : conditions) {
			RdLaneCondition condition = new RdLaneCondition();
			condition.copy(fs);
			condition.setLanePid(this.getPid());
			conditions.add(condition);
		}

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
	public String parentPKName() {

		return "lane_pid";
	}

	@Override
	public int parentPKValue() {
		return this.getPid();
	}

	@Override
	public List<List<IRow>> children() {

		List<List<IRow>> children = new ArrayList<List<IRow>>();
		children.add(conditions);
		return children;
	}

	public List<IRow> getConditions() {
		return conditions;
	}

	public void setConditions(List<IRow> conditions) {
		this.conditions = conditions;
	}

	public String getRowId() {
		return rowId;
	}

	public int getPid() {
		return pid;
	}

	@Override
	public String parentTableName() {

		return "rd_lane";
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
	public String primaryKey() {
		return "lane_pid";
	}

	@Override
	public Map<Class<? extends IRow>, List<IRow>> childList() {
		Map<Class<? extends IRow>, List<IRow>> childList = new HashMap<Class<? extends IRow>, List<IRow>>();
		childList.put(RdLaneCondition.class, conditions);
		return childList;
	}

	@Override
	public Map<Class<? extends IRow>, Map<String, ?>> childMap() {
		Map<Class<? extends IRow>, Map<String, ?>> childMap = new HashMap<Class<? extends IRow>, Map<String, ?>>();
		childMap.put(RdLaneCondition.class, conditionMap);
		return childMap;
	}
}
