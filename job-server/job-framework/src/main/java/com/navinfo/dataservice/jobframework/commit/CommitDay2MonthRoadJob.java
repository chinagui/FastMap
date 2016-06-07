package com.navinfo.dataservice.jobframework.commit;

import java.util.List;
import java.util.Map;
import java.util.Set;





import com.navinfo.dataservice.api.datahub.iface.DbManagerApiService;
import com.navinfo.dataservice.api.edit.iface.GridSelectorApiService;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;

/*
 * @author mayunfei
 * 2016年6月6日
 * 描述：job-frameworkCommitDay2MonthRoad.java
 * 道路数据从日大区库落入月大区库
 */
public class CommitDay2MonthRoadJob extends AbstractJob {

	public CommitDay2MonthRoadJob(JobInfo jobInfo) {
		super(jobInfo);
	}

	/* 给定grid列表；
	 * 根据grid计算出对应的大区库，并将对应大区日库中grid范围内的履历刷到月库；
	 * 刷履历过程中，如果出现异常，需要跳过异常继续刷其他的履历；
	 * 出现异常的grid需要给grid打标记为"日落月失败"；
	 * @see com.navinfo.dataservice.jobframework.runjob.AbstractJob#execute()
	 */
	@Override
	public void execute() throws JobException {
		CommitDay2MonthRoadJobRequest req = (CommitDay2MonthRoadJobRequest)this.getRequest();
		List<Integer> gridList = req.getGridList();
		GridSelectorApiService gridSelectorApiSvr = (GridSelectorApiService) ApplicationContextUtil.getBean("gridSelector");
		try{
			//获取大区和grid的映射关系
			Map regionGridMapping = gridSelectorApiSvr.queryRegionGridMapping(gridList);
			Set<Integer> regionSet = regionGridMapping.keySet();
			for (Integer regionId:regionSet){
				//得到大区对应的grid列表
				List<Integer> gridListOfRegion = (List<Integer>) regionGridMapping.get(regionId);
				//在大区日库中根据grid列表获取履历，并刷新对应的月库
				//根据大区id获取对应的大区日库、大区月库
				DbManagerApiService dbManApiSvr = (DbManagerApiService) ApplicationContextUtil.getBean("dbManagerApiService");
				regionDbDaily = dbManApiSvr.getOnlyDbByName(dbName);
			}
			
		}catch(Exception e){
			throw new JobException(e);
		}
		
		
	}

}

