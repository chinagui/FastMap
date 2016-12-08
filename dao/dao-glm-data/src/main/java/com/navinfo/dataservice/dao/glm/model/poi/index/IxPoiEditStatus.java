/**
 * 
 */
package com.navinfo.dataservice.dao.glm.model.poi.index;

import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;

import net.sf.json.JSONObject;

/** 
* @ClassName: IxPoiEditStatus 
* @author Zhang Xiaolong
* @date 2016年7月27日 下午4:09:06 
*/
public class IxPoiEditStatus implements IRow{

	//2016.12.08  zl:当前编辑状态 0:未提交过 ;  1:已提交
	private int commitHisStatus = 0;
	
	public int getCommit_his_status() {
		return commitHisStatus;
	}
	
	public void setCommitHisStatus(int commitHisStatus) {
		this.commitHisStatus = commitHisStatus;
	}

	@Override
	public JSONObject Serialize(ObjLevel objLevel) throws Exception {
		return null;
	}

	@Override
	public boolean Unserialize(JSONObject json) throws Exception {
		return false;
	}

	@Override
	public String rowId() {
		return null;
	}

	@Override
	public void setRowId(String rowId) {
	}

	@Override
	public String tableName() {
		return null;
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
		return null;
	}

	@Override
	public void copy(IRow row) {
	}

	@Override
	public Map<String, Object> changedFields() {
		return null;
	}

	@Override
	public String parentPKName() {
		return null;
	}

	@Override
	public int parentPKValue() {
		return 0;
	}

	@Override
	public String parentTableName() {
		return null;
	}

	@Override
	public List<List<IRow>> children() {
		return null;
	}

	@Override
	public boolean fillChangeFields(JSONObject json) throws Exception {
		return false;
	}

	@Override
	public int mesh() {
		return 0;
	}

	@Override
	public void setMesh(int mesh) {
	}

}
