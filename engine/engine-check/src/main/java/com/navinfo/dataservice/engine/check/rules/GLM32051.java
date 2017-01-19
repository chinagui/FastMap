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
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResultWithGeo;

/** 
 * @ClassName: GLM32051
 * @author songdongyan
 * @date 2016年12月16日
 * @Description: 车道类型值域为减速车道、复合车道时，则对应的link种别必须为1或者2
 * 新增详细车道：RdLane
 * 修改详细车道：RdLane
 * 道路属性编辑：RdLink
 */
public class GLM32051 extends baseRule{
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
		//link种别编辑
		if(rdLink.changedFields().containsKey("kind")){
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT L.GEOMETRY, '[RD_LINK,' || L.LINK_PID || ']', L.MESH_ID");
			sb.append(" FROM RD_LANE RL, RD_LINK L");
			sb.append(" WHERE (BITAND(RL.LANE_TYPE, 8) = 8 OR");
			sb.append(" BITAND(RL.LANE_TYPE, 2) = 2)");
			sb.append(" AND RL.LINK_PID = L.LINK_PID");
			sb.append(" AND L.KIND NOT IN (1, 2)");
			sb.append(" AND L.U_RECORD <> 2");
			sb.append(" AND RL.U_RECORD <> 2");
			sb.append(" AND L.LINK_PID = " + rdLink.getPid());
			
			String sql = sb.toString();
			log.info("RdLink后检查GLM32051:" + sql);
	
			DatabaseOperatorResultWithGeo getObj = new DatabaseOperatorResultWithGeo();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);
			
			if(!resultList.isEmpty()){
				this.setCheckResult(resultList.get(0).toString(), resultList.get(1).toString(),(int)resultList.get(2));
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
		sb.append("SELECT DISTINCT 1 FROM RD_LANE RL, RD_LINK L");
		sb.append(" WHERE (BITAND(RL.LANE_TYPE, 8) = 8 OR");
		sb.append(" BITAND(RL.LANE_TYPE, 2) = 2)");
		sb.append(" AND RL.LINK_PID = L.LINK_PID");
		sb.append(" AND L.KIND NOT IN (1, 2)");
		sb.append(" AND L.U_RECORD <> 2");
		sb.append(" AND RL.U_RECORD <> 2");
		sb.append(" AND RL.LANE_PID = " + rdLane.getPid());
		
		String sql = sb.toString();
		log.info("RdLane后检查GLM32051:" + sql);

		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if(!resultList.isEmpty()){
			this.setCheckResult("", "[RD_LANE," + rdLane.getPid() + "]", 0);
		}
		
	}

}
