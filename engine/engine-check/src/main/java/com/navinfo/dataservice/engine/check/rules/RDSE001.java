package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.se.RdSe;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: RDSE001
 * @author songdongyan
 * @date 2017年3月22日
 * @Description: 环岛不能作为SE的进入线和退出线
 * 新增分岔口提示前检查
 * 修改分岔口提示前检查
 * 道路属性编辑后检查
 */
public class RDSE001 extends baseRule{

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#preCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for(IRow row:checkCommand.getGlmList()){
			//新增分岔口提示,修改分岔口提示
			if (row instanceof RdSe){
				RdSe rdSe = (RdSe) row;
				this.checkRdSe(rdSe);
			}
		}
	}

	/**
	 * @param rdSe
	 * @throws Exception 
	 */
	private void checkRdSe(RdSe rdSe) throws Exception {
		List<Integer> linkPidList = new ArrayList<Integer>();
		if(rdSe.status().equals(ObjStatus.INSERT)){
			linkPidList.add(rdSe.getInLinkPid());
			linkPidList.add(rdSe.getOutLinkPid());
		}
		else if(rdSe.status().equals(ObjStatus.UPDATE)){
			if(rdSe.changedFields().containsKey("inLinkPid")){
				linkPidList.add(Integer.parseInt(rdSe.changedFields().get("inLinkPid").toString()));
			}
			if(rdSe.changedFields().containsKey("outLinkPid")){
				linkPidList.add(Integer.parseInt(rdSe.changedFields().get("outLinkPid").toString()));
			}
		}
		
		if(!linkPidList.isEmpty()){
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT 1                   ");
			sb.append("  FROM RD_LINK_FORM F      ");
			sb.append(" WHERE F.LINK_PID IN (" + StringUtils.join(linkPidList.toArray(),",") + ")" );
			sb.append("   AND F.FORM_OF_WAY = 33  ");
			sb.append("   AND F.U_RECORD <> 2     ");
			
			String sql = sb.toString();
			log.info("RdSe RDSE001 sql:" + sql);

			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);
			
			if(!resultList.isEmpty()){
				this.setCheckResult("", "", 0);
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#postCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		
		for(IRow row:checkCommand.getGlmList()){
			//道路属性编辑
			if (row instanceof RdLinkForm){
				RdLinkForm rdLinkForm = (RdLinkForm) row;
				this.checkRdLinkForm(rdLinkForm);
			}
		}
		
	}

	/**
	 * @param rdLinkForm
	 * @throws Exception 
	 */
	private void checkRdLinkForm(RdLinkForm rdLinkForm) throws Exception {
		boolean ckFlg = false;
		if(rdLinkForm.status().equals(ObjStatus.INSERT)){
			if(rdLinkForm.getFormOfWay()==33){
				ckFlg = true;
			}
		}
		else if(rdLinkForm.status().equals(ObjStatus.UPDATE)){
			if(rdLinkForm.changedFields().containsKey("formOfWay")){
				int formOfWay = Integer.parseInt(rdLinkForm.changedFields().get("inLinkPid").toString());
				if(formOfWay==33){
					ckFlg = true;
				}
			}
		}
		
		if(ckFlg){
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT 1 ");
			sb.append("  FROM RD_SE S ");
			sb.append(" WHERE (S.IN_LINK_PID = " + rdLinkForm.getLinkPid() +" OR S.OUT_LINK_PID = " + rdLinkForm.getLinkPid() +") ");
			sb.append("   AND S.U_RECORD <> 2 ");
			
			String sql = sb.toString();
			log.info("RdSe RDSE001 sql:" + sql);

			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);
			
			if(!resultList.isEmpty()){
				String target = "[RD_LINK," + rdLinkForm.getLinkPid() + "]";
				this.setCheckResult("", target, 0);
			}
		}
		
	}

}
