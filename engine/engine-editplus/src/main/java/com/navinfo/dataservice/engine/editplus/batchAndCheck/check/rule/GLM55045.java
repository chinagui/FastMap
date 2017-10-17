package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpointaddress.IxPointaddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPointAddressObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import oracle.sql.STRUCT;

/**
 * @ClassName: GLM55045
 * @author: zhangpengpeng
 * @date: 2017年10月13日
 * @Desc: GLM55045.java 检查条件： 非删除点门牌对象 检查原则：
 *        点门牌的引导坐标距离其非删除引导Link的最短距离应小于等于2米，否则报出Log：点门牌引导坐标不在道路上！
 *        引导link仅是道路，不包含测线
 */
public class GLM55045 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void run() throws Exception {
		Map<Long, IxPointaddress> map = new HashMap<>();

		for (Map.Entry<Long, BasicObj> entry : getRowList().entrySet()) {
			BasicObj basicObj = entry.getValue();
			if (basicObj.objName().equals(ObjectName.IX_POINTADDRESS) && !basicObj.opType().equals(OperationType.PRE_DELETED)) {
				IxPointAddressObj ixPointaddressObj = (IxPointAddressObj) basicObj;
				IxPointaddress ixPonitaddress = (IxPointaddress) ixPointaddressObj.getMainrow();
				map.put(basicObj.objPid(), ixPonitaddress);
			}
		}
		if (map.isEmpty()) {
			return;
		}

		Connection conn = getCheckRuleCommand().getConn();
		Clob clob = null;
		String pidString;
		if (map.size() > 1000) {
			clob = ConnectionUtil.createClob(conn);
			clob.setString(1, StringUtils.join(map.keySet(), ","));
			pidString = " PID IN (select to_number(column_value) from table(clob_to_table(?)))";
		} else {
			pidString = " PID IN (" + StringUtils.join(map.keySet(), ",") + ")";
		}
		
		String sql = "select p.pid,r.geometry "
				+ "  from ix_pointaddress p, rd_link r "
				+ " where p." + pidString
				+ "   and p.guide_link_pid = r.link_pid "
				+ "   and p.u_record <> 2 " 
				+ "   and r.u_record <> 2";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try{
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			Set<String> filters = new HashSet<>();
			while(rs.next()){
				long pid = rs.getLong("PID");
				IxPointaddress ixPoint = map.get(pid);
				Geometry guideGeo = GeoTranslator.point2Jts(ixPoint.getXGuide(), ixPoint.getYGuide());
				STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
				Geometry linkGeo = GeoTranslator.struct2Jts(struct);
				Coordinate guideCoordinate = GeoTranslator.transform(guideGeo, GeoTranslator.dPrecisionMap, 5).getCoordinate();
                Coordinate pedalCoordinate = GeometryUtils.GetNearestPointOnLine(guideCoordinate, linkGeo);
                double distance = GeometryUtils.getDistance(guideCoordinate, pedalCoordinate);
                if(distance > 2){
					String targets = String.format("[IX_POINTADDRESS,%s]", pid);
					if (!filters.contains(targets)) {
						filters.add(targets);
						setCheckResult(ixPoint.getGeometry(), targets, ixPoint.getMeshId());
					}
                }
			}
		}catch(Exception e){
			throw e;
		}finally{
            DBUtils.closeResultSet(rs);
            DBUtils.closeStatement(pstmt);
		}
	}
}
