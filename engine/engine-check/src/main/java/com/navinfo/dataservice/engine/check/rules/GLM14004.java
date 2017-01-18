package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.directroute.RdDirectroute;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/**
 * @ClassName GLM14004
 * @author Han Shaoming
 * @date 2017年1月16日 上午9:42:43
 * @Description TODO
 * 检查对象：顺行信息
 * 检查原则：
 * 1.检查对象进入线的终点必须至少挂接3条Link，否则报log；
 * 2.检查对象退出线的起点必须至少挂接3条Link，否则报log；
 * 新增顺行	服务端前检查
 */
public class GLM14004 extends baseRule {

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		for(IRow row:checkCommand.getGlmList()){
			//新增顺行
			if (row instanceof RdDirectroute){
				RdDirectroute rdDirectroute = (RdDirectroute) row;
				this.checkRdDirectroute(rdDirectroute);
			}
		}
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}

	/**
	 * @author Han Shaoming
	 * @param rdDirectroute
	 * @throws Exception 
	 */
	private void checkRdDirectroute(RdDirectroute rdDirectroute) throws Exception {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();
				
		sb.append("WITH T AS(SELECT RL.E_NODE_PID NODE_PID FROM RD_LINK RL");
		sb.append(" WHERE RL.LINK_PID ="+rdDirectroute.getInLinkPid()+" AND RL.U_RECORD <>2");
		sb.append(" UNION SELECT RL.S_NODE_PID NODE_PID FROM RD_LINK RL");
		sb.append(" WHERE RL.LINK_PID ="+rdDirectroute.getOutLinkPid()+" AND RL.U_RECORD <>2)");
		sb.append(" SELECT T.NODE_PID FROM RD_LINK RL,T WHERE RL.U_RECORD <>2");
		sb.append(" AND (RL.S_NODE_PID = T.NODE_PID OR RL.E_NODE_PID = T.NODE_PID)");
		sb.append(" GROUP BY T.NODE_PID HAVING COUNT(1)<3");
		String sql = sb.toString();
		log.info("新增顺行前检查GLM14004--sql:" + sql);
		
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);
		
		if(!resultList.isEmpty()){
			String target = "[RD_DIRECTROUTE," + rdDirectroute.getPid() + "]";
			this.setCheckResult("", target, 0);
		}
	}
}
