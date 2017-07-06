package com.navinfo.dataservice.engine.fcc.service;

import com.navinfo.dataservice.api.fcc.iface.FccApi;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.fcc.TaskType;
import com.navinfo.dataservice.dao.fcc.check.operate.CheckTaskSelector;
import com.navinfo.dataservice.engine.fcc.tips.TipsOperator;
import com.navinfo.dataservice.engine.fcc.tips.TipsSelector;
import com.navinfo.nirobot.business.Tips2AuMarkApi;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

//    @Override
//    public JSONObject getSubTaskStats(JSONArray grids) throws Exception {
//        JSONObject result=new JSONObject();
//
//        if (grids==null||grids.isEmpty()) {
//
//            throw new IllegalArgumentException("参数错误:grids不能为空。");
//        }
//
//        TipsSelector selector = new TipsSelector();
//
//        //统计日编总量 stage=1
//        int total=selector.getTipsCountByStage(grids, 1);
//
//        //统计日编已完成量stage=2 and t_dStatus=1
//        int finished=selector.getTipsCountByStageAndTdStatus(grids,2,1);
//
//        result.put("total", total);
//
//        result.put("finished", finished);
//
//        return result;
//    }

    @Override
    public JSONObject getSubTaskStatsByWkt(String wkt, Set<Integer> collectTaskIds) throws Exception {
        JSONObject result = new JSONObject();

        if (wkt == null || wkt.isEmpty()) {

            throw new IllegalArgumentException("参数错误:wkt不能为空。");
        }

        TipsSelector selector = new TipsSelector();

        //统计日编总量 stage=1
        int total = selector.getTipsDayTotal(wkt, collectTaskIds, "total");

        //统计日编已完成量stage=2 and t_dStatus=1
        int finished = selector.getTipsDayTotal(wkt, collectTaskIds, "dFinished");

        result.put("total", total);

        result.put("finished", finished);

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
        int phaseId =0;

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

            if(!parameter.containsKey("collectTaskIds")){
                throw new IllegalArgumentException("参数错误:collectTaskIds不能为空");
            }
            JSONArray collectArray = parameter.getJSONArray("collectTaskIds");

            // collectTaskIds = JSONArray.toList(collectArray,new String(),new JsonConfig());

            for (Object object : collectArray) {
                collectTaskIds.add(Integer.valueOf(object.toString()));
            }


            if(collectTaskIds==null||collectTaskIds.isEmpty()){
                throw new IllegalArgumentException("参数错误:collectTaskIds不能为空");
            }


            types = parameter.getString("types");

            taskInfo = parameter.getJSONObject("taskid");

            if (taskInfo==null||taskInfo.isEmpty()) {
                throw new IllegalArgumentException("参数错误:任务信息参数不能为空");
            }else{
                String managerId = taskInfo.getString("manager_id");
                if (managerId==null||managerId.isEmpty()) {
                    throw new IllegalArgumentException("参数错误:中线采集任务ID不能为空");
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
            phaseId = parameter.getInt("phaseId");

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
                count=api.tips2Aumark(auip,ausid,auport,auuser,aupw,gdbId,collectTaskIds,types,taskInfo);


                if(count!=0){
                    apiService.taskUpdateCmsProgress(phaseId,2,"转mark执行成功");
                    logger.debug("回调用manApi:taskUpdateCmsProgress（"+phaseId+","+2+",转mark执行成功)");
                    logger.info("回调用manApi:taskUpdateCmsProgress（"+phaseId+","+2+",转mark执行成功)");

                }else{
                    apiService.taskUpdateCmsProgress(phaseId,4,"转mark执行成功,转出0条");
                    logger.debug("回调用manApi:taskUpdateCmsProgress（"+phaseId+","+4+",转mark执行成功,转出0条)");
                    logger.info("回调用manApi:taskUpdateCmsProgress（"+phaseId+","+4+",转mark执行成功,转出0条)");


                }

                logger.info("API,调用完成-------------------！");
                logger.debug("API,调用完成-------------------！");

            }catch(Exception e){
                logger.error("转mark出错："+e.getMessage(),e);
                logger.info("回调用manApi:taskUpdateCmsProgress（"+phaseId+","+3+",转mark出错："+e.getMessage()+")");
                logger.debug("回调用manApi:taskUpdateCmsProgress（"+phaseId+","+3+",转mark出错："+e.getMessage()+")");
                try {
                    apiService.taskUpdateCmsProgress(phaseId,3,"转mark出错："+e.getMessage());
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
    public Set<Integer> getTipsMeshIdSet(Set<Integer> collectTaskSet) throws Exception {
        TipsSelector selector = new TipsSelector();
        Set<Integer> meshSet = selector.getTipsMeshIdSet(collectTaskSet);
        return meshSet;
    }

}
