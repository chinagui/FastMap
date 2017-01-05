package com.navinfo.dataservice.engine.check.rules;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInter;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInterLink;
import com.navinfo.dataservice.dao.glm.model.rd.road.RdRoad;
import com.navinfo.dataservice.dao.glm.model.rd.road.RdRoadLink;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: GLM28016
 * @author songdongyan
 * @date 2017年1月5日
 * @Description: CRFRoad的两端必须是CRFIntersection，且每端node必须组成同一个CRFI（图廓点端除外）
 * 新增CRFR前检查:RdRoad
 */
public class GLM28016 extends baseRule{

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#preCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for(IRow obj : checkCommand.getGlmList()){
			//RdInter新增触发
			if (obj instanceof RdRoad){
				RdRoad rdRoad = (RdRoad)obj;
				checkRdRoad(rdRoad);
			}
		}
	}

	/**
	 * @param rdRoad
	 * @throws SQLException 
	 */
	private void checkRdRoad(RdRoad rdRoad) throws SQLException {
		if(rdRoad.status().equals(ObjStatus.INSERT)){
			Set<Integer> linkPidSet = new HashSet<Integer>();
			for(IRow iRow:rdRoad.getLinks()){
				RdRoadLink rdRoadLink = (RdRoadLink)iRow;
				linkPidSet.add(rdRoadLink.getLinkPid());
			}
			if(!linkPidSet.isEmpty()){
				StringBuilder sb = new StringBuilder();

				sb.append("SELECT RIN.PID, RIN.NODE_PID,1 AS TYPE FROM RD_INTER_NODE RIN");
				sb.append(" WHERE RIN.NODE_PID IN (");
				sb.append(" SELECT R.S_NODE_PID FROM RD_LINK R WHERE R.U_RECORD <> 2 AND R.LINK_PID IN (" + StringUtils.join(linkPidSet.toArray(),",") + ")");
				sb.append(" UNION");
				sb.append(" SELECT R.E_NODE_PID FROM RD_LINK R WHERE R.U_RECORD <> 2 AND R.LINK_PID IN (" + StringUtils.join(linkPidSet.toArray(),",") + ")");
				sb.append(" )");
				sb.append(" AND RIN.U_RECORD <> 2");
				sb.append(" UNION");
				sb.append(" SELECT 0 AS PID, RN.NODE_PID,2 AS TYPE FROM RD_NODE RN,RD_NODE_FORM F");
				sb.append(" WHERE RN.NODE_PID IN (");
				sb.append(" SELECT R.S_NODE_PID FROM RD_LINK R WHERE R.U_RECORD <> 2 AND R.LINK_PID IN (" + StringUtils.join(linkPidSet.toArray(),",") + ")");
				sb.append(" UNION");
				sb.append(" SELECT R.E_NODE_PID FROM RD_LINK R WHERE R.U_RECORD <> 2 AND R.LINK_PID IN (" + StringUtils.join(linkPidSet.toArray(),",") + ")");
				sb.append(" )");
				sb.append(" AND RN.U_RECORD <> 2");
				sb.append(" AND RN.NODE_PID = F.NODE_PID");
				sb.append(" AND F.FORM_OF_WAY = 2");
				
				String sql = sb.toString();
				log.info("RdInter前检查GLM28016:" + sql);
				
				PreparedStatement pstmt = this.getConn().prepareStatement(sql);	
				
				ResultSet resultSet = pstmt.executeQuery();
				Map<Integer,Integer> rdInterNodeMap=new HashMap<Integer,Integer>();
				Set<Integer> NodeMap=new HashSet<Integer>();

				while (resultSet.next()){
					if(resultSet.getInt("TYPE")==1){
						rdInterNodeMap.put(resultSet.getInt("NODE_PID"), resultSet.getInt("PID"));
					}else if(resultSet.getInt("TYPE")==2){
						NodeMap.add(resultSet.getInt("NODE_PID"));
					}
				} 
				resultSet.close();
				pstmt.close();
				

				Set<Integer> pidSet = new HashSet<Integer>();
				for(Map.Entry<Integer,Integer> entry:rdInterNodeMap.entrySet()){
					pidSet.add(entry.getValue());
					if(NodeMap.contains(entry.getKey())){
						NodeMap.remove(entry.getKey());
					}
				}
				if(pidSet.size()>2){
					this.setCheckResult("", "", 0);
					return;
				}else if(pidSet.size() + NodeMap.size()<2){
					this.setCheckResult("", "", 0);
					return;
				}
			}
		}
		
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#postCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
