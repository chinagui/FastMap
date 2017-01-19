package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLane;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: GLM32071
 * @author songdongyan
 * @date 2016年12月15日
 * @Description: 如果详细车道所在的link为车信进入线，则该link通行方向上的车道数应与对应通行方向上的车信中的车道数一致，否则报log
 * 新增车信:RdLaneConnexity
 * 修改车信:RdLaneConnexity
 * 新增详细车道:RdLane
 * 修改详细车道:RdLane
 */
public class GLM32071 extends baseRule{
	protected Logger log = Logger.getLogger(this.getClass());

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#preCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#postCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		for (IRow obj : checkCommand.getGlmList()) {
			// 新增/修改详细车道RdLane
			if (obj instanceof RdLane) {
				RdLane rdLane = (RdLane) obj;
				checkRdLane(rdLane);
			}
			// 新增/修改车信
			else if (obj instanceof RdLaneConnexity) {
				RdLaneConnexity rdLaneConnexity = (RdLaneConnexity) obj;
				checkRdLaneConnexity(rdLaneConnexity);
			}	
		}
		
	}

	/**
	 * @param rdLaneConnexity
	 * @param operType
	 * @throws Exception 
	 */
	private void checkRdLaneConnexity(RdLaneConnexity rdLaneConnexity) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT DISTINCT 1 FROM RD_LANE_CONNEXITY RLC, RD_LANE RLN,RD_LINK RL");
		sb.append(" WHERE RL.LINK_PID = RLN.LINK_PID");
		sb.append(" AND RLC.IN_LINK_PID = RL.LINK_PID");
		sb.append(" AND RL.E_NODE_PID = RLC.NODE_PID");
		sb.append(" AND RLN.LANE_DIR = 1");
		sb.append(" AND RL.DIRECT = 2");
		sb.append(" AND RLN.LANE_NUM <> RLC.LANE_NUM");
		sb.append(" AND RLC.PID = " + rdLaneConnexity.getPid());
		sb.append(" AND RLC.U_RECORD <> 2");
		sb.append(" AND RLN.U_RECORD <> 2");
		sb.append(" AND RL.U_RECORD <> 2");
		sb.append(" UNION");
		sb.append(" SELECT DISTINCT 1 FROM RD_LANE_CONNEXITY RLC, RD_LANE RLN,RD_LINK RL");
		sb.append(" WHERE RL.LINK_PID = RLN.LINK_PID");
		sb.append(" AND RLC.IN_LINK_PID = RL.LINK_PID");
		sb.append(" AND RL.S_NODE_PID = RLC.NODE_PID");
		sb.append(" AND RLN.LANE_DIR = 1");
		sb.append(" AND RL.DIRECT = 3");
		sb.append(" AND RLN.LANE_NUM <> RLC.LANE_NUM");
		sb.append(" AND RLC.PID = " + rdLaneConnexity.getPid());
		sb.append(" AND RLC.U_RECORD <> 2");
		sb.append(" AND RLN.U_RECORD <> 2");
		sb.append(" AND RL.U_RECORD <> 2");
		sb.append(" UNION");
		sb.append(" SELECT DISTINCT 1 FROM RD_LANE_CONNEXITY RLC, RD_LANE RLN,RD_LINK RL");
		sb.append(" WHERE RL.LINK_PID = RLN.LINK_PID");
		sb.append(" AND RLC.IN_LINK_PID = RL.LINK_PID");
		sb.append(" AND RL.E_NODE_PID = RLC.NODE_PID");
		sb.append(" AND RLN.LANE_DIR = 2");
		sb.append(" AND RL.DIRECT IN (0,1)");
		sb.append(" AND RLN.LANE_NUM <> RLC.LANE_NUM");
		sb.append(" AND RLC.PID = " + rdLaneConnexity.getPid());
		sb.append(" AND RLC.U_RECORD <> 2");
		sb.append(" AND RLN.U_RECORD <> 2");
		sb.append(" AND RL.U_RECORD <> 2");
		sb.append(" UNION");
		sb.append(" SELECT DISTINCT 1 FROM RD_LANE_CONNEXITY RLC, RD_LANE RLN,RD_LINK RL");
		sb.append(" WHERE RL.LINK_PID = RLN.LINK_PID");
		sb.append(" AND RLC.IN_LINK_PID = RL.LINK_PID");
		sb.append(" AND RL.S_NODE_PID = RLC.NODE_PID");
		sb.append(" AND RLN.LANE_DIR = 3");
		sb.append(" AND RL.DIRECT IN (0,1)");
		sb.append(" AND RLN.LANE_NUM <> RLC.LANE_NUM");
		sb.append(" AND RLC.PID = " + rdLaneConnexity.getPid());
		sb.append(" AND RLC.U_RECORD <> 2");
		sb.append(" AND RLN.U_RECORD <> 2");
		sb.append(" AND RL.U_RECORD <> 2");

		String sql = sb.toString();
		log.info("RdLaneConnexity后检查GLM32071:" + sql);

		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if(!resultList.isEmpty()){
			this.setCheckResult("", "[RD_LANE_CONNEXITY," + rdLaneConnexity.getPid() + "]", 0);
		}	
		
		
	}

	/**
	 * @param rdLane
	 * @param operType
	 * @throws Exception 
	 */
	private void checkRdLane(RdLane rdLane) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT DISTINCT 1 FROM RD_LANE_CONNEXITY RLC, RD_LANE RLN,RD_LINK RL");
		sb.append(" WHERE RL.LINK_PID = RLN.LINK_PID");
		sb.append(" AND RLC.IN_LINK_PID = RL.LINK_PID");
		sb.append(" AND RL.E_NODE_PID = RLC.NODE_PID");
		sb.append(" AND RLN.LANE_DIR = 1");
		sb.append(" AND RL.DIRECT = 2");
		sb.append(" AND RLN.LANE_NUM <> RLC.LANE_NUM");
		sb.append(" AND RLN.LINK_PID = " + rdLane.getPid());
		sb.append(" AND RLC.U_RECORD <> 2");
		sb.append(" AND RLN.U_RECORD <> 2");
		sb.append(" AND RL.U_RECORD <> 2");
		sb.append(" UNION");
		sb.append(" SELECT DISTINCT 1 FROM RD_LANE_CONNEXITY RLC, RD_LANE RLN,RD_LINK RL");
		sb.append(" WHERE RL.LINK_PID = RLN.LINK_PID");
		sb.append(" AND RLC.IN_LINK_PID = RL.LINK_PID");
		sb.append(" AND RL.S_NODE_PID = RLC.NODE_PID");
		sb.append(" AND RLN.LANE_DIR = 1");
		sb.append(" AND RL.DIRECT = 3");
		sb.append(" AND RLN.LANE_NUM <> RLC.LANE_NUM");
		sb.append(" AND RLN.LINK_PID = " + rdLane.getPid());
		sb.append(" AND RLC.U_RECORD <> 2");
		sb.append(" AND RLN.U_RECORD <> 2");
		sb.append(" AND RL.U_RECORD <> 2");
		sb.append(" UNION");
		sb.append(" SELECT DISTINCT 1 FROM RD_LANE_CONNEXITY RLC, RD_LANE RLN,RD_LINK RL");
		sb.append(" WHERE RL.LINK_PID = RLN.LINK_PID");
		sb.append(" AND RLC.IN_LINK_PID = RL.LINK_PID");
		sb.append(" AND RL.E_NODE_PID = RLC.NODE_PID");
		sb.append(" AND RLN.LANE_DIR = 2");
		sb.append(" AND RL.DIRECT IN (0,1)");
		sb.append(" AND RLN.LANE_NUM <> RLC.LANE_NUM");
		sb.append(" AND RLN.LINK_PID = " + rdLane.getPid());
		sb.append(" AND RLC.U_RECORD <> 2");
		sb.append(" AND RLN.U_RECORD <> 2");
		sb.append(" AND RL.U_RECORD <> 2");
		sb.append(" UNION");
		sb.append(" SELECT DISTINCT 1 FROM RD_LANE_CONNEXITY RLC, RD_LANE RLN,RD_LINK RL");
		sb.append(" WHERE RL.LINK_PID = RLN.LINK_PID");
		sb.append(" AND RLC.IN_LINK_PID = RL.LINK_PID");
		sb.append(" AND RL.S_NODE_PID = RLC.NODE_PID");
		sb.append(" AND RLN.LANE_DIR = 3");
		sb.append(" AND RL.DIRECT IN (0,1)");
		sb.append(" AND RLN.LANE_NUM <> RLC.LANE_NUM");
		sb.append(" AND RLN.LINK_PID = " + rdLane.getPid());
		sb.append(" AND RLC.U_RECORD <> 2");
		sb.append(" AND RLN.U_RECORD <> 2");
		sb.append(" AND RL.U_RECORD <> 2");

		String sql = sb.toString();
		log.info("RdLane后检查GLM32071:" + sql);

		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if(!resultList.isEmpty()){
			this.setCheckResult("", "[RD_LANE," + rdLane.getPid() + "]", 0);
		}	
	}

}
