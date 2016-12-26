package com.navinfo.dataservice.column.job;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONObject;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.control.column.core.DeepCoreControl;
import com.navinfo.dataservice.dao.plus.log.LogDetail;
import com.navinfo.dataservice.dao.plus.log.ObjHisLogParser;
import com.navinfo.dataservice.dao.plus.log.PoiLogDetailStat;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.selector.ObjBatchSelector;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.check.Check;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.check.CheckCommand;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.navicommons.database.QueryRunner;
/**
 * poi精编检查
 * @author zhangxiaoyi
 *
 */
public class PoiColumnValidationJob extends AbstractJob {

	public PoiColumnValidationJob(JobInfo jobInfo) {
		super(jobInfo);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void execute() throws JobException {
		log.info("start PoiColumnValidationJob");
		PoiColumnValidationJobRequest myRequest = (PoiColumnValidationJobRequest) request;
		Connection conn=null;
		try{
			conn=DBConnector.getInstance().getConnectionById(myRequest.getTargetDbId());
			log.info("PoiColumnValidationJob:获取精编检查数据pid");
			//获取要检查的数据pid
			getCheckPidList(conn,myRequest);
			if(myRequest.getPids()==null||myRequest.getPids().size()==0){
				log.info("没有需要检查的数据");
				return;
			}
			log.info("PoiColumnValidationJob:获取精编检查规则列表");
			//获取规则号列表
			getCheckRuleList(conn, myRequest);			
			// 清理检查结果
			log.info("清理检查结果");
			DeepCoreControl deepControl = new DeepCoreControl();
			List<Integer> pidIntList=new ArrayList<Integer>();
			for(Long pidTmp:myRequest.getPids()){
				pidIntList.add(Integer.valueOf(pidTmp.toString()));
			}
			deepControl.cleanExByCkRule(conn, pidIntList, myRequest.getRules(), ObjectName.IX_POI);
			/*JSONObject jsonReq=new JSONObject();
			jsonReq.put("subtaskId", jobInfo.getTaskId());
			jsonReq.put("pids", myRequest.getPids());
			jsonReq.put("ckRules", myRequest.getRules());
			jsonReq.put("checkType", 1);
			deepControl.cleanCheck(jsonReq, jobInfo.getUserId());*/
			log.info("PoiColumnValidationJob:获取精编检查数据履历");
			//获取log
			Map<Long, List<LogDetail>> logs = PoiLogDetailStat.loadByColEditStatus(conn, myRequest.getPids(),
					jobInfo.getUserId(),jobInfo.getTaskId(),myRequest.getFirstWorkItem(),myRequest.getSecondWorkItem());
			Set<String> tabNames=getChangeTableSet(logs);
			log.info("PoiColumnValidationJob:加载精编检查对象");
			//获取poi对象			
			Map<Long, BasicObj> objs = ObjBatchSelector.selectByPids(conn, ObjectName.IX_POI, tabNames, false,
					myRequest.getPids(), false, false);
			//将poi对象与履历合并起来
			ObjHisLogParser.parse(objs, logs);
			log.info("PoiColumnValidationJob:执行检查");
			//构造检查参数，执行检查
			OperationResult operationResult=new OperationResult();
			Map<String,Map<Long,BasicObj>> objsMap=new HashMap<String, Map<Long,BasicObj>>();
			objsMap.put(ObjectName.IX_POI, objs);
			operationResult.putAll(objsMap);
			
			CheckCommand checkCommand=new CheckCommand();
			checkCommand.setRuleIdList(myRequest.getRules());
			
			Check check=new Check(conn, operationResult);
			check.operate(checkCommand);
			log.info("end PoiColumnValidationJob");
		}catch(Exception e){
			log.error("PoiColumnValidationJob错误", e);
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new JobException(e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	/**
	 * 分析履历，将履历中涉及的变更过的子表集合返回
	 * @param logs
	 * @return [IX_POI_NAME,IX_POI_ADDRESS]
	 */
	private Set<String> getChangeTableSet(Map<Long, List<LogDetail>> logs) {
		Set<String> subtables=new HashSet<String>();
		if(logs==null || logs.size()==0){return subtables;}
		String mainTable="IX_POI";
		for(Long objId:logs.keySet()){
			List<LogDetail> logList = logs.get(objId);
			for(LogDetail logTmp:logList){
				String tableName = logTmp.getTbNm();
				if(!mainTable.equals(tableName)){subtables.add(tableName);}
			}
		}
		return subtables;
	}

	/**
	 * 获取精编检查对象pid
	 * 1.pids有值，则直接针对改pid进行检查
	 * 2.pids无值,查询job用户，子任务，一级项，二级项对应的待作业，已作业状态的poi列表
	 * @param conn
	 * @param myRequest
	 * @throws JobException
	 */
	private void getCheckPidList(Connection conn,
			PoiColumnValidationJobRequest myRequest) throws JobException {
		try{
			List<Long> pids = myRequest.getPids();
			if(pids!=null&&pids.size()>0){return;}
			String sql="SELECT DISTINCT P.PID"
					+ "  FROM POI_COLUMN_STATUS P, POI_COLUMN_WORKITEM_CONF C"
					+ " WHERE P.WORK_ITEM_ID = C.WORK_ITEM_ID"
					+ "   AND C.CHECK_FLAG IN (1, 2)"
					+ "   AND C.FIRST_WORK_ITEM = '"+myRequest.getFirstWorkItem()+"'"
					+ "   AND P.HANDLER="+jobInfo.getUserId()
					+ "   AND P.TASK_ID="+jobInfo.getTaskId()
					+ "   AND P.SECOND_WORK_STATUS IN (1,2)";
			String secondWorkItem=myRequest.getSecondWorkItem();
			//若针对二级项进行自定义检查，则检查对象应该是二级项状态为待作业/已作业状态
			if(secondWorkItem!=null&&!secondWorkItem.isEmpty()){
				sql+="   AND C.SECOND_WORK_ITEM = '"+myRequest.getSecondWorkItem()+"'"
						+"   AND P.SECOND_WORK_STATUS ="+myRequest.getStatus();
			}
			QueryRunner run=new QueryRunner();
			pids=run.query(conn, sql,new ResultSetHandler<List<Long>>(){

				@Override
				public List<Long> handle(ResultSet rs) throws SQLException {
					List<Long> pids =new ArrayList<Long>();
					while (rs.next()) {
						pids.add(rs.getLong("PID"));						
					}
					return pids;
				}});
			myRequest.setPids(pids);
		}catch(Exception e){
			log.error("精编获取检查数据报错", e);
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new JobException(e);
		}
	}

	/**
	 * 获取精编自定义检查规则
	 * 1.参数request中rules有值，则直接返回
	 * 2.rule没值，通过request的FirstWorkItem参数获取一级项对应的自定义检查规则列表
	 * 
	 * poi精编按照一级项获取检查规则，poi精编深度信息按照二级项获取检查规则
	 * @param conn
	 * @param myRequest
	 * @throws JobException
	 */
	private void getCheckRuleList(Connection conn,PoiColumnValidationJobRequest myRequest) throws JobException{
		try{
			List<String> rules = myRequest.getRules();
			if(rules!=null && rules.size()>0){return;}
			String sql="SELECT DISTINCT WORK_ITEM_ID"
					+ "  FROM POI_COLUMN_WORKITEM_CONF C"
					+ " WHERE C.FIRST_WORK_ITEM = '"+myRequest.getFirstWorkItem()+"'"
					+ "   AND CHECK_FLAG IN (2, 3)";
			//poi精编按照一级项获取检查规则，poi精编深度信息按照二级项获取检查规则
			if(myRequest.getFirstWorkItem().equals("poi_deep")){
				sql+="   AND C.SECOND_WORK_ITEM='"+myRequest.getSecondWorkItem()+"'";
			}
			QueryRunner run=new QueryRunner();
			rules=run.query(conn, sql, new ResultSetHandler<List<String>>(){

				@Override
				public List<String> handle(ResultSet rs) throws SQLException {
					List<String> rules=new ArrayList<String>();
					while(rs.next()){
						rules.add(rs.getString("WORK_ITEM_ID"));
					}
					return rules;
				}});
			myRequest.setRules(rules);
		}catch(Exception e){
			log.error("PoiColumnValidationJob错误", e);
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new JobException(e);
		}
	}

}
