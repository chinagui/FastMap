package com.navinfo.dataservice.engine.man.job.Tips2Mark;

import com.alibaba.fastjson.JSONObject;
import com.navinfo.dataservice.api.datahub.iface.DatahubApi;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.fcc.iface.FccApi;
import com.navinfo.dataservice.api.man.model.TaskCmsProgress;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.ServiceInvokeUtil;
import com.navinfo.dataservice.engine.man.job.JobPhase;
import com.navinfo.dataservice.engine.man.job.bean.InvokeType;
import com.navinfo.dataservice.engine.man.job.bean.JobProgress;
import com.navinfo.dataservice.engine.man.job.bean.JobProgressStatus;
import com.navinfo.dataservice.engine.man.job.bean.JobStatus;
import com.navinfo.dataservice.engine.man.job.operator.JobProgressOperator;
import com.navinfo.dataservice.engine.man.task.TaskService;
import com.navinfo.navicommons.database.QueryRunner;
import net.sf.json.JSONArray;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by wangshishuai3966 on 2017/7/6.
 */
public class CreateCMSTaskPhase extends JobPhase {
    private static Logger log = LoggerRepos.getLogger(CreateCMSTaskPhase.class);

    @Override
    public void initInvokeType() {
        this.invokeType = InvokeType.SYNC;
    }

    @Override
    public JobProgressStatus run() throws Exception{
        Connection conn = null;
        JobProgressOperator jobProgressOperator = null;
        try {
            conn = DBConnector.getInstance().getManConnection();
            jobProgressOperator = new JobProgressOperator(conn);
            if(lastJobProgress.getStatus()==JobProgressStatus.NODATA){
                jobProgressOperator.updateStatus(jobProgress, JobProgressStatus.SUCCESS);
                return jobProgress.getStatus();
            }

            //更新状态为进行中
            jobProgressOperator.updateStatus(jobProgress, JobProgressStatus.RUNNING);
            conn.commit();

             //业务逻辑
//            Map<String, Object> cmsInfo = Tips2MarkUtils.getCmsInfo(conn, jobRelation.getItemId());
//            JSONObject par=new JSONObject();
//            DatahubApi datahub = (DatahubApi) ApplicationContextUtil
//                    .getBean("datahubApi");
//            DbInfo metaDb = datahub.getOnlyDbByType("metaRoad");
//            par.put("metaIp", metaDb.getDbServer().getIp());
//            par.put("metaUserName", metaDb.getDbUserName());
//
//            DbInfo auDb = datahub.getOnlyDbByType("gen2Au");
//            par.put("fieldDbIp", auDb.getDbServer().getIp());
//            par.put("fieldDbName", auDb.getDbUserName());
//
//            JSONObject taskPar=new JSONObject();
//            taskPar.put("taskName", cmsInfo.get("cmsName"));
//            taskPar.put("fieldTaskId", cmsInfo.get("collectId"));
//            taskPar.put("taskId", cmsInfo.get("cmsId"));
//            taskPar.put("province", cmsInfo.get("provinceName"));
//            taskPar.put("city", cmsInfo.get("cityName"));
//            taskPar.put("town", cmsInfo.get("blockName"));
//            Object workProperty=cmsInfo.get("workProperty");
//            if(workProperty==null){workProperty="更新";}
//            taskPar.put("workType", workProperty);
//            Object workType=cmsInfo.get("workType");
//            if(workType==null){workType="行人导航";}
//            taskPar.put("area",workType);
//            taskPar.put("userId", cmsInfo.get("userNickName"));
//            taskPar.put("workSeason", SystemConfigFactory.getSystemConfig().getValue(PropConstant.seasonVersion));
//            TaskCmsProgress phase = TaskService.getInstance().queryCmsProgreeByPhaseId(conn, (int)job.getJobId());
//            taskPar.put("meshs",phase.getMeshIds());
//
//            //判断之前tip2aumark的过程，是有tips还是没有tips
//            if(lastJobProgress.getStatus()==JobProgressStatus.NODATA) {
//                taskPar.put("hasAumark", false);
//            }else{
//                taskPar.put("hasAumark", true);
//            }
//            par.put("taskInfo", taskPar);
//
//            String cmsUrl = SystemConfigFactory.getSystemConfig().getValue(PropConstant.cmsUrl);
//            Map<String,String> parMap = new HashMap<String,String>();
//            parMap.put("parameter", par.toString());
//            log.info(par.toString());
//            jobProgress.setMessage(par.toString());
//            String result = ServiceInvokeUtil.invoke(cmsUrl, parMap, 10000);
//            //result="{success:false, msg:\"没有找到用户名为【fm_meta_all_sp6】元数据库版本信息！\"}";
//            JSONObject res = null;
//            try {
//                res = JSONObject.parseObject(result);
//            }catch (Exception ex){
//                res=null;
//                jobProgress.setStatus(JobProgressStatus.FAILURE);
//                jobProgress.setMessage(jobProgress.getMessage()+result);
//            }
//            if(res!=null) {
//                boolean success = res.getBoolean("success");
//                if (success) {
//                    jobProgress.setStatus(JobProgressStatus.SUCCESS);
//                } else {
//                    log.error("cms error msg" + res.get("msg"));
//                    jobProgress.setStatus(JobProgressStatus.FAILURE);
//                    jobProgress.setMessage(jobProgress.getMessage() + "cms error:" + res.get("msg").toString());
//                }
//            }
            jobProgressOperator.updateStatus(jobProgress, JobProgressStatus.SUCCESS);
            return jobProgress.getStatus();
        } catch (Exception ex) {
            //有异常，更新状态为执行失败
            DbUtils.rollback(conn);
            if (jobProgressOperator != null && jobProgress != null) {
                jobProgress.setStatus(JobProgressStatus.FAILURE);
                jobProgress.setMessage(jobProgress.getMessage()+ex.getMessage());
                jobProgressOperator.updateStatus(jobProgress, JobProgressStatus.FAILURE);
            }
            throw ex;
        } finally {
            DbUtils.commitAndCloseQuietly(conn);
        }
    }
}
