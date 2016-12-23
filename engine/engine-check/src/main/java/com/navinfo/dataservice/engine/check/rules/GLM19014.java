package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: GLM19014
 * @author songdongyan
 * @date 2016年12月22日
 * @Description: 路口车信上的进入线和退出线不能是交叉口内link
 * 道路属性编辑服务端前检查:RdLinkForm
 */
public class GLM19014 extends baseRule{

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#preCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for(IRow obj : checkCommand.getGlmList()){
			if (obj instanceof RdLinkForm){
				RdLinkForm rdLinkForm = (RdLinkForm)obj;
				checkRdLinkForm(rdLinkForm,checkCommand.getOperType());
			}
		}
		
	}

	/**
	 * @param rdLinkForm
	 * @param operType
	 * @throws Exception 
	 */
	private void checkRdLinkForm(RdLinkForm rdLinkForm, OperType operType) throws Exception {
		if(rdLinkForm.getFormOfWay() == 50){
			StringBuilder sb = new StringBuilder();
		
			sb.append("SELECT 1 FROM RD_LANE_CONNEXITY C, RD_LANE_TOPOLOGY T");
			sb.append(" WHERE C.PID = T.CONNEXITY_PID");
			sb.append(" AND T.RELATIONSHIP_TYPE = 1");
			sb.append(" AND C.IN_LINK_PID = " + rdLinkForm.getLinkPid());
			sb.append(" AND C.U_RECORD <> 2");
			sb.append(" AND T.U_RECORD <> 2");
			sb.append(" UNION ALL");
			sb.append(" SELECT 1 FROM RD_LANE_TOPOLOGY T");
			sb.append(" WHERE T.RELATIONSHIP_TYPE = 1");
			sb.append(" AND T.OUT_LINK_PID = " + rdLinkForm.getLinkPid());
			sb.append(" AND T.U_RECORD <> 2");

			String sql = sb.toString();
			log.info("RdLinkForm前检查GLM19014:" + sql);
			
			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);
			
			if(resultList.size()>0){
				this.setCheckResult("", "", 0);
			}
		}
		
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#postCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
