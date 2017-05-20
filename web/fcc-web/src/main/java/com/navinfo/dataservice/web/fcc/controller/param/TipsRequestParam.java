package com.navinfo.dataservice.web.fcc.controller.param;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.fcc.*;
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
            builder.append(" AND stage:(");
            for (int i = 0; i < stages.size(); i++) {
                int stage = stages.getInt(i);
                if (i > 0) {
                    builder.append(" ");
                }
                builder.append(stage);
            }
            builder.append(")");
        }

        Set<Integer> taskSet = this.getCollectIdsBySubTaskId(subtaskId);
        if (taskSet != null) {
            addTaskIdFilterSql(builder, taskSet);
        }

        //315过滤
        builder.append(this.getFilter315(builder));

        //制作状态
        if(jsonReq.containsKey("tipStatus")) {
            int tipStatus = jsonReq.getInt("tipStatus");
            builder.append(" AND t_tipStatus:" + tipStatus);
            //接边Tips
            if(tipStatus == 2 && stages.contains(1) && stages.contains(5) && stages.contains(6)) {
                builder.append(" OR (s_sourceType:8002 AND stage:2 AND t_tipStatus:" + tipStatus + " AND t_dEditStatus:0)");
            }
        }

        //日编作业/质检状态
        if(jsonReq.containsKey("dEditStatus")) {
            int dEditStatus = jsonReq.getInt("dEditStatus");
            builder.append(" AND t_dEditStatus:" + dEditStatus);
        }

        return null;

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
            if (StringUtils.isEmpty(builder.toString())) {
                builder.append(SolrQueryUtils.NOT_DISPLAY_TIP_FOR_315_TYPES_FILER_SQL);
            } else {
                builder.append(" AND "
                        + SolrQueryUtils.NOT_DISPLAY_TIP_FOR_315_TYPES_FILER_SQL);
            }
        }
        return builder;
    }
}
