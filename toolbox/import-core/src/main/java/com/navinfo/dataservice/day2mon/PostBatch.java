package com.navinfo.dataservice.day2mon;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiNameFlag;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.operation.AbstractOperation;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.operation.OperationSegment;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.Batch;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.BatchCommand;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.check.Check;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.check.CheckCommand;
import com.navinfo.navicommons.database.sql.DBUtils;

public class PostBatch {
	Logger log = LoggerRepos.getLogger(this.getClass());
	private Connection conn;
	private OperationResult opResult;

	public PostBatch(OperationResult opResult, Connection conn) {
		super();
		this.opResult = opResult;
		this.conn = conn;
	}

	public void execute() throws Exception {

		// 20170525 根强与凤琴商讨,所有数据都需要执行115批处理
		log.info("执行115批处理");
		deteal201150();
		// 处理sourceFlag
		log.info("执行sourceFlag特殊处理");
		detealSourceFlag();
		// 200170特殊处理
		log.info("执行200170特殊处理");
		deteal200170();
		// 201250特殊处理
		log.info("执行201250特殊处理");
		deteal201250();
		// handler置0
		updateHandler();
	}
	
	public void execute915() throws Exception {

		// 20170525 根强与凤琴商讨,所有数据都需要执行115批处理
		log.info("执行M108批处理");
		deteal201150_915();
		// 处理sourceFlag
		//og.info("执行sourceFlag特殊处理");
		//detealSourceFlag();
		// 200170特殊处理
		//log.info("执行200170特殊处理");
		//deteal200170();
		// 201250特殊处理
		log.info("执行201250特殊处理");
		deteal201250();
		// handler置0
		updateHandler915();
	}

	// 处理sourceFlag
	public void detealSourceFlag() throws Exception {
		OperationResult operationResult = new OperationResult();
		operationResult.putAll(changeSourceFlag("FM-YW-20-013", "110020010000"));
		operationResult.putAll(changeSourceFlag("FM-YW-20-012", "110020010000"));
		operationResult.putAll(changeSourceFlag("FM-YW-20-014", "110020060000"));
		operationResult.putAll(changeSourceFlag("FM-YW-20-017", "110020090000"));
		PostBatchOperation postBatchOp = new PostBatchOperation(conn, operationResult);
		postBatchOp.setName("DAY2MON");
		postBatchOp.setPhysiDelete(true);
		postBatchOp.persistChangeLog(OperationSegment.SG_COLUMN, 0);
	}

	/**
	 * 20170725变更 by jch
	 * 1、标记FM-YW-20-012数据，删除原始英文和标准英文对应的IX_POI_NAME_FLAG记录，新增一条原始flag记录，
	 * 且给FLAG_CODE赋值110020010000;；
	 * 2、标记FM-YW-20-013数据，删除原始英文和标准英文对应的IX_POI_NAME_FLAG记录，新增一条原始flag记录，
	 * 且给FLAG_CODE赋值110020010000； 3、标记FM-YW-20-014数据，
	 * 如果原始英文或标准英文的FLAG_CODE为110020070000或110020080000或110020090000，
	 * 则删除这些IX_POI_NAME_FLAG记录，然后新增一条原始英文flag记录，且给FLAG_CODE赋值110020060000；
	 * 如果是其它来源（非以上3个来源标示）标示且是标准英文的NAME_ID，则将IX_POI_NAME_FLAG.NAME_ID替换原始NAME_ID；
	 * 如果是其它来源（非以上3个来源标示）标示且是原始英文的FLAG_CODE，则不处理；
	 * 4、标记FM-YW-20-017数据，删除原始英文和标准英文对应的IX_POI_NAME_FLAG记录，新增一条原始flag记录，
	 * 且给FLAG_CODE赋值110020090000;
	 * 
	 * @param workItem
	 * @param sourceFlag
	 * @return
	 * @throws Exception
	 */
	private List<BasicObj> changeSourceFlag(String workItem, String sourceFlag) throws Exception {
		List<Long> pidList = getPidByWorkItem(workItem);
		log.info("changeSourceFlag:" + workItem + ",pids:" + pidList.toString());
		List<BasicObj> objList = new ArrayList<BasicObj>();
		for (Long pid : pidList) {
			List<BasicObj> allObjs = opResult.getAllObjs();
			for (BasicObj obj : allObjs) {
				IxPoi ixPoi = (IxPoi) obj.getMainrow();

				if (ixPoi.getPid() == pid) {
					log.info("pid:" +pid);
					List<BasicRow> nameList = obj.getSubrows().get("IX_POI_NAME");
					IxPoiObj poiObj = (IxPoiObj) obj;
					Long nameId = 0l;
					Long standardNameId = 0l;
					if (CollectionUtils.isNotEmpty(nameList)) {
						for (BasicRow name : nameList) {
							IxPoiName poiName = (IxPoiName) name;
							if (poiName.getLangCode().equals("ENG") && poiName.getNameClass() == 1) {
								if (poiName.getNameType() == 2) {
									nameId = poiName.getNameId();
								} else {
									standardNameId = poiName.getNameId();
								}
							}
						}
					}
					List<BasicRow> flagList = obj.getSubrows().get("IX_POI_NAME_FLAG");

					if (CollectionUtils.isNotEmpty(flagList)) {
						String lastSourceFlag = sourceFlag;
						for (BasicRow flag : flagList) {
							IxPoiNameFlag poiFlag = (IxPoiNameFlag) flag;
							String flagCode = poiFlag.getFlagCode();
							if ("FM-YW-20-014".equals(workItem) && (!("110020070000".equals(flagCode)
									|| "110020080000".equals(flagCode) || "110020090000".equals(flagCode)))) {
								lastSourceFlag = flagCode;
							}
							if (poiFlag.getNameId() == nameId || poiFlag.getNameId() == standardNameId) {
								poiObj.deleteSubrow(poiFlag);
							}
						}
						IxPoiNameFlag poiFlag = poiObj.createIxPoiNameFlag(nameId);
						poiFlag.setFlagCode(lastSourceFlag);

					} else {
						IxPoiNameFlag poiFlag = poiObj.createIxPoiNameFlag(nameId);
						poiFlag.setFlagCode(sourceFlag);
					}
				}
				objList.add(obj);
			}
		}
		return objList;

	}

