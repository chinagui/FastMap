package com.navinfo.dataservice.dao.glm.model.rd.branch;

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

public class RdBranchSchematic implements IObj {

	private int pid;

	private int branchPid;

	private String schematicCode;

	private String arrowCode;
	
	private String memo;

	private String rowId;

	private Map<String, Object> changedFields = new HashMap<String, Object>();
	
	protected  ObjStatus status;

	@Override
	public void setRowId(String rowId) {
		this.rowId = rowId;
	}

	public RdBranchSchematic() {

	}
	
	public String getRowId() {
		return rowId;
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public String getSchematicCode() {
		return schematicCode;
	}

	public void setSchematicCode(String schematicCode) {
		this.schematicCode = schematicCode;
	}

	public String getArrowCode() {
		return arrowCode;
	}

	public void setArrowCode(String arrowCode) {
		this.arrowCode = arrowCode;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
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

		return "rd_branch_schematic";
	}


	@Override
	public ObjStatus status() {

		return status;
	}

	@Override
	public void setStatus(ObjStatus os) {
		status=os;
	}

	@Override
	public ObjType objType() {

		return ObjType.RDBRANCHSCHEMATIC;
	}


	@Override
	public void copy(IRow row) {

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

		return this.rowId;
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
	public int mesh() {
		return 0;
	}

	@Override
	public void setMesh(int mesh) {
	}

	@Override
	public String primaryKey() {
		// TODO Auto-generated method stub
		return "schematic_id";
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.dao.glm.iface.IRow#childMap()
	 */
	@Override
	public Map<Class<? extends IRow>, List<IRow>> childList() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.dao.glm.iface.IObj#childMap()
	 */
	@Override
	public Map<Class<? extends IRow>,Map<String,?>> childMap() {
		// TODO Auto-generated method stub
		return null;
	}

}
