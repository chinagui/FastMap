package com.navinfo.dataservice.dao.glm.model.ad.geo;

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

public class AdFaceTopo implements IRow {

	private int facePid;

	private int linkPid;

	private int seqNum;

	private String rowId;

	private int mesh;

	private Map<String, Object> changedFields = new HashMap<String, Object>();

	public AdFaceTopo() {
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
	public String rowId() {

		return rowId;
	}

	@Override
	public void setRowId(String rowId) {
		this.rowId = rowId;
	}

	@Override
	public String tableName() {
		return "ad_face_topo";
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
		return ObjType.ADFACETOPO;
	}

	@Override
	public void copy(IRow row) {
		AdFaceTopo adFaceTopoSource = (AdFaceTopo) row;

		this.facePid = adFaceTopoSource.getFacePid();

		this.linkPid = adFaceTopoSource.getLinkPid();

		this.rowId = adFaceTopoSource.getRowId();

		this.seqNum = adFaceTopoSource.getSeqNum();
	}

	@Override
	public Map<String, Object> changedFields() {

		return changedFields;
	}

	@Override
	public String parentPKName() {
		return "face_pid";
	}

	@Override
	public int parentPKValue() {
		return this.getFacePid();
	}

	@Override
	public String parentTableName() {
		return "ad_face";
	}

	@Override
	public List<List<IRow>> children() {
		return null;
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
		return mesh;
	}

	@Override
	public void setMesh(int mesh) {
		this.mesh = mesh;
	}

	public int getFacePid() {
		return facePid;
	}

	public void setFacePid(int facePid) {
		this.facePid = facePid;
	}

	public int getLinkPid() {
		return linkPid;
	}

	public void setLinkPid(int linkPid) {
		this.linkPid = linkPid;
	}

	public int getSeqNum() {
		return seqNum;
	}

	public void setSeqNum(int seqNum) {
		this.seqNum = seqNum;
	}

	public String getRowId() {
		return rowId;
	}

}
