package com.navinfo.dataservice.impcore.job;

import com.navinfo.dataservice.api.datahub.iface.DatahubApi;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.impcore.flushbylog.FlushResult;
import com.navinfo.dataservice.impcore.flusher.DefaultLogFlusher;
import com.navinfo.dataservice.impcore.flusher.LogFlusher;
import com.navinfo.dataservice.impcore.mover.DefaultLogMover;
import com.navinfo.dataservice.impcore.mover.LogMover;
import com.navinfo.dataservice.impcore.selector.DefaultLogSelector;
import com.navinfo.dataservice.impcore.selector.LogSelector;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;

/** 
* @ClassName: GdbImportJob 
* @author Xiao Xiaowen 
* @date 2016年6月23日 上午11:24:54 
* @Description: TODO
*  
*/
public class GdbImportJob extends AbstractJob {

	public GdbImportJob(JobInfo jobInfo) {
		super(jobInfo);
	}

	@Override
	public void execute() throws JobException {
		try{
			GdbImportJobRequest req = (GdbImportJobRequest)request;
			//1. 履历选择
			DatahubApi datahub = (DatahubApi)ApplicationContextUtil.getBean("datahubApi");
			DbInfo logDbInfo = datahub.getDbById(req.getLogDbId());
			OracleSchema logSchema = new OracleSchema(
					DbConnectConfig.createConnectConfig(logDbInfo.getConnectParam()));
			LogSelector logSelector = new DefaultLogSelector(logSchema);
			String tempTable = logSelector.select();
			response("履历选择完成",null);
			//2. 履历刷库
			DbInfo tarDbInfo = datahub.getDbById(req.getTargetDbId());
			OracleSchema tarSchema = new OracleSchema(
					DbConnectConfig.createConnectConfig(tarDbInfo.getConnectParam()));
			LogFlusher logFlusher = new DefaultLogFlusher(logSchema, tarSchema, false, tempTable);
			FlushResult result = logFlusher.flush();
			response("履历刷库完成",null);
			//3. 履历搬迁
			LogMover logMover = new DefaultLogMover(logSchema, tarSchema, tempTable, null);
			logMover.move();
			response("履历搬迁完成",null);
		}catch(Exception e){
			log.error(e.getMessage(),e);
			throw new JobException("job执行过程出错，"+e.getMessage(),e);
		}
				
	}
	

}
