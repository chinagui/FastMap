package com.navinfo.dataservice.engine.fcc.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.api.man.model.Subtask;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.navinfo.dataservice.api.fcc.iface.FccApi;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.fcc.TaskType;
import com.navinfo.dataservice.dao.fcc.check.operate.CheckTaskSelector;
import com.navinfo.dataservice.engine.fcc.tips.TipsOperator;
import com.navinfo.dataservice.engine.fcc.tips.TipsSelector;
import com.navinfo.nirobot.business.Tips2AuMarkApi;

@Service("fccApi")
public class FccApiImpl implements FccApi{

    private static final Logger logger = Logger.getLogger(FccApiImpl.class);

    @Override
    public JSONArray searchDataBySpatial(String wkt, int editTaskId, int type, JSONArray stages) throws Exception {
        try {
            TipsSelector selector = new TipsSelector();
            JSONArray array = selector.searchDataBySpatial(wkt,editTaskId, type,stages);
            return array;
        } catch (Exception e) {
            logger.error("按照范围查询tips出错："+e.getMessage(),e);
            throw e;
        }

    }


    
    /**
     * 任务卡片统计：taskType=1是质检任务
     */
    @Override
    public JSONObject getSubTaskStatsByWkt(int subtaskId, String wkt, int subTaskType,int handler, int isQuality) throws Exception {
        JSONObject result = new JSONObject();

        TipsSelector selector = new TipsSelector();

        //统计日编总量
        int total = selector.getTipsDayTotal(subtaskId, wkt, subTaskType,handler, isQuality, "total");

        //统计日编待作业
        int prepared = selector.getTipsDayTotal(subtaskId, wkt, subTaskType,handler, isQuality, "prepared");

        result.put("total", total);

        result.put("prepared", prepared);


        return result;
    }


