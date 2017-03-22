package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeForm;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: GLM04009
 * @author songdongyan
 * @date 2017年3月22日
 * @Description: GLM04009.java
 */
public class GLM04009 extends baseRule{

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
		for (IRow obj : checkCommand.getGlmList()) {
			// 大门类型编辑RdGate
			if (obj instanceof RdNodeForm) {
				RdNodeForm rdNodeForm = (RdNodeForm) obj;
				if(!rdNodeForm.status().equals(ObjStatus.DELETE)){
					int formOfWay = rdNodeForm.getFormOfWay();
					if(rdNodeForm.changedFields().containsKey("formOfWay")){
						formOfWay = Integer.parseInt(rdNodeForm.changedFields().get("formOfWay").toString());
						if(formOfWay==15){
							check(rdNodeForm.getNodePid());
						}
					}
				}
			}
		}
		
	}


	/**
	 * @param nodePid
	 * @throws Exception 
	 */
	private void check(int nodePid) throws Exception {
		StringBuilder sb = new StringBuilder();

		sb.append("SELECT 1                        ");
		sb.append("  FROM RD_NODE_FORM F, RD_GATE G");
		sb.append(" WHERE G.NODE_PID = F.NODE_PID  ");
		sb.append("   AND F.FORM_OF_WAY = 15       ");
		sb.append("   AND G.U_RECORD <> 2          ");
		sb.append("   AND F.U_RECORD <> 2          ");
		sb.append("   AND F.NODE_PID = " + nodePid);

		String sql = sb.toString();
		log.info("RdNode GLM04009 sql:" + sql);
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if (!resultList.isEmpty()) {
			this.setCheckResult("", "[RD_NODE," + nodePid + "]", 0);
		}
	}
}
