package com.navinfo.dataservice.engine.fcc.tips.solrquery;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.fcc.SolrQueryUtils;
import com.navinfo.dataservice.dao.fcc.TaskType;
import com.navinfo.dataservice.dao.fcc.TipsWorkStatus;
import com.navinfo.navicommons.geo.computation.GridUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;

import java.util.Set;


/**
 * Created by zhangjunfang on 2017/5/20.
 */
public class TipsRequestParam {
    private static final Logger logger = Logger.getLogger(TipsRequestParam.class);

    //获取Tips个数列表 tip/getStats 接口参数
    public String getTipStat(String parameter) throws Exception{
        JSONObject jsonReq = JSONObject.fromObject(parameter);
        JSONArray grids = jsonReq.getJSONArray("grids");
        String wkt = GridUtils.grids2Wkt(grids);
        int workStatus = jsonReq.getInt("workStatus");
        int subtaskId = jsonReq.getInt("subtaskId");

        //solr查询语句
        StringBuilder builder = new StringBuilder();

        builder.append("wkt:\"intersects(" + wkt + ")\"");

        ManApi apiService=(ManApi) ApplicationContextUtil.getBean("manApi");
        Subtask subtask = apiService.queryBySubtaskId(subtaskId);
        Set<Integer> taskSet = this.getCollectIdsBySubTaskId(subtaskId);
        StringBuilder taskBuilder = null;
        if (taskSet != null && taskSet.size() > 0) {
            taskBuilder = this.getSolrIntSetQueryNoAnd(taskSet, "s_qTaskId");
        }
        
        //日编Grid粗编子任务作业时不展示FC预处理tips（8001）
        int subTaskType = subtask.getType();//3 grid粗编 4 区域粗编
        
        if(subTaskType != 3 && subTaskType != 4 && taskBuilder != null){
        	
        	 builder.append(" AND "+taskBuilder);
        }
        else if(subTaskType == 3) {//3 grid粗编,查8001之外的所有。 8002+其他（不包含8001）
        	
        	if(taskBuilder==null){
        		
        		builder.append("AND ( s_sourceType:8002 AND stage:(2 7) AND t_tipStatus:2)  ");//接边Tips
        	}else{
        		
        		builder.append("AND (( s_sourceType:8002 AND stage:(2 7) AND t_tipStatus:2)  OR  "+taskBuilder+" )");//接边Tips
        	}
        	
        	
        }else if(subTaskType == 4) {//4 区域粗编
        	//20170712修改。 如果是区域粗编子任务，tips列表中只统计显示FC预处理Tips（s_sourceType=8001）
        	builder.append(" AND s_sourceType:8001 AND stage:(2 5 7) AND t_tipStatus:2 ");//预处理提交
            
        }

/*        builder.append(" AND ");
        builder.append("(");
        builder.append(taskTypeBuilder);
        if(taskBuilder != null) {
            if(taskTypeBuilder.length() > 0) {
                builder.append(" OR ");
            }
            builder.append("(");
            builder.append(taskBuilder);
            builder.append(")");
        }
        builder.append(")");*/

        //315过滤
        this.getFilter315(builder);

        if(workStatus == TipsWorkStatus.PREPARED_WORKING) {//待作业
            builder.append(" AND (");
            builder.append("t_tipStatus:2 AND t_dEditStatus:0 AND stage:(1 2 5 6)");
            builder.append(")");
        }else if(workStatus == TipsWorkStatus.WORK_HAS_PROBLEM) {//有问题待确认
            builder.append(" AND stage:2 AND t_dEditStatus:1");
        }else if(workStatus == TipsWorkStatus.WORK_HAS_FINISHED) {//已作业
            builder.append(" AND stage:2 AND t_dEditStatus:2");
        }else if(workStatus == TipsWorkStatus.ALL) {//全部
            StringBuilder allBuilder = new StringBuilder();
            allBuilder.append(" AND ");
            allBuilder.append("(");

            allBuilder.append("(");
            allBuilder.append("t_tipStatus:2 AND t_dEditStatus:0 AND stage:(1 2 5 6)");
            allBuilder.append(")");

            allBuilder.append(" OR ");

            allBuilder.append("(stage:2 AND t_dEditStatus:(1 2))");

            allBuilder.append(")");

            builder.append(allBuilder);
        }//1.日编待质检tips：取stage=7，且t_dEditStatus=0
        else if(workStatus == TipsWorkStatus.PREPARED_CHECKING){

            builder.append(" AND stage:7 AND t_dEditStatus:0 ");

        }
        //日编已质检tips：取stage=7，且t_dEditStatus=2
        else if(workStatus == TipsWorkStatus.CHECK_HAS_FINISHED){

            builder.append(" AND stage:7 AND t_dEditStatus:2 ");


        }
        //③日编质检有问题待确认tips:取stage=7，且t_dEditStatus=1
        else if(workStatus == TipsWorkStatus.CHECK_HAS_PROBLEM){

            builder.append(" AND stage:7 AND t_dEditStatus:1 ");

        }else if(workStatus == TipsWorkStatus.CHECK_ALL){

            builder.append(" AND stage:7  ");
        }


        logger.info("getTipStat:" + builder.toString());
        return builder.toString();
    }


