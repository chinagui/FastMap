package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
import com.navinfo.navicommons.database.sql.DBUtils;

/**
 * FM-11Win-08-22 检查条件： Lifecycle！=1（删除） 检查原则： 加油站（分类为230215）、加气站（230216）
 * 同点的设施需要与加油站建立父子关系，否则报Log:与加油站同点的POI未与加油站建立父子关系
 * 
 * @author wangdongbin
 *
 */
public class FM11Win0822 extends BasicCheckRule {

	public void run() throws Exception {
		log.info("CopyOfFM11Win0822");
		Map<Long, BasicObj> rows = getRowList();
		Set<Long> pidAllSet = new HashSet<Long>();
		List<ArrayList<Long>> pid = new ArrayList<ArrayList<Long>>();
		int n = 0;
		ArrayList<Long> tmpPids = new ArrayList<>();
		for (Long key : rows.keySet()) {
			BasicObj obj = rows.get(key);
			IxPoiObj poiObj = (IxPoiObj) obj;
			IxPoi poi = (IxPoi) poiObj.getMainrow();
			// 已删除的数据不检查
			if (poi.getOpType().equals(OperationType.PRE_DELETED)) {
				continue;
			}
			pidAllSet.add(poi.getPid());
			tmpPids.add(poi.getPid());
			if( n /900 > 0){
				pid.add(tmpPids);
				n = 0;
				tmpPids = new ArrayList<>();
			}
			n++;
		}
		if (tmpPids.size() > 0){
			pid.add(tmpPids);
		}

		if (pidAllSet == null || pidAllSet.size() == 0) {
			return;
		}
		// 获取已存在的父子关系

		// key:childPid value:parent
		Map<Long, Long> existsRelateMap = IxPoiSelector.getParentChildByPids(this.getCheckRuleCommand().getConn(),
				pidAllSet);
		Map<Long, Set<Long>> errorList = new HashMap<Long, Set<Long>>();
		Connection conn = this.getCheckRuleCommand().getConn();
		String pidString = "";
		for (List tmppid: pid){
				String pids = tmppid.toString().replace("[", "").replace("]", "");
			    pidString = " PID IN (" + pids + ")";

		String sqlStr = "SELECT P1.PID PID_MAIN,P1.KIND_CODE KIND_MAIN,P2.PID,P2.KIND_CODE "
				+ " FROM IX_POI P1,IX_POI P2"
				+ " WHERE SDO_WITHIN_DISTANCE(P2.GEOMETRY, P1.GEOMETRY, 'DISTANCE=3 UNIT=METER') = 'TRUE'"
				+ "	AND P1." + pidString + " AND P1.U_RECORD <>2 AND P2.U_RECORD <>2	" + " AND P1.PID <> P2.PID"
				+ " AND (P1.KIND_CODE IN ('230215','230216') OR P2.KIND_CODE IN ('230215','230216'))";
		log.info("FM-11-Win-08-22 sql:" + sqlStr);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = conn.prepareStatement(sqlStr);
			rs = pstmt.executeQuery();
			log.info("exeSql执行完成");
			
			while (rs.next()) {
				Long pidTmp1 = rs.getLong("PID_MAIN");
				Long pidTmp2 = rs.getLong("PID");
	
				// 这对pid是否已经存在父子关系，已经存在则判断下一对
				if (existsRelateMap.containsKey(pidTmp1)) {
					if (existsRelateMap.get(pidTmp1).equals(pidTmp2)) {
						continue;
					}
				}
				if (existsRelateMap.containsKey(pidTmp2)) {
					if (existsRelateMap.get(pidTmp2).equals(pidTmp1)) {
						continue;
					}
				}
				// 这对pid没有父子关系，则报错
				if (!errorList.containsKey(pidTmp1)) {
					errorList.put(pidTmp1, new HashSet<Long>());
				}
				errorList.get(pidTmp1).add(pidTmp2);
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(rs);
			DBUtils.closeStatement(pstmt);
		}
		log.info("errorList组装完成");
			}
		// 过滤相同pid
		Set<Long> filterPid = new HashSet<Long>();
		for (Long pid1 : errorList.keySet()) {
			String targets = "[IX_POI," + pid1 + "]";
			for (Long pid2 : errorList.get(pid1)) {
				targets = targets + ";[IX_POI," + pid2 + "]";
			}
			if (!(filterPid.contains(pid1) && filterPid.containsAll(errorList.get(pid1)))) {
				setCheckResult("", targets, 0);
			}
			filterPid.add(pid1);
			filterPid.addAll(errorList.get(pid1));
		}
	}

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
