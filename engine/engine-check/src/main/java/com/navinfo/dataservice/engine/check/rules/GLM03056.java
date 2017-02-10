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
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeForm;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/**
 * @ClassName GLM03056
 * @author Han Shaoming
 * @date 2016年12月29日 下午1:21:26
 * @Description TODO
 * 具有障碍物属性的点只能挂接2条link，否则报err
 * node属性编辑 服务端后检查:
 * 新增LINK,分离节点,平滑修形 服务端后检查:
 */
public class GLM03056 extends baseRule {

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
			//新增LINK,分离节点,平滑修形
			else if (row instanceof RdLink){
				RdLink rdLink = (RdLink) row;
				this.checkRdLink(rdLink);
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
					if(formOfWay == 15){
						checkFlag = true;
					}
				}
			}
		}else if (rdNodeForm.status().equals(ObjStatus.INSERT)){
			int formOfWay = rdNodeForm.getFormOfWay();
			if(formOfWay == 15){
				checkFlag = true;
			}
		}
		if(checkFlag){
			StringBuilder sb = new StringBuilder();
			 
			sb.append("SELECT DISTINCT N.NODE_PID FROM RD_NODE N, RD_LINK R");
			sb.append(" WHERE N.NODE_PID = "+rdNodeForm.getNodePid());
			sb.append(" AND N.U_RECORD <> 2 AND R.U_RECORD <> 2");
			sb.append(" AND (R.S_NODE_PID = N.NODE_PID OR R.E_NODE_PID = N.NODE_PID)");
			sb.append(" GROUP BY N.NODE_PID HAVING COUNT(1) <> 2");
			String sql = sb.toString();
			log.info("后检查GLM03056--sql:" + sql);
			
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
	 * @param rdLink
	 * @throws Exception 
	 */
	private void checkRdLink(RdLink rdLink) throws Exception {
		// TODO Auto-generated method stub
		Map<String, Object> changedFields = rdLink.changedFields();
		Set<Integer> nodePids = new HashSet<Integer>();
		//新增LINK
		if(ObjStatus.INSERT.equals(rdLink.status())){
			nodePids.add(rdLink.getsNodePid());
			nodePids.add(rdLink.geteNodePid());
		}
		//分离节点,平滑修形
		if(ObjStatus.UPDATE.equals(rdLink.status())){
			Integer sNodePid = null;
			Integer eNodePid = null;
			if(changedFields.containsKey("sNodePid")){
				nodePids.add(rdLink.getsNodePid());
				sNodePid = (Integer) changedFields.get("sNodePid");
				if(sNodePid != null){
					nodePids.add(sNodePid);
				}
			}
			if(changedFields.containsKey("eNodePid")){
				nodePids.add(rdLink.geteNodePid());
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
	 * @param rdNodeForm
	 * @throws Exception 
	 */
	private boolean check(int nodePid) throws Exception {
		// TODO Auto-generated method stub
		boolean flag = false;
		StringBuilder sb = new StringBuilder();
		    
		sb.append("SELECT N.NODE_PID FROM RD_NODE N, RD_NODE_FORM F, RD_LINK R");
		sb.append(" WHERE N.NODE_PID = F.NODE_PID AND N.NODE_PID = "+nodePid);
		sb.append(" AND F.FORM_OF_WAY = 15 AND N.U_RECORD <> 2 AND F.U_RECORD <> 2 AND R.U_RECORD <> 2");
		sb.append(" AND (R.S_NODE_PID = N.NODE_PID OR R.E_NODE_PID = N.NODE_PID)");
		sb.append(" GROUP BY N.NODE_PID HAVING COUNT(1) <> 2");
		String sql = sb.toString();
		log.info("后检查GLM03056--sql:" + sql);
		
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);
		
		if(!resultList.isEmpty()){
			flag = true;
		}
		return flag;
	}

}
