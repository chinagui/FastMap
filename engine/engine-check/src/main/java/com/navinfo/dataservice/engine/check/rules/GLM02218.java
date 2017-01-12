package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkName;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/**
 * @ClassName GLM02218
 * @author Han Shaoming
 * @date 2017年1月11日 下午4:43:35
 * @Description TODO
 * 检查对象：道路名称不为空的link
 * 检查原则：名称类型只能存在“普通”、“立交桥（连接路）”、“立交桥（主路）”、“风景路线”、“虚拟名称”、“编号名称”、“点门牌”、“线门牌”、“隧道”，否则报LOG
 * 名称类型编辑	服务端后检查
 */
public class GLM02218 extends baseRule {

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		for(IRow row:checkCommand.getGlmList()){
			//名称类型编辑
			if (row instanceof RdLinkName){
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
		Map<String, Object> changedFields = rdLinkName.changedFields();
		if(!changedFields.isEmpty()){
			//名称类型编辑
			if(changedFields.containsKey("nameType")){
				int nameType = (int) changedFields.get("nameType");
				if(nameType != 0 && nameType != 1 && nameType != 2 && nameType != 3 && nameType != 5
						&& nameType != 6 && nameType != 8 && nameType != 14 && nameType != 15){
					boolean check = this.check(rdLinkName.getLinkPid());
					
					if(check){
						String target = "[RD_LINK," + rdLinkName.getLinkPid() + "]";
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
		 
		sb.append("SELECT RLN.LINK_PID FROM RD_LINK_NAME RLN");
		sb.append(" WHERE RLN.LINK_PID = "+pid);
		sb.append(" AND RLN.U_RECORD <> 2 ");
		String sql = sb.toString();
		log.info("后检查GLM02218--sql:" + sql);
		
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);
		
		if(!resultList.isEmpty()){
			flag = true;
		}
		return flag;
	}
}
