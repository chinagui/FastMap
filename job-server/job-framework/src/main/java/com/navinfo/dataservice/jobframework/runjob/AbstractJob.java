package com.navinfo.dataservice.jobframework.runjob;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.job.model.JobStep;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.mq.job.JobMsgPublisher;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.sample.SamplebJobRequest;

import java.io.IOException;
import java.util.HashMap;
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
	
	public AbstractJob(JobInfo jobInfo){
		this.jobInfo=jobInfo;
	}
	
	@Override
	public void run() {
		try{
			jobInfo.setResponse(new JSONObject());
			volidateRequest();
			initLogger();
			jobInfo.setStatus(2);
			response("检查、初始化任务执行环境及相关操作已完成...",jobInfo.getStatus());
			execute();
			jobInfo.setStatus(3);
		}catch(Exception e){
			jobInfo.setStatus(4);
			exception = e;
			log.error(e.getMessage(),e);
		}finally{
			try{
				response("job执行完成。",jobInfo.getStatus());
			}catch(Exception err){
				log.error(err.getMessage(),err);
				log.warn("注意：job执行完成后修改任务状态出错。");
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
		log.info("开始验证request参数...");
		try{
			request.validate();
			log.info("验证request参数完成。");
		}catch(Exception e){
			throw new JobException(e.getMessage(),e);
		}
	};
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
				jobInfo.getResponse().put(key, data.get(key));
			}
		}
		if(runAsMethod)return;//如果作为方法执行，不需要反馈
		try{
			//step如果有parent需要添加到parent
			if(parent==null){
				JobStep step = jobInfo.addStep(stepMsg);
				JobMsgPublisher.responseJob(jobInfo.getId(),jobInfo.getStatus(),jobInfo.getStepCount(), jobInfo.getResponse(),step);
			}else{
				JobStep step = parent.jobInfo.addStep("[from sub job(type:"+jobInfo.getType().toString()+")]"+stepMsg);
				JobMsgPublisher.responseJob(jobInfo.getId(),parent.jobInfo.getStatus(),parent.jobInfo.getStepCount(), parent.jobInfo.getResponse(),step);
			}
		}catch(Exception e){
			log.error(e.getMessage(),e);
			throw new JobException("");
			
		}
	}
	private void response(String stepMsg,int status)throws JobException{
		Map<String,Object> data = new HashMap<String,Object>();
		data.put("exeStatus", String.valueOf(status));
		response(stepMsg,data);
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
}
