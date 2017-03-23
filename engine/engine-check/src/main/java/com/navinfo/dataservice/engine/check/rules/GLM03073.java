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
 * @ClassName GLM03073
 * @author Han Shaoming
 * @date 2017年3月23日 上午9:45:16
 * @Description TODO
 * 点的种别属性“平面交叉点（图廓点）”不能与其他点属性共存
 * node属性编辑	服务端后检查
 * node种别编辑	服务端后检查
 */
public class GLM03073 extends baseRule {

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
					int kind = (int) changedFields.get("kind");
					if(kind == 1){
						checkFlag = true;
					}
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
		       
		sb.append("SELECT N.NODE_PID FROM RD_NODE N, RD_NODE_FORM F");
		sb.append(" WHERE N.KIND = 1 AND N.NODE_PID = F.NODE_PID");
		sb.append(" AND N.NODE_PID="+nodePid);
		sb.append(" AND F.FORM_OF_WAY = 2");
		sb.append(" AND N.U_RECORD <> 2 AND F.U_RECORD <> 2");
		sb.append(" AND EXISTS (SELECT 1 FROM RD_NODE_FORM F1");
		sb.append(" WHERE F1.NODE_PID = F.NODE_PID");
		sb.append(" AND F1.FORM_OF_WAY <> 2 AND F1.U_RECORD <>2)");
		String sql = sb.toString();
		log.info("后检查GLM03073--sql:" + sql);
		
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);
		
		if(!resultList.isEmpty()){
			flag = true;
		}
		return flag;
	}
}
