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

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneTopology;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneVia;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;
/**
 * 车信	html	RDLANE002	后台	
 * 线线车信必须有经过线
 * @author zhangxiaoyi
 *新增车信服务端前检查RdLaneConnexity
 *修改车信服务端前检查RdLaneTopology
 */
public class RdLane002 extends baseRule {

	public RdLane002() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for(IRow obj : checkCommand.getGlmList()){
//			//create的时候只有主表对象，其中包含的内容涵盖子表内容，可直接用
//			if (obj instanceof RdLaneConnexity){//交限
//				RdLaneConnexity laneObj=(RdLaneConnexity) obj;
//				checkRdLaneConnexity(laneObj);
//			}
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
		if(rdLaneVia.status().equals(ObjStatus.DELETE)){
			int connexityPid = 0;
			Map<Integer,Integer> viaLinkMap = new HashMap<Integer,Integer>();


			StringBuilder sb = new StringBuilder();

			sb.append("SELECT DISTINCT V.LINK_PID,T.CONNEXITY_PID FROM RD_LANE_TOPOLOGY T,RD_LANE_VIA V ");
			sb.append(" WHERE T.TOPOLOGY_ID = " + rdLaneVia.getTopologyId());
			sb.append(" AND T.TOPOLOGY_ID = V.TOPOLOGY_ID");
			sb.append(" AND T.RELATIONSHIP_TYPE = 2");
			sb.append(" AND T.U_RECORD <> 2");
			sb.append(" AND V.U_RECORD <> 2");

			String sql = sb.toString();
			log.info("RdLaneVia前检查RdLane002:" + sql);
			
			PreparedStatement pstmt = null;
			ResultSet resultSet = null;
			try {
				pstmt = this.getConn().prepareStatement(sql);	
				resultSet = pstmt.executeQuery();
				
				while (resultSet.next()){
					int linkPid = resultSet.getInt("LINK_PID");
					viaLinkMap.put(linkPid, 1);
					connexityPid = resultSet.getInt("CONNEXITY_PID");
				} 
			}catch (SQLException e) {
				throw e;
			} finally {
				DbUtils.closeQuietly(resultSet);
				DbUtils.closeQuietly(pstmt);
			}

			//如果删除的经过线不是线线车信经过线，则不检查
			if(viaLinkMap.size()==0){
				return;
			}
			
			for(IRow objInnerLoop : checkCommand.getGlmList()){
				if(objInnerLoop instanceof RdLaneTopology){
					RdLaneTopology rdLaneTopology = (RdLaneTopology)objInnerLoop;
					if(rdLaneTopology.getPid() == rdLaneVia.getTopologyId()){
						//删除了就不在考虑范围内了
						if(rdLaneTopology.status().equals(ObjStatus.DELETE)){
							return;
						}
						//路口车信不在考虑范围内
						else if(rdLaneTopology.status().equals(ObjStatus.UPDATE)){
							if(rdLaneTopology.changedFields().containsKey("relationshipType")){
								int relationshipType = Integer.parseInt(rdLaneTopology.changedFields().get("relationshipType").toString());
								if(relationshipType==1){
									return;
								}
							}
						}
					}
				}
				
				if(objInnerLoop instanceof RdLaneVia){
					RdLaneVia via = (RdLaneVia)objInnerLoop;
					if(via.getTopologyId() == rdLaneVia.getTopologyId()){
						if(via.status().equals(ObjStatus.DELETE)){
							int linkPid = via.getLinkPid();
							if(viaLinkMap.containsKey(linkPid)){
								int value = viaLinkMap.get(linkPid) -1;
								viaLinkMap.put(linkPid, value);
							}
						}
						//不知道这种奇葩情况会不会出现，先写上，以备万一
						else if(via.status().equals(ObjStatus.UPDATE)){
							int linkPid = via.getLinkPid();
							if(viaLinkMap.containsKey(linkPid)){
								int value = viaLinkMap.get(linkPid) + 1;
								viaLinkMap.put(linkPid, value);
							}else{
								viaLinkMap.put(linkPid, 1);
							}
						}
						else if(via.status().equals(ObjStatus.INSERT)){
							int linkPid = via.getLinkPid();
							if(viaLinkMap.containsKey(linkPid)){
								int value = viaLinkMap.get(linkPid) + 1;
								viaLinkMap.put(linkPid, value);
							}else{
								viaLinkMap.put(linkPid, 1);
							}
						}
					}
				}
			}
			Set<Integer> viaLinkSet = new HashSet<Integer>();
			for(Map.Entry<Integer, Integer> entry:viaLinkMap.entrySet()){
				if(entry.getValue()>0){
					viaLinkSet.add(entry.getKey());
				}
			}
			//该经过线所在的RdLaneTopology内经过线数量
			int num = getViaNumNotInCross(connexityPid,viaLinkSet);
			//经过线全部为同一路口内link，则认为没有经过线信息
			if(0==num){
				this.setCheckResult("", "", 0);
				return;
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
			if(rdLaneTopology.getRelationshipType()==2){
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
					this.setCheckResult("", "", 0);
					return;
				}
				else{
					//获取经过线中不存在于同一路口内link数
					int num = getViaNumNotInCross(rdLaneTopology.getConnexityPid(),viaLinkSet);
					//经过线全部为同一路口内link，则认为没有经过线信息
					if(0==num){
						this.setCheckResult("", "", 0);
						return;
					}
				}
			}
		}
		//修改联通关系
		else if(rdLaneTopology.status().equals(ObjStatus.UPDATE)){
			int relationshipType = 1;
			if(rdLaneTopology.changedFields().containsKey("relationshipType")){
				relationshipType = Integer.parseInt(rdLaneTopology.changedFields().get("relationshipType").toString());
			}
			//获取车信经过线
			Map<Integer,Integer> viaLinkMap = getViaLinkNotInCross(rdLaneTopology.getPid());
			if(relationshipType==2){
				for(IRow objInnerLoop : checkCommand.getGlmList()){
					if(objInnerLoop instanceof RdLaneVia){
						RdLaneVia via = (RdLaneVia)objInnerLoop;
						if(via.status().equals(ObjStatus.DELETE)){
							int linkPid = via.getLinkPid();
							if(viaLinkMap.containsKey(linkPid)){
								int value = viaLinkMap.get(linkPid) -1;
								viaLinkMap.put(linkPid, value);
							}
						}
						//不知道这种奇葩情况会不会出现，先写上，以备万一
						else if(via.status().equals(ObjStatus.UPDATE)){
							int linkPid = via.getLinkPid();
							if(viaLinkMap.containsKey(linkPid)){
								int value = viaLinkMap.get(linkPid) + 1;
								viaLinkMap.put(linkPid, value);
							}else{
								viaLinkMap.put(linkPid, 1);
							}
						}
						else if(via.status().equals(ObjStatus.INSERT)){
							int linkPid = via.getLinkPid();
							if(viaLinkMap.containsKey(linkPid)){
								int value = viaLinkMap.get(linkPid) + 1;
								viaLinkMap.put(linkPid, value);
							}else{
								viaLinkMap.put(linkPid, 1);
							}
						}
					}
				}
				
				Set<Integer> viaLinkSet = new HashSet<Integer>();
				for(Map.Entry<Integer, Integer> entry:viaLinkMap.entrySet()){
					if(entry.getValue()>0){
						viaLinkSet.add(entry.getKey());
					}
				}
				//获取经过线中不存在于同一路口内link数
				int num = getViaNumNotInCross(rdLaneTopology.getConnexityPid(),viaLinkSet);
				//经过线全部为同一路口内link，则认为没有经过线信息
				if(0==num){
					this.setCheckResult("", "", 0);
					return;
				}
			}
		}
		
	}

