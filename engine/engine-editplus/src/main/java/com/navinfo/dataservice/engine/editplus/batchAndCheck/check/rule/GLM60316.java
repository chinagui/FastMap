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

import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;

/**
 * @ClassName GLM60316
 * @author Han Shaoming
 * @date 2017年2月21日 下午8:03:41
 * @Description TODO
 * 检查条件： 非删除POI且存在同一关系
 * 检查原则：
 * IX_SAMEPOI.RELATION_TYPE为1的一组POI，且都存在父，则他们对应的父必须存在同一关系，
 * 否则报LOG：制作同一关系的一组POI，它们的父也应该制作同一关系！
 * 注：这组POI对应是同一父，不报LOG
 */
public class GLM60316 extends BasicCheckRule {

	
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
		//IX_SAMEPOI.RELATION_TYPE为1的一组POI，且都存在父，则他们对应的父必须存在同一关系
		if(pid!=null&&pid.size()>0){
			String pids=pid.toString().replace("[", "").replace("]", "");
			Connection conn = null;
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			try{
				conn = this.getCheckRuleCommand().getConn();
				List<Clob> values=new ArrayList<Clob>();
				String pidString="";
				if(pid.size()>1000){
					Clob clob=ConnectionUtil.createClob(conn);
					clob.setString(1, pids);
					pidString="PID IN (select to_number(column_value) from table(clob_to_table(?)))";
					values.add(clob);
				}else{
					pidString="PID IN ("+pids+")";
				}
				String sqlStr=" WITH T AS ("
						+" 	SELECT P1.POI_PID PID1,PP1.PARENT_POI_PID P_PID1,P2.POI_PID PID2,PP2.PARENT_POI_PID P_PID2"
						+" 	FROM"
						+" 		IX_SAMEPOI S,"
						+" 		IX_SAMEPOI_PART P1,"
						+" 		IX_SAMEPOI_PART P2,"
						+" 		IX_POI_PARENT PP1,"
						+" 		IX_POI_PARENT PP2,"
						+" 		IX_POI_CHILDREN C1,"
						+" 		IX_POI_CHILDREN C2"
						+" 	WHERE"
						+" 		S.GROUP_ID = P1.GROUP_ID"
						+" 	AND P1.GROUP_ID = P2.GROUP_ID"
						+" 	AND S.RELATION_TYPE = 1"
						+" 	AND P1.POI_PID = C1.CHILD_POI_PID"
						+" 	AND C1.GROUP_ID = PP1.GROUP_ID"
						+" 	AND P2.POI_PID = C2.CHILD_POI_PID"
						+" 	AND C2.GROUP_ID = PP2.GROUP_ID"
						+" 	AND PP1.PARENT_POI_PID <> PP2.PARENT_POI_PID"
						+" 	AND P1.POI_PID <> P2.POI_PID"
						+" 	AND S.U_RECORD <> 2"
						+" 	AND P1.U_RECORD <> 2"
						+" 	AND P2.U_RECORD <> 2"
						+" 	AND PP1.U_RECORD <> 2"
						+" 	AND PP2.U_RECORD <> 2"
						+" 	AND C1.U_RECORD <> 2"
						+" 	AND C2.U_RECORD <> 2"
						+" 	AND P1.POI_"+pidString+") "
						+" SELECT	/*+ NO_MERGE(T)*/	T.PID1 ,T.PID2"
						+" FROM T"
						+" WHERE"
						+" 	NOT EXISTS (SELECT 1"
						+" 	FROM"
						+" 			IX_SAMEPOI_PART P1,"
						+" 			IX_SAMEPOI_PART P2"
						+" 		WHERE"
						+" 			P1.POI_PID = T.P_PID1"
						+" 		AND P2.POI_PID = T.P_PID2"
						+" 		AND P1.GROUP_ID = P2.GROUP_ID"
						+" 		AND P1.U_RECORD <> 2"
						+" 		AND P2.U_RECORD <> 2)";
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
					Long pidTmp1=rs.getLong("PID1");
					Long pidTmp2=rs.getLong("PID2");
					BasicObj obj=rows.get(pidTmp1);
					IxPoiObj poiObj=(IxPoiObj) obj;
					IxPoi poi =(IxPoi) poiObj.getMainrow();
					String targets="[IX_POI,"+pidTmp1+"];[IX_POI,"+pidTmp2+"]";
					if(!filterPid.contains(targets)){
						setCheckResult(poi.getGeometry(), targets,poi.getMeshId(), null);
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
	}
	
	@Override
	public void runCheck(BasicObj obj) throws Exception {
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
	}

}
