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
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: GLM19001_3
 * @author songdongyan
 * @date 2016年12月22日
 * @Description: GLM19001_3.java
 */
public class GLM19001_3 extends baseRule{

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
				linkPid = Integer.parseInt( rdLaneVia.changedFields().get("linkPid").toString());
			}	
		}
		if(linkPid!=0){
			StringBuilder sb = new StringBuilder();

			sb.append("SELECT 1 FROM RD_LINK_FORM F");
			sb.append(" WHERE F.LINK_PID = " + linkPid);
			sb.append(" AND F.FORM_OF_WAY = 22");
			sb.append(" AND F.U_RECORD <> 2");
			sb.append(" AND NOT EXISTS (SELECT 1 FROM RD_LINK_FORM FF WHERE FF.LINK_PID = F.LINK_PID AND FF.FORM_OF_WAY = 50 AND FF.U_RECORD <> 2)");

			String sql = sb.toString();
			log.info("RdLaneVia前检查GLM19001_3:" + sql);
			
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
			linkPid = rdLaneTopology.getOutLinkPid();
		}
		else if(rdLaneTopology.status().equals(ObjStatus.UPDATE)){
			if(rdLaneTopology.changedFields().containsKey("outLinkPid")){
				linkPid = Integer.parseInt(rdLaneTopology.changedFields().get("outLinkPid").toString());
			}	
		}
		if(linkPid!=0){
			StringBuilder sb = new StringBuilder();

			sb.append("SELECT 1 FROM RD_LINK_FORM F");
			sb.append(" WHERE F.LINK_PID = " + linkPid);
			sb.append(" AND F.FORM_OF_WAY = 22");
			sb.append(" AND F.U_RECORD <> 2");

			String sql = sb.toString();
			log.info("RdLaneTopology前检查GLM19001_3:" + sql);
			
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
			Set<Integer> viaLinkPids = new HashSet<Integer>();
			linkPids.add(rdLaneConnexity.getInLinkPid());
			
			for(IRow topo:rdLaneConnexity.getTopos()){
				RdLaneTopology topoObj=(RdLaneTopology) topo;
				linkPids.add(topoObj.getOutLinkPid());
				if(topoObj.getRelationshipType()==2){
					List<IRow> vias=topoObj.getVias();
					for(IRow via:vias){
						RdLaneVia viaObj = (RdLaneVia)via;
						viaLinkPids.add(viaObj.getLinkPid());
					}
				}
			}
			StringBuilder sb = new StringBuilder();

			sb.append("SELECT 1 FROM RD_LINK_FORM F");
			sb.append(" WHERE F.LINK_PID IN (" + StringUtils.join(linkPids.toArray(),",") + ")");
			sb.append(" AND F.FORM_OF_WAY = 22");
			sb.append(" AND F.U_RECORD <> 2");
			
			if(!viaLinkPids.isEmpty()){
				sb.append(" UNION ALL");
				sb.append(" SELECT 1 FROM RD_LINK_FORM F");
				sb.append(" WHERE F.LINK_PID IN (" + StringUtils.join(viaLinkPids.toArray(),",") + ")");
				sb.append(" AND F.FORM_OF_WAY = 22");
				sb.append(" AND F.U_RECORD <> 2");
				sb.append(" AND NOT EXISTS (SELECT 1 FROM RD_LINK_FORM FF WHERE FF.LINK_PID = F.LINK_PID AND FF.FORM_OF_WAY = 50 AND FF.U_RECORD <> 2)");
			}

			String sql = sb.toString();
			log.info("RdLaneConnexity前检查GLM19001_3:" + sql);
			
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
			if(obj instanceof RdLinkForm ){
				RdLinkForm RdLinkForm=(RdLinkForm) obj;
				checkRdLinkForm(RdLinkForm);
			}
		}
	}

	/**
	 * @param rdLinkForm
	 * @throws Exception 
	 */
	private void checkRdLinkForm(RdLinkForm rdLinkForm) throws Exception {
		boolean checkFlg = false;
		if(rdLinkForm.status().equals(ObjStatus.UPDATE)){
			if(rdLinkForm.changedFields().containsKey("formOfWay")){
				//交叉口内道路修改为非交叉口内道路
				if(rdLinkForm.getFormOfWay()==50){
					checkFlg = true;
				}
				//环岛
				int formOfWay = Integer.parseInt(rdLinkForm.changedFields().get("formOfWay").toString());
				if(formOfWay==22){
					checkFlg = true;
				}
			}
		}else if(rdLinkForm.status().equals(ObjStatus.INSERT)){
			int formOfWay = rdLinkForm.getFormOfWay();
			if(formOfWay==22){
				checkFlg = true;
			}
		}else if(rdLinkForm.status().equals(ObjStatus.DELETE)){
			int formOfWay = rdLinkForm.getFormOfWay();
			if(formOfWay==50){
				checkFlg = true;
			}
		}

		if(checkFlg){
			StringBuilder sb2 = new StringBuilder();

			sb2.append("SELECT 1 FROM RD_LANE_CONNEXITY C,RD_LINK_FORM F");
			sb2.append(" WHERE C.IN_LINK_PID = " + rdLinkForm.getLinkPid());
			sb2.append(" AND C.U_RECORD <> 2");
			sb2.append(" AND F.LINK_PID = C.IN_LINK_PID");
			sb2.append(" AND F.FORM_OF_WAY = 22");
			sb2.append(" AND F.U_RECORD <> 2");
			sb2.append(" UNION");
			sb2.append(" SELECT 1 FROM RD_LANE_TOPOLOGY L,RD_LINK_FORM F");
			sb2.append(" WHERE L.OUT_LINK_PID = " + rdLinkForm.getLinkPid());
			sb2.append(" AND L.U_RECORD <> 2");
			sb2.append(" AND F.LINK_PID = L.OUT_LINK_PID");
			sb2.append(" AND F.FORM_OF_WAY = 22");
			sb2.append(" AND F.U_RECORD <> 2");
			sb2.append(" UNION");
			sb2.append(" SELECT 1 FROM RD_LANE_VIA V,RD_LINK_FORM F");
			sb2.append(" WHERE V.LINK_PID = " + rdLinkForm.getLinkPid());
			sb2.append(" AND V.U_RECORD <> 2");
			sb2.append(" AND F.LINK_PID = V.LINK_PID");
			sb2.append(" AND F.FORM_OF_WAY = 22");
			sb2.append(" AND F.U_RECORD <> 2");
			sb2.append(" AND NOT EXISTS (SELECT 1 FROM RD_LINK_FORM FF");
			sb2.append(" WHERE FF.LINK_PID = V.LINK_PID");
			sb2.append(" AND FF.FORM_OF_WAY = 50");
			sb2.append(" AND FF.U_RECORD <> 2)");


			String sql2 = sb2.toString();
			log.info("RdLinkForm后检查GLM19001_3:" + sql2);

			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql2);

			if(resultList.size()>0){
				String target = "[RD_LINK," + rdLinkForm.getLinkPid() + "]";
				this.setCheckResult("", target, 0);
			}
		}
	}

}
