package com.navinfo.dataservice.engine.man.job;

import java.sql.Connection;
import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.engine.man.job.Day2Month.Day2MonthJobRunner;
import com.navinfo.dataservice.engine.man.job.NoTask2Medium.NoTask2MediumJobRunner;
import com.navinfo.dataservice.engine.man.job.Tips2Mark.Tips2MarkJobRunner;
import com.navinfo.dataservice.engine.man.job.bean.ItemType;
import com.navinfo.dataservice.engine.man.job.bean.Job;
import com.navinfo.dataservice.engine.man.job.bean.JobProgressStatus;
import com.navinfo.dataservice.engine.man.job.bean.JobStatus;
import com.navinfo.dataservice.engine.man.job.bean.JobType;
import com.navinfo.dataservice.engine.man.job.medium2quick.TaskMedium2QuickRunner;
import com.navinfo.dataservice.engine.man.job.bean.*;
import com.navinfo.dataservice.engine.man.job.mangeMesh.MangeMeshRunner;
import com.navinfo.dataservice.engine.man.job.message.JobMessage;
import com.navinfo.dataservice.engine.man.job.operator.JobDetailOperator;
import com.navinfo.dataservice.engine.man.job.operator.JobOperator;
import com.navinfo.dataservice.engine.man.job.operator.JobProgressOperator;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Created by wangshishuai3966 on 2017/7/10.
 */
public class JobService {
    private Logger log = LoggerRepos.getLogger(JobService.class);

    private JobService() {
    }

    public static JobService getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * 执行tips转mark
     *
     * @param itemId     目标对象ID
     * @param itemType   目标对象类型（项目、任务、子任务）
     * @param operator   执行人
     * @param isContinue 是否继续
     * @return jobId
     * @throws Exception
     */
    public long tips2Mark(long itemId, ItemType itemType, long operator, boolean isContinue, String parameter) throws Exception {
        try {
            if(itemType == ItemType.LOT){
                throw new Exception("不支持的对象类型 "+itemType);
            }
            return runCommonJob(JobType.TiPS2MARK,itemId, itemType , operator,isContinue, parameter);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new Exception("执行tips转mark失败，原因为:" + e.getMessage(), e);
        }
    }

    /**
     * 执行日落月
     *
     * @param itemId     目标对象ID
     * @param itemType   目标对象类型（项目、批次）
     * @param operator   执行人
     * @param isContinue 是否继续
     * @return jobId
     * @throws Exception
     */
    public long day2month(long itemId, ItemType itemType, long operator, boolean isContinue, String parameter) throws Exception {
        try {
            if(itemType != ItemType.LOT && itemType != ItemType.PROJECT){
                throw new Exception("不支持的对象类型 "+itemType);
            }
            return runCommonJob(JobType.DAY2MONTH,itemId, itemType , operator,isContinue, parameter);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new Exception("执行日落月失败，原因为:" + e.getMessage(), e);
        }
    }

    /**
     * 获取JOB每个步骤的执行状态
     *
     * @param itemId
     * @param itemType
     * @return
     * @throws Exception
     */
    public JSONArray getJobProgress(long itemId, ItemType itemType, JobType jobType) throws Exception {
        Connection conn = null;
        try {
            conn = DBConnector.getInstance().getManConnection();
            JobOperator jobOperator = new JobOperator(conn);
            return jobOperator.getJobProgressStatus(itemId, itemType, jobType);
        } catch (Exception e) {
            DbUtils.rollbackAndCloseQuietly(conn);
            log.error(e.getMessage(), e);
            throw new Exception("获取JOB状态失败，原因为:" + e.getMessage(), e);
        } finally {
            DbUtils.commitAndCloseQuietly(conn);
        }
    }

    /**
     * 获取按批次日落月的进度
     * @return
     * @throws Exception
     */
    public JSONObject getDay2MonthLotJobProgress() throws Exception {
        Connection conn = null;
        try {
            conn = DBConnector.getInstance().getManConnection();
            JobOperator jobOperator = new JobOperator(conn);
            return jobOperator.getDay2MonthLotJobProgressStatus();
        } catch (Exception e) {
            DbUtils.rollbackAndCloseQuietly(conn);
            log.error(e.getMessage(), e);
            throw new Exception("获取JOB状态失败，原因为:" + e.getMessage(), e);
        } finally {
            DbUtils.commitAndCloseQuietly(conn);
        }
    }