    /**
     * 质检查询条件
     * @param worker
     * @param checker
     * @param workStatus
     * @param rowkeyList
     * @return
     * @throws Exception
     */
    public String assambleSqlForCheckQuery(int worker,int checker,int workStatus,JSONArray rowkeyList) throws Exception{

        //solr查询语句
        StringBuilder builder = new StringBuilder();

        //1.日编待质检tips：取stage=2，且t_dEditStatus=2，且handler=质检子任务对应的日编子任务所分配的作业员ID的tips；
        if(workStatus == TipsWorkStatus.PREPARED_CHECKING){

            builder.append("  stage:2 AND t_dEditStatus:2 and handler:"+worker+"");

            this.getSolrStringArrayQuery(builder,rowkeyList, "id");
        }
        //日编已质检tips：取stage=7，且t_dEditStatus=2，且handler=质检子任务对应的质检员ID；
        else if(workStatus == TipsWorkStatus.CHECK_HAS_FINISHED){

            builder.append("  stage:7 AND t_dEditStatus:2 and handler:"+checker+"");

            this.getSolrStringArrayQuery(builder,rowkeyList, "id");

        }
        //③日编质检有问题待确认tips: 取stage=7，且t_dEditStatus=1，且handler=质检子任务对应的质检员ID；
        else if(workStatus == TipsWorkStatus.CHECK_HAS_PROBLEM){

            builder.append("  stage:7 AND t_dEditStatus:1 and handler:"+checker+"");

            this.getSolrStringArrayQuery(builder,rowkeyList, "id");
        }

        logger.info("assambleSqlForCheckQuery:" + builder.toString());
        return builder.toString();
    }



    /**
     * @Description:过滤条件组装（质检）
     * @param grids
     * @param workStatus:作业状态
     * @param subtaskId：作业子任务号
     * @param woker：作业员id
     * @param cheker：质检员id
     * @param rowkeyList：已抽取的tipsrowkey
     * @return
     * @throws Exception
     * @author: y
     * @time:2017-5-26 上午11:49:09
     */
    public String getQueryFilterSqlForCheck(int workStatus,int subtaskId,int woker,int cheker,JSONArray rowkeyList) throws Exception{

        //solr查询语句
        StringBuilder builder = new StringBuilder("select * from tips_index where ");
        

        //2.任务号过滤同时补充接边Tips和预处理Tips
        Set<Integer> taskSet = this.getCollectIdsBySubTaskId(subtaskId);
        StringBuilder taskBuilder = null;
        if (taskSet != null && taskSet.size() > 0) {
            taskBuilder = this.getSolrIntSetQueryNoAnd(taskSet, "s_qTaskId");
        }
        
        builder.append("(");
        builder.append("(s_sourceType in ('8001','8002') AND t_tipStatus=2)");//接边Tips，预处理提交
        if(taskBuilder != null) {
            builder.append(" OR ");
            builder.append("(");
            builder.append(taskBuilder);
            builder.append(")");
        }
        builder.append(")");

        //3.315过滤
        this.getFilter315(builder);

        //4.状态过滤（状态显示隐藏）
        this.getWokerStatusFilterQuery(woker, cheker, workStatus, builder,rowkeyList);

        builder.append(" AND ");
        //builder.append("wkt:\"intersects(" + wkt + ")\"");
        builder.append(" sdo_filter(wkt,sdo_geometry(:1,8307)) = 'TRUE'");

        logger.info("getQueryFilterSqlForCheck:" + builder.toString());
        return builder.toString();
    }





