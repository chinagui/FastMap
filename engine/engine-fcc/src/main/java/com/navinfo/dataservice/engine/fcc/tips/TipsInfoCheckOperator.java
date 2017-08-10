package com.navinfo.dataservice.engine.fcc.tips;


import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.navicommons.database.PageQueryUtils;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.sql.DBUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Created by zhangjunfang on 2017/5/31.
 */
public class TipsInfoCheckOperator {

    /**
     * 更新检查结果
     * @param resultId
     * @param status
     * @param ckConfirm
     */
    public int updateInfoCheckResult(int resultId, int status, int ckConfirm) {
        Connection checkConn = null;
        PreparedStatement pstmt = null;
        String updateSql = "update tips_check_result t set t.status = ?, t.ck_exception = ?, t.is_show = ?," +
                " t.ck_confirm = ? WHERE t.id = ?";

        try {
            checkConn = DBConnector.getInstance().getCheckConnection();
//            QueryRunner run = new QueryRunner();
//            return run.update(checkConn, updateSql, status, status, status == 1 ? 0 : 1, ckConfirm, resultId);
//        DBConnector.getInstance().getCheckConnection();
            pstmt = checkConn.prepareStatement(updateSql);
            pstmt.setInt(1, status);
            pstmt.setInt(2, status);
            pstmt.setInt(3, status == 1 ? 0 : 1);
            pstmt.setInt(4, ckConfirm);
            pstmt.setInt(5, resultId);
            pstmt.executeUpdate();
            checkConn.commit();
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
//            DBUtils.closeStatement(pstmt);
            DBUtils.closeConnection(checkConn);
        }
        return 0;
    }

    public JSONObject listInfoCheckResult(int subTaskId, int curPage, int pageSize) {
        String sql = "select id,rowkey,rule_id,err_Msg,err_Level,severity,status,ck_Confirm,geometry" +
                "          FROM tips_check_result WHERE task_id = ? and status = 0 order by rowkey,id";
        String countSql = PageQueryUtils.decorateOracleCountSql(sql);
        sql = PageQueryUtils.decorateOraclePageSql(sql);
        Connection checkConn = null;
        PreparedStatement pstmtCount = null;
        PreparedStatement pstmtQuery = null;
        JSONObject jsonObject = new JSONObject();
        try {
            checkConn = DBConnector.getInstance().getCheckConnection();
//        DBConnector.getInstance().getCheckConnection();
            pstmtCount = checkConn.prepareStatement(countSql);
            pstmtCount.setInt(1, subTaskId);
            ResultSet rs = pstmtCount.executeQuery();

            int total = 0;
            while(rs.next()) {
                total = rs.getInt(1);
            }
            jsonObject.put("total", total);
            rs.close();

            JSONArray jsonArray = new JSONArray();

            if(total > 0) {
                int firstNum = (curPage - 1) * pageSize;
                int lastNum = curPage * pageSize;
                lastNum = lastNum > total ? total : lastNum;
                pstmtQuery = checkConn.prepareStatement(sql);
                pstmtQuery.setInt(1, subTaskId);
                pstmtQuery.setInt(2, lastNum);
                pstmtQuery.setInt(3, firstNum);
                ResultSet rsQuery = pstmtQuery.executeQuery();
                while(rsQuery.next()) {
                    JSONObject resultObj = new JSONObject();
                    resultObj.put("id", rsQuery.getInt(1));
                    resultObj.put("rowkey", rsQuery.getString(2));
                    resultObj.put("ruleId", rsQuery.getString(3));
                    resultObj.put("errMsg", rsQuery.getString(4));
                    resultObj.put("errLevel", rsQuery.getInt(5));
                    resultObj.put("severity", rsQuery.getInt(6));
                    resultObj.put("status", rsQuery.getInt(7));
                    resultObj.put("ckConfirm", rsQuery.getInt(8));
                    STRUCT struct = (STRUCT) rsQuery.getObject(9);
                    resultObj.put("geometry", GeoTranslator.struct2Wkt(struct));
                    jsonArray.add(resultObj);
                }
                rsQuery.close();
            }
            jsonObject.put("result", jsonArray);

        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            DBUtils.closeStatement(pstmtCount);
            DBUtils.closeStatement(pstmtQuery);
            DBUtils.closeConnection(checkConn);
        }
        return jsonObject;
    }
}
