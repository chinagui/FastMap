package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import oracle.net.aso.s;
import oracle.sql.STRUCT;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.api.metadata.model.ScPointNameckObj;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.vividsolutions.jts.geom.Geometry;
/**
 * FM-14Sum-12-10-01
 * 指定分类名称重复	DHM	可忽略
 * 检查条件：非删除POI对象；
 * 检查原则：
 * 同一行政区划范围内（admin_code相同），指定分类中出现官方原始中文名称相同的设施。按名称相同分组显示。
 * 检查指定分类：120101、120201、120202、140101、140201、140202、150101、160105、160203、160204、160205、160206、160207、
 * 170101、170102、180103、180104、180105、180106、180107、180209、180302、180303、180304、180306、180307、180308、
 * 180309、180400、190100、190101、190102、190103、190104、190105、190106、190107、190108、190109、190110、190112、
 * 190113、190114、190200、190201、190202、190203、190204、190400、190500、190501、190502、200101、200102、200103、
 * 230103、230105、230108、230111、230114、230125、230126、230127、230128、230129、140301、140302、140303、140304，
 * 排除官方原始中文名称包含‘ATM’的设施。
 * 官方原始中文名称与配置表SC_POINT_NAMECK中TYPE=1的关键词进行匹配，包含RESULT_KEY中关键字的需要还原成PRE_KEY中的全称后再进行是否重复的判断
 * Log：分类为***（kindcode），存在名称相同设施，名称为***"
 * @author zhangxiaoyi
 */
public class FM14Sum121001 extends BasicCheckRule {
	
