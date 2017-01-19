package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: GLM32001
 * @author songdongyan
 * @date 2017年1月19日
 * @Description: 9级路、10级路、航线、人渡制作了详细车道信息
 * Link种别编辑后检查
 */
public class GLM32001 extends baseRule {

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
			//道路属性编辑
			if(obj instanceof RdLink ){
				RdLink RdLink=(RdLink) obj;
				checkRdLink(RdLink);
			}
		}
	}

	/**
	 * @param rdLinkForm
	 * @throws Exception 
	 */
	private void checkRdLink(RdLink rdLink) throws Exception {
		if(rdLink.changedFields().containsKey("kind")){
			int kind = Integer.parseInt(rdLink.changedFields().get("kind").toString());
			if((kind==9)||(kind==10)||(kind==11)||(kind==13)){
				
				StringBuilder sb2 = new StringBuilder();

				sb2.append("SELECT 1 FROM RD_LANE LA ");
				sb2.append(" WHERE LA.LINK_PID = " + rdLink.getPid());
				sb2.append(" AND LA.U_RECORD <> 2");
				sb2.append(" MINUS");
				sb2.append(" SELECT 1 FROM RD_LANE_CONNEXITY RLC");
				sb2.append(" WHERE RLC.IN_LINK_PID = " + rdLink.getPid());
				sb2.append(" AND RLC.U_RECORD <> 2");
				sb2.append(" MINUS");
				sb2.append(" SELECT 1 FROM RD_LANE_TOPOLOGY RLT");
				sb2.append(" WHERE RLT.OUT_LINK_PID = " + rdLink.getPid());
				sb2.append(" AND RLT.U_RECORD <> 2");
				sb2.append(" MINUS");
				sb2.append(" SELECT 1 FROM RD_LANE_VIA V");
				sb2.append(" WHERE V.LINK_PID = " + rdLink.getPid());
				sb2.append(" AND V.U_RECORD <> 2");
	
				String sql2 = sb2.toString();
				log.info("RdLink后检查GLM32001:" + sql2);
	
				DatabaseOperator getObj = new DatabaseOperator();
				List<Object> resultList = new ArrayList<Object>();
				resultList = getObj.exeSelect(this.getConn(), sql2);
	
				if(resultList.size()>0){
					String target = "[RD_LINK," + rdLink.getPid() + "]";
					this.setCheckResult("", target, 0);
				}
			}
		}
	}

}
