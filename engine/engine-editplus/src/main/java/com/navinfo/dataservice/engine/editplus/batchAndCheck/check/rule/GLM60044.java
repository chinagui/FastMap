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

import org.apache.commons.dbutils.DbUtils;

import oracle.sql.STRUCT;

import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.vividsolutions.jts.geom.Geometry;
/**
 * GLM60044
 * 检查条件：以下条件其中之一满足时，需要进行检查：
 * 对IX_POI表中“STATE(状态)”字段不为“1（删除）”的进行检查。
 * 检查原则： 
 * 如果POI标准化官方中文名称、显示坐标、种别、chain与其他非删除POI的标准化官方中文名称相同、显示坐标、种别、chain全部相同，则这些POI全部报出
 * 屏蔽：
 * POI的类别为、磁悬浮主点（230115）、地铁站主点（230112）、长途客运站出入口（行人导航）（230110）、出租车停靠站（230117）、
 * 出租车站出入口（230118）、公交车站、BRT（230101）、公交车站出入口（230102）、缆车站出入口（230122）、缆车站主点（230121）、
 * 水上公交车站出入口（230124）、水上公交站（230123）、小巴出入口（行人导航）（230120）、专线小巴站（230119）、彩票销售(180208)、
 * 停车场（230210）且两条数据备注字段（label）分别包含‘室内|地下’与‘室外’、充电桩(230227),不报log
 * @author zhangxiaoyi
 */
public class GLM60044 extends BasicCheckRule {
	public void run() throws Exception {
		Map<Long,Geometry> geoMap=new HashMap<Long, Geometry>();
		Map<Long,Integer> meshMap=new HashMap<Long, Integer>();
		Map<Long, BasicObj> rows=getRowList();
		List<Long> pid=new ArrayList<Long>();
		for(Long key:rows.keySet()){
			BasicObj obj=rows.get(key);
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi =(IxPoi) poiObj.getMainrow();
			Geometry geometry = poi.getGeometry();
			int meshId = poi.getMeshId();
			//已删除的数据不检查
			if(poi.getOpType().equals(OperationType.PRE_DELETED)){continue;}
			String kind=poi.getKindCode();
			if(kind.equals("230112")||kind.equals("230115")||kind.equals("230110")||kind.equals("230117")
					||kind.equals("230118")||kind.equals("230101")||kind.equals("230102")||kind.equals("230122")
					||kind.equals("230121")||kind.equals("230124")||kind.equals("230123")||kind.equals("230120")
					||kind.equals("230119")||kind.equals("180208")||kind.equals("230227"))
				{continue;}
			pid.add(poi.getPid());
			geoMap.put(poi.getPid(), geometry);
			meshMap.put(poi.getPid(), meshId);
		}
		//同点的设施，名称（name）、地址（address）、分类和品牌相同
		if(pid!=null&&pid.size()>0){
			String pids=pid.toString().replace("[", "").replace("]", "");
			List<Clob> values=new ArrayList<Clob>();
			String pidString="";
			Connection conn = this.getCheckRuleCommand().getConn();
			if(pid.size()>1000){
				Clob clob=ConnectionUtil.createClob(conn);
				clob.setString(1, pids);
				pidString=" PID IN (select to_number(column_value) from table(clob_to_table(?)))";
				values.add(clob);
				values.add(clob);
			}else{
				pidString=" PID IN ("+pids+")";
			}
			String sqlStr="WITH TEMP AS"
					+ " (SELECT PID,"
					+ "         KIND_CODE,"
					+ "         A.GEOMETRY,"
					+ "         B.NAME,"
					+ "         NVL(A.CHAIN, '0') CHAIN,"
					+ "         NVL(P.PARKING_TYPE, '0') PARKING_TYPE,"
					+ "			P.U_RECORD"
					+ "    FROM IX_POI A, IX_POI_NAME B, IX_POI_PARKING P"
					+ "   WHERE A.PID = B.POI_PID"
					//+ "     AND A.STATE <> 1"
					+ "     AND A.PID = P.POI_PID(+)"
					+ "     AND B.NAME_CLASS = 1"
					+ "     AND B.NAME_TYPE = 2"
					+ "     AND A.U_RECORD!=2"
					+ "     AND B.U_RECORD!=2"
					//+ "     AND P.U_RECORD!=2"
					+ "     AND B.LANG_CODE IN ('CHI', 'CHT'))"
					+ " SELECT T1.PID,T2.PID PID2"
					+ "  FROM TEMP T1, TEMP T2"
					+ " WHERE T1.KIND_CODE = T2.KIND_CODE"
					+ "   AND T1.GEOMETRY.SDO_POINT.X = T2.GEOMETRY.SDO_POINT.X"
					+ "   AND T1.GEOMETRY.SDO_POINT.Y = T2.GEOMETRY.SDO_POINT.Y"
					+ "   AND T1.NAME = T2.NAME"
					+ "   AND T1.CHAIN = T2.CHAIN"
					+ "   AND T1."+pidString
					+ "   AND T1.PID <> T2.PID"
					+ " MINUS"
					+ " SELECT T1.PID,T2.PID PID2"
					+ "  FROM TEMP T1, TEMP T2"
					+ " WHERE T1.KIND_CODE = '230210'"
					+ "   AND T2.KIND_CODE = '230210'"
					+ "   AND T1.NAME = T2.NAME"
					+ "   AND T1.CHAIN = T2.CHAIN"
					+ "   AND T1."+pidString
					+ "   AND ((T1.PARKING_TYPE = '4' AND T2.PARKING_TYPE = '1') OR"
					+ "       (T1.PARKING_TYPE = '1' AND T2.PARKING_TYPE = '4'))"
					+ "   AND T1.U_RECORD <>2 AND T2.U_RECORD <>2";
			PreparedStatement pstmt=null;
			ResultSet rs = null;
			try{
			pstmt=conn.prepareStatement(sqlStr);;
			if(values!=null&&values.size()>0){
				for(int i=0;i<values.size();i++){
					pstmt.setClob(i+1,values.get(i));
				}
			}
			rs = pstmt.executeQuery();
			Map<Long, Set<Long>> errorList=new HashMap<Long, Set<Long>>();
			while(rs.next()) {
				Long pidTmp1=rs.getLong("PID");
				Long pidTmp2=rs.getLong("PID2");
				if(!errorList.containsKey(pidTmp1)){errorList.put(pidTmp1, new HashSet<Long>());}
				errorList.get(pidTmp1).add(pidTmp2);
			}
			
			if(errorList==null||errorList.size()==0){return;}
			//过滤相同pid
			Set<Long> filterPid = new HashSet<Long>();
			for(Long pid1:errorList.keySet()){
				String targets="[IX_POI,"+pid1+"]";
				for(Long pid2:errorList.get(pid1)){
					targets=targets+";[IX_POI,"+pid2+"]";
				}
				if(!(filterPid.contains(pid1)&&filterPid.containsAll(errorList.get(pid1)))){
					setCheckResult(geoMap.get(pid1), targets, meshMap.get(pid1),"PID为"+pid1+","+errorList.get(pid1).toString().replace("[", "").replace("]", "")+"的POI对象的原始中文名称、分类、显示坐标、品牌完全相同，请确认");
				}
				filterPid.add(pid1);
				filterPid.addAll(errorList.get(pid1));
			}
		}catch (SQLException e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(pstmt);
		}
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
