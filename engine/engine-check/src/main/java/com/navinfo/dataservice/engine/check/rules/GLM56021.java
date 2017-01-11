package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.speedlimit.RdSpeedlimit;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/**
 * @ClassName GLM56021
 * @author Han Shaoming
 * @date 2017年1月5日 上午11:19:30
 * @Description TODO
 * 当“内业作业状态”≠2时，做如下检查：点限速的限速类型不为“普通”时,限速来源必须是“现场标牌”，否则报log
 * 点限速限速来源	服务端后检查
 * 点限速限速类型	服务端后检查
 */
public class GLM56021 extends baseRule {

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		for(IRow row:checkCommand.getGlmList()){
			//点限速限速来源,点限速限速类型
			if (row instanceof RdSpeedlimit){
				RdSpeedlimit rdSpeedlimit = (RdSpeedlimit) row;
				this.checkRdSpeedlimit(rdSpeedlimit);
			}
		}
	}
	
	/**
	 * @author Han Shaoming
	 * @param rdSpeedlimit
	 * @throws Exception 
	 */
	private void checkRdSpeedlimit(RdSpeedlimit rdSpeedlimit) throws Exception {
		// TODO Auto-generated method stub
		Map<String, Object> changedFields = rdSpeedlimit.changedFields();
		if(!changedFields.isEmpty()){
			//点限速限速来源
			if(changedFields.containsKey("limitSrc")){
				int limitSrc = (int) changedFields.get("limitSrc");
				if(limitSrc != 1){
					boolean check = this.check(rdSpeedlimit.getPid());
					
					if(check){
						String target = "[RD_SPEEDLIMIT," + rdSpeedlimit.getPid() + "]";
						this.setCheckResult("", target, 0);
					}
				}
			}
			//点限速限速类型
			if(changedFields.containsKey("speedType")){
				int speedType = (int) changedFields.get("speedType");
				if(speedType != 0){
					boolean check = this.check(rdSpeedlimit.getPid());
					
					if(check){
						String target = "[RD_SPEEDLIMIT," + rdSpeedlimit.getPid() + "]";
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
		   
		sb.append("SELECT RS.PID FROM RD_SPEEDLIMIT RS");
		sb.append(" WHERE RS.PID= "+pid);
		sb.append(" AND RS.U_RECORD <>2 AND RS.REC_STATUS_IN <> 2");
		sb.append(" AND RS.SPEED_TYPE <> 0 AND RS.LIMIT_SRC <> 1");
		String sql = sb.toString();
		log.info("后检查GLM56021--sql:" + sql);
		
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);
		
		if(!resultList.isEmpty()){
			flag = true;
		}
		return flag;
	}
}