	public void run() throws Exception {
		Map<Long, BasicObj> rows=getRowList();
		List<Long> pid=new ArrayList<Long>();
		String[] kindList={"120101","120201","120202","140101","140201","140202","150101","160105","160203","160204","160205","160206","160207",
				"170101","170102","180103","180104","180105","180106","180107","180209","180302","180303","180304","180306","180307","180308",
				"180309","180400","190100","190101","190102","190103","190104","190105","190106","190107","190108","190109","190110","190112",
				"190113","190114","190200","190201","190202","190203","190204","190400","190500","190501","190502","200101","200102","200103",
				"230103","230105","230108","230111","230114","230125","230126","230127","230128","230129","140301","140302","140303","140304"};
		for(Long key:rows.keySet()){
			BasicObj obj=rows.get(key);
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi =(IxPoi) poiObj.getMainrow();
			//已删除的数据不检查
			if(poi.getOpType().equals(OperationType.PRE_DELETED)){
				continue;}
			
			String kind=poi.getKindCode();
			if(Arrays.asList(kindList).contains(kind)){pid.add(poi.getPid());}
		}
		if(pid==null||pid.size()==0){return;}
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
				+ " (SELECT N.NAME, P.PID,P.GEOMETRY, P.MESH_ID, P.KIND_CODE, A.ADMIN_ID"
				+ "    FROM IX_POI_NAME N, IX_POI P, AD_ADMIN A"
				+ "   WHERE N.NAME_CLASS = 1"
				+ "     AND N.NAME_TYPE = 2"
				+ "     AND N.LANG_CODE IN ('CHI', 'CHT')"
				+ "     AND INSTR(N.NAME, 'ＡＴＭ') = 0 AND INSTR(N.NAME, 'ATM') = 0 "
				+ "     AND N.POI_PID = P.PID"
				+ "     AND P.REGION_ID = A.REGION_ID "
				+ "		AND N.U_RECORD <>2 AND P.U_RECORD <>2 AND A.U_RECORD <>2)"
				+ " SELECT T1.NAME, T1.PID,T1.GEOMETRY, T1.MESH_ID,T1.KIND_CODE, T2.NAME COMPARE_NAME, T2.PID COMPARE_PID"
				+ "  FROM T T1, T T2"
				+ " WHERE T1.KIND_CODE = T2.KIND_CODE"
				+ "   AND T1.ADMIN_ID = T2.ADMIN_ID"
				+ "   AND T1.PID != T2.PID"
				+ "   AND T1."+pidString
				+ " ORDER BY T1.PID";
		log.info("FM-14Sum-12-10-01 sql:"+sqlStr);
		PreparedStatement pstmt=conn.prepareStatement(sqlStr);;
		if(values!=null&&values.size()>0){
			for(int i=0;i<values.size();i++){
				pstmt.setClob(i+1,values.get(i));
			}
		}
		MetadataApi api=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
		List<ScPointNameckObj> typeD1List = api.scPointNameckTypeD1();
		ResultSet rs = pstmt.executeQuery();
		Map<Long, Set<Long>> errorList=new HashMap<Long, Set<Long>>();
		Map<String,String> preNameMap=new HashMap<String, String>();
		Set<String> nameSet=new HashSet<String>();
		Map<Long,Geometry> geoMap=new HashMap<Long, Geometry>();
		Map<Long,Integer> meshMap=new HashMap<Long, Integer>();
		Map<Long,String> kindMap=new HashMap<Long, String>();
		Map<Long,String> nameMap=new HashMap<Long, String>();
		while (rs.next()) {
			Long pid1=rs.getLong("PID");
			String name1=rs.getString("NAME");
			String preName1=name1;
			STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
			Geometry geometry = GeoTranslator.struct2Jts(struct, 100000, 0);
			geoMap.put(pid1, geometry);
			meshMap.put(pid1, rs.getInt("MESH_ID"));
			kindMap.put(pid1, rs.getString("KIND_CODE"));
			nameMap.put(pid1, name1);
			
			Long pid2=rs.getLong("COMPARE_PID");
			String name2=rs.getString("COMPARE_NAME");
			String preName2=name2;
			if(name1.equals(name2)){
				if(!errorList.containsKey(pid1)){errorList.put(pid1, new HashSet<Long>());}
				errorList.get(pid1).add(pid2);
				continue;
			}

			Set<String> name1PreKeySet = new HashSet<String>();
			Set<String> name2PreKeySet = new HashSet<String>();
			name1PreKeySet.add(name1);
			name2PreKeySet.add(name2);
			//官方原始中文名称与配置表SC_POINT_NAMECK中TYPE=1的关键词进行匹配，包含RESULT_KEY中关键字的需要还原成PRE_KEY中的全称				
			for(ScPointNameckObj obj:typeD1List){
				if(name1.contains(obj.getResultKey())){
					//preKey包含resulteKey,name包含preKey，那么name不需要进行替换
					if(!(obj.getPreKey().contains(obj.getResultKey())&&name1.contains(obj.getPreKey()))){
						preName1=name1.replace(obj.getResultKey(), obj.getPreKey());
						name1PreKeySet.add(preName1);
					}
				}
				if(name2.contains(obj.getResultKey())){
					//preKey包含resulteKey,name包含preKey，那么name不需要进行替换
					if(!(obj.getPreKey().contains(obj.getResultKey())&&name2.contains(obj.getPreKey()))){
						preName2=name2.replace(obj.getResultKey(), obj.getPreKey());
						name2PreKeySet.add(preName2);
					}
				}				
			}
			
			for(String name1PreKey:name1PreKeySet){
				if(name2PreKeySet.contains(name1PreKey)){
					if(!errorList.containsKey(pid1)){
						errorList.put(pid1, new HashSet<Long>());
					}
					errorList.get(pid1).add(pid2);
					continue;
				}
			}
			
			
//			boolean name1ok=false;
//			boolean name2ok=false;
//			if(nameSet.contains(name1)){
//				if(preNameMap.containsKey(name1)){preName1=preNameMap.get(name1);}
//				name1ok=true;
//			}
//			if(nameSet.contains(name2)){
//				if(preNameMap.containsKey(name2)){preName2=preNameMap.get(name2);}
//				name2ok=true;
//			}
//			
//			//官方原始中文名称与配置表SC_POINT_NAMECK中TYPE=1的关键词进行匹配，包含RESULT_KEY中关键字的需要还原成PRE_KEY中的全称				
//			for(ScPointNameckObj obj:typeD1List){
//				if(name1ok&&name2ok){break;}
//				if(!name1ok&&name1.contains(obj.getResultKey())){
//					name1ok=true;
//					//preKey包含resulteKey,name包含preKey，那么name不需要进行替换
//					if(!(obj.getPreKey().contains(obj.getResultKey())&&name1.contains(obj.getPreKey()))){
//						preName1=preName1.replace(obj.getResultKey(), obj.getPreKey());
//						preNameMap.put(name1, preName1);
//					}
//				}
//				if(!name2ok&&name2.contains(obj.getResultKey())){
//					name2ok=true;
//					//preKey包含resulteKey,name包含preKey，那么name不需要进行替换
//					if(!(obj.getPreKey().contains(obj.getResultKey())&&name2.contains(obj.getPreKey()))){
//						preName2=preName2.replace(obj.getResultKey(), obj.getPreKey());
//						preNameMap.put(name2, preName2);
//					}
//				}				
//			}
//			nameSet.add(name1);
//			nameSet.add(name2);
//			if(preName1.equals(preName2)){
//				if(!errorList.containsKey(pid1)){errorList.put(pid1, new HashSet<Long>());}
//				errorList.get(pid1).add(pid2);
//				continue;
//			}
		}
		
		Map<String, String> kindNameByKindCode = api.getKindNameByKindCode();
		
		if(errorList==null||errorList.size()==0){return;}
		//过滤相同pid
		Set<Long> filterPid = new HashSet<Long>();
		for(Long pid1:errorList.keySet()){
			String targets="[IX_POI,"+pid1+"]";
			for(Long pid2:errorList.get(pid1)){
				targets=targets+";[IX_POI,"+pid2+"]";
			}
			if(!(filterPid.contains(pid1)&&filterPid.containsAll(errorList.get(pid1)))){
				setCheckResult(geoMap.get(pid1), targets, meshMap.get(pid1),"分类为"+kindNameByKindCode.get(kindMap.get(pid1))+"，存在名称相同设施，名称为"+nameMap.get(pid1));
			}
			filterPid.add(pid1);
			filterPid.addAll(errorList.get(pid1));
		}
	}
	
	@Override
	public void runCheck(BasicObj obj) throws Exception {
	}
    
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
	}

}
