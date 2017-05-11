package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/**
 * @ClassName GLM18015
 * @author Han Shaoming
 * @date 2017年1月19日 下午4:36:51
 * @Description TODO
 * 环岛不能作为语音引导的进入线和退出线
 * 道路属性编辑	服务端后检查
 */
public class GLM18015 extends baseRule {

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
		boolean checkFlag = false;
		if(rdLinkForm.status().equals(ObjStatus.UPDATE)){
			Map<String, Object> changedFields = rdLinkForm.changedFields();
			if(!changedFields.isEmpty()){
				//道路属性编辑
				if(changedFields.containsKey("formOfWay")){
					int formOfWay = (int) changedFields.get("formOfWay");
					if(formOfWay == 33){
					checkFlag = true;
					}
				}
			}
		}else if (rdLinkForm.status().equals(ObjStatus.INSERT)){
			int formOfWay = rdLinkForm.getFormOfWay();
			if(formOfWay == 33){
				checkFlag = true;
			}
		}
		if(checkFlag){
			StringBuilder sb = new StringBuilder();
			 
			sb.append("SELECT DISTINCT V.IN_LINK_PID LINK_PID FROM RD_VOICEGUIDE V");
			sb.append(" WHERE V.IN_LINK_PID = "+rdLinkForm.getLinkPid()+" AND V.U_RECORD <>2");
			sb.append(" UNION");
			sb.append(" SELECT DISTINCT D.OUT_LINK_PID LINK_PID FROM RD_VOICEGUIDE_DETAIL D");
			sb.append(" WHERE D.OUT_LINK_PID = "+rdLinkForm.getLinkPid()+" AND D.U_RECORD <>2");
			
			String sql = sb.toString();
			log.info("RdLinkForm后检查GLM18015--sql:" + sql);
			
			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);
			
			if(!resultList.isEmpty()){
				String target = "[RD_LINK," + rdLinkForm.getLinkPid() + "]";
				this.setCheckResult("", target, 0);
			}
		}
	}
}
