package com.navinfo.dataservice.day2mon;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiNameFlag;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.Batch;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.BatchCommand;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.check.Check;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.check.CheckCommand;
import com.navinfo.navicommons.database.sql.DBUtils;

public class PostBatch {
	Logger log = LoggerRepos.getLogger(this.getClass());
	private Connection conn;
	private OperationResult opResult ;

	public PostBatch(OperationResult opResult,Connection conn) {
		super();
		this.opResult = opResult;
		this.conn = conn;
	}
	
	public void execute() throws Exception{
		// 200170特殊处理
		log.info("执行200170特殊处理");
		deteal200170();
		// 200140与201150特殊处理
		log.info("执行200140特殊处理");
		deteal200140();
		// 201250特殊处理
		log.info("执行201250特殊处理");
		deteal201250();
		// 处理sourceFlag
		log.info("执行sourceFlag特殊处理");
		detealSourceFlag();
	}
	
	// 处理sourceFlag
	private void detealSourceFlag() throws Exception {
		OperationResult operationResult=new OperationResult();
		operationResult.putAll(changeSourceFlag("FM-YW-20-013","002000010000"));
		operationResult.putAll(changeSourceFlag("FM-YW-20-012","002000010000"));
		operationResult.putAll(changeSourceFlag("FM-YW-20-014","002000060000"));
		operationResult.putAll(changeSourceFlag("FM-YW-20-017","002000090000"));
	}
	
	private List<BasicObj> changeSourceFlag(String workItem,String sourceFlag) throws Exception {
		List<Long> pidList = getPidByWorkItem(workItem);
		log.info("changeSourceFlag:"+workItem+",pids:"+pidList.toString());
		List<BasicObj> objList = new ArrayList<BasicObj>();
		for (Long pid:pidList) {
			List<BasicObj> allObjs = opResult.getAllObjs();
			for (BasicObj obj:allObjs) {
				IxPoi ixPoi = (IxPoi) obj.getMainrow();
				if (ixPoi.getPid() == pid) {
					List<BasicRow> nameList = obj.getSubrows().get("IX_POI_NAME");
					Long nameId = 0l;
					for (BasicRow name:nameList) {
						IxPoiName poiName = (IxPoiName) name;
						if (poiName.getLangCode().equals("ENG")&&poiName.getNameType()==2&&poiName.getNameClass()==1) {
							nameId = poiName.getNameId();
						}
					}
					List<BasicRow> flagList = obj.getSubrows().get("IX_POI_NAME_FLAG");
					for (BasicRow flag:flagList) {
						IxPoiNameFlag poiFlag = (IxPoiNameFlag) flag;
						if (poiFlag.getNameId() == nameId) {
							poiFlag.setFlagCode(sourceFlag);
						}
					}
					objList.add(obj);
				}
			}
		}
		return objList;
	}
	
	// 200170特殊处理
	private void deteal200170() throws Exception {
		int handler = 200170;
		List<Long> pidList = getPidByHandler(handler);
		log.info("特殊处理200170pids:"+pidList.toString());
		OperationResult operationResult=new OperationResult();
		List<BasicObj> objList = new ArrayList<BasicObj>();
		for (Long pid:pidList) {
			List<BasicObj> allObj = opResult.getAllObjs();
			for (BasicObj obj:allObj) {
				IxPoi ixPoi = (IxPoi) obj.getMainrow();
				if (ixPoi.getPid() == pid) {
					objList.add(obj);
				}
			}
		}
		operationResult.putAll(objList);
		
		// 执行批处理FM-BAT-20-115
		BatchCommand batchCommand=new BatchCommand();
		batchCommand.setRuleId("FM-BAT-20-115");
		Batch batch=new Batch(conn,operationResult);
		batch.operate(batchCommand);
		// 执行检查项FM-YW-20-052
		CheckCommand checkCommand=new CheckCommand();		
		List<String> checkList=new ArrayList<String>();
		checkList.add("FM-YW-20-052");
		checkCommand.setRuleIdList(checkList);
		Check check=new Check(conn,operationResult);
		check.operate(checkCommand);
		Map<String, Map<Long, Set<String>>> checkResult = check.getErrorPidMap();
		// 打上052标记
		Map<Long, Set<String>> poiMap = checkResult.get("IX_POI");
		Set<String> workItem = new HashSet<String>();
		workItem.add("FM-YW-20-052");
		for (Long pid:poiMap.keySet()) {
			updateColumnStatus(pid,workItem,1);
		}
		// 执行批处理FM-BAT-20-135,FM-BAT-20-163
		batchCommand=new BatchCommand();
		batchCommand.setRuleId("FM-BAT-20-135");
		batchCommand.setRuleId("FM-BAT-20-163");
		batch=new Batch(conn,operationResult);
		batch.operate(batchCommand);
	}
	
