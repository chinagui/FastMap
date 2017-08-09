package com.navinfo.dataservice.job;

import java.sql.Connection;

import net.sf.json.JSONArray;

import org.apache.commons.dbutils.DbUtils;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.fcc.tips.TipsOperator;
import com.navinfo.dataservice.engine.man.task.TaskService;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.navicommons.geo.computation.GridUtils;
/**
 * 功能：
 * 1.采集任务进行无任务转中的操作。（无任务转中代码参考采集任务无任务转中按钮）。
 * 		fcc的转换调用http接口
 * 2.	修改task_progress中的状态：成功/失败
 * @author zhangxiaoyi
 *
 */
public class TaskOther2MediumJob extends AbstractJob {

	public TaskOther2MediumJob(JobInfo jobInfo) {
		super(jobInfo);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void execute() throws JobException {
		Connection conn=null;
		TaskOther2MediumJobRequest myJobRequest=(TaskOther2MediumJobRequest)request;
		ManApi api=(ManApi) ApplicationContextUtil.getBean("manApi");
		try {
			conn=DBConnector.getInstance().getManConnection();
			int taskId=myJobRequest.getTaskId();
			JSONArray gridIds = TaskService.getInstance().getGridListByTaskId(taskId);
			String wkt = GridUtils.grids2Wkt(gridIds);
			log.info("无任务的tips批中线任务号:taskId="+taskId+",wkt="+wkt);
			//fcc成果批中线
			TipsOperator tipsOperator = new TipsOperator();
            long tipsNum = tipsOperator.batchNoTaskDataByMidTask(wkt, myJobRequest.getTaskId());
            log.info("taskId="+taskId+"批poi无任务数据，并修改统计信息");
			int poiNum=TaskService.getInstance().batchMidTaskByTaskId(myJobRequest.getTaskId());
			api.updateJobProgress(myJobRequest.getPhaseId(), 2, "tips数量:"+tipsNum+";poi数量:"+poiNum);
			//api.endProgressAndSocket(myJobRequest.getPhaseId(), 2, "tips数量:"+tipsNum+";poi数量:"+poiNum);
			//TaskProgressOperation.updateProgress(conn, myJobRequest.getPhaseId(), 2, "poi数量:"+poiNum);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);	
			try {
				api.updateJobProgress(myJobRequest.getPhaseId(),3, e.getMessage());
			} catch (Exception e1) {
				DbUtils.rollbackAndCloseQuietly(conn);
				log.error("", e);
				throw new JobException(e);
			}
			log.error("", e);
			throw new JobException(e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

}
