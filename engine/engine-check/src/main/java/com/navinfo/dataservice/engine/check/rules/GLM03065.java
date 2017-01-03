package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeForm;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/**
 * @ClassName GLM03065
 * @author Han Shaoming
 * @date 2016年12月29日 下午3:02:07
 * @Description TODO
 * 隧道属性Node点挂接隧道link数不能大于2
 * node属性编辑,移动端点 服务端后检查:
 * 道路属性编辑,分离节点 服务端后检查:
 */
public class GLM03065 extends baseRule {

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		for(IRow row:checkCommand.getGlmList()){
			//移动端点
			if (row instanceof RdNode){
				RdNode rdNode = (RdNode) row;
				this.checkRdNode(rdNode);
			}
			//node属性编辑
			else if (row instanceof RdNodeForm){
				RdNodeForm rdNodeForm = (RdNodeForm) row;
				this.checkRdNodeForm(rdNodeForm);
			}
			//分离节点
			else if (row instanceof RdLink){
				RdLink rdLink = (RdLink) row;
				this.checkRdLink(rdLink);
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
	 * @param rdNode
	 * @throws Exception 
	 */
	private void checkRdNode(RdNode rdNode) throws Exception {
		// TODO Auto-generated method stub
		boolean check = this.check(rdNode.getPid());

		if(check){
			String target = "[RD_NODE," + rdNode.getPid() + "]";
			this.setCheckResult("", target, 0);
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
		int formOfWay = 1;
		if(changedFields.containsKey("formOfWay")){
			formOfWay = (int) changedFields.get("formOfWay");
		}
		if(formOfWay == 13){
			boolean check = this.check(rdNodeForm.getNodePid());

			if(check){
				String target = "[RD_NODE," + rdNodeForm.getNodePid() + "]";
				this.setCheckResult("", target, 0);
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
		Set<Integer> nodePids = new HashSet<Integer>();
		//分离节点
		Map<String, Object> changedFields = rdLink.changedFields();
		if(!changedFields.isEmpty()){
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
		}
		for (Integer nodePid : nodePids) {
			boolean check = this.check(nodePid);

			if(check){
				String target = "[RD_LINK," + rdLink.getPid() + "]";
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
		Map<String, Object> changedFields = rdLinkForm.changedFields();
		int formOfWay = 1;
		if(changedFields.containsKey("formOfWay")){
			formOfWay = (int) changedFields.get("formOfWay");
		}
		if(formOfWay == 31){
			
			RdLinkSelector linkSelector = new RdLinkSelector(this.getConn());
			RdLink rdLink = (RdLink) linkSelector.loadByIdOnlyRdLink(rdLinkForm.getLinkPid(), false);
			if(rdLink != null){
				Set<Integer> nodePids = new HashSet<Integer>();
				nodePids.add(rdLink.getsNodePid());
				nodePids.add(rdLink.geteNodePid());
				for (Integer nodePid : nodePids) {
					boolean check = this.check(nodePid);

					if(check){
						String target = "[RD_LINK," + rdLink.getPid() + "]";
						this.setCheckResult("", target, 0);
					}
				}
			}
		}
	}

	/**
	 * @author Han Shaoming
	 * @param rdNodeForm
	 * @throws Exception 
	 */
	private boolean check(int nodePid) throws Exception {
		// TODO Auto-generated method stub
		boolean flag = false;
		StringBuilder sb = new StringBuilder();
		     
		sb.append("SELECT N.NODE_PID FROM RD_NODE N, RD_NODE_FORM F, RD_LINK R ,RD_LINK_FORM RF");
		sb.append(" WHERE N.NODE_PID = F.NODE_PID AND N.NODE_PID = "+nodePid);
		sb.append(" AND R.LINK_PID = RF.LINK_PID AND F.FORM_OF_WAY = 13 AND RF.FORM_OF_WAY = 31");
		sb.append(" AND N.U_RECORD <> 2 AND F.U_RECORD <> 2 AND R.U_RECORD <> 2 AND RF.U_RECORD <> 2");
		sb.append(" AND (R.S_NODE_PID = N.NODE_PID OR R.E_NODE_PID = N.NODE_PID)");
		sb.append(" GROUP BY N.NODE_PID HAVING COUNT(1) > 2");
		String sql = sb.toString();
		log.info("后检查GLM03065--sql:" + sql);
		
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);
		
		if(!resultList.isEmpty()){
			flag = true;
		}
		return flag;
	}

}
