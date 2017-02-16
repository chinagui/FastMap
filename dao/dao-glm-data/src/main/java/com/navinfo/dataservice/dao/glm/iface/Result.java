package com.navinfo.dataservice.dao.glm.iface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 操作结果
 */
public class Result implements ISerializable {

	private int primaryPid;
	
	private OperStage operStage = OperStage.DayEdit;

	public OperStage getOperStage() {
		return operStage;
	}

	public void setOperStage(OperStage operStage) {
		this.operStage = operStage;
	}

	public int getPrimaryPid() {
		return primaryPid;
	}

	public void setPrimaryPid(int primaryPid) {
		this.primaryPid = primaryPid;
	}
	
	/**
	 * 新增对象列表
	 */
	private List<IRow> listAddIRow = new ArrayList<IRow>();

	/**
	 * 删除对象集合
	 */
	private List<IRow> listDelIRow = new ArrayList<IRow>();

	/**
	 * 修改对象列表
	 */
	private List<IRow> listUpdateIRow = new ArrayList<IRow>();
	
	private List<Integer> listAddIRowObPid = new ArrayList<>();
	
	private List<Integer> listUpdateIRowObPid = new ArrayList<>();
	
	private List<Integer> listDelIRowObPid = new ArrayList<>();

	private JSONArray checkResults = new JSONArray();

	private JSONArray logs = new JSONArray();
	
	HashMap<String, TreeMap<Integer, TreeMap<Integer, IVia>>> viaMap = null;
	
	Map<String, TreeMap<Integer, IVia>> nextViaMap = null;

	public JSONArray getCheckResults() {
		return checkResults;
	}

	public void setCheckResults(JSONArray checkResults) {
		this.checkResults = checkResults;
	}

	/**
	 * 添加对象到结果列表
	 * 
	 * @param row
	 *            对象
	 * @param os
	 *            对象状态
	 * @param topParentPid
	 *            最顶级父表的pid,用来给前台做定位用
	 * 
	 */
	public void insertObject(IRow row, ObjStatus os, int topParentPid) {

		row.setStatus(os);

		JSONObject json = new JSONObject();

		json.put("type", row.objType());

		if (row instanceof IObj) {
			IObj obj = (IObj) row;

			if (obj.parentTableName().equals(obj.tableName())) {
				// 该表没有父
				json.put("pid", obj.pid());

				json.put("childPid", "");
			} else {
				// 该表有父表
				json.put("pid", topParentPid);

				json.put("childPid", obj.pid());
			}
		} else {
			json.put("pid", topParentPid);

			if (row.parentPKValue() == topParentPid) {
				// 该表是二级子表
				json.put("childPid", "");
			} else {
				// 该表是三级子表
				json.put("childPid", row.parentPKValue());
			}
		}

		switch (os) {
		case INSERT:
			listAddIRow.add(row);
			listAddIRowObPid.add(topParentPid);
			json.put("op", "新增");
			break;
		case DELETE:
			listDelIRow.add(row);
			listDelIRowObPid.add(topParentPid);
			json.put("op", "删除");
			break;
		case UPDATE:
			listUpdateIRow.add(row);
			listUpdateIRowObPid.add(topParentPid);
			json.put("op", "修改");
			break;
		default:
			break;
		}
		logs.add(json);

	}

	/**
	 * @return 新增对象列表
	 */
	public List<IRow> getAddObjects() {
		return listAddIRow;
	}

	/**
	 * @return 删除对象列表
	 */
	public List<IRow> getDelObjects() {
		return listDelIRow;
	}

	/**
	 * @return 修改对象列表
	 */
	public List<IRow> getUpdateObjects() {
		return listUpdateIRow;
	}
	
	public List<Integer> getListAddIRowObPid() {
		return listAddIRowObPid;
	}

	public List<Integer> getListUpdateIRowObPid() {
		return listUpdateIRowObPid;
	}

	public List<Integer> getListDelIRowObPid() {
		return listDelIRowObPid;
	}

	@Override
	public JSONObject Serialize(ObjLevel objLevel) {

		return null;
	}

	@Override
	public boolean Unserialize(JSONObject json) throws Exception {

		return false;
	}

	public void clear() {
		this.listAddIRow.clear();
		this.listDelIRow.clear();
		this.listUpdateIRow.clear();
	}

