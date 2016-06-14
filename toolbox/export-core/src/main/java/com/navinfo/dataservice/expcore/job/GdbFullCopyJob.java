package com.navinfo.dataservice.expcore.job;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.api.datahub.iface.DatahubApi;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.thread.ThreadLocalContext;
import com.navinfo.dataservice.commons.util.RandomUtil;
import com.navinfo.dataservice.expcore.sql.ExecuteFullCopySql;
import com.navinfo.dataservice.expcore.sql.ExpSQL;
import com.navinfo.dataservice.expcore.sql.assemble.AssembleFullCopySql;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.navicommons.database.sql.DbLinkCreator;

/** 
* @ClassName: GdbFullCopyJob 
* @author Xiao Xiaowen 
* @date 2016年6月14日 下午2:54:27 
* @Description: TODO
*  
*/
public class GdbFullCopyJob extends AbstractJob {

	public GdbFullCopyJob(JobInfo jobInfo) {
		super(jobInfo);
	}


	@Override
	public void execute() throws JobException {
		String dbLinkName=null;
		try{
			GdbFullCopyJobRequest req = (GdbFullCopyJobRequest)request;
			//1.装配sqls
			DatahubApi datahub = (DatahubApi)ApplicationContextUtil.getBean("datahubApi");
			DbInfo sourceDb = datahub.getDbById(req.getSourceDbId());
			OracleSchema sourceSchema = new OracleSchema(MultiDataSourceFactory.createConnectConfig(sourceDb.getConnectParam()));
			DbInfo targetDb = datahub.getDbById(req.getTargetDbId());
			OracleSchema targetSchema = new OracleSchema(MultiDataSourceFactory.createConnectConfig(targetDb.getConnectParam()));
			//在target上创建指向source的dblink
			DbLinkCreator cr = new DbLinkCreator();
			dbLinkName = targetSchema.getConnConfig().getUserName()+"_"+RandomUtil.nextNumberStr(4);
			cr.create(dbLinkName, false, targetSchema.getPoolDataSource(), sourceSchema.getConnConfig().getUserName(), sourceSchema.getConnConfig().getUserPasswd(), sourceSchema.getConnConfig().getServerIp(), String.valueOf(sourceSchema.getConnConfig().getServerPort()), sourceSchema.getConnConfig().getDbName());
			//装配sqls
			AssembleFullCopySql assemble = new AssembleFullCopySql();
			List<ExpSQL> copySqls =assemble.assemble(dbLinkName, sourceSchema, targetSchema, req.getGdbVersion()
					, req.getSpecificTables(),req.getExcludedTables(),req.getTableReNames());
			response("复制sql列表已经装配完成",null);
			ExecuteFullCopySql copySqlExecutor = new ExecuteFullCopySql(targetSchema,req.isMultiThread4Output());
			ThreadLocalContext ctx = new ThreadLocalContext(log);
			copySqlExecutor.execute(copySqls, ctx);
			cr.drop(dbLinkName, false, targetSchema.getPoolDataSource());
			response("复制sql已经执行完毕",null);
		}catch(Exception e){
			log.error(e.getMessage(),e);
			throw new JobException("job执行过程出错："+e.getMessage(),e);
		}finally{
			if(StringUtils.isNotEmpty(dbLinkName)){
				//...删除dblink
			}
		}
	}

}
