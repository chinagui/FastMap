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
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneVia;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: GLM19001_2
 * @author songdongyan
 * @date 2016年12月22日
 * @Description: GLM19001_2.java
 */
public class GLM19001_2 extends baseRule{

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
			else if (obj instanceof RdLaneVia){
				RdLaneVia rdLaneRdLaneVia = (RdLaneVia)obj;
				checkRdLaneVia(rdLaneRdLaneVia,checkCommand.getOperType());
			}
		}
		
	}

	/**
	 * @param rdLaneRdLaneVia
	 * @param operType
	 * @throws Exception 
	 */
	private void checkRdLaneVia(RdLaneVia rdLaneVia, OperType operType) throws Exception {
		int linkPid = 0;
		if(rdLaneVia.status().equals(ObjStatus.INSERT)){
			linkPid = rdLaneVia.getLinkPid();
		}
		else if(rdLaneVia.status().equals(ObjStatus.UPDATE)){
			if(rdLaneVia.changedFields().containsKey("linkPid")){
				linkPid = (int) rdLaneVia.changedFields().get("linkPid");
			}	
		}
		if(linkPid!=0){
			StringBuilder sb = new StringBuilder();

			sb.append("SELECT 1 FROM RD_LINK R");
			sb.append(" WHERE R.LINK_PID = " + linkPid);
			sb.append(" AND R.KIND = 9");
			sb.append(" AND R.U_RECORD <> 2");

			String sql = sb.toString();
			log.info("RdLaneVia前检查GLM19001_2:" + sql);
			
			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);
			
			if(resultList.size()>0){
				this.setCheckResult("", "", 0);
			}
		}
		
	}

	/**
	 * @param rdLaneTopology
	 * @param operType
	 * @throws Exception 
	 */
	private void checkRdLaneTopology(RdLaneTopology rdLaneTopology, OperType operType) throws Exception {
		int linkPid = 0;
		if(rdLaneTopology.status().equals(ObjStatus.INSERT)){
			//路口车信不触发
			if(rdLaneTopology.getRelationshipType()==1){
				return;
			}
			linkPid = rdLaneTopology.getOutLinkPid();
		}
		else if(rdLaneTopology.status().equals(ObjStatus.UPDATE)){
			//路口车信不触发
			if(rdLaneTopology.changedFields().containsKey("relationshipType")){
				if((int) rdLaneTopology.changedFields().get("relationshipType")==1){
					return;
				}
			}else if(rdLaneTopology.getRelationshipType()==1){
				return;
			}
			if(rdLaneTopology.changedFields().containsKey("outLinkPid")){
				linkPid = (int) rdLaneTopology.changedFields().get("outLinkPid");
			}	
		}
		if(linkPid!=0){
			StringBuilder sb = new StringBuilder();

			sb.append("SELECT 1 FROM RD_LINK R");
			sb.append(" WHERE R.LINK_PID = " + linkPid);
			sb.append(" AND R.KIND = 9");
			sb.append(" AND R.U_RECORD <> 2");

			String sql = sb.toString();
			log.info("RdLaneTopology前检查GLM19001_2:" + sql);
			
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
		if(rdLaneConnexity.status().equals(ObjStatus.INSERT)){
			Set<Integer> linkPids = new HashSet<Integer>();
			linkPids.add(rdLaneConnexity.getInLinkPid());
			
			for(IRow topo:rdLaneConnexity.getTopos()){
				RdLaneTopology topoObj=(RdLaneTopology) topo;
				//路口车信不触发
				if(topoObj.getRelationshipType()==1){
					continue;
				}
				linkPids.add(topoObj.getOutLinkPid());
				if(topoObj.getRelationshipType()==2){
					List<IRow> vias=topoObj.getVias();
					for(IRow via:vias){
						RdLaneVia viaObj = (RdLaneVia)via;
						linkPids.add(viaObj.getLinkPid());
					}
				}
			}
			StringBuilder sb = new StringBuilder();

			sb.append("SELECT 1 FROM RD_LINK R");
			sb.append(" WHERE R.LINK_PID IN (" + StringUtils.join(linkPids.toArray(),",") + ")");
			sb.append(" AND R.KIND = 9");
			sb.append(" AND R.U_RECORD <> 2");

			String sql = sb.toString();
			log.info("RdLaneConnexity前检查GLM19001_2:" + sql);
			
			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);
			
			if(resultList.size()>0){
				this.setCheckResult("", "", 0);
			}
		}
		
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#postCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		for(IRow obj : checkCommand.getGlmList()){
			//道路属性编辑
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
		boolean checkFlag = false;
		if(rdLink.changedFields().containsKey("kind")){
			int kind = Integer.parseInt(rdLink.changedFields().get("kind").toString());
			if((kind==9)){
				checkFlag = true;
			}
		}
		if(checkFlag){
			StringBuilder sb2 = new StringBuilder();

			sb2.append("SELECT 1 FROM RD_LANE_CONNEXITY C, RD_LANE_TOPOLOGY T");
			sb2.append(" WHERE C.PID = T.CONNEXITY_PID");
			sb2.append(" AND T.RELATIONSHIP_TYPE = 2");
			sb2.append(" AND C.U_RECORD <> 2");
			sb2.append(" AND T.U_RECORD <> 2");
			sb2.append(" AND C.IN_LINK_PID = " + rdLink.getPid());
			sb2.append(" UNION");
			sb2.append(" SELECT 1 FROM RD_LANE_TOPOLOGY T");
			sb2.append(" WHERE T.RELATIONSHIP_TYPE = 2");
			sb2.append(" AND T.U_RECORD <> 2");
			sb2.append(" AND T.OUT_LINK_PID = " + rdLink.getPid());
			sb2.append(" UNION");
			sb2.append(" SELECT 1 FROM RD_LANE_VIA V, RD_LANE_TOPOLOGY T");
			sb2.append(" WHERE T.RELATIONSHIP_TYPE = 2");
			sb2.append(" AND V.TOPOLOGY_ID = T.TOPOLOGY_ID");
			sb2.append(" AND V.U_RECORD <> 2");
			sb2.append(" AND T.U_RECORD <> 2");
			sb2.append(" AND T.OUT_LINK_PID = " + rdLink.getPid());
			
			String sql2 = sb2.toString();
			log.info("RdLink后检查GLM19001_2:" + sql2);

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