	/**
	 * @return 操作结果信息
	 */
	public String getLogs() {
		BranchResultHandle.handleResult(listAddIRow,logs);
		return logs.toString();

	}
	
	public void add(Result result) {
		this.listAddIRow.addAll(result.getAddObjects());
		this.listUpdateIRow.addAll(result.getUpdateObjects());
		this.listDelIRow.addAll(result.getDelObjects());
	}
	
	

	/**
	 * 连续打断Via维护
	 * @param tableNamePid via表名+父Pid+GROUP_ID(via表有GROUP_ID字段时加)
	 * @param oldSeqNum 打断前Via的seqNum
	 * @param newVias 打断后新增的Via
	 * @param nextVias 后续seqNum变化的Via
	 */
	public void breakVia(String tableNamePid,int oldSeqNum,TreeMap<Integer,IVia> newVias,TreeMap<Integer, IVia> nextVias)
	{
		if (newVias.size() == 0) {
			return;
		}
			
		if (viaMap == null) {
			viaMap = new HashMap<String, TreeMap<Integer, TreeMap<Integer, IVia>>>();
		}
		
		if (nextViaMap == null) {
			nextViaMap = new HashMap<String, TreeMap<Integer, IVia>>();
		}
		
		String nextViaFlag = tableNamePid + String.valueOf(oldSeqNum);
		
		nextViaMap.put(nextViaFlag, nextVias);
		
		//原seqnum,newVias
		TreeMap<Integer, TreeMap<Integer, IVia>> sameGroupVias=null;
		
		if (!viaMap.containsKey(tableNamePid)) {

			sameGroupVias = new TreeMap<Integer, TreeMap<Integer, IVia>>();

			viaMap.put(tableNamePid, sameGroupVias);
		}
		
		sameGroupVias = viaMap.get(tableNamePid);

		if (!sameGroupVias.containsKey(oldSeqNum)) {

			sameGroupVias.put(oldSeqNum, newVias);
		}
		
		handleViaSeqNum(tableNamePid, sameGroupVias);
	}
	
	/**
	 * 更新seqnum
	 * 
	 * @param tableNamePid
	 *            via表名+父Pid+GROUP_ID(via表有GROUP_ID字段时加)
	 * @param sameGroupVias
	 *            TreeMap<Integer：原via的seqnum, TreeMap<Integer：新via的seqnum,
	 *            IVia：新via>>
	 */
	private void handleViaSeqNum(String tableNamePid,
			TreeMap<Integer, TreeMap<Integer, IVia>> sameGroupVias) {

		if (sameGroupVias.size() < 2) {

			return;
		}

		int addSumCount=0;

		for (Map.Entry<Integer, TreeMap<Integer, IVia>> entry : sameGroupVias
				.entrySet()) {

			int newSeqNum = entry.getKey();

			if (entry.getKey() == 0) {
				
				newSeqNum = 1;
			}

			newSeqNum += addSumCount;

			TreeMap<Integer, IVia> newVias = entry.getValue();

			if (newVias == null || newVias.size() == 0) {
				continue;
			}
			
			for(Map.Entry<Integer, IVia> entryNew: newVias.entrySet())
			{
				entryNew.getValue().setSeqNum(newSeqNum++);
			}		

			String nextViaFlag = tableNamePid + String.valueOf(entry.getKey());

			if (nextViaMap.containsKey(nextViaFlag)) {

				TreeMap<Integer, IVia> nextVias = nextViaMap.get(nextViaFlag);

				for (Map.Entry<Integer, IVia> entryNext : nextVias.entrySet()) {
					
					IRow row = (IRow) entryNext.getValue();
					
					setUpdateObjectVia(row.rowId(),  newSeqNum++);
				}
			}

			addSumCount += newVias.size() - 1;

			if (entry.getKey() == 0) {

				addSumCount = newVias.size();
			}
		}
	}
	
	/**
	 * 处理更新结果集合里冗余的VIA序号（seqNum）
	 * @param rowId VIA的rowId
	 * @param seqNum 最新seqNum
	 */
	private void setUpdateObjectVia(String rowId, int seqNum) {

		for (IRow updateRow : listUpdateIRow) {

			if (updateRow instanceof IVia && updateRow.rowId().equals(rowId)) {

				updateRow.changedFields().put("seqNum", seqNum);
			}
		}
	}
}
