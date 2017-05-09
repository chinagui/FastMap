package com.navinfo.dataservice.job.datahub;

import java.util.HashMap;
import java.util.Map;

import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.commons.database.DbServerType;
import com.navinfo.dataservice.datahub.exception.DataHubException;
import com.navinfo.dataservice.datahub.service.DbService;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;

/** 
* @ClassName: CreateDbJob 
* @author Xiao Xiaowen 
* @date 2016年6月12日 下午3:29:07 
* @Description: TODO
*  
*/
public class CreateDbJob extends AbstractJob {

	public CreateDbJob(JobInfo jobInfo) {
		super(jobInfo);
	}

	@Override
	public void execute() throws JobException {
		// TODO Auto-generated method stub
		CreateDbJobRequest req = (CreateDbJobRequest)request;
		DbInfo db = null;
		try{
			if(DbServerType.TYPE_ORACLE.equals(req.getServerType())){
				db = DbService.getInstance().createOracleDb(req.getUserName(),req.getUserPasswd(),req.getBizType(),req.getDescp()
						,req.getGdbVersion(),req.getRefDbId(),req.getSpecSvrId());
			}else{
				db = DbService.getInstance().createMongoDb(req.getDbName(),req.getBizType(),req.getDescp()
						,req.getGdbVersion(),req.getRefDbId());
			}
			Map<String,Object> data = new HashMap<String,Object>();
			data.put("outDbId", db.getDbId());
			log.info("Created Db:"+db);
			super.response("创建库完成",data);
		}catch(DataHubException e){
			log.error(e.getMessage(),e);
			throw new JobException(e.getMessage(),e);
		}
	}

}
