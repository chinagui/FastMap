package com.navinfo.dataservice.impcore.commit;

import java.util.Map;

import com.navinfo.dataservice.api.edit.model.FmEditLock;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.impcore.flushbylog.LogFlusher;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;

/*
 * @author mayunfei
 * 2016年6月6日
 * 描述：job-frameworkCommitDay2MonthRoad.java
 * 道路数据从日大区库落入月大区库
 */
public class CommitDay2MonthRoadJob extends AbstractCommitDay2MonthJob {
	public CommitDay2MonthRoadJob(JobInfo jobInfo) {
		super(jobInfo);
	}
	@Override
	public IDay2MonthCommand createDay2MonthCommand() {
		return new CommitDay2MonthRoadCommand(this.request);
	}
	

	
	public class CommitDay2MonthRoadCommand implements IDay2MonthCommand{
		AbstractJobRequest req;
		
		public CommitDay2MonthRoadCommand(AbstractJobRequest req) {
			super();
			this.req = req;
		}

		@Override
		public Map queryRegionGridMapping() throws Exception {
			ManApi gridSelectorApiSvr = (ManApi) ApplicationContextUtil.getBean("manApi");
			return gridSelectorApiSvr.queryRegionGridMapping(((CommitDay2MonthRoadJobRequest )req).getGridList());
		}

		@Override
		public String getStopTime() {
			return ((CommitDay2MonthRoadJobRequest )req).getStopTime();
		}

		@Override
		public String getFlushFeatureType() {
			return LogFlusher.FEATURE_ROAD;
		}

		@Override
		public int getLockType() {
			return FmEditLock.TYPE_DAY2MON;
		}
		
	}




	
	

}

