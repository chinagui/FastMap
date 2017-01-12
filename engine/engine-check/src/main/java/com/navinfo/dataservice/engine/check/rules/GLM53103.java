package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkIntRtic;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkRtic;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: GLM53013
 * @author songdongyan
 * @date 2017年1月12日
 * @Description: 双向道路的RTIC上下行标识一侧为上行，另一侧为下行
 * 车厂RTIC图面编辑
 * 互联网RTIC图面编辑
 * 车厂RTIC上下行标识编辑
 * 互联网RTIC上下行标识编辑
 */
public class GLM53103 extends baseRule{

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
			else if(obj instanceof RdLinkIntRtic ){
				RdLinkIntRtic rdLinkIntRtic=(RdLinkIntRtic) obj;
				checkRdLinkIntRtic(rdLinkIntRtic);
			}
		}
		
	}

	/**
	 * @param rdLinkIntRtic
	 * @throws Exception 
	 */
	private void checkRdLinkIntRtic(RdLinkIntRtic rdLinkRtic) throws Exception {
		boolean checkFlag = false;
		
		if(rdLinkRtic.status().equals(ObjStatus.INSERT)){
			checkFlag = true;
		}else if(rdLinkRtic.status().equals(ObjStatus.UPDATE)){
			if(rdLinkRtic.changedFields().containsKey("updownFlag")){
				checkFlag = true;
			}
		}
		if(checkFlag){
			StringBuilder sb2 = new StringBuilder();

			sb2.append("SELECT 1 FROM RD_LINK_RTIC R1,RD_LINK_INT_RTIC R2,RD_LINK L");
			sb2.append(" WHERE L.LINK_PID = R1.LINK_PID");
			sb2.append(" AND R1.LINK_PID = R2.LINK_PID");
			sb2.append(" AND L.U_RECORD <> 2");
			sb2.append(" AND R1.U_RECORD <> 2");
			sb2.append(" AND R2.U_RECORD <> 2");
			sb2.append(" AND L.DIRECT = 1");
			sb2.append(" AND R1.UPDOWN_FLAG = R2.UPDOWN_FLAG");
			sb2.append(" AND R1.ROW_ID <> R2.ROW_ID");
			sb2.append(" AND L.LINK_PID = " + rdLinkRtic.getLinkPid());

			String sql2 = sb2.toString();
			log.info("RdLinkRtic后检查GLM53103:" + sql2);

			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql2);

			if(resultList.size()>0){
				String target = "[RD_LINK," + rdLinkRtic.getLinkPid() + "]";
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
			checkFlag = true;
		}else if(rdLinkRtic.status().equals(ObjStatus.UPDATE)){
			if(rdLinkRtic.changedFields().containsKey("updownFlag")){
				checkFlag = true;
			}
		}
		if(checkFlag){
			StringBuilder sb2 = new StringBuilder();

			sb2.append("SELECT 1 FROM RD_LINK_RTIC R1,RD_LINK_RTIC R2,RD_LINK L");
			sb2.append(" WHERE L.LINK_PID = R1.LINK_PID");
			sb2.append(" AND R1.LINK_PID = R2.LINK_PID");
			sb2.append(" AND L.U_RECORD <> 2");
			sb2.append(" AND R1.U_RECORD <> 2");
			sb2.append(" AND R2.U_RECORD <> 2");
			sb2.append(" AND L.DIRECT = 1");
			sb2.append(" AND R1.UPDOWN_FLAG = R2.UPDOWN_FLAG");
			sb2.append(" AND R1.ROW_ID <> R2.ROW_ID");
			sb2.append(" AND L.LINK_PID = " + rdLinkRtic.getLinkPid());


			String sql2 = sb2.toString();
			log.info("RdLinkRtic后检查GLM53103:" + sql2);

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