    /**
     * @Description:状态过滤（状态显示隐藏-通用）
     * @param woker
     * @param cheker
     * @param workStatus
     * @param builder
     * @author: y
     * @param rowkeyList
     * @time:2017-5-26 上午11:44:16
     */
    private void getWokerStatusFilterQuery(int woker, int cheker,
                                           int workStatus, StringBuilder builder, JSONArray rowkeyList) {
        //1.日编待质检tips：取stage=2，且t_dEditStatus=2，且handler=质检子任务对应的日编子任务所分配的作业员ID的tips；

        builder.append(" AND stage=2 AND t_dEditStatus=2 AND handler="+woker+"");

    }


    public String getSnapShot(String parameter) throws Exception{
        JSONObject jsonReq = JSONObject.fromObject(parameter);
        JSONArray grids = jsonReq.getJSONArray("grids");
        String wkt = GridUtils.grids2Wkt(grids);
        int workStatus = jsonReq.getInt("workStatus");
        int subtaskId = jsonReq.getInt("subtaskId");
        String sourceType = jsonReq.getString("type");

        //solr查询语句
        StringBuilder builder = new StringBuilder();

        builder.append("wkt:\"intersects(" + wkt + ")\"");

        //任务过滤,疑问taskBuilder为什么会为空？？？ 其实为空是有问题的
        Set<Integer> taskSet = this.getCollectIdsBySubTaskId(subtaskId);
        StringBuilder taskBuilder = null;
        if (taskSet != null && taskSet.size() > 0) {
            taskBuilder = this.getSolrIntSetQueryNoAnd(taskSet, "s_qTaskId");
        }
        
        ManApi apiService=(ManApi) ApplicationContextUtil.getBean("manApi");
        Subtask subtask = apiService.queryBySubtaskId(subtaskId);
        //日编Grid粗编子任务作业时不展示FC预处理tips（8001）
        int subTaskType = subtask.getType();//3 grid粗编 4 区域粗编
        
        if(subTaskType!=3&subTaskType!=4){
        	
        	builder.append( " AND "+taskBuilder);
        }
       //3 grid粗编,查8001之外的所有。 8002+其他（不包含8001）
        else if(subTaskType == 3) {
        	if(taskBuilder==null){
        		builder.append("AND (s_sourceType:8002 AND stage:(2 7) AND t_tipStatus:2)");//接边Tips
        	}else{
        		
        		builder.append("AND (( s_sourceType:8002 AND stage:(2 7) AND t_tipStatus:2)  OR  "+taskBuilder+" )");//接边Tips
        	}
        	
        }
       //4 区域粗编
        else if(subTaskType == 4) {
        	//20170712修改。 如果是区域粗编子任务，tips列表中只统计显示FC预处理Tips（s_sourceType=8001）
        	builder.append(" AND s_sourceType:8001 AND stage:(2 5 7) AND t_tipStatus:2 ");//预处理提交
            
        }
        
        

        //315过滤
        this.getFilter315(builder);

        builder.append(" AND s_sourceType:" + sourceType);

        if(workStatus == TipsWorkStatus.PREPARED_WORKING) {//待作业
            builder.append(" AND (");
            builder.append("t_tipStatus:2 AND t_dEditStatus:0 AND stage:(1 2 5 6)");
            builder.append(")");
        }else if(workStatus ==TipsWorkStatus.WORK_HAS_PROBLEM ) {//有问题待确认
            builder.append(" AND stage:2 AND t_dEditStatus:1");
        }else if(workStatus == TipsWorkStatus.WORK_HAS_FINISHED) {//已作业
            builder.append(" AND stage:2 AND t_dEditStatus:2");
        }else if(workStatus == TipsWorkStatus.ALL) {//全部
            StringBuilder allBuilder = new StringBuilder();
            allBuilder.append(" AND ");
            allBuilder.append("(");

            allBuilder.append("(");
            allBuilder.append("t_tipStatus:2 AND t_dEditStatus:0 AND stage:(1 2 5 6)");
            allBuilder.append(")");

            allBuilder.append(" OR ");

            allBuilder.append("(stage:2 AND t_dEditStatus:(1 2))");

            allBuilder.append(")");

            builder.append(allBuilder);
        }
        //1.日编待质检tips：取stage=7，且t_dEditStatus=0
        else if(workStatus == TipsWorkStatus.PREPARED_CHECKING){

            builder.append(" AND  stage:7 AND t_dEditStatus:0 ");

        }
        //日编已质检tips：取stage=7，且t_dEditStatus=2
        else if(workStatus == TipsWorkStatus.CHECK_HAS_FINISHED){

            builder.append(" AND stage:7 AND t_dEditStatus:2 ");


        }
        //③日编质检有问题待确认tips:取stage=7，且t_dEditStatus=1
        else if(workStatus == TipsWorkStatus.CHECK_HAS_PROBLEM){

            builder.append(" AND  stage:7 AND t_dEditStatus:1 ");

        }else if(workStatus == TipsWorkStatus.CHECK_ALL){

            builder.append(" AND stage:7  ");
        }

        logger.info("getSnapShot:" + builder.toString());
        return builder.toString();
    }

