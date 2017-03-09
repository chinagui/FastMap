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
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChildren;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
import com.vividsolutions.jts.geom.Geometry;

import oracle.net.aso.k;
import oracle.net.aso.p;
import oracle.sql.STRUCT;

/**
 * FM-14Sum-11-08
 * 检查条件：非删除POI且存在父子关系；
 * 检查原则：
 * 与父分类（200103大厦\200104商务中心\120101星级酒店）的设施同点，却没有建立父子关系，
 * 报log：与父分类（200103大厦\200104商务中心\120101星级酒店）的设施同点，却没有建立父子关系！
 * 备注：没有父的POI需要报出，有父的POI不用报。
 * 当这三个父分类同时存在时，按“200103\200104\120101”从左到右的优先顺序，报出优先的分类作为父POI。
 * 备注：当POI分类一样且同点位没有更高优先级POI时，该POI已有同分类的子时，不用报log
 *20170306 BY ZXY 规则优化
 */
public class FM14Sum1108 extends BasicCheckRule {
	
	Map<Long, Long> parentIds = new HashMap<Long, Long>();
	Map<Long, BasicObj> childData=new HashMap<Long, BasicObj>();

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
			//有父的POI不用报
			if(parentIds.containsKey(poi.getPid())){continue;}
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
				
			String sqlStr="WITH T AS"
					+ " (SELECT P1.PID PID1, P1.GEOMETRY G1, P1.KIND_CODE"
					+ "    FROM IX_POI P1"
					+ "   WHERE P1.KIND_CODE IN ('200103', '200104', '120101')"
					+ "     AND P1.U_RECORD <> 2)"
					+ " SELECT /*+ NO_MERGE(T) PARALLEL(P)*/"
					+ " P.PID, P.GEOMETRY, P.MESH_ID, P.KIND_CODE, T.KIND_CODE K1, T.PID1"
					+ "  FROM T, IX_POI P"
					+ " WHERE SDO_GEOM.SDO_DISTANCE(P.GEOMETRY, G1, 0.00000005) < 3"
					+ "   AND P."+pidString
					+ "   AND P.U_RECORD <> 2"
					+ "   AND P.PID <> T.PID1";
			log.info("FM-14Sum-11-08:"+sqlStr);
			PreparedStatement pstmt=conn.prepareStatement(sqlStr);;
			if(values!=null&&values.size()>0){
				for(int i=0;i<values.size();i++){
					pstmt.setClob(i+1,values.get(i));
				}
			}			
			ResultSet rs = pstmt.executeQuery();
			Map<Long,Map<String,Object>> poiList = new HashMap<Long,Map<String,Object>>();
			//同点位的poi中顶级kind及对应的pid
			//key:子pid,value:顶级kind的pid
			Map<Long,Set<Long>> poiMap = new HashMap<Long,Set<Long>>();
			//key:子pid,value:顶级kindcode
			Map<Long,String> kindMap = new HashMap<Long,String>();
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
				//map.put("kindCode1", kindCode1);
				map.put("geometry", geometry);
				map.put("meshId", meshId);
				poiList.put(pidTmp,map);
				