    /**
     * 更新Job步骤的状态，并继续执行Job
     *
     * @param phaseId
     * @param status
     * @param outParameter
     * @throws Exception
     */
    public void updateJobProgress(long phaseId, JobProgressStatus status, String outParameter) throws Exception {
        Connection conn = null;
        JobProgressOperator jobProgressOperator =null;
        try {
            log.info("updateJobProgress:phaseId:" + phaseId + ",status:" + status.value() + ",message:" + outParameter);
            conn = DBConnector.getInstance().getManConnection();            
            jobProgressOperator= new JobProgressOperator(conn);
            jobProgressOperator.updateStatus(phaseId, status, outParameter);
            conn.commit();
            if(!StringUtils.isEmpty(outParameter)){
            	try{
            		JSONObject outJson = null;
            		try{
            			outJson = JSONObject.fromObject(outParameter);
            		}catch (Exception e) {
            			log.warn("返回值不是json格式，不进行后续解析："+outParameter);
            		}
            		if(outJson!=null&&outJson.containsKey("detail")){
            			JSONObject detailJson = JSONObject.fromObject(outJson.get("detail"));
            			JobOperator jobOperator = new JobOperator(conn);
            			Job job = jobOperator.getByPhaseId(phaseId);
                        if (job == null) {
                            throw new Exception("phaseId:" + phaseId + "对应的job不存在！");
                        }
                        JobDetailOperator detailOperator=new JobDetailOperator(conn);
                        detailOperator.batchInsert(job.getJobId(),detailJson);
            		}
            	}catch (Exception e) {
					log.warn("详情解析失败："+outParameter, e);
				}
            }
        } catch (Exception e) {
            DbUtils.rollbackAndCloseQuietly(conn);
            log.error(e.getMessage(), e);
            throw new Exception("更新JOB步骤状态失败，原因为:" + e.getMessage(), e);
        }
        //job接续步骤的执行不应该影响已有步骤的执行情况。此处后续异常不进行抛出
        try{
        	jobProgressOperator.pushMsg(phaseId);

            if (status == JobProgressStatus.FAILURE) {
                //步骤失败，更新job状态为失败，停止执行
                log.info("updateJobProgress: phaseId "+phaseId+" set failure");
                JobOperator jobOperator = new JobOperator(conn);
                jobOperator.updateStatusByPhaseId(phaseId, JobStatus.FAILURE);
            } else {
                //步骤成功，继续执行job
                JobOperator jobOperator = new JobOperator(conn);
                Job job = jobOperator.getByPhaseId(phaseId);
                if (job == null) {
                    throw new Exception("phaseId:" + phaseId + "对应的job不存在！");
                }
                JobRunner runner = jobFactory(job.getType());

                if (runner == null) {
                    throw new Exception("不支持的任务类型：jobid " + job.getJobId() + ",type " + job.getType().value());
                }
                log.info("继续执行job:"+job.getJobId());
                runner.resume(job);
            }
        } catch (Exception e) {
            DbUtils.rollbackAndCloseQuietly(conn);
            log.error("JOB继续执行失败，原因为:" + e.getMessage(), e);
            //throw new Exception("更新JOB步骤状态失败，原因为:" + e.getMessage(), e);
        } finally {
            DbUtils.commitAndCloseQuietly(conn);
        }
    }

    private static class SingletonHolder {
        private static final JobService INSTANCE = new JobService();
    }

	public long runCommonJob(JobType jobType, long itemId, ItemType itemType, long operator, boolean isContinue, String parameter) throws Exception {
		log.info("start runCommonJob:jobType="+jobType+",itemType="+itemType+",itemId="+itemId+",isContinue="+isContinue+",parameter="+parameter);
		try {
			JobRunner runner = jobFactory(jobType);
			if(runner==null){
				throw new Exception("执行JOB失败，原因为:没有该类型的job="+jobType);
			}
            long jobId= runner.run(itemId, itemType, isContinue, operator, parameter);
            log.info("end runCommonJob:jobType="+jobType+",itemType="+itemType+",itemId="+itemId+",isContinue="+isContinue+",parameter="+parameter);
            return jobId;
		} catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new Exception("执行JOB失败，原因为:" + e.getMessage(), e);
        }
	}
	
	/**
	 * 根据jobType获取执行类
	 * @param jobType
	 * @return
	 */
	private JobRunner jobFactory(JobType jobType){
		JobRunner runner=null;
		if(jobType==JobType.DAY2MONTH){
			runner= new Day2MonthJobRunner();
		}else if(jobType==JobType.TiPS2MARK){
			runner= new Tips2MarkJobRunner();
		}else if(jobType==JobType.NOTASK2MID){
			runner= new NoTask2MediumJobRunner();
		}else if(jobType == JobType.MID2QUICK){		
 			runner = new TaskMedium2QuickRunner();		
  		}else if(jobType==JobType.MANGE_MESH){
  			runner=new MangeMeshRunner();
  		}
		return runner;
	}
}
