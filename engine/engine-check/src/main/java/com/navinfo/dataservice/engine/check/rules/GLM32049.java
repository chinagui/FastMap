package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLane;
import com.navinfo.dataservice.dao.glm.model.rd.tollgate.RdTollgate;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: GLM32049
 * @author songdongyan
 * @date 2016年12月15日
 * @Description: 如果详细车道的对应的link是收费站的进入线或者退出线，则该详细车道的车道数必须与对应收费站的通道数一致，（排除掉收费站通道数为0的情况），否则报log
 * 新增详细车道：RdLane
 * 修改详细车道：RdLane
 * 新增收费站后检查：RdTollgate
 * 新增收费站前检查：RdTollgate
 */
public class GLM32049 extends baseRule{
	protected Logger log = Logger.getLogger(this.getClass());

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#preCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for (IRow obj : checkCommand.getGlmList()) {
			// 新增收费站RdTollgate
			if (obj instanceof RdTollgate) {
				RdTollgate rdTollgate = (RdTollgate) obj;
				checkRdTollgatePre(rdTollgate);
			}	
		}
		
	}

	/**
	 * @param rdTollgate
	 * @throws Exception 
	 */
	private void checkRdTollgatePre(RdTollgate rdTollgate) throws Exception {
		//新增收费站触发检查
		if(rdTollgate.changedFields().isEmpty()){
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT DISTINCT 1 FROM RD_LANE L");
			sb.append(" WHERE L.LINK_PID IN (" + rdTollgate.getInLinkPid() + "," + rdTollgate.getOutLinkPid() + ")");
			sb.append(" AND L.LANE_NUM <> " + rdTollgate.getPassageNum());
			sb.append(" AND L.U_RECORD <> 2");
			
			String sql = sb.toString();
			log.info("RdTollgate前检查GLM32049:" + sql);

			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);

			if(!resultList.isEmpty()){
				this.setCheckResult("", "", 0);
			}	
		}
		
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
				checkRdLane(rdLane,checkCommand.getOperType());
			}
			// 新增收费站RdTollgate
			else if (obj instanceof RdTollgate) {
				RdTollgate rdTollgate = (RdTollgate) obj;
				checkRdTollgate(rdTollgate,checkCommand.getOperType());
			}	
		}
		
	}

	/**
	 * @param rdTollgate
	 * @param operType
	 * @throws Exception 
	 */
	private void checkRdTollgate(RdTollgate rdTollgate, OperType operType) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT DISTINCT 1 FROM RD_TOLLGATE RT, RD_LINK L, RD_LANE RL");
		sb.append(" WHERE RT.IN_LINK_PID = L.LINK_PID");
		sb.append(" AND L.LINK_PID = RL.LINK_PID");
		sb.append(" AND L.S_NODE_PID = RT.NODE_PID");
		sb.append(" AND RL.LANE_DIR IN (3, 1)");
		sb.append(" AND RT.PASSAGE_NUM <> 0");
		sb.append(" AND RT.PASSAGE_NUM <> RL.LANE_NUM");
		sb.append(" AND RT.PID = " + rdTollgate.getPid());
		sb.append(" AND RT.U_RECORD <> 2");
		sb.append(" AND L.U_RECORD <> 2");
		sb.append(" AND RL.U_RECORD <> 2");
		sb.append(" UNION");
		
		sb.append(" SELECT DISTINCT 1 FROM RD_TOLLGATE RT, RD_LINK L, RD_LANE RL");
		sb.append(" WHERE RT.IN_LINK_PID = L.LINK_PID");
		sb.append(" AND L.LINK_PID = RL.LINK_PID");
		sb.append(" AND L.E_NODE_PID = RT.NODE_PID");
		sb.append(" AND RL.LANE_DIR IN (2, 1)");
		sb.append(" AND RT.PASSAGE_NUM <> 0");
		sb.append(" AND RT.PASSAGE_NUM <> RL.LANE_NUM");
		sb.append(" AND RT.PID = " + rdTollgate.getPid());
		sb.append(" AND RT.U_RECORD <> 2");
		sb.append(" AND L.U_RECORD <> 2");
		sb.append(" AND RL.U_RECORD <> 2");
		sb.append(" UNION");

		sb.append(" SELECT DISTINCT 1 FROM RD_TOLLGATE RT, RD_LINK L, RD_LANE RL");
		sb.append(" WHERE RT.OUT_LINK_PID = L.LINK_PID");
		sb.append(" AND L.LINK_PID = RL.LINK_PID");
		sb.append(" AND L.S_NODE_PID = RT.NODE_PID");
		sb.append(" AND RL.LANE_DIR IN (2, 1)");
		sb.append(" AND RT.PASSAGE_NUM <> 0");
		sb.append(" AND RT.PASSAGE_NUM <> RL.LANE_NUM");
		sb.append(" AND RT.PID = " + rdTollgate.getPid());
		sb.append(" AND RT.U_RECORD <> 2");
		sb.append(" AND L.U_RECORD <> 2");
		sb.append(" AND RL.U_RECORD <> 2");
		sb.append(" UNION");
		
		sb.append(" SELECT DISTINCT 1 FROM RD_TOLLGATE RT, RD_LINK L, RD_LANE RL");
		sb.append(" WHERE RT.OUT_LINK_PID = L.LINK_PID");
		sb.append(" AND L.LINK_PID = RL.LINK_PID");
		sb.append(" AND L.E_NODE_PID = RT.NODE_PID");
		sb.append(" AND RL.LANE_DIR IN (3, 1)");
		sb.append(" AND RT.PASSAGE_NUM <> 0");
		sb.append(" AND RT.PASSAGE_NUM <> RL.LANE_NUM");
		sb.append(" AND RT.PID = " + rdTollgate.getPid());
		sb.append(" AND RT.U_RECORD <> 2");
		sb.append(" AND L.U_RECORD <> 2");
		sb.append(" AND RL.U_RECORD <> 2");

		String sql = sb.toString();
		log.info("RdTollgate后检查GLM32049:" + sql);

		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if(!resultList.isEmpty()){
			this.setCheckResult("", "[RD_TOLLGATE," + rdTollgate.getPid() + "]", 0);
		}	
		
	}

	/**
	 * @param rdLane
	 * @param operType
	 * @throws Exception 
	 */
	private void checkRdLane(RdLane rdLane, OperType operType) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT DISTINCT 1 FROM RD_TOLLGATE RT, RD_LINK L, RD_LANE RL");
		sb.append(" WHERE RT.IN_LINK_PID = L.LINK_PID");
		sb.append(" AND L.LINK_PID = RL.LINK_PID");
		sb.append(" AND L.S_NODE_PID = RT.NODE_PID");
		sb.append(" AND RL.LANE_DIR IN (3, 1)");
		sb.append(" AND RT.PASSAGE_NUM <> 0");
		sb.append(" AND RT.PASSAGE_NUM <> RL.LANE_NUM");
		sb.append(" AND RL.LANE_PID = " + rdLane.getPid());
		sb.append(" AND RT.U_RECORD <> 2");
		sb.append(" AND L.U_RECORD <> 2");
		sb.append(" AND RL.U_RECORD <> 2");
		sb.append(" UNION");
		
		sb.append(" SELECT DISTINCT 1 FROM RD_TOLLGATE RT, RD_LINK L, RD_LANE RL");
		sb.append(" WHERE RT.IN_LINK_PID = L.LINK_PID");
		sb.append(" AND L.LINK_PID = RL.LINK_PID");
		sb.append(" AND L.E_NODE_PID = RT.NODE_PID");
		sb.append(" AND RL.LANE_DIR IN (2, 1)");
		sb.append(" AND RT.PASSAGE_NUM <> 0");
		sb.append(" AND RT.PASSAGE_NUM <> RL.LANE_NUM");
		sb.append(" AND RL.LANE_PID = " + rdLane.getPid());
		sb.append(" AND RT.U_RECORD <> 2");
		sb.append(" AND L.U_RECORD <> 2");
		sb.append(" AND RL.U_RECORD <> 2");
		sb.append(" UNION");

		sb.append(" SELECT DISTINCT 1 FROM RD_TOLLGATE RT, RD_LINK L, RD_LANE RL");
		sb.append(" WHERE RT.OUT_LINK_PID = L.LINK_PID");
		sb.append(" AND L.LINK_PID = RL.LINK_PID");
		sb.append(" AND L.S_NODE_PID = RT.NODE_PID");
		sb.append(" AND RL.LANE_DIR IN (2, 1)");
		sb.append(" AND RT.PASSAGE_NUM <> 0");
		sb.append(" AND RT.PASSAGE_NUM <> RL.LANE_NUM");
		sb.append(" AND RL.LANE_PID = " + rdLane.getPid());
		sb.append(" AND RT.U_RECORD <> 2");
		sb.append(" AND L.U_RECORD <> 2");
		sb.append(" AND RL.U_RECORD <> 2");
		sb.append(" UNION");
		
		sb.append(" SELECT DISTINCT 1 FROM RD_TOLLGATE RT, RD_LINK L, RD_LANE RL");
		sb.append(" WHERE RT.OUT_LINK_PID = L.LINK_PID");
		sb.append(" AND L.LINK_PID = RL.LINK_PID");
		sb.append(" AND L.E_NODE_PID = RT.NODE_PID");
		sb.append(" AND RL.LANE_DIR IN (3, 1)");
		sb.append(" AND RT.PASSAGE_NUM <> 0");
		sb.append(" AND RT.PASSAGE_NUM <> RL.LANE_NUM");
		sb.append(" AND RL.LANE_PID = " + rdLane.getPid());
		sb.append(" AND RT.U_RECORD <> 2");
		sb.append(" AND L.U_RECORD <> 2");
		sb.append(" AND RL.U_RECORD <> 2");

		String sql = sb.toString();
		log.info("RdLane后检查GLM32049:" + sql);

		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if(!resultList.isEmpty()){
			this.setCheckResult("", "[RD_LANE," + rdLane.getPid() + "]", 0);
		}	
		
	}

}
