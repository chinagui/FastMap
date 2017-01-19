package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;

/**
 * 检查条件：Lifecycle为“1（删除）”不检查； 检查原则：
 * 与加油站、加气站同点的便利店设施（分类：便利零售、其他零售、旧货市场、丧葬用品零售、宗教用品零售）
 * （130105\130800\130804\130806\130807）没有与加油站（分类：230215\230216）建立父子关系。
 *
 */
public class FM14Sum1115 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if (obj.objName().equals(ObjectName.IX_POI)) {
			IxPoiObj poiObj = (IxPoiObj) obj;
			IxPoi poi = (IxPoi) poiObj.getMainrow();
			if (!poi.getKindCode().equals("230215") && !poi.getKindCode().equals("230216")) {
				return;
			}
			Set<Long> parentPids = new HashSet<Long>();
			parentPids.add(poi.getPid());
			List<Long> childrenPids = IxPoiSelector.getChildrenPidsByParentPid(getCheckRuleCommand().getConn(),
					parentPids);
			
			String sqlStr=" SELECT P2.PID"
					+ " FROM IX_POI P1,"
					+ " IX_POI P2,"
					+ " WHERE SDO_GEOM.SDO_DISTANCE(P1.GEOMETRY, P2.GEOMETRY, 0.00000005) < 3"
					+ " AND P1.PID=:1"
					+ " AND P2.KIND_CODE IN ('130105','130800','130804','130806','130807')"
					+ " AND P2.PID!=P1.PID";
			
			Connection conn = this.getCheckRuleCommand().getConn();
			
			PreparedStatement pstmt = null;
			
			ResultSet rs = null;
			try {
				List<Long> pidList = new ArrayList<Long>();
				pstmt=conn.prepareStatement(sqlStr);
				pstmt.setLong(1, poi.getPid());
				rs = pstmt.executeQuery();
				while (rs.next()) {
					pidList.add(rs.getLong("PID"));
				}
				for (Long pid:pidList) {
					if (!childrenPids.contains(pid)) {
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(),"加油站、加气站内部便利店未做父子关系");
					}
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
		// TODO Auto-generated method stub

	}

}
