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
 * @ClassName GLM02265
 * @author Han Shaoming
 * @date 2017年1月11日 下午3:21:57
 * @Description TODO
 * Z开头的专用道类型名制作在辅路形态上，报log。
 * 道路属性编辑	服务端后检查
 * 道路名称编辑	服务端后检查
 * 新增道路名	服务端后检查
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
			//道路名称编辑,新增道路名
			else if (row instanceof RdLinkName){
				RdLinkName rdLinkName = (RdLinkName) row;
				this.checkRdLinkName(rdLinkName);
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
		boolean checkFlag = false;
		if(rdLinkName.status().equals(ObjStatus.UPDATE)){
			Map<String, Object> changedFields = rdLinkName.changedFields();
			if(!changedFields.isEmpty()){
				// 道路名称编辑
				if (changedFields.containsKey("nameGroupid")) {
					checkFlag = true;
				}
			}
		}
		//新增道路名
		else if (rdLinkName.status().equals(ObjStatus.INSERT)){
			checkFlag = true;
		}
		if(checkFlag){
			boolean check = this.check(rdLinkName.getLinkPid());
			
			if(check){
				String target = "[RD_LINK," + rdLinkName.getLinkPid() + "]";
				this.setCheckResult("", target, 0);
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
					if(formOfWay == 34){
					checkFlag = true;
					}
				}
			}
		}else if (rdLinkForm.status().equals(ObjStatus.INSERT)){
			int formOfWay = rdLinkForm.getFormOfWay();
			if(formOfWay == 34){
				checkFlag = true;
			}
		}
		if(checkFlag){
			StringBuilder sb = new StringBuilder();
			
			sb.append("SELECT RLN.LINK_PID FROM RD_LINK_NAME RLN, RD_LINK_FORM LF, RD_NAME NN");
			sb.append(" WHERE RLN.LINK_PID ="+rdLinkForm.getLinkPid()+" AND RLN.LINK_PID = LF.LINK_PID");
			sb.append(" AND RLN.U_RECORD <>2 AND LF.U_RECORD <>2 AND NN.U_RECORD <>2");
			sb.append(" AND RLN.NAME_GROUPID = NN.NAME_GROUPID");
			sb.append(" AND NN.CODE_TYPE = 6 AND NN.LANG_CODE IN ('CHI', 'CHT')");
			sb.append(" AND (NN.NAME LIKE 'Ｚ%' OR NN.NAME LIKE 'ｚ%')");
			String sql = sb.toString();
			log.info("道路属性编辑后检查GLM02265--sql:" + sql);
			
			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);
			
			if(!resultList.isEmpty()){
				String target = "[RD_LINK," + rdLinkForm.getLinkPid() + "]";
				this.setCheckResult("", target, 0);
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
		sb.append(" WHERE RLN.LINK_PID ="+pid+" AND RLN.LINK_PID = LF.LINK_PID AND LF.FORM_OF_WAY = 34");
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
