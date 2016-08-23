/**
 * 
 */
package com.navinfo.dataservice.dao.glm.model.rd.crosswalk;

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
import com.navinfo.dataservice.dao.glm.model.rd.crf.RdObjectNode;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @ClassName: RdCrosswalk
 * @author Zhang Xiaolong
 * @date 2016年8月23日 下午5:10:22
 * @Description: TODO
 */
public class RdCrosswalk implements IObj {

	private int pid;

	// 小斜坡
	private int curbRamp;

	// 时间段
	private String timeDomain;

	private String rowId;

	private Map<String, Object> changedFields = new HashMap<String, Object>();

	private List<IRow> infos = new ArrayList<>();

	private List<IRow> nodes = new ArrayList<>();

	public Map<String, RdCrosswalkInfo> infoMap = new HashMap<>();

	public Map<String, RdCrosswalkNode> nodeMap = new HashMap<>();

	public List<IRow> getNodes() {
		return nodes;
	}

	public void setNodes(List<IRow> nodes) {
		this.nodes = nodes;
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public String getRowId() {
		return rowId;
	}

	public int getCurbRamp() {
		return curbRamp;
	}

	public void setCurbRamp(int curbRamp) {
		this.curbRamp = curbRamp;
	}

	public String getTimeDomain() {
		return timeDomain;
	}

	public void setTimeDomain(String timeDomain) {
		this.timeDomain = timeDomain;
	}

	public List<IRow> getInfos() {
		return infos;
	}

	public void setInfos(List<IRow> infos) {
		this.infos = infos;
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
		return "RD_OBJECT";
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
		return ObjType.RDCROSSWALK;
	}

	@Override
	public void copy(IRow row) {
		RdCrosswalk object = (RdCrosswalk) row;

		this.pid = object.pid;
		
		this.curbRamp = object.curbRamp;
		
		this.timeDomain = object.timeDomain;

		this.infos = new ArrayList<IRow>();

		for (IRow r : object.infos) {

			RdCrosswalkInfo info = new RdCrosswalkInfo();

			info.copy(r);

			this.infos.add(info);
		}

		this.nodes = new ArrayList<IRow>();

		for (IRow r : object.nodes) {

			RdCrosswalkNode node = new RdCrosswalkNode();

			node.copy(r);

			this.nodes.add(node);
		}
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
		return "RD_CROSSWALK";
	}

	@Override
	public List<List<IRow>> children() {
		List<List<IRow>> children = new ArrayList<>();

		children.add(this.nodes);

		children.add(this.infos);

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
	public JSONObject Serialize(ObjLevel objLevel) throws Exception {
		return JSONObject.fromObject(this, JsonUtils.getStrConfig());
	}

	@Override
	public boolean Unserialize(JSONObject json) throws Exception {
		return false;
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
		Map<Class<? extends IRow>, List<IRow>> childList = new HashMap<Class<? extends IRow>, List<IRow>>();
		childList.put(RdCrosswalkInfo.class, infos);
		childList.put(RdObjectNode.class, nodes);
		return childList;
	}

	@Override
	public Map<Class<? extends IRow>, Map<String, ?>> childMap() {
		Map<Class<? extends IRow>, Map<String, ?>> childMap = new HashMap<Class<? extends IRow>, Map<String, ?>>();
		childMap.put(RdCrosswalkInfo.class, infoMap);
		childMap.put(RdObjectNode.class, nodeMap);
		return childMap;
	}

}
