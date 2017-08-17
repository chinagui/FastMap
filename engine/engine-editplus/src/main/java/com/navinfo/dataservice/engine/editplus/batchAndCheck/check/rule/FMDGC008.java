package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import oracle.sql.STRUCT;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

/**
 * @Title: FMDGC008
 * @Package: com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule
 * @Description: 检查对象：非删除POI对象
                检查原则：
                1、如果POI的引导坐标（3米范围内）没有关联道路或测线（测线为非删除对象），则报log
                2、如果POI的引导坐标（3米范围内）有关联道路但无测线（测线为非删除对象），且30米范围内有其他测线，则报log
 * @Author: Crayeres
 * @Date: 8/15/2017
 * @Version: V1.0
 */
public class FMDGC008 extends BasicCheckRule {
    private static Logger logger = Logger.getLogger(FMDGC008.class);

    @Override
    public void runCheck(BasicObj obj) throws Exception {

    }

    @Override
    public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {

    }

    @Override
    public void run() throws Exception {
        long startTime = System.currentTimeMillis();

        Map<Long, IxPoi> map = new HashMap<>();

        for (Map.Entry<Long, BasicObj> entry : getRowList().entrySet()) {
            BasicObj basicObj = entry.getValue();

            if (basicObj.objName().equals(ObjectName.IX_POI) && !basicObj.opType().equals(OperationType.DELETE)) {
                IxPoiObj poiObj = (IxPoiObj) basicObj;
                map.put(entry.getKey(), (IxPoi) poiObj.getMainrow());
            }
        }

        if (map.isEmpty()) {
            return;
        }

        Map<Long, IxPoi> noAssociationLink = new HashMap<>();

        Connection tipsConn = null;
        PreparedStatement tipsPstmt = null;
        ResultSet tipsResultSet = null;

        PreparedStatement pstmt = null;
        ResultSet resultSet = null;

        try {
            Connection conn = getCheckRuleCommand().getConn();

            List<Clob> values = new ArrayList<>();

            String pidString;
            if (map.size() > 1000) {
                Clob clob = ConnectionUtil.createClob(conn);
                clob.setString(1, StringUtils.join(map.keySet(), ","));
                pidString = " PID IN (select to_number(column_value) from table(clob_to_table(?)))";
                values.add(clob);
            } else {
                pidString = " PID IN (" + StringUtils.join(map.keySet(), ",") + ")";
            }

            String sql = "SELECT /*+ INDEX(T2 RD_LINK_GEOMETRY) */" + 
                    " T1.PID" + 
                    "  FROM IX_POI T1, RD_LINK T2" + 
                    " WHERE T1.U_RECORD <> 2" + 
                    "   AND T1." + pidString +
                    "   AND SDO_NN(T2.GEOMETRY," + 
                    "              SDO_GEOMETRY('POINT (' || T1.X_GUIDE || ' ' || T1.Y_GUIDE || ')'," + 
                    "                           8307)," + 
                    "              'SDO_NUM_RES=3'," +
                    "              1) = 'TRUE'" + 
                    "   AND T2.U_RECORD <> 2" + 
                    " GROUP BY T1.PID" + 
                    " HAVING MIN(SDO_NN_DISTANCE(1)) >= 3";

            pstmt = conn.prepareStatement(sql);
            if (CollectionUtils.isNotEmpty(values)) {
                for (int i = 0; i < values.size(); i++) {
                    pstmt.setClob(i + 1, values.get(i));
                }
            }
            resultSet = pstmt.executeQuery();

            while (resultSet.next()) {
                Long pid = resultSet.getLong("PID");
                noAssociationLink.put(pid, map.get(pid));
            }

            if (noAssociationLink.isEmpty()) {
                return;
            }

            tipsConn = DBConnector.getInstance().getTipsIdxConnection();

            String tipsSql = "SELECT T.WKTLOCATION" +
                    "  FROM TIPS_INDEX T" +
                    " WHERE T.S_SOURCETYPE = '2001'" +
                    "   AND T.T_LIFECYCLE <> 1";

            tipsPstmt = tipsConn.prepareStatement(tipsSql);
            tipsResultSet = tipsPstmt.executeQuery();

            List<Geometry> geometries = new ArrayList<>();

            while (tipsResultSet.next()) {
                STRUCT struct = (STRUCT) tipsResultSet.getObject("WKTLOCATION");
                geometries.add(GeoTranslator.struct2Jts(struct));
            }

            if (CollectionUtils.isEmpty(geometries)) {
                for (Map.Entry<Long, IxPoi> entry : noAssociationLink.entrySet()) {
                    setCheckResult("", String.format("[IX_POI,%s]", entry.getKey()), 0, "引导坐标没有关联测线或道路");
                }
                return;
            }

            label1:
            for (Map.Entry<Long, IxPoi> entry : noAssociationLink.entrySet()) {
                Coordinate coordinate = new Coordinate(entry.getValue().getXGuide(), entry.getValue().getYGuide());

                boolean has30M = false;

                for (Geometry geometry : geometries) {
                    double length = GeometryUtils.getDistance(coordinate, GeometryUtils.GetNearestPointOnLine(coordinate, geometry));
                    if (length <= 3d) {
                        continue label1;
                    }
                    if (length <= 30d) {
                        has30M = true;
                    }
                }

                if (has30M) {
                    setCheckResult("", String.format("[IX_POI,%s]", entry.getKey()), 0, "请确认POI是否关联测线");
                } else {
                    setCheckResult("", String.format("[IX_POI,%s]", entry.getKey()), 0, "引导坐标没有关联测线或道路");
                }
            }
        } catch (Exception e) {
            throw e;
        } finally {
            DBUtils.closeResultSet(tipsResultSet);
            DBUtils.closeStatement(tipsPstmt);
            DBUtils.closeConnection(tipsConn);

            DBUtils.closeResultSet(resultSet);
            DBUtils.closeStatement(pstmt);

            logger.info("FMDGC008: (SPEND TIME: " + (System.currentTimeMillis() - startTime) + ", CHECK RESULT SIZE:" + getCheckResultList().size() + ")");
        }
    }
}
