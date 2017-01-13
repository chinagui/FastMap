package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeForm;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/**
 * @ClassName GLM03059
 * @author Han Shaoming
 * @date 2016年12月29日 上午10:18:00
 * @Description TODO
 * 检查对象：障碍物属性Node点上，仅挂接有2根link。
 * 检查原则：该Node上挂接的2根link不能同时为高速或城高，否则报err
 * node属性编辑服务端后检查:RdNodeForm
 * Link种别编辑服务端后检查:RdLink
 */
public class GLM03059 extends baseRule {

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		for(IRow row:checkCommand.getGlmList()){
			//node属性编辑
			if (row instanceof RdNodeForm){
				RdNodeForm rdNodeForm = (RdNodeForm) row;
				this.checkRdNodeForm(rdNodeForm);
			}
			//Link种别编辑
			else if (row instanceof RdLink){
				RdLink rdLink = (RdLink) row;
				this.checkRdLink(rdLink);
			}
		}
	}

	/**
	 * @author Han Shaoming
	 * @param rdLink
	 * @throws Exception 
	 */
	private void checkRdLink(RdLink rdLink) throws Exception {
		// TODO Auto-generated method stub
		Map<String, Object> changedFields = rdLink.changedFields();
		if(changedFields != null && changedFields.containsKey("kind")){
			int kind = (int) changedFields.get("kind");
			if(kind == 1 || kind == 2){
				StringBuilder sb = new StringBuilder();
				
				sb.append("SELECT DISTINCT RS.LINK_PID FROM RD_NODE_FORM F, RD_LINK RS, RD_LINK RE");
				sb.append(" WHERE RS.LINK_PID = "+rdLink.getPid()+" AND F.FORM_OF_WAY = 15");
				sb.append(" AND (RS.S_NODE_PID = F.NODE_PID OR RS.E_NODE_PID = F.NODE_PID)");
				sb.append(" AND (RE.S_NODE_PID = F.NODE_PID OR RE.E_NODE_PID = F.NODE_PID)");
				sb.append(" AND RS.LINK_PID <> RE.LINK_PID AND F.U_RECORD <> 2 AND RS.U_RECORD <> 2");
				sb.append(" AND RE.U_RECORD <> 2 AND RE.KIND IN (1, 2)");
				String sql = sb.toString();
				log.info("RdLink后检查GLM03059--sql:" + sql);
				
				DatabaseOperator getObj = new DatabaseOperator();
				List<Object> resultList = new ArrayList<Object>();
				resultList = getObj.exeSelect(this.getConn(), sql);
				
				if(!resultList.isEmpty()){
					String target = "[RD_LINK," + rdLink.getPid() + "]";
					this.setCheckResult("", target, 0);
				}
			}
		}
	}

	/**
	 * @author Han Shaoming
	 * @param rdNodeForm
	 * @throws Exception 
	 */
	private void checkRdNodeForm(RdNodeForm rdNodeForm) throws Exception {
		// TODO Auto-generated method stub
		Map<String, Object> changedFields = rdNodeForm.changedFields();
		if(changedFields != null && changedFields.containsKey("formOfWay")){
			int formOfWay = (int) changedFields.get("formOfWay");
			if(formOfWay == 15){
				StringBuilder sb = new StringBuilder();
				
				sb.append("SELECT DISTINCT F.NODE_PID FROM RD_NODE_FORM F, RD_LINK RS, RD_LINK RE");
				sb.append(" WHERE F.NODE_PID = "+rdNodeForm.getNodePid());
				sb.append(" AND (RS.S_NODE_PID = F.NODE_PID OR RS.E_NODE_PID = F.NODE_PID)");
				sb.append(" AND (RE.S_NODE_PID = F.NODE_PID OR RE.E_NODE_PID = F.NODE_PID)");
				sb.append(" AND RS.LINK_PID <> RE.LINK_PID AND F.U_RECORD <> 2 AND RS.U_RECORD <> 2");
				sb.append(" AND RE.U_RECORD <> 2 AND RS.KIND IN (1, 2) AND RE.KIND IN (1, 2)");
				String sql = sb.toString();
				log.info("RdNode后检查GLM03059--sql:" + sql);
				
				DatabaseOperator getObj = new DatabaseOperator();
				List<Object> resultList = new ArrayList<Object>();
				resultList = getObj.exeSelect(this.getConn(), sql);
				
				if(!resultList.isEmpty()){
					String target = "[RD_NODE," + rdNodeForm.getNodePid() + "]";
					this.setCheckResult("", target, 0);
				}
			}
		}
	}
}
