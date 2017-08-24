package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.navicommons.database.sql.DBUtils;
import org.apache.commons.collections.CollectionUtils;

import java.sql.*;
import java.util.*;

/**
 * @Title: GLM60224
 * @Package: com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule
 * @Description: POI的显示坐标不应落在种别为水系（1～6）、高尔夫（12）、滑雪场（13）的土地覆盖面内，否则报log
 * 屏蔽对象：
 * 1.种别为“230201”、“230105”（港口、码头）、“230202”（立交桥）或标记字段含“水”的POI，如果落在种别为水系（1～6），不报log；
 * 2.若种别为“180105/180106”（高尔夫）的POI落入种别为高尔夫的土地覆盖面内，不报log。
 * 3、若种别为“180104”（滑雪场）的POI落入滑雪场的土地覆盖面内，不报log。
 * @Author: Crayeres
 * @Date: 7/10/2017
 * @Version: V1.0
 */
public class GLM60224 extends BasicCheckRule {

    @Override
    public void run() throws Exception {
        List<Integer> waters = Arrays.asList(new Integer[]{1, 2, 3, 4, 5, 6});
        Integer golf = 12;
        Integer skiFacility = 13;

        List<String> allowWater = Arrays.asList(new String[]{"230201", "230105", "230202"});
        List<String> allowGolf = Arrays.asList(new String[]{"180105", "180106"});
        String allowSki = "180104";

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

        String pidStr = org.apache.commons.lang.StringUtils.join(pids, ",");

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

        String sql = "SELECT T1.PID, T1.KIND_CODE, T1.LABEL, T2.KIND" + 
                "  FROM IX_POI T1, LC_FACE T2" + 
                " WHERE T1." + pidString +
                "   AND T1.U_RECORD <> 2" + 
                "   AND SDO_RELATE(T2.GEOMETRY, T1.GEOMETRY, 'MASK=ANYINTERACT') = 'TRUE'" + 
                "   AND T2.KIND IN (1, 2, 3, 4, 5, 6, 12, 13)" + 
                "   AND T2.U_RECORD <> 2";

        PreparedStatement pstmt = null;

        ResultSet resultSet = null;
        try {
            pstmt = conn.prepareStatement(sql);
            if (CollectionUtils.isNotEmpty(values)) {
                for (int i = 0; i < values.size(); i++) {
                    pstmt.setClob(i + 1, values.get(i));
                }
            }

            Set<String> validate = new HashSet<>();

            resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                int pid = resultSet.getInt("PID");
                String kindCode = resultSet.getString("KIND_CODE");
                String label = resultSet.getString("LABEL");
                int kind = resultSet.getInt("KIND");

                String targets = null;

                if (waters.contains(Integer.valueOf(kind))) {
                    if (!allowWater.contains(kindCode) && org.apache.commons.lang.StringUtils.indexOf(label, '水') == -1) {
                        targets = String.format("[IX_POI,%s]", pid);
                    }
                } else if (kind == golf) {
                    if (!allowGolf.contains(kindCode)) {
                        targets = String.format("[IX_POI,%s]", pid);
                    }
                } else if (kind == skiFacility) {
                    if (!allowSki.equals(kindCode)) {
                        targets = String.format("[IX_POI,%s]", pid);
                    }
                }

                if (StringUtils.isNotEmpty(targets) && !validate.contains(targets)) {
                    setCheckResult("", targets, 0);
                    validate.add(targets);
                }
            }
        } catch (SQLException e) {
            throw e;
        } finally {
            DBUtils.closeResultSet(resultSet);
            DBUtils.closeStatement(pstmt);
        }
    }

    @Override
    public void runCheck(BasicObj obj) throws Exception {
    }

    @Override
    public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {

    }
}
