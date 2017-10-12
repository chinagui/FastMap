package com.navinfo.dataservice.row.job;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.control.column.core.DeepCoreControl;
import com.navinfo.dataservice.dao.plus.log.LogDetail;
import com.navinfo.dataservice.dao.plus.log.LogDetailRsHandler4ChangeLog;
import com.navinfo.dataservice.dao.plus.log.ObjHisLogParser;
import com.navinfo.dataservice.dao.plus.log.PoiLogDetailStat;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.operation.OperationSegment;
import com.navinfo.dataservice.dao.plus.selector.ObjBatchSelector;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.Batch;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.BatchCommand;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.check.Check;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.check.CheckCommand;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.exception.LockException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.navicommons.database.QueryRunner;

import net.sf.json.JSONObject;

/**
 * 点门牌数据提交接口
 * @Title:PointAddressReleaseJob
 * @Package:com.navinfo.dataservice.row.job
 * @Description: 
 *  1）查询子任务下待提交的数据，进行加锁
	2) 对待提交的数据执行点门牌检查，其中鲜度验证、删除的数据不做检查
	3) 无检查错误数据执行批处理FM-BAT-PA20-002，无检查错误数据和鲜度验证数据执行批处理FM-BAT-PA20-001
	4）无检查错误、鲜度验证数据修改status状态为已提交
	5）提交以Job形式执行
 * @author:Jarvis 
 * @date: 2017年9月29日
 */
