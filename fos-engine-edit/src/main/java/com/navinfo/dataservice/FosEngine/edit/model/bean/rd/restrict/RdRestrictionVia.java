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

public class RdRestrictionVia implements IRow {
	
	private int mesh;

	private int detailId;

	private int linkPid;

	private int groupId = 1;

	private int seqNum = 1;

	private String rowId;

	private int sNodePid;

	private int eNodePid;

	private int inNodePid;

	private Map<String, Object> changedFields = new HashMap<String, Object>();

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

	public int igetInNodePid() {
		return inNodePid;
	}

	public void isetInNodePid(int inNodePid) {
		this.inNodePid = inNodePid;
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

		return JSONObject.fromObject(this,JsonUtils.getStrConfig());
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

		return null;
	}

	@Override
	public void setStatus(ObjStatus os) {

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

	public int getSeqNum() {
		return seqNum;
	}

	public void setSeqNum(int seqNum) {
		this.seqNum = seqNum;
	}

	@Override
	public void copy(IRow row) {

		RdRestrictionVia via = (RdRestrictionVia) row;

		this.linkPid = via.linkPid;

		this.groupId = via.groupId;

		this.seqNum = via.seqNum;
		
		this.mesh = via.mesh();

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
						changedFields.put(key,newValue.replace("'","''"));
						
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
