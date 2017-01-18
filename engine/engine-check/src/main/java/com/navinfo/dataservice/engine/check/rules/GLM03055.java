package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeForm;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/**
 * @ClassName GLM03055
 * @author Han Shaoming
 * @date 2017年1月18日 下午3:46:34
 * @Description TODO
 * 具有障碍物属性的点所挂接的link上具有步行街属性，报err；（挂接的多条link具有步行街属性，只报一次）
 * 道路属性编辑	服务端后检查
 * 分离节点	服务端后检查
 * node属性编辑	服务端后检查
 */
public class GLM03055 extends baseRule {

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		for(IRow row:checkCommand.getGlmList()){
			//道路属性编辑
			if(row instanceof RdLinkForm){
				RdLinkForm rdLinkForm = (RdLinkForm)row;
				checkRdLinkForm(rdLinkForm);
			}
			//node属性编辑
			if (row instanceof RdNodeForm){
				RdNodeForm rdNodeForm = (RdNodeForm) row;
				this.checkRdNodeForm(rdNodeForm);
			}
			//分离节点
			else if (row instanceof RdLink){
				RdLink rdLink = (RdLink) row;
				this.checkRdLink(rdLink);
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
		//道路属性编辑,触发检查
		boolean checkFlag = false;
		if(rdLinkForm.status().equals(ObjStatus.UPDATE)){
			Map<String, Object> changedFields = rdLinkForm.changedFields();
			if(!changedFields.isEmpty()){
				//道路属性编辑
				if(changedFields.containsKey("formOfWay")){
					int formOfWay = (int) changedFields.get("formOfWay");
					if(formOfWay == 20){
					checkFlag = true;
					}
				}
			}
		}else if (rdLinkForm.status().equals(ObjStatus.INSERT)){
			int formOfWay = rdLinkForm.getFormOfWay();
			if(formOfWay == 20){
				checkFlag = true;
			}
		}
		if(checkFlag){
			StringBuilder sb = new StringBuilder();
			  
			sb.append("SELECT DISTINCT RL.LINK_PID FROM RD_LINK RL,RD_LINK_FORM RLF,RD_NODE_FORM RNF");
			sb.append(" WHERE RL.LINK_PID ="+rdLinkForm.getLinkPid());
			sb.append(" AND RL.LINK_PID = RLF.LINK_PID AND RNF.FORM_OF_WAY = 15");
			sb.append(" AND (RNF.NODE_PID = RL.S_NODE_PID OR RNF.NODE_PID = RL.E_NODE_PID)");
			sb.append(" AND RL.U_RECORD <> 2 AND RLF.U_RECORD <> 2 AND RNF.U_RECORD <> 2");
			
			String sql = sb.toString();
			log.info("RdLinkForm后检查GLM03055--sql:" + sql);
			
			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);
			
			if(!resultList.isEmpty()){
				String target = "[RD_LINK," + rdLinkForm.getLinkPid() + "]";
				this.setCheckResult("", target, 0);
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
				  
				sb.append("SELECT DISTINCT RL.LINK_PID FROM RD_LINK RL,RD_LINK_FORM RLF,RD_NODE_FORM RNF");
				sb.append(" WHERE RNF.NODE_PID ="+rdNodeForm.getNodePid());
				sb.append(" AND RL.LINK_PID = RLF.LINK_PID AND RLF.FORM_OF_WAY = 20");
				sb.append(" AND (RNF.NODE_PID = RL.S_NODE_PID OR RNF.NODE_PID = RL.E_NODE_PID)");
				sb.append(" AND RL.U_RECORD <> 2 AND RLF.U_RECORD <> 2 AND RNF.U_RECORD <> 2");
				String sql = sb.toString();
				log.info("RdNode后检查GLM03055--sql:" + sql);
				
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

	/**
	 * @author Han Shaoming
	 * @param rdLink
	 * @throws Exception 
	 */
	private void checkRdLink(RdLink rdLink) throws Exception {
		// TODO Auto-generated method stub
		Map<String, Object> changedFields = rdLink.changedFields();
		if(!changedFields.isEmpty()){
			//分离节点
			Set<Integer> nodePids = new HashSet<Integer>();
			Integer sNodePid = null;
			Integer eNodePid = null;
			if(changedFields.containsKey("sNodePid")){
				sNodePid = (Integer) changedFields.get("sNodePid");
				if(sNodePid != null){
					nodePids.add(sNodePid);
				}
			}
			if(changedFields.containsKey("eNodePid")){
				eNodePid = (Integer) changedFields.get("eNodePid");
				if(eNodePid != null){
					nodePids.add(eNodePid);
				}
			}
			for (Integer nodePid : nodePids) {
				StringBuilder sb = new StringBuilder();
				
				sb.append("SELECT DISTINCT RL.LINK_PID FROM RD_LINK RL,RD_LINK_FORM RLF,RD_NODE_FORM RNF");
				sb.append(" WHERE RNF.NODE_PID ="+nodePid);
				sb.append(" AND RL.LINK_PID = RLF.LINK_PID AND RLF.FORM_OF_WAY = 20");
				sb.append(" AND (RNF.NODE_PID = RL.S_NODE_PID OR RNF.NODE_PID = RL.E_NODE_PID)");
				sb.append(" AND RL.U_RECORD <> 2 AND RLF.U_RECORD <> 2 AND RNF.U_RECORD <> 2");
				String sql = sb.toString();
				log.info("RdLink后检查GLM03055--sql:" + sql);
				
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
}
