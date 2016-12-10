package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLane;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLaneCondition;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResultWithGeo;

/** 
 * @ClassName: GLM32020
 * @author songdongyan
 * @date 2016年12月10日
 * @Description: Link属性为公交车专用道，详细车道车辆类型必须只能允许公交车，否则报log
 * 道路属性编辑服务端后检查：RdLink,RdLinkForm
 * 新增详细车道后检查:RdLane
 * 修改详细车道后检查:RdLane,RdLaneCondition
 */
public class GLM32020 extends baseRule {

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
		for(IRow row:checkCommand.getGlmList()){
			//新增详细车道、修改详细车道
			if(row instanceof RdLane){
				RdLane rdLane = (RdLane)row;
				checkRdLane(rdLane);
			}
			//修改详细车道
			else if(row instanceof RdLaneCondition){
				RdLaneCondition rdLaneCondition = (RdLaneCondition)row;
				checkRdLaneCondition(rdLaneCondition);
			}
			//道路属性编辑
			else if(row instanceof RdLink){
				RdLink rdLink = (RdLink)row;
				checkRdLink(rdLink);
			}
			//道路属性编辑
			else if(row instanceof RdLinkForm){
				RdLinkForm rdLinkForm = (RdLinkForm)row;
				checkRdLink(rdLinkForm);
			}
		}

	}

	/**
	 * @param rdLinkForm
	 * @throws Exception 
	 */
	private void checkRdLink(RdLinkForm rdLinkForm) throws Exception {
		//link属性为公交车专用道,触发检查
		if(rdLinkForm.getFormOfWay()==22){
			StringBuilder sb = new StringBuilder();

			sb.append("SELECT R.GEOMETRY,'[RD_LINK,' || R.LINK_PID || ']',R.MESH_ID");
			sb.append(" FROM RD_LANE L, RD_LANE_CONDITION C, RD_LINK R");
			sb.append(" WHERE L.LANE_PID = C.LANE_PID");
			sb.append(" AND L.LINK_PID = R.LINK_PID");
			sb.append(" AND C.VEHICLE NOT IN (2147484160, 2147483135)");
			sb.append(" AND R.LINK_PID = " + rdLinkForm.getLinkPid());
			sb.append(" AND R.U_RECORD <> 2");
			sb.append(" AND L.U_RECORD <> 2");
			sb.append(" AND C.U_RECORD <> 2");
			
			String sql = sb.toString();
			log.info("RdLinkForm后检查GLM32020sql:" + sql);
			
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
	 * @throws Exception 
	 */
	private void checkRdLink(RdLink rdLink) throws Exception {
		StringBuilder sb = new StringBuilder();

		sb.append("SELECT R.GEOMETRY,'[RD_LINK,' || R.LINK_PID || ']',R.MESH_ID");
		sb.append(" FROM RD_LINK_FORM LF, RD_LANE L, RD_LANE_CONDITION C, RD_LINK R");
		sb.append(" WHERE LF.LINK_PID = L.LINK_PID");
		sb.append(" AND R.LINK_PID = LF.LINK_PID");
		sb.append(" AND L.LINK_PID = R.LINK_PID");
		sb.append(" AND LF.FORM_OF_WAY = 22");
		sb.append(" AND L.LANE_PID = C.LANE_PID");
		sb.append(" AND C.VEHICLE NOT IN (2147484160, 2147483135)");
		sb.append(" AND R.LINK_PID = " + rdLink.getPid());
		sb.append(" AND LF.U_RECORD <> 2");
		sb.append(" AND L.U_RECORD <> 2");
		sb.append(" AND C.U_RECORD <> 2");
		
		String sql = sb.toString();
		log.info("RdLink后检查GLM32020sql:" + sql);
		
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
		//车辆类型不是只包含公交车，触发检查
		if((rdLaneCondition.getVehicle()!=2147484160L)&&(rdLaneCondition.getVehicle()!=2147483135L)){
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT 1 FROM RD_LINK_FORM LF, RD_LANE L");
			sb.append(" WHERE LF.LINK_PID = L.LINK_PID");
			sb.append(" AND LF.FORM_OF_WAY = 22");
			sb.append(" AND LF.U_RECORD <> 2");
			sb.append(" AND L.U_RECORD <> 2");
			sb.append(" AND L.LANE_PID = " + rdLaneCondition.getLanePid());

			String sql = sb.toString();
			
			log.info("RdLaneCondition后检查GLM32020sql:" + sql);
			
			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);

			if(resultList.size()>0){
				String target = "[RD_LANE," + rdLaneCondition.getLanePid() + "]";
				this.setCheckResult("", target, 0);
			}
		}
	}

	/**
	 * @param rdLane
	 * @throws Exception 
	 */
	private void checkRdLane(RdLane rdLane) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT 1 FROM RD_LINK_FORM LF, RD_LANE L, RD_LANE_CONDITION C");
		sb.append(" WHERE LF.LINK_PID = L.LINK_PID");
		sb.append(" AND LF.FORM_OF_WAY = 22");
		sb.append(" AND L.LANE_PID = C.LANE_PID");
		sb.append(" AND C.VEHICLE NOT IN(2147484160,2147483135)");
		sb.append(" AND C.U_RECORD <> 2");
		sb.append(" AND LF.U_RECORD <> 2");
		sb.append(" AND L.U_RECORD <> 2");
		sb.append(" AND L.LANE_PID = " + rdLane.getPid());

		String sql = sb.toString();
		
		log.info("RdLane后检查GLM32020sql:" + sql);
		
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if(resultList.size()>0){
			String target = "[RD_LANE," + rdLane.getPid() + "]";
			this.setCheckResult("", target, 0);
		}
	}

}
