package com.navinfo.dataservice.dao.glm.model.rd.branch;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class RdBranchVia implements IRow {

	private int branchPid;

	private int linkPid;

	private int groupId = 1;

	private int seqNum = 1;

	private String rowId;

	private Map<String, Object> changedFields = new HashMap<String, Object>();

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

	public String getRowId() {
		return rowId;
	}

	@Override
	public void setRowId(String rowId) {
		this.rowId = rowId;
	}

	public RdBranchVia() {

	}

	@Override
	public JSONObject Serialize(ObjLevel objLevel) {

		return JSONObject.fromObject(this, JsonUtils.getStrConfig());
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

		return "rd_branch_via";
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

		return ObjType.RDBRANCHVIA;
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
		RdBranchVia via = (RdBranchVia) row;
		
		this.rowId = via.rowId;
		
		this.seqNum = via.seqNum;
		
		this.linkPid = via.linkPid;
		
		this.groupId = via.groupId;
	}

	@Override
	public Map<String, Object> changedFields() {

		return changedFields;
	}

	@Override
	public String parentPKName() {

		return "branch_pid";
	}

	@Override
	public int parentPKValue() {

		return this.getBranchPid();
	}

	@Override
	public List<List<IRow>> children() {

		return null;
	}

	@Override
	public String parentTableName() {

		return "rd_branch";
	}

	@Override
	public String rowId() {

		return this.getRowId();
	}

	public int getBranchPid() {
		return branchPid;
	}

	public void setBranchPid(int branchPid) {
		this.branchPid = branchPid;
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
