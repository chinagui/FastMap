package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLane;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLaneCondition;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResultWithGeo;

/** 
 * @ClassName: GLM32092
 * @author songdongyan
 * @date 2016年12月14日
 * @Description: 如果link为公交车专用道，则该link上所有车道车辆类型必须存在允许公交车车辆类型
 * 道路属性编辑:RdLink,RdLinkForm
 * 新增详细车道:RdLane
 * 修改详细车道:RdLane,RdLaneCondition
 */
public class GLM32092 extends baseRule{

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
				checkRdLane(rdLane,checkCommand.getOperType());
			}
			//修改详细车道RdLaneCondition
			else if(obj instanceof RdLaneCondition){
				RdLaneCondition rdLaneCondition = (RdLaneCondition)obj;
				checkRdLaneCondition(rdLaneCondition);
			}
//			// Link属性编辑RdLink
//			else if (obj instanceof RdLink) {
//				RdLink rdLink = (RdLink) obj;
//				checkRdLink(rdLink,checkCommand.getOperType());
//			}

			// Link属性编辑RdLinkForm
			else if (obj instanceof RdLinkForm) {
				RdLinkForm rdLinkForm = (RdLinkForm) obj;
				checkRdLinkForm(rdLinkForm,checkCommand.getOperType());
			}	
		}
		
	}

	/**
	 * @param rdLinkForm
	 * @param operType
	 * @throws Exception 
	 */
	private void checkRdLinkForm(RdLinkForm rdLinkForm, OperType operType) throws Exception {
		int formOfWay = 0;
		if(rdLinkForm.changedFields().containsKey("formOfWay")){
			formOfWay = Integer.parseInt(rdLinkForm.changedFields().get("formOfWay").toString()) ;
		}else{
			formOfWay = rdLinkForm.getFormOfWay();
		}
		if(formOfWay==22){
//		if(rdLinkForm.getFormOfWay() == 22){
			StringBuilder sb = new StringBuilder();

			sb.append("SELECT RL.GEOMETRY,'[RD_LINK,' || RL.LINK_PID || ']',RL.MESH_ID FROM RD_LINK RL");
			sb.append(" WHERE RL.LINK_PID = " + rdLinkForm.getLinkPid());
			sb.append(" AND RL.U_RECORD <> 2");
			sb.append(" AND NOT EXISTS");
			sb.append(" (SELECT 1 FROM RD_LANE RA, RD_LANE_CONDITION RLC");
			sb.append(" WHERE RA.LINK_PID= " + rdLinkForm.getLinkPid());
			sb.append(" AND RA.LANE_PID = RLC.LANE_PID");
			sb.append(" AND ((BITAND(RLC.VEHICLE, 2147483648) = 2147483648 AND BITAND(RLC.VEHICLE, 256) = 0) OR");
			sb.append(" (BITAND(RLC.VEHICLE, 2147483648) = 0 AND BITAND(RLC.VEHICLE, 256) = 256))");
			sb.append(" AND RA.U_RECORD <> 2");
			sb.append(" AND RLC.U_RECORD <> 2)");

			String sql = sb.toString();
			log.info("RdLinkForm后检查GLM32092:" + sql);

			DatabaseOperatorResultWithGeo getObj = new DatabaseOperatorResultWithGeo();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);

			if(!resultList.isEmpty()){
				this.setCheckResult(resultList.get(0).toString(), resultList.get(1).toString(), (int)resultList.get(2));
			}	
		}
		
	}

	/**
	 * @param rdLink
	 * @param operType
	 * @throws Exception 
	 */
	private void checkRdLink(RdLink rdLink, OperType operType) throws Exception {
		
		StringBuilder sb = new StringBuilder();

		sb.append("SELECT RL.GEOMETRY,'[RD_LINK,' || RL.LINK_PID || ']',RL.MESH_ID FROM RD_LINK RL, RD_LINK_FORM RLF");
		sb.append(" WHERE RL.LINK_PID = " + rdLink.getPid());
		sb.append(" AND RL.LINK_PID = RLF.LINK_PID");
		sb.append(" AND RLF.FORM_OF_WAY = 22");
		sb.append(" AND RL.U_RECORD <> 2");
		sb.append(" AND RLF.U_RECORD <> 2");
		sb.append(" AND NOT EXISTS");
		sb.append(" (SELECT 1 FROM RD_LANE RA, RD_LANE_CONDITION RLC");
		sb.append(" WHERE RA.LINK_PID= " + rdLink.getPid());
		sb.append(" AND RA.LANE_PID = RLC.LANE_PID");
		sb.append(" AND ((BITAND(RLC.VEHICLE, 2147483648) = 2147483648 AND BITAND(RLC.VEHICLE, 256) = 0) OR");
		sb.append(" (BITAND(RLC.VEHICLE, 2147483648) = 0 AND BITAND(RLC.VEHICLE, 256) = 256))");
		sb.append(" AND RA.U_RECORD <> 2");
		sb.append(" AND RLC.U_RECORD <> 2)");

		String sql = sb.toString();
		log.info("RdLink后检查GLM32092:" + sql);

		DatabaseOperatorResultWithGeo getObj = new DatabaseOperatorResultWithGeo();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if(!resultList.isEmpty()){
			this.setCheckResult(resultList.get(0).toString(), resultList.get(1).toString(), (int)resultList.get(2));
		}	
	}

	/**
	 * @param rdLaneCondition
	 * @throws Exception 
	 */
	private void checkRdLaneCondition(RdLaneCondition rdLaneCondition) throws Exception {
		StringBuilder sb = new StringBuilder();

		sb.append("SELECT 1 FROM RD_LINK RL, RD_LINK_FORM RLF,RD_LANE RA");
		sb.append(" WHERE RA.LANE_PID = " + rdLaneCondition.getLanePid());
		sb.append(" AND RL.LINK_PID = RLF.LINK_PID");
		sb.append(" AND RLF.FORM_OF_WAY = 22");
		sb.append(" AND RL.LINK_PID = RA.LINK_PID");
		sb.append(" AND RA.U_RECORD <> 2");
		sb.append(" AND RL.U_RECORD <> 2");
		sb.append(" AND RLF.U_RECORD <> 2");
		sb.append(" AND NOT EXISTS");
		sb.append(" (SELECT 1 FROM RD_LANE_CONDITION RLC");
		sb.append(" WHERE RA.LANE_PID = RLC.LANE_PID");
		sb.append(" AND ((BITAND(RLC.VEHICLE, 2147483648) = 2147483648 AND BITAND(RLC.VEHICLE, 256) = 0) OR");
		sb.append(" (BITAND(RLC.VEHICLE, 2147483648) = 0 AND BITAND(RLC.VEHICLE, 256) = 256))");
		sb.append(" AND RLC.U_RECORD <> 2)");

		String sql = sb.toString();
		log.info("RdLink后检查GLM32092:" + sql);

		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if(!resultList.isEmpty()){
			this.setCheckResult("", "[RD_LANE," + rdLaneCondition.getLanePid() +"]", 0);
		}	
		
	}

	/**
	 * @param rdLane
	 * @param operType
	 * @throws Exception 
	 */
	private void checkRdLane(RdLane rdLane, OperType operType) throws Exception {
		StringBuilder sb = new StringBuilder();

		sb.append("SELECT 1 FROM RD_LINK RL, RD_LINK_FORM RLF,RD_LANE A");
		sb.append(" WHERE A.LANE_PID = " + rdLane.getPid());
		sb.append(" AND RL.LINK_PID = A.LINK_PID");
		sb.append(" AND RL.LINK_PID = RLF.LINK_PID");
		sb.append(" AND RLF.FORM_OF_WAY = 22");
		sb.append(" AND RL.U_RECORD <> 2");
		sb.append(" AND RLF.U_RECORD <> 2");
		sb.append(" AND NOT EXISTS");
		sb.append(" (SELECT 1 FROM RD_LANE RA, RD_LANE_CONDITION RLC");
		sb.append(" WHERE RA.LANE_PID = " + rdLane.getPid());
		sb.append(" AND RA.LANE_PID = RLC.LANE_PID");
		sb.append(" AND ((BITAND(RLC.VEHICLE, 2147483648) = 2147483648 AND BITAND(RLC.VEHICLE, 256) = 0) OR");
		sb.append(" (BITAND(RLC.VEHICLE, 2147483648) = 0 AND BITAND(RLC.VEHICLE, 256) = 256))");
		sb.append(" AND RA.U_RECORD <> 2");
		sb.append(" AND RLC.U_RECORD <> 2)");

		String sql = sb.toString();
		log.info("RdLink后检查GLM32092:" + sql);

		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if(!resultList.isEmpty()){
			this.setCheckResult("", "[RD_LANE," + rdLane.getPid() +"]", 0);
		}	
		
	}

}
