package com.navinfo.dataservice.engine.statics.launcher;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** 
 * @ClassName: GroupStatJob
 * @author xiaoxiaowen4127
 * @date 2017年5月18日
 * @Description: GroupStatJob.java
 */
public class GroupStatJob {
	
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
	
	public void trigger(String timestamp,String jobType){
		if(!subJobs.contains(jobType)){
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
			//触发starter启动
			if(types.size()==subJobs.size()&&types.containsAll(subJobs)){
				//
				
			}
		}
	}
	
}