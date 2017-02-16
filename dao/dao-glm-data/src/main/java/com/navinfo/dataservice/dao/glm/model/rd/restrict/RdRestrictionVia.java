package com.navinfo.dataservice.dao.glm.model.rd.restrict;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.IVia;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;

public class RdRestrictionVia implements IRow,IVia {

	private int detailId;

	private int linkPid;

	private int groupId = 1;

	private int seqNum = 1;

	private String rowId;
	protected ObjStatus status;
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

	public RdRestrictionVia() {

	}

	@Override
	public JSONObject Serialize(ObjLevel objLevel) {
		JSONObject json = JSONObject.fromObject(this,JsonUtils.getStrConfig());
		
		if (objLevel == ObjLevel.HISTORY) {
			json.remove("status");
		}
		return json;
//		return JSONObject.fromObject(this, JsonUtils.getStrConfig());
	}

	@Override
	public boolean Unserialize(JSONObject json) throws Exception {

		return false;
	}

	@Override
	public String tableName() {

		return "rd_restriction_via";
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

		return ObjType.RDRESTRICTIONVIA;
	}

	public int getDetailId() {
		return detailId;
	}

	public void setDetailId(int detailId) {
		this.detailId = detailId;
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

	@Override
	public int getSeqNum() {
		return seqNum;
	}

	@Override
	public void setSeqNum(int seqNum) {
		this.seqNum = seqNum;
	}

	@Override
	public void copy(IRow row) {

		RdRestrictionVia via = (RdRestrictionVia) row;

		this.linkPid = via.linkPid;

		this.groupId = via.groupId;

		this.seqNum = via.seqNum;

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
