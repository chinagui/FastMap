package com.navinfo.dataservice.impcore.commit.batch;

import java.sql.Clob;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.api.datahub.iface.DatahubApi;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Region;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.impcore.flushbylog.FlushResult;
import com.navinfo.dataservice.impcore.flushbylog.LogFlushUtil;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;

/*
 * @author MaYunFei
 * 2016年6月21日
 * 描述：import-coreBatchLogFlushJob.java
 */
public class BatchLogFlushJob extends AbstractJob {

	public BatchLogFlushJob(JobInfo jobInfo) {
		super(jobInfo);
	}

	@Override
	public void execute() throws JobException {
		HashMap<String,FlushResult> jobResponse = new HashMap<String,FlushResult> ();
		try{
			BatchLogFlushJobRequest req = (BatchLogFlushJobRequest)this.request;
			int batchDbId = req.getBatchDbId();
			DatahubApi databhubApi = (DatahubApi) ApplicationContextUtil.getBean("datahubApi");
			DbInfo sourceDbInfo = databhubApi.getDbById(batchDbId);
			DbInfo targetDbInfo = databhubApi.getDbById(req.getTargetDbId());
			this.log.info("开始进行批处理刷库（源库:"+sourceDbInfo+",目标库："+targetDbInfo+")");
			
			//获取目标库对应的大区信息
			ManApi manApi = (ManApi) ApplicationContextUtil.getBean("manApi");
			Region regionInfo = manApi.queryRegionByDbId(req.getTargetDbId());
			
			LogFlushUtil logUtils = LogFlushUtil.getInstance();
			Connection sourceDbConn = logUtils.intiConenction(sourceDbInfo, true);
			Connection targetDbConn =logUtils.intiConenction(targetDbInfo, true);
			logUtils.createTempTable(sourceDbConn);//创建临时表，
			logUtils.createTargetDbLink(sourceDbInfo, targetDbInfo);//创建指向目标库的dblink
			this.response("数据库初始化完毕", jobResponse);
			String prepareSql="select * from ";
//			logUtils.prepareAndLockLog(sourceDbConn, 
//					prepareSql, 
//					isExtLog, 
//					extLogSql, 
//					isLockLog, 
//					logLockSql);
			this.response("准备待刷新的日志完毕", jobResponse);
			this.response("日落月执行完毕", jobResponse);
		}catch(Exception e){
			throw new JobException(e);
		}

	}
	private  String getPrepareSql(Connection conn,String tempTable,List<Integer> grids) throws Exception{
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO ");
		sb.append(tempTable);
		sb.append(" SELECT DISTINCT P.OP_ID,P.OP_DT FROM LOG_OPERATION P,LOG_DETAIL L,LOG_DETAIL_GRID T WHERE P.OP_ID=L.OP_ID AND L.ROW_ID=T.LOG_ROW_ID ");
		String gridInClause=null;
		if(CollectionUtils.isNotEmpty(grids)){
			if(grids.size()>1000){
				Clob clobGrids = conn.createClob();
				clobGrids.setString(1, StringUtils.join(grids, ","));
				gridInClause = " GRID_ID IN (select to_number(column_value) from table(clob_to_table(?)))";
			}else{
				gridInClause = " GRID_ID IN ("+StringUtils.join(grids, ",")+")";
			}
			
		}
		sb.append(gridInClause);
		return sb.toString();
	}

}

