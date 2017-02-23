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

import oracle.sql.STRUCT;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
import com.vividsolutions.jts.geom.Geometry;

/**
 * 检查条件：Lifecycle为“1（删除）”不检查； 检查原则：
 * 与加油站、加气站同点的便利店设施（分类：便利零售、其他零售、旧货市场、丧葬用品零售、宗教用品零售）
 * （130105\130800\130804\130806\130807）没有与加油站（分类：230215\230216）建立父子关系。
 *
 */
public class FM14Sum1115 extends BasicCheckRule {
	
	public void run() throws Exception {
		Map<Long, BasicObj> rows=getRowList();
		List<Long> pidParent=new ArrayList<Long>();
		List<Long> pidChildren=new ArrayList<Long>();
		for(Long key:rows.keySet()){
			BasicObj obj=rows.get(key);
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi =(IxPoi) poiObj.getMainrow();
			//已删除的数据不检查
			if(poi.getOpType().equals(OperationType.PRE_DELETED)){continue;}
			String kind=poi.getKindCode();
			if(kind.equals("130105")||kind.equals("130800")||kind.equals("130804")||kind.equals("130806")
					||kind.equals("130807"))
				{pidChildren.add(poi.getPid());}
			if(kind.equals("230215")||kind.equals("230216"))
				{pidParent.add(poi.getPid());}
		}
		if(pidChildren!=null&&pidChildren.size()>0){
			String pids=pidChildren.toString().replace("[", "").replace("]", "");
			Connection conn = this.getCheckRuleCommand().getConn();
			List<Clob> values=new ArrayList<Clob>();
			String pidString="";
			if(pidChildren.size()>1000){
				Clob clob=ConnectionUtil.createClob(conn);
				clob.setString(1, pids);
				pidString=" PID IN (select to_number(column_value) from table(clob_to_table(?)))";
				values.add(clob);
				values.add(clob);
			}else{
				pidString=" PID IN ("+pids+")";
			}
			String sqlStr="WITH T AS"
					+ " (SELECT P1.PID PID2, P1.GEOMETRY G2, P1.KIND_CODE"
					+ "    FROM IX_POI P1"
					+ "   WHERE P1.U_RECORD != 2"
					+ "     AND P1."+pidString+")"
					+ " SELECT /*+ NO_MERGE(T)*/"
					+ " P.PID, T.PID2"
					+ "  FROM T, IX_POI P"
					+ " WHERE SDO_GEOM.SDO_DISTANCE(P.GEOMETRY, T.G2, 0.00000005) < 3"
					+ "   AND P.KIND_CODE IN ('230215', '230216')"
					+ "   AND P.U_RECORD != 2"
					+ " MINUS"
					+ " SELECT /*+ NO_MERGE(T)*/P.PARENT_POI_PID, C.CHILD_POI_PID"
					+ "  FROM IX_POI_CHILDREN C, IX_POI_PARENT P,T"
					+ " WHERE P.GROUP_ID = C.GROUP_ID"
					+ " AND C.CHILD_POI_PID =T.PID2";
			PreparedStatement pstmt=conn.prepareStatement(sqlStr);;
			if(values!=null&&values.size()>0){
				for(int i=0;i<values.size();i++){
					pstmt.setClob(i+1,values.get(i));
				}
			}			
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				Long pidTmp1=rs.getLong("PID");
				Long pidTmp2=rs.getLong("PID2");
				//STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
				//Geometry geometry = GeoTranslator.struct2Jts(struct, 100000, 0);
				String targets="[IX_POI,"+pidTmp1+"];[IX_POI,"+pidTmp2+"]";
				setCheckResult("", targets, 0);
			}
		}
		if(pidParent!=null&&pidParent.size()>0){
			String pids=pidParent.toString().replace("[", "").replace("]", "");
			Connection conn = this.getCheckRuleCommand().getConn();
			List<Clob> values=new ArrayList<Clob>();
			String pidString="";
			if(pidParent.size()>1000){
				Clob clob=ConnectionUtil.createClob(conn);
				clob.setString(1, pids);
				pidString=" PID IN (select to_number(column_value) from table(clob_to_table(?)))";
				values.add(clob);
				values.add(clob);
			}else{
				pidString=" PID IN ("+pids+")";
			}
			String sqlStr="WITH T AS"
					+ " (SELECT P1.PID PID2, P1.GEOMETRY G2, P1.KIND_CODE"
					+ "    FROM IX_POI P1"
					+ "   WHERE P1.U_RECORD != 2"
					+ "     AND P1."+pidString+")"
					+ " SELECT /*+ NO_MERGE(T)*/"
					+ "  T.PID2,P.PID"
					+ "  FROM T, IX_POI P"
					+ " WHERE SDO_GEOM.SDO_DISTANCE(P.GEOMETRY, T.G2, 0.00000005) < 3"
					+ "   AND P.KIND_CODE IN ('130105', '130800', '130804', '130806', '130807')"
					+ "   AND P.U_RECORD != 2"
					+ " MINUS"
					+ " SELECT /*+ NO_MERGE(T)*/P.PARENT_POI_PID, C.CHILD_POI_PID"
					+ "  FROM IX_POI_CHILDREN C, IX_POI_PARENT P,T"
					+ " WHERE P.GROUP_ID = C.GROUP_ID"
					+ " AND P.PARENT_POI_PID =T.PID2";
			PreparedStatement pstmt=conn.prepareStatement(sqlStr);;
			if(values!=null&&values.size()>0){
				for(int i=0;i<values.size();i++){
					pstmt.setClob(i+1,values.get(i));
				}
			}			
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				Long pidTmp1=rs.getLong("PID");
				Long pidTmp2=rs.getLong("PID2");
				//STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
				//Geometry geometry = GeoTranslator.struct2Jts(struct, 100000, 0);
				String targets="[IX_POI,"+pidTmp2+"];[IX_POI,"+pidTmp1+"]";
				setCheckResult("", targets, 0);
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