	// 200140与201150特殊处理
	private void deteal200140() throws Exception {
		int handler = 200140;
		List<Long> pidList = getPidByHandler(handler);
		handler = 200150;
		pidList.addAll(getPidByHandler(handler));
		log.info("特殊处理200140和200150:"+pidList.toString());
		OperationResult operationResult=new OperationResult();
		List<BasicObj> objList = new ArrayList<BasicObj>();
		for (Long pid:pidList) {
			List<BasicObj> allObj = opResult.getAllObjs();
			for (BasicObj obj:allObj) {
				IxPoi ixPoi = (IxPoi) obj.getMainrow();
				if (ixPoi.getPid() == pid) {
					objList.add(obj);
				}
			}
		}
		operationResult.putAll(objList);
		
		// 执行批处理FM-BAT-20-115
		BatchCommand batchCommand=new BatchCommand();
		batchCommand.setRuleId("FM-BAT-20-115");
		Batch batch=new Batch(conn,operationResult);
		batch.operate(batchCommand);
	}
	
	// 201250特殊处理
	private void deteal201250() throws Exception {
		int handler = 201250;
		List<Long> pidList = getPidByHandler(handler);
		log.info("特殊处理200150:"+pidList.toString());
		OperationResult operationResult=new OperationResult();
		List<BasicObj> objList = new ArrayList<BasicObj>();
		for (Long pid:pidList) {
			List<BasicObj> allObj = opResult.getAllObjs();
			for (BasicObj obj:allObj) {
				IxPoi ixPoi = (IxPoi) obj.getMainrow();
				if (ixPoi.getPid() == pid) {
					objList.add(obj);
				}
			}
		}
		operationResult.putAll(objList);
		
		// 执行批处理FM-BAT-20-125
		BatchCommand batchCommand=new BatchCommand();
		batchCommand.setRuleId("FM-BAT-20-125");
		Batch batch=new Batch(conn,operationResult);
		batch.operate(batchCommand);
	}
	
	// 获取需特殊处理的数据的pid
	private List<Long> getPidByHandler(int handler) throws Exception {
		String sql = "SELECT DISTINCT pid from poi_column_status WHERE handler=?";
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, handler);
			resultSet = pstmt.executeQuery();
			List<Long> pidList = new ArrayList<Long>();
			while (resultSet.next()) {
				pidList.add(resultSet.getLong("pid"));
			}
			return pidList;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DBUtils.closeStatement(pstmt);
		}
	}
	
	// 通过作业标记获取pid
	private List<Long> getPidByWorkItem(String workItem) throws Exception{
		String sql = "SELECT DISTINCT pid from poi_column_status WHERE work_item_id=?";
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, workItem);
			resultSet = pstmt.executeQuery();
			List<Long> pidList = new ArrayList<Long>();
			while (resultSet.next()) {
				pidList.add(resultSet.getLong("pid"));
			}
			return pidList;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DBUtils.closeStatement(pstmt);
		}
	}
	
	// 执行重分类，更新POI_COLUMN_STATUS 表
	private void updateColumnStatus(Long pid,Set<String> workItemId,int handler) throws Exception {
		PreparedStatement pstmt = null;
		try {
			for (String workItem:workItemId) {
				StringBuilder sb = new StringBuilder(" MERGE INTO poi_column_status T1 ");
				sb.append(" USING (SELECT "+pid+" as b,'" + workItem + "' as c," + handler
						+ " as d  FROM dual) T2 ");
				sb.append(" ON ( T1.pid=T2.b and T1.work_item_id=T2.c) ");
				sb.append(" WHEN MATCHED THEN ");
				sb.append(" UPDATE SET T1.first_work_status = 1,T1.second_work_status = 1,T1.handler = T2.d ");
				sb.append(" WHEN NOT MATCHED THEN ");
				sb.append(" INSERT (T1.pid,T1.work_item_id,T1.first_work_status,T1.second_work_status,T1.handler) VALUES(T2.b,T2.c,1,1,T2.d)");
				pstmt = conn.prepareStatement(sb.toString());
				pstmt.execute();
			}
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeStatement(pstmt);
		}
	}
}
