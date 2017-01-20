package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import oracle.sql.STRUCT;

import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.vividsolutions.jts.geom.Geometry;
/**
 * FM-11Win-01-17
 * 检查条件：Lifecycle=3（新增）且kindcode!={130403,210215,180208,230222,230210}
 * 检查原则：
 * 100米内名称（name）和分类、品牌相同的新增设施和其他非删除设施，成对显示，颜色区分新增与删除
 * 当分类={230210、230213,230214}时，需要增加停车场建筑物类型一起判断
 * 报log：100米内存在相同的设施，请确认新增的POI是否正确
 * @author zhangxiaoyi
 */
public class FM11Win0117 extends BasicCheckRule {
	
	public void run() throws Exception {
		Map<Long, BasicObj> rows=getRowList();
		List<Long> pid1=new ArrayList<Long>();
		List<Long> pid2=new ArrayList<Long>();
		for(Long key:rows.keySet()){
			BasicObj obj=rows.get(key);
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi =(IxPoi) poiObj.getMainrow();
			//已删除的数据不检查
			if(poi.getOpType().equals(OperationType.PRE_DELETED)||!poi.getHisOpType().equals(OperationType.INSERT)){
				continue;}
			String kind=poi.getKindCode();
			if(kind.equals("130403")||kind.equals("210215")||kind.equals("180208")||kind.equals("230222")
					||kind.equals("230210")){continue;}
			if(kind.equals("230210")||kind.equals("230213")||kind.equals("230214")){pid2.add(poi.getPid());}
			else{pid1.add(poi.getPid());}
		}
		//100米内名称（name）和分类、品牌相同的新增设施和其他非删除设施
		if(pid1!=null&&pid1.size()>0){
			String pids=pid1.toString().replace("[", "").replace("]", "");
			Connection conn = this.getCheckRuleCommand().getConn();
			List<Clob> values=new ArrayList<Clob>();
			String pidString="";
			if(pid1.size()>1000){
				Clob clob=ConnectionUtil.createClob(conn);
				clob.setString(1, pids);
				pidString=" PID IN (select to_number(column_value) from table(clob_to_table(?)))";
				values.add(clob);
				values.add(clob);
			}else{
				pidString=" PID IN ("+pids+")";
			}
			String sqlStr="WITH T AS"
					+ " (SELECT P2.PID PID2, P2.GEOMETRY G2"
					+ "    FROM IX_POI         P1,"
					+ "         IX_POI         P2,"
					+ "         IX_POI_NAME    N1,"
					+ "         IX_POI_NAME    N2"
					+ "   WHERE P1.KIND_CODE = P2.KIND_CODE"
					+ "     AND NVL(P1.CHAIN, 0) = NVL(P2.CHAIN, 0)"
					+ "     AND P1.PID = N1.POI_PID"
					+ "     AND N1.NAME_CLASS = 1"
					+ "     AND N1.NAME_TYPE = 2"
					+ "     AND N1.LANG_CODE IN ('CHI', 'CHT')"
					+ "     AND P2.PID = N2.POI_PID"
					+ "     AND N2.NAME_CLASS = 1"
					+ "     AND N2.NAME_TYPE = 2"
					+ "     AND N2.LANG_CODE IN ('CHI', 'CHT')"
					+ "     AND N1.NAME = N2.NAME"
					+ "     AND P2.U_RECORD!=2"
					+ "     AND P1."+pidString
					+ "     AND P1.PID != P2.PID"
					+ "     )"
					+ " SELECT /*+ NO_MERGE(T)*/"
					+ " P.PID,P.GEOMETRY,P.MESH_ID, PID2"
					+ "  FROM T, IX_POI P"
					+ " WHERE SDO_GEOM.SDO_DISTANCE(P.GEOMETRY, G2, 0.00000005) < 100"
					+ "   AND P."+pidString;
			PreparedStatement pstmt=conn.prepareStatement(sqlStr);;
			if(values!=null&&values.size()>0){
				for(int i=0;i<values.size();i++){
					pstmt.setClob(i+1,values.get(i));
				}
			}			
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				Long pidTmp1=rs.getLong("PID");
				Long pidTmp2=rs.getLong("PID2");
				STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
				Geometry geometry = GeoTranslator.struct2Jts(struct, 100000, 0);
				String targets="[IX_POI,"+pidTmp1+"];[IX_POI,"+pidTmp2+"]";
				setCheckResult(geometry, targets, rs.getInt("MESH_ID"));
			}
		}
		//100米内名称（name）和分类、品牌相同的新增设施和其他非删除设施,当分类={230210,230213,230214}时，需要增加停车场建筑物类型一起判断
		if(pid2!=null&&pid2.size()>0){
			String pids=pid1.toString().replace("[", "").replace("]", "");
			Connection conn = this.getCheckRuleCommand().getConn();
			List<Clob> values=new ArrayList<Clob>();
			String pidString="";
			if(pid1.size()>1000){
				Clob clob=ConnectionUtil.createClob(conn);
				clob.setString(1, pids);
				pidString=" PID IN (select to_number(column_value) from table(clob_to_table(?)))";
				values.add(clob);
				values.add(clob);
			}else{
				pidString=" PID IN ("+pids+")";
			}
			String sqlStr="WITH T AS"
					+ " (SELECT P2.PID PID2, P2.GEOMETRY G2"
					+ "    FROM IX_POI         P1,"
					+ "         IX_POI         P2,"
					+ "         IX_POI_NAME    N1,"
					+ "         IX_POI_NAME    N2"
					+ "   WHERE P1.KIND_CODE = P2.KIND_CODE"
					+ "     AND NVL(P1.CHAIN, 0) = NVL(P2.CHAIN, 0)"
					+ "     AND P1.PID = N1.POI_PID"
					+ "     AND N1.NAME_CLASS = 1"
					+ "     AND N1.NAME_TYPE = 2"
					+ "     AND N1.LANG_CODE IN ('CHI', 'CHT')"
					+ "     AND P2.PID = N2.POI_PID"
					+ "     AND N2.NAME_CLASS = 1"
					+ "     AND N2.NAME_TYPE = 2"
					+ "     AND N2.LANG_CODE IN ('CHI', 'CHT')"
					+ "     AND N1.NAME = N2.NAME"
					+ "     AND NVL(P1.LABEL,'')=NVL(P2.LABEL,'')"
					+ "     AND P2.U_RECORD!=2"
					+ "     AND P1."+pidString
					+ "     AND P1.PID != P2.PID"
					+ "     )"
					+ " SELECT /*+ NO_MERGE(T)*/"
					+ " P.PID,P.GEOMETRY,P.MESH_ID, PID2"
					+ "  FROM T, IX_POI P"
					+ " WHERE SDO_GEOM.SDO_DISTANCE(P.GEOMETRY, G2, 0.00000005) < 100"
					+ "   AND P."+pidString;
			PreparedStatement pstmt=conn.prepareStatement(sqlStr);;
			if(values!=null&&values.size()>0){
				for(int i=0;i<values.size();i++){
					pstmt.setClob(i+1,values.get(i));
				}
			}			
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				Long pidTmp1=rs.getLong("PID");
				Long pidTmp2=rs.getLong("PID2");
				STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
				Geometry geometry = GeoTranslator.struct2Jts(struct, 100000, 0);
				String targets="[IX_POI,"+pidTmp1+"];[IX_POI,"+pidTmp2+"]";
				setCheckResult(geometry, targets, rs.getInt("MESH_ID"));
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
