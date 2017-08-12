package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Geometry;

import oracle.sql.STRUCT;

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

    public void run() throws Exception {
		Map<Long, BasicObj> rows = getRowList();
		List<Long> pids = new ArrayList<Long>();
		for (Long key : rows.keySet()) {
			BasicObj obj = rows.get(key);
			IxPoiObj poiObj = (IxPoiObj) obj;
			IxPoi poi = (IxPoi) poiObj.getMainrow();
			// 已删除的数据不检查
			if (poi.getOpType().equals(OperationType.PRE_DELETED)) {
				continue;
			}
			pids.add(poi.getPid());
		}
		
		String pidStr = org.apache.commons.lang.StringUtils.join(pids, ",");
		
		checkCoverLcFace(pidStr);
		
		checkCoverLuFace(pidStr);
    }    
    
    
    private void checkCoverLcFace(String pids) throws Exception {
        String sql = "SELECT T1.KIND, T1.DISPLAY_CLASS,T2.PID,T2.GEOMETRY,T2.MESH_ID,T2.X_GUIDE,T2.Y_GUIDE FROM LC_FACE T1, IX_POI T2 WHERE T2.PID IN (select to_number(column_value) from table(clob_to_table(?))) AND T1.MESH_ID = T2.MESH_ID AND " +
                "T1.U_RECORD <> 2 AND T2.U_RECORD <> 2 AND SDO_RELATE(T1.GEOMETRY, SDO_GEOMETRY('LINESTRING(' || " + "T2.GEOMETRY" + "" +
                ".SDO_POINT.X || ' ' || T2.GEOMETRY.SDO_POINT.Y || ' , ' || T2.X_GUIDE || ' ' || T2.Y_GUIDE || ')' , 8307), " +
                "'MASK=011011111') = 'TRUE' AND T1.KIND!=16 AND (T1.DISPLAY_CLASS != 1 OR (T1.KIND != 2 AND T1.KIND != 3)) AND T2.X_GUIDE != 0 AND T2.Y_GUIDE != 0";
        log.info("GLM60228,checkCoverLcFace:"+sql);
        Connection conn = getCheckRuleCommand().getConn();
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        try {
			Clob clob = ConnectionUtil.createClob(conn);
			clob.setString(1, pids);
            pstmt = conn.prepareStatement(sql);
            pstmt.setClob(1, clob);
            resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
            	long poiPid = resultSet.getLong("PID");
            	int meshId = resultSet.getInt("MESH_ID");
				STRUCT struct = (STRUCT) resultSet.getObject("GEOMETRY");
				Geometry geometry = GeoTranslator.struct2Jts(struct, 100000, 0);
				double length = GeometryUtils.getDistance(geometry.getCoordinate().y, geometry.getCoordinate().x, resultSet.getDouble("Y_GUIDE"), resultSet.getDouble("X_GUIDE"));
                if (length < MAX_DISTANCE) {
                    continue;
                }

                setCheckResult(geometry, String.format("[IX_POI,%s]", poiPid), meshId);
            }
        } catch (SQLException e) {
            throw e;
        } finally {
        	
        	DbUtils.closeQuietly(resultSet);
        	DbUtils.closeQuietly(pstmt);
        }
    }
    
    private void checkCoverLuFace(String pids) throws Exception {
        String sql = "SELECT T2.PID,T2.GEOMETRY,T2.MESH_ID FROM LU_FACE T1, IX_POI T2 WHERE T2.PID IN (select to_number(column_value) from table(clob_to_table(?))) AND T1.MESH_ID = T2.MESH_ID AND " + "T1.U_RECORD <> 2 AND "
                + "T2.U_RECORD <> 2 AND SDO_RELATE(T1.GEOMETRY, SDO_GEOMETRY('LINESTRING(' || " + "T2.GEOMETRY.SDO_POINT.X || ' ' || " +
                "T2.GEOMETRY.SDO_POINT.Y || ' , ' || T2.X_GUIDE || ' ' || T2.Y_GUIDE || ')' , 8307), " + "'MASK=011011111') = 'TRUE'";
        log.info("GLM60228,checkCoverLuFace:"+sql);
        Connection conn = getCheckRuleCommand().getConn();
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        try {
			Clob clob = ConnectionUtil.createClob(conn);
			clob.setString(1, pids);
            pstmt = conn.prepareStatement(sql);
            pstmt.setClob(1, clob);
            resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
            	long poiPid = resultSet.getLong("PID");
            	int meshId = resultSet.getInt("MESH_ID");
				STRUCT struct = (STRUCT) resultSet.getObject("GEOMETRY");
				Geometry geometry = GeoTranslator.struct2Jts(struct, 100000, 0);
                setCheckResult(geometry, String.format("[IX_POI,%s]", poiPid), meshId);
            }
        } catch (SQLException e) {
            throw e;
        } finally {
        	DbUtils.closeQuietly(resultSet);
        	DbUtils.closeQuietly(pstmt);
        }
    }

    
    @Override
    public void runCheck(BasicObj obj) throws Exception {
    }

  

    @Override
    public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
    }
}