	/**
	 * @param pid
	 * @return
	 * @throws SQLException 
	 */
	private Map<Integer,Integer> getViaLinkNotInCross(int pid) throws SQLException {
		Map<Integer,Integer> linkPidMap = new HashMap<Integer,Integer>();
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
				linkPidMap.put(resultSet.getInt("LINK_PID"),1) ;
			} 
		}catch (SQLException e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
		return linkPidMap;
	}

	/**
	 * @param connexityPid
	 * @param viaLinkSet
	 * @return 
	 * @throws Exception 
	 */
	private int getViaNumNotInCross(int connexityPid, Set<Integer> viaLinkSet) throws Exception {
		int num = 0;
		if(viaLinkSet.isEmpty()){
			return num;
		}
		StringBuilder sb = new StringBuilder();

		sb.append("SELECT COUNT(1)");
		sb.append("  FROM RD_LANE_CONNEXITY RLC, RD_CROSS_NODE RCN, RD_LINK RL,RD_CROSS_LINK RCL");
		sb.append(" WHERE RLC.NODE_PID = RCN.NODE_PID");
		sb.append("   AND RLC.U_RECORD <> 2");
		sb.append("   AND RCN.U_RECORD <> 2");
		sb.append("   AND RL.U_RECORD <> 2");
		sb.append("   AND RLC.PID = " + connexityPid);
		sb.append("   AND RL.LINK_PID IN (" + StringUtils.join(viaLinkSet.toArray(),",") + ")");
		sb.append("   AND RL.LINK_PID = RCL.LINK_PID");
		sb.append("   AND RCL.PID = RCN.PID");
		sb.append("   AND RCL.U_RECORD <> 2");
		
		String sql = sb.toString();
		log.info("RdLaneTopology后检查RDLANE002:" + sql);

		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if(resultList.size()>0){
			num = viaLinkSet.size() - Integer.parseInt(resultList.get(0).toString());
		}
		return num;
		
	}

	/**
	 * @param pid
	 * @return
	 * @throws Exception 
	 */
	private int getViaNum(int pid) throws Exception {
		StringBuilder sb = new StringBuilder();

		sb.append("SELECT COUNT(1) FROM RD_LANE_TOPOLOGY T ,RD_LANE_VIA V");
		sb.append(" WHERE T.RELATIONSHIP_TYPE = 1");
		sb.append(" AND T.TOPOLOGY_ID = V.TOPOLOGY_ID");
		sb.append(" AND T.U_RECORD <> 2");
		sb.append(" AND V.U_RECORD <> 2");
		sb.append(" AND T.TOPOLOGY_ID = " + pid);

		String sql = sb.toString();
		log.info("前检查RdLane002:" + sql);
		
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);
		
		if(resultList!=null && resultList.size()>0){
			return Integer.parseInt(resultList.get(0).toString());
		}
		return 0;
	}

	/**
	 * @param laneObj
	 */
	private void checkRdLaneConnexity(RdLaneConnexity laneObj) {
		Map<String, Object> changedFields=laneObj.changedFields();
		//新增执行该检查
		if(changedFields!=null && !changedFields.isEmpty()){return;}
		for(IRow topo:laneObj.getTopos()){
			RdLaneTopology topoObj=(RdLaneTopology) topo;
			if(topoObj.getRelationshipType()==2){
				List<IRow> vias=topoObj.getVias();
				if(vias==null||vias.size()==0){
					this.setCheckResult("", "", 0);
					return;
				}
			}
		}
		
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
