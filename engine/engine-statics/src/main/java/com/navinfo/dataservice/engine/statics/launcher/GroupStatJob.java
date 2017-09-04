package com.navinfo.dataservice.engine.statics.launcher;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;

/** 
 * @ClassName: GroupStatJob
 * @author xiaoxiaowen4127
 * @date 2017年5月18日
 * @Description: GroupStatJob.java
 */
public class GroupStatJob {
	private Logger log = LoggerRepos.getLogger(this.getClass());
	
	private String groupJobType;
	private String groupJobStarter;
	private Set<String> subJobs;//key:jobType,
	private Map<String,Set<String>> statFeedbacks = new HashMap<String,Set<String>>();//key:timestamp,value:job types
	
	public String getGroupJobType() {
		return groupJobType;
	}

	public void setGroupJobType(String groupJobType) {
		this.groupJobType = groupJobType;
	}

	public String getGroupJobStarter() {
		return groupJobStarter;
	}

	public void setGroupJobStarter(String groupJobStarter) {
		this.groupJobStarter = groupJobStarter;
	}

	public Set<String> getSubJobs() {
		return subJobs;
	}

	public void setSubJobs(Set<String> subJobs) {
		this.subJobs = subJobs;
	}
	
	public boolean typeEquals(Collection<String> types){
		if(types!=null&&types.size()>0&&types.size()==subJobs.size()&&types.containsAll(subJobs)){
			return true;
		}
		return false;
	}
	
	public void trigger(String timestamp,String jobType)throws Exception{
		//log.info("2");
		if(!subJobs.contains(jobType)){
			//log.info("3");
			return;
		}
		synchronized(this){
			//统计结果加入
			Set<String> types = statFeedbacks.get(timestamp);
			if(types==null){
				types = new HashSet<String>();
				statFeedbacks.put(timestamp, types);
			}
			types.add(jobType);
			log.info("timestamp:"+timestamp+",groupJobType:"+groupJobType+",ready:"+types.toString()+",all:"+subJobs.toString());
			//触发starter启动
			if(types.size()==subJobs.size()&&types.containsAll(subJobs)){
				//log.info("4");
				StatJobStarter starter =  (StatJobStarter)Class.forName(groupJobStarter).getConstructor().newInstance();
				starter.start(timestamp);
				//remove started timestamp
				statFeedbacks.remove(timestamp);
			}
		}
	}
	
}
