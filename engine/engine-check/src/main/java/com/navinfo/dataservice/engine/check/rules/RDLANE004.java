package com.navinfo.dataservice.engine.check.rules;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneTopology;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneVia;
import com.navinfo.dataservice.engine.check.core.baseRule;

/** 
 * @ClassName: RDLANE004
 * @author songdongyan
 * @date 2017年2月22日
 * @Description: RDLANE004 经过线没有闭合，请完成经过线设置
 * 新增车信前检查:RdLaneConnexity（新增）
 * 修改车信前检查:RdLaneTopology（新增，修改）,RdLaneVia（新增，修改，删除）
 */
public class RDLANE004 extends baseRule{

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#preCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for(IRow obj : checkCommand.getGlmList()){
			//create的时候只有主表对象，其中包含的内容涵盖子表内容，可直接用
			if (obj instanceof RdLaneConnexity){//交限
				RdLaneConnexity laneObj=(RdLaneConnexity) obj;
				checkRdLaneConnexity(laneObj);
			}
			//修改车信
			else if(obj instanceof RdLaneTopology){
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
	 * @param rdLaneTopology
	 * @param checkCommand
	 * @throws SQLException 
	 */
	private void checkRdLaneTopology(RdLaneTopology rdLaneTopology, CheckCommand checkCommand) throws SQLException {
		boolean checkFlg = false;
		Set<Integer> linkPidList = new HashSet<Integer>();
		linkPidList.add(rdLaneTopology.getOutLinkPid());

		if(rdLaneTopology.status().equals(ObjStatus.INSERT)){
			if(rdLaneTopology.getRelationshipType()==2){
				checkFlg = true;
			}
		}
		
		else if(rdLaneTopology.status().equals(ObjStatus.UPDATE)){
			if(rdLaneTopology.changedFields().containsKey("relationshipType")){
				int relationshipType = Integer.parseInt(rdLaneTopology.changedFields().get("relationshipType").toString());
				if(relationshipType==2){
					checkFlg = true;
				}
			}
		}
		if(checkFlg){
			for(IRow objInnerLoop : checkCommand.getGlmList()){
				if(objInnerLoop instanceof RdLaneVia){
					RdLaneVia rdLaneVia = (RdLaneVia)objInnerLoop;
					//排除删除的经过线
					if(rdLaneVia.status().equals(ObjStatus.DELETE)){
						continue;
					}
					if(rdLaneVia.getTopologyId()==rdLaneTopology.getPid()){
						linkPidList.add(rdLaneVia.getLinkPid());
					}
				}
			}
			//获取车信进入线
			String sql = "SELECT C.IN_LINK_PID FROM RD_LANE_CONNEXITY C WHERE C.U_RECORD <> 2 AND C.PID = " + rdLaneTopology.getConnexityPid();
			log.info("前检查RdLane004:" + sql);
			PreparedStatement pstmt = this.getConn().prepareStatement(sql);	
			ResultSet resultSet = pstmt.executeQuery();
			
			if (resultSet.next()){
				linkPidList.add(resultSet.getInt("IN_LINK_PID")) ;
			} 
			resultSet.close();
			pstmt.close();
			//检查连通性
			checkConnexity(rdLaneTopology.getConnexityPid(),linkPidList);
		}
	
	}

	/**
	 * @param rdLaneVia
	 * @param checkCommand
	 * @throws SQLException 
	 */
	private void checkRdLaneVia(RdLaneVia rdLaneVia, CheckCommand checkCommand) throws SQLException {
		boolean checkFlg = false;
		Set<Integer> linkPidList = new HashSet<Integer>();
		if(rdLaneVia.status().equals(ObjStatus.INSERT)){
			checkFlg = true;
		}
		else if(rdLaneVia.status().equals(ObjStatus.UPDATE)){
			if(rdLaneVia.changedFields().containsKey("linkPid")){
				checkFlg = true;
			}
		}
		
		else if(rdLaneVia.status().equals(ObjStatus.DELETE)){
			checkFlg = true;
		}
		if(checkFlg){
			for(IRow objInnerLoop : checkCommand.getGlmList()){
				if(objInnerLoop instanceof RdLaneVia){
					RdLaneVia via = (RdLaneVia)objInnerLoop;
					//排除删除的经过线
					if(rdLaneVia.status().equals(ObjStatus.DELETE)){
						continue;
					}
					if(via.getTopologyId()==rdLaneVia.getTopologyId()){
						linkPidList.add(rdLaneVia.getLinkPid());
					}
				}
			}
			//获取车信进入线,退出线
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT C.IN_LINK_PID,T.OUT_LINK_PID,V.LINK_PID FROM RD_LANE_CONNEXITY C,RD_LANE_TOPOLOGY T,RD_LANE_VIA V");
			sb.append(" WHERE C.PID = T.CONNEXITY_PID");
			sb.append(" AND V.TOPOLOGY_ID = T.TOPOLOGY_ID");
			sb.append(" AND C.U_RECORD <> 2");
			sb.append(" AND T.U_RECORD <> 2");
			sb.append(" AND V.U_RECORD <> 2");
			sb.append(" AND T.TOPOLOGY_ID = " + rdLaneVia.getTopologyId());
			String sql = sb.toString();
			log.info("前检查RdLane004:" + sql);
			PreparedStatement pstmt = this.getConn().prepareStatement(sql);	
			ResultSet resultSet = pstmt.executeQuery();
			int connexityPid = 0;
			if (resultSet.next()){
				connexityPid = resultSet.getInt("IN_LINK_PID");
				linkPidList.add(resultSet.getInt("IN_LINK_PID")) ;
				linkPidList.add(resultSet.getInt("OUT_LINK_PID")) ;
				linkPidList.add(resultSet.getInt("LINK_PID")) ;
			} 
			resultSet.close();
			pstmt.close();
			//检查连通性
			checkConnexity(connexityPid,linkPidList);
		}
		
	}

	/**
	 * @param laneObj
	 * @throws SQLException 
	 */
	private void checkRdLaneConnexity(RdLaneConnexity rdLaneConnexity) throws SQLException {
		if(rdLaneConnexity.status().equals(ObjStatus.INSERT)){
			for(IRow topo:rdLaneConnexity.getTopos()){
				//所有linkPid
				Set<Integer> linkPidList = new HashSet<Integer>();
				linkPidList.add(rdLaneConnexity.getInLinkPid());
				RdLaneTopology topoObj=(RdLaneTopology) topo;
				linkPidList.add(topoObj.getOutLinkPid());
				if(topoObj.getRelationshipType()==2){
					for(IRow via:topoObj.getVias()){
						RdLaneVia viaObj = (RdLaneVia) via;
						linkPidList.add(viaObj.getLinkPid());
					}
					checkConnexity(rdLaneConnexity.getPid(),linkPidList);
				}
			}
		}
	}

	/**
	 * @param pid
	 * @param linkPidList
	 * @throws SQLException 
	 */
	private void checkConnexity(int pid, Set<Integer> linkPidList) throws SQLException {
		StringBuilder sb = new StringBuilder();
		sb.append(" SELECT R.S_NODE_PID,R.E_NODE_PID FROM RD_LINK R  ");
		sb.append(" WHERE R.LINK_PID IN (" + StringUtils.join(linkPidList.toArray(),",") + ")");
		sb.append(" AND R.U_RECORD <> 2");
		String sql = sb.toString();
		log.info("前检查RdLane004:" + sql);
		PreparedStatement pstmt = this.getConn().prepareStatement(sql);	
		ResultSet resultSet = pstmt.executeQuery();
		Map<Integer,Integer> nodePidList = new HashMap<Integer,Integer>();
		if (resultSet.next()){
			if(nodePidList.containsKey(resultSet.getInt("S_NODE_PID"))){
				int num = nodePidList.get(resultSet.getInt("S_NODE_PID"));
				nodePidList.put(resultSet.getInt("S_NODE_PID"), num+1);
			}else{
				nodePidList.put(resultSet.getInt("S_NODE_PID"), 1);
			}
			if(nodePidList.containsKey(resultSet.getInt("E_NODE_PID"))){
				int num = nodePidList.get(resultSet.getInt("E_NODE_PID"));
				nodePidList.put(resultSet.getInt("E_NODE_PID"), num+1);
			}else{
				nodePidList.put(resultSet.getInt("E_NODE_PID"), 1);
			}
		} 
		resultSet.close();
		pstmt.close();
		
		if(nodePidList.size()!=(linkPidList.size()+1)){
			this.setCheckResult("", "", 0);
			return;
		}
		int num2 = 0;
		int num1 = 0;
		for(Map.Entry<Integer, Integer> entry:nodePidList.entrySet()){
			if(entry.getValue() == 1){
				num1 ++;
			}else if(entry.getValue() == 2){
				num1 ++;
			}
		}
		
		if((num1==2)&&(num2==linkPidList.size()-1)){
			return;
		}else{
			this.setCheckResult("", "", 0);
			return;
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
