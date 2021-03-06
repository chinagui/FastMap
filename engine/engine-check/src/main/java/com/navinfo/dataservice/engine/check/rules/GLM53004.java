package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkRtic;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: GLM53004
 * @author songdongyan
 * @date 2017年1月9日
 * @Description: 单向道路不能做下行走向
 * 车厂RTIC图面编辑
 * 道路方向编辑
 */
public class GLM53004 extends baseRule{

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
		for(IRow obj:checkCommand.getGlmList()){
			//新增/修改RdLinkRtic
			if(obj instanceof RdLinkRtic ){
				RdLinkRtic rdLinkRtic=(RdLinkRtic) obj;
				checkRdLinkRtic(rdLinkRtic);
			}
			//道路方向编辑
			else if(obj instanceof RdLink){
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
		if(rdLink.changedFields().containsKey("direct")){
			int direct = Integer.parseInt(rdLink.changedFields().get("direct").toString());
			if((direct==2)||(direct==3)){
				//道路方向由双向变成单向
				if(rdLink.getDirect() == 1){
					checkFlag = true;	
				}
			}
		}
		if(checkFlag){
			StringBuilder sb2 = new StringBuilder();

			sb2.append("SELECT 1 FROM RD_LINK_RTIC R");
			sb2.append(" WHERE R.UPDOWN_FLAG = 1");
			sb2.append(" AND R.U_RECORD <> 2");
			sb2.append(" AND R.LINK_PID = " + rdLink.getPid());

			String sql2 = sb2.toString();
			log.info("RdLink后检查GLM53004:" + sql2);

			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql2);

			if(resultList.size()>0){
				String target = "[RD_LINK," + rdLink.getPid() + "]";
				this.setCheckResult("", target, 0);
			}
		}
		
		
	}

	/**
	 * @param rdLinkRtic
	 * @throws Exception 
	 */
	private void checkRdLinkRtic(RdLinkRtic rdLinkRtic) throws Exception {
		boolean checkFlag = false;
		
		if(rdLinkRtic.status().equals(ObjStatus.INSERT)){
			if(rdLinkRtic.getUpdownFlag() == 1){
				checkFlag = true;
			}
		}else if(rdLinkRtic.status().equals(ObjStatus.UPDATE)){
			if(rdLinkRtic.changedFields().containsKey("updownFlag")){
				int updownFlag = Integer.parseInt(rdLinkRtic.changedFields().get("updownFlag").toString());
				if(updownFlag==1){
					checkFlag = true;
				}
			}
		}
		if(checkFlag){
			StringBuilder sb2 = new StringBuilder();

			sb2.append("SELECT 1 FROM RD_LINK L");
			sb2.append(" WHERE L.DIRECT IN (2, 3)");
			sb2.append(" AND L.U_RECORD <> 2");
			sb2.append(" AND L.LINK_PID = " + rdLinkRtic.getLinkPid());

			String sql2 = sb2.toString();
			log.info("RdLinkRtic后检查GLM53004:" + sql2);

			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql2);

			if(resultList.size()>0){
				String target = "[RD_LINK," + rdLinkRtic.getLinkPid() + "]";
				this.setCheckResult("", target, 0);
			}
		}
		
	}

}