    public String getTipsDayTotal(String parameter) throws Exception {
        JSONObject jsonReq = JSONObject.fromObject(parameter);
        
        int taskType=jsonReq.getInt("taskType");
        int handler=jsonReq.getInt("handler");
        
        //solr查询语句
        StringBuilder builder = new StringBuilder();

/*        if(jsonReq.containsKey("grids")) {
            JSONArray grids = jsonReq.getJSONArray("grids");
            String wkt = GridUtils.grids2Wkt(grids);
            builder.append("wkt:\"intersects(" + wkt + ")\"");
        }*/

        if(jsonReq.containsKey("wkt")) {
            String wkt = jsonReq.getString("wkt");
            if(builder.length() > 0) {
                builder.append(" AND wkt:\"intersects(" + wkt + ")\"");
            } else {
                builder.append("wkt:\"intersects(" + wkt + ")\"");
            }
        }

/*        if(jsonReq.containsKey("subtaskId")) {
            int subtaskId = jsonReq.getInt("subtaskId");
            Set<Integer> taskSet = this.getCollectIdsBySubTaskId(subtaskId);
            if (taskSet != null && taskSet.size() > 0) {
                this.getSolrIntSetQuery(builder, taskSet, "s_qTaskId");
            }
        }
        
        else */
        if(jsonReq.containsKey("collectTaskIds")) {
            JSONArray taskSet = jsonReq.getJSONArray("collectTaskIds");
            if (taskSet != null && taskSet.size() > 0) {
                this.getSolrIntArrayQuery(builder, taskSet, "s_qTaskId");
            }
        }

        //315过滤
        this.getFilter315(builder);

        String statType = jsonReq.getString("statType");
        
        //任务类型，1是质检任务
        if(taskType!=1){
        	
        	  //Tips总量：根据子任务grid范围、项目ID、且stage=1，2，5，6，7， 且t_tipStatus=2；
            if(statType.equals("total")) {
            	
                if(builder.length() > 0) {
                    builder.append(" AND ");
                }
                builder.append(" stage:(1 2 5 6 7) AND t_tipStatus:2");
       /*         builder.append("(");
                builder.append("(stage:(1 5 6) AND t_tipStatus:2)");
                builder.append(" OR ");
                builder.append("stage:2");
                builder.append(")");*/
            }
            //Tips待作业量：根据子任务grid范围、项目ID、且stage=1、2、5、6且t_tipStatus=2 && t_dEditStatus不等于2；
            else if(statType.equals("prepared")) {
                if(builder.length() > 0) {
                    builder.append(" AND ");
                }
                builder.append("stage:(1 2 5 6 ) AND t_tipStatus:2 AND -t_dEditStatus:2");
         
            }
        	
        }
        //质检任务
        else if(taskType==1){
        	
        	  //Tips 总量：显示抽取到的质检Tips总量（stage= 7且handler=质检子任务对应分配的质检员ID的tips）
            if(statType.equals("total")) {
            	
                if(builder.length() > 0) {
                    builder.append(" AND ");
                }
                builder.append(" stage:7 AND handler:"+handler);
            }
            
            //Tips 待作业量：显示抽取到的未质检作业的tips量（stage= 7且handler=质检子任务对应分配的质检员ID的tips 且t_dEditStatus不等于2）
            else if(statType.equals("prepared")) {
                if(builder.length() > 0) {
                    builder.append(" AND ");
                }
                builder.append(" stage:7 AND handler:"+handler+" AND -t_dEditStatus:2 ");
            }
        	
        }
        
     

        logger.info("getTipsDayTotal:" + builder.toString());
        return builder.toString();
    }

//     20170523 和于桐万冲确认该接口取消
//    public String getStatusByWkt(String parameter, boolean filterDelete) {
//        JSONObject jsonReq = JSONObject.fromObject(parameter);
//        String wkt = jsonReq.getString("wkt");
//
//        StringBuilder builder = new StringBuilder();
//        builder.append("wktLocation:\"intersects(" + wkt + ")\" ");
//
//        if (filterDelete) {
//            // 过滤删除的数据
//            builder.append(" AND -t_lifecycle:1 ");
//        }
//
//        this.getFilter315(builder);
//
//        return builder.toString();
//    }

