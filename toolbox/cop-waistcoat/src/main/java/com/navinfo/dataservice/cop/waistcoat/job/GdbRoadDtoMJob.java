package com.navinfo.dataservice.cop.waistcoat.job;

import com.navinfo.dataservice.api.datahub.iface.DatahubApi;
import com.navinfo.dataservice.api.datahub.model.BizType;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.edit.iface.DatalockApi;
import com.navinfo.dataservice.api.edit.model.FmEditLock;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.bizcommons.datarow.CkResultTool;
import com.navinfo.dataservice.bizcommons.datarow.PhysicalDeleteRow;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.exception.LockException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.dataservice.jobframework.runjob.JobCreateStrategy;
import net.sf.json.JSONArray;

import java.util.HashSet;
import java.util.Set;

/**
 * @ClassName: GdbRoadDtoMJob
 * @author Zhang Runze
 * @date 2016年6月17日 下午6:01:43
 * @Description: TODO
 */
public class GdbRoadDtoMJob extends AbstractJob {

	protected FmEditLock editLock=null;
	public GdbRoadDtoMJob(JobInfo jobInfo) {
		super(jobInfo);
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.navinfo.dataservice.jobframework.runjob.AbstractJob#execute()
	 */
	@Override
	public void execute() throws JobException {
		GdbRoadDtoMJobRequest req = (GdbRoadDtoMJobRequest) request;

		try {
			DatahubApi datahub = (DatahubApi)ApplicationContextUtil.getBean("datahubApi");
			//1.1 创建日库参考库的子版本，清空冗余数据
			DbInfo dayDb = null;
			int dayDbId = 0;
			OracleSchema daySchema = null;
			//先找是否利用可重复使用的库,重用的库是空库，需要导数据
			if(req.isReuseDb()){
				dayDb = datahub.getReuseDb(BizType.DB_COP_VERSION);
			}//未设置利用重用的库，或者未找到可重用的库，需要新建库
			if(dayDb!=null){
				dayDbId=dayDb.getDbId();
				jobInfo.addResponse("dayDbId", dayDbId);
			}else{
				if(req.getSubJobRequest("createDayDb")!=null){
					JobInfo createDayDbJobInfo = new JobInfo(jobInfo.getId(), jobInfo.getGuid());
					AbstractJob createDayDbJob = JobCreateStrategy.createAsSubJob(createDayDbJobInfo,
							req.getSubJobRequest("createDayDb"), this);
					createDayDbJob.run();
					if (createDayDbJob.getJobInfo().getStatus()!= 3) {
						String msg = (createDayDbJob.getException()==null)?"未知错误。":"错误："+createDayDbJob.getException().getMessage();

						throw new Exception("创建参考日子版本库job内部发生"+msg);
					}
					//把子job返回的response挑选自己需要的写入response
					dayDbId = createDayDbJob.getJobInfo().getResponse().getInt("outDbId");
					jobInfo.addResponse("dayDbId", dayDbId);
					dayDb = datahub.getDbById(dayDbId);
				}else{
					throw new Exception("未设置创建参考日子版本库request参数。");
				}
			}
			daySchema = new OracleSchema(
					DbConnectConfig.createConnectConfig(dayDb.getConnectParam()));

			// 1.2  库导数据
			if(req.getSubJobRequest("expDayDb")!=null){
				req.getSubJobRequest("expDayDb").setAttrValue("sourceDbId", req.getReferenceDbId());
				req.getSubJobRequest("expDayDb").setAttrValue("targetDbId", dayDbId);
				req.getSubJobRequest("expDayDb").setAttrValue("meshExtendCount", req.getExtendCount());
				Set<String> meshes = new HashSet<String>();
				for (Integer g : req.getGrids()) {
					int m = g / 100;
					meshes.add(m < 99999 ? "0" + String.valueOf(m) : String.valueOf(m));
				}
				req.getSubJobRequest("expDayDb").setAttrValue("conditionParams", JSONArray.fromObject(meshes));
				JobInfo expDayDbJobInfo = new JobInfo(jobInfo.getId(), jobInfo.getGuid());
				AbstractJob expDayDbJob = JobCreateStrategy.createAsSubJob(expDayDbJobInfo, req.getSubJobRequest("expDayDb"), this);
				expDayDbJob.run();
				if (expDayDbJob.getJobInfo().getStatus()!= 3) {
					String msg = (expDayDbJob.getException()==null)?"未知错误。":"错误："+expDayDbJob.getException().getMessage();

					throw new Exception("日参考子版本库导数据job内部发生"+msg);
				}
				//do nothing,不需要导出子job的输出参数
				//cop 子版本物理删除逻辑删除数据
				PhysicalDeleteRow.doDelete(daySchema);
			}else{
				throw new Exception("未设置给日参考子版本库导数据的request参数。");
			}

			// 2.1 创建批处理库
			int batchDbId = 0;
			OracleSchema batSchema = null;
			if(req.getBatchDbId()!=0){
				batchDbId = req.getBatchDbId();
				DbInfo batDb = datahub.getDbById(batchDbId);
				batSchema = new OracleSchema(
						DbConnectConfig.createConnectConfig(batDb.getConnectParam()));
			}else{
				//先找是否利用可重复使用的库,重用的库是空库，需要导数据
				DbInfo batDb = null;
				if(req.isReuseDb()){
					batDb = datahub.getReuseDb(BizType.DB_COP_VERSION);
				}
				//未设置利用重用的库，或者未找到可重用的库，需要新建库
				if(batDb!=null){
					batchDbId=batDb.getDbId();
					jobInfo.addResponse("batDbId", batchDbId);
				}else{
					if(req.getSubJobRequest("createBatchDb")!=null){
						JobInfo createBatchDbJobInfo = new JobInfo(jobInfo.getId(), jobInfo.getGuid());
						AbstractJob createBatchDbJob = JobCreateStrategy.createAsSubJob(createBatchDbJobInfo,
								req.getSubJobRequest("createBatchDb"), this);
						createBatchDbJob.run();
						if (createBatchDbJob.getJobInfo().getStatus()!= 3) {
							String msg = (createBatchDbJob.getException()==null)?"未知错误。":"错误："+createBatchDbJob.getException().getMessage();

							throw new Exception("创建批处理子版本库job内部发生"+msg);
						}
						//把子job返回的response挑选自己需要的写入response
						batchDbId = createBatchDbJob.getJobInfo().getResponse().getInt("outDbId");
						jobInfo.addResponse("batDbId", batchDbId);
						batDb = datahub.getDbById(batchDbId);
					}else{
						throw new Exception("未设置创建批处理子版本库request参数。");
					}
				}
				batSchema = new OracleSchema(
						DbConnectConfig.createConnectConfig(batDb.getConnectParam()));
				// 2.2 批处理库导数据
				if(req.getSubJobRequest("expBatchDb")!=null){
					req.getSubJobRequest("expBatchDb").setAttrValue("sourceDbId", req.getTargetDbId());
					req.getSubJobRequest("expBatchDb").setAttrValue("targetDbId", batchDbId);
					req.getSubJobRequest("expBatchDb").setAttrValue("meshExtendCount", req.getExtendCount());
					Set<String> meshes = new HashSet<String>();
					for (Integer g : req.getGrids()) {
						int m = g / 100;
						meshes.add(m < 99999 ? "0" + String.valueOf(m) : String.valueOf(m));
					}
					req.getSubJobRequest("expBatchDb").setAttrValue("conditionParams", JSONArray.fromObject(meshes));
					JobInfo expBatchDbJobInfo = new JobInfo(jobInfo.getId(), jobInfo.getGuid());
					AbstractJob expBatchDbJob = JobCreateStrategy.createAsSubJob(expBatchDbJobInfo, req.getSubJobRequest("expBatchDb"), this);
					expBatchDbJob.run();
					if (expBatchDbJob.getJobInfo().getStatus()!= 3) {
						String msg = (expBatchDbJob.getException()==null)?"未知错误。":"错误："+expBatchDbJob.getException().getMessage();

						throw new Exception("批处理子版本库导数据job内部发生"+msg);
					}
					//do nothing,不需要导出子job的输出参数
					//cop 子版本物理删除逻辑删除数据
					PhysicalDeleteRow.doDelete(batSchema);
				}else{
					throw new Exception("未设置给批处理子版本库导数据的request参数。");
				}
			}
			// 3.创建批处理子版本备份库
			int bakDbId=0;
			//先找是否利用可重复使用的库,重用的库是空库，需要导数据
			if(req.isReuseDb()){
				DbInfo bakDbInfo = datahub.getReuseDb(BizType.DB_COP_VERSION,batchDbId);
				if(bakDbInfo!=null)bakDbId=bakDbInfo.getDbId();
			}
			if(bakDbId==0){
				if(req.getSubJobRequest("createBakDb")!=null){
					req.getSubJobRequest("createBakDb").setAttrValue("refDbId", batchDbId);
					JobInfo createBakDbJobInfo = new JobInfo(jobInfo.getId(), jobInfo.getGuid());
					AbstractJob createBakDbJob = JobCreateStrategy.createAsSubJob(createBakDbJobInfo, req.getSubJobRequest("createBakDb"),
							this);
					createBakDbJob.run();
					if (createBakDbJob.getJobInfo().getStatus()!= 3) {
						String msg = (createBakDbJob.getException()==null)?"未知错误。":"错误："+createBakDbJob.getException().getMessage();
						throw new Exception("创建备份子版本库job内部发生"+msg);
					}
					bakDbId = createBakDbJob.getJobInfo().getResponse().getInt("outDbId");
					jobInfo.addResponse("bakDbId", bakDbId);
				}else{
					throw new Exception("未设置创建批处理备份子版本库的request参数。");
				}
			}
			// 4.给批处理备份子版本复制数据
			req.getSubJobRequest("copyBakDb").setAttrValue("sourceDbId", batchDbId);
			req.getSubJobRequest("copyBakDb").setAttrValue("targetDbId", bakDbId);
			JobInfo copyBakDbJobInfo = new JobInfo(jobInfo.getId(), jobInfo.getGuid());
			AbstractJob copyBakDbJob = JobCreateStrategy.createAsSubJob(copyBakDbJobInfo, req.getSubJobRequest("copyBakDb"), this);
			copyBakDbJob.run();
			if (copyBakDbJob.getJobInfo().getStatus()!= 3) {
				String msg = (copyBakDbJob.getException()==null)?"未知错误。":"错误："+copyBakDbJob.getException().getMessage();
				throw new Exception("备份子版本库复制数据时job内部发生"+msg);
			}
			// 5. 在月库子版本上执行批处理
			req.getSubJobRequest("dtoM").setAttrValue("executeDBId", batchDbId);
			req.getSubJobRequest("dtoM").setAttrValue("dayDBId", dayDbId);

//			req.getSubJobRequest("batch").setAttrValue("pidDbInfo",SystemConfigFactory.getSystemConfig().getValue("dms.pid.dbInfo"));
//			req.getSubJobRequest("batch").setAttrValue("pidDbInfo", req.getPidDbInfo());
//			req.getSubJobRequest("dtoM").setAttrValue("ruleIds", req.getRules());
			JobInfo dtoMJobInfo = new JobInfo(jobInfo.getId(),jobInfo.getGuid());
			AbstractJob dtoMJob = JobCreateStrategy.createAsSubJob(dtoMJobInfo, req.getSubJobRequest("dtoM"),this);
			dtoMJob.run();
			if(dtoMJob.getJobInfo().getStatus()!=3){
				String msg = (dtoMJob.getException()==null)?"未知错误。":"错误：" + dtoMJob.getException().getMessage();
				throw new Exception("道路日落月过程中job内部发生"+msg);
			}

			// 6. 执行差分
			req.getSubJobRequest("diff").setAttrValue("leftDbId", batchDbId);
			req.getSubJobRequest("diff").setAttrValue("rightDbId", bakDbId);
			JobInfo diffJobInfo = new JobInfo(jobInfo.getId(), jobInfo.getGuid());
			diffJobInfo.setTaskId(jobInfo.getTaskId());
			AbstractJob diffJob = JobCreateStrategy.createAsSubJob(diffJobInfo, req.getSubJobRequest("diff"), this);
			diffJob.run();
			if (diffJob.getJobInfo().getStatus() != 3) {
				String msg = (diffJob.getException()==null)?"未知错误。":"错误："+diffJob.getException().getMessage();
				throw new Exception("差分时job内部发生"+msg);
			}
			/*
			//7. 差分履历大区库
			req.getSubJobRequest("commit").setAttrValue("logDbId", batchDbId);
			req.getSubJobRequest("commit").setAttrValue("targetDbId", req.getTargetDbId());
			req.getSubJobRequest("commit").setAttrValue("grids", JSONArray.fromObject(req.getGrids()));
			JobInfo commitJobInfo = new JobInfo(jobInfo.getId(), jobInfo.getGuid());
			AbstractJob commitJob = JobCreateStrategy.createAsSubJob(commitJobInfo, req.getSubJobRequest("commit"), this);
			commitJob.run();
			if (commitJob.getJobInfo().getStatus() != 3) {
				String msg = (commitJob.getException()==null)?"未知错误。":"错误："+commitJob.getException().getMessage();
				throw new Exception("回区域库时job内部发生"+msg);
			}
			//8. 检查结果搬迁
			CkResultTool.generateCkMd5(batSchema);
			CkResultTool.generateCkResultObject(batSchema);
			CkResultTool.generateCkResultGrid(batSchema);
			DbInfo tarDb = datahub.getDbById(req.getTargetDbId());
			OracleSchema tarSchema = new OracleSchema(
					DbConnectConfig.createConnectConfig(tarDb.getConnectParam()));
			CkResultTool.moveNiVal(batSchema, tarSchema, req.getGrids());
			response("批处理生成的检查结果搬迁完毕。",null);*/
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new JobException(e.getMessage(), e);
		}

	}

	@Override
	public void lockResources() throws LockException {
		GdbRoadDtoMJobRequest req = (GdbRoadDtoMJobRequest) request;
		// 根据批处理的目标库找到对应的大区
		try {
			//DatalockApi datalock = (DatalockApi) ApplicationContextUtil.getBean("datalockApi");
			//editLock = datalock.lockGrid(req.getTargetDbId(), FmEditLock.LOCK_OBJ_ALL, req.getGrids(), FmEditLock.TYPE_BATCH, jobInfo.getId());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new LockException("加锁发生错误," + e.getMessage(), e);
		}
	}

	@Override
	public void unlockResources() throws LockException {
		if (editLock==null)
			return;
		try {
			//DatalockApi datalock = (DatalockApi) ApplicationContextUtil.getBean("datalockApi");
			//datalock.unlockGrid(editLock.getLockSeq(), editLock.getDbType());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new LockException("解锁时发生错误," + e.getMessage(), e);
		}
	}
	
}
