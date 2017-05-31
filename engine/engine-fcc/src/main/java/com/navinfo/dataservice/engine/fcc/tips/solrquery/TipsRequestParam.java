package com.navinfo.dataservice.engine.fcc.tips.solrquery;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.fcc.SolrQueryUtils;
import com.navinfo.navicommons.geo.computation.GridUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.Set;

/**
 * Created by zhangjunfang on 2017/5/20.
 */
public class TipsRequestParam {

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

        Set<Integer> taskSet = this.getCollectIdsBySubTaskId(subtaskId);
        if (taskSet != null && taskSet.size() > 0) {
            this.getSolrIntSetQuery(builder, taskSet, "s_qTaskId");
        }

        //315过滤
        this.getFilter315(builder);

        if(workStatus == 0) {//待作业
            builder.append(" AND ((t_tipStatus:2");
            builder.append(" AND stage:(1 5 6)");
            builder.append(")");
            //接边Tips
            builder.append(" OR (s_sourceType:8002 AND stage:2 AND t_tipStatus:2 AND t_dEditStatus:0))");
        }else if(workStatus == 1) {//有问题待确认
            builder.append(" AND stage:2 AND t_dEditStatus:1");
        }else if(workStatus == 2) {//已作业
            builder.append(" AND stage:2 AND t_dEditStatus:2");
        }else if(workStatus == 9) {//全部
            StringBuilder allBuilder = new StringBuilder();
            allBuilder.append(" AND ");
            allBuilder.append("(");

            allBuilder.append("((t_tipStatus:2");
            allBuilder.append(" AND stage:(1 5 6)");
            allBuilder.append(")");
            allBuilder.append(" OR (s_sourceType:8002 AND stage:2 AND t_tipStatus:2 AND t_dEditStatus:0))");

            allBuilder.append(" OR ");

            allBuilder.append("(stage:2 AND t_dEditStatus:(1 2))");

            allBuilder.append(")");

            builder.append(allBuilder);
        }

        return builder.toString();
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

        Set<Integer> taskSet = this.getCollectIdsBySubTaskId(subtaskId);
        if (taskSet != null && taskSet.size() > 0) {
            this.getSolrIntSetQuery(builder, taskSet, "s_qTaskId");
        }

        //315过滤
        this.getFilter315(builder);

        builder.append(" AND s_sourceType:" + sourceType);


        if(workStatus == 0) {//待作业
            builder.append(" AND ((t_tipStatus:2");
            builder.append(" AND stage:(1 5 6)");
            builder.append(")");
            //接边Tips
            builder.append(" OR (s_sourceType:8002 AND stage:2 AND t_tipStatus:2 AND t_dEditStatus:0))");
        }else if(workStatus == 1) {//有问题待确认
            builder.append(" AND stage:2 AND t_dEditStatus:1");
        }else if(workStatus == 2) {//已作业
            builder.append(" AND stage:2 AND t_dEditStatus:2");
        }else if(workStatus == 9) {//全部
            StringBuilder allBuilder = new StringBuilder();
            allBuilder.append(" AND ");
            allBuilder.append("(");

            allBuilder.append("((t_tipStatus:2");
            allBuilder.append(" AND stage:(1 5 6)");
            allBuilder.append(")");
            allBuilder.append(" OR (s_sourceType:8002 AND stage:2 AND t_tipStatus:2 AND t_dEditStatus:0))");

            allBuilder.append(" OR ");

            allBuilder.append("(stage:2 AND t_dEditStatus:(1 2))");

            allBuilder.append(")");

            builder.append(allBuilder);
        }

