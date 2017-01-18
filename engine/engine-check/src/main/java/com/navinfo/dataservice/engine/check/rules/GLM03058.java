package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeForm;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/**
 * @ClassName GLM03058
 * @author Han Shaoming
 * @date 2016年12月29日 上午10:17:53
 * @Description TODO
 * 检查对象：障碍物属性Node点
 * 检查原则：该Node上若挂接了10级路，报err
 * node属性编辑服务端后检查:RdNodeForm
 * Link种别编辑服务端后检查:RdLink
 * 分离节点	服务端后检查
 */
public class GLM03058 extends baseRule {

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
			//Link种别编辑,分离节点
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
		if(!changedFields.isEmpty()){
			//Link种别编辑
			if(changedFields.containsKey("kind")){
				int kind = (int) changedFields.get("kind");
				if(kind == 10){
					StringBuilder sb = new StringBuilder();
					
					sb.append("SELECT DISTINCT R.LINK_PID FROM RD_NODE_FORM F, RD_LINK R");
					sb.append(" WHERE R.LINK_PID = "+rdLink.getPid()+" AND F.FORM_OF_WAY = 15");
					sb.append(" AND F.U_RECORD <> 2 AND R.U_RECORD <> 2");
					sb.append(" AND (R.S_NODE_PID = F.NODE_PID OR R.E_NODE_PID = F.NODE_PID)");
					String sql = sb.toString();
					log.info("RdLink后检查GLM03058--sql:" + sql);
					
					DatabaseOperator getObj = new DatabaseOperator();
					List<Object> resultList = new ArrayList<Object>();
					resultList = getObj.exeSelect(this.getConn(), sql);
					
					if(!resultList.isEmpty()){
						String target = "[RD_LINK," + rdLink.getPid() + "]";
						this.setCheckResult("", target, 0);
					}
				}
			}
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
				   
				sb.append("SELECT DISTINCT R.LINK_PID FROM RD_NODE_FORM F, RD_LINK R");
				sb.append(" WHERE R.LINK_PID = "+rdLink.getPid());
				sb.append(" AND F.FORM_OF_WAY = 15 AND F.U_RECORD <> 2 AND R.U_RECORD <> 2");
				sb.append(" AND F.NODE_PID = "+nodePid);
				String sql = sb.toString();
				log.info("RdLink后检查GLM03058--sql:" + sql);
				
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
				
				sb.append("SELECT F.NODE_PID FROM RD_NODE_FORM F, RD_LINK R");
				sb.append(" WHERE F.NODE_PID = "+rdNodeForm.getNodePid()+" AND F.U_RECORD <> 2");
				sb.append(" AND R.U_RECORD <> 2 AND R.KIND = 10");
				sb.append(" AND (R.S_NODE_PID = F.NODE_PID OR R.E_NODE_PID = F.NODE_PID)");
				String sql = sb.toString();
				log.info("RdNode后检查GLM03058--sql:" + sql);
				
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
