package com.navinfo.dataservice.expcore.job;

import com.navinfo.dataservice.api.datahub.iface.DatahubApi;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.thread.ThreadLocalContext;
import com.navinfo.dataservice.expcore.ExportConfig;
import com.navinfo.dataservice.expcore.input.OracleInput;
import com.navinfo.dataservice.expcore.output.Oracle2OracleDataOutput;
import com.navinfo.dataservice.expcore.sql.ExecuteSql;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;

/** 
* @ClassName: GdbExportJob 
* @author Xiao Xiaowen 
* @date 2016年6月14日 上午9:48:03 
* @Description: TODO
*  
*/
public class GdbExportJob extends AbstractJob {

	public GdbExportJob(JobInfo jobInfo) {
		super(jobInfo);
	}

	@Override
	public void execute() throws JobException {
		try{
			GdbExportJobRequest req = (GdbExportJobRequest)request;
			//1. 导出源预处理
			DatahubApi datahub = (DatahubApi)ApplicationContextUtil.getBean("datahubApi");
			DbInfo sourceDb = datahub.getDbById(req.getSourceDbId());
			OracleSchema sourceSchema = new OracleSchema(DbConnectConfig.createConnectConfig(sourceDb.getConnectParam()));
			OracleInput input = new OracleInput(sourceSchema,req.getFeatureType()
					,req.getCondition(),req.getConditionParams(),req.getGdbVersion());
			input.initSource();
			input.serializeParameters();
			input.loadScripts();
			response("导出源预处理完成",null);
			//2.导出目标预处理
			DbInfo targetDb = datahub.getDbById(req.getTargetDbId());
			OracleSchema targetSchema = new OracleSchema(DbConnectConfig.createConnectConfig(targetDb.getConnectParam()));
			ThreadLocalContext ctx = new ThreadLocalContext(log);
			Oracle2OracleDataOutput output = new Oracle2OracleDataOutput(targetSchema,req.getCheckExistTables(),req.getWhenExist(),req.getTableReNames(),ctx);
			response("导出目标预处理完成",null);
			//3.执行导出脚本
			ExecuteSql exportSqlExecutor = new ExecuteSql(input,output,ExportConfig.MODE_COPY,req.isDataIntegrity(),req.isMultiThread4Input(),req.isMultiThread4Output());
			exportSqlExecutor.execute();
			response("导出脚本执行完成",null);
		}catch(Exception e){
			log.error(e.getMessage(),e);
			throw new JobException("job执行过程出错："+e.getMessage(),e);
		}
	}

}
