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
 * @ClassName GLM15008
 * @author Han Shaoming
 * @date 2017年1月5日 下午3:27:37
 * @Description TODO
 * 当内业作业状态≠2时，如果时间段有值，则限速类型一定为特定条件3且限速条件为10（时间限制）或限速条件为6（学校），否则报log 
 * 点限速限速条件	服务端后检查
 * 点限速限速类型	服务端后检查
 */
public class GLM15008 extends baseRule {

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		for(IRow row:checkCommand.getGlmList()){
			//点限速限速条件,点限速限速类型
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
			//点限速限速条件
			if(changedFields.containsKey("speedDependent")){
				int speedDependent = (int) changedFields.get("speedDependent");
				if(speedDependent != 6 && speedDependent != 10){
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
				if(speedType != 3){
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
		sb.append(" AND RS.U_RECORD <>2 AND RS.REC_STATUS_IN <> 2 AND RS.TIME_DOMAIN IS NOT NULL");
		sb.append(" AND (RS.SPEED_TYPE <> 3 OR RS.SPEED_DEPENDENT NOT IN (6,10))");
		String sql = sb.toString();
		log.info("后检查GLM15008--sql:" + sql);
		
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);
		
		if(!resultList.isEmpty()){
			flag = true;
		}
		return flag;
	}
}
