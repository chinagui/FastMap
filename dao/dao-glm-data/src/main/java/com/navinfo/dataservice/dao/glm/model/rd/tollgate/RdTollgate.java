package com.navinfo.dataservice.dao.glm.model.rd.tollgate;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class RdTollgate implements IObj {
	
	private int pid;
	private int inLinkPid;
	private int nodePid;
	private int outLinkPid;
	private int type=0;
	private int passageNum=0;
	private int etcFigureCode=0;
	private String hwName="";
	private int feeType=2;
	private int feeStd=0;
	private int systemId=0;
	private int locationFlag=0;
	private String rowId;
	public Map<String, Object> changedFields = new HashMap<String, Object>();
	private List<IRow> tollgateName = new ArrayList<IRow>();
	public Map<String, RdTollgateName> tollgateNameMap = new HashMap<String, RdTollgateName>();
	private List<IRow> tollgatePassage = new ArrayList<IRow>();
	public Map<String, RdTollgatePassage> tollgatePassageMap = new HashMap<String, RdTollgatePassage>();

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
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

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getPassageNum() {
		return passageNum;
	}

	public void setPassageNum(int passageNum) {
		this.passageNum = passageNum;
	}

	public int getEtcFigureCode() {
		return etcFigureCode;
	}

	public void setEtcFigureCode(int etcFigureCode) {
		this.etcFigureCode = etcFigureCode;
	}

	public String getHwName() {
		return hwName;
	}

	public void setHwName(String hwName) {
		this.hwName = hwName;
	}

	public int getFeeType() {
		return feeType;
	}

	public void setFeeType(int feeType) {
		this.feeType = feeType;
	}

	public int getFeeStd() {
		return feeStd;
	}

	public void setFeeStd(int feeStd) {
		this.feeStd = feeStd;
	}

	public int getSystemId() {
		return systemId;
	}

	public void setSystemId(int systemId) {
		this.systemId = systemId;
	}

	public int getLocationFlag() {
		return locationFlag;
	}

	public void setLocationFlag(int locationFlag) {
		this.locationFlag = locationFlag;
	}

	public String getRowId() {
		return rowId;
	}

	public List<IRow> getTollgateName() {
		return tollgateName;
	}

	public void setTollgateName(List<IRow> tollgateName) {
		this.tollgateName = tollgateName;
	}

	public List<IRow> getTollgatePassage() {
		return tollgatePassage;
	}

	public void setTollgatePassage(List<IRow> tollgatePassage) {
		this.tollgatePassage = tollgatePassage;
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
		return "rd_tollgate";
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
		return ObjType.RDTOLLGATE;
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
		return "pid";
	}

	@Override
	public int parentPKValue() {
		return pid;
	}

	@Override
	public String parentTableName() {
		return "rd_tollgate";
	}

	@Override
	public List<List<IRow>> children() {
		List<List<IRow>> children = new ArrayList<List<IRow>>();
		children.add(this.getTollgateName());
		children.add(this.getTollgatePassage());
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
			}  else {
				if ( !"objStatus".equals(key)) {
					
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
	public int mesh() {
		return 0;
	}

	@Override
	public void setMesh(int mesh) {

	}

	@Override
	public JSONObject Serialize(ObjLevel objLevel) throws Exception {
		JSONObject json = JSONObject.fromObject(this);

		return json;
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
		return pid;
	}

	@Override
	public String primaryKey() {
		return "pid";
	}

	@Override
	public Map<Class<? extends IRow>, List<IRow>> childList() {
		Map<Class<? extends IRow>,List<IRow>> childList = new HashMap<Class<? extends IRow>, List<IRow>>();
		childList.put(RdTollgateName.class, tollgateName);
		childList.put(RdTollgatePassage.class, tollgatePassage);
		return childList;
	}

	@Override
	public Map<Class<? extends IRow>, Map<String, ?>> childMap() {
		Map<Class<? extends IRow>,Map<String,?>> childMap = new HashMap<Class<? extends IRow>,Map<String,?>>();
		childMap.put(RdTollgateName.class, tollgateNameMap);
		childMap.put(RdTollgatePassage.class, tollgatePassageMap);
		return childMap;
	}

}
