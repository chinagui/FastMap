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
			Connection conn = this.getCheckRuleCommand().getConn();
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
				STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
				Geometry geometry = GeoTranslator.struct2Jts(struct, 100000, 0);
				String targets="[IX_POI,"+pidTmp1+"];[IX_POI,"+pidTmp2+"]";
				//查询父子关系
				Set<Long> pidList=new HashSet<Long>();
				pidList.add(pidTmp1);
				pidList.add(pidTmp2);
				Map<Long, Long> parentMap = IxPoiSelector.getParentPidsByChildrenPids(this.getCheckRuleCommand().getConn(), pidList);
				boolean flag = false;
				//无父子关系
				if(!parentMap.containsKey(pidTmp1)&&!parentMap.containsKey(pidTmp1)){
					flag = true;
				}
				//有相同的父
				if(parentMap.containsKey(pidTmp1)&&parentMap.containsKey(pidTmp1)){
					Long pidP1 = parentMap.get(pidTmp1);
					Long pidP2 = parentMap.get(pidTmp2);
					if(pidP1 ==pidP2){
						flag = true;
					}
				}
				if(flag){
					setCheckResult(geometry, targets, rs.getInt("MESH_ID"));
				}
			}
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