    public String getByTileWithGap(String parameter) throws Exception {
        JSONObject jsonReq = JSONObject.fromObject(parameter);
        int x = jsonReq.getInt("x");
        int y = jsonReq.getInt("y");
        int z = jsonReq.getInt("z");
        int gap = jsonReq.getInt("gap");


        int subtaskId=0; //web编辑才有

        Subtask subtask = null;
        int subTaskType = 0;//3 grid粗编 4 区域粗编

        if(jsonReq.containsKey("subtaskId")){
        	ManApi apiService=(ManApi) ApplicationContextUtil.getBean("manApi");
        	subtaskId=jsonReq.getInt("subtaskId");
        	subtask=apiService.queryBySubtaskId(subtaskId);
        	subTaskType = subtask.getType();//3 grid粗编 4 区域粗编
        }



        String mdFlag = null;
        if(jsonReq.containsKey("mdFlag")) {
            mdFlag = jsonReq.getString("mdFlag");
        }
        String pType = null;
        if(jsonReq.containsKey("pType")){
            pType = jsonReq.getString("pType");
        }

        JSONArray types = new JSONArray();
        if (jsonReq.containsKey("types")) {
            types = jsonReq.getJSONArray("types");
        }

        JSONArray noQFilter = new JSONArray();
        if (jsonReq.containsKey("noQFilter")) {
            noQFilter = jsonReq.getJSONArray("noQFilter");
        }

        JSONArray stages = new JSONArray();
        if(StringUtils.isNotEmpty(pType)) {
            if(pType.equals("sl")) {//矢量化 赵航
                stages.add(0);
                stages.add(1);
                stages.add(2);
                stages.add(6);
            }else if(pType.equals("ms")) {//生产管理 万冲
                stages.add(0);
                stages.add(1);
                stages.add(2);
                stages.add(3);
                stages.add(6);
                stages.add(7);
                stages.add(8);
            }else if(pType.equals("fc")) {//FC 预处理 钟小明
                stages.add(1);
                stages.add(2);
                stages.add(3);
                stages.add(5);
                stages.add(6);
            }
        }else {//web 刘哲
            if ("d".equals(mdFlag)) {//日编
                stages.add(1);
                stages.add(2);
                stages.add(5);
                stages.add(6);
                stages.add(7);
            } else if ("m".equals(mdFlag)) {//月编
                stages.add(1);
                stages.add(3);
                stages.add(5);
                stages.add(6);
                stages.add(7);
            }
        }

        StringBuilder builder = new StringBuilder();
        String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);
        builder.append("wktLocation:\"intersects(" + wkt + ")\" ");

