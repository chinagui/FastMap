package com.navinfo.dataservice.jobframework.runjob;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.edit.model.FmEditLock;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.job.model.JobStatus;
import com.navinfo.dataservice.api.job.model.JobStep;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.mq.job.JobMsgPublisher;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.exception.LockException;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;


/** 
* @ClassName: AbstractJob 
* @author Xiao Xiaowen 
* @date 2016年3月25日 下午4:12:36 
* @Description: 
* 
*/
public abstract class AbstractJob implements Runnable {
	
	protected Logger log = LoggerRepos.getLogger(this.getClass());
	protected JobInfo jobInfo;
	
	protected boolean runAsMethod = false;//供脚本等不需要作为独立job线程执行的情形使用
	//protected boolean rerunnable=false;
	
	protected AbstractJob parent=null;
	
	protected AbstractJobRequest request;
	
	protected Exception exception;
	
	protected List<Integer> lockDbIds;
	protected List<FmEditLock> editLocks;

	public AbstractJob(JobInfo jobInfo){
		this.jobInfo=jobInfo;
	}
	
	@Override
	public void run() {
		try{
			jobInfo.setBeginTime(new Date());
			initLogger();
			jobInfo.setResponse(new JSONObject());
			volidateRequest();
			lock();
			response("检查、初始化任务执行环境及相关操作已完成...",null);
			execute();
			endJob(JobStatus.STATUS_SUCCESS,"job执行成功");
		}catch(Exception e){
			exception = e;
			log.error(e.getMessage(),e);
			endJob(JobStatus.STATUS_FAILURE,StringUtils.cutSpecLength(e.getMessage(), 1000));
		}finally{
			try{
				unlock();
			}catch(LockException le){
				log.error(le.getMessage(),le);
				log.warn("注意：job执行完成后解锁失败。");
			}
			log.info("job执行完成。status="+jobInfo.getStatus());
		}
	}
	/**
	 * 初始化每个任务一个日志文件的日志系统
	 * 
	 * @throws IOException
	 */
	private void initLogger() throws IOException {
		if(runAsMethod) return;//如果作为方法执行，不需要初始化独立日志
		if(parent==null){
			log.debug("初始化job日志,将日志对象Logger 放入ThreadLocal对象中：" + jobInfo.getId());
			log = LoggerRepos.createLogger(jobInfo.getIdentity());
		}else{
			log.debug("当前job作为子job执行，不单独生成日志");
		}
	}
	
	public void volidateRequest()throws JobException{
		log.debug("开始验证request参数:"+JSONObject.fromObject(request).toString());
		try{
			request.validate();
			log.info("验证request参数完成。");
		}catch(Exception e){
			throw new JobException(e.getMessage(),e);
		}
	}
	public abstract void execute()throws JobException;
	
	/**
	 * 第一次反馈消息，接收消息方会设置job状态为执行中，并写入总步骤数
	 * @param stepMsg
	 * @throws JobException
	 */
	public void response(String stepMsg,Map<String,?> data)throws JobException{
		log.debug("resp:"+stepMsg+","+JSONObject.fromObject(data).toString());
		//data添加到jobInfo
		if(data!=null){
			for(String key:data.keySet()){
				jobInfo.addResponse(key, data.get(key));
			}
		}
		//发送消息
		if(runAsMethod)return;//如果作为方法执行，不需要反馈
		try{
			//step如果有parent需要添加到parent
			if(parent==null){
				JobStep step = jobInfo.addStep(stepMsg);
				JobMsgPublisher.responseJob(jobInfo.getId(),step);
			}else{
				JobStep step = parent.jobInfo.addStep("[from sub job(type:"+jobInfo.getType().toString()+")]"+stepMsg);
				JobMsgPublisher.responseJob(parent.jobInfo.getId(),step);
			}
		}catch(Exception e){
			log.error(e.getMessage(),e);
			throw new JobException("");
			
		}
	}
	private void endJob(int status,String resultMsg){
		jobInfo.endJob(status, resultMsg);
		//发送消息
		if(runAsMethod)return;//如果作为方法执行，不需要反馈
		if(parent==null){//独立job
			try{
				log.debug("job主体执行完成，发送end_job消息(status:"+status+",resultMsg:"+resultMsg+")");
				long durationSeconds = (jobInfo.getEndTime().getTime()-jobInfo.getBeginTime().getTime())/60000;
				JobMsgPublisher.endJob(jobInfo.getUserId(),jobInfo.getId(), status,resultMsg,jobInfo.getResponse());
			}catch(Exception e){
				log.warn("******注意：job执行主体已经完毕，发送end_job消息过程出现错误。该错误已忽略，需手工对应。******");
				log.error(e.getMessage(),e);
			}
		}else{//作为子job
			log.debug("子job主体执行完成，不发送end_job消息(status:"+status+",resultMsg:"+resultMsg+")");
		}
	}
	private void lock()throws LockException{
		if(parent==null){
			lockResources();
		}
	}
	private void unlock()throws LockException{
		if(parent==null){
			unlockResources();
		}
	}
	
	/**
	 * 有加锁逻辑的job需要重写此方法
	 * @throws LockException
	 */
	public void lockResources()throws LockException{
		
	}
	/**
	 * 有解锁逻辑的job需要重写此方法
	 * @throws LockException
	 */
	public void unlockResources()throws LockException{
		
	}
	

	public JobInfo getJobInfo() {
		return jobInfo;
	}
	public void setJobInfo(JobInfo jobInfo) {
		this.jobInfo = jobInfo;
	}
	public boolean isRunAsMethod() {
		return runAsMethod;
	}
	public void setRunAsMethod(boolean runAsMethod) {
		this.runAsMethod = runAsMethod;
	}
	public AbstractJob getParent(){
		return parent;
	}
	public void setParent(AbstractJob parent){
		this.parent=parent;
	}
	public AbstractJobRequest getRequest() {
		return request;
	}
	public void setRequest(AbstractJobRequest request) {
		this.request = request;
	}
	public Exception getException() {
		return exception;
	}

	public List<Integer> getLockDbIds() {
		return lockDbIds;
	}

	public void setLockDbIds(List<Integer> lockDbIds) {
		this.lockDbIds = lockDbIds;
	}

	public List<FmEditLock> getEditLocks() {
		return editLocks;
	}

	public void setEditLocks(List<FmEditLock> editLocks) {
		this.editLocks = editLocks;
	}

}
