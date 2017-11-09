package com.navinfo.dataservice.engine.man.job.Tips2Mark;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;
import com.navinfo.dataservice.api.datahub.iface.DatahubApi;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.ServiceInvokeUtil;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.engine.man.job.JobPhase;
import com.navinfo.dataservice.engine.man.job.bean.InvokeType;
import com.navinfo.dataservice.engine.man.job.bean.ItemType;
import com.navinfo.dataservice.engine.man.job.bean.JobProgressStatus;
import com.navinfo.dataservice.engine.man.job.operator.JobProgressOperator;
import net.sf.json.JSONObject;
import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

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
        log.info("CreateCMSTaskPhase start:phaseId "+jobProgress.getPhaseId());
        Connection conn = null;
        JobProgressOperator jobProgressOperator = null;
        try {
            conn = DBConnector.getInstance().getManConnection();
            jobProgressOperator = new JobProgressOperator(conn);
            jobProgress.setStatus(JobProgressStatus.RUNNING);
            //更新状态为进行中
            jobProgressOperator.updateStatus(jobProgress);
            conn.commit();
            //中线采集子任务tips2mark：=4时，不继续执行
            //中线采集任务，快线项目tips2mark=4时，继续执行
            if (lastJobProgress.getStatus() == JobProgressStatus.NODATA&&jobRelation.getItemType()==ItemType.SUBTASK) {
                //如果无数据，不需要创建cms任务
            	jobProgress.setStatus(JobProgressStatus.NODATA);
            	jobProgressOperator.updateStatus(jobProgress);
                //JobService.getInstance().updateJobProgress(jobProgress.getPhaseId(), jobProgress.getStatus(), jobProgress.getOutParameter());
                return jobProgress.getStatus();
            }
            //业务逻辑
            Map<String, Object> cmsInfo = Tips2MarkUtils.getItemInfo(conn, jobRelation.getItemId(), jobRelation.getItemType());
            JSONObject parameter = new JSONObject();
            DatahubApi datahub = (DatahubApi) ApplicationContextUtil
                    .getBean("datahubApi");
            DbInfo metaDb = datahub.getOnlyDbByType("metaRoad");
            parameter.put("metaIp", metaDb.getDbServer().getIp());
            parameter.put("metaUserName", metaDb.getDbUserName());

            DbInfo auDb = datahub.getOnlyDbByType("gen2Au");
            parameter.put("fieldDbIp", auDb.getDbServer().getIp());
            parameter.put("fieldDbName", auDb.getDbUserName());
            
            //modify by songhe 2017/09/27  增加参数周出品外业成果库信息
            DbInfo auWeekDb = datahub.getOnlyDbByType("gen2AuWeek");
            parameter.put("fieldWeekDbIp", auWeekDb.getDbServer().getIp());
            parameter.put("fieldWeekDbName", auWeekDb.getDbUserName());
            

            JSONObject taskPar = new JSONObject();
            taskPar.put("taskName", cmsInfo.get("collectName"));
            taskPar.put("fieldTaskId", cmsInfo.get("collectId"));
            taskPar.put("taskId", cmsInfo.get("collectId"));
            taskPar.put("province", cmsInfo.get("provinceName"));
            taskPar.put("city", cmsInfo.get("cityName"));
            taskPar.put("town", cmsInfo.get("blockName"));
            taskPar.put("infoName", cmsInfo.get("infoName"));

            String area = "中线一体化作业";
            String workType = "更新";  
            String workSeason = SystemConfigFactory.getSystemConfig().getValue(PropConstant.seasonVersion);
            String uploadMethod="";
            if(cmsInfo.get("uploadMethod")!=null&&!StringUtils.isEmpty(cmsInfo.get("uploadMethod").toString())){
            	uploadMethod=cmsInfo.get("uploadMethod").toString();
            }
            if (jobRelation.getItemType() == ItemType.PROJECT) {
                area = "快线一体化作业";
                workType = "快速更新";   
                taskPar.put("poiMeshes", cmsInfo.get("poiMeshes"));
            	taskPar.put("poiPlanLoad", cmsInfo.get("poiPlanLoad").toString());  
            	if(StringUtils.isEmpty(uploadMethod)){
            		uploadMethod="快速更新";
            	}
            } 
            if (jobRelation.getItemType() == ItemType.TASK) {
            	taskPar.put("poiMeshes", cmsInfo.get("poiMeshes"));
            	taskPar.put("poiPlanLoad", cmsInfo.get("poiPlanLoad").toString());               
            }
            if (jobRelation.getItemType() == ItemType.SUBTASK) {
            	taskPar.put("taskParentId", cmsInfo.get("taskId"));              
            }
            
            taskPar.put("workType", workType);
            taskPar.put("area", area);
            taskPar.put("userId", cmsInfo.get("userNickName"));
            taskPar.put("workSeason", workSeason);
            taskPar.put("markTaskType", jobRelation.getItemType().value());
            taskPar.put("taskBatch", cmsInfo.get("lot"));
        	taskPar.put("uploadMethod",uploadMethod);
            parameter.put("taskInfo", taskPar);

            String cmsUrl = SystemConfigFactory.getSystemConfig().getValue(PropConstant.cmsUrl);
            Map<String, String> parMap = new HashMap<>();
            parMap.put("parameter", parameter.toString());
            log.info("phaseId:"+jobProgress.getPhaseId()+",cms param:"+parameter.toString());
            jobProgress.setInParameter(parameter.toString());
            String result = ServiceInvokeUtil.invoke(cmsUrl, parMap, 10000);
            log.info("phaseId:"+jobProgress.getPhaseId()+",cms result:"+result);
            //result="{success:false, msg:\"没有找到用户名为【fm_meta_all_sp6】元数据库版本信息！\"}";
            jobProgress.setOutParameter(result);
            JSONObject res = null;
            try {
                res = JSONObject.fromObject(result);
            } catch (Exception ex) {
                res = null;
                jobProgress.setStatus(JobProgressStatus.FAILURE);
                jobProgress.setMessage(jobProgress.getMessage() + ex.getMessage());
            }
            if (res != null) {
            	//msg:{"status":1}//1创建2没创建.判断cms接口执行成功，是否有创建cms任务，若创建，则赋值执行成功；否则赋值无数据，表示未创建cms任务
                boolean success = res.getBoolean("success");
                if (success) {
                	try{
	                	if(res.containsKey("msg")&&res.getJSONObject("msg").containsKey("status")){
	                		int status=res.getJSONObject("msg").getInt("status");
	                		if(status==1){jobProgress.setStatus(JobProgressStatus.SUCCESS);}
	                		else{jobProgress.setStatus(JobProgressStatus.NODATA);}
	                	}else{jobProgress.setStatus(JobProgressStatus.SUCCESS);}
                	}catch (Exception e) {
                		 jobProgress.setStatus(JobProgressStatus.SUCCESS);
					}                   
                } else {
                    log.error("phaseId:"+jobProgress.getPhaseId()+",cms error msg:" + res.get("msg"));
                    jobProgress.setStatus(JobProgressStatus.FAILURE);
                    jobProgress.setOutParameter("cms error:" + res.get("msg").toString());
                }
            }
            //JobService.getInstance().updateJobProgress(jobProgress.getPhaseId(), jobProgress.getStatus(), jobProgress.getOutParameter());

            jobProgressOperator.updateStatus(jobProgress);

            return jobProgress.getStatus();
        } catch (Exception ex) {
            //有异常，更新状态为执行失败
            log.error(ex.getMessage(), ex);
            DbUtils.rollback(conn);
            if (jobProgressOperator != null && jobProgress != null) {
                jobProgress.setStatus(JobProgressStatus.FAILURE);
                jobProgress.setOutParameter(ex.getMessage());
                //JobService.getInstance().updateJobProgress(jobProgress.getPhaseId(), jobProgress.getStatus(), jobProgress.getOutParameter());
                jobProgressOperator.updateStatus(jobProgress);
            }
            //throw ex;
        } finally {
            log.info("CreateCMSTaskPhase end:phaseId "+jobProgress.getPhaseId() + ",status "+jobProgress.getStatus());
            DbUtils.commitAndCloseQuietly(conn);
        }
		return jobProgress.getStatus();
    }    
}
