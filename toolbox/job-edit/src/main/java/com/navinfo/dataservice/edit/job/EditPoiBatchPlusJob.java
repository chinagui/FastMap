package com.navinfo.dataservice.edit.job;

import java.sql.Connection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.plus.log.LogDetail;
import com.navinfo.dataservice.dao.plus.log.ObjHisLogParser;
import com.navinfo.dataservice.dao.plus.log.PoiLogDetailStat;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.operation.OperationSegment;
import com.navinfo.dataservice.dao.plus.selector.ObjBatchSelector;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.Batch;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.BatchCommand;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.exception.LockException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;

/**
 * 
 * @author zhangxiaoyi
 * batchPlus规则测试接口用
 *
 */
public class EditPoiBatchPlusJob extends AbstractJob{

	public EditPoiBatchPlusJob(JobInfo jobInfo) {
		super(jobInfo);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void execute() throws JobException {
		EditPoiBatchPlusJobRequest releaseJobRequest=(EditPoiBatchPlusJobRequest) request;
		Connection conn = null;
		try {
			log.info(releaseJobRequest.getTargetDbId());
			conn = DBConnector.getInstance().getConnectionById(releaseJobRequest.getTargetDbId());
			log.info(releaseJobRequest.getTargetDbId());
			//获取log
			Map<Long, List<LogDetail>> logs = PoiLogDetailStat.loadAllLog(conn, releaseJobRequest.getPids());
			Set<String> tabNames=getChangeTableSet(logs);
			//获取poi对象			
			Map<Long, BasicObj> objs =null;
			if(tabNames==null||tabNames.size()==0){
				log.info(1);
				objs=ObjBatchSelector.selectByPids(conn, ObjectName.IX_POI, tabNames, true,
						releaseJobRequest.getPids(), false, false);
				log.info(2);
			}else{
				objs=ObjBatchSelector.selectByPids(conn, ObjectName.IX_POI, tabNames, false,
						releaseJobRequest.getPids(), false, false);
			}
			//将poi对象与履历合并起来
			ObjHisLogParser.parse(objs, logs);
			
			BatchCommand batchCommand=new BatchCommand();
			for(String rule:releaseJobRequest.getBatchRules()){
				batchCommand.setRuleId(rule);}
			OperationResult operationResult=new OperationResult();
			operationResult.putAll(objs.values());
			Batch batch=new Batch(conn,operationResult);
			batch.operate(batchCommand);
			batch.persistChangeLog(OperationSegment.SG_COLUMN, jobInfo.getUserId());
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new JobException(e);
		} finally {
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
	
	
	@Override
	public void lockResources() throws LockException {
	}

	@Override
	public void unlockResources() throws LockException {
	}
}
