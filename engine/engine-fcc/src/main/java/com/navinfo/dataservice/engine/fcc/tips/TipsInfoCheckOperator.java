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

import org.apache.commons.dbutils.DbUtils;

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
    public int updateInfoCheckResult(int resultId, int status, int ckConfirm) throws Exception {
        Connection checkConn = null;
        PreparedStatement pstmt = null;
        String updateSql = "update tips_check_result t set t.status = ?, t.ck_exception = ?, t.is_show = ?," +
                " t.ck_confirm = ? WHERE t.id = ?";

        try {
            checkConn = DBConnector.getInstance().getCheckConnection();
            pstmt = checkConn.prepareStatement(updateSql);
            pstmt.setInt(1, status);
            pstmt.setInt(2, status);
            pstmt.setInt(3, status == 1 ? 0 : 1);
            pstmt.setInt(4, ckConfirm);
            pstmt.setInt(5, resultId);
            int count = pstmt.executeUpdate();
            checkConn.commit();
            return count;
        }catch (Exception e) {
            e.printStackTrace();
            DbUtils.rollbackAndCloseQuietly(checkConn);
            throw new Exception("更新检查结果报错", e);
        }finally {
        	DbUtils.closeQuietly(pstmt);
            DbUtils.commitAndCloseQuietly(checkConn);
        }
    }

    public JSONObject listInfoCheckResult(int subTaskId, int curPage, int pageSize) throws Exception {
        String sql = "select id,rowkey,rule_id,err_Msg,err_Level,severity,status,ck_Confirm,geometry" +
                "          FROM tips_check_result WHERE task_id = ? and status = 0 order by rowkey,id";
        String countSql = PageQueryUtils.decorateOracleCountSql(sql);
        sql = PageQueryUtils.decorateOraclePageSql(sql);
        Connection checkConn = null;
        PreparedStatement pstmtCount = null;
        PreparedStatement pstmtQuery = null;
        ResultSet rs = null;
        ResultSet rsQuery = null;
        JSONObject jsonObject = new JSONObject();
        try {
            checkConn = DBConnector.getInstance().getCheckConnection();
            pstmtCount = checkConn.prepareStatement(countSql);
            pstmtCount.setInt(1, subTaskId);
            rs = pstmtCount.executeQuery();

            int total = 0;
            while(rs.next()) {
                total = rs.getInt(1);
            }
            jsonObject.put("total", total);
            JSONArray jsonArray = new JSONArray();

            if(total > 0) {
                int firstNum = (curPage - 1) * pageSize;
                int lastNum = curPage * pageSize;
                lastNum = lastNum > total ? total : lastNum;
                pstmtQuery = checkConn.prepareStatement(sql);
                pstmtQuery.setInt(1, subTaskId);
                pstmtQuery.setInt(2, lastNum);
                pstmtQuery.setInt(3, firstNum);
                rsQuery = pstmtQuery.executeQuery();
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
                //rsQuery.close();
            }
            jsonObject.put("result", jsonArray);

        }catch (Exception e) {
            e.printStackTrace();
            DbUtils.rollbackAndCloseQuietly(checkConn);
            throw new Exception("查询检查结果列表报错", e);
        }finally {
        	DbUtils.closeQuietly(rs);
        	DbUtils.closeQuietly(rsQuery);
            DbUtils.closeQuietly(pstmtCount);
            DbUtils.closeQuietly(pstmtQuery);
            DbUtils.commitAndCloseQuietly(checkConn);
        }
        return jsonObject;
    }
}
