package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLane;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResultWithGeo;

/** 
 * @ClassName: GLM32052
 * @author songdongyan
 * @date 2016年12月16日
 * @Description: 车道类型值域为转向车道、潮汐车道时，则对应的link种别不能为1或者2，否则log
 * 排除对象：如果对应的link种别为1或者2时，link的形态为IC时不报错
 * 新增详细车道：RdLane
 * 修改详细车道：RdLane
 * 道路属性编辑：RdLink
 */
public class GLM32052 extends baseRule{

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
			// link种别编辑
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
			sb.append(" WHERE (BITAND(RL.LANE_TYPE, 2048) = 2048 OR");
			sb.append(" BITAND(RL.LANE_TYPE, 8192) = 8192)");
			sb.append(" AND RL.LINK_PID = L.LINK_PID");
			sb.append(" AND L.KIND IN (1, 2)");
			sb.append(" AND L.U_RECORD <> 2");
			sb.append(" AND RL.U_RECORD <> 2");
			sb.append(" AND L.LINK_PID = " + rdLink.getPid());
			sb.append(" AND NOT EXISTS (SELECT 1 FROM RD_LINK_FORM RF");
			sb.append(" WHERE RF.FORM_OF_WAY = 10");
			sb.append(" AND RF.LINK_PID = L.LINK_PID");
			sb.append(" AND RF.U_RECORD <> 2)");
			
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
		sb.append(" WHERE (BITAND(RL.LANE_TYPE, 2048) = 2048 OR");
		sb.append(" BITAND(RL.LANE_TYPE, 8192) = 8192)");
		sb.append(" AND RL.LINK_PID = L.LINK_PID");
		sb.append(" AND L.KIND IN (1, 2)");
		sb.append(" AND L.U_RECORD <> 2");
		sb.append(" AND RL.U_RECORD <> 2");
		sb.append(" AND RL.LANE_PID = " + rdLane.getPid());
		sb.append(" AND NOT EXISTS (SELECT 1 FROM RD_LINK_FORM RF");
		sb.append(" WHERE RF.FORM_OF_WAY = 10");
		sb.append(" AND RF.LINK_PID = L.LINK_PID");
		sb.append(" AND RF.U_RECORD <> 2)");
		
		String sql = sb.toString();
		log.info("RdLane后检查GLM32052:" + sql);

		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if(!resultList.isEmpty()){
			this.setCheckResult("", "[RD_LANE," + rdLane.getPid() + "]", 0);
		}
		
	}

}
