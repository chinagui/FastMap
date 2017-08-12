package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.navicommons.database.sql.DBUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
    }

    @Override
    public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {

    }

    @Override
    public void run() throws Exception {
        List<Long> pids = new ArrayList<>();
        for (Map.Entry<Long, BasicObj> entryRow : getRowList().entrySet()) {
            BasicObj basicObj = entryRow.getValue();

            if (basicObj.objName().equals(ObjectName.IX_POI)) {
                pids.add(entryRow.getKey());
            }
        }

        if (CollectionUtils.isEmpty(pids)) {
            return;
        }

        String pidStr = StringUtils.join(pids, ",");

        Connection conn = getCheckRuleCommand().getConn();

        List<Clob> values = new ArrayList<>();
        String pidString;
        if (pids.size() > 1000) {
            Clob clob = ConnectionUtil.createClob(conn);
            clob.setString(1, pidStr);
            pidString = " PID IN (select to_number(column_value) from table(clob_to_table(?)))";
            values.add(clob);
        } else {
            pidString = " PID IN (" + pidStr + ")";
        }

        String sql = "SELECT T2.PID" + 
                "  FROM RW_LINK T1, IX_POI T2" + 
                " WHERE T2." + pidString +
                "   AND T1.MESH_ID = T2.MESH_ID" + 
                "   AND T1.U_RECORD <> 2" + 
                "   AND T2.U_RECORD <> 2" + 
                "   AND SDO_RELATE(T1.GEOMETRY," + 
                "                  SDO_GEOMETRY('LINESTRING(' || T2.GEOMETRY.SDO_POINT.X || ' ' ||" + 
                "                               T2.GEOMETRY.SDO_POINT.Y || ' , ' ||" + 
                "                               T2.X_GUIDE || ' ' || T2.Y_GUIDE || ')'," + " 8307)," +
                "                  'MASK=ANYINTERACT') = 'TRUE'";

        PreparedStatement pstmt = null;
        ResultSet resultSet = null;

        try {
            pstmt = conn.prepareStatement(sql);
            if (CollectionUtils.isNotEmpty(values)) {
                for (int i = 0; i < values.size(); i++) {
                    pstmt.setClob(i + 1, values.get(i));
                }
            }

            resultSet = pstmt.executeQuery();
            List<String> validate = new ArrayList<>();
            while (resultSet.next()) {
                int pid = resultSet.getInt("PID");
                String targets = "[IX_POI," + pid + "]";

                if (!validate.contains(targets)) {
                    setCheckResult("", targets, 0);
                    validate.add(targets);
                }
            }
        } catch (Exception e) {
            throw e;
        } finally {
            DBUtils.closeResultSet(resultSet);
            DBUtils.closeStatement(pstmt);
        }
    }
}
