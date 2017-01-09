package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeForm;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/**
 * @ClassName GLM17008
 * @author Han Shaoming
 * @date 2017年1月9日 上午10:04:50
 * @Description TODO
 * 图廓点，点收费站，不能制作人行过道信息
 * node属性编辑	服务端后检查
 */
public class GLM17008 extends baseRule {

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
		}
	}

	/**
	 * @author Han Shaoming
	 * @param rdSpeedlimit
	 * @throws Exception 
	 */
	private void checkRdNodeForm(RdNodeForm rdNodeForm) throws Exception {
		// TODO Auto-generated method stub
		Map<String, Object> changedFields = rdNodeForm.changedFields();
		if(!changedFields.isEmpty()){
			//node属性编辑
			if(changedFields.containsKey("formOfWay")){
				int formOfWay = (int) changedFields.get("formOfWay");
				if(formOfWay == 2 || formOfWay == 4){
					boolean check = this.check(rdNodeForm.getNodePid());
					
					if(check){
						String target = "[RD_NODE," + rdNodeForm.getNodePid() + "]";
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
	private boolean check(int pid) throws Exception {
		// TODO Auto-generated method stub
		boolean flag = false;
		StringBuilder sb = new StringBuilder();
		 
		sb.append("SELECT RNF.NODE_PID FROM RD_NODE_FORM RNF");
		sb.append(" WHERE RNF.NODE_PID ="+pid+" AND RNF.U_RECORD <>2 ");
		sb.append(" AND RNF.FORM_OF_WAY IN(2,4) AND EXISTS(");
		sb.append(" SELECT 1 FROM RD_CROSSWALK_INFO RCI,RD_CROSSWALK_NODE RCN WHERE RCI.PID = RCN.PID");
		sb.append(" AND RCI.U_RECORD <>2 AND RCN.U_RECORD <>2");
		sb.append(" AND (RCI.NODE_PID = RNF.NODE_PID OR RCN.FIR_NODE_PID = RNF.NODE_PID");
		sb.append(" OR RCN.SEN_NODE_PID = RNF.NODE_PID))");
		String sql = sb.toString();
		log.info("后检查GLM17008--sql:" + sql);
		
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);
		
		if(!resultList.isEmpty()){
			flag = true;
		}
		return flag;
	}
}
