package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLane;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: GLM32060
 * @author songdongyan
 * @date 2016年12月15日
 * @Description: 如果双方向link，已经制作了RD_LANE信息，那么必须同时存在顺方向（RD_LANE.LANE_DIR）的记录和逆方向(RD_LANE.LANE_DIR)的记录：
 * 1、如果缺失了逆方向的记录，则报log1：双方向link仅制作了顺方向车道，需要增加逆方向车道
 * 2、如果缺失了顺方向的记录，则报log2：双方向link仅制作了逆方向车道，需要增加顺方向车道
 * 新增详细车道:RdLane
 * 删除详细车道:RdLane
 * 道路方向编辑:RdLink
 */
public class GLM32060 extends baseRule{
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
			// 新增/删除详细车道RdLane
			if (obj instanceof RdLane) {
				RdLane rdLane = (RdLane) obj;
				checkRdLane(rdLane);
			}
			//道路方向编辑:RdLink
			else if (obj instanceof RdLink) {
				RdLink rdLink = (RdLink) obj;
				checkRdLink(rdLink);
			}	
		}
		
	}

	/**
	 * @param rdLink
	 * @param operType
	 * @throws Exception 
	 */
	private void checkRdLink(RdLink rdLink) throws Exception {
		//道路方向编辑
		if(rdLink.changedFields().containsKey("direct")){
			StringBuilder sb = new StringBuilder();
			
			sb.append("SELECT '双方向link仅制作了顺方向车道，需要增加逆方向车道' FROM RD_LINK RL, RD_LANE LAN");
			sb.append(" WHERE RL.LINK_PID = LAN.LINK_PID");
			sb.append(" AND RL.DIRECT = 1");
			sb.append(" AND RL.U_RECORD <> 2");
			sb.append(" AND LAN.U_RECORD <> 2");
			sb.append(" AND NOT EXISTS (SELECT 1 FROM RD_LANE LAN1");
			sb.append(" WHERE RL.LINK_PID = LAN1.LINK_PID AND LAN1.LANE_DIR = 3)");
			sb.append(" AND RL.LINK_PID = " + rdLink.getPid());
			sb.append(" UNION");
			sb.append(" SELECT '双方向link仅制作了逆方向车道，需要增加顺方向车道' FROM RD_LINK RL, RD_LANE LAN");
			sb.append(" WHERE RL.LINK_PID = LAN.LINK_PID");
			sb.append(" AND RL.DIRECT = 1");
			sb.append(" AND RL.U_RECORD <> 2");
			sb.append(" AND LAN.U_RECORD <> 2");
			sb.append(" AND NOT EXISTS (SELECT 1 FROM RD_LANE LAN1");
			sb.append(" WHERE RL.LINK_PID = LAN1.LINK_PID AND LAN1.LANE_DIR = 2)");
			sb.append(" AND RL.LINK_PID = " + rdLink.getPid());

			String sql = sb.toString();
			log.info("RdLink后检查GLM32060:" + sql);

			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);

			if(!resultList.isEmpty()){
				this.setCheckResult("", "[RD_LINK," + rdLink.getPid() + "]", 0,resultList.get(0).toString());
			}	
		}	
	}

	/**
	 * @param rdLane
	 * @param operType
	 * @throws Exception 
	 */
	private void checkRdLane(RdLane rdLane) throws Exception {
		StringBuilder sb = new StringBuilder();
		
		sb.append("SELECT '双方向link仅制作了顺方向车道，需要增加逆方向车道' FROM RD_LINK RL, RD_LANE LAN");
		sb.append(" WHERE RL.LINK_PID = LAN.LINK_PID");
		sb.append(" AND RL.DIRECT = 1");
		sb.append(" AND RL.U_RECORD <> 2");
		sb.append(" AND LAN.U_RECORD <> 2");
		sb.append(" AND NOT EXISTS (SELECT 1 FROM RD_LANE LAN1");
		sb.append(" WHERE RL.LINK_PID = LAN1.LINK_PID AND LAN1.LANE_DIR = 3)");
		sb.append(" AND LAN.LANE_PID = " + rdLane.getPid());
		sb.append(" UNION");
		sb.append(" SELECT '双方向link仅制作了逆方向车道，需要增加顺方向车道' FROM RD_LINK RL, RD_LANE LAN");
		sb.append(" WHERE RL.LINK_PID = LAN.LINK_PID");
		sb.append(" AND RL.DIRECT = 1");
		sb.append(" AND RL.U_RECORD <> 2");
		sb.append(" AND LAN.U_RECORD <> 2");
		sb.append(" AND NOT EXISTS (SELECT 1 FROM RD_LANE LAN1");
		sb.append(" WHERE RL.LINK_PID = LAN1.LINK_PID AND LAN1.LANE_DIR = 2)");
		sb.append(" AND LAN.LANE_PID = " + rdLane.getPid());

		String sql = sb.toString();
		log.info("RdLane后检查GLM32060:" + sql);

		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if(!resultList.isEmpty()){
			this.setCheckResult("", "[RD_LANE," + rdLane.getPid() + "]", 0,resultList.get(0).toString());
		}	
		
	}

}
