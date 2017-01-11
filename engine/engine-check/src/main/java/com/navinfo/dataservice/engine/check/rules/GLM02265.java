package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/**
 * @ClassName GLM02265
 * @author Han Shaoming
 * @date 2017年1月11日 下午3:21:57
 * @Description TODO
 * Z开头的专用道类型名制作在辅路形态上，报log。
 * 道路属性编辑	服务端后检查
 */
public class GLM02265 extends baseRule {

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		for(IRow row:checkCommand.getGlmList()){
			//道路属性编辑
			if (row instanceof RdLinkForm){
				RdLinkForm rdLinkForm = (RdLinkForm) row;
				this.checkRdLinkForm(rdLinkForm);
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
		if(!changedFields.isEmpty()){
			//道路属性编辑
			if(changedFields.containsKey("formOfWay")){
				int formOfWay = (int) changedFields.get("formOfWay");
				if(formOfWay == 34){
					boolean check = this.check(rdLinkForm.getLinkPid());
					
					if(check){
						String target = "[RD_LINK," + rdLinkForm.getLinkPid() + "]";
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
		
		sb.append("SELECT RLN.LINK_PID FROM RD_LINK_NAME RLN, RD_LINK_FORM LF, RD_NAME NN");
		sb.append(" WHERE RLN.LINK_PID ="+pid+" AND RLN.LINK_PID = LF.LINK_PID ");
		sb.append(" AND RLN.U_RECORD <>2 AND LF.U_RECORD <>2 AND NN.U_RECORD <>2");
		sb.append(" AND RLN.NAME_GROUPID = NN.NAME_GROUPID");
		sb.append(" AND NN.CODE_TYPE = 6 AND NN.LANG_CODE IN ('CHI', 'CHT')");
		sb.append(" AND (NN.NAME LIKE 'Ｚ%' OR NN.NAME LIKE 'ｚ%')");
		String sql = sb.toString();
		log.info("后检查GLM02265--sql:" + sql);
		
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);
		
		if(!resultList.isEmpty()){
			flag = true;
		}
		return flag;
	}
}
