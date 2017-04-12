package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.directroute.RdDirectroute;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/**
 * 
 * @ClassName GLM14007
 * @author Han Shaoming
 * @date 2016年12月14日 下午3:54:09
 * @Description TODO
 * 路口顺行的进入线或退出线不能为交叉口link
 * 新增顺行服务端后检查:RdDirectroute
 * 道路属性编辑服务端前检查:RdLinkForm--删掉该触发时机
 * 道路属性编辑服务端前后:RdLinkForm
 */
public class GLM14007 extends baseRule {
	protected Logger log = Logger.getLogger(this.getClass());
	
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
//		for(IRow row:checkCommand.getGlmList()){
//			//道路属性编辑
//			if(row instanceof RdLinkForm){
//				RdLinkForm rdLinkForm = (RdLinkForm)row;
//				checkRdLinkForm(rdLinkForm);
//			}
//		}
	}




	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		for(IRow row:checkCommand.getGlmList()){
			//新增顺行
			if(row instanceof RdDirectroute){
				RdDirectroute rdDirectroute = (RdDirectroute)row;
				checkRdDirectroute(rdDirectroute);
			}
			//道路属性编辑
			else if(row instanceof RdLinkForm){
				RdLinkForm rdLinkForm = (RdLinkForm)row;
				checkRdLinkForm(rdLinkForm);
			}
		}

	}
	
	/**
	 * @author Han Shaoming
	 * @param rdLink
	 * @throws Exception 
	 */
	/*private void checkRdLink(RdLink rdLink) throws Exception {
		//道路属性编辑,触发检查
		//获取子表
		List<IRow> forms = rdLink.getForms();
		for (IRow iRow : forms) {
			RdLinkForm rdLinkForm = (RdLinkForm) iRow;
			this.checkRdLinkForm(rdLinkForm);
		}
	}*/
	/**
	 * @author Han Shaoming
	 * @param rdLinkForm
	 * @throws Exception 
	 */
	private void checkRdLinkForm(RdLinkForm rdLinkForm) throws Exception {
		//道路属性编辑,触发检查
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
			
			sb.append("SELECT DR.PID FROM RD_DIRECTROUTE DR");
			sb.append(" WHERE DR.IN_LINK_PID = " + rdLinkForm.getLinkPid());
			sb.append(" AND DR.RELATIONSHIP_TYPE = 1 ");
			sb.append(" AND DR.U_RECORD <> 2 ");
			sb.append(" UNION");
			sb.append(" SELECT DR.PID FROM RD_DIRECTROUTE DR");
			sb.append(" WHERE DR.OUT_LINK_PID = " + rdLinkForm.getLinkPid());
			sb.append(" AND DR.RELATIONSHIP_TYPE = 1 ");
			sb.append(" AND DR.U_RECORD <> 2");
			
			String sql = sb.toString();
			log.info("RdLinkForm前检查GLM14007--sql:" + sql);
			
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
	 * 
	 * @author Han Shaoming
	 * @param rdDirectroute
	 * @throws Exception 
	 */
	private void checkRdDirectroute(RdDirectroute rdDirectroute) throws Exception {
		//新增线线关系顺行,不触发检查
		if(rdDirectroute.getRelationshipType() == 2){
			return;
		}
		StringBuilder sb = new StringBuilder();

		sb.append("SELECT DISTINCT RF.LINK_PID FROM RD_LINK_FORM RF WHERE");
		sb.append(" (RF.LINK_PID = "+rdDirectroute.getInLinkPid()+" OR RF.LINK_PID = "+rdDirectroute.getOutLinkPid()+")");
		sb.append(" AND RF.FORM_OF_WAY = 50 AND RF.U_RECORD <> 2");
		String sql = sb.toString();
		log.info("RdDirectroute后检查GLM14007--sql:" + sql);
		
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if(!resultList.isEmpty()){
			String target = "[RD_DIRECTROUTE," + rdDirectroute.getPid() + "]";
			this.setCheckResult("", target, 0);
		}
		
	}

}
