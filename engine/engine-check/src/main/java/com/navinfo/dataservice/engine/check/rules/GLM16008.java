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
 * @ClassName GLM16008
 * @author Han Shaoming
 * @date 2017年1月6日 下午2:31:32
 * @Description TODO
 * 图廓点不能设置虚拟连接
 * node属性编辑	服务端后检查
 */
public class GLM16008 extends baseRule {

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
				if(formOfWay == 2){
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
		
		sb.append("SELECT DISTINCT RNF.NODE_PID FROM RD_NODE_FORM RNF");
		sb.append(" WHERE RNF.NODE_PID = "+pid);
		sb.append(" AND RNF.U_RECORD <> 2 AND RNF.FORM_OF_WAY = 2");
		sb.append(" AND EXISTS (SELECT 1 FROM RD_VIRCONNECT_TRANSIT RT");
		sb.append(" WHERE RT.U_RECORD <> 2 AND (RT.FIR_NODE_PID = RNF.NODE_PID OR RT.SEN_NODE_PID = RNF.NODE_PID))");
		String sql = sb.toString();
		log.info("后检查GLM16008--sql:" + sql);
		
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);
		
		if(!resultList.isEmpty()){
			flag = true;
		}
		return flag;
	}
}
