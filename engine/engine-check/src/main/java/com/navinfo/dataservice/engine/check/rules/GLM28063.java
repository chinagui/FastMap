package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.road.RdRoad;
import com.navinfo.dataservice.dao.glm.model.rd.road.RdRoadLink;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: GLM28063
 * @author songdongyan
 * @date 2017年1月5日
 * @Description: RD_ROAD_LINK表中LINK的种别不能为10级路，否则报LOG
 * 修改CRFI:RdRoadLink
 * 新增CRFI:RdRoad
 * Link种别编辑:RdLink
 */
public class GLM28063 extends baseRule{

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#preCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#postCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		for(IRow obj : checkCommand.getGlmList()){
			//RdInterLink新增会触发
			if (obj instanceof RdRoadLink){
				RdRoadLink rdRoadLink = (RdRoadLink)obj;
				checkRdRoadLink(rdRoadLink);
			}
			//RdInter新增会触发
			else if (obj instanceof RdRoad){
				RdRoad rdRoad = (RdRoad)obj;
				checkRdRoad(rdRoad);
			}
			//RdLink修改会触发
			else if (obj instanceof RdLink){
				RdLink rdLink = (RdLink)obj;
				checkRdLink(rdLink);
			}
		}
		
	}

	/**
	 * @param rdLink
	 * @throws Exception 
	 */
	private void checkRdLink(RdLink rdLink) throws Exception {
		//link种别编辑触发检查
		if(rdLink.changedFields().containsKey("kind")){
			int kind = Integer.parseInt(rdLink.changedFields().get("kind").toString());
			if(kind==10){
				StringBuilder sb = new StringBuilder();
				sb.append("SELECT 1 FROM RD_ROAD_LINK RIL");
				sb.append(" WHERE RIL.U_RECORD <> 2");
				sb.append(" AND RIL.LINK_PID = " + rdLink.getPid());
				
				String sql = sb.toString();
				log.info("RdLink后检查GLM28063:" + sql);
				
				DatabaseOperator getObj = new DatabaseOperator();
				List<Object> resultList = new ArrayList<Object>();
				resultList = getObj.exeSelect(this.getConn(), sql);
				
				if(resultList.size()>0){
					String target = "[RD_LINK," + rdLink.getPid() + "]";
					this.setCheckResult("", target, 0);
				}
			}	
		}
		
	}

	/**
	 * @param rdRoad
	 * @throws Exception 
	 */
	private void checkRdRoad(RdRoad rdRoad) throws Exception {
		// 新增RdRoad
		if(rdRoad.status().equals(ObjStatus.INSERT)){
			Set<Integer> linkPidSet = new HashSet<Integer>();
			for(IRow link:rdRoad.getLinks()){
				RdRoadLink rdRoadLink = (RdRoadLink)link;
				linkPidSet.add(rdRoadLink.getLinkPid());
			}
			if(!linkPidSet.isEmpty()){
				StringBuilder sb = new StringBuilder();

				sb.append("SELECT 1 FROM RD_ROAD_LINK RIL, RD_LINK RL");
				sb.append(" WHERE RIL.U_RECORD <> 2");
				sb.append(" AND RL.U_RECORD <> 2");
				sb.append(" AND RL.KIND = 10");
				sb.append(" AND RL.LINK_PID IN (" + StringUtils.join(linkPidSet.toArray(),",") + ")");
				
				String sql = sb.toString();
				log.info("RdRoad后检查GLM28063:" + sql);
				
				DatabaseOperator getObj = new DatabaseOperator();
				List<Object> resultList = new ArrayList<Object>();
				resultList = getObj.exeSelect(this.getConn(), sql);
				
				if(resultList.size()>0){
					String target = "[RD_ROAD," + rdRoad.getPid() + "]";
					this.setCheckResult("", target, 0);
				}
			}
			
		}
		
	}

	/**
	 * @param rdRoadLink
	 * @throws Exception 
	 */
	private void checkRdRoadLink(RdRoadLink rdRoadLink) throws Exception {
		//新增RdRoadLink
		if(rdRoadLink.status().equals(ObjStatus.INSERT)){
			int linkPid = rdRoadLink.getLinkPid();

			StringBuilder sb = new StringBuilder();

			sb.append("SELECT 1 FROM RD_LINK RL");
			sb.append(" WHERE RL.U_RECORD <> 2");
			sb.append(" AND RL.KIND = 10");
			sb.append(" AND RL.LINK_PID =" + linkPid);
				
			String sql = sb.toString();
			log.info("RdRoadLink后检查GLM28063:" + sql);
				
			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);
				
			if(resultList.size()>0){
				String target = "[RD_ROAD," + rdRoadLink.getPid() + "]";
				this.setCheckResult("", target, 0);
			}
		}	
		
	}

}