    @Override
    public void tips2Aumark(JSONObject parameter)throws Exception  {

        try{

            if (parameter==null||parameter.isEmpty()) {

                throw new IllegalArgumentException("参数错误:数据parameter不能为空。");
            }
            Tips2Aumark tips2AuMark=new Tips2Aumark(parameter);

            tips2AuMark.validateParamAndInit();

            Thread newThread=new Thread(tips2AuMark);

            newThread.start();

            logger.debug("进入Api:tips2Aumark,调用run()");
        }catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw e;
        }



    }

    class Tips2Aumark implements Runnable{
        JSONObject parameter = null;
        //外业库信息
        String auip =null;
        String auuser =null;
        String aupw =null;
        String auport =null;
        String ausid=null;
        //gdb参考库
        String gdbId =null;
        String managerId =null;
        //  List<String> gridList =null;

        List<Integer> collectTaskIds =new ArrayList<Integer>(); //中线任务号
        JSONObject taskInfo =null;
        String types=null;
        long phaseId =0;
        
        int taskType=0;

        Tips2Aumark(JSONObject parameter){
            this.parameter=parameter;
        }

        /**
         * @Description:参数验证（不放在异步中）
         * @author: y
         * @time:2017-2-27 上午11:46:33
         */
        public void validateParamAndInit() {

            logger.info("API,参数验证：");
            logger.debug("API,参数验证：");

            //外业库信息
            auip = parameter.getString("au_db_ip");

            if (auip==null||auip.isEmpty()) {
                throw new IllegalArgumentException("参数错误:au_db_ip不能为空");
            }

            auuser = parameter.getString("au_db_username");

            if (auuser==null||auuser.isEmpty()) {
                throw new IllegalArgumentException("参数错误:au_db_username不能为空");
            }

            aupw = parameter.getString("au_db_password");

            if (aupw==null||aupw.isEmpty()) {
                throw new IllegalArgumentException("参数错误:au_db_password不能为空");
            }

            ausid = parameter.getString("au_db_sid");

            if (ausid==null||ausid.isEmpty()) {
                throw new IllegalArgumentException("参数错误:au_db_sid不能为空");
            }

            auport = parameter.getString("au_db_port");

            if (auport==null||auport.isEmpty()) {
                throw new IllegalArgumentException("参数错误:au_db_port不能为空");
            }

            //gdb参考库
            gdbId = parameter.getString("gdbid");

            if (gdbId==null||gdbId.isEmpty()) {
                throw new IllegalArgumentException("参数错误:参考库gdbId不能为空");
            }

            //grid，types
            // String grids = parameter.getString("grids");
/*             JSONArray gridsArray = parameter.getJSONArray("grids");
             gridList = JSONArray.toList(gridsArray,new String(),new JsonConfig());

             if (grids==null||grids.isEmpty()) {
                 throw new IllegalArgumentException("参数错误:grids不能为空");
             }
             if (gridList.isEmpty()||gridList.size()==0) {
                 throw new IllegalArgumentException("参数错误:grids不能为空");
             }*/
            
            if(!parameter.containsKey("task_type")){
                throw new IllegalArgumentException("参数错误:task_type不能为空");
            }
            
            taskType=parameter.getInt("task_type");
            
            //如果是快线任务号，则需要出传递快线项目下的所有采集任务号
            if(taskType==TaskType.Q_TASK_TYPE){
            	
            	  if(!parameter.containsKey("collectTaskIds")){
                      throw new IllegalArgumentException("参数错误:快线任务，collectTaskIds不能为空");
                  }
                  JSONArray collectArray = parameter.getJSONArray("collectTaskIds");
                  
                  if(collectArray==null|| collectArray.size()==0){
                	  
                	  throw new IllegalArgumentException("参数错误:快线任务，collectTaskIds不能为空");
                  }

                  for (Object object : collectArray) {
                      collectTaskIds.add(Integer.valueOf(object.toString()));
                  }
                  
            }


           

            types = parameter.getString("types");

            taskInfo = parameter.getJSONObject("taskInfo");

            if (taskInfo==null||taskInfo.isEmpty()) {
                throw new IllegalArgumentException("参数错误:任务信息参数不能为空");
            }else{
                if (!taskInfo.containsKey("manager_id")) {
                    throw new IllegalArgumentException("参数错误:manager_id不能为空");
                }
                String taskName = taskInfo.getString("imp_task_name");
                if (taskName==null||taskName.isEmpty()) {
                    throw new IllegalArgumentException("参数错误:中线采集任务名称不能为空");
                }
                String province = taskInfo.getString("province");
                if (province==null||province.isEmpty()) {
                    throw new IllegalArgumentException("参数错误:省份不能为空");
                }
                String city = taskInfo.getString("city");
                if (city==null||city.isEmpty()) {
                    throw new IllegalArgumentException("参数错误:市不能为空");
                }
                String district = taskInfo.getString("district");
                if (district==null||district.isEmpty()) {
                    throw new IllegalArgumentException("参数错误:区不能为空");
                }
                String jobNature= taskInfo.getString("job_nature");
                if (jobNature==null||jobNature.isEmpty()) {
                    throw new IllegalArgumentException("参数错误:作业性质不能为空");
                }
                String jobType = taskInfo.getString("job_type");
                if (jobType==null||jobType.isEmpty()) {
                    throw new IllegalArgumentException("参数错误:作业类型不能为空");
                }
            }

            //phaseId
            phaseId = parameter.getLong("phaseId");

            logger.info("API,参数验证通过！");
            logger.debug("API,参数验证通过！");

        }

        @Override
        public void run() {

            ManApi apiService=null;

            try{


                apiService= (ManApi) ApplicationContextUtil.getBean("manApi");
                int count=0;

                Tips2AuMarkApi api=new Tips2AuMarkApi();
                count=api.tips2Aumark(auip,ausid,auport,auuser,aupw,gdbId,collectTaskIds,types,taskInfo,taskType);

                if(count!=0){
                	
                	////返回 格式 : {detail:{ tipsNum:12}}
                	JSONObject  result=new JSONObject();
                	JSONObject  detail =new JSONObject();
                	detail.put("tipsNum", count);
                	result.put("detail", detail);
                	
                    apiService.updateJobProgress(phaseId,2,result.toString());
                    logger.debug("回调用manApi:updateJobProgress（"+phaseId+","+2+",转mark执行成功:共：)"+count+"条");
                    logger.info("回调用manApi:updateJobProgress（"+phaseId+","+2+",转mark执行成功:共：)"+count+"条");

                }else{
                    apiService.updateJobProgress(phaseId,4,"转mark执行成功,转出0条");
                    logger.debug("回调用manApi:updateJobProgress（"+phaseId+","+4+",转mark执行成功,转出0条)");
                    logger.info("回调用manApi:updateJobProgress（"+phaseId+","+4+",转mark执行成功,转出0条)");


                }

                logger.info("API,调用完成-------------------！");
                logger.debug("API,调用完成-------------------！");

            }catch(Exception e){
                logger.error("转mark出错："+e.getMessage(),e);
                logger.info("回调用manApi:updateJobProgress（"+phaseId+","+3+",转mark出错："+e.getMessage()+")");
                logger.debug("回调用manApi:updateJobProgress（"+phaseId+","+3+",转mark出错："+e.getMessage()+")");
                try {
                    apiService.updateJobProgress(phaseId,3,"转mark出错："+e.getMessage());
                } catch (Exception e1) {
                    logger.error("回掉接口出错："+e.getMessage(),e);
                }
            }
        }
    }





    /**
     * @Description: 快转中1：获取快线采集任务包含的tips的grids
     * @param collectTaskid:快线采集任务号
     * @return
     * @author: y
     * @time:2017-4-19 下午8:25:41
     */
    @Override
    public Set<Integer> getTipsGridsBySqTaskId(int collectTaskid)
            throws Exception {

        TipsSelector selector = new TipsSelector();
        
        //根据任务查询 任务下的所有tips的grid
        Set <Integer> grids=selector.getGridsListByTask(collectTaskid,com.navinfo.dataservice.dao.fcc.TaskType.Q_TASK_TYPE);

        return grids;
    }


    /**
     * @Description:快转中2：根据grid-taskMap批tips中线任务id
     * @param sQTaskId：快线任务号
     * @param gridMTaskMap
     * @throws Exception
     * @author: y
     * @time:2017-4-19 下午8:27:17
     */
    @Override
    public void batchUpdateSmTaskId(int sQTaskId,
                                    Map<Integer, Integer> gridMTaskMap) throws Exception {

        TipsOperator operate=new TipsOperator();

        operate.batchUpdateMTaskId(sQTaskId,gridMTaskMap);

    }



    /**
     * @Description: 动态调整：获取采集任务包含的tips的grids
     * @param subTaskid:采集子任务号
     * @param programType：任务类型 :1,中线（表示是中线的子任务号），4 。快线（表示是快线的子任务号）
     * @return
     * @throws Exception
     * @author: y
     * @time:2017-4-19 下午8:31:39
     */
    @Override
    public Set<Integer> getTipsGridsBySubtaskId(int subTaskid, int programType)
            throws Exception {

        TipsSelector selector = new TipsSelector();

        int taskType=0;

        if(programType==TaskType.Q_TASK_TYPE){

            taskType=TaskType.Q_SUB_TASK_TYPE;

        }else if(programType==TaskType.M_TASK_TYPE){

            taskType=TaskType.M_SUB_TASK_TYPE;
        }

        //根据任务查询 任务下的所有tips的grid
        Set<Integer> grids=selector.getGridsListByTask(subTaskid,taskType);

        return grids;

    }

    public static void main(String[] args) {
        String pa = "{\"gdbid\":41,\"au_db_ip\":\"192.168.3.227\",\"au_db_username\":\"gdb270_dcs_17sum_bj\",\"au_db_password\":\"gdb270_dcs_17sum_bj\",\"au_db_sid\":2,\"au_db_port\":1521,\"types\":\"1514\",\"phaseId\":55,\"grids\":[\"59552530\"],\"taskid\":{\"manager_id\":2,\"imp_task_name\":\"task_test_collect\",\"province\":\"城市\",\"city\":\"城市\",\"district\":\"测试Block_130\",\"job_nature\":\"更新\",\"job_type\":\"行人导航\"}}";
        JSONObject par = JSONObject.fromObject(pa);

        JSONObject taskInfo = par.getJSONObject("taskid");

        System.out.println(taskInfo);

        try {
            FccApiImpl fccApi = new FccApiImpl();
            //fccApi.tips2Aumark(par);
		/*	System.out.println("end");*/

		/*	int s_qTaskId=56;
			Set<Integer> list= fccApi.getTipsGridsBySqTaskId(s_qTaskId);
			System.out.println(list.size());
			for (Integer grid : list) {
				System.out.println(grid);
			}


			Map<Integer,Integer> gridMTaskMap=new HashMap<Integer, Integer>();
			gridMTaskMap.put(60560303, 55);
			gridMTaskMap.put(60561201, 51);
			gridMTaskMap.put(60561220, 50);

			fccApi.batchUpdateSmTaskId(56, gridMTaskMap);
			*/

            Set<Integer> grid=fccApi.getTipsGridsBySqTaskId(25);

            for (Integer grid2 : grid) {
                System.out.println(grid2);
            }


            System.out.println("快转中完成");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Map> getCollectTaskTipsStats(Set<Integer> collectTaskIds) throws Exception {
        if (collectTaskIds == null || collectTaskIds.size() == 0) {
            throw new IllegalArgumentException("参数错误:collectTaskIds不能为空。");
        }
        TipsSelector selector = new TipsSelector();
        return selector.getCollectTaskTipsStats(collectTaskIds);
    }

    @Override
    public void batchQuickTask(int taskId, int subtaskId, List<String> tips) throws Exception {
        if (taskId == 0) {
            throw new IllegalArgumentException("参数错误:taskId不能为空。");
        }
        if (subtaskId == 0) {
            throw new IllegalArgumentException("参数错误:subtaskId不能为空。");
        }
        if (tips == null || tips.size() == 0) {
            throw new IllegalArgumentException("参数错误:rowkey列表不能为空。");
        }
        TipsOperator tipsOperator = new TipsOperator();
        tipsOperator.batchQuickTask(taskId, subtaskId, tips);
    }

    @Override
    public void batchNoTaskDataByMidTask(String wkt,int midTaskId) throws Exception {
        if (midTaskId == 0) {
            throw new IllegalArgumentException("参数错误:midTaskId不能为空。");
        }
        if (StringUtils.isEmpty(wkt)) {
            throw new IllegalArgumentException("参数错误:wkt不能为空。");
        }
        TipsOperator tipsOperator = new TipsOperator();
        tipsOperator.batchNoTaskDataByMidTask(wkt, midTaskId);
    }

	@Override
	public Map<String, Integer> getCheckTaskCount(int checkSubTaskId)
			throws Exception {
		CheckTaskSelector selector=new CheckTaskSelector();
		
		Map<String, Integer> result=selector.queryTaskCountByTaskId(checkSubTaskId);
		
		return result;
	}

    @Override
    public Set<Integer> getTipsMeshIdSet(Set<Integer> collectTaskSet,int taskType) throws Exception {
        TipsSelector selector = new TipsSelector();
        Set<Integer> meshSet = selector.getTipsMeshIdSet(collectTaskSet,taskType);
        return meshSet;
    }

}
