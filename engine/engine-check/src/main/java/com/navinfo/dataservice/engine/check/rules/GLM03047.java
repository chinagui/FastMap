package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeForm;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/**
 * @ClassName GLM03047
 * @author Han Shaoming
 * @date 2017年3月22日 下午6:14:53
 * @Description TODO
 * NODE种别和形态共存信息详见《通用需求定义》中GLM03047配置表，如不匹配，程序报错
 * node属性编辑	服务端后检查
 * node种别编辑	服务端后检查
 */
public class GLM03047 extends baseRule {

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		for(IRow row:checkCommand.getGlmList()){
			//node种别编辑
			if(row instanceof RdNode){
				RdNode rdNode = (RdNode)row;
				checkRdNode(rdNode);
			}
			//node属性编辑
			if (row instanceof RdNodeForm){
				RdNodeForm rdNodeForm = (RdNodeForm) row;
				this.checkRdNodeForm(rdNodeForm);
			}
		}
	}

	/**
	 * @author Han Shaoming
	 * @param rdNode
	 * @throws Exception 
	 */
	private void checkRdNode(RdNode rdNode) throws Exception {
		boolean checkFlag = false;
		if(rdNode.status().equals(ObjStatus.UPDATE)){
			Map<String, Object> changedFields = rdNode.changedFields();
			if(!changedFields.isEmpty()){
				if(changedFields.containsKey("kind")){
					checkFlag = true;
				}
			}
		}
		if(checkFlag){
			boolean check = this.check(rdNode.getPid());
			if(check){
				String target = "[RD_NODE," + rdNode.getPid() + "]";
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
		boolean checkFlag = false;
		if(rdNodeForm.status().equals(ObjStatus.UPDATE)){
			Map<String, Object> changedFields = rdNodeForm.changedFields();
			if(!changedFields.isEmpty()){
				if(changedFields.containsKey("formOfWay")){
					checkFlag = true;
				}
			}
		}else if (rdNodeForm.status().equals(ObjStatus.INSERT)){
			checkFlag = true;
		}
		if(checkFlag){
			boolean check = this.check(rdNodeForm.getNodePid());
			if(check){
				String target = "[RD_NODE," + rdNodeForm.getNodePid() + "]";
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
		       
		sb.append("SELECT R.NODE_PID FROM RD_NODE R, RD_NODE_FORM F");
		sb.append(" WHERE R.NODE_PID = "+nodePid);
		sb.append(" AND R.NODE_PID = F.NODE_PID AND R.KIND = 1");
		sb.append(" AND F.FORM_OF_WAY NOT IN (1,2,5,6,12,13,15,22,30)");
		sb.append(" AND R.U_RECORD <> 2 AND F.U_RECORD <> 2");
		sb.append(" UNION");
		sb.append(" SELECT R.NODE_PID FROM RD_NODE R, RD_NODE_FORM F");
		sb.append(" WHERE R.NODE_PID = "+nodePid);
		sb.append(" AND R.NODE_PID = F.NODE_PID AND R.KIND = 2");
		sb.append(" AND F.FORM_OF_WAY NOT IN (5,6,11)");
		sb.append(" AND R.U_RECORD <> 2 AND F.U_RECORD <> 2");
		sb.append(" UNION");
		sb.append(" SELECT R.NODE_PID FROM RD_NODE R, RD_NODE_FORM F");
		sb.append(" WHERE R.NODE_PID = "+nodePid);
		sb.append(" AND R.NODE_PID = F.NODE_PID AND R.KIND = 3");
		sb.append(" AND F.FORM_OF_WAY NOT IN (3,16)");
		sb.append(" AND R.U_RECORD <> 2 AND F.U_RECORD <> 2");
		String sql = sb.toString();
		log.info("后检查GLM03047--sql:" + sql);
		
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);
		
		if(!resultList.isEmpty()){
			flag = true;
		}
		return flag;
	}

}
