package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameNode;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameNodePart;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/**
 * 在一组同一Node关系中，必须包含一个道路Node，否则报Log
 * 
 * @author wangdongbin
 *
 */
public class GLM22003 extends baseRule {

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		for(IRow obj:checkCommand.getGlmList()){
			RdSameNode rdSameNode = (RdSameNode) obj;
			checkSameNode(rdSameNode);
		}
	}

	/**
	 * 查询同一点关系
	 * @param rdSameNode
	 * @throws Exception
	 */
	private void checkSameNode(RdSameNode rdSameNode) throws Exception {
		List<IRow> parts = rdSameNode.getParts();
		List<Integer> nodePids = new ArrayList<Integer>();
		for (IRow part:parts) {
			RdSameNodePart rdSameNodePart = (RdSameNodePart)part;
			nodePids.add(rdSameNodePart.getNodePid());
		}
		// 检查组成同一关系的点是否包含道路Node
		checkNode(nodePids,rdSameNode.getPid());
	}

	/**
	 * 
	 * @param nodePids
	 * @param samePid
	 * @throws Exception
	 */
	private void checkNode(List<Integer> nodePids, int samePid) throws Exception {
		String pids = StringUtils.join(nodePids,",");
		StringBuilder sb = new StringBuilder();
		sb.append("select 1");
		sb.append(" from rd_node t");
		sb.append(" where t.node_pid in ");
		sb.append("("+pids+")");
		
		
		String sql = sb.toString();
		log.info("RdSameNode后检查GLM22003:" + sql);

		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		// 不包含道路node，报log
		if(resultList.size() == 0){
			String target = "[RD_SAME_NODE," + samePid + "]";
			this.setCheckResult("", target, 0);
		}
	}

}
