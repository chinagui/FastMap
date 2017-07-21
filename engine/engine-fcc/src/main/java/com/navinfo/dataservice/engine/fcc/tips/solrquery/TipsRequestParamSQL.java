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
public class TipsRequestParamSQL {
    private static final Logger logger = Logger.getLogger(TipsRequestParamSQL.class);

    public String getByTileWithGap(String parameter) throws Exception {
        JSONObject jsonReq = JSONObject.fromObject(parameter);

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
        if (types.size() > 0) {
            this.getStringArrayQuery(builder, types, "s_sourceType");
        }

        if (stages.size() > 0) {
            this.getIntArrayQuery(builder, stages, "stage");

            if(StringUtils.isNotEmpty(pType)) {
                if(pType.equals("sl")) {//矢量化 赵航
                }else if(pType.equals("ms")) {//生产管理 万冲
                    builder.append(" and t_tipStatus=2");
                    //20170615 过滤内业Tips
                    builder.append(" and not REGEXP_LIKE(s_sourceType,'^80')");
                    //20170510 增加中线有无过滤
                    addTaskFilterSql(noQFilter, builder);
                }else if(pType.equals("fc")) {//FC 预处理 钟小明
                    builder.append(" AND (t_tipStatus=2 OR (s_sourceType='8001' AND t_tipStatus=1))");
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
                    webBuilder.append("(t_tipStatus=2)");
                }else {
                    if (workStatus.contains(0)) {

                        if (webBuilder.length() > 0) {
                            webBuilder.append(" OR ");
                        }

                        webBuilder.append("(");
                        webBuilder.append("(");
                        webBuilder.append("t_tipStatus=2 AND t_dEditStatus=0 AND stage in (1,2,5,6)");
                        webBuilder.append(")");

                        //待质检的tips
                        webBuilder.append(" OR (stage=7 AND t_dEditStatus=0 AND t_tipStatus=2)");

                        webBuilder.append(	")");


                    }
                    if (workStatus.contains(1)) {
                        if (webBuilder.length() > 0) {
                            webBuilder.append(" OR ");
                        }
                        webBuilder.append("(stage in (2,7) AND t_dEditStatus=1)");
                    }
                    if (workStatus.contains(2)) {
                        if (webBuilder.length() > 0) {
                            webBuilder.append(" OR ");
                        }
                        webBuilder.append("(stage in (2,7) AND t_dEditStatus=2)");
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
                    builder.append(" AND s_sourceType!='8001'");//接边Tips
                }
                else if(subTaskType == 4) {//4 区域粗编
                    builder.append(" AND s_sourceType='8001'");//预处理提交
                }
            }
        }
        // 过滤315 web不显示的tips 20170118
        this.getFilter315(builder);

        if(builder.length()>0){
            builder.append(" and");
        }
        builder.append(" sdo_relate(wktLocation,sdo_geometry(:1,8307),'mask=anyinteract') = 'TRUE'");
        String sql = "select * from tips_index where " + builder.toString();
        logger.info("getByTileWithGap:" + sql);
        return sql;
    }

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

    private StringBuilder getStringArrayQuery(StringBuilder builder, JSONArray stringArray, String fieldName) {

        if(stringArray!=null){
            if(builder.length() > 0) {
                builder.append(" AND");
            }
            builder.append(" " + fieldName + " in (");
            for (int i = 0; i < stringArray.size(); i++) {
                String fieldValue = stringArray.getString(i);
                if (i > 0) {
                    builder.append(",");
                }
                builder.append("'");
                builder.append(fieldValue);
                builder.append("'");
            }
            builder.append(")");
        }

        return builder;
    }

    private StringBuilder getIntArrayQuery(StringBuilder builder, JSONArray intArray, String fieldName) {
        if(builder.length() > 0) {
            builder.append(" AND");
        }
        builder.append(" " + fieldName + " in (");
        for (int i = 0; i < intArray.size(); i++) {
            int fieldValue = intArray.getInt(i);
            if (i > 0) {
                builder.append(",");
            }
            builder.append(fieldValue);
        }
        builder.append(")");
        return builder;
    }

    /**
     * 中线有无过滤
     * @param noQFilter
     * @param builder
     */
    private void addTaskFilterSql(JSONArray noQFilter, StringBuilder builder) {
        if ((noQFilter != null) && (noQFilter.size() > 0)) {
            builder.append(" AND s_qTaskId=0");
            if (noQFilter.size() < 2) {
                int flag = noQFilter.getInt(0);
                if (flag == 1)
                    builder.append(" AND s_mTaskId!=0");
                else if (flag == 2)
                    builder.append(" AND s_mTaskId=0");
            }
        }
    }
}
