package com.navinfo.dataservice.cop.waistcoat.job;

import java.util.HashSet;
import java.util.Set;

import com.navinfo.dataservice.api.edit.iface.DatalockApi;
import com.navinfo.dataservice.api.edit.model.FmEditLock;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Region;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.exception.LockException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.dataservice.jobframework.runjob.JobCreateStrategy;

import net.sf.json.JSONArray;

/** 
* @ClassName: GdbBatchJob 
* @author Xiao Xiaowen 
* @date 2016年6月17日 下午6:01:43 
* @Description: TODO
*  
*/
public class GdbBatchJob extends AbstractJob {
	
	protected int lockSeq=-1;//加锁时得到值
	protected String dbType;//加锁时得到值

	public GdbBatchJob(JobInfo jobInfo) {
		super(jobInfo);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.jobframework.runjob.AbstractJob#execute()
	 */
	@Override
	public void execute() throws JobException {
		GdbBatchJobRequest req = (GdbBatchJobRequest)request;
		try{
			//1. 创建批处理库
			JobInfo createBatchDbJobInfo = new JobInfo(jobInfo.getId(),jobInfo.getGuid());
			AbstractJob createBatchDbJob = JobCreateStrategy.createAsSubJob(
					createBatchDbJobInfo,req.getCreateBatchDb(), this);
			createBatchDbJob.run();
			if(createBatchDbJob.getJobInfo().getResponse().getInt("exeStatus")!=3){
				throw new Exception("创建批处理子版本库是job执行失败。");
			}
			//2.批处理库导数据
			req.getExpBatchDb().setAttrValue("sourceDbId", req.getTargetDbId());
			int batchDbId = createBatchDbJob.getJobInfo().getResponse().getInt("outDbId");
			req.getExpBatchDb().setAttrValue("targetDbId", batchDbId);
			Set<String> meshes = new HashSet<String>();
			for(Integer g:req.getGrids()){
				int m = g/100;
				meshes.add(m<99999?"0"+String.valueOf(m):String.valueOf(m));
			}
			req.getExpBatchDb().setAttrValue("conditionParams", JSONArray.fromObject(meshes));
			JobInfo expBatchDbJobInfo = new JobInfo(jobInfo.getId(),jobInfo.getGuid());
			AbstractJob expBatchDbJob = JobCreateStrategy.createAsSubJob(
					expBatchDbJobInfo,req.getExpBatchDb(), this);
			expBatchDbJob.run();
			if(expBatchDbJob.getJobInfo().getResponse().getInt("exeStatus")!=3){
				throw new Exception("批处理子版本库导数据时job执行失败。");
			}
			//3.创建批处理子版本备份库
			req.getCreateBakDb().setAttrValue("refDbId", batchDbId);
			JobInfo createBakDbJobInfo = new JobInfo(jobInfo.getId(),jobInfo.getGuid());
			AbstractJob createBakDbJob = JobCreateStrategy.createAsSubJob(
					createBakDbJobInfo,req.getCreateBakDb(), this);
			createBakDbJob.run();
			if(createBakDbJob.getJobInfo().getResponse().getInt("exeStatus")!=3){
				throw new Exception("创建备份子版本库时job执行失败。");
			}
			//4.给批处理备份子版本复制数据
			req.getCopyBakDb().setAttrValue("sourceDbId", batchDbId);
			int bakDbId = createBakDbJob.getJobInfo().getResponse().getInt("outDbId");
			req.getCopyBakDb().setAttrValue("targetDbId", bakDbId);
			JobInfo copyBakDbJobInfo = new JobInfo(jobInfo.getId(),jobInfo.getGuid());
			AbstractJob copyBakDbJob = JobCreateStrategy.createAsSubJob(
					copyBakDbJobInfo, req.getCopyBakDb(),this);
			copyBakDbJob.run();
			if(copyBakDbJob.getJobInfo().getResponse().getInt("exeStatus")!=3){
				throw new Exception("批处理备份子版本库复制数据时job执行失败。");
			}
		}catch(Exception e){
			log.error(e.getMessage(),e);
			throw new JobException(e.getMessage(),e);
		}
		
	}
	
	@Override
	public void lockResources()throws LockException{
		GdbBatchJobRequest req = (GdbBatchJobRequest)request;
		//预处理
		//根据批处理的目标库找到对应的大区
		try{
			ManApi man = (ManApi)ApplicationContextUtil.getBean("manApi");
			Region r = man.queryRegionByDbId(req.getTargetDbId());
			if(r==null){
				throw new Exception("根据batchDbId未查询到匹配的区域");
			}
			String dbType = null;
			if(r.getDailyDbId()==req.getTargetDbId()){
				dbType = FmEditLock.DB_TYPE_DAY;
			}else if(r.getMonthlyDbId()==req.getTargetDbId()){
				dbType = FmEditLock.DB_TYPE_MONTH;
			}
			DatalockApi datalock = (DatalockApi) ApplicationContextUtil.getBean("datalockApi");
			lockSeq = datalock.lockGrid(r.getRegionId(), FmEditLock.LOCK_OBJ_ALL, req.getGrids(), FmEditLock.TYPE_BATCH, dbType);
		}catch(Exception e){
			log.error(e.getMessage(),e);
			throw new LockException("加锁发生错误,"+e.getMessage(),e);
		}
	}
	
	@Override
	public void unlockResources()throws LockException{
		if(lockSeq<0)return;
		try{
			DatalockApi datalock = (DatalockApi) ApplicationContextUtil.getBean("datalockApi");
			datalock.unlockGrid(lockSeq, dbType);
		}catch(Exception e){
			log.error(e.getMessage(),e);
			throw new LockException("加锁发生错误,"+e.getMessage(),e);
		}
	}

}
