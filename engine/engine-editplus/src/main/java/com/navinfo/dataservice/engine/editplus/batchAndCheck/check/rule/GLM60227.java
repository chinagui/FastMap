package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.navicommons.database.sql.DBUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;

/**
 * @Title: GLM60227
 * @Package: com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule
 * @Description: POI的显示坐标与引导坐标之间的连线（以显示坐标和引导坐标为端点的线段），不应与铁路相交，否则报log
 * @Author: Crayeres
 * @Date: 7/7/2017
 * @Version: V1.0
 */
public class GLM60227 extends BasicCheckRule {
    @Override
    public void runCheck(BasicObj obj) throws Exception {
        if (obj.objName().equals(ObjectName.IX_POI)) {
            IxPoiObj poiObj = (IxPoiObj) obj;
            IxPoi poi = (IxPoi) poiObj.getMainrow();

            String sql = "SELECT T1.LINK_PID FROM RW_LINK T1, IX_POI T2 WHERE T2.PID = :1 AND T1.MESH_ID = T2.MESH_ID AND " +
                    "T1.U_RECORD <> 2 AND T2.U_RECORD <> 2 AND SDO_RELATE(T1.GEOMETRY, SDO_GEOMETRY('LINESTRING(' || " +
                    "T2.GEOMETRY.SDO_POINT.X || ' ' || T2.GEOMETRY.SDO_POINT.Y || ' , ' || T2.X_GUIDE || ' ' || T2.Y_GUIDE ||" +
                    " ')', 8307), 'MASK=ANYINTERACT') = 'TRUE'";
            Connection conn = getCheckRuleCommand().getConn();
            PreparedStatement pstmt = null;
            ResultSet resultSet = null;
            try {
                pstmt = conn.prepareStatement(sql);
                pstmt.setLong(1, poi.getPid());
                resultSet = pstmt.executeQuery();
                while (resultSet.next()) {
                    setCheckResult(poi.getGeometry(), String.format("[IX_POI,%s]", poi.getPid()), poi.getMeshId());
                }
            } catch (Exception e) {
                throw e;
            } finally {
                DBUtils.closeResultSet(resultSet);
                DBUtils.closeStatement(pstmt);
            }
        }
    }

    @Override
    public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {

    }
}
