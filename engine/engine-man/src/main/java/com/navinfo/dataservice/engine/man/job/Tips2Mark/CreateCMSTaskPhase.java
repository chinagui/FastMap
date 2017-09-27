package com.navinfo.dataservice.engine.man.job.Tips2Mark;

import com.navinfo.dataservice.api.datahub.iface.DatahubApi;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.man.model.Region;
import com.navinfo.dataservice.api.man.model.Task;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.ServiceInvokeUtil;
import com.navinfo.dataservice.engine.man.job.JobPhase;
import com.navinfo.dataservice.engine.man.job.bean.InvokeType;
import com.navinfo.dataservice.engine.man.job.bean.ItemType;
import com.navinfo.dataservice.engine.man.job.bean.JobProgressStatus;
import com.navinfo.dataservice.engine.man.job.operator.JobProgressOperator;
import com.navinfo.dataservice.engine.man.program.ProgramService;
import com.navinfo.dataservice.engine.man.region.RegionService;
import com.navinfo.dataservice.engine.man.subtask.SubtaskService;
import com.navinfo.dataservice.engine.man.task.TaskService;
import com.navinfo.navicommons.database.QueryRunner;

import net.sf.json.JSONObject;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
            DbInfo auWeekDb = datahub.getOnlyDbByType("metaRoad");
            parameter.put("fieldWeekDbIp", auWeekDb.getDbServer().getIp());
            parameter.put("fieldWeekDbName", auWeekDb.getDbUserName());
            

            JSONObject taskPar = new JSONObject();
            taskPar.put("taskName", cmsInfo.get("collectName"));
            taskPar.put("fieldTaskId", cmsInfo.get("collectId"));
            taskPar.put("taskId", cmsInfo.get("collectId"));
            taskPar.put("province", cmsInfo.get("provinceName"));
            taskPar.put("city", cmsInfo.get("cityName"));
            taskPar.put("town", cmsInfo.get("blockName"));

            String area = "中线一体化作业";
            String workType = "更新";
            String workSeason = SystemConfigFactory.getSystemConfig().getValue(PropConstant.seasonVersion);
            if (jobRelation.getItemType() == ItemType.PROJECT) {
                area = "快线一体化作业";
                workType = "快速更新";
            } 
            taskPar.put("workType", workType);
            taskPar.put("area", area);
            taskPar.put("userId", cmsInfo.get("userNickName"));
            taskPar.put("workSeason", workSeason);
            taskPar.put("markTaskType", jobRelation.getItemType().value());
            //modify by songhe taskInfo中添加参数
            addTaskParData(conn, taskPar);
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
    
    /**
     * 处理添加参数中的数据
     * @param Connection
     * @param JSONObject
     * @throws Exception 
     * 
     * */
    @SuppressWarnings("unchecked")
	public void addTaskParData(Connection conn, JSONObject taskPar) throws Exception{
    	List<Task> tasks = new ArrayList<>();
    	int taskBatch = 0;
    	//默认快线
    	String uploadMethod = "快速更新";
    	if(jobRelation.getItemType() == ItemType.PROJECT){
    		try {
    			Map<String, Object> programMap = ProgramService.getInstance().query(conn, (int) jobRelation.getItemId());
    			//快线项目传递情报名称(这里的项目只能是快线)
    			taskPar.put("infoName", programMap.get("inforName"));
				List<Task> taskPojos = TaskService.getInstance().getTaskByProgramId(conn, (int) jobRelation.getItemId());
				for(Task task : taskPojos){
					//采集任务
					if(0 == task.getType()){
						tasks.add(task);
					}
					//项目批次取月编任务的批次
					if(2 == task.getType()){
						taskBatch = task.getLot();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw e;
			}
    	}
    	if(jobRelation.getItemType() == ItemType.TASK){
    		Task task = TaskService.getInstance().queryByTaskId(conn, (int) jobRelation.getItemId());
    		if(0 == task.getType()){
    			tasks.add(task);
    		}
    		taskBatch = task.getLot();
    		if(task.getProgramType() == 1){
    			uploadMethod = task.getUploadMethod();
    		}
    	}
    	if(jobRelation.getItemType() == ItemType.SUBTASK){
    		Map<String, Integer> taskMap = SubtaskService.getInstance().getTaskBySubtaskId(conn, (int) jobRelation.getItemId());
    		Task taskPojo = TaskService.getInstance().queryByTaskId(conn, taskMap.get("taskId"));
    		taskBatch = taskPojo.getLot();
    		taskPar.put("taskParentId", taskPojo.getTaskId());
    		if(1 == taskMap.get("programType")){
    			uploadMethod = taskPojo.getUploadMethod();
    		}
    	}
    	//子任务不传poiMeshes和poiPlanLoad
    	Map<String, Object> poisData = queryMeshesByTasks(conn, tasks);
    	if(poisData != null){
        	taskPar.put("poiMeshes", (Set<Integer>) poisData.get("poiMeshes"));
        	taskPar.put("poiPlanLoad", (int) poisData.get("poiPlanLoad"));
    	}
    	taskPar.put("taskBatch", taskBatch);
    	taskPar.put("uploadMethod", uploadMethod);
    	
    }
    
    /**
     * 根据taskIds查询增加参数的数据
     * @throws Exception 
     * 
     * */
    public Map<String, Object> queryMeshesByTasks(Connection conn, List<Task> tasks) throws Exception{
    	if(jobRelation.getItemType() == ItemType.SUBTASK){
    		return null;
    	}
    	Connection dailyConn = null;
    	Map<String, Object> result = new HashMap<>();
    	try{
    		Map<Integer, ArrayList<Integer>> taskMap = new HashMap<Integer, ArrayList<Integer>>();
    		QueryRunner run = new QueryRunner();
    		for(Task task : tasks){
    			Region region = RegionService.getInstance().query(conn, task.getRegionId());
    			int DbId = region.getDailyDbId();
    			int taskId = task.getTaskId();
    			if(taskMap.containsKey(DbId)){
    				taskMap.get(DbId).add(taskId);
    			}else{
    				ArrayList<Integer> taskIds = new ArrayList<>();
    				taskIds.add(taskId);
    				taskMap.put(DbId, taskIds);
    			}
    		}
    		Set<Integer> meshs = new HashSet<>();
    		Set<Integer> pids = new HashSet<>();
    		for(Entry<Integer, ArrayList<Integer>> entry : taskMap.entrySet()){  
    			dailyConn = DBConnector.getInstance().getConnectionById(entry.getKey());
//        		String sql = "select distinct t.mesh_id from IX_POI t where exists "
//            			+ "(select ts.pid from POI_EDIT_STATUS ts where ts.medium_task_id in "
//            			+ entry.getValue().toString().replace("[", "(").replace("]", ")")+" and ts.pid = t.pid)";
        		
        		if(entry.getValue().size() > 0){
        			String poiSql = "select ts.pid from POI_EDIT_STATUS ts where ts.medium_task_id in"
                			+ entry.getValue().toString().replace("[", "(").replace("]", ")");
            		
            		log.info("querypoiSql :" + poiSql);
            		
            		ResultSetHandler<Set<Integer>> handler = new ResultSetHandler<Set<Integer>>() {
            			public Set<Integer> handle(ResultSet rs) throws SQLException {
            				Set<Integer> result = new HashSet<>();
            				while(rs.next()) {
            					result.add(rs.getInt("pid"));
            				}
            				return result;
            			}
            		};
            		pids.addAll(run.query(dailyConn, poiSql, handler));
        		}
            	
        		if(pids.size() > 0){
//        			String sql = "select t.mesh_id from IX_POI t where t.pid in ("
//                			+ pids.toString().replace("[", "(").replace("]", ")");
            		String sql = "select t.mesh_id from IX_POI t where exists "
        			+ "(select ts.pid from POI_EDIT_STATUS ts where ts.medium_task_id in "
        			+ entry.getValue().toString().replace("[", "(").replace("]", ")")+" and ts.pid = t.pid)";
            		ResultSetHandler<Set<Integer>> rsHandler = new ResultSetHandler<Set<Integer>>() {
            			public Set<Integer> handle(ResultSet rs) throws SQLException {
            				Set<Integer> result = new HashSet<Integer>();
            				while(rs.next()) {
            					result.add(rs.getInt("mesh_id"));
            				}
            				return result;
            			}
            		};
            		meshs.addAll(run.query(dailyConn, sql, rsHandler));
        			}
        		}
        		 
    		result.put("poiMeshes", meshs);
    		result.put("poiPlanLoad", pids.size());
    	}catch(Exception e){
    		log.error("queryMeshesByTasks error" + e.getMessage(), e);
    		throw e;
    	}finally{
    		DbUtils.closeQuietly(dailyConn);
    	}
    	return result;
    }
}