public class PointAddressReleaseJob extends AbstractJob{
	public PointAddressReleaseJob(JobInfo jobInfo) {
		super(jobInfo);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void execute() throws JobException {
		PointAddressReleaseJobRequest myRequest=(PointAddressReleaseJobRequest) request;
		Connection conn=null;
		try{
			conn=DBConnector.getInstance().getConnectionById(myRequest.getDbId());
			log.info("pointAddressReleaseJob:获取要检查的数据pid");
			//获取要检查的数据pid
			String sqlCond=" AND PS.FRESH_VERIFIED <> 1 AND IP.U_RECORD <> 2";
			String sqlFreshCond=" AND PS.FRESH_VERIFIED = 1 AND IP.U_RECORD <> 2";
			List<Long> poiPids = getCheckPidList(conn,myRequest,sqlCond);//过滤鲜度认证删除的数据
			List<Long> freshPoiPids = getCheckPidList(conn,myRequest,sqlFreshCond);//鲜度认证的数据
			List<Long> allPoiPids = getCheckPidList(conn,myRequest,null);//不过滤鲜度认证删除的数据
			log.info("PointAddressReleaseJob:获取要检查的数据的履历");
			//获取log
			Map<Long, List<LogDetail>> logs = loadByPointAddressStatus(conn, poiPids);
			Map<String, Set<String>> tabNames=ObjHisLogParser.getChangeTableSet(logs);
			log.info("PointAddressReleaseJob:加载检查对象");
			//获取poi对象			
			Map<Long, BasicObj> objs = ObjBatchSelector.selectByPids(conn, 
					ObjectName.IX_POINTADDRESS, tabNames.get(ObjectName.IX_POINTADDRESS), false,
					poiPids, true, true);
			//将poi对象与履历合并起来
			ObjHisLogParser.parse(objs, logs);
			
			//获取鲜度认证数据log
			Map<Long, List<LogDetail>> freshPoiPidsLogs = loadByPointAddressStatus(conn, freshPoiPids);
			Map<String, Set<String>> freshTabNames = ObjHisLogParser.getChangeTableSet(freshPoiPidsLogs);
			log.info("PointAddressReleaseJob:加载检查对象");
			//获取鲜度认证数据对象			
			Map<Long, BasicObj> freshObjs = ObjBatchSelector.selectByPids(conn, 
					ObjectName.IX_POINTADDRESS, freshTabNames.get(ObjectName.IX_POINTADDRESS), false,
					freshPoiPids, true, true);
			//将鲜度认证数据对象与履历合并起来
			ObjHisLogParser.parse(freshObjs, freshPoiPidsLogs);
			
			log.info("PointAddressReleaseJob:执行检查");
			
			//构造检查参数
			log.info("构造检查参数");
			OperationResult operationResult=new OperationResult();
			Map<String,Map<Long,BasicObj>> objsMap=new HashMap<String, Map<Long,BasicObj>>();
			objsMap.put(ObjectName.IX_POINTADDRESS, objs);
			operationResult.putAll(objsMap);
			
			CheckCommand checkCommand=new CheckCommand();
			checkCommand.setOperationName(getOperationName());
			
			// 清理检查结果
			log.info("清理检查结果");
			DeepCoreControl deepControl = new DeepCoreControl();
			List<Integer> pidIntList=new ArrayList<Integer>();
			for(Long pidTmp:poiPids){
				pidIntList.add(Integer.valueOf(pidTmp.toString()));
			}
			deepControl.cleanExByCkRule(myRequest.getDbId(), pidIntList, checkCommand.getRuleIdList(), ObjectName.IX_POINTADDRESS);
			
			log.info("执行检查");
			Check check=new Check(conn, operationResult);
			check.operate(checkCommand);
			
			log.info("构造无检查错误数据批处理参数");
			OperationResult batchData=new OperationResult();
			batchData=queryNoErrorData(operationResult,conn,poiPids);
			
			log.info("执行无检查错误数据批处理");
			BatchCommand batchCommand=new BatchCommand();		
			batchCommand.setRuleId("FM-BAT-PA20-002");
			batchCommand.setRuleId("FM-BAT-PA20-001");
			Batch batch=new Batch(conn,batchData);
			batch.setSubtaskId((int)jobInfo.getTaskId());
			batch.operate(batchCommand);
			batch.persistChangeLog(OperationSegment.SG_ROW, jobInfo.getUserId());
			
			OperationResult freshData = new OperationResult();
			Map<String,Map<Long,BasicObj>> freshMap=new HashMap<String, Map<Long,BasicObj>>();
			freshMap.put(ObjectName.IX_POINTADDRESS, freshObjs);
			freshData.putAll(freshMap);
			
			log.info("执行鲜度认证数据批处理");
			BatchCommand freshDataCommand = new BatchCommand();		
			freshDataCommand.setRuleId("FM-BAT-PA20-001");
			Batch freshBatch=new Batch(conn,freshData);
			freshBatch.setSubtaskId((int)jobInfo.getTaskId());
			freshBatch.operate(freshDataCommand);
			freshBatch.persistChangeLog(OperationSegment.SG_ROW, jobInfo.getUserId());
			
			//修改数据提交状态:将没有检查错误的已作业poi进行提交
			log.info("start change pointaddress_edit_status=3 commit");
			int count=commitPoi(conn,allPoiPids);
			
			log.info("end change pointaddress_edit_status=3 commit ："+count+" 条");
			JSONObject response =new JSONObject();
			response.put("count", count);
			JSONObject data =new JSONObject();
			data.put("type", "提交");
			data.put("resNum", count);
			this.exeResultMsg=" #"+data.toString()+"#";
			log.info("end PointAddressReleaseJob");
			super.response("点门牌提交成功！",response);
			
		}catch(Exception e){
			log.error("PointAddressReleaseJob错误", e);
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new JobException(e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	/**
	 * 对点门牌进行履历统计
	 * @param conn
	 * @param pids
	 * @return
	 * @throws SQLException 
	 */
	private Map<Long, List<LogDetail>> loadByPointAddressStatus(Connection conn, List<Long> pids) throws SQLException {
		if (pids == null || pids.size() == 0)
			return null;
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT T.OB_NM,T.OB_PID,T.TB_NM,T.OLD,T.NEW,T.FD_LST,T.OP_TP,T.TB_ROW_ID "
				+ "FROM LOG_DETAIL T,LOG_OPERATION LP,POINTADDRESS_EDIT_STATUS P"
				+ " WHERE T.OP_ID=LP.OP_ID "
				+ " AND T.OB_NM='"
				+ ObjectName.IX_POINTADDRESS + "'" + "   AND T.OB_PID=P.PID");
		// 若P.SUBMIT_DATE最后一次提交时间为空，则，取poi的全部履历；否则取SUBMIT_DATE最后一次提交时间之后的所有履历。
		sb.append(" AND ((LP.OP_DT>=P.SUBMIT_DATE AND P.SUBMIT_DATE IS NOT NULL) OR P.SUBMIT_DATE IS NULL)");

		List<Object> values = new ArrayList<Object>();
		if (pids != null && pids.size() > 0) {
			if (pids.size() > 1000) {
				Clob clob = ConnectionUtil.createClob(conn);
				clob.setString(1, StringUtils.join(pids, ","));
				sb.append(" AND P.PID IN (select to_number(column_value) from table(clob_to_table(?)))");
				values.add(clob);
			} else {
				sb.append(" AND P.PID IN (" + StringUtils.join(pids, ",") + ")");
			}
		}
		log.debug(sb.toString());
		if (values != null && values.size() > 0) {
			Object[] queryValues = new Object[values.size()];
			for (int i = 0; i < values.size(); i++) {
				queryValues[i] = values.get(i);
			}
			return new QueryRunner().query(conn, sb.toString(),
					new LogDetailRsHandler4ChangeLog(), queryValues);
		} else {
			return new QueryRunner().query(conn, sb.toString(),
					new LogDetailRsHandler4ChangeLog());
		}
	}

	public String getOperationName() {
		return "POINTADDRESS_ROW_COMMIT";
	}
	
	
	@Override
	public void lockResources() throws LockException {
	}

	@Override
	public void unlockResources() throws LockException {
	}
	
	/**
	 * 获取需要检查的pidList
	 * @param conn
	 * @param myRequest
	 * @param sqlCond
	 * @return
	 * @throws JobException
	 */
	private List<Long> getCheckPidList(Connection conn,PointAddressReleaseJobRequest myRequest,
				String sqlCond) throws JobException {
		try{
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT ip.pid FROM IX_POINTADDRESS ip, POINTADDRESS_EDIT_STATUS ps WHERE ip.pid = ps.pid");
			if (sqlCond!=null){
				sb.append(sqlCond);
			}
			sb.append(" AND (ps.QUICK_SUBTASK_ID="+(int)jobInfo.getTaskId()+" or ps.MEDIUM_SUBTASK_ID="+(int)jobInfo.getTaskId()+") ");	
	
			QueryRunner run=new QueryRunner();
			return run.query(conn, sb.toString(),new ResultSetHandler<List<Long>>(){

				@Override
				public List<Long> handle(ResultSet rs) throws SQLException {
					List<Long> pids =new ArrayList<Long>();
					while (rs.next()) {
						pids.add(rs.getLong("PID"));						
					}
					return pids;
				}});
		}catch(Exception e){
			log.error("点门牌获取检查数据报错", e);
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new JobException(e);
		}
	}
	
	/**
	 * 提交poi
	 * @param conn
	 * @param poiPids
	 * @return
	 * @throws Exception
	 */
	public int commitPoi(Connection conn,List<Long> poiPids) throws Exception{
		try{
			StringBuilder sb = new StringBuilder();
			sb.append("UPDATE POINTADDRESS_EDIT_STATUS E SET E.STATUS = 3 , E.SUBMIT_DATE = SYSDATE, E.COMMIT_HIS_STATUS = 1 ");
			sb.append("WHERE E.STATUS = 2 ");
			List<Object> values = new ArrayList<Object>();
			if (poiPids != null && poiPids.size() > 0) {
				if (poiPids.size() > 1000) {
					Clob clob = ConnectionUtil.createClob(conn);
					clob.setString(1, StringUtils.join(poiPids, ","));
					sb.append(" AND E.PID IN (select to_number(column_value) from table(clob_to_table(?)))");
					values.add(clob);
				} else {
					sb.append(" AND E.PID IN (" + StringUtils.join(poiPids, ",") + ")");
				}
			}
			sb.append(" AND NOT EXISTS (SELECT 1 FROM CK_RESULT_OBJECT R,NI_VAL_EXCEPTION N WHERE R.TABLE_NAME = 'IX_POINTADDRESS' ");
			sb.append(" AND R.PID = E.PID AND R.MD5_CODE = N.MD5_CODE)");
			sb.append(" AND (E.QUICK_SUBTASK_ID="+(int)jobInfo.getTaskId()+" or E.MEDIUM_SUBTASK_ID="+(int)jobInfo.getTaskId()+") ");
			
			QueryRunner run = new QueryRunner();		
	    	if (values != null && values.size() > 0) {
				Object[] queryValues = new Object[values.size()];
				for (int i = 0; i < values.size(); i++) {
					queryValues[i] = values.get(i);
				}
				return run.update(conn, sb.toString(),queryValues);
	    	}else{
	    		return run.update(conn, sb.toString());
	    	}
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw e;
		}
	}
	
	
	/**
	 * 查询没有检查错误的POI
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	public OperationResult queryNoErrorData(OperationResult opResult,Connection conn,List<Long> poiPids) throws Exception{
		
		Map<Long, BasicObj> objs=opResult.getObjsMapByType(ObjectName.IX_POINTADDRESS);
		OperationResult operationResult=new OperationResult();
		Map<String,Map<Long,BasicObj>> objsMap=new HashMap<String, Map<Long,BasicObj>>();
			
		StringBuilder sb = new StringBuilder();
		Clob clob = null;
		
		sb.append("SELECT E.PID FROM POINTADDRESS_EDIT_STATUS E WHERE E.STATUS = 2 ");
		if (poiPids != null && poiPids.size() > 0) {
			if (poiPids.size() > 1000) {
				clob = ConnectionUtil.createClob(conn);
				clob.setString(1, StringUtils.join(poiPids, ","));
				sb.append(" AND E.PID IN (select to_number(column_value) from table(clob_to_table(?)))");
			} else {
				sb.append(" AND E.PID IN (" + StringUtils.join(poiPids, ",") + ")");
			}
		}
		sb.append(" AND EXISTS (SELECT 1 FROM CK_RESULT_OBJECT R,NI_VAL_EXCEPTION N WHERE R.TABLE_NAME = 'IX_POINTADDRESS'");
		sb.append(" AND R.PID = E.PID AND R.MD5_CODE = N.MD5_CODE)");
		sb.append(" AND (E.QUICK_SUBTASK_ID="+(int)jobInfo.getTaskId()+" or E.MEDIUM_SUBTASK_ID="+(int)jobInfo.getTaskId()+")");
		PreparedStatement pstmt = null;
		ResultSet rs=null;

		try {
			
			pstmt=conn.prepareStatement(sb.toString());
			if(poiPids.size()>1000){
				 pstmt.setClob(1,clob);
			}
			rs=pstmt.executeQuery();
			while(rs.next()){
				Long pid=rs.getLong("PID");
				if(objs.containsKey(pid)){
					objs.remove(pid);
				}
			}
			objsMap.put(ObjectName.IX_POINTADDRESS, objs);
			operationResult.putAll(objsMap);
			return operationResult;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw e;
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(pstmt);
			try {
				pstmt.close();
			} catch (Exception e) {

			}

		}
	}

}
