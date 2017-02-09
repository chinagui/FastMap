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
 * @ClassName GLM18012
 * @author Han Shaoming
 * @date 2017年1月19日 下午4:59:30
 * @Description TODO
 * 路口语音引导的进入线或退出线均不能为交叉口link
 * 道路属性编辑	服务端后检查
 */
public class GLM18012 extends baseRule {

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
					if(formOfWay == 50){
					checkFlag = true;
					}
				}
			}
		}else if (rdLinkForm.status().equals(ObjStatus.INSERT)){
			int formOfWay = rdLinkForm.getFormOfWay();
			if(formOfWay == 50){
				checkFlag = true;
			}
		}
		if(checkFlag){
			StringBuilder sb = new StringBuilder();
			
			sb.append("SELECT V.IN_LINK_PID LINK_PID FROM RD_VOICEGUIDE V, RD_VOICEGUIDE_DETAIL D");
			sb.append(" WHERE V.IN_LINK_PID = "+rdLinkForm.getLinkPid()+"  AND V.PID = D.VOICEGUIDE_PID");
			sb.append(" AND V.U_RECORD != 2 AND D.U_RECORD != 2 ");
			//sb.append(" AND D.RELATIONSHIP_TYPE = 1");
			sb.append(" UNION");
			sb.append(" SELECT D.OUT_LINK_PID LINK_PID FROM RD_VOICEGUIDE_DETAIL D");
			sb.append(" WHERE D.OUT_LINK_PID = "+rdLinkForm.getLinkPid());
			sb.append(" AND D.U_RECORD != 2 AND D.RELATIONSHIP_TYPE = 1");
			
			String sql = sb.toString();
			log.info("RdLinkForm后检查GLM18012--sql:" + sql);
			
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
