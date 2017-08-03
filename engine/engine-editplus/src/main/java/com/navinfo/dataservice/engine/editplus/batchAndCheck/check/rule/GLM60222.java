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
import java.util.List;

/**
 * @Title: GLM60222
 * @Package: com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule
 * @Description: 检查对象：种别不为230201、230202的POI点位
 * 检查原则：
 * POI的引导坐标不应落入类型为“1”（海）、“2”（河川域）、“3”（湖沼池）、“4”（水库）、“5”（港湾）、“6”（运河）的土地覆盖面中，否则报出Log
 * @Author: Crayeres
 * @Date: 7/10/2017
 * @Version: V1.0
 */
public class GLM60222 extends BasicCheckRule {
    @Override
    public void runCheck(BasicObj obj) throws Exception {
        if (obj.objName().equals(ObjectName.IX_POI)) {
            IxPoiObj poiObj = (IxPoiObj) obj;
            IxPoi poi = (IxPoi) poiObj.getMainrow();

            String kindCode = poi.getKindCode();
            if (StringUtils.isEmpty(kindCode)) {
                return;
            }

            if ("230201".equals(kindCode) || "230202".equals(kindCode)) {
                return;
            }

            String sql = "SELECT T1.KIND FROM LC_FACE T1, IX_POI T2 WHERE T2.PID = :1 AND T1.MESH_ID = T2.MESH_ID AND " +
                    "T1.U_RECORD <> 2 AND T2.U_RECORD <> 2 AND SDO_RELATE(T1.GEOMETRY, SDO_GEOMETRY('POINT ('|| T2.X_GUIDE || ' ' " +
                    "|| T2.Y_GUIDE || ')', 8307), 'MASK=ANYINTERACT') = 'TRUE' AND T1.KIND IN (1,2,3,4,5,6)";
        	log.info("GLM60222,sql:"+sql);
            PreparedStatement pstmt = null;
            ResultSet resultSet = null;
            try {
                pstmt = getCheckRuleCommand().getConn().prepareStatement(sql);
                pstmt.setLong(1, poi.getPid());
                resultSet = pstmt.executeQuery();
                while (resultSet.next()) {
                       setCheckResult(poi.getGeometry(), String.format("[IX_POI,%s]", poi.getPid()), poi.getMeshId());
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
