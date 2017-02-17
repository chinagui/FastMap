package com.navinfo.dataservice.engine.check.rules;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInter;
import com.navinfo.dataservice.dao.glm.model.rd.road.RdRoad;
import com.navinfo.dataservice.dao.glm.model.rd.road.RdRoadLink;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/**
 * @ClassName: GLM28017
 * @author songdongyan
 * @date 2017年1月4日
 * @Description: CRFRoad两端的CRFIntersection不应当超过2个
 */
public class GLM28017 extends baseRule {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.navinfo.dataservice.engine.check.core.baseRule#preCheck(com.navinfo.
	 * dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.navinfo.dataservice.engine.check.core.baseRule#postCheck(com.navinfo.
	 * dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		for (IRow obj : checkCommand.getGlmList()) {
			if (obj instanceof RdRoad) {
				RdRoad rdRoad = (RdRoad) obj;
				if (rdRoad.status().equals(ObjStatus.INSERT)) {
					check(rdRoad.getPid());
				}
			} else if (obj instanceof RdRoadLink) {
				RdRoadLink rdRoadLink = (RdRoadLink) obj;
				check(rdRoadLink.getPid());
			}
			//删除CRFI
			else if (obj instanceof RdInter) {
				RdInter rdInter = (RdInter) obj;
				//获取与该CRFI相关的RdRoadPidList
				List<Integer> RdRoadPidList = getRdRoadPidList(rdInter);
				for(Integer rdRoadPid:RdRoadPidList){
					check(rdRoadPid);
				}
				
			}
		}

	}

	/**
	 * @param rdInter
	 * @throws SQLException 
	 */
	private List<Integer> getRdRoadPidList(RdInter rdInter) throws SQLException {
		StringBuilder sb = new StringBuilder();

		sb.append("SELECT DISTINCT RRL.PID FROM RD_ROAD_LINK RRL,RD_LINK RL,RD_INTER_NODE RIN");
		sb.append(" WHERE RIN.PID = " + rdInter.getPid() );
		sb.append(" AND (RIN.NODE_PID = RL.S_NODE_PID OR RIN.NODE_PID = RL.E_NODE_PID) ");
		sb.append(" AND RRL.LINK_PID = RL.LINK_PID");
		sb.append(" AND RRL.U_RECORD <> 2 ");
		sb.append(" AND RL.U_RECORD <> 2");
		String sql = sb.toString();
		log.info("RdInter后检查GLM28017:" + sql);
		
		PreparedStatement pstmt = this.getConn().prepareStatement(sql);	
			
		ResultSet resultSet2 = pstmt.executeQuery();
		List<Integer> rdRoadPidList=new ArrayList<Integer>();

		while (resultSet2.next()){
			rdRoadPidList.add(resultSet2.getInt("PID"));
		} 
		resultSet2.close();
		pstmt.close();
		return rdRoadPidList;
	}

	/**
	 * @param pid
	 * @throws Exception
	 */
	private void check(int pid) throws Exception {
		StringBuilder sb = new StringBuilder();

		sb.append(" SELECT COUNT(1) FROM ");
		sb.append(" ( ");
		sb.append(" SELECT DISTINCT RIN.PID FROM RD_INTER_NODE RIN ");
		sb.append(" WHERE RIN.NODE_PID IN ");
		sb.append(" ( ");
		sb.append(" SELECT DISTINCT RL.S_NODE_PID FROM RD_ROAD_LINK RRL,RD_LINK RL ");
		sb.append(" WHERE RRL.LINK_PID = RL.LINK_PID ");
		sb.append(" AND RRL.U_RECORD <> 2 ");
		sb.append(" AND RL.U_RECORD <> 2 ");
		sb.append(" AND RRL.PID = " + pid );
		sb.append(" UNION ");
		sb.append(" SELECT DISTINCT RL.E_NODE_PID FROM RD_ROAD_LINK RRL,RD_LINK RL ");
		sb.append(" WHERE RRL.LINK_PID = RL.LINK_PID ");
		sb.append(" AND RRL.U_RECORD <> 2 ");
		sb.append(" AND RL.U_RECORD <> 2 ");
		sb.append(" AND RRL.PID = " + pid );
		sb.append(" ) ");
		sb.append(" AND RIN.U_RECORD <> 2");
		sb.append(" ) ");
		
//		sb.append("SELECT COUNT(1) FROM (");
//		sb.append("SELECT DISTINCT RIN.PID FROM RD_INTER_NODE RIN");
//		sb.append(" WHERE RIN.NODE_PID IN");
//		sb.append(" (");
//		sb.append(" SELECT R.S_NODE_PID");
//		sb.append(" FROM RD_LINK R");
//		sb.append(" WHERE R.DIRECT = 2");
//		sb.append(" AND R.U_RECORD <> 2");
//		sb.append(" AND R.LINK_PID IN (SELECT RRL.LINK_PID FROM RD_ROAD_LINK RRL WHERE RRL.U_RECORD <> 2 AND RRL.PID = "
//				+ pid + ")");
//		sb.append(" UNION ");
//		sb.append(" SELECT R.E_NODE_PID");
//		sb.append(" FROM RD_LINK R");
//		sb.append(" WHERE R.DIRECT = 2");
//		sb.append(" AND R.U_RECORD <> 2");
//		sb.append(" AND R.LINK_PID IN (SELECT RRL.LINK_PID FROM RD_ROAD_LINK RRL WHERE RRL.U_RECORD <> 2 AND RRL.PID = "
//				+ pid + ")");
//		sb.append(" UNION ");
//		sb.append(" SELECT R.S_NODE_PID");
//		sb.append(" FROM RD_LINK R");
//		sb.append(" WHERE R.DIRECT = 3");
//		sb.append(" AND R.U_RECORD <> 2");
//		sb.append(" AND R.LINK_PID IN (SELECT RRL.LINK_PID FROM RD_ROAD_LINK RRL WHERE RRL.U_RECORD <> 2 AND RRL.PID = "
//				+ pid + ")");
//		sb.append(" UNION ");
//		sb.append(" SELECT R.E_NODE_PID");
//		sb.append(" FROM RD_LINK R");
//		sb.append(" WHERE R.DIRECT = 3");
//		sb.append(" AND R.U_RECORD <> 2");
//		sb.append(" AND R.LINK_PID IN (SELECT RRL.LINK_PID FROM RD_ROAD_LINK RRL WHERE RRL.U_RECORD <> 2 AND RRL.PID = "
//				+ pid + ")");
//		sb.append(" )");
//		sb.append(" AND RIN.U_RECORD <> 2)");

		String sql = sb.toString();
		log.info("RdRoad后检查GLM28017:" + sql);

		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if (Integer.parseInt(resultList.get(0).toString())>2) {
			String target = "[RD_ROAD," + pid + "]";
			this.setCheckResult("", target, 0);
		}

	}
}
