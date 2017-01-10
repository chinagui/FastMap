package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkSpeedlimit;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/**
 * @ClassName GLM56026
 * @author Han Shaoming
 * @date 2017年1月5日 下午4:09:17
 * @Description TODO
 * 同一根link上，线限速的“限速类型+限速条件”记录唯一，否则报log
 * 条件线限速限速条件	服务端后检查
 */
public class GLM56026 extends baseRule {

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		for(IRow row:checkCommand.getGlmList()){
			//条件线限速限速条件
			if (row instanceof RdLinkSpeedlimit){
				RdLinkSpeedlimit rdLinkSpeedlimit = (RdLinkSpeedlimit) row;
				this.checkRdLinkSpeedlimit(rdLinkSpeedlimit);
			}
		}
	}

	/**
	 * @author Han Shaoming
	 * @param rdSpeedlimit
	 * @throws Exception 
	 */
	private void checkRdLinkSpeedlimit(RdLinkSpeedlimit rdLinkSpeedlimit) throws Exception {
		// TODO Auto-generated method stub
		Map<String, Object> changedFields = rdLinkSpeedlimit.changedFields();
		if(!changedFields.isEmpty()){
			//条件线限速限速条件
			if(changedFields.containsKey("speedDependent")){
				boolean check = this.check(rdLinkSpeedlimit.getLinkPid());
				
				if(check){
					String target = "[RD_LINK," + rdLinkSpeedlimit.getLinkPid() + "]";
					this.setCheckResult("", target, 0);
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
		  
		sb.append("SELECT DISTINCT RLS.LINK_PID FROM RD_LINK_SPEEDLIMIT RLS");
		sb.append(" WHERE RLS.LINK_PID = "+pid);
		sb.append(" AND RLS.U_RECORD <>2 GROUP BY RLS.LINK_PID,");
		sb.append(" RLS.SPEED_TYPE,RLS.SPEED_DEPENDENT HAVING COUNT(1) > 1");
		String sql = sb.toString();
		log.info("后检查GLM56026--sql:" + sql);
		
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);
		
		if(!resultList.isEmpty()){
			flag = true;
		}
		return flag;
	}
}
