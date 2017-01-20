package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.se.RdSe;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/**
 * @ClassName GLM06007
 * @author Han Shaoming
 * @date 2017年1月9日 下午3:03:49
 * @Description TODO
 * 如果分岔口点,不允许有两条及以上的进入线。屏蔽条件：如果多组分岔口信息，进入线不同退出线相同的，不报log
 * 新增分岔口提示	服务端前检查
 */
public class GLM06007 extends baseRule {

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for(IRow row:checkCommand.getGlmList()){
			//新增分岔口提示
			if (row instanceof RdSe){
				RdSe rdSe = (RdSe) row;
				this.checkRdSe(rdSe);
			}
		}
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}

	/**
	 * @author Han Shaoming
	 * @param rdSe
	 * @throws Exception 
	 */
	private void checkRdSe(RdSe rdSe) throws Exception {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();
	    
		sb.append("SELECT SE.NODE_PID FROM RD_SE SE");
		sb.append(" WHERE SE.NODE_PID = "+rdSe.getNodePid());
		sb.append(" AND SE.IN_LINK_PID != "+rdSe.getInLinkPid());
		sb.append(" AND SE.OUT_LINK_PID != "+rdSe.getOutLinkPid());
		sb.append(" AND SE.U_RECORD <>2");
		String sql = sb.toString();
		log.info("新增分岔口提示前检查GLM06007--sql:" + sql);
		
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);
		
		if(!resultList.isEmpty()){
			String target = "[RD_SE," + rdSe.getPid() + "]";
			this.setCheckResult("", target, 0);
		}
	}
	
}
