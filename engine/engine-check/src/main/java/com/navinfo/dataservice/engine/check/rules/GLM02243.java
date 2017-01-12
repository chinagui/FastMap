package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkName;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/**
 * @ClassName GLM02243
 * @author Han Shaoming
 * @date 2017年1月11日 下午5:48:02
 * @Description TODO
 * 检查对象：属性含SA/PA属性的link
 * 检查原则：Link上不能具有道路名称信息（立交桥名（主路）或立交桥名（连接路）类型的名称除外），否则报err
 * 道路属性编辑	服务端后检查
 * 名称类型编辑	服务端后检查
 */
public class GLM02243 extends baseRule {

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
		boolean checkFlag = false;
		if(rdLinkForm.status().equals(ObjStatus.UPDATE)){
			Map<String, Object> changedFields = rdLinkForm.changedFields();
			if(!changedFields.isEmpty()){
				//道路属性编辑
				if(changedFields.containsKey("formOfWay")){
					int formOfWay = (int) changedFields.get("formOfWay");
					if(formOfWay == 12 || formOfWay == 13){
					checkFlag = true;
					}
				}
			}
		}else if (rdLinkForm.status().equals(ObjStatus.INSERT)){
			int formOfWay = rdLinkForm.getFormOfWay();
			if(formOfWay == 12 || formOfWay == 13){
				checkFlag = true;
			}
		}
		if(checkFlag){
			StringBuilder sb = new StringBuilder();
			
			sb.append("SELECT F.LINK_PID FROM RD_LINK_FORM F, RD_LINK_NAME LN");
			sb.append(" WHERE F.LINK_PID ="+rdLinkForm.getLinkPid());
			sb.append(" AND F.LINK_PID = LN.LINK_PID");
			sb.append(" AND LN.NAME_TYPE NOT IN (1, 2)");
			sb.append(" AND F.U_RECORD <>2 AND LN.U_RECORD <>2");
			String sql = sb.toString();
			log.info("道路属性编辑后检查GLM02243--sql:" + sql);

			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);
			if (!resultList.isEmpty()) {
				String target = "[RD_LINK," + rdLinkForm.getLinkPid() + "]";
				this.setCheckResult("", target, 0);
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
				if(nameType != 1 && nameType != 2){
					StringBuilder sb = new StringBuilder();
					
					sb.append("SELECT F.LINK_PID FROM RD_LINK_FORM F, RD_LINK_NAME LN");
					sb.append(" WHERE F.LINK_PID ="+rdLinkName.getLinkPid());
					sb.append(" AND F.FORM_OF_WAY IN (12, 13) AND F.LINK_PID = LN.LINK_PID");
					sb.append(" AND F.U_RECORD <>2 AND LN.U_RECORD <>2");
					String sql = sb.toString();
					log.info("名称类型编辑后检查GLM02243--sql:" + sql);

					DatabaseOperator getObj = new DatabaseOperator();
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
