package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInter;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInterLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.road.RdRoad;
import com.navinfo.dataservice.dao.glm.model.rd.road.RdRoadLink;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: GLM28055
 * @author songdongyan
 * @date 2017年1月5日
 * @Description: 组成CRFR的道路必须有上下线分离属性，否则报Log
 * 新增CRFR:RdRoad
 * 修改CRFR:RdRoadLink
 * 新增CRFI:RdInter
 * 修改CRFI:RdInterLink
 * 道路属性编辑:RdLink
 */
public class GLM28055 extends baseRule{

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
			//RdRoad新增触发
			if (obj instanceof RdRoad){
				RdRoad RdRoad = (RdRoad)obj;
				checkRdRoad(RdRoad);
			}
			//RdRoadLink新增会触发
			else if (obj instanceof RdRoadLink){
				RdRoadLink RdRoadLink = (RdRoadLink)obj;
				checkRdRoadLink(RdRoadLink);
			}
//			//RdInter新增会触发
//			else if (obj instanceof RdInter){
//				RdInter RdInter = (RdInter)obj;
//				checkRdInter(RdInter);
//			}
//			//RdInterLink新增会触发
//			else if (obj instanceof RdInterLink){
//				RdInterLink RdInterLink = (RdInterLink)obj;
//				checkRdInterLink(RdInterLink);
//			}
			//RdLink属性修改会触发
			else if (obj instanceof RdLink){
				RdLink RdLink = (RdLink)obj;
				checkRdLink(RdLink);
			}
		}
	}

	/**
	 * @param rdLink
	 * @throws Exception 
	 */
	private void checkRdLink(RdLink rdLink) throws Exception {
		if(rdLink.changedFields().containsKey("multiDigitized")){
			int multiDigitized = Integer.parseInt(rdLink.changedFields().get("multiDigitized").toString());
			if(multiDigitized == 0){
				StringBuilder sb = new StringBuilder();
				
				sb.append("SELECT 1 FROM RD_ROAD_LINK RRL");
				sb.append(" WHERE RRL.U_RECORD <> 2");
				sb.append(" AND RRL.LINK_PID = " + rdLink.getPid());
				
				String sql = sb.toString();
				log.info("RdLink后检查GLM28055:" + sql);
				
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
	 * @param rdInterLink
	 * @throws Exception 
	 */
	private void checkRdInterLink(RdInterLink rdInterLink) throws Exception {
		if(rdInterLink.status().equals(ObjStatus.INSERT)){
			StringBuilder sb = new StringBuilder();

			sb.append("SELECT 1 FROM RD_LINK R");
			sb.append(" WHERE R.U_RECORD <> 2");
			sb.append(" AND R.MULTI_DIGITIZED = 0");
			sb.append(" AND R.LINK_PID = " + rdInterLink.getLinkPid());
			
			String sql = sb.toString();
			log.info("RdRoadLink后检查GLM28055:" + sql);
			
			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);
			
			if(resultList.size()>0){
				String target = "[RD_INTER," + rdInterLink.getPid() + "]";
				this.setCheckResult("", target, 0);
			}
		}
		
	}

	/**
	 * @param rdInter
	 * @throws Exception 
	 */
	private void checkRdInter(RdInter rdInter) throws Exception {
		if(rdInter.status().equals(ObjStatus.INSERT)){
			StringBuilder sb = new StringBuilder();

			sb.append("SELECT 1 FROM RD_INTER_LINK RIL,RD_LINK R");
			sb.append(" WHERE RIL.LINK_PID = R.LINK_PID");
			sb.append(" AND RIL.U_RECORD <> 2");
			sb.append(" AND R.U_RECORD <> 2");
			sb.append(" AND R.MULTI_DIGITIZED = 0");
			sb.append(" AND RIL.PID = " + rdInter.getPid());
			
			String sql = sb.toString();
			log.info("RdInter后检查GLM28055:" + sql);
			
			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);
			
			if(resultList.size()>0){
				String target = "[RD_INTER," + rdInter.getPid() + "]";
				this.setCheckResult("", target, 0);
			}
		}
		
	}

	/**
	 * @param rdRoadLink
	 * @throws Exception 
	 */
	private void checkRdRoadLink(RdRoadLink rdRoadLink) throws Exception {
		if(rdRoadLink.status().equals(ObjStatus.INSERT)){
			StringBuilder sb = new StringBuilder();

			sb.append("SELECT 1 FROM RD_LINK R");
			sb.append(" WHERE R.U_RECORD <> 2");
			sb.append(" AND R.MULTI_DIGITIZED = 0");
			sb.append(" AND R.LINK_PID = " + rdRoadLink.getLinkPid());
			
			String sql = sb.toString();
			log.info("RdRoadLink后检查GLM28055:" + sql);
			
			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);
			
			if(resultList.size()>0){
				String target = "[RD_ROAD," + rdRoadLink.getPid() + "]";
				this.setCheckResult("", target, 0);
			}
		}
		
	}

	/**
	 * @param rdRoad
	 * @throws Exception 
	 */
	private void checkRdRoad(RdRoad rdRoad) throws Exception {
		if(rdRoad.status().equals(ObjStatus.INSERT)){
			StringBuilder sb = new StringBuilder();

			sb.append("SELECT 1 FROM RD_ROAD_LINK RRL,RD_LINK R");
			sb.append(" WHERE RRL.LINK_PID = R.LINK_PID");
			sb.append(" AND RRL.U_RECORD <> 2");
			sb.append(" AND R.U_RECORD <> 2");
			sb.append(" AND R.MULTI_DIGITIZED = 0");
			sb.append(" AND RRL.PID = " + rdRoad.getPid());
			
			String sql = sb.toString();
			log.info("RdRoad后检查GLM28055:" + sql);
			
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