        if (types.size() > 0) {
            this.getSolrStringArrayQuery(builder, types, "s_sourceType");
        }

        if (stages.size() > 0) {
            this.getSolrIntArrayQuery(builder, stages, "stage");
            if(StringUtils.isNotEmpty(pType)) {
                if(pType.equals("sl")) {//矢量化 赵航
                }else if(pType.equals("ms")) {//生产管理 万冲
                    if(builder.length() > 0) {
                        builder.append(" AND t_tipStatus:2");
                    }else{
                        builder.append("t_tipStatus:2");
                    }
                    //20170615 过滤内业Tips
                    builder.append(" AND ");
                    builder.append("-s_sourceType:80*");
                    //20170510 增加中线有无过滤
                    addTaskFilterSql(noQFilter, builder);
                }else if(pType.equals("fc")) {//FC 预处理 钟小明
                    if(builder.length() > 0) {
                        builder.append(" AND (t_tipStatus:2 OR (s_sourceType:8001 AND t_tipStatus:1))");
                    }else{
                        builder.append("(t_tipStatus:2 OR (s_sourceType:8001 AND t_tipStatus:1))");
                    }
                }
            }else {//web 刘哲
                StringBuilder webBuilder = new StringBuilder();
                JSONArray workStatus = null;
                if(jsonReq.containsKey("workStatus")) {
                    workStatus = jsonReq.getJSONArray("workStatus");
                }

                //状态过滤
                if(workStatus == null || workStatus.contains(9)
                        || (workStatus.contains(0) && workStatus.contains(1) && workStatus.contains(2))) {
                    if(webBuilder.length() > 0) {
                        webBuilder.append(" OR ");
                    }
                    webBuilder.append("(t_tipStatus:2)");
                }else {
                    if (workStatus.contains(0)) {

                        if (webBuilder.length() > 0) {
                            webBuilder.append(" OR ");
                        }

                        webBuilder.append("(");
                        webBuilder.append("(");
                        webBuilder.append("t_tipStatus:2 AND t_dEditStatus:0 AND stage:(1 2 5 6)");
                        webBuilder.append(")");

                        //待质检的tips
                        webBuilder.append(" OR (stage:7 AND t_dEditStatus:0 AND t_tipStatus:2)");

                        webBuilder.append(	")");


                    }
                    if (workStatus.contains(1)) {
                        if (webBuilder.length() > 0) {
                            webBuilder.append(" OR ");
                        }
                        webBuilder.append("(stage:(2 7) AND t_dEditStatus:1)");
                    }
                    if (workStatus.contains(2)) {
                        if (webBuilder.length() > 0) {
                            webBuilder.append(" OR ");
                        }
                        webBuilder.append("(stage:(2 7) AND t_dEditStatus:2)");
                    }
                }

                if(webBuilder.length() > 0) {
                    if(builder.length() > 0) {
                        builder.append(" AND ");
                    }
                    builder.append("(");
                    builder.append(webBuilder);
                    builder.append(")");
                }

                //类型过滤
                //日编Grid粗编子任务作业时不展示FC预处理tips（8001）
                //3 grid粗编,查8001之外的所有。 8002+其他（不包含8001）
                if(subTaskType == 3) {
                	builder.append("-s_sourceType:8001 ");//接边Tips
                }
                else if(subTaskType == 4) {//4 区域粗编
                	builder.append(" AND s_sourceType:8001  ");//预处理提交

                }

            }
        }

