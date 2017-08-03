package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Geometry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

/**
 * @Title: GLM60228
 * @Package: com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule
 * @Description: POI的显示坐标与引导坐标之间的连线（以显示坐标和引导坐标为端点的线段），不应横跨（与面的边界相交2次视为横跨）土地覆盖面或土地利用面，
 * 否则报log屏蔽对象：
 * 1.如果横跨的土地覆盖面类别为“绿化带”则不报log
 * 2.如果横跨的土地覆盖显示等级为1级，且种别为“河川域”或“湖沼池”且该点显示坐标和引导坐标间距离小于50米时不需要报log，否则报LOG
 * @Author: Crayeres
 * @Date: 7/7/2017
 * @Version: V1.0
 */
public class GLM60228 extends BasicCheckRule {

    /**
     * 最大允许偏移距离(m)
     */
    private final static double MAX_DISTANCE = 50.0d;

    /**
     * 绿化带
     */
    private final static int KIND_GREEN_BELT = 16;

    /**
     * 河川域
     */
    private final static int KIND_RIVER_AREA = 2;

    /**
     * 沼泽池
     */
    private final static int KIND_SWAMP_POOL = 3;

    @Override
    public void runCheck(BasicObj obj) throws Exception {
        if (obj.objName().equals(ObjectName.IX_POI)) {
            IxPoiObj poiObj = (IxPoiObj) obj;
            IxPoi poi = (IxPoi) poiObj.getMainrow();
            if (0 == poi.getXGuide() || 0 == poi.getYGuide()) {
                return;
            }

            // 检查LC_FACE
            checkCoverLcFace(poi);
            // 检查LU_FACE
            checkCoverLuFace(poi);
        }
    }

    private void checkCoverLcFace(IxPoi poi) throws Exception {
        Geometry geometry = GeoTranslator.transform(poi.getGeometry(), GeoTranslator.dPrecisionMap, 5);
        double length = GeometryUtils.getDistance(geometry.getCoordinate().y, geometry.getCoordinate().x, poi.getYGuide(), poi.getXGuide
                ());

        String sql = "SELECT T1.KIND, T1.DISPLAY_CLASS FROM LC_FACE T1, IX_POI T2 WHERE T2.PID = :1 AND T1.MESH_ID = T2.MESH_ID AND " +
                "T1.U_RECORD <> 2 AND T2.U_RECORD <> 2 AND SDO_RELATE(T1.GEOMETRY, SDO_GEOMETRY('LINESTRING(' || " + "T2.GEOMETRY" + "" +
                ".SDO_POINT.X || ' ' || T2.GEOMETRY.SDO_POINT.Y || ' , ' || T2.X_GUIDE || ' ' || T2.Y_GUIDE || ')' , 8307), " +
                "'MASK=011011111') = 'TRUE' AND T1.KIND!=16";
        log.info("GLM60228,checkCoverLcFace:"+sql);
        Connection conn = getCheckRuleCommand().getConn();
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, poi.getPid());
            resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                int kind = resultSet.getInt("KIND");

                int displayClass = resultSet.getInt("DISPLAY_CLASS");
                if (1 == displayClass && (kind == KIND_RIVER_AREA || kind == KIND_SWAMP_POOL) && length < MAX_DISTANCE) {
                    continue;
                }

                setCheckResult(poi.getGeometry(), String.format("[IX_POI,%s]", poi.getPid()), poi.getMeshId());
            }
        } catch (SQLException e) {
            throw e;
        } finally {
            DBUtils.closeResultSet(resultSet);
            DBUtils.closeStatement(pstmt);
        }
    }

    private void checkCoverLuFace(IxPoi poi) throws Exception {
        String sql = "SELECT 1 FROM LU_FACE T1, IX_POI T2 WHERE T2.PID = :1 AND T1.MESH_ID = T2.MESH_ID AND " + "T1.U_RECORD <> 2 AND "
                + "T2.U_RECORD <> 2 AND SDO_RELATE(T1.GEOMETRY, SDO_GEOMETRY('LINESTRING(' || " + "T2.GEOMETRY.SDO_POINT.X || ' ' || " +
                "T2.GEOMETRY.SDO_POINT.Y || ' , ' || T2.X_GUIDE || ' ' || T2.Y_GUIDE || ')' , 8307), " + "'MASK=011011111') = 'TRUE'";
        log.info("GLM60228,checkCoverLuFace:"+sql);
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
        } catch (SQLException e) {
            throw e;
        } finally {
            DBUtils.closeResultSet(resultSet);
            DBUtils.closeStatement(pstmt);
        }
    }

    @Override
    public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
    }
}