				if (!poiMap.containsKey(pidTmp)) {
					poiMap.put(pidTmp, new HashSet<Long>());
					poiMap.get(pidTmp).add(pidTmp1);
					kindMap.put(pidTmp, kindCode1);
				}else if(kindMap.get(pidTmp).equals("120101")&&(kindCode1.equals("200104")||kindCode1.equals("200103"))){//200103\200104\120101
					poiMap.put(pidTmp, new HashSet<Long>());
					poiMap.get(pidTmp).add(pidTmp1);
					kindMap.put(pidTmp, kindCode1);
				}else if(kindMap.get(pidTmp).equals("200104")&&kindCode1.equals("200103")){//200103\200104\120101
					poiMap.put(pidTmp, new HashSet<Long>());
					poiMap.get(pidTmp).add(pidTmp1);
					kindMap.put(pidTmp, kindCode1);
				}else if(kindMap.get(pidTmp).equals(kindCode1)){
					poiMap.get(pidTmp).add(pidTmp1);
				}				
			}
			if(poiList==null||poiList.size()==0){return;}
			log.info("FM-14Sum-11-08:批量加载推荐父");
			//加载推荐父
			Set<Long> referParentPids=new HashSet<Long>();
			for(Set<Long> ps:poiMap.values()){
				referParentPids.addAll(ps);
			}
			Set<String> referSubrow=new HashSet<String>();
			referSubrow.add("IX_POI_CHILDREN");
			Map<Long, BasicObj> result = getCheckRuleCommand().loadReferObjs(referParentPids, ObjectName.IX_POI, referSubrow, false);
			//加载子
			log.info("FM-14Sum-11-08:批量加载推荐父对应的子");
			Set<Long> referChildPids=new HashSet<Long>();
			for(Long parent:referParentPids){
				if(!result.containsKey(parent)){continue;}
				List<IxPoiChildren> cs = ((IxPoiObj)result.get(parent)).getIxPoiChildrens();
				if(cs==null||cs.size()==0){continue;}
				for(IxPoiChildren c:cs){
					referChildPids.add(c.getChildPoiPid());
				}
			}
			Map<Long, BasicObj> resultcMap=new HashMap<Long, BasicObj>();
			if(referChildPids!=null&&referChildPids.size()>0){
				resultcMap=getCheckRuleCommand().loadReferObjs(referChildPids, ObjectName.IX_POI, null, false);
				if(resultcMap!=null&&resultcMap.size()>0){
					result.putAll(resultcMap);
				}
			}
			//判断推荐的父poi是否有与当前poi分类相同的子，没有则报log
			//Map<Long, BasicObj> refers = getCheckRuleCommand().getReferDatas().get(ObjectName.IX_POI);
			for (Long pidC : poiList.keySet()) {
				String kindCode = (String) poiList.get(pidC).get("kindCode");
				Geometry geometry = (Geometry) poiList.get(pidC).get("geometry");
				int meshId = (int) poiList.get(pidC).get("meshId");
				
				Set<Long> errorPids=new HashSet<Long>();
				for(Long p:poiMap.get(pidC)){
					List<IxPoiChildren> cs = ((IxPoiObj)result.get(p)).getIxPoiChildrens();
					if(cs==null||cs.size()==0){
						errorPids.add(p);
						continue;}
					boolean haskind=false;
					for(IxPoiChildren c:cs){
						IxPoi cObj = (IxPoi)result.get(c.getChildPoiPid()).getMainrow();
						if(cObj.getKindCode().equals(kindCode)){
							haskind=true;break;
						}
					}
					if(!haskind){errorPids.add(p);}
				}
				if(errorPids==null||errorPids.size()==0){continue;}
				String target="[IX_POI,"+pidC+"]";
				for(Long tmp:errorPids){
					target=target+";[IX_POI,"+tmp+"]";}
				if ("200103".equals(kindMap.get(pidC))) {
					setCheckResult(geometry, target, meshId,"与父分类（200103大厦）的设施同点，却没有建立父子关系");
				} else if ("200104".equals(kindMap.get(pidC))) {
					setCheckResult(geometry, target, meshId,"与父分类（200104商务中心）的设施同点，却没有建立父子关系");
				} else if ("120101".equals(kindMap.get(pidC))) {
					setCheckResult(geometry, target, meshId,"与父分类（120101星级酒店）的设施同点，却没有建立父子关系");
				}
			}
		}
	}
	@Override
	public void runCheck(BasicObj obj) throws Exception {
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		Set<Long> pidList = new HashSet<Long>();
		Set<Long> childPidList = new HashSet<Long>();
		for (BasicObj obj : batchDataList) {
			pidList.add(obj.objPid());
			IxPoiObj poiObj=(IxPoiObj) obj;
			List<IxPoiChildren> cs = poiObj.getIxPoiChildrens();
			if(cs==null||cs.size()==0){continue;}
			for(IxPoiChildren c:cs){
				childPidList.add(c.getChildPoiPid());
			}
		}
		parentIds = IxPoiSelector.getParentPidsByChildrenPids(getCheckRuleCommand().getConn(), pidList);
		childData = getCheckRuleCommand().loadReferObjs(childPidList, ObjectName.IX_POI, null, false);
	}

}