        System.out.println(builder.toString());
        // 过滤315 web不显示的tips 20170118
        this.getFilter315(builder);

        logger.info("getByTileWithGap:" + builder.toString());
        return builder.toString();
    }

    public String getTipsCheck(String parameter) throws Exception{
        JSONObject jsonReq = JSONObject.fromObject(parameter);
//        JSONArray grids = jsonReq.getJSONArray("grids");
//        String wkt = GridUtils.grids2Wkt(grids);
        int subtaskId = jsonReq.getInt("subTaskId");

        //solr查询语句
        StringBuilder builder = new StringBuilder();

//        builder.append("wkt:\"intersects(" + wkt + ")\"");

        int programType = jsonReq.getInt("programType");

        if(programType == TaskType.PROGRAM_TYPE_Q) {//快线
            builder.append("s_qSubTaskId:");
            builder.append(subtaskId);
        }else if(programType == TaskType.PROGRAM_TYPE_M) {//中线
            builder.append("s_mSubTaskId:");
            builder.append(subtaskId);
        }

        if(jsonReq.containsKey("type")) {
            builder.append(" AND ");
            builder.append(" s_sourceType:2001 ");
            builder.append(" AND ");
            builder.append(" t_lifecycle:3 ");
        }

        logger.info("getTipsCheck:" + builder.toString());
        return builder.toString();
    }

    /**
     * 根据子任务号获取采集任务ID
     * @param subtaskId
     * @return
     * @throws Exception
     */
    private Set<Integer> getCollectIdsBySubTaskId(int subtaskId) throws Exception {
        ManApi manApi = (ManApi) ApplicationContextUtil.getBean("manApi");
        Set<Integer> taskSet = manApi.getCollectTaskIdByDaySubtask(subtaskId);
        return taskSet;
    }

    /**
     * 过滤315不显示的Tips
     * builder
     * @return
     */
    private StringBuilder getFilter315(StringBuilder builder) {
        if (StringUtils.isNotEmpty(SolrQueryUtils.NOT_DISPLAY_TIP_FOR_315_TYPES_FILER_SQL)) {
            if (builder.length() == 0) {
                builder.append(SolrQueryUtils.NOT_DISPLAY_TIP_FOR_315_TYPES_FILER_SQL);
            } else {
                builder.append(" AND "
                        + SolrQueryUtils.NOT_DISPLAY_TIP_FOR_315_TYPES_FILER_SQL);
            }
        }
        return builder;
    }

    private StringBuilder getSolrIntSetQuery(StringBuilder builder, Set<Integer> intSet, String fieldName) {
        if(builder.length() > 0) {
            builder.append(" AND");
        }
        builder.append(" " + fieldName + ":(");
        int i = 0;
        for (Integer filedValue : intSet) {
            if (i > 0) {
                builder.append(" ");
            }
            builder.append(filedValue);
            i++;
        }
        builder.append(")");
        return builder;
    }

    private StringBuilder getSolrIntSetQueryNoAnd(Set<Integer> intSet, String fieldName) {
        StringBuilder builder = new StringBuilder();
        builder.append(fieldName + " in (");
        int i = 0;
        for (Integer filedValue : intSet) {
            if (i > 0) {
                builder.append(" , ");
            }
            builder.append(filedValue);
            i++;
        }
        builder.append(")");
        return builder;
    }

    private StringBuilder getSolrIntArrayQuery(StringBuilder builder, JSONArray intArray, String fieldName) {
        if(builder.length() > 0) {
            builder.append(" AND");
        }
        builder.append(" " + fieldName + ":(");
        for (int i = 0; i < intArray.size(); i++) {
            int fieldValue = intArray.getInt(i);
            if (i > 0) {
                builder.append(" ");
            }
            builder.append(fieldValue);
        }
        builder.append(")");
        return builder;
    }

    private StringBuilder getSolrStringArrayQuery(StringBuilder builder, JSONArray stringArray, String fieldName) {

        if(stringArray!=null){

            if(builder.length() > 0) {
                builder.append(" AND");
            }
            builder.append(" " + fieldName + ":(");
            for (int i = 0; i < stringArray.size(); i++) {
                String fieldValue = stringArray.getString(i);
                if (i > 0) {
                    builder.append(" ");
                }
                builder.append(fieldValue);
            }
            builder.append(")");
        }

        return builder;
    }

    /**
     * 中线有无过滤
     * @param nQFilter
     * @param builder
     */
    private void addTaskFilterSql(JSONArray nQFilter, StringBuilder builder) {
        if ((nQFilter != null) && (nQFilter.size() > 0)) {
            builder.append(" AND s_qTaskId:0");
            if (nQFilter.size() < 2) {
                int flag = nQFilter.getInt(0);
                if (flag == 1)
                    builder.append(" AND -s_mTaskId:0");
                else if (flag == 2)
                    builder.append(" AND s_mTaskId:0");
            }
        }
    }

    public String getTipsCheckUnCommit(String parameter) throws Exception{
        JSONObject jsonReq = JSONObject.fromObject(parameter);
        int subtaskId = jsonReq.getInt("subTaskId");

        //solr查询语句
        StringBuilder builder = new StringBuilder();

        builder.append("-t_tipStatus:2");

        int programType = jsonReq.getInt("programType");

        if(programType == TaskType.PROGRAM_TYPE_Q) {//快线
            builder.append(" AND ");
            builder.append("s_qSubTaskId:");
            builder.append(subtaskId);
        }else if(programType == TaskType.PROGRAM_TYPE_M) {//中线
            builder.append(" AND ");
            builder.append("s_mSubTaskId:");
            builder.append(subtaskId);
        }
        logger.info("getTipsCheckUnCommit:" + builder.toString());
        return builder.toString();
    }

    public String getTipsCheckTotal(String parameter) throws Exception{
        JSONObject jsonReq = JSONObject.fromObject(parameter);
        int subtaskId = jsonReq.getInt("subTaskId");

        //solr查询语句
        StringBuilder builder = new StringBuilder("select * from tips_index where ");

        int tipsStatus = jsonReq.getInt("tipStatus");
        builder.append("t_tipStatus=");
        builder.append(tipsStatus);

        int programType = jsonReq.getInt("programType");

        if(programType == TaskType.PROGRAM_TYPE_Q) {//快线
            builder.append(" AND ");
            builder.append("s_qSubTaskId=");
            builder.append(subtaskId);
        }else if(programType == TaskType.PROGRAM_TYPE_M) {//中线
            builder.append(" AND ");
            builder.append("s_mSubTaskId=");
            builder.append(subtaskId);
        }
        logger.info("getTipsCheckTotal:" + builder.toString());
        return builder.toString();
    }

    public static void main(String[] args) throws Exception {
        String parameter = "{\"subtaskId\":517,\"grids\":[60561412,60561413,60561410,60561411,60561420,60561421,60561422,60561423,60561431\n" +
                ",60561430,60561433,60561400,60561432,60561401,60561402,60561403],\"mdFlag\":\"d\",\"workStatus\":0}";
        TipsRequestParam param = new TipsRequestParam();
        System.out.println(param.getTipStat(parameter));
    }
}
