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
 * 道路:详细车道连通表，仅记录客车（小汽车）可通行的车道连通关系
 * 
 * @author zhaokaikai
 *
 */
public class RdLaneTopoDetail implements IObj {

	private String rowId;

	private int pid;

	public int getInLanePid() {
		return inLanePid;
	}

	public void setInLanePid(int inLanePid) {
		this.inLanePid = inLanePid;
	}

	public int getOutLanePid() {
		return outLanePid;
	}

	public void setOutLanePid(int outLanePid) {
		this.outLanePid = outLanePid;
	}

	public int getInLinkPid() {
		return inLinkPid;
	}

	public void setInLinkPid(int inLinkPid) {
		this.inLinkPid = inLinkPid;
	}

	public int getNodePid() {
		return nodePid;
	}

	public void setNodePid(int nodePid) {
		this.nodePid = nodePid;
	}

	public int getOutLinkPid() {
		return outLinkPid;
	}

	public void setOutLinkPid(int outLinkPid) {
		this.outLinkPid = outLinkPid;
	}

	public int getReachDir() {
		return reachDir;
	}

	public void setReachDir(int reachDir) {
		this.reachDir = reachDir;
	}

	public String getTimeDomain() {
		return timeDomain;
	}

	public void setTimeDomain(String timeDomain) {
		this.timeDomain = timeDomain;
	}

	public long getVehicle() {
		return vehicle;
	}

	public void setVehicle(long vehicle) {
		this.vehicle = vehicle;
	}

	

	public int getProcessFlag() {
		return processFlag;
	}

	public void setProcessFlag(int processFlag) {
		this.processFlag = processFlag;
	}

	public int getThroughTurn() {
		return throughTurn;
	}

	public void setThroughTurn(int throughTurn) {
		this.throughTurn = throughTurn;
	}



	private int inLanePid; // 进入车道
	private int outLanePid; // 退出车道
	private int inLinkPid;// 进入 LINK
	private int nodePid; // 进入 NODE
	private int outLinkPid;// 退出 LINK
	private int reachDir = 0; // 通达方向
	private String timeDomain ;// 时间段
	private long vehicle = 0;// 车辆类型
	private int processFlag = 2;//处理标志
	private int throughTurn = 0;//是否借道


	private List<IRow> topoVias = new ArrayList<IRow>();

	private Map<String, Object> changedFields = new HashMap<String, Object>();
	public Map<String, RdLaneTopoVia> mapVia = new HashMap<>();

	public RdLaneTopoDetail() {

	}

	public void setPid(int nodePid) {
		this.pid = nodePid;
	}

	@Override
	public String tableName() {

		return "rd_lane_topo_detail";
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

		return ObjType.RDLANETOPODETAIL;
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
	public String parentPKName() {

		return "topo_id";
	}

	@Override
	public int parentPKValue() {
		return this.getPid();
	}

	@Override
	public List<List<IRow>> children() {

		List<List<IRow>> children = new ArrayList<List<IRow>>();
		children.add(topoVias);
		return children;
	}

	public List<IRow> getTopoVias() {
		return topoVias;
	}

	public void setTopoVias(List<IRow> topoVias) {
		this.topoVias = topoVias;
	}

	public String getRowId() {
		return rowId;
	}

	public int getPid() {
		return pid;
	}

	@Override
	public String parentTableName() {

		return "rd_lane_topo_detail";
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
		return "topo_id";
	}

	@Override
	public Map<Class<? extends IRow>, List<IRow>> childList() {
		Map<Class<? extends IRow>, List<IRow>> childList = new HashMap<Class<? extends IRow>, List<IRow>>();
		childList.put(RdLaneTopoVia.class, topoVias);
		return childList;
	}

	@Override
	public Map<Class<? extends IRow>, Map<String, ?>> childMap() {
		Map<Class<? extends IRow>, Map<String, ?>> childMap = new HashMap<Class<? extends IRow>, Map<String, ?>>();
		childMap.put(RdLaneTopoVia.class, mapVia);
		return childMap;
	}
}