	// 200170特殊处理
	public void deteal200170() throws Exception {
		int handler = 200170;
		List<Long> pidList = getPidByHandler(handler);
		log.info("特殊处理200170pids:" + pidList.toString());
		OperationResult operationResult = new OperationResult();
		List<BasicObj> objList = new ArrayList<BasicObj>();
		for (Long pid : pidList) {
			List<BasicObj> allObj = opResult.getAllObjs();
			for (BasicObj obj : allObj) {
				IxPoi ixPoi = (IxPoi) obj.getMainrow();
				if (ixPoi.getPid() == pid) {
					objList.add(obj);
				}
			}
		}
		operationResult.putAll(objList);

		// 执行批处理FM-BAT-20-115
		// BatchCommand batchCommand=new BatchCommand();
		// batchCommand.setRuleId("FM-BAT-20-115");
		// Batch batch=new Batch(conn,operationResult);
		// batch.operate(batchCommand);
		// batch.setPhysiDelete(true);
		// persistBatch(batch);
		// 执行检查项FM-YW-20-052
		CheckCommand checkCommand = new CheckCommand();
		List<String> checkList = new ArrayList<String>();
		checkList.add("FM-YW-20-052");
		checkCommand.setRuleIdList(checkList);
		Check check = new Check(conn, operationResult);
		check.operate(checkCommand);
		Map<String, Map<Long, Set<String>>> checkResult = check.getErrorPidMap();
		if (checkResult != null) {
			// 打上052标记
			Map<Long, Set<String>> poiMap = checkResult.get("IX_POI");
			Set<String> workItem = new HashSet<String>();
			workItem.add("FM-YW-20-052");
			for (Long pid : poiMap.keySet()) {
				updateColumnStatus(pid, workItem, 1);
			}
		}
		// 执行批处理FM-BAT-20-135,FM-BAT-20-163
		BatchCommand batchCommand = new BatchCommand();
		batchCommand.setRuleId("FM-BAT-20-135");
		batchCommand.setRuleId("FM-BAT-20-163");
		Batch batch = new Batch(conn, operationResult);
		batch.operate(batchCommand);
		batch.setPhysiDelete(true);
		persistBatch(batch);
	}

	// 所有数据均需执行115批处理
	private void deteal201150() throws Exception {

		log.info("所有数据均需执行115批处理");
		OperationResult operationResult = new OperationResult();
		operationResult.putAll(opResult.getAllObjs());

		// 执行批处理FM-BAT-20-115

		BatchCommand batchCommand=new BatchCommand();
		batchCommand.setRuleId("FM-BAT-M01-08");
		batchCommand.setRuleId("FM-BAT-20-115");
		Batch batch=new Batch(conn,operationResult);

		batch.operate(batchCommand);
		persistBatch(batch);
	}
	
