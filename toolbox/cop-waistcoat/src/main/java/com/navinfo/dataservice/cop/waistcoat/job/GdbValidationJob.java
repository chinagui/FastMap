package com.navinfo.dataservice.cop.waistcoat.job;

import java.util.HashSet;
import java.util.Set;

import com.navinfo.dataservice.api.datahub.iface.DatahubApi;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.bizcommons.datarow.CkResultTool;
import com.navinfo.dataservice.bizcommons.datarow.PhysicalDeleteRow;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.dataservice.jobframework.runjob.JobCreateStrategy;

import net.sf.json.JSONArray;

/** 
* @ClassName: GdbValidationJob 
* @author Xiao Xiaowen 
* @date 2016年6月21日 下午3:52:09 
* @Description: TODO
*  
*/
public class GdbValidationJob extends AbstractJob {

	public GdbValidationJob(JobInfo jobInfo) {
		super(jobInfo);
	}

	@Override
	public void execute() throws JobException {
		GdbValidationJobRequest req = (GdbValidationJobRequest)request;
		//1. 创建检查子版本
		try {
			// 1. 创建检查子版本库库
			int valDbId = 0;
			OracleSchema valSchema = null;
			DatahubApi datahub = (DatahubApi)ApplicationContextUtil.getBean("datahubApi");
			if(req.getValDbId()>0){
				valDbId = req.getValDbId();
				jobInfo.getResponse().put("valDbId", valDbId);
				DbInfo valDb = datahub.getDbById(valDbId);
				valSchema = new OracleSchema(
						DbConnectConfig.createConnectConfig(valDb.getConnectParam()));
			}else if(req.getSubJobRequest("createValDb")!=null&&req.getSubJobRequest("expValDb")!=null){
				JobInfo createValDbJobInfo = new JobInfo(jobInfo.getId(), jobInfo.getGuid());
				AbstractJob createValDbJob = JobCreateStrategy.createAsSubJob(createValDbJobInfo,
						req.getSubJobRequest("createValDb"), this);
				createValDbJob.run();
				if (createValDbJob.getJobInfo().getResponse().getInt("exeStatus") != 3) {
					throw new Exception("创建检查子版本库是job执行失败。");
				}
				valDbId = createValDbJob.getJobInfo().getResponse().getInt("outDbId");
				jobInfo.getResponse().put("valDbId", valDbId);
				// 2.给检查子版本库导数据
				req.getSubJobRequest("expValDb").setAttrValue("sourceDbId", req.getTargetDbId());
				req.getSubJobRequest("expValDb").setAttrValue("targetDbId", valDbId);
				Set<String> meshes = new HashSet<String>();
				for (Integer g : req.getGrids()) {
					int m = g / 100;
					meshes.add(m < 99999 ? "0" + String.valueOf(m) : String.valueOf(m));
				}
				req.getSubJobRequest("expValDb").setAttrValue("conditionParams", JSONArray.fromObject(meshes));
				JobInfo expValDbJobInfo = new JobInfo(jobInfo.getId(), jobInfo.getGuid());
				AbstractJob expValDbJob = JobCreateStrategy.createAsSubJob(expValDbJobInfo, req.getSubJobRequest("expValDb"), this);
				expValDbJob.run();
				if (expValDbJob.getJobInfo().getResponse().getInt("exeStatus") != 3) {
					throw new Exception("批处理子版本库导数据时job执行失败。");
				}

				//cop 子版本物理删除逻辑删除数据
				DbInfo valDb = datahub.getDbById(valDbId);
				valSchema = new OracleSchema(
						DbConnectConfig.createConnectConfig(valDb.getConnectParam()));
				PhysicalDeleteRow.doDelete(valSchema);
			}
			// 2. 在检查子版本上执行检查
			req.getSubJobRequest("val").setAttrValue("executeDBId", valDbId);
			req.getSubJobRequest("val").setAttrValue("ruleIds", req.getRules());
			DbInfo metaDb = datahub.getOnlyDbByType("metaRoad");
			req.getSubJobRequest("val").setAttrValue("kdbDBId", metaDb.getDbId());
			req.getSubJobRequest("val").setAttrValue("timeOut", req.getTimeOut());
			JobInfo valJobInfo = new JobInfo(jobInfo.getId(),jobInfo.getGuid());
			AbstractJob valJob = JobCreateStrategy.createAsSubJob(valJobInfo, req.getSubJobRequest("val"), this);
			valJob.run();
			if(valJob.getJobInfo().getResponse().getInt("exeStatus")!=3){
				throw new Exception("检查job内部执行失败。");
			}
			// 3. 检查结果搬迁
			CkResultTool.generateCkMd5(valSchema);
			CkResultTool.generateCkResultObject(valSchema);
			CkResultTool.generateCkResultGrid(valSchema);
			DbInfo tarDb = datahub.getDbById(req.getTargetDbId());
			OracleSchema tarSchema = new OracleSchema(
					DbConnectConfig.createConnectConfig(tarDb.getConnectParam()));
			CkResultTool.moveNiVal(valSchema, tarSchema, req.getGrids());
			response("检查生成的检查结果后处理及搬迁完毕。",null);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new JobException(e.getMessage(), e);
		}
	}

}
