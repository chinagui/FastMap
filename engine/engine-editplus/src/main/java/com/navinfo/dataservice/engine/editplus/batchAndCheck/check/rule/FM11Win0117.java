package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
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
 * FM-11Win-01-17
 * 检查条件：Lifecycle=3（新增）且kindcode!={130403,210215,180208,230222}
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
			if(kind.equals("130403")||kind.equals("210215")||kind.equals("180208")||kind.equals("230222"))
				{continue;}
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
			}else{
				pidString=" PID IN ("+pids+")";
			}

					String sqlStr="SELECT /*+ORDERD*/ P2.PID PID2, P2.GEOMETRY, P1.PID PID1,P1.GEOMETRY,P1.MESH_ID"
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
					+ "     AND P1.U_RECORD!=2"
					+ "     AND N1.U_RECORD!=2"
					+ "     AND N2.U_RECORD!=2"
					+"		AND SDO_NN(P2.GEOMETRY, P1.GEOMETRY, 'DISTANCE=100 UNIT=METER') = 'TRUE'"
					+ "     AND P1."+pidString
					+ "     AND P1.PID != P2.PID";

			log.info("FM11Win0117:"+sqlStr);
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
			//过滤相同pid
			Set<String> filterPid = new HashSet<String>();
			while (rs.next()) {
				Long pidTmp1=rs.getLong("PID1");
				Long pidTmp2=rs.getLong("PID2");
				STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
				Geometry geometry = GeoTranslator.struct2Jts(struct, 100000, 0);
				String targets="[IX_POI,"+pidTmp1+"];[IX_POI,"+pidTmp2+"]";
				if(!filterPid.contains(targets)){
					setCheckResult(geometry, targets, rs.getInt("MESH_ID"));
				}
				filterPid.add(targets);
				filterPid.add("[IX_POI,"+pidTmp2+"];[IX_POI,"+pidTmp1+"]");
			}
		}catch (SQLException e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(pstmt);
		}
		}
		//100米内名称（name）和分类、品牌相同的新增设施和其他非删除设施,当分类={230210,230213,230214}时，需要增加停车场建筑物类型一起判断
		if(pid2!=null&&pid2.size()>0){
			String pids=pid2.toString().replace("[", "").replace("]", "");
			Connection conn = this.getCheckRuleCommand().getConn();
			List<Clob> values=new ArrayList<Clob>();
			String pidString="";
			if(pid2.size()>1000){
				Clob clob=ConnectionUtil.createClob(conn);
				clob.setString(1, pids);
				pidString=" PID IN (select to_number(column_value) from table(clob_to_table(?)))";
				values.add(clob);
				values.add(clob);
			}else{
				pidString=" PID IN ("+pids+")";
			}
			String sqlStr="WITH T AS"
					+ " SELECT /*+ORDERD*/ P2.PID PID2, P1.PID PID1,P1.GEOMETRY,P1.MESH_ID"
					+ "    FROM IX_POI         P1,"
					+ "         IX_POI         P2,"
					+ "         IX_POI_PARKING PK1,"
					+ "         IX_POI_PARKING PK2,"
					+ "         IX_POI_NAME    N1,"
					+ "         IX_POI_NAME    N2"
					+ "   WHERE P1.KIND_CODE = P2.KIND_CODE"
					+ "     AND NVL(P1.CHAIN, 0) = NVL(P2.CHAIN, 0)"
					+ "     AND P1.PID = N1.POI_PID"
					+ "     AND P1.PID = PK1.POI_PID"
					+ "     AND N1.NAME_CLASS = 1"
					+ "     AND N1.NAME_TYPE = 2"
					+ "     AND N1.LANG_CODE IN ('CHI', 'CHT')"
					+ "     AND P2.PID = N2.POI_PID"
					+ "     AND P2.PID = PK2.POI_PID"
					+ "     AND NVL(PK1.parking_typE,'null') = NVL(PK2.parking_typE,'null')"
					+ "     AND N2.NAME_CLASS = 1"
					+ "     AND N2.NAME_TYPE = 2"
					+ "     AND N2.LANG_CODE IN ('CHI', 'CHT')"
					+ "     AND N1.NAME = N2.NAME"
					//+ "     AND NVL(P1.LABEL,'')=NVL(P2.LABEL,'')"
					+ "     AND P2.U_RECORD!=2"
					+ "     AND P1.U_RECORD!=2"
					+ "     AND PK2.U_RECORD!=2"
					+ "     AND PK1.U_RECORD!=2"
					+ "     AND N1.U_RECORD!=2"
					+ "     AND N2.U_RECORD!=2"
					+ "     AND P1."+pidString
					+"      AND SDO_NN(P2.GEOMETRY, P1.GEOMETRY, 'DISTANCE=100 UNIT=METER') = 'TRUE' "
					+ "     AND P1.PID != P2.PID";
			log.info("FM11Win0117:"+sqlStr);
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
			//过滤相同pid
			Set<String> filterPid = new HashSet<String>();
			while (rs.next()) {
				Long pidTmp1=rs.getLong("PID1");
				Long pidTmp2=rs.getLong("PID2");
				STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
				Geometry geometry = GeoTranslator.struct2Jts(struct, 100000, 0);
				String targets="[IX_POI,"+pidTmp1+"];[IX_POI,"+pidTmp2+"]";
				if(!filterPid.contains(targets)){
					setCheckResult(geometry, targets, rs.getInt("MESH_ID"));
				}
				filterPid.add(targets);
				filterPid.add("[IX_POI,"+pidTmp2+"];[IX_POI,"+pidTmp1+"]");
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
