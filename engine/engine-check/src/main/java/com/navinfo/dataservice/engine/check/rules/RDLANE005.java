package com.navinfo.dataservice.engine.check.rules;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneTopology;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneVia;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: RDLANE005
 * @author songdongyan
 * @date 2017年2月22日
 * @Description: RDLANE005 路口车信里不允许有经过线信息
 * 修改车信服务端后检查：RdLaneTopolyge,RdLaneVia
 */
public class RDLANE005  extends baseRule {

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#preCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for(IRow obj : checkCommand.getGlmList()){
			//修改车信
			if(obj instanceof RdLaneTopology){
				RdLaneTopology rdLaneTopology = (RdLaneTopology)obj;
				checkRdLaneTopology(rdLaneTopology,checkCommand);
			}
			//修改车信
			else if(obj instanceof RdLaneVia){
				RdLaneVia rdLaneVia = (RdLaneVia)obj;
				checkRdLaneVia(rdLaneVia,checkCommand);
			}
		}
		
	}

	/**
	 * @param rdLaneVia
	 * @param checkCommand 
	 * @throws Exception 
	 */
	private void checkRdLaneVia(RdLaneVia rdLaneVia, CheckCommand checkCommand) throws Exception {
		if(rdLaneVia.status().equals(ObjStatus.INSERT)){
			StringBuilder sb = new StringBuilder();

			sb.append("SELECT T.CONNEXITY_PID FROM RD_LANE_TOPOLOGY T ");
			sb.append(" WHERE T.TOPOLOGY_ID = " + rdLaneVia.getTopologyId());
			sb.append(" AND T.RELATIONSHIP_TYPE = 1");
			sb.append(" AND T.U_RECORD <> 2");

			String sql = sb.toString();
			log.info("RdLaneVia前检查RdLane002:" + sql);
			
			boolean flg;
			int connexityPid;
			Set<Integer> viaLinkSet;
			PreparedStatement pstmt = null;
			ResultSet resultSet = null;
			try {
				pstmt = this.getConn().prepareStatement(sql);	
				resultSet = pstmt.executeQuery();
				flg = false;
				connexityPid = 0;
				viaLinkSet = new HashSet<Integer>();
				if (resultSet.next()){
					connexityPid = resultSet.getInt("CONNEXITY_PID");
					flg = true;
					viaLinkSet.add(rdLaneVia.getLinkPid());
				} 
			}catch (SQLException e) {
				throw e;
			} finally {
				DbUtils.closeQuietly(resultSet);
				DbUtils.closeQuietly(pstmt);
			}
			
			for(IRow objInnerLoop : checkCommand.getGlmList()){
				if(objInnerLoop instanceof RdLaneTopology){
					RdLaneTopology rdLaneTopology = (RdLaneTopology)objInnerLoop;
					if(rdLaneTopology.getPid() == rdLaneVia.getTopologyId()){
						//路口车信修改为线线车信
						if(rdLaneTopology.status().equals(ObjStatus.UPDATE)){
							if(rdLaneTopology.changedFields().containsKey("relationshipType")){
								int relationshipType = Integer.parseInt(rdLaneTopology.changedFields().get("relationshipType").toString());
								if(relationshipType==2){
									flg = false;
									return;
								}
							}
						}
					}
				}
			}
			
			if(flg){
				int num = getViaNumNotInCross(connexityPid,viaLinkSet);
				if(num > 0){
					this.setCheckResult("", "", 0);
					return;
				}
			}
		}
		
	}

	/**
	 * @param rdLaneTopology
	 * @param checkCommand 
	 * @throws Exception 
	 */
	private void checkRdLaneTopology(RdLaneTopology rdLaneTopology, CheckCommand checkCommand) throws Exception {
		//新增联通关系
		if(rdLaneTopology.status().equals(ObjStatus.INSERT)){
			if(rdLaneTopology.getRelationshipType()==1){
				int viaNum = 0;
				Set<Integer> viaLinkSet = new HashSet<Integer>();
				for(IRow objInnerLoop : checkCommand.getGlmList()){
					if(objInnerLoop instanceof RdLaneVia){
						RdLaneVia rdLaneVia = (RdLaneVia)objInnerLoop;
						//排除删除的经过线
						if(rdLaneVia.status().equals(ObjStatus.DELETE)){
							continue;
						}
						if(rdLaneVia.getTopologyId()==rdLaneTopology.getPid()){
							viaNum ++;
							viaLinkSet.add(rdLaneVia.getLinkPid());
						}
					}
				}
				if(viaNum==0){
					return;
				}
				
				else{
					//获取经过线中不存在于同一路口内link数
					int num = getViaNumNotInCross(rdLaneTopology.getConnexityPid(),viaLinkSet);
					//经过线全部为同一路口内link，则认为没有经过线信息
					if(0 < num){
						this.setCheckResult("", "", 0);
						return;
					}
				}
			}
		}
		//修改联通关系
		else if(rdLaneTopology.status().equals(ObjStatus.UPDATE)){
			int relationshipType = 2;
			if(rdLaneTopology.changedFields().containsKey("relationshipType")){
				relationshipType = Integer.parseInt(rdLaneTopology.changedFields().get("relationshipType").toString());
			}
//			int viaNum = getViaNum(rdLaneTopology.getPid());
//			int viaNum = 0;
			Set<Integer> viaLinkSet = getViaLinkNotInCross(rdLaneTopology.getPid());
			if(relationshipType==1){
				for(IRow objInnerLoop : checkCommand.getGlmList()){
					if(objInnerLoop instanceof RdLaneVia){
						RdLaneVia rdLaneVia = (RdLaneVia)objInnerLoop;
						//删除的经过线
						if(rdLaneVia.status().equals(ObjStatus.DELETE)){
							if(rdLaneVia.getTopologyId()==rdLaneTopology.getPid()){
								if(rdLaneVia.getTopologyId()==rdLaneTopology.getPid()){
									viaLinkSet.remove(rdLaneVia.getLinkPid());
								}
							}
							continue;
						}
						//新增的经过线
						if(rdLaneVia.status().equals(ObjStatus.INSERT)){
							if(rdLaneVia.getTopologyId()==rdLaneTopology.getPid()){
								if(rdLaneVia.getTopologyId()==rdLaneTopology.getPid()){
									viaLinkSet.remove(rdLaneVia.getLinkPid());
								}
							}
							continue;
						}
					}
				}
				
				if(viaLinkSet.size()==0){
					return;
				}
				else{
					//获取经过线中不存在于同一路口内link数
					int num = getViaNumNotInCross(rdLaneTopology.getConnexityPid(),viaLinkSet);
					//经过线全部为同一路口内link，则认为没有经过线信息
					if(num>0){
						this.setCheckResult("", "", 0);
						return;
					}
				}
			}
		}
	}
	
	/**
	 * @param pid
	 * @return
	 * @throws SQLException 
	 */
	private Set<Integer> getViaLinkNotInCross(int pid) throws SQLException {
		Set<Integer> viaLinkSet = new HashSet<Integer>();
		StringBuilder sb = new StringBuilder();

		sb.append(" SELECT V.LINK_PID FROM RD_LANE_VIA V");
		sb.append(" WHERE V.U_RECORD <> 2");
		sb.append(" AND V.TOPOLOGY_ID = " + pid);
		sb.append(" MINUS");
		sb.append(" SELECT RCL.LINK_PID FROM RD_LANE_CONNEXITY RLC,RD_LANE_TOPOLOGY RLT,RD_CROSS_NODE RCN,RD_CROSS_LINK RCL");
		sb.append(" WHERE RLC.PID = RLT.CONNEXITY_PID");
		sb.append(" AND RLT.TOPOLOGY_ID = " + pid);
		sb.append(" AND RLC.NODE_PID = RCN.NODE_PID");
		sb.append(" AND RCN.PID = RCL.PID");
		sb.append(" AND RLC.U_RECORD <> 2");
		sb.append(" AND RLT.U_RECORD <> 2");
		sb.append(" AND RCN.U_RECORD <> 2");
		sb.append(" AND RCL.U_RECORD <> 2");
		
		String sql = sb.toString();
		log.info("RdLaneTopology后检查RDLANE005:" + sql);

		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			pstmt = this.getConn().prepareStatement(sql);	
			resultSet = pstmt.executeQuery();
			
			while (resultSet.next()){
				viaLinkSet.add(resultSet.getInt("LINK_PID")) ;
			} 
		}catch (SQLException e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
		return viaLinkSet;
	}

	/**
	 * @param pid
	 * @return
	 * @throws Exception 
	 */
	private int getViaNum(int pid) throws Exception {
		StringBuilder sb = new StringBuilder();

		sb.append("SELECT COUNT(1) FROM RD_LANE_TOPOLOGY T ,RD_LANE_VIA V");
		sb.append(" WHERE T.TOPOLOGY_ID = V.TOPOLOGY_ID");
		sb.append(" AND T.U_RECORD <> 2");
		sb.append(" AND V.U_RECORD <> 2");
		sb.append(" AND T.TOPOLOGY_ID = " + pid);

		String sql = sb.toString();
		log.info("前检查RdLane005:" + sql);
		
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);
		
		if(resultList!=null && resultList.size()>0){
			return Integer.parseInt(resultList.get(0).toString());
		}
		return 0;
	}

	/**
	 * @param pid
	 * @throws Exception 
	 */
	private void check(int pid) throws Exception {
		StringBuilder sb = new StringBuilder();

		sb.append("SELECT T.CONNEXITY_PID FROM RD_LANE_TOPOLOGY T ,RD_LANE_VIA V");
		sb.append(" WHERE T.RELATIONSHIP_TYPE = 1");
		sb.append(" AND T.TOPOLOGY_ID = V.TOPOLOGY_ID");
		sb.append(" AND T.U_RECORD <> 2");
		sb.append(" AND V.U_RECORD <> 2");
		sb.append(" AND T.TOPOLOGY_ID = " + pid);

		String sql = sb.toString();
		log.info("后检查RdLane005:" + sql);
		
		DatabaseOperator getObj = new DatabaseOperator();
        List<Object> resultList = getObj.exeSelect(this.getConn(), sql);
        if (resultList != null && resultList.size() > 0) {
            this.setCheckResult("", "[RD_LANE_CONNEXITY," + resultList.get(0) + "]", 0);
        }
	}


	/**
	 * @param connexityPid
	 * @param viaLinkSet
	 * @return 
	 * @throws Exception 
	 */
	private int getViaNumNotInCross(int connexityPid, Set<Integer> viaLinkSet) throws Exception {
		int num = 0;
		StringBuilder sb = new StringBuilder();

		sb.append("SELECT COUNT(1)");
		sb.append("  FROM RD_LANE_CONNEXITY RLC, RD_CROSS_NODE RCN, RD_LINK RL");
		sb.append(" WHERE RLC.NODE_PID = RCN.NODE_PID");
		sb.append("   AND RLC.U_RECORD <> 2");
		sb.append("   AND RCN.U_RECORD <> 2");
		sb.append("   AND RL.U_RECORD <> 2");
		sb.append("   AND RLC.PID = " + connexityPid);
		sb.append("   AND RL.LINK_PID IN (" + StringUtils.join(viaLinkSet.toArray(),",") + ")");
		sb.append("   AND RL.LINK_PID NOT IN (SELECT RCL.LINK_PID");
		sb.append("                             FROM RD_CROSS_LINK RCL");
		sb.append("                            WHERE RCL.U_RECORD <> 2");
		sb.append("                              AND RCL.PID = RCN.PID)");
		
		String sql = sb.toString();
		log.info("RdLaneTopology后检查RDLANE002:" + sql);

		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if(resultList.size()>0){
			num = Integer.parseInt(resultList.get(0).toString());
		}
		return num;
		
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#postCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {

		
	}

}
