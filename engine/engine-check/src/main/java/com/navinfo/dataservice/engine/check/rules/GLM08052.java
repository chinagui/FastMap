package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeForm;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/**
 * @ClassName GLM08052
 * @author Han Shaoming
 * @date 2016年12月22日 下午4:36:24
 * @Description TODO
 * 一组车信（路口和线线车信）的连通关系中的经过线存在障碍物，则报log
 * 新增车信,修改车信服务端后检查:RdLaneConnexity	
 * node属性编辑服务端后检查:RdNode,RdNodeForm
 */
public class GLM08052 extends baseRule {

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		for(IRow row:checkCommand.getGlmList()){
			//新增车信,修改车信
			if (row instanceof RdLaneConnexity){
				RdLaneConnexity rdLaneConnexity = (RdLaneConnexity) row;
				checkRdLaneConnexity(rdLaneConnexity);
			}
			//node属性编辑
			else if (row instanceof RdNode){
				RdNode rdNode = (RdNode) row;
				checkRdNode(rdNode);
			}else if (row instanceof RdNodeForm){
				RdNodeForm rdNodeForm = (RdNodeForm) row;
				checkRdNodeForm(rdNodeForm);
			}
		}
	}

	/**
	 * @author Han Shaoming
	 * @param rdLaneConnexity
	 * @throws Exception 
	 */
	private void checkRdLaneConnexity(RdLaneConnexity rdLaneConnexity) throws Exception {
		// TODO Auto-generated method stub
		//新增车信,修改车信,触发检查
		StringBuilder sb = new StringBuilder();

		sb.append("WITH T AS (SELECT RL.S_NODE_PID NODE_PID FROM ");
		sb.append(" RD_LANE_CONNEXITY RLC,RD_LANE_TOPOLOGY RLT,RD_LANE_VIA RLV, RD_LINK RL");
		sb.append(" WHERE RLC.PID=RLT.CONNEXITY_PID AND RLT.TOPOLOGY_ID=RLV.TOPOLOGY_ID");
		sb.append(" AND RLV.LINK_PID = RL.LINK_PID AND RLC.PID="+rdLaneConnexity.getPid());
		sb.append(" UNION SELECT RL.E_NODE_PID NODE_PID");
		sb.append(" FROM RD_LANE_CONNEXITY RLC,RD_LANE_TOPOLOGY RLT,RD_LANE_VIA RLV, RD_LINK RL");
		sb.append(" WHERE RLC.PID=RLT.CONNEXITY_PID AND RLT.TOPOLOGY_ID=RLV.TOPOLOGY_ID");
		sb.append(" AND RLV.LINK_PID = RL.LINK_PID AND RLC.PID="+rdLaneConnexity.getPid()+")");
		sb.append(" SELECT DISTINCT T.NODE_PID FROM T, RD_NODE_FORM RNF");
		sb.append(" WHERE T.NODE_PID = RNF.NODE_PID AND RNF.FORM_OF_WAY = 15");
		
		String sql = sb.toString();
		log.info("RdLaneConnexity后检查GLM08052--sql:" + sql);
		
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if(!resultList.isEmpty()){
			String target = "[RD_LANE_CONNEXITY," + rdLaneConnexity.getPid() + "]";
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
		//node属性编辑
		int nodePid = rdNode.getPid();
		this.check(nodePid);
	}
	
	/**
	 * @author Han Shaoming
	 * @param rdNodeForm
	 * @throws Exception 
	 */
	private void checkRdNodeForm(RdNodeForm rdNodeForm) throws Exception {
		// TODO Auto-generated method stub
		//node属性编辑
		int nodePid = rdNodeForm.getNodePid();
		this.check(nodePid);
	}
	
	/**
	 * @author Han Shaoming
	 * @param rdNodeForm
	 * @throws Exception 
	 *  
  
 
  


	 */
	private void check(int nodePid) throws Exception {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();

		sb.append("WITH R AS (SELECT RL.LINK_PID LINK_PID");
		sb.append(" FROM RD_LINK RL ,RD_NODE_FORM RNF WHERE RL.S_NODE_PID = RNF.NODE_PID");
		sb.append(" AND RNF.NODE_PID="+nodePid+" AND RNF.FORM_OF_WAY = 15");
		sb.append(" UNION SELECT RL.LINK_PID LINK_PID FROM RD_LINK RL ,RD_NODE_FORM RNF");
		sb.append(" WHERE RL.E_NODE_PID = RNF.NODE_PID AND RNF.NODE_PID="+nodePid+" AND RNF.FORM_OF_WAY = 15)");
		sb.append(" SELECT R.LINK_PID FROM RD_LANE_VIA RLV,R WHERE RLV.LINK_PID=R.LINK_PID");
		
		String sql = sb.toString();
		log.info("RdNode后检查GLM08052--sql:" + sql);
		
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if(!resultList.isEmpty()){
			String target = "[RD_NODE," + nodePid + "]";
			this.setCheckResult("", target, 0);
		}
	}


}
