package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/**
 * @ClassName PERMIT_CHECK_CREATENODE_MUST_SNAP_LINK
 * @author Han Shaoming
 * @date 2016年12月27日 下午8:32:26
 * @Description TODO
 * 创建node必须捕捉到link上
 * 新增NODE服务端前检查:RdNode
 * 新增AD_NODE服务端前检查:AdNode
 */
public class PERMIT_CHECK_CREATENODE_MUST_SNAP_LINK extends baseRule {

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		for (IRow row: checkCommand.getGlmList()){
			//新增NODE
			if(row instanceof RdNode){
				RdNode rdNode = (RdNode)row;
				checkRdNode(rdNode);
			}
			//新增AD_NODE
			else if(row instanceof AdNode){
				AdNode adNode = (AdNode)row;
				checkAdNode(adNode);
			}
		}
		
	}

	/**
	 * @author Han Shaoming
	 * @param adNode
	 * @throws Exception 
	 */
	private void checkAdNode(AdNode adNode) throws Exception {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();
		 
		sb.append("SELECT A.LINK_PID FROM AD_LINK A WHERE A.U_RECORD <> 2");
		sb.append("AND (A.S_NODE_PID = "+adNode.getPid()+" OR A.E_NODE_PID = "+adNode.getPid()+")");
		String sql = sb.toString();
		log.info("AdNode前检查PERMIT_CHECK_CREATENODE_MUST_SNAP_LINK--sql:" + sql);
		
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if(resultList.isEmpty()){
			String target = "[AD_NODE," + adNode.getPid() + "]";
			this.setCheckResult("", target, 0);
		}
	}

	/**
	 * @author Han Shaoming
	 * @param rdNode
	 * @throws Exception 
	 */
	private void checkRdNode(RdNode rdNode) throws Exception {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();
		 
		sb.append("SELECT R.LINK_PID FROM RD_LINK R WHERE R.U_RECORD <> 2");
		sb.append("AND (R.S_NODE_PID = "+rdNode.getPid()+" OR R.E_NODE_PID = "+rdNode.getPid()+")");
		String sql = sb.toString();
		log.info("RdNode前检查PERMIT_CHECK_CREATENODE_MUST_SNAP_LINK--sql:" + sql);
		
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if(resultList.isEmpty()){
			String target = "[RD_NODE," + rdNode.getPid() + "]";
			this.setCheckResult("", target, 0);
		}
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
