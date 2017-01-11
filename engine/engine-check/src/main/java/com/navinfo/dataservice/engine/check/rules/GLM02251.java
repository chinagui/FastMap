package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkName;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResult;

/**
 * @ClassName GLM02251
 * @author Han Shaoming
 * @date 2017年1月11日 下午5:41:31
 * @Description TODO
 * 制作虚拟名称的link无区域内道路形态的报log
 * 道路属性编辑	服务端后检查
 * 名称类型编辑	服务端后检查
 */
public class GLM02251 extends baseRule {

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
			//名称类型编辑
			else if (row instanceof RdLinkName){
				RdLinkName rdLinkName = (RdLinkName) row;
				this.checkRdLinkName(rdLinkName);
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
				if(formOfWay != 52){
					StringBuilder sb = new StringBuilder();
					
					sb.append("SELECT RLN.LINK_PID FROM RD_LINK_NAME RLN");
					sb.append(" WHERE RLN.LINK_PID = "+rdLinkForm.getLinkPid());
					sb.append(" AND RLN.U_RECORD <>2 AND RLN.NAME_TYPE = 6");
					sb.append(" AND NOT EXISTS(SELECT 1 FROM RD_LINK_FORM RF");
					sb.append(" WHERE RLN.LINK_PID = RF.LINK_PID");
					sb.append(" AND RF.U_RECORD <>2 AND RF.FORM_OF_WAY = 52)");
					String sql = sb.toString();
					log.info("道路属性编辑后检查GLM02251--sql:" + sql);

					DatabaseOperatorResult getObj = new DatabaseOperatorResult();
					List<Object> resultList = new ArrayList<Object>();
					resultList = getObj.exeSelect(this.getConn(), sql);
					if (!resultList.isEmpty()) {
						String target = "[RD_LINK," + rdLinkForm.getLinkPid() + "]";
						this.setCheckResult("", target, 0);
					}
				}
			}
		}
	}

	/**
	 * @author Han Shaoming
	 * @param rdLinkName
	 * @throws Exception 
	 */
	private void checkRdLinkName(RdLinkName rdLinkName) throws Exception {
		// TODO Auto-generated method stub
		Map<String, Object> changedFields = rdLinkName.changedFields();
		if(!changedFields.isEmpty()){
			//名称类型编辑
			if(changedFields.containsKey("nameType")){
				int nameType = (int) changedFields.get("nameType");
				if(nameType == 6 ){
					StringBuilder sb = new StringBuilder();
					
					sb.append("SELECT RLN.LINK_PID FROM RD_LINK_NAME RLN");
					sb.append(" WHERE RLN.LINK_PID = "+rdLinkName.getLinkPid());
					sb.append(" AND RLN.U_RECORD <>2 ");
					sb.append(" AND NOT EXISTS(SELECT 1 FROM RD_LINK_FORM RF");
					sb.append(" WHERE RLN.LINK_PID = RF.LINK_PID");
					sb.append(" AND RF.U_RECORD <>2 AND RF.FORM_OF_WAY = 52)");
					String sql = sb.toString();
					log.info("名称类型编辑后检查GLM02251--sql:" + sql);

					DatabaseOperatorResult getObj = new DatabaseOperatorResult();
					List<Object> resultList = new ArrayList<Object>();
					resultList = getObj.exeSelect(this.getConn(), sql);
					if (!resultList.isEmpty()) {
						String target = "[RD_LINK," + rdLinkName.getLinkPid() + "]";
						this.setCheckResult("", target, 0);
					}
				}
			}
		}
	}
}
