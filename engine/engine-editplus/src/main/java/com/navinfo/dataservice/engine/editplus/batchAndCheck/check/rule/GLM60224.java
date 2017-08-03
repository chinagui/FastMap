package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.navicommons.database.sql.DBUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;

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
    public void runCheck(BasicObj obj) throws Exception {
        if (obj.objName().equals(ObjectName.IX_POI)) {
            IxPoiObj poiObj = (IxPoiObj) obj;
            IxPoi poi = (IxPoi) poiObj.getMainrow();

            String kindCode = poi.getKindCode();
            if (StringUtils.isEmpty(kindCode)) {
                return;
            }

            String sql = "SELECT T1.KIND FROM LC_FACE T1, IX_POI T2 WHERE T2.PID = :1 AND T1.MESH_ID = T2.MESH_ID AND " +
                    "T1.U_RECORD <> 2 AND T2.U_RECORD <> 2 AND SDO_RELATE(T1.GEOMETRY, T2.GEOMETRY, 'MASK=ANYINTERACT') = 'TRUE'"
                    + " AND T1.KIND IN (1,2,3,4,5,6,12,13)";
            log.info(sql);
            PreparedStatement pstmt = null;
            ResultSet resultSet = null;
            try {
                pstmt = getCheckRuleCommand().getConn().prepareStatement(sql);
                pstmt.setLong(1, poi.getPid());
                resultSet = pstmt.executeQuery();
                while (resultSet.next()) {
                    int kind = resultSet.getInt("KIND");

                    String label = StringUtils.isEmpty(poi.getLabel()) ? "" : poi.getLabel();

                    if (Arrays.asList(1, 2, 3, 4, 5, 6).contains(Integer.valueOf(kind))) {
                        if (Arrays.asList("230201", "230105", "230202").contains(kindCode) || label.indexOf("水") != -1) {
                            continue;
                        } else {
                            setCheckResult(poi.getGeometry(), String.format("[IX_POI,%s]", poi.getPid()), poi.getMeshId());
                        }
                    }
                    if (kind == 12) {
                        if (Arrays.asList("180105", "180106").contains(kindCode)) {
                            continue;
                        } else {
                            setCheckResult(poi.getGeometry(), String.format("[IX_POI,%s]", poi.getPid()), poi.getMeshId());
                        }
                    }
                    if (kind == 13) {
                        if (Arrays.asList("180104").contains(kindCode)) {
                            continue;
                        } else {
                            setCheckResult(poi.getGeometry(), String.format("[IX_POI,%s]", poi.getPid()), poi.getMeshId());
                        }
                    }
                }
            } catch (SQLException e) {
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
