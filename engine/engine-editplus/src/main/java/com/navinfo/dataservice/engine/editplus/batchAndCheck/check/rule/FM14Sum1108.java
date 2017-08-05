package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.sql.Clob;
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

import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChildren;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
import com.navinfo.navicommons.database.sql.DBUtils;

/**
 * FM-14Sum-11-08 检查条件：非删除POI且存在父子关系； 检查原则：
 * 与父分类（200103大厦\200104商务中心\120101星级酒店）的设施同点，却没有建立父子关系，
 * 报log：与父分类（200103大厦\200104商务中心\120101星级酒店）的设施同点，却没有建立父子关系！
 * 备注：没有父的POI需要报出，有父的POI不用报。
 * 当这三个父分类同时存在时，按“200103\200104\120101”从左到右的优先顺序，报出优先的分类作为父POI。
 * 备注：当POI分类一样且同点位没有更高优先级POI时，该POI已有同分类的子时，不用报log 20170306 BY ZXY 规则优化
 */
public class FM14Sum1108 extends BasicCheckRule {

	Map<Long, Long> parentIds = new HashMap<Long, Long>();

	public void run() throws Exception {
		log.info("CopyOfFM14Sum1108");
		Map<Long, BasicObj> rows=getRowList();
		loadReferDatas(rows.values());
		List<Long> pidList = new ArrayList<Long>();
		for(Long key:rows.keySet()){
			BasicObj obj=rows.get(key);
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi =(IxPoi) poiObj.getMainrow();
			//已删除的数据不检查
			if(poi.getOpType().equals(OperationType.PRE_DELETED)){continue;}
			//有父的POI不用报
			if(parentIds.containsKey(poi.getPid())){continue;}
			pidList.add(poi.getPid());
		}
		
		//1.针对每个poi查询同点位的父分类（200103大厦\200104商务中心\120101星级酒店），并筛选出顶级父poi的集合
		Map<Long,Map<String,Object>> poiList = new HashMap<Long,Map<String,Object>>();
		//同点位的poi中顶级kind及对应的pid
		//key:子pid,value:顶级kind的pid
		Map<Long,Set<Long>> poiMap = new HashMap<Long,Set<Long>>();
		//key:子pid,value:顶级kindcode
		Map<Long,String> kindMap = new HashMap<Long,String>();
		
		String pids = pidList.toString().replace("[", "").replace("]", "");
		Connection conn = this.getCheckRuleCommand().getConn();
		List<Clob> values = new ArrayList<Clob>();
		String pidString = "";
		if (pidList.size() > 1000) {
			Clob clob = ConnectionUtil.createClob(conn);
			clob.setString(1, pids);
			pidString = " PID IN (select to_number(column_value) from table(clob_to_table(?)))";
			values.add(clob);
		} else {
			pidString = " PID IN (" + pids + ")";
		}
		String sqlStr = "SELECT P1.PID PID_MAIN,P1.KIND_CODE KIND_MAIN,P2.PID,P2.KIND_CODE "
				+ " FROM IX_POI P1,IX_POI P2"
				+ " WHERE SDO_NN(P2.GEOMETRY,P1.GEOMETRY,'sdo_batch_size=0 DISTANCE=3 UNIT=METER') = 'TRUE'"
				+ "	AND P1." + pidString + " AND P2.KIND_CODE IN ('200103', '200104', '120101') "
				+ "AND P1.U_RECORD <>2 AND P2.U_RECORD <>2	" + " AND P1.PID <> P2.PID";
		log.info("FM-14-Sum-11-08 sql:" + sqlStr);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try{
			pstmt = conn.prepareStatement(sqlStr);
			rs = pstmt.executeQuery();
			log.info("sql执行完成");
			while (rs.next()) {
				Map<String, Object> map = new HashMap<String, Object>();
				Long pidTmp = rs.getLong("PID_MAIN");
				Long pidTmp1 = rs.getLong("PID");
				String kindCode = rs.getString("KIND_MAIN");
				String kindCode1 = rs.getString("KIND_CODE");
				// if(pidTmp==520000002||pidTmp1==520000002){
				// log.info("");
				// }
				// 查出的同点poi若是检查对象的子，则跳过
				BasicObj obj = rows.get(pidTmp);
				IxPoiObj poiObj = (IxPoiObj) obj;
				List<IxPoiChildren> children = poiObj.getIxPoiChildrens();
				boolean isChild = false;
				for (IxPoiChildren c : children) {
					if (c.getChildPoiPid() == pidTmp1) {
						isChild = true;
						break;
					}
				}
				if (isChild) {
					continue;
				}

				map.put("pidTmp", pidTmp);
				map.put("kindCode", kindCode);
				poiList.put(pidTmp, map);

				if (!poiMap.containsKey(pidTmp)) {
					poiMap.put(pidTmp, new HashSet<Long>());
					poiMap.get(pidTmp).add(pidTmp1);
					kindMap.put(pidTmp, kindCode1);
				} else if (kindMap.get(pidTmp).equals("120101")
						&& (kindCode1.equals("200104") || kindCode1.equals("200103"))) {// 200103\200104\120101
					poiMap.put(pidTmp, new HashSet<Long>());
					poiMap.get(pidTmp).add(pidTmp1);
					kindMap.put(pidTmp, kindCode1);
				} else if (kindMap.get(pidTmp).equals("200104") && kindCode1.equals("200103")) {// 200103\200104\120101
					poiMap.put(pidTmp, new HashSet<Long>());
					poiMap.get(pidTmp).add(pidTmp1);
					kindMap.put(pidTmp, kindCode1);
				} else if (kindMap.get(pidTmp).equals(kindCode1)) {
					poiMap.get(pidTmp).add(pidTmp1);
				}
			}
		}catch (SQLException e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(rs);
			DBUtils.closeStatement(pstmt);
		}
		if(poiList==null||poiList.size()==0)
	{
		return;
	}
		log.info("FM-14Sum-11-08:批量加载推荐父");
	// 2.加载筛选出的顶级推荐父
	Set<Long> referParentPids = new HashSet<Long>();for(
	Set<Long> ps:poiMap.values())
	{
		referParentPids.addAll(ps);
	}
	Set<String> referSubrow = new HashSet<String>();referSubrow.add("IX_POI_CHILDREN");
	Map<Long, BasicObj> result = getCheckRuleCommand().loadReferObjs(referParentPids, ObjectName.IX_POI, referSubrow,
			false);
	// 3.批量加载推荐父对应的子
	log.info("FM-14Sum-11-08:批量加载推荐父对应的子");
	Set<Long> referChildPids = new HashSet<Long>();for(
	Long parent:referParentPids)
	{
		if (!result.containsKey(parent)) {
			continue;
		}
		List<IxPoiChildren> cs = ((IxPoiObj) result.get(parent)).getIxPoiChildrens();
		if (cs == null || cs.size() == 0) {
			continue;
		}
		for (IxPoiChildren c : cs) {
			referChildPids.add(c.getChildPoiPid());
		}
	}
	Map<Long, BasicObj> resultcMap = new HashMap<Long, BasicObj>();if(referChildPids!=null&&referChildPids.size()>0)
	{
		resultcMap = getCheckRuleCommand().loadReferObjs(referChildPids, ObjectName.IX_POI, null, false);
		if (resultcMap != null && resultcMap.size() > 0) {
			result.putAll(resultcMap);
		}
	}
	// 判断推荐的父poi是否有与当前poi分类相同的子，没有则报log
	// Map<Long, BasicObj> refers =
	// getCheckRuleCommand().getReferDatas().get(ObjectName.IX_POI);
	// 去重用，若targets重复（不判断顺序，只要pid相同即可），则不重复报。否则报出
	Set<Long> filterPid = new HashSet<Long>();for(
	Long pidC:poiList.keySet())
	{
		String kindCode = (String) poiList.get(pidC).get("kindCode");

		Set<Long> errorPids = new HashSet<Long>();
		for (Long p : poiMap.get(pidC)) {
			List<IxPoiChildren> cs = ((IxPoiObj) result.get(p)).getIxPoiChildrens();
			if (cs == null || cs.size() == 0) {
				errorPids.add(p);
				continue;
			}
			boolean haskind = false;
			for (IxPoiChildren c : cs) {
				IxPoi cObj = (IxPoi) result.get(c.getChildPoiPid()).getMainrow();
				if (cObj.getKindCode().equals(kindCode)) {
					haskind = true;
					break;
				}
			}
			if (!haskind) {
				errorPids.add(p);
			}
		}
		if (errorPids == null || errorPids.size() == 0) {
			continue;
		}
		String target = "[IX_POI," + pidC + "]";
		for (Long tmp : errorPids) {
			target = target + ";[IX_POI," + tmp + "]";
		}
		if (!(filterPid.contains(pidC) && filterPid.containsAll(errorPids))) {
			if ("200103".equals(kindMap.get(pidC))) {
				setCheckResult("", target, 0, "与父分类（200103大厦）的设施同点，却没有建立父子关系");
			} else if ("200104".equals(kindMap.get(pidC))) {
				setCheckResult("", target, 0, "与父分类（200104商务中心）的设施同点，却没有建立父子关系");
			} else if ("120101".equals(kindMap.get(pidC))) {
				setCheckResult("", target, 0, "与父分类（120101星级酒店）的设施同点，却没有建立父子关系");
			}
		}
		filterPid.add(pidC);
		filterPid.addAll(errorPids);
	}
	}

	@Override
	public void runCheck(BasicObj obj) throws Exception {
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
