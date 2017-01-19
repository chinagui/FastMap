package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.directroute.RdDirectroute;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/**
 * @ClassName PERMIT_CHECK_IS_INTEGRITY_RDDIRECTROUTE
 * @author Han Shaoming
 * @date 2017年1月18日 上午9:25:43
 * @Description TODO
 * 顺行信息不完整，顺行信息应该是完整的线点线关系
 * 新增顺行	服务端前检查
 */
public class PERMIT_CHECK_IS_INTEGRITY_RDDIRECTROUTE extends baseRule {

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
		   
		sb.append("SELECT RL.LINK_PID FROM RD_LINK RL");
		sb.append(" WHERE RL.LINK_PID ="+rdDirectroute.getOutLinkPid());
		sb.append(" AND RL.E_NODE_PID <>"+rdDirectroute.getNodePid());
		sb.append(" AND RL.S_NODE_PID <>"+rdDirectroute.getNodePid());
		sb.append(" AND RL.U_RECORD <>2");
		String sql = sb.toString();
		log.info("新增顺行前检查PERMIT_CHECK_IS_INTEGRITY_RDDIRECTROUTE--sql:" + sql);
		
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);
		
		if(!resultList.isEmpty()){
			String target = "[RD_DIRECTROUTE," + rdDirectroute.getPid() + "]";
			this.setCheckResult("", target, 0);
		}
	}
}
