package com.navinfo.dataservice.engine.statics.launcher;

import java.util.Set;

/** 
 * @ClassName: GroupStatJob
 * @author xiaoxiaowen4127
 * @date 2017年5月18日
 * @Description: GroupStatJob.java
 */
public class GroupStatJob {
	
	private StatJob groupJob;
	private Set<StatJob> subJobs;
	public StatJob getGroupJob() {
		return groupJob;
	}
	public void setGroupJob(StatJob groupJob) {
		this.groupJob = groupJob;
	}
	public Set<StatJob> getSubJobs() {
		return subJobs;
	}
	public void setSubJobs(Set<StatJob> subJobs) {
		this.subJobs = subJobs;
	}
	
}
