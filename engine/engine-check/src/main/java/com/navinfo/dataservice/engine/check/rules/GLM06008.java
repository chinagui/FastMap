package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.directroute.RdDirectroute;
import com.navinfo.dataservice.dao.glm.model.rd.se.RdSe;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/**
 * @ClassName GLM06008
 * @author Han Shaoming
 * @date 2017年1月9日 下午4:02:17
 * @Description TODO
 * 分岔口处不应当有顺行信息
 * 新增顺行	服务端前检查
 * 新增分岔口提示	服务端前检查
 */
public class GLM06008 extends baseRule {

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		for(IRow row:checkCommand.getGlmList()){
			//新增顺行
			if (row instanceof RdDirectroute){
				RdDirectroute rdDirectroute = (RdDirectroute) row;
				this.checkRdDirectroute(rdDirectroute);
			}
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
	 * @param rdDirectroute
	 * @throws Exception 
	 */
	private void checkRdDirectroute(RdDirectroute rdDirectroute) throws Exception {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();
		
		sb.append("SELECT DISTINCT RS.PID FROM RD_SE RS");
		sb.append(" WHERE RS.NODE_PID = "+rdDirectroute.getNodePid());
		sb.append(" AND RS.IN_LINK_PID = "+rdDirectroute.getInLinkPid());
		sb.append(" AND RS.OUT_LINK_PID = "+rdDirectroute.getOutLinkPid());
		sb.append("  AND RS.U_RECORD <>2");
		String sql = sb.toString();
		log.info("新增顺行后检查GLM06008--sql:" + sql);
		
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);
		
		if(!resultList.isEmpty()){
			String target = "[RD_DIRECTROUTE," + rdDirectroute.getPid() + "]";
			this.setCheckResult("", target, 0);
		}
	}

	/**
	 * @author Han Shaoming
	 * @param rdSe
	 * @throws Exception 
	 */
	private void checkRdSe(RdSe rdSe) throws Exception {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();
		
		sb.append("SELECT DE.PID FROM RD_DIRECTROUTE DE");
		sb.append(" WHERE DE.NODE_PID = "+rdSe.getNodePid());
		sb.append(" AND DE.IN_LINK_PID = "+rdSe.getInLinkPid());
		sb.append(" AND DE.OUT_LINK_PID = "+rdSe.getOutLinkPid());
		sb.append(" AND DE.U_RECORD <>2");
		String sql = sb.toString();
		log.info("新增分岔口提示后检查GLM06008--sql:" + sql);
		
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);
		
		if(!resultList.isEmpty()){
			String target = "[RD_SE," + rdSe.getPid() + "]";
			this.setCheckResult("", target, 0);
		}
	}
}
