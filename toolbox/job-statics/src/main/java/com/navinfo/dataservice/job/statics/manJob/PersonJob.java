package com.navinfo.dataservice.job.statics.manJob;

import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.job.statics.AbstractStatJob;
import com.navinfo.dataservice.jobframework.exception.JobException;

public class PersonJob extends AbstractStatJob {

	public PersonJob(JobInfo jobInfo) {
		super(jobInfo);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String stat() throws JobException {
		PersonJobRequest statReq = (PersonJobRequest)request;
		try {
			ManApi manApi = (ManApi)ApplicationContextUtil.getBean("manApi");
			List<Map<String, Object>> persionList = manApi.staticsPersionJob(statReq.getTimestamp());
			System.out.println(persionList);
			
			return null;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new JobException(e.getMessage(),e);
		}finally{
		}
	}

}
