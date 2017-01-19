package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeForm;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/**
 * @ClassName GLM03086
 * @author Han Shaoming
 * @date 2017年1月19日 上午11:29:50
 * @Description TODO
 * 检查对象:道路形态为"跨线立交桥"的LINK.
 * 检查原则:具有"跨线立交桥"形态的LINK,两个端点的形态不能包含"桥",否则报LOG
 * 道路属性编辑	服务端后检查
 * node属性编辑	服务端后检查
 */
public class GLM03086 extends baseRule {

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
			//道路属性编辑
			else if (row instanceof RdLinkForm){
				RdLinkForm rdLinkForm = (RdLinkForm) row;
				this.checkRdLinkForm(rdLinkForm);
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
		boolean checkFlag = false;
		if(rdNodeForm.status().equals(ObjStatus.UPDATE)){
			Map<String, Object> changedFields = rdNodeForm.changedFields();
			if(!changedFields.isEmpty()){
				if(changedFields.containsKey("formOfWay")){
					int formOfWay = (int) changedFields.get("formOfWay");
					if(formOfWay == 12){
					checkFlag = true;
					}
				}
			}
		}else if (rdNodeForm.status().equals(ObjStatus.INSERT)){
			int formOfWay = rdNodeForm.getFormOfWay();
			if(formOfWay == 12){
				checkFlag = true;
			}
		}
		if(checkFlag){
			StringBuilder sb = new StringBuilder();
			
			sb.append("SELECT DISTINCT RNF.NODE_PID FROM RD_NODE_FORM RNF,RD_LINK L,RD_LINK_FORM F");
			sb.append(" WHERE RNF.NODE_PID = "+rdNodeForm.getNodePid());
			sb.append(" AND RNF.U_RECORD <> 2 AND L.LINK_PID = F.LINK_PID");
			sb.append(" AND (L.S_NODE_PID = RNF.NODE_PID OR L.E_NODE_PID = RNF.NODE_PID)");
			sb.append(" AND F.FORM_OF_WAY = 24 AND L.U_RECORD <> 2	AND F.U_RECORD <> 2");
			String sql = sb.toString();
			log.info("后检查GLM03086--sql:" + sql);
			
			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);
			
			if(!resultList.isEmpty()){
				String target = "[RD_NODE," + rdNodeForm.getNodePid() + "]";
				this.setCheckResult("", target, 0);
			}
		}
	}
	
	/**
	 * @author Han Shaoming
	 * @param rdLinkForm
	 * @throws Exception 
	 */
	private void checkRdLinkForm(RdLinkForm rdLinkForm) throws Exception {
		// TODO Auto-generated method stub
		boolean checkFlag = false;
		if(rdLinkForm.status().equals(ObjStatus.UPDATE)){
			Map<String, Object> changedFields = rdLinkForm.changedFields();
			if(!changedFields.isEmpty()){
				//道路属性编辑
				if(changedFields.containsKey("formOfWay")){
					int formOfWay = (int) changedFields.get("formOfWay");
					if(formOfWay == 24){
					checkFlag = true;
					}
				}
			}
		}else if (rdLinkForm.status().equals(ObjStatus.INSERT)){
			int formOfWay = rdLinkForm.getFormOfWay();
			if(formOfWay == 24){
				checkFlag = true;
			}
		}
		if(checkFlag){
			StringBuilder sb = new StringBuilder();
				
			sb.append("SELECT DISTINCT L.LINK_PID FROM RD_NODE_FORM RNF,RD_LINK L,RD_LINK_FORM F");
			sb.append(" WHERE L.LINK_PID ="+rdLinkForm.getLinkPid()+" AND RNF.FORM_OF_WAY = 12");
			sb.append(" AND RNF.U_RECORD <> 2 AND L.LINK_PID = F.LINK_PID");
			sb.append(" AND (L.S_NODE_PID = RNF.NODE_PID OR L.E_NODE_PID = RNF.NODE_PID)");
			sb.append(" AND L.U_RECORD <> 2	AND F.U_RECORD <> 2");
			
			String sql = sb.toString();
			log.info("RdLinkForm后检查GLM03086--sql:" + sql);
			
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
