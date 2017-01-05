package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInter;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInterLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: GLM28062
 * @author songdongyan
 * @date 2017年1月5日
 * @Description: RD_INTER_LINK表中LINK的种别不能为10级路，否则报LOG
 * 修改CRFI:RdInterLink
 * 新增CRFI:RdInter
 * Link种别编辑:RdLink
 */
public class GLM28062 extends baseRule{

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
			if (obj instanceof RdInterLink){
				RdInterLink rdInterLink = (RdInterLink)obj;
				checkRdInterLink(rdInterLink);
			}
			//RdInter新增会触发
			else if (obj instanceof RdInter){
				RdInter rdInter = (RdInter)obj;
				checkRdInter(rdInter);
			}
			//RdLink修改会触发
			else if (obj instanceof RdLink){
				RdLink rdLink = (RdLink)obj;
				checkRdLink(rdLink);
			}
		}
		
	}

	/**
	 * @param rdInterLink
	 * @throws Exception 
	 */
	private void checkRdLink(RdLink rdLink) throws Exception {
		//link种别编辑触发检查
		if(rdLink.changedFields().containsKey("kind")){
			int kind = Integer.parseInt(rdLink.changedFields().get("kind").toString());
			if(kind==10){
				StringBuilder sb = new StringBuilder();
				sb.append("SELECT 1 FROM RD_INTER_LINK RIL");
				sb.append(" WHERE RIL.U_RECORD <> 2");
				sb.append(" AND RIL.LINK_PID = " + rdLink.getPid());
				
				String sql = sb.toString();
				log.info("RdLink后检查GLM28062:" + sql);
				
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
	 * @param rdInter
	 * @throws Exception 
	 */
	private void checkRdInter(RdInter rdInter) throws Exception {
		// 新增RdInter
		if(rdInter.status().equals(ObjStatus.INSERT)){
			Set<Integer> linkPidSet = new HashSet<Integer>();
			for(IRow link:rdInter.getLinks()){
				RdInterLink rdInterLink = (RdInterLink)link;
				linkPidSet.add(rdInterLink.getLinkPid());
			}
			if(!linkPidSet.isEmpty()){
				StringBuilder sb = new StringBuilder();

				sb.append("SELECT 1 FROM RD_INTER_LINK RIL, RD_LINK RL");
				sb.append(" WHERE RIL.U_RECORD <> 2");
				sb.append(" AND RL.U_RECORD <> 2");
				sb.append(" AND RL.KIND = 10");
				sb.append(" AND RL.LINK_PID IN (" + StringUtils.join(linkPidSet.toArray(),",") + ")");
				
				String sql = sb.toString();
				log.info("RdInter后检查GLM28062:" + sql);
				
				DatabaseOperator getObj = new DatabaseOperator();
				List<Object> resultList = new ArrayList<Object>();
				resultList = getObj.exeSelect(this.getConn(), sql);
				
				if(resultList.size()>0){
					String target = "[RD_INTER," + rdInter.getPid() + "]";
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
		//新增RdInterLink
		if(rdInterLink.status().equals(ObjStatus.INSERT)){
			int linkPid = rdInterLink.getLinkPid();

			StringBuilder sb = new StringBuilder();

			sb.append("SELECT 1 FROM RD_LINK RL");
			sb.append(" WHERE RL.U_RECORD <> 2");
			sb.append(" AND RL.KIND = 10");
			sb.append(" AND RL.LINK_PID =" + linkPid);
				
			String sql = sb.toString();
			log.info("RdInterLink后检查GLM28062:" + sql);
				
			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);
				
			if(resultList.size()>0){
				String target = "[RD_INTER," + rdInterLink.getPid() + "]";
				this.setCheckResult("", target, 0);
			}
		}		
	}

}
