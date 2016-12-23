package com.navinfo.dataservice.dao.glm.model.rd.laneconnexity;

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
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionVia;

public class RdLaneVia implements IRow {

	private int topologyId;

	private int linkPid;

	private int groupId = 1;

	private int seqNum = 1;

	private String rowId;

	private Map<String, Object> changedFields = new HashMap<String, Object>();
	
	private ObjStatus status;

	public String getRowId() {
		return rowId;
	}

	@Override
	public void setRowId(String rowId) {
		this.rowId = rowId;
	}

	public RdLaneVia() {

	}

	@Override
	public JSONObject Serialize(ObjLevel objLevel) {

		return JSONObject.fromObject(this, JsonUtils.getStrConfig());
	}

	// sNodePid、eNodePid、inNodePid不属于模型字段，使用protected修饰符。
	protected int sNodePid;

	protected int eNodePid;

	protected int inNodePid;

	public int igetInNodePid() {
		return inNodePid;
	}

	public void isetInNodePid(int inNodePid) {
		this.inNodePid = inNodePid;
	}

	public int igetsNodePid() {
		return sNodePid;
	}

	public void isetsNodePid(int sNodePid) {
		this.sNodePid = sNodePid;
	}

	public int igeteNodePid() {
		return eNodePid;
	}

	public void iseteNodePid(int eNodePid) {
		this.eNodePid = eNodePid;
	}

	@Override
	public boolean Unserialize(JSONObject json) throws Exception {

		return false;
	}

	@Override
	public String tableName() {

		return "rd_lane_via";
	}

	@Override
	public ObjStatus status() {

		return status;
	}

	@Override
	public void setStatus(ObjStatus os) {
		status = os;
	}

	@Override
	public ObjType objType() {

		return ObjType.RDLANEVIA;
	}

	public int getLinkPid() {
		return linkPid;
	}

	public void setLinkPid(int linkPid) {
		this.linkPid = linkPid;
	}

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public int getSeqNum() {
		return seqNum;
	}

	public void setSeqNum(int seqNum) {
		this.seqNum = seqNum;
	}

	@Override
	public void copy(IRow row) {
		
		RdLaneVia via = (RdLaneVia) row;

		this.linkPid = via.linkPid;

		this.groupId = via.groupId;
		
	}

	@Override
	public Map<String, Object> changedFields() {

		return changedFields;
	}

	@Override
	public String parentPKName() {

		return "topology_id";
	}

	@Override
	public int parentPKValue() {

		return this.getTopologyId();
	}

	@Override
	public List<List<IRow>> children() {

		return null;
	}

	@Override
	public String parentTableName() {

		return "rd_lane_topology";
	}

	@Override
	public String rowId() {

		return this.getRowId();
	}

	public int getTopologyId() {
		return topologyId;
	}

	public void setTopologyId(int topologyId) {
		this.topologyId = topologyId;
	}

	@Override
	public boolean fillChangeFields(JSONObject json) throws Exception {

		Iterator keys = json.keys();

		while (keys.hasNext()) {
			String key = (String) keys.next();

			JSONArray ja = null;

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

}
