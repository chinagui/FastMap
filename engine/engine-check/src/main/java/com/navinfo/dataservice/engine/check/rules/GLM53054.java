package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkIntRtic;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: GLM53054
 * @author songdongyan
 * @date 2017年1月10日
 * @Description: link为双方向时，如果上下行都制作了RTIC信息，则RTIC上下行等级应一致
 * 互联网RTIC上下行标识编辑
 */
public class GLM53054 extends baseRule{

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
			if(obj instanceof RdLinkIntRtic ){
				RdLinkIntRtic rdLinkRtic=(RdLinkIntRtic) obj;
				checkRdLinkIntRtic(rdLinkRtic);
			}
			
		}
		
	}

	/**
	 * @param rdLinkRtic
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

			sb2.append("SELECT 1 FROM RD_LINK_INT_RTIC R1, RD_LINK_INT_RTIC R2");
			sb2.append(" WHERE R1.LINK_PID = R2.LINK_PID");
			sb2.append(" AND R1.UPDOWN_FLAG < R2.UPDOWN_FLAG");
			sb2.append(" AND R1.RANK <> R2.RANK");
			sb2.append(" AND R1.U_RECORD <> 2");
			sb2.append(" AND R2.U_RECORD <> 2");
			sb2.append(" AND R1.LINK_PID = " + rdLinkRtic.getLinkPid());

			String sql2 = sb2.toString();
			log.info("RdLinkRtic后检查GLM53006:" + sql2);

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

