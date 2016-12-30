package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.warninginfo.RdWarninginfo;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: GLM09019
 * @author songdongyan
 * @date 2016年12月29日
 * @Description: 检查原则：警示信息不能够制作在9级辅路上。否则报LOG
 * 道路属性编辑后检查 RdLinkForm(修改或者新增)
 * link种别编辑后检查 RdLink修改
 * 新增警示信息后检查 RdWarningInfo
 * 道路属性编辑前检查 RdLinkForm(修改或者新增)
 * link种别编辑前检查 RdLink修改
 */
public class GLM09019 extends baseRule{

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#preCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for(IRow obj:checkCommand.getGlmList()){
			//LINK属性修改
			if(obj instanceof RdLinkForm ){
				RdLinkForm rdLinkForm=(RdLinkForm) obj;
				checkRdRdLinkForm(rdLinkForm);
			}
			//link种别修改
			else if(obj instanceof RdLink){
				RdLink rdLink=(RdLink) obj;
				checkRdLink(rdLink,checkCommand);
			}
		}
		
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#postCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		for(IRow obj:checkCommand.getGlmList()){
			//LINK属性修改
			if(obj instanceof RdLinkForm ){
				RdLinkForm rdLinkForm=(RdLinkForm) obj;
				checkRdRdLinkForm(rdLinkForm);
			}
			//link种别修改
			else if(obj instanceof RdLink){
				RdLink rdLink=(RdLink) obj;
				checkRdLink(rdLink,checkCommand);
			}
			//新增警示信息
			else if(obj instanceof RdWarninginfo){
				RdWarninginfo rdWarninginfo=(RdWarninginfo) obj;
				checkRdWarningInfo(rdWarninginfo);
			}
		}
	}

	/**
	 * @param rdWarninginfo
	 * @throws Exception 
	 */
	private void checkRdWarningInfo(RdWarninginfo rdWarninginfo) throws Exception {
		//新增警示信息
		StringBuilder sb = new StringBuilder();

		sb.append("SELECT 1 FROM RD_LINK R,RD_LINK_FORM F ");
		sb.append(" WHERE R.LINK_PID = F.LINK_PID");
		sb.append(" AND F.FORM_OF_WAY = 34");
		sb.append(" AND R.KIND = 9");
		sb.append(" AND F.U_RECORD <> 2");
		sb.append(" AND R.U_RECORD <> 2");
		sb.append(" AND R.LINK_PID = " + rdWarninginfo.getLinkPid());

		String sql = sb.toString();
		log.info("RdWarninginfo后检查GLM09019:" + sql);

		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if(resultList.size()>0){
			String target = "[RD_WARNINGINFO," + rdWarninginfo.getPid() + "]";
			this.setCheckResult("", target, 0);
		}
	}

	/**
	 * @param rdLink
	 * @param checkCommand
	 * @throws Exception 
	 */
	private void checkRdLink(RdLink rdLink, CheckCommand checkCommand) throws Exception {
		//道路属性编辑
		if(rdLink.changedFields().containsKey("kind")){
			int kind = Integer.parseInt(rdLink.changedFields().get("kind").toString());
			if(kind == 9){
				StringBuilder sb = new StringBuilder();

				sb.append("SELECT 1 FROM RD_LINK_FORM F,RD_WARNINGINFO RW ");
				sb.append(" WHERE F.LINK_PID = RW.LINK_PID");
				sb.append(" AND F.FORM_OF_WAY = 34");
				sb.append(" AND F.U_RECORD <> 2");
				sb.append(" AND RW.U_RECORD <> 2");
				sb.append(" AND F.LINK_PID = " + rdLink.getPid());

				String sql = sb.toString();
				log.info("RdLink前后检查GLM09019:" + sql);

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
	 * @param rdLinkForm
	 * @throws Exception 
	 */
	private void checkRdRdLinkForm(RdLinkForm rdLinkForm) throws Exception {
		//道路属性编辑
		if(rdLinkForm.changedFields().containsKey("formOfWay")){
			int formOfWay = Integer.parseInt(rdLinkForm.changedFields().get("formOfWay").toString());
			if(formOfWay == 34){
				StringBuilder sb = new StringBuilder();

				sb.append("SELECT 1 FROM RD_LINK R,RD_WARNINGINFO RW ");
				sb.append(" WHERE R.LINK_PID = RW.LINK_PID");
				sb.append(" AND R.KIND = 9");
				sb.append(" AND R.U_RECORD <> 2");
				sb.append(" AND RW.U_RECORD <> 2");
				sb.append(" AND R.LINK_PID = " + rdLinkForm.getLinkPid());

				String sql = sb.toString();
				log.info("RdLinkForm前后检查GLM09019:" + sql);

				DatabaseOperator getObj = new DatabaseOperator();
				List<Object> resultList = new ArrayList<Object>();
				resultList = getObj.exeSelect(this.getConn(), sql);

				if(resultList.size()>0){
					String target = "[RD_LINK," + rdLinkForm.getLinkPid() + "]";
					this.setCheckResult("", target, 0);
				}
			}
		}
		
	}

}
