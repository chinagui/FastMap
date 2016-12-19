package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.directroute.RdDirectroute;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/**
 * 
 * @ClassName GLM14007
 * @author Han Shaoming
 * @date 2016年12月14日 下午3:54:09
 * @Description TODO
 * 路口顺行的进入线或退出线不能为交叉口link
 * 新增顺行服务端后检查:RdDirectroute
 * 道路属性编辑服务端前检查:RdLinkForm
 */
public class GLM14007 extends baseRule {

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		for(IRow row:checkCommand.getGlmList()){
			//道路属性编辑
			if(row instanceof RdLink){
				RdLink rdLink = (RdLink)row;
				checkRdLink(rdLink);
			}else if(row instanceof RdLinkForm){
				RdLinkForm rdLinkForm = (RdLinkForm)row;
				checkRdLinkForm(rdLinkForm);
			}
		}
	}




	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		for(IRow row:checkCommand.getGlmList()){
			//新增顺行
			if(row instanceof RdDirectroute){
				RdDirectroute rdDirectroute = (RdDirectroute)row;
				checkRdDirectroute(rdDirectroute);
			}
		}

	}
	
	/**
	 * @author Han Shaoming
	 * @param rdLink
	 * @throws Exception 
	 */
	private void checkRdLink(RdLink rdLink) throws Exception {
		//道路属性编辑,触发检查
		StringBuilder sb = new StringBuilder();

		sb.append("WITH RLF AS (SELECT RF.LINK_PID FROM RD_LINK_FORM RF WHERE");
		sb.append(" RF.LINK_PID = "+rdLink.getPid()+" AND RF.FORM_OF_WAY = 50 AND RF.U_RECORD <> 2)");
		sb.append(" SELECT DR.PID FROM RD_DIRECTROUTE DR,RLF WHERE DR.IN_LINK_PID = RLF.LINK_PID AND DR.U_RECORD <> 2");
		sb.append(" UNION");
		sb.append(" SELECT DR.PID FROM RD_DIRECTROUTE DR,RLF WHERE DR.OUT_LINK_PID = RLF.LINK_PID AND DR.U_RECORD <> 2");
		
		String sql = sb.toString();
		log.info("RdLink前检查GLM14007--sql:" + sql);
		
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if(!resultList.isEmpty()){
			String target = "[RD_LINK," + rdLink.getPid() + "]";
			this.setCheckResult("", target, 0);
		}
	}
	/**
	 * @author Han Shaoming
	 * @param rdLinkForm
	 * @throws Exception 
	 */
	private void checkRdLinkForm(RdLinkForm rdLinkForm) throws Exception {
		//道路属性编辑,触发检查
		if(rdLinkForm.getFormOfWay()==50){
			StringBuilder sb = new StringBuilder();

			sb.append("SELECT DR.PID FROM RD_DIRECTROUTE DR");
			sb.append(" WHERE DR.IN_LINK_PID = " + rdLinkForm.getLinkPid());
			sb.append(" AND DR.U_RECORD <> 2 UNION");
			sb.append(" SELECT DR.PID FROM RD_DIRECTROUTE DR");
			sb.append(" WHERE DR.OUT_LINK_PID = " + rdLinkForm.getLinkPid());
			sb.append(" AND DR.U_RECORD <> 2");
			
			String sql = sb.toString();
			log.info("RdLinkForm前检查GLM14007--sql:" + sql);
			
			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);

			if(!resultList.isEmpty()){
				String target = "[RD_LINK," + rdLinkForm.getLinkPid() + "]";
				this.setCheckResult("", target, 0);
			}
		}
	}

	/**
	 * 
	 * @author Han Shaoming
	 * @param rdDirectroute
	 * @throws Exception 
	 */
	private void checkRdDirectroute(RdDirectroute rdDirectroute) throws Exception {
		//新增顺行,触发检查
		StringBuilder sb = new StringBuilder();

		sb.append("SELECT DISTINCT RF.LINK_PID FROM RD_LINK_FORM RF WHERE");
		sb.append(" (RF.LINK_PID = "+rdDirectroute.getInLinkPid()+" OR RF.LINK_PID = "+rdDirectroute.getOutLinkPid()+")");
		sb.append(" AND RF.FORM_OF_WAY = 50 AND RF.U_RECORD <> 2");
		String sql = sb.toString();
		log.info("RdDirectroute后检查GLM14007--sql:" + sql);
		
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if(!resultList.isEmpty()){
			String target = "[RD_DIRECTROUTE," + rdDirectroute.getPid() + "]";
			this.setCheckResult("", target, 0);
		}
		
	}

}