	// 所有数据均需执行115批处理
	private void deteal201150_915() throws Exception {

		log.info("所有数据均需执行115批处理");
		OperationResult operationResult = new OperationResult();
		operationResult.putAll(opResult.getAllObjs());

		// 执行批处理FM-BAT-20-115

		BatchCommand batchCommand=new BatchCommand();
		batchCommand.setRuleId("FM-BAT-M01-08");
		//batchCommand.setRuleId("FM-BAT-20-115");
		Batch batch=new Batch(conn,operationResult);

		batch.operate(batchCommand);
		persistBatch(batch);
	}

	// 201250特殊处理
	private void deteal201250() throws Exception {
		int handler = 201250;
		List<Long> pidList = getPidByHandler(handler);
		log.info("特殊处理200150:" + pidList.toString());
		OperationResult operationResult = new OperationResult();
		List<BasicObj> objList = new ArrayList<BasicObj>();
		for (Long pid : pidList) {
			List<BasicObj> allObj = opResult.getAllObjs();
			for (BasicObj obj : allObj) {
				IxPoi ixPoi = (IxPoi) obj.getMainrow();
				if (ixPoi.getPid() == pid) {
					objList.add(obj);
				}
			}
		}
		operationResult.putAll(objList);

		// 执行批处理FM-BAT-20-125
		BatchCommand batchCommand = new BatchCommand();
		batchCommand.setRuleId("FM-BAT-20-125");
		Batch batch = new Batch(conn, operationResult);
		batch.operate(batchCommand);
		persistBatch(batch);
	}

	private void persistBatch(Batch batch) throws Exception {
		batch.persistChangeLog(OperationSegment.SG_COLUMN, 0);// FIXME:修改默认的用户
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
	private List<Long> getPidByWorkItem(String workItem) throws Exception {
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
	private void updateColumnStatus(Long pid, Set<String> workItemId, int handler) throws Exception {
		PreparedStatement pstmt = null;
		try {
			for (String workItem : workItemId) {
				StringBuilder sb = new StringBuilder(" MERGE INTO poi_column_status T1 ");
				sb.append(
						" USING (SELECT " + pid + " as b,'" + workItem + "' as c," + handler + " as d  FROM dual) T2 ");
				sb.append(" ON ( T1.pid=T2.b and T1.work_item_id=T2.c) ");
				sb.append(" WHEN MATCHED THEN ");
				sb.append(" UPDATE SET T1.first_work_status = 1,T1.second_work_status = 1,T1.handler = T2.d ");
				sb.append(" WHEN NOT MATCHED THEN ");
				sb.append(
						" INSERT (T1.pid,T1.work_item_id,T1.first_work_status,T1.second_work_status,T1.handler) VALUES(T2.b,T2.c,1,1,T2.d)");
				pstmt = conn.prepareStatement(sb.toString());
				pstmt.execute();
			}
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeStatement(pstmt);
		}
	}

	// 后批完成，handler置0
	public void updateHandler() throws Exception {
		String sql = "UPDATE poi_column_status SET handler=0 WHERE handler in (1,201250,200170,200140,201150,107020) and pid in (select to_number(column_value) from table(clob_to_table(?)))";
		PreparedStatement pstmt = null;
		try {
			Map<String, Map<Long, BasicObj>> ObjMap = opResult.getAllObjsMap();
			Map<Long, BasicObj> poiMap = ObjMap.get("IX_POI");
			Set<Long> pids = poiMap.keySet();
			Clob pidsClob = ConnectionUtil.createClob(conn);
			pidsClob.setString(1, StringUtils.join(pids, ","));

			pstmt = conn.prepareStatement(sql);
			pstmt.setClob(1, pidsClob);
			pstmt.executeUpdate();

		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeStatement(pstmt);
		}
	}
	// 后批完成，handler置0,去掉对200170的修改，日落月完成后才批批完再改状态
	private void updateHandler915() throws Exception {
		String sql = "UPDATE poi_column_status SET handler=0 WHERE handler in (1,201250,200140,201150,107020) and pid in (select to_number(column_value) from table(clob_to_table(?)))";
		PreparedStatement pstmt = null;
		try {
			Map<String, Map<Long, BasicObj>> ObjMap = opResult.getAllObjsMap();
			Map<Long, BasicObj> poiMap = ObjMap.get("IX_POI");
			Set<Long> pids = poiMap.keySet();
			Clob pidsClob = ConnectionUtil.createClob(conn);
			pidsClob.setString(1, StringUtils.join(pids, ","));

			pstmt = conn.prepareStatement(sql);
			pstmt.setClob(1, pidsClob);
			pstmt.executeUpdate();

		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeStatement(pstmt);
		}
	}
}
