package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;

/**
 * 
 * 检查条件：Lifecycle为“1（删除）”不检查； 检查原则：
 * 与父分类（200103大厦\200104商务中心\120101星级酒店）的设施同点，却没有建立父子关系。
 *
 */
public class FM14Sum1108 extends BasicCheckRule {
	
	Map<Long, Long> parentIds = new HashMap<Long, Long>();

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if (obj.objName().equals(ObjectName.IX_POI)) {
			IxPoiObj poiObj = (IxPoiObj) obj;
			IxPoi poi = (IxPoi) poiObj.getMainrow();
			if (parentIds!=null && parentIds.containsKey(poi.getPid())) {
				return;
			}
			
			String sqlStr=" SELECT P2.KIND_CODE"
					+ " FROM IX_POI P1,"
					+ " IX_POI P2,"
					+ " WHERE SDO_GEOM.SDO_DISTANCE(P1.GEOMETRY, P2.GEOMETRY, 0.00000005) < 3"
					+ " AND P1.PID=:1"
					+ " AND P2.PID!=P1.PID";
			
			Connection conn = this.getCheckRuleCommand().getConn();
			
			PreparedStatement pstmt = null;
			
			ResultSet rs = null;
			try {
				List<String> kindCodeList = new ArrayList<String>();
				pstmt=conn.prepareStatement(sqlStr);
				pstmt.setLong(1, poi.getPid());
				rs = pstmt.executeQuery();
				while (rs.next()) {
					kindCodeList.add(rs.getString("KIND_CODE"));
				}
				if (kindCodeList.contains("200103")) {
					setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(),"与父分类（200103大厦）的设施同点，却没有建立父子关系");
				} else if (kindCodeList.contains("200104")) {
					setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(),"与父分类（200104商务中心）的设施同点，却没有建立父子关系");
				} else if (kindCodeList.contains("120101")) {
					setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(),"与父分类（120101星级酒店）的设施同点，却没有建立父子关系");
				}
			} catch (Exception e) {
				throw e;
			} finally {
				DbUtils.close(rs);
	    		DbUtils.close(pstmt);
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		Set<Long> pidList = new HashSet<Long>();
		for (BasicObj obj : batchDataList) {
			pidList.add(obj.objPid());
		}
		parentIds = IxPoiSelector.getParentPidsByChildrenPids(getCheckRuleCommand().getConn(), pidList);
	}

}
