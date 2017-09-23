package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
 * FM-14Sum-08-02-01
 * 检查条件：Lifecycle为“1（删除）”不检查；
 * 检查原则：
 * 同点的设施，名称（name）、地址（address）、分类和品牌相同
 * 当分类={230210,230213,230214}时，需要增加停车场建筑物类型一起判断
 * 备注：增加品牌一起判断，品牌数组的第一个code相同
 * 充电桩kindcode=230227不查
 * @author zhangxiaoyi
 */
public class FM14Sum080201 extends BasicCheckRule {
	
	public void run() throws Exception {
		Map<Long, BasicObj> rows = getRowList();
		List<Long> pid1 = new ArrayList<Long>();
		List<Long> pid2 = new ArrayList<Long>();
		List<Long> pid3 = new ArrayList<Long>();
		
		for (Long key:rows.keySet()) {
			BasicObj obj = rows.get(key);
			IxPoiObj poiObj = (IxPoiObj) obj;
			IxPoi poi = (IxPoi) poiObj.getMainrow();
			//已删除的数据不检查
			if (poi.getOpType().equals(OperationType.PRE_DELETED)) {
				continue;
			}
			String kind=poi.getKindCode();
			if (kind.equals("230227")) {
				continue;
			}
			if (kind.equals("230210") || kind.equals("230213") || kind.equals("230214")) {
				pid2.add(poi.getPid());
			} else if (kind.equals("230218")) {
				pid3.add(poi.getPid());
			} else {
				pid1.add(poi.getPid());
			}
		}
		//同点的设施，名称（name）、地址（address）、分类和品牌相同
		if(pid1 != null && pid1.size()>0){
			Connection conn = null;
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			try{
				String pids = pid1.toString().replace("[", "").replace("]", "");
				conn = this.getCheckRuleCommand().getConn();
				List<Clob> values = new ArrayList<Clob>();
				String pidString="";
				if(pid1.size()>1000){
					Clob clob=ConnectionUtil.createClob(conn);
					clob.setString(1, pids);
					pidString=" PID IN (select to_number(column_value) from table(clob_to_table(?)))";
					values.add(clob);
				}else{
					pidString=" PID IN ("+pids+")";
				}
				String sqlStr="WITH T AS"
						+ " (SELECT P1.PID PID1, P1.GEOMETRY G1,P1.MESH_ID M1,P2.PID PID2,P2.GEOMETRY G2"
						+ "    FROM IX_POI         P1,"
						+ "         IX_POI         P2,"
						+ "         IX_POI_NAME    N1,"
						+ "         IX_POI_NAME    N2,"
						+ "         IX_POI_ADDRESS A1,"
						+ "         IX_POI_ADDRESS A2"
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
						+ "     AND P1.PID = A1.POI_PID"
						+ "     AND A1.LANG_CODE IN ('CHI', 'CHT')"
						+ "     AND P2.PID = A2.POI_PID"
						+ "     AND A2.LANG_CODE IN ('CHI', 'CHT')"
						+ "     AND A1.FULLNAME = A2.FULLNAME"
						+ "     AND N1.NAME = N2.NAME"
						+ "     AND P1."+pidString
						+ "     AND P1.PID != P2.PID"
						+ "		AND P1.U_RECORD <> 2 AND P2.U_RECORD <> 2"
						+ "		AND N1.U_RECORD <> 2 AND N2.U_RECORD <> 2"
						+ "		AND A1.U_RECORD <> 2 AND A2.U_RECORD <> 2)"
						+ " SELECT /*+ NO_MERGE(T)*/"
						+ " T.PID1 PID,T.G1 GEOMETRY,T.M1 MESH_ID,PID2"
						+ "  FROM T"
						+ " WHERE SDO_GEOM.SDO_DISTANCE(G1, G2, 0.00000005) < 3"
						+ "   AND T.PID1 != T.PID2 ";
				pstmt = conn.prepareStatement(sqlStr);;
				if(values!=null&&values.size()>0){
					for(int i=0;i<values.size();i++){
						pstmt.setClob(i+1,values.get(i));
					}
				}			
				rs = pstmt.executeQuery();
				//过滤相同pid
				Set<String> filterPid = new HashSet<String>();
				while (rs.next()) {
					Long pidTmp1=rs.getLong("PID");
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
			}catch(Exception e){
				log.error(e.getMessage(),e);
				throw e;
			}finally {
				DbUtils.closeQuietly(rs);
				DbUtils.closeQuietly(pstmt);
			}
			
		}
		
		
		//同点的设施，名称（name）、地址（address）、分类和品牌相同,当分类={230210,230213,230214}时，需要增加停车场建筑物类型一起判断
		if(pid2 != null && pid2.size()>0){
			Connection conn = null;
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			try{
				String pids=pid2.toString().replace("[", "").replace("]", "");
				conn = this.getCheckRuleCommand().getConn();
				List<Clob> values=new ArrayList<Clob>();
				String pidString="";
				if(pid2.size()>1000){
					Clob clob=ConnectionUtil.createClob(conn);
					clob.setString(1, pids);
					pidString=" PID IN (select to_number(column_value) from table(clob_to_table(?)))";
					values.add(clob);
				}else{
					pidString=" PID IN ("+pids+")";
				}
				String sqlStr="WITH T AS"
						+ " (SELECT P1.PID PID1, P1.GEOMETRY G1,P1.MESH_ID M1,P2.PID PID2,P2.GEOMETRY G2"
						+ "    FROM IX_POI         P1,"
						+ "         IX_POI         P2,"
						+ "         IX_POI_NAME    N1,"
						+ "         IX_POI_NAME    N2,"
						+ "         IX_POI_ADDRESS A1,"
						+ "         IX_POI_ADDRESS A2,"
						+ "			IX_POI_PARKING K1,"
						+ "			IX_POI_PARKING K2"
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
						+ "     AND P1.PID = A1.POI_PID"
						+ "     AND A1.LANG_CODE IN ('CHI', 'CHT')"
						+ "     AND P2.PID = A2.POI_PID"
						+ "     AND A2.LANG_CODE IN ('CHI', 'CHT')"
						+ "     AND A1.FULLNAME = A2.FULLNAME"
						+ "     AND N1.NAME = N2.NAME"
						+ "     AND P1.PID = K1.POI_PID"
						+ "     AND P2.PID = K2.POI_PID"
						+ "     AND NVL (K1.PARKING_TYPE, '') = NVL (K2.PARKING_TYPE, '')"
						+ "     AND P1."+pidString
						+ "     AND P1.PID != P2.PID"
						+ "		AND P1.U_RECORD <> 2 AND P2.U_RECORD <> 2"
						+ "		AND N1.U_RECORD <> 2 AND N2.U_RECORD <> 2"
						+ "		AND A1.U_RECORD <> 2 AND A2.U_RECORD <> 2"
						+ "		AND K1.U_RECORD <> 2 AND K2.U_RECORD <> 2)"
						+ " SELECT /*+ NO_MERGE(T)*/"
						+ " T.PID1 PID,T.G1 GEOMETRY,T.M1 MESH_ID,PID2"
						+ "  FROM T"
						+ " WHERE SDO_GEOM.SDO_DISTANCE(G1, G2, 0.00000005) < 3"
						+ "   AND T.PID1 != T.PID2 ";
				pstmt = conn.prepareStatement(sqlStr);;
				if(values!=null&&values.size()>0){
					for(int i=0;i<values.size();i++){
						pstmt.setClob(i+1,values.get(i));
					}
				}			
				rs = pstmt.executeQuery();
				//过滤相同pid
				Set<String> filterPid = new HashSet<String>();
				while (rs.next()) {
					Long pidTmp1=rs.getLong("PID");
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
			}catch(Exception e){
				log.error(e.getMessage(),e);
				throw e;
			}finally {
				DbUtils.closeQuietly(rs);
				DbUtils.closeQuietly(pstmt);
			}
		}
		
		
		//同点的设施，名称（name）、地址（address）、分类和品牌相同,当分类=(23018----充电站)，需要增加服务提供商SERVICE_PROV判断
		if(pid3 != null && pid3.size() > 0){
			Connection conn = null;
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			try{
				String pids = pid3.toString().replace("[", "").replace("]", "");
				conn = this.getCheckRuleCommand().getConn();
				List<Clob> values = new ArrayList<Clob>();
				String pidString = "";
				if(pid3.size() > 1000){
					Clob clob = ConnectionUtil.createClob(conn);
					clob.setString(1, pids);
					pidString = " PID IN (select to_number(column_value) from table(clob_to_table(?)))";
					values.add(clob);
				}else{
					pidString = " PID IN (" + pids + ")";
				}
				String sqlStr = "WITH T AS"
						+ " (SELECT P1.PID PID1, P1.GEOMETRY G1,P1.MESH_ID M1,P2.PID PID2,P2.GEOMETRY G2"
						+ "    FROM IX_POI         P1,"
						+ "         IX_POI         P2,"
						+ "         IX_POI_NAME    N1,"
						+ "         IX_POI_NAME    N2,"
						+ "         IX_POI_ADDRESS A1,"
						+ "         IX_POI_ADDRESS A2,"
						+ "			IX_POI_CHARGINGSTATION C1,"
						+ "			IX_POI_CHARGINGSTATION C2"
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
						+ "     AND P1.PID = A1.POI_PID"
						+ "     AND A1.LANG_CODE IN ('CHI', 'CHT')"
						+ "     AND P2.PID = A2.POI_PID"
						+ "     AND A2.LANG_CODE IN ('CHI', 'CHT')"
						+ "     AND A1.FULLNAME = A2.FULLNAME"
						+ "     AND N1.NAME = N2.NAME"
						+ "     AND P1.PID = C1.POI_PID"
						+ "     AND P2.PID = C2.POI_PID"
						+ "     AND NVL (C1.SERVICE_PROV, '') = NVL (C2.SERVICE_PROV, '')"
						+ "     AND P1." + pidString
						+ "     AND P1.PID != P2.PID"
						+ "		AND P1.U_RECORD <> 2 AND P2.U_RECORD <> 2"
						+ "		AND N1.U_RECORD <> 2 AND N2.U_RECORD <> 2"
						+ "		AND A1.U_RECORD <> 2 AND A2.U_RECORD <> 2"
						+ "		AND C1.U_RECORD <> 2 AND C2.U_RECORD <> 2)"
						+ " SELECT /*+ NO_MERGE(T)*/"
						+ " T.PID1 PID,T.G1 GEOMETRY,T.M1 MESH_ID,PID2"
						+ "  FROM T"
						+ " WHERE SDO_GEOM.SDO_DISTANCE(G1, G2, 0.00000005) < 3"
						+ "   AND T.PID1 != T.PID2 ";
				pstmt = conn.prepareStatement(sqlStr);;
				if(values != null && values.size() > 0){
					for(int i = 0; i < values.size(); i++){
						pstmt.setClob(i + 1, values.get(i));
					}
				}			
				rs = pstmt.executeQuery();
				//过滤相同pid
				Set<String> filterPid = new HashSet<String>();
				while (rs.next()) {
					Long pidTmp1 = rs.getLong("PID");
					Long pidTmp2 = rs.getLong("PID2");
					STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
					Geometry geometry = GeoTranslator.struct2Jts(struct, 100000, 0);
					String targets = "[IX_POI," + pidTmp1 + "];[IX_POI," + pidTmp2 + "]";
					if(!filterPid.contains(targets)){
						setCheckResult(geometry, targets, rs.getInt("MESH_ID"));
					}
					filterPid.add(targets);
					filterPid.add("[IX_POI," + pidTmp2 + "];[IX_POI," + pidTmp1 + "]");
				}
			}catch(Exception e){
				log.error(e.getMessage(),e);
				throw e;
			}finally {
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