        return builder.toString();
    }

    public String getTipsDayTotal(String parameter) throws Exception {
        JSONObject jsonReq = JSONObject.fromObject(parameter);

        //solr查询语句
        StringBuilder builder = new StringBuilder();

        if(jsonReq.containsKey("grids")) {
            JSONArray grids = jsonReq.getJSONArray("grids");
            String wkt = GridUtils.grids2Wkt(grids);
            builder.append("wkt:\"intersects(" + wkt + ")\"");
        }

        if(jsonReq.containsKey("wkt")) {
            String wkt = jsonReq.getString("wkt");
            if(builder.length() > 0) {
                builder.append(" AND wkt:\"intersects(" + wkt + ")\"");
            } else {
                builder.append("wkt:\"intersects(" + wkt + ")\"");
            }
        }

        if(jsonReq.containsKey("subtaskId")) {
            int subtaskId = jsonReq.getInt("subtaskId");
            Set<Integer> taskSet = this.getCollectIdsBySubTaskId(subtaskId);
            if (taskSet != null && taskSet.size() > 0) {
                this.getSolrIntSetQuery(builder, taskSet, "s_qTaskId");
            }
        }else if(jsonReq.containsKey("taskIds")) {
            JSONArray taskSet = jsonReq.getJSONArray("taskIds");
            if (taskSet != null && taskSet.size() > 0) {
                this.getSolrIntArrayQuery(builder, taskSet, "s_qTaskId");
            }
        }

        //315过滤
        this.getFilter315(builder);

        String statType = jsonReq.getString("statType");
        if(statType.equals("total")) {//日编所有Tips
            if(builder.length() > 0) {
                builder.append(" AND ");
            }
            builder.append("(");
            builder.append("(stage:(1 5 6) AND t_tipStatus:2)");
            builder.append(" OR ");
            builder.append("stage:2");
            builder.append(")");
        }else if(statType.equals("dFinished")) {//日编已完成
            if(builder.length() > 0) {
                builder.append(" AND ");
            }
            builder.append("stage:2 AND t_dEditStatus:2");
        }

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

    public String getByTileWithGap(String parameter, boolean filterDelete) {
        JSONObject jsonReq = JSONObject.fromObject(parameter);
        int x = jsonReq.getInt("x");
        int y = jsonReq.getInt("y");
        int z = jsonReq.getInt("z");
        int gap = jsonReq.getInt("gap");

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
            } else if ("m".equals(mdFlag)) {//月编
                stages.add(1);
                stages.add(3);
                stages.add(5);
                stages.add(6);
            }
        }

        StringBuilder builder = new StringBuilder();
        String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);
        builder.append("wktLocation:\"intersects(" + wkt + ")\" ");

        if (filterDelete) {
            // 过滤删除的数据
            builder.append(" AND -t_lifecycle:1 ");
        }

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
                    //20170510 增加中线有无过滤
                    addTaskFilterSql(noQFilter, builder);
                }else if(pType.equals("fc")) {//FC 预处理 钟小明
                    if(builder.length() > 0) {
                        builder.append(" AND ((s_sourceType:8001 AND t_tipStatus:(1 2)) OR (-s_sourceType:8001 AND t_tipStatus:2))");
                    }else{
                        builder.append("((s_sourceType:8001 AND t_tipStatus:(1 2)) OR (-s_sourceType:8001 AND t_tipStatus:2))");
                    }
                }
            }else {//web 刘哲
                if(builder.length() > 0) {
                    builder.append(" AND t_tipStatus:2");
                }else{
                    builder.append("t_tipStatus:2");
                }
            }
        }

        // 过滤315 web不显示的tips 20170118
        this.getFilter315(builder);

        return builder.toString();
    }

    public String getTipsCheck(String parameter) throws Exception{
        JSONObject jsonReq = JSONObject.fromObject(parameter);
        JSONArray grids = jsonReq.getJSONArray("grids");
        String wkt = GridUtils.grids2Wkt(grids);
        int subtaskId = jsonReq.getInt("subtaskId");

        //solr查询语句
        StringBuilder builder = new StringBuilder();

        builder.append("wkt:\"intersects(" + wkt + ")\"");

        Set<Integer> taskSet = this.getCollectIdsBySubTaskId(subtaskId);
        if (taskSet != null && taskSet.size() > 0) {
            this.getSolrIntSetQuery(builder, taskSet, "s_qTaskId");
        }

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
//        Set<Integer> taskSet = new HashSet<>();
//        taskSet.add(1);
//        taskSet.add(2);
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

    public static void main(String[] args) throws Exception {
        String parameter = "{\"grids\":[59567201],\"stage\":[1,5,6],\"subtaskId\":123,\"tipStatus\":2,\"type\":\"8001\"}";
        TipsRequestParam param = new TipsRequestParam();
        System.out.println(param.getSnapShot(parameter));
    }
}