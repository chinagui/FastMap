package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.directroute.RdDirectroute;
import com.navinfo.dataservice.dao.glm.model.rd.directroute.RdDirectrouteVia;
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
		//判断新增
		if(ObjStatus.INSERT.equals(rdDirectroute.status())){
			List<Integer> linkPids = new ArrayList<Integer>();
			//进入线
			int inLinkPid = rdDirectroute.getInLinkPid();
			linkPids.add(inLinkPid);
			//退出线
			int outLinkPid = rdDirectroute.getOutLinkPid();
			linkPids.add(outLinkPid);
			//经过线
			List<IRow> vias = rdDirectroute.getVias();
			if(vias == null || vias.isEmpty()){
				StringBuilder sb = new StringBuilder();
				
				sb.append("SELECT DISTINCT RL.LINK_PID FROM RD_LINK RL");
				sb.append(" WHERE (RL.S_NODE_PID = "+rdDirectroute.getNodePid());
				sb.append(" OR RL.E_NODE_PID = "+rdDirectroute.getNodePid()+")");
				sb.append(" AND RL.U_RECORD <>2");
				String sql = sb.toString();
				log.info("新增顺行前检查GLM14004--sql:" + sql);
				
				DatabaseOperator getObj = new DatabaseOperator();
				List<Object> resultList = new ArrayList<Object>();
				resultList = getObj.exeSelect(this.getConn(), sql);
				
				if(resultList.size()<3){
					String target = "[RD_DIRECTROUTE," + rdDirectroute.getPid() + "]";
					this.setCheckResult("", target, 0);
				}
			}
			//线线关系
			else if(!vias.isEmpty()){
				List<Integer> viaPids = new ArrayList<Integer>();
				for (IRow iRows : vias) {
					if(iRows instanceof RdDirectrouteVia){
						RdDirectrouteVia rdDirectrouteVia = (RdDirectrouteVia) iRows;
						viaPids.add(rdDirectrouteVia.getLinkPid());
					}
				}
				for (Integer pid : linkPids) {
					boolean check = this.check(pid,viaPids);
					
					if(check){
						String target = "[RD_DIRECTROUTE," + rdDirectroute.getPid() + "]";
						this.setCheckResult("", target, 0);
					}
				}
			}
		}
	}
	
	/**
	 * @author Han Shaoming
	 * @param viaPids 
	 * @param rdNodeForm
	 * @throws Exception 
	 */
	private boolean check(int pid, List<Integer> viaPids) throws Exception {
		// TODO Auto-generated method stub
		boolean flag = false;
		StringBuilder sb = new StringBuilder();
		 
		sb.append("WITH T AS(SELECT RL.S_NODE_PID NODE_PID FROM RD_LINK RL,RD_LINK RL1 WHERE");
		sb.append(" RL.LINK_PID = "+pid);
		sb.append(" AND (RL.S_NODE_PID = RL1.S_NODE_PID OR RL.S_NODE_PID = RL1.E_NODE_PID)");
		sb.append(" AND RL1.LINK_PID IN("+StringUtils.join(viaPids,",")+")");
		sb.append(" AND RL.U_RECORD <>2 AND RL1.U_RECORD <>2");
		sb.append(" UNION");
		sb.append(" SELECT RL.E_NODE_PID NODE_PID FROM RD_LINK RL,RD_LINK RL2 WHERE");
		sb.append(" RL.LINK_PID = "+pid);
		sb.append(" AND (RL.E_NODE_PID = RL2.S_NODE_PID OR RL.E_NODE_PID = RL2.E_NODE_PID)");
		sb.append(" AND RL2.LINK_PID IN("+StringUtils.join(viaPids,",")+")");
		sb.append(" AND RL.U_RECORD <>2 AND RL2.U_RECORD <>2)");
		sb.append(" SELECT T.NODE_PID FROM RD_LINK RL ,T");
		sb.append(" WHERE (RL.S_NODE_PID = T.NODE_PID OR RL.E_NODE_PID = T.NODE_PID)");
		sb.append(" AND RL.U_RECORD <>2 GROUP BY T.NODE_PID HAVING COUNT(1)<3");
		String sql = sb.toString();
		log.info("新增顺行前检查GLM14004--sql:" + sql);
		
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);
		
		if(!resultList.isEmpty()){
			flag = true;
		}
		return flag;
	}
}
