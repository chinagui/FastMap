package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiParking;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

/**
 * @Title: FMYW20123
 * @Package: com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule
 * @Description: 检查条件：非删除POI对象且为占道停车场（分类为230210，停车场建筑物类型为占道，即IX_POI_PARKING.PARKING_TYPE=2)
 * 检查规则：
 * POI5米BUFFER内（小于5米）有道路link时，报log
 * @Author: Crayeres
 * @Date: 7/10/2017
 * @Version: V1.0
 */
public class FMYW20123 extends BasicCheckRule {
    @Override
    public void runCheck(BasicObj obj) throws Exception {
        if (obj.objName().equals(ObjectName.IX_POI)) {
            IxPoiObj poiObj = (IxPoiObj) obj;
            IxPoi poi = (IxPoi) poiObj.getMainrow();

            String kindCode = poi.getKindCode();
            if (StringUtils.isEmpty(kindCode)) {
                return;
            }

            if (!"230210".equals(kindCode)) {
                return;
            }

            List<IxPoiParking> ixPoiParkings = poiObj.getIxPoiParkings();

            boolean flag = false;
            for (IxPoiParking parking : ixPoiParkings) {
                String parkingType = parking.getParkingType();
                if ("2".equals(parkingType)) {
                    flag = true;
                }
            }
          
            if (flag) {
                String sql = "SELECT COUNT(1) COUNT_NUM FROM IX_POI IP, RD_LINK RL WHERE IP.PID = :1 AND IP.MESH_ID = RL.MESH_ID AND IP" +
                        ".U_RECORD <> 2 AND RL.U_RECORD <> 2 AND SDO_GEOM.SDO_DISTANCE(IP.GEOMETRY, RL.GEOMETRY, 0.00000005) < 5";
                PreparedStatement pstmt = null;
                ResultSet resultSet = null;
                try {
                    pstmt = getCheckRuleCommand().getConn().prepareStatement(sql);
                    pstmt.setLong(1, poi.getPid());
                    resultSet = pstmt.executeQuery();
                    while (resultSet.next()) {
                        int count = resultSet.getInt("COUNT_NUM");
                        if (count > 0) {
                            setCheckResult(poi.getGeometry(), String.format("[IX_POI,%s]", poi.getPid()), poi.getMeshId());
                        }
                    }
                } catch (SQLException e) {
                	log.error(e.getMessage(),e);
    				throw e;
                } finally {
                	DbUtils.closeQuietly(resultSet);
        			DbUtils.closeQuietly(pstmt);
                }
            }
        }
    }

    @Override
    public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {

    }
}
