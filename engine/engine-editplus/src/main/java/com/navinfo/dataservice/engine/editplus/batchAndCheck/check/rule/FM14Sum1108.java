package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.sql.Clob;
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

import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
import com.vividsolutions.jts.geom.Geometry;

import oracle.sql.STRUCT;

/**
 * 
 * 检查条件：非删除POI且存在父子关系；
 * 检查原则：
 * 与父分类（200103大厦\200104商务中心\120101星级酒店）的设施同点，却没有建立父子关系，
 * 报log：与父分类（200103大厦\200104商务中心\120101星级酒店）的设施同点，却没有建立父子关系！
 * 备注：没有父的POI需要报出，有父的POI不用报。
 * 当这三个父分类同时存在时，按“200103\200104\120101”从左到右的优先顺序，报出优先的分类作为父POI。
 * 备注：当POI分类一样且同点位没有更高优先级POI时，该POI已有同分类的子时，不用报log
 *
 */
public class FM14Sum1108 extends BasicCheckRule {
	
	Map<Long, Long> parentIds = new HashMap<Long, Long>();

	public void run() throws Exception {
		Map<Long, BasicObj> rows=getRowList();
		loadReferDatas(rows.values());
		List<Long> pid=new ArrayList<Long>();
		for(Long key:rows.keySet()){
			BasicObj obj=rows.get(key);
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi =(IxPoi) poiObj.getMainrow();
			//已删除的数据不检查
			if(poi.getOpType().equals(OperationType.PRE_DELETED)){continue;}
			pid.add(poi.getPid());
		}
		//判断1和2条件是否满足
		if(pid!=null&&pid.size()>0){
			String pids=pid.toString().replace("[", "").replace("]", "");
			Connection conn = this.getCheckRuleCommand().getConn();
			List<Clob> values=new ArrayList<Clob>();
			String pidString="";
			if(pid.size()>1000){
				Clob clob=ConnectionUtil.createClob(conn);
				clob.setString(1, pids);
				pidString=" PID IN (select to_number(column_value) from table(clob_to_table(?)))";
				values.add(clob);
			}else{
				pidString=" PID IN ("+pids+")";
			}
				
			String sqlStr="WITH T AS (" 
					+"SELECT P1.PID PID1,P1.GEOMETRY G1,P1.KIND_CODE"
					+"	FROM IX_POI P1"
					+"	WHERE"
					+"		P1.KIND_CODE IN ('200103', '200104', '120101')"
					+"	AND P1.U_RECORD <> 2)"
					+"	SELECT	/*+ NO_MERGE(T)*/ P.PID,P.GEOMETRY,P.MESH_ID,P.KIND_CODE,T.Kind_Code K1,T.PID1"
					+"	FROM"
					+"	T,"
					+"	IX_POI P"
					+"	WHERE"
					+"	SDO_GEOM.SDO_DISTANCE (P .GEOMETRY, G1, 0.00000005) < 3"
					+"	AND P ."+pidString
					+"	AND P.U_RECORD <> 2 AND P.PID <> T.PID1";
			PreparedStatement pstmt=conn.prepareStatement(sqlStr);;
			if(values!=null&&values.size()>0){
				for(int i=0;i<values.size();i++){
					pstmt.setClob(i+1,values.get(i));
				}
			}			
			ResultSet rs = pstmt.executeQuery();
			List<Map<String,Object>> poiList = new ArrayList<Map<String,Object>>();
			//key:子pid,value:父pid集合
			Map<Long,List<Long>> poiMap = new HashMap<Long,List<Long>>();
			//key:父pid,value:父kindcode
			Map<Long,String> parentMap = new HashMap<Long,String>();
			while (rs.next()) {
				Map<String,Object> map = new HashMap<String,Object>();
				Long pidTmp=rs.getLong("PID");
				Long pidTmp1=rs.getLong("PID1");
				String kindCode = rs.getString("KIND_CODE");
				String kindCode1 = rs.getString("K1");
				STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
				Geometry geometry = GeoTranslator.struct2Jts(struct, 100000, 0);
				int meshId = rs.getInt("MESH_ID");
				map.put("pidTmp", pidTmp);
				map.put("kindCode", kindCode);
				//map.put("pidTmp1", pidTmp1);
				map.put("kindCode1", kindCode1);
				map.put("geometry", geometry);
				map.put("meshId", meshId);
				poiList.add(map);
				//key:子pid,value:父pid集合
				if (!poiMap.containsKey(pidTmp)) {
					poiMap.put(pidTmp, new ArrayList<Long>());
				}
				poiMap.get(pidTmp).add(pidTmp1);
				//key:父pid,value:父kindcode
				parentMap.put(pidTmp1, kindCode1);
			}
			long pid0 = 0;
			for (Map<String, Object> map : poiList) {
				long pidC = (long) map.get("pidTmp");
				String kindCode = (String) map.get("kindCode");
				String kindCode1 = (String) map.get("kindCode1");
				Geometry geometry = (Geometry) map.get("geometry");
				int meshId = (int) map.get("meshId");
				//判断是否为同一poi
				if(pid0 == pidC){continue;}else{pid0=pidC;}
				//有父的POI不用报
				if (parentIds.containsKey(pidC)) {continue;}
				//当POI分类一样且同点位没有更高优先级POI时，该POI已有同分类的子时，不用报log
				Set<Long> pidList = new HashSet<Long>();
				Set<Long> pidList03 = new HashSet<Long>();
				Set<Long> pidList04 = new HashSet<Long>();
				Set<Long> pidList01 = new HashSet<Long>();
				List<Long> pidPList = poiMap.get(pidC);
				if(pidPList != null && !pidPList.isEmpty()){
					for (Long p : pidPList) {
						String kind = parentMap.get(p);
						if("200103".equals(kind)){
							pidList03.add(p);
						}else if("200104".equals(kind)){
							pidList04.add(p);
						}else if("120101".equals(kind)){
							pidList01.add(p);
						}
					}
				}
				if(!pidList03.isEmpty()){
					pidList.addAll(pidList03);
				}else if(!pidList04.isEmpty()){
					pidList.addAll(pidList04);
				}else if(!pidList01.isEmpty()){
					pidList.addAll(pidList01);
				}
				//查询子
				List<Long> childIds = IxPoiSelector.getChildrenPidsByParentPid(getCheckRuleCommand().getConn(), pidList);
				boolean flag = false;
				if(childIds != null&& !childIds.isEmpty()){
					Set<String> referSubrow=new HashSet<String>();
					//referSubrow.add("IX_POI_NAME");
					Map<Long, BasicObj> referObjs = getCheckRuleCommand().loadReferObjs(childIds, ObjectName.IX_POI, referSubrow, false);
					for(Map.Entry<Long, BasicObj> entry : referObjs.entrySet()){
						IxPoiObj parentPoiObj = (IxPoiObj) entry.getValue();
						IxPoi parentPoi = (IxPoi) parentPoiObj.getMainrow();
						String kindCodeC = parentPoi.getKindCode();
						if(kindCodeC == null){continue;}
						if(kindCode.equals(kindCodeC)){flag = true;break;}
					}
				}
				if(flag){continue;}
				String target = "[IX_POI,"+pidC+"]";
				if ("200103".equals(kindCode1)) {
					setCheckResult(geometry, target, meshId,"与父分类（200103大厦）的设施同点，却没有建立父子关系");
				} else if ("200104".equals(kindCode1)) {
					setCheckResult(geometry, target, meshId,"与父分类（200104商务中心）的设施同点，却没有建立父子关系");
				} else if ("120101".equals(kindCode1)) {
					setCheckResult(geometry, target, meshId,"与父分类（120101星级酒店）的设施同点，却没有建立父子关系");
				}
			}
		}
	}
	@Override
	public void runCheck(BasicObj obj) throws Exception {
//		if (obj.objName().equals(ObjectName.IX_POI)) {
//			IxPoiObj poiObj = (IxPoiObj) obj;
//			IxPoi poi = (IxPoi) poiObj.getMainrow();
//			//有父的POI不用报
//			if (parentIds.containsKey(poi.getPid())) {return;}
//			
//			String sqlStr=" WITH T AS"
//					+ " (SELECT P1.PID PID1, P1.GEOMETRY G1,P1.KIND_CODE"
//					+ " FROM IX_POI P1"
//					+ " WHERE P1.KIND_CODE IN ('200103','200104','120101')"
//					+ " AND P1.U_RECORD != 2"
//					+ " AND P1.PID !=:1)"
//					+ " SELECT /*+ NO_MERGE(T)*/"
//					+ " T.Kind_Code,PID2"
//					+ " FROM T, IX_POI P"
//					+ " WHERE SDO_GEOM.SDO_DISTANCE(P.GEOMETRY, G2, 0.00000005) < 3"
//					+" AND P.PID =:2";
//			
//			Connection conn = this.getCheckRuleCommand().getConn();
//			
//			PreparedStatement pstmt = null;
//			
//			ResultSet rs = null;
//			try {
//				List<String> kindCodeList = new ArrayList<String>();
//				pstmt=conn.prepareStatement(sqlStr);
//				pstmt.setLong(1, poi.getPid());
//				pstmt.setLong(2, poi.getPid());
//				rs = pstmt.executeQuery();
//				while (rs.next()) {
//					kindCodeList.add(rs.getString("KIND_CODE"));
//				}
//				if (kindCodeList.contains("200103")) {
//					setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(),"与父分类（200103大厦）的设施同点，却没有建立父子关系");
//				} else if (kindCodeList.contains("200104")) {
//					setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(),"与父分类（200104商务中心）的设施同点，却没有建立父子关系");
//				} else if (kindCodeList.contains("120101")) {
//					setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(),"与父分类（120101星级酒店）的设施同点，却没有建立父子关系");
//				}
//			} catch (Exception e) {
//				throw e;
//			} finally {
//				DbUtils.close(rs);
//	    		DbUtils.close(pstmt);
//			}
//		}
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
