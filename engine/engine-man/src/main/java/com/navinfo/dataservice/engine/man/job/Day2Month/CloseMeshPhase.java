package com.navinfo.dataservice.engine.man.job.Day2Month;

import com.navinfo.dataservice.api.fcc.iface.FccApi;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.man.config.ConfigService;
import com.navinfo.dataservice.engine.man.job.JobPhase;
import com.navinfo.dataservice.engine.man.job.JobService;
import com.navinfo.dataservice.engine.man.job.bean.InvokeType;
import com.navinfo.dataservice.engine.man.job.bean.ItemType;
import com.navinfo.dataservice.engine.man.job.bean.JobProgressStatus;
import com.navinfo.dataservice.engine.man.job.operator.JobProgressOperator;
import com.navinfo.navicommons.database.QueryRunner;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by wangshishuai3966 on 2017/7/14.
 */
public class CloseMeshPhase extends JobPhase {
    private static Logger log = LoggerRepos.getLogger(CloseMeshPhase.class);

    @Override
    public void initInvokeType() {
        this.invokeType = InvokeType.SYNC;
    }

    @Override
    public JobProgressStatus run() throws Exception {
        log.info("CloseMeshPhase start:phaseId "+jobProgress.getPhaseId());
        Connection conn = null;
        Connection meta = null;
        JobProgressOperator jobProgressOperator = null;
        //modiby by songhe
        JSONObject viewJson = new JSONObject();
        try {
            conn = DBConnector.getInstance().getManConnection();
            //更新状态为进行中
            jobProgressOperator = new JobProgressOperator(conn);
            jobProgress.setStatus(JobProgressStatus.RUNNING);
            jobProgressOperator.updateStatus(jobProgress);
            conn.commit();

            //业务逻辑
            if(jobRelation.getItemType()== ItemType.PROJECT) {
                //按项目落需要关闸
                JSONObject parameter = JSONObject.fromObject(job.getParameter());
                int lot = parameter.getInt("lot");

                JSONObject outPrarm = JSONObject.fromObject(lastJobProgress.getOutParameter());
                List<Integer> meshs =new ArrayList<Integer>();
                //存在日落月没有图幅的情况
                if(outPrarm.containsKey("allQuickMeshes")){
                	try{
                		meshs = (List<Integer>) JSONArray.toCollection(outPrarm.getJSONArray("allQuickMeshes"));
                	}catch (Exception e) {
						log.warn("获取日落月图幅信息错误："+e.getMessage()+",outParam:"+outPrarm);
					}
                }
                log.info("phaseId:"+jobProgress.getPhaseId()+",day2month mesh:"+meshs.toString());
                FccApi fccApi = (FccApi) ApplicationContextUtil.getBean("fccApi");

                Set<Integer> collectTaskSet = Day2MonthUtils.getTaskIdSet(conn, jobRelation.getItemId());
                Set<Integer> tipsMeshset = fccApi.getTipsMeshIdSet(collectTaskSet,4);
                log.info("phaseId:"+jobProgress.getPhaseId()+",tips mesh:"+tipsMeshset.toString());
                viewJson.put("tipsMeshs", tipsMeshset);
                viewJson.put("collectTaskMeshs", collectTaskSet);
                if(meshs!=null&&meshs.size()!=0){tipsMeshset.addAll(meshs);}                
                
                QueryRunner run = new QueryRunner();
                if(tipsMeshset.size()>0) {
                	//快线传进来的参数为第3批次，不关闸；其他的，则与原始批次不一致的都要关闸
                	meta = DBConnector.getInstance().getMetaConnection();
                	if(lot!=3){
	                	JSONObject dataJson=new JSONObject();
	                	dataJson.put("openFlag", 0);
	                	dataJson.put("quickAction", lot);
	                	viewJson.put("openFlag", 0);
	                	viewJson.put("lot", lot);
	                	
	                	dataJson.put("meshList", tipsMeshset.toString().replace("[", "").replace("]", ""));
	                	log.info("快线批次赋值+图幅关闭");
	                	//快线项目日落月关图幅时，只有批次不一致的才关闭图幅
	                    
	                    ConfigService.getInstance().mangeMesh(meta, dataJson);
                	}
                    log.info("快线批次赋值");//快线批次字段是所有图幅均赋值
                    String updateSql = "UPDATE SC_PARTITION_MESHLIST SET QUICK"+lot+"_FLAG=1 WHERE MESH IN "
                            + tipsMeshset.toString().replace("[", "(").replace("]", ")");
                    log.info("phaseId:"+jobProgress.getPhaseId()+",updateMesh sql:"+updateSql);
                    run.update(meta, updateSql);
                }
                //维护快线项目下月编任务对应的lot字段
                int programId = (int) jobRelation.getItemId();
                String sql = "UPDATE TASK T SET T.LOT = "+lot+" WHERE T.PROGRAM_ID = "+programId;
                run.update(conn, sql);
            }
            //更新状态为成功
            jobProgress.setStatus(JobProgressStatus.SUCCESS);
            jobProgress.setInParameter(viewJson.toString());
            jobProgressOperator.updateStatus(jobProgress);
            //return jobProgress.getStatus();
        } catch (Exception ex) {
            //有异常，更新状态为执行失败
            log.error(ex.getMessage(), ex);
            DbUtils.rollback(conn);
            DbUtils.rollback(meta);
            jobProgress.setStatus(JobProgressStatus.FAILURE);
            if (jobProgressOperator != null && jobProgress != null) {
            	jobProgress.setInParameter(viewJson.toString());
                jobProgress.setOutParameter(ex.getMessage());
                jobProgressOperator.updateStatus(jobProgress);
            }
            //throw ex;
        } finally {
            log.info("CloseMeshPhase end:phaseId "+jobProgress.getPhaseId() + ",status "+jobProgress.getStatus());
            DbUtils.commitAndCloseQuietly(conn);
            DbUtils.commitAndCloseQuietly(meta);
        }
        return jobProgress.getStatus();
    }
}
