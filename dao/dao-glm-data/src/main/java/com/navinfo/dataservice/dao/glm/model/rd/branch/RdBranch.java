package com.navinfo.dataservice.dao.glm.model.rd.branch;

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

public class RdBranch implements IObj {

	private int pid;

	private int inLinkPid;

	private int nodePid;

	private int outLinkPid;

	private int relationshipType;

	private String rowId;

	private Map<String, Object> changedFields = new HashMap<String, Object>();

	private List<IRow> details = new ArrayList<IRow>();

	private List<IRow> signboards = new ArrayList<IRow>();

	private List<IRow> signasreals = new ArrayList<IRow>();

	private List<IRow> seriesbranches = new ArrayList<IRow>();

	private List<IRow> realimages = new ArrayList<IRow>();

	private List<IRow> schematics = new ArrayList<IRow>();

	private List<IRow> vias = new ArrayList<IRow>();

	public Map<Integer, RdBranchDetail> detailMap = new HashMap<Integer, RdBranchDetail>();

	public Map<Integer, RdSignboard> signboardMap = new HashMap<Integer, RdSignboard>();

	public Map<Integer, RdSignasreal> signasrealMap = new HashMap<Integer, RdSignasreal>();

	public Map<String, RdSeriesbranch> seriesbranchMap = new HashMap<String, RdSeriesbranch>();

	public Map<String, RdBranchRealimage> realimageMap = new HashMap<String, RdBranchRealimage>();

	public Map<Integer, RdBranchSchematic> schematicMap = new HashMap<Integer, RdBranchSchematic>();

	public Map<String, RdBranchVia> viaMap = new HashMap<String, RdBranchVia>();

	
	//outNodePid不属于模型字段，使用protected修饰符。
	protected int outNodePid;

	public int igetOutNodePid() {
		return outNodePid;
	}

	public void isetOutNodePid(int outNodePid) {
		this.outNodePid = outNodePid;
	}

	@Override
	public void setRowId(String rowId) {
		this.rowId = rowId;
	}

	public RdBranch() {

	}

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

	public int getRelationshipType() {
		return relationshipType;
	}

	public void setRelationshipType(int relationshipType) {
		this.relationshipType = relationshipType;
	}

	public List<IRow> getDetails() {
		return details;
	}

	public void setDetails(List<IRow> details) {
		this.details = details;
	}

	public List<IRow> getSignboards() {
		return signboards;
	}

	public void setSignboards(List<IRow> signboards) {
		this.signboards = signboards;
	}

	public List<IRow> getSignasreals() {
		return signasreals;
	}

	public void setSignasreals(List<IRow> signasreals) {
		this.signasreals = signasreals;
	}

	public List<IRow> getSeriesbranches() {
		return seriesbranches;
	}

	public void setSeriesbranches(List<IRow> seriesbranches) {
		this.seriesbranches = seriesbranches;
	}

	public List<IRow> getRealimages() {
		return realimages;
	}

	public void setRealimages(List<IRow> realimages) {
		this.realimages = realimages;
	}

	public List<IRow> getSchematics() {
		return schematics;
	}

	public void setSchematics(List<IRow> schematics) {
		this.schematics = schematics;
	}

	public List<IRow> getVias() {
		return vias;
	}

	public void setVias(List<IRow> vias) {
		this.vias = vias;
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

			JSONArray ja = null;

			if (json.get(key) instanceof JSONArray) {

				switch (key) {
				case "details":
					details.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						RdBranchDetail row = new RdBranchDetail();

						row.Unserialize(jo);

						details.add(row);
					}

					break;

				case "signboards":
					signboards.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						RdSignboard row = new RdSignboard();

						row.Unserialize(jo);

						signboards.add(row);
					}

					break;

				case "signasreals":
					signasreals.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						RdSignasreal row = new RdSignasreal();

						row.Unserialize(jo);

						signasreals.add(row);
					}

					break;

				case "seriesbranches":
					seriesbranches.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						RdSeriesbranch row = new RdSeriesbranch();

						row.Unserialize(jo);

						seriesbranches.add(row);
					}

					break;

				case "realimages":
					realimages.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						RdBranchRealimage row = new RdBranchRealimage();

						row.Unserialize(jo);

						realimages.add(row);
					}

					break;

				case "schematics":
					schematics.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						RdBranchSchematic row = new RdBranchSchematic();

						row.Unserialize(jo);

						schematics.add(row);
					}

					break;
				case "vias":
					vias.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						RdBranchVia row = new RdBranchVia();

						row.Unserialize(jo);

						vias.add(row);
					}

					break;

				default:
					break;
				}

			} else {
				Field f = this.getClass().getDeclaredField(key);

				f.setAccessible(true);

				f.set(this, json.get(key));
			}
		}

		return true;
	}

	@Override
	public String tableName() {

		return "rd_branch";
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

		return ObjType.RDBRANCH;
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

		return this.getPid();
	}

	@Override
	public List<List<IRow>> children() {

		List<List<IRow>> children = new ArrayList<List<IRow>>();

		children.add(this.getDetails());

		children.add(this.getSignboards());

		children.add(this.getSignasreals());

		children.add(this.getSeriesbranches());

		children.add(this.getRealimages());

		children.add(this.getSchematics());

		children.add(this.getVias());

		return children;
	}

	@Override
	public String parentTableName() {

		return "rd_branch";
	}
	
	public String getRowId() {
		return rowId;
	}

	@Override
	public String rowId() {

		return this.rowId;
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

		if (changedFields.size() > 0) {
			return true;
		} else {
			return false;
		}

	}

	@Override
	public List<IRow> relatedRows() {
		return null;
	}

	@Override
	public int pid() {
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
		return "branch_pid";
	}

	@Override
	public Map<Class<? extends IRow>, List<IRow>> childList() {
		return null;
	}

	@Override
	public Map<Class<? extends IRow>,Map<String,?>> childMap() {
		Map<Class<? extends IRow>,Map<String,?>> childMap = new HashMap<>();
		return childMap;
	}
}
