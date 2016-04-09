package com.navinfo.dataservice.jobframework.runjob;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;

/**
 * 不需要限制最大的线程数，所以取个尽可能大的数999作为MaximumPoolSize
 * @author xiaoxiaowen4127
 *
 */
public class JobThreadPoolExecutor extends ThreadPoolExecutor implements Observer  {
	protected Logger log = LoggerRepos.getLogger(this.getClass());
	private static class SingletonHolder{
		private static final JobThreadPoolExecutor INSTANCE=new JobThreadPoolExecutor();
	}
	public static final JobThreadPoolExecutor getInstance(){
		return SingletonHolder.INSTANCE;
	}
	private static final String POOL_SIZE_KEY = "job.threadpool.size";

	private Map<String,Set<String>> jobPool;//key:jobType,value:jobIdentity set
	private JobThreadPoolExecutor(){
		super(Integer.parseInt(SystemConfigFactory.getSystemConfig().getValue(POOL_SIZE_KEY))
				,Integer.parseInt(SystemConfigFactory.getSystemConfig().getValue(POOL_SIZE_KEY))
				,3,TimeUnit.SECONDS,new LinkedBlockingQueue(),new ThreadPoolExecutor.CallerRunsPolicy());
		jobPool = new ConcurrentHashMap<String,Set<String>>();
		SystemConfigFactory.getSystemConfig().addObserver(this);
	}

	public int getCountByJobType(String jobType){
		Set<String> set = jobPool.get(jobType);
		if(set!=null){
			return set.size();
		}
		return 0;
	}
	public int getTotalCount(){
		int count=0;
		for(Set<String> set:jobPool.values()){
			count+=set.size();
		}
		return count;
	}
	public boolean isTotalFull(){
		return this.getTotalCount()>=this.getCorePoolSize();
	}
	public Map<String ,Set<String>> getJobAll(){
		return jobPool;
	}
	public boolean isJobExists(String jobIdentity){
		if(StringUtils.isEmpty(jobIdentity))return false;
		for(Set<String> set:jobPool.values()){
			if(set.contains(jobIdentity))return true;
		}
		return false;
	}
	public boolean isJobExists(String jobIdentity,String jobType){
		if(StringUtils.isEmpty(jobIdentity)||StringUtils.isEmpty(jobType)){
			return false;
		}
		Set<String> set = jobPool.get(jobType);
		if(set.contains(jobIdentity))return true;
		return false;
	}
	
	public boolean execute(JobInfo jobInfo){
		log.debug("开始将任务加入job池。jobIdentity："+jobInfo.getIdentity());
		//
		try{
			Set<String> set = jobPool.get(jobInfo.getType().toString());
			if(set!=null){
				if(set.contains(jobInfo.getIdentity())){
					log.debug("job(jobIdentity:"+jobInfo.getIdentity()+")加入执行池中失败：job已经开始执行。");
					return false;
				}else{
					set.add(jobInfo.getIdentity());
				}
			}else{
				set = new ConcurrentSkipListSet<String>();
				set.add(jobInfo.getIdentity());
				jobPool.put(jobInfo.getType().toString(), set);
			}
			AbstractJob job = JobCreateStrategy.create(jobInfo);
			super.execute(job);
			log.debug("开始执行job(jobIdentity:"+jobInfo.getIdentity()+")......");
			return true;
		}catch(Exception e){
			log.debug("执行job(jobIdentity:"+jobInfo.getIdentity()+")失败："+e.getMessage());
			log.error(e);
		}
		return false;
	}
	public void afterExecute(Runnable r, Throwable t){
		super.afterExecute(r, t);
		AbstractJob job = (AbstractJob) r;
		JobInfo jobInfo = job.getJobInfo();
		Set<String> set = jobPool.get(jobInfo.getType().toString());
		set.remove(jobInfo.getIdentity());
		
		log.debug("removed job thread. jobIdentity:"+jobInfo.getIdentity());
	}

	@Override
	public void update(Observable o, Object arg) {
		if(arg!=null){
			Set<String> changedKeys = (Set<String>)arg;
			if(changedKeys!=null){
				if(changedKeys.contains(POOL_SIZE_KEY)){
					log.info("重置线程池大小---");
					this.setCorePoolSize(Integer.parseInt(SystemConfigFactory.getSystemConfig().getValue("scheduleTaskCount")));
					this.setMaximumPoolSize(Integer.parseInt(SystemConfigFactory.getSystemConfig().getValue("scheduleTaskCount")));
				}
			}
		}
	}
	
	

}
