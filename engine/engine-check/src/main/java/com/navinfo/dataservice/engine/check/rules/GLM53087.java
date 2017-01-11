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
 * @ClassName: GLM53087
 * @author songdongyan
 * @date 2017年1月10日
 * @Description:道路种别为"高速/城市高速"时,必须存在RTIC信息.
 * 屏蔽:道路形态为环岛和交叉口LINK的情况、供用信息为“未供用”的link
 * 删除车厂RTIC
 * 道路种别编辑
 */
public class GLM53087 extends baseRule{

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
			//删除RdLinkRtic
			if(obj instanceof RdLinkRtic ){
				RdLinkRtic rdLinkRtic=(RdLinkRtic) obj;
				checkRdLinkRtic(rdLinkRtic);
			}
			//道路种别编辑
			else if(obj instanceof RdLink ){
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
			if((kind==1)||(kind==2)){
				checkFlag = true;
			}
		}
		if(checkFlag){
			StringBuilder sb2 = new StringBuilder();

			sb2.append("SELECT DISTINCT L.LINK_PID FROM RD_LINK L");
			sb2.append(" WHERE L.U_RECORD <> 2");
			sb2.append(" AND NOT EXISTS (SELECT 1 FROM RD_LINK_LIMIT RLL");
			sb2.append(" WHERE RLL.LINK_PID = L.LINK_PID");
			sb2.append(" AND RLL.U_RECORD <> 2");
			sb2.append(" AND RLL.TYPE = 10)");
			sb2.append(" AND NOT EXISTS (SELECT 1 FROM RD_LINK_FORM LF");
			sb2.append(" WHERE LF.LINK_PID = L.LINK_PID");
			sb2.append(" AND LF.U_RECORD <> 2");
			sb2.append(" AND LF.FORM_OF_WAY IN (33, 50))");
			sb2.append(" AND L.LINK_PID =" + rdLink.getPid());
			sb2.append(" MINUS");
			sb2.append(" SELECT DISTINCT LR.LINK_PID FROM RD_LINK_RTIC LR WHERE LR.U_RECORD <> 2");
			sb2.append(" AND LR.LINK_PID = " + rdLink.getPid());
			String sql2 = sb2.toString();
			log.info("RdLink后检查GLM53087:" + sql2);

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
		if(rdLinkRtic.status().equals(ObjStatus.DELETE)){
			StringBuilder sb2 = new StringBuilder();

			sb2.append("SELECT DISTINCT L.LINK_PID FROM RD_LINK L");
			sb2.append(" WHERE L.KIND IN (1, 2)");
			sb2.append(" AND L.U_RECORD <> 2");
			sb2.append(" AND NOT EXISTS (SELECT 1 FROM RD_LINK_LIMIT RLL");
			sb2.append(" WHERE RLL.LINK_PID = L.LINK_PID");
			sb2.append(" AND RLL.U_RECORD <> 2");
			sb2.append(" AND RLL.TYPE = 10)");
			sb2.append(" AND NOT EXISTS (SELECT 1 FROM RD_LINK_FORM LF");
			sb2.append(" WHERE LF.LINK_PID = L.LINK_PID");
			sb2.append(" AND LF.U_RECORD <> 2");
			sb2.append(" AND LF.FORM_OF_WAY IN (33, 50))");
			sb2.append(" AND L.LINK_PID =" + rdLinkRtic.getLinkPid());
			sb2.append(" MINUS");
			sb2.append(" SELECT DISTINCT LR.LINK_PID FROM RD_LINK_RTIC LR WHERE LR.U_RECORD <> 2");
			sb2.append(" AND LR.LINK_PID = " + rdLinkRtic.getLinkPid());

			String sql2 = sb2.toString();
			log.info("RdLinkRtic后检查GLM53087:" + sql2);

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

