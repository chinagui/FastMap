package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.vividsolutions.jts.geom.Geometry;

import oracle.sql.STRUCT;

/**
 * @ClassName FM14Sum0101
 * @author Han Shaoming
 * @date 2017年2月23日 下午7:46:31
 * @Description TODO
 * 检查条件：删除POI对象
 * 检查原则：
 * POI对象在在IX_POI_ICON表或CMG_BUILDING_POI表icon表中存在的设施，报log：删除POI对象是ICON标识，请确认
 */
public class FM14Sum0101 extends BasicCheckRule {

	public void run() throws Exception {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			Map<Long, BasicObj> rows=getRowList();
			List<Long> pid=new ArrayList<Long>();
			for(Long key:rows.keySet()){
				BasicObj obj=rows.get(key);
				IxPoiObj poiObj=(IxPoiObj) obj;
				IxPoi poi =(IxPoi) poiObj.getMainrow();
				//已删除的数据检查
				if(poi.getOpType().equals(OperationType.PRE_DELETED)){
					pid.add(poi.getPid());
				}
			}
			//判断条件是否满足
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
				}else{
					pidString=" PID IN ("+pids+")";
				}

				String sqlStr="WITH T AS" 
				+" (SELECT P.PID,P.GEOMETRY,P.MESH_ID FROM IX_POI P"
				+" WHERE P."+pidString+" AND P.U_RECORD =2)"
				+" SELECT /*+ NO_MERGE(T)*/ T.PID,T.GEOMETRY,T.MESH_ID FROM T,IX_POI_ICON C"
				+" WHERE C.POI_PID = T.PID AND C.U_RECORD <>2"
				+" UNION ALL"
				+" SELECT /*+ NO_MERGE(T)*/ T.PID,T.GEOMETRY,T.MESH_ID FROM CMG_BUILDING_POI B,T"
				+" WHERE B.POI_PID = T.PID AND B.U_RECORD <>2";
				pstmt = conn.prepareStatement(sqlStr);;
				if(values!=null&&values.size()>0){
					for(int i=0;i<values.size();i++){
						pstmt.setClob(i+1,values.get(i));
					}
				}			
				rs = pstmt.executeQuery();
				while (rs.next()) {
					Long pidTmp1=rs.getLong("PID");
					STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
					Geometry geometry = GeoTranslator.struct2Jts(struct, 100000, 0);
					String targets="[IX_POI,"+pidTmp1+"]";
					setCheckResult(geometry, targets, rs.getInt("MESH_ID"));
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
