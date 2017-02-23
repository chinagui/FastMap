package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneTopology;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: CrossingLaneOutlinkDirect
 * @author songdongyan
 * @date 2016年12月23日
 * @Description: 退出线方向检查。路口车信车道信息退出线必须为退出车信登记路口方向
 * 新增车信前检查RdLaneConnexity
 * 修改车信前检查RdLaneTopology，新增退出线或者修改车信关系类型时才会触发
 */
public class CrossingLaneOutlinkDirect extends baseRule{

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#preCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for(IRow obj : checkCommand.getGlmList()){
			if (obj instanceof RdLaneConnexity){
				RdLaneConnexity rdLaneConnexity = (RdLaneConnexity)obj;
				checkRdLaneConnexity(rdLaneConnexity,checkCommand.getOperType());
			}
			else if (obj instanceof RdLaneTopology){
				RdLaneTopology rdLaneTopology = (RdLaneTopology)obj;
				checkRdLaneTopology(rdLaneTopology,checkCommand.getOperType());
			}
		}
		
	}

	/**
	 * @param rdLaneTopology
	 * @param operType
	 * @throws Exception 
	 */
	private void checkRdLaneTopology(RdLaneTopology rdLaneTopology, OperType operType) throws Exception {
		//修改车信
		int linkPid = 0;
		int rdLaneConnexityPid = rdLaneTopology.getConnexityPid();
		if(rdLaneTopology.status().equals(ObjStatus.INSERT)){
			//线线车信不触发
			if(rdLaneTopology.getRelationshipType()==2){
				return;
			}
			linkPid = rdLaneTopology.getOutLinkPid();
		}
		else if(rdLaneTopology.status().equals(ObjStatus.UPDATE)){
			//线线车信不触发
			if(rdLaneTopology.changedFields().containsKey("relationshipType")){
				if(Integer.parseInt(rdLaneTopology.changedFields().get("relationshipType").toString())==2){
					return;
				}
			}else if(rdLaneTopology.getRelationshipType()==2){
				return;
			}
			if(rdLaneTopology.changedFields().containsKey("outLinkPid")){
				linkPid = Integer.parseInt(rdLaneTopology.changedFields().get("outLinkPid").toString());
			}	
		}
		if(linkPid!=0){
			StringBuilder sb = new StringBuilder();

			sb.append("SELECT 1 FROM RD_LINK R,RD_LANE_CONNEXITY C");
			sb.append(" WHERE R.U_RECORD <> 2");
			sb.append(" AND C.U_RECORD <> 2");
			sb.append(" AND C.PID =" + rdLaneConnexityPid);
			sb.append(" AND R.DIRECT = 2");
			sb.append(" AND R.E_NODE_PID = C.NODE_PID");
			sb.append(" AND R.LINK_PID = " + linkPid);
			sb.append(" UNION ALL");
			sb.append(" SELECT 1 FROM RD_LINK R,RD_LANE_CONNEXITY C");
			sb.append(" WHERE R.U_RECORD <> 2");
			sb.append(" AND C.U_RECORD <> 2");
			sb.append(" AND C.PID =" + rdLaneConnexityPid);
			sb.append(" AND R.DIRECT = 3");
			sb.append(" AND R.E_NODE_PID = C.NODE_PID");
			sb.append(" AND R.LINK_PID = " + linkPid);

			String sql = sb.toString();
			log.info("RdLaneConnexity前检查CrossingLaneOutlinkDirect:" + sql);
			
			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);
			
			if(resultList.size()>0){
				this.setCheckResult("", "", 0);
			}
		}
		
	}

	/**
	 * @param rdLaneConnexity
	 * @param operType
	 * @throws Exception 
	 */
	private void checkRdLaneConnexity(RdLaneConnexity rdLaneConnexity, OperType operType) throws Exception {
		//新增车信
		if(rdLaneConnexity.status().equals(ObjStatus.INSERT)){
			int nodePid = rdLaneConnexity.getNodePid();
			Set<Integer> outLinkPids = new HashSet<Integer>();
			for(IRow topo:rdLaneConnexity.getTopos()){
				RdLaneTopology topoObj=(RdLaneTopology) topo;
				//线线车信不触发
				if(topoObj.getRelationshipType()==2){
					continue;
				}
				//路口车信触发
				if(topoObj.getRelationshipType()==1){
					outLinkPids.add(topoObj.getOutLinkPid());
				}
			}
			
			if(!outLinkPids.isEmpty()){
				StringBuilder sb = new StringBuilder();

				sb.append("SELECT 1 FROM RD_LINK R");
				sb.append(" WHERE R.U_RECORD <> 2");
				sb.append(" AND R.DIRECT = 2");
				sb.append(" AND R.E_NODE_PID = " + nodePid);
				sb.append(" AND R.LINK_PID IN (" + StringUtils.join(outLinkPids.toArray(),",") +")");
				sb.append(" UNION ALL");
				sb.append(" SELECT 1 FROM RD_LINK R");
				sb.append(" WHERE R.U_RECORD <> 2");
				sb.append(" AND R.DIRECT = 3");
				sb.append(" AND R.S_NODE_PID = " + nodePid);
				sb.append(" AND R.LINK_PID IN (" + StringUtils.join(outLinkPids.toArray(),",") +")");

				String sql = sb.toString();
				log.info("RdLaneConnexity前检查CrossingLaneOutlinkDirect:" + sql);
				
				DatabaseOperator getObj = new DatabaseOperator();
				List<Object> resultList = new ArrayList<Object>();
				resultList = getObj.exeSelect(this.getConn(), sql);
				
				if(resultList.size()>0){
					this.setCheckResult("", "", 0);
				}
			}
		}		
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#postCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		for(IRow obj:checkCommand.getGlmList()){
			if(obj instanceof RdLink ){
				RdLink rdLink=(RdLink) obj;
				checkRdLink(rdLink);
			}
		}
	}

	/**
	 * @param rdLink
	 * @throws Exception 
	 */
	private void checkRdLink(RdLink rdLink) throws Exception {
		if(rdLink.changedFields().containsKey("direct")){
			int direct;
			direct = Integer.parseInt(rdLink.changedFields().get("direct").toString());
			if((direct==2)||(direct==3)){
				StringBuilder sb2 = new StringBuilder();

				sb2.append("SELECT 1 FROM RD_LINK R, RD_LANE_TOPOLOGY T, RD_LANE_CONNEXITY C ");
				sb2.append(" WHERE R.LINK_PID = " + rdLink.getPid());
				sb2.append(" AND T.OUT_LINK_PID = R.LINK_PID");
				sb2.append(" AND T.RELATIONSHIP_TYPE = 1");
				sb2.append(" AND C.PID = T.CONNEXITY_PID");
				sb2.append(" AND T.U_RECORD <> 2");
				sb2.append(" AND R.U_RECORD <> 2");
				sb2.append(" AND R.DIRECT = 2");
				sb2.append(" AND C.U_RECORD <> 2");
				sb2.append(" AND R.E_NODE_PID = C.NODE_PID");
				sb2.append(" UNION");
				sb2.append(" SELECT 1 FROM RD_LINK R, RD_LANE_TOPOLOGY T, RD_LANE_CONNEXITY C");
				sb2.append(" WHERE R.LINK_PID = " + rdLink.getPid());
				sb2.append(" AND T.OUT_LINK_PID = R.LINK_PID");
				sb2.append(" AND T.RELATIONSHIP_TYPE = 1");
				sb2.append(" AND C.PID = T.CONNEXITY_PID");
				sb2.append(" AND T.U_RECORD <> 2");
				sb2.append(" AND R.U_RECORD <> 2");
				sb2.append(" AND C.U_RECORD <> 2");
				sb2.append(" AND R.DIRECT = 3");
				sb2.append(" AND R.S_NODE_PID = C.NODE_PID");
				
				String sql2 = sb2.toString();
				log.info("RdLink后检查CrossingLaneOutlinkDirect:" + sql2);
	
				DatabaseOperator getObj = new DatabaseOperator();
				List<Object> resultList = new ArrayList<Object>();
				resultList = getObj.exeSelect(this.getConn(), sql2);
	
				if(resultList.size()>0){
					String target = "[RD_LINK," + rdLink.getPid() + "]";
					this.setCheckResult("", target, 0);
				}
			}
		}
		
	}

}
