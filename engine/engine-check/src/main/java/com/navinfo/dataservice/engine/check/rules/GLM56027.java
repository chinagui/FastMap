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
 * @ClassName GLM56027
 * @author Han Shaoming
 * @date 2017年1月5日 下午4:30:53
 * @Description TODO
 * 线限速的限速类型为“特定条件”时，限速条件不能为“无”；当限速条件不为“无”时，限速类型必须为“特定条件”，否则报log
 * 条件线限速限速条件	服务端后检查
 */
public class GLM56027 extends baseRule {

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
				int speedDependent = (int) changedFields.get("speedDependent");
				if(speedDependent == 0){
					boolean check = this.checkZero(rdLinkSpeedlimit.getLinkPid());
					
					if(check){
						String target = "[RD_LINK," + rdLinkSpeedlimit.getLinkPid() + "]";
						this.setCheckResult("", target, 0,"线限速的限速类型为“特定条件”时，限速条件不能为“无”");
					}
				}
				else if(speedDependent != 0){
					boolean check = this.check(rdLinkSpeedlimit.getLinkPid());
					
					if(check){
						String target = "[RD_LINK," + rdLinkSpeedlimit.getLinkPid() + "]";
						this.setCheckResult("", target, 0,"当限速条件不为“无”时，限速类型必须为“特定条件”");
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
	private boolean checkZero(int pid) throws Exception {
		// TODO Auto-generated method stub
		boolean flag = false;
		StringBuilder sb = new StringBuilder();
		  
		sb.append("SELECT DISTINCT RLS.LINK_PID FROM RD_LINK_SPEEDLIMIT RLS");
		sb.append(" WHERE RLS.LINK_PID = "+pid);
		sb.append(" AND RLS.U_RECORD <>2");
		sb.append(" AND RLS.SPEED_TYPE = 3 AND RLS.SPEED_DEPENDENT = 0");
		String sql = sb.toString();
		log.info("后检查GLM56027--sql:" + sql);
		
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);
		
		if(!resultList.isEmpty()){
			flag = true;
		}
		return flag;
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
		sb.append(" AND RLS.U_RECORD <>2");
		sb.append(" AND RLS.SPEED_TYPE <> 3 AND RLS.SPEED_DEPENDENT <> 0");
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
