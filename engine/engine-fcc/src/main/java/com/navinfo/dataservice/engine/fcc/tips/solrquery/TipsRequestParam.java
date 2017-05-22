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
        JSONArray stages = jsonReq.getJSONArray("stage");
        int subtaskId = jsonReq.getInt("subtaskId");

        //solr查询语句
        StringBuilder builder = new StringBuilder();

        builder.append("wkt:\"intersects(" + wkt + ")\"");

        if (stages.size() > 0) {
            this.getSolrIntArrayQuery(builder, stages, "stage");
        }

        Set<Integer> taskSet = this.getCollectIdsBySubTaskId(subtaskId);
        if (taskSet != null && taskSet.size() > 0) {
            this.getSolrIntSetQuery(builder, taskSet, "s_qTaskId");
        }

        //315过滤
        this.getFilter315(builder);

        //日编作业/质检状态
        if(jsonReq.containsKey("dEditStatus")) {
            int dEditStatus = jsonReq.getInt("dEditStatus");
            builder.append(" AND t_dEditStatus:" + dEditStatus);
        }

        //制作状态
        if(jsonReq.containsKey("tipStatus")) {
            int tipStatus = jsonReq.getInt("tipStatus");
            builder.append(" AND t_tipStatus:" + tipStatus);
            //接边Tips
            if(tipStatus == 2 && stages.contains(1) && stages.contains(5) && stages.contains(6)) {
                builder.insert(0, "(");
                builder.append(")");
                builder.append(" OR (s_sourceType:8002 AND stage:2 AND t_tipStatus:" + tipStatus + " AND t_dEditStatus:0)");
            }
        }

        return builder.toString();
    }

    public String getSnapShot(String parameter) throws Exception{
        JSONObject jsonReq = JSONObject.fromObject(parameter);
        JSONArray grids = jsonReq.getJSONArray("grids");
        String wkt = GridUtils.grids2Wkt(grids);
        JSONArray stages = jsonReq.getJSONArray("stage");
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

        //日编作业/质检状态
        if(jsonReq.containsKey("dEditStatus")) {
            int dEditStatus = jsonReq.getInt("dEditStatus");
            builder.append(" AND t_dEditStatus:" + dEditStatus);
        }

        builder.append(" AND s_sourceType:" + sourceType);

        //制作状态
        if(jsonReq.containsKey("tipStatus")) {
            int tipStatus = jsonReq.getInt("tipStatus");
            if(sourceType.equals("8002")) {
                //接边Tips
                if(tipStatus == 2 && stages.contains(1) && stages.contains(5) && stages.contains(6)) {
                    builder.append(" AND stage:2");
                    builder.append(" AND t_tipStatus:" + tipStatus);
                    builder.append(" AND t_dEditStatus:0");
                }
            }else{
                if (stages.size() > 0) {
                    builder.append(" AND t_tipStatus:" + tipStatus);
                    this.getSolrIntArrayQuery(builder, stages, "stage");
                }
            }
        }

        return builder.toString();
    }

    public String getStatusByWkt(String parameter, boolean filterDelete) {
        JSONObject jsonReq = JSONObject.fromObject(parameter);
        String wkt = jsonReq.getString("wkt");

        StringBuilder builder = new StringBuilder();
        builder.append("wktLocation:\"intersects(" + wkt + ")\" ");

        if (filterDelete) {
            // 过滤删除的数据
            builder.append(" AND -t_lifecycle:1 ");
        }

        this.getFilter315(builder);

        return builder.toString();
    }

    public String getByTileWithGap(String parameter, boolean filterDelete) {
        JSONObject jsonReq = JSONObject.fromObject(parameter);
        int x = jsonReq.getInt("x");
        int y = jsonReq.getInt("y");
        int z = jsonReq.getInt("z");
        int gap = jsonReq.getInt("gap");
        String mdFlag = jsonReq.getString("mdFlag");

        JSONArray types = new JSONArray();
        if (jsonReq.containsKey("types")) {
            types = jsonReq.getJSONArray("types");
        }

        JSONArray stages = new JSONArray();
        if (jsonReq.containsKey("stage")) {
            stages = jsonReq.getJSONArray("stage");
        }

        JSONArray noQFilter = new JSONArray();
        if (jsonReq.containsKey("noQFilter")) {
            noQFilter = jsonReq.getJSONArray("noQFilter");
        }

        StringBuilder builder = new StringBuilder();
        String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);
        builder.append("wktLocation:\"intersects(" + wkt + ")\" ");

        if (filterDelete) {
            // 过滤删除的数据
            builder.append(" AND -t_lifecycle:1 ");
        }

        if (stages.size() > 0) {
            this.getSolrIntArrayQuery(builder, stages, "stage");
        }

        if (types.size() > 0) {
            this.getSolrStringArrayQuery(builder, types, "s_sourceType");
        }

        //20170510 增加中线有无过滤
        addTaskFilterSql(noQFilter, builder);

        //TODO 不是预处理，则需要过滤预处理没提交的tips,t_pStatus=0是没有提交的
//        if (!isPre) {
//
//            if ("".equals(builder.toString())) {
//                builder.append(" -(t_pStatus:0 AND s_sourceType:8001)");
//
//                builder.append(" -(t_fStatus:0 AND stage:6 )");  //情报矢量化的  不查询t_fStatus为0的
//            } else {
//                builder.append(" AND -(t_pStatus:0 AND s_sourceType:8001)");
//
//                builder.append(" AND -(t_fStatus:0 AND stage:6 )"); ////情报矢量化的  不查询t_fStatus为0的
//            }
//        }

        // 过滤315 web不显示的tips 20170118
        this.getFilter315(builder);

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
