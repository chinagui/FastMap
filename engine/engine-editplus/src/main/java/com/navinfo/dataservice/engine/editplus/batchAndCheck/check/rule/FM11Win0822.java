package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;

/**
 * FM-11Win-08-22
 * 检查条件： Lifecycle！=1（删除） 检查原则： 加油站（分类为230215）、加气站（230216）
 * 同点的设施需要与加油站建立父子关系，否则报Log:与加油站同点的POI未与加油站建立父子关系
 * 
 * @author wangdongbin
 *
 */
public class FM11Win0822 extends BasicCheckRule {

	public void run() throws Exception {
		Map<Long, BasicObj> rows = getRowList();
		List<Long> pidParent = new ArrayList<Long>();
		List<Long> pidChildren = new ArrayList<Long>();
		for (Long key : rows.keySet()) {
			BasicObj obj = rows.get(key);
			IxPoiObj poiObj = (IxPoiObj) obj;
			IxPoi poi = (IxPoi) poiObj.getMainrow();
			// 已删除的数据不检查
			if (poi.getOpType().equals(OperationType.PRE_DELETED)) {
				continue;
			}
			String kind = poi.getKindCode();

			if (kind.equals("230215") || kind.equals("230216")) {
				pidParent.add(poi.getPid());
			} else {
				pidChildren.add(poi.getPid());
			}
		}
		if (pidChildren != null && pidChildren.size() > 0) {
			String pids = pidChildren.toString().replace("[", "").replace("]", "");
			Connection conn = this.getCheckRuleCommand().getConn();
			List<Clob> values = new ArrayList<Clob>();
			String pidString = "";
			if (pidChildren.size() > 1000) {
				Clob clob = ConnectionUtil.createClob(conn);
				clob.setString(1, pids);
				pidString = " PID IN (select to_number(column_value) from table(clob_to_table(?)))";
				values.add(clob);
				values.add(clob);
			} else {
				pidString = " PID IN (" + pids + ")";
			}
			String sqlStr = "SELECT /*+ PARALLEL(P)*/"
					+ "					 P.PID, P1.PID"
					+ "					  FROM IX_POI P1, IX_POI P"
					+ "					 WHERE SDO_GEOM.SDO_DISTANCE(P.GEOMETRY, P1.GEOMETRY, 0.00000005) < 3"
					+ "					   AND P.KIND_CODE IN ('230215', '230216')"
					+ "					   AND P1. "+pidString
					+ "					   AND P.U_RECORD != 2"
					+ "					MINUS"
					+ "					SELECT P.PARENT_POI_PID, C.CHILD_POI_PID"
					+ "					  FROM IX_POI_CHILDREN C, IX_POI_PARENT P"
					+ "					 WHERE P.GROUP_ID = C.GROUP_ID"
					+ "					   AND C.U_RECORD != 2"
					+ "					   AND P.U_RECORD != 2"
					+ "					MINUS"
					+ "					SELECT C.CHILD_POI_PID, P.PARENT_POI_PID"
					+ "					  FROM IX_POI_CHILDREN C, IX_POI_PARENT P"
					+ "					 WHERE P.GROUP_ID = C.GROUP_ID"
					+ "					   AND C.U_RECORD != 2"
					+ "					   AND P.U_RECORD != 2";
			log.info("FM-11Win-08-22 sql1:"+sqlStr);
			PreparedStatement pstmt = conn.prepareStatement(sqlStr);
			;
			if (values != null && values.size() > 0) {
				for (int i = 0; i < values.size(); i++) {
					pstmt.setClob(i + 1, values.get(i));
				}
			}
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				Long pidTmp1 = rs.getLong("PID");
				Long pidTmp2 = rs.getLong("PID2");
				String targets = "[IX_POI," + pidTmp1 + "];[IX_POI," + pidTmp2 + "]";
				setCheckResult("", targets, 0);
			}
		}
		if (pidParent != null && pidParent.size() > 0) {
			String pids = pidParent.toString().replace("[", "").replace("]", "");
			Connection conn = this.getCheckRuleCommand().getConn();
			List<Clob> values = new ArrayList<Clob>();
			String pidString = "";
			if (pidParent.size() > 1000) {
				Clob clob = ConnectionUtil.createClob(conn);
				clob.setString(1, pids);
				pidString = " PID IN (select to_number(column_value) from table(clob_to_table(?)))";
				values.add(clob);
				values.add(clob);
			} else {
				pidString = " PID IN (" + pids + ")";
			}
			//同一点获取速度较慢，
			String sqlStr = "SELECT /*+PARALLEL(P)*/"
					+ " P.PID, P1.PID"
					+ "  FROM IX_POI P1, IX_POI P"
					+ " WHERE SDO_GEOM.SDO_DISTANCE(P.GEOMETRY, P1.GEOMETRY, 0.00000005) < 3"
					+ "   AND P1.PID != P.PID"
					+ "   AND P1."+pidString
					+ "   AND P.U_RECORD != 2"
					+ " MINUS"
					+ " SELECT P.PARENT_POI_PID, C.CHILD_POI_PID"
					+ "  FROM IX_POI_CHILDREN C, IX_POI_PARENT P"
					+ " WHERE P.GROUP_ID = C.GROUP_ID"
					+ "   AND C.U_RECORD != 2"
					+ "   AND P.U_RECORD != 2"
					+ " MINUS"
					+ " SELECT C.CHILD_POI_PID，P.PARENT_POI_PID"
					+ "  FROM IX_POI_CHILDREN C, IX_POI_PARENT P"
					+ " WHERE P.GROUP_ID = C.GROUP_ID"
					+ "   AND C.U_RECORD != 2"
					+ "   AND P.U_RECORD != 2";
			log.info("FM-11Win-08-22 sql2:"+sqlStr);
			PreparedStatement pstmt = conn.prepareStatement(sqlStr);
			;
			if (values != null && values.size() > 0) {
				for (int i = 0; i < values.size(); i++) {
					pstmt.setClob(i + 1, values.get(i));
				}
			}
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				Long pidTmp1 = rs.getLong("PID");
				Long pidTmp2 = rs.getLong("PID2");
				String targets = "[IX_POI," + pidTmp2 + "];[IX_POI," + pidTmp1 + "]";
				setCheckResult("", targets, 0);
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
