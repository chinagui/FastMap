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

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
import com.vividsolutions.jts.geom.Geometry;

import oracle.sql.STRUCT;

/**
 * @ClassName FMYW20031
 * @author Han Shaoming
 * @date 2017年3月2日 上午10:13:03
 * @Description TODO
 * 检查对象：非删除POI对象
 * 检查原则：对于不同点位、不同名称无父子关系的POI或有相同父的poi，不允许存在相同的电话号码（contact）。
 * 否则报log：不同POI电话相同！
 */
public class FMYW20031 extends BasicCheckRule {

	public void run() throws Exception {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try{
			Map<Long, BasicObj> rows=getRowList();
			List<Long> pid=new ArrayList<Long>();
			for(Long key:rows.keySet()){
				BasicObj obj=rows.get(key);
				IxPoiObj poiObj=(IxPoiObj) obj;
				IxPoi poi =(IxPoi) poiObj.getMainrow();
				//已删除的数据不检查
				if(poi.getOpType().equals(OperationType.PRE_DELETED)){
					continue;}
				pid.add(poi.getPid());
			}
			//对于不同点位、不同名称无父子关系的POI或有相同父的poi，不允许存在相同的电话号码（contact）
			if(pid!=null&&pid.size()>0){
				String pids=pid.toString().replace("[", "").replace("]", "");
				conn = this.getCheckRuleCommand().getConn();
				List<Clob> values=new ArrayList<Clob>();
				String pidString="";
				if(pid.size()>1000){
					Clob clob=ConnectionUtil.createClob(conn);
					clob.setString(1, pids);
					pidString=" PID IN (select to_number(column_value) from table(clob_to_table(?)))";
					values.add(clob);
					values.add(clob);
				}else{
					pidString=" PID IN ("+pids+")";
				}
				String sqlStr="WITH T AS"
						+ "	(SELECT P2.PID PID2, P2.GEOMETRY G2, P1.PID PID1"
						+ "	   FROM IX_POI         P1,"
						+ "			IX_POI         P2,"
						+ "			IX_POI_NAME    N1,"
						+ "			IX_POI_NAME    N2,"
						+ "					IX_POI_CONTACT C1,"
						+ "					IX_POI_CONTACT C2"
						+ "	  WHERE P1.PID = N1.POI_PID"
						+ "		AND N1.NAME_CLASS = 1"
						+ "		AND N1.NAME_TYPE = 2"
						+ "		AND N1.LANG_CODE IN ('CHI', 'CHT')"
						+ "		AND P2.PID = N2.POI_PID"
						+ "		AND N2.NAME_CLASS = 1"
						+ "		AND N2.NAME_TYPE = 2"
						+ "		AND N2.LANG_CODE IN ('CHI', 'CHT')"
						+ "		AND N1.NAME <> N2.NAME"
						+ "			AND P1.PID = C1.POI_PID"
						+ "			AND P2.PID = C2.POI_PID"
						+ "			AND C1.CONTACT = C2.CONTACT"
						+ "     AND C1.CONTACT_TYPE = C2.CONTACT_TYPE"
						+ "		AND P1.U_RECORD!=2"
						+ "		AND P2.U_RECORD!=2"
						+ "		AND N1.U_RECORD!=2"
						+ "		AND N2.U_RECORD!=2"
						+ "			AND C1.U_RECORD!=2"
						+ "		AND C2.U_RECORD!=2"
						+ "		AND P1."+pidString
						+ "		AND P1.PID != P2.PID"
						+ "		)"
						+ "	SELECT /*+ NO_MERGE(T)*/"
						+ "	P.PID,P.GEOMETRY,P.MESH_ID, PID2"
						+ "	 FROM T, IX_POI P"
						+ "	WHERE SDO_GEOM.SDO_DISTANCE(P.GEOMETRY, G2, 0.00000005) > 3"
						+ "	  AND P.PID = T.PID1"
						+ "	  AND P.U_RECORD!=2"
						+ "	  AND P."+pidString;
				pstmt = conn.prepareStatement(sqlStr);;
				if(values!=null&&values.size()>0){
					for(int i=0;i<values.size();i++){
						pstmt.setClob(i+1,values.get(i));
					}
				}			
				rs = pstmt.executeQuery();
				Map<Long, Set<Long>> errorList=new HashMap<Long, Set<Long>>();
				Map<Long,Geometry> geoMap=new HashMap<Long, Geometry>();
				Map<Long,Integer> meshMap=new HashMap<Long, Integer>();
				List<Map<String, Long>> resultList=new ArrayList<Map<String,Long>>();
				Set<Long> pidList=new HashSet<Long>();
				while (rs.next()) {
					Long pidTmp1=rs.getLong("PID");
					Long pidTmp2=rs.getLong("PID2");
					int meshId = rs.getInt("MESH_ID");
					STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
					Geometry geometry = GeoTranslator.struct2Jts(struct, 100000, 0);
					geoMap.put(pidTmp1, geometry);
					meshMap.put(pidTmp1, meshId);
					
					Map<String, Long> pidMap=new HashMap<String, Long>();
					pidMap.put("PID1", pidTmp1);
					pidMap.put("PID2", pidTmp2);
					resultList.add(pidMap);
					pidList.add(pidTmp1);
					pidList.add(pidTmp2);
				}
				//加载父子关系
				Map<Long, Long> parentMap = IxPoiSelector.getParentPidsByChildrenPids(this.getCheckRuleCommand().getConn(), pidList);
				//查询父子关系
				for(Map<String, Long> pidMap:resultList){
					Long pidTmp1=pidMap.get("PID1");
					Long pidTmp2=pidMap.get("PID2");
					boolean flag = false;
					//无父子关系
					if(!(parentMap.containsKey(pidTmp1)&&(parentMap.get(pidTmp1).equals(pidTmp2)))
							&&!(parentMap.containsKey(pidTmp2)&&(parentMap.get(pidTmp2).equals(pidTmp1)))){
						flag = true;
					}
					//有相同的父
					if(parentMap.containsKey(pidTmp1)&&parentMap.containsKey(pidTmp2)){
						Long pidP1 = parentMap.get(pidTmp1);
						Long pidP2 = parentMap.get(pidTmp2);
						if(pidP1 ==pidP2){
							flag = true;
						}
					}
					if(flag){
						if(!errorList.containsKey(pidTmp1)){errorList.put(pidTmp1, new HashSet<Long>());}
						errorList.get(pidTmp1).add(pidTmp2);
					}
				}	
				//过滤相同pid
				Set<Long> filterPid = new HashSet<Long>();
				for(Long pid1:errorList.keySet()){
					String targets="[IX_POI,"+pid1+"]";
					for(Long pid2:errorList.get(pid1)){
						targets=targets+";[IX_POI,"+pid2+"]";
					}
					if(!(filterPid.contains(pid1)&&filterPid.containsAll(errorList.get(pid1)))){
						setCheckResult(geoMap.get(pid1), targets, meshMap.get(pid1));
					}
					filterPid.add(pid1);
					filterPid.addAll(errorList.get(pid1));
				}
			}
		}catch(Exception e){
			log.error(e.getMessage(),e);
			throw e;
		}finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(pstmt);
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
