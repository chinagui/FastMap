package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

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
 * @ClassName: GLM32021
 * @author songdongyan
 * @date 2016年12月12日
 * @Description: LINK为步行街属性，详细车道车辆类型只能允许步行者、配送卡车、急救车，否则报log
 * 实现逻辑：rdLink有公交车属性，rdLaneCondition.vehicle不等于2147483786,2147483509，检查不通过
 * 道路属性编辑后检查:RdLinkForm
 * 新增详细车道后检查:RdLane
 * 修改详细车道后检查:RdLane,RdLaneCondition
 */
public class GLM32021 extends baseRule {
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
		for(IRow obj:checkCommand.getGlmList()){
//			//道路属性编辑RdLink
//			if(obj instanceof RdLink){
//				RdLink rdLink = (RdLink)obj;
//				checkRdLink(rdLink);
//			}
			//道路属性编辑RdLinkForm
			if(obj instanceof RdLinkForm){
				RdLinkForm rdLinkForm = (RdLinkForm)obj;
				checkRdLinkForm(rdLinkForm);
			}
			//新增/修改详细车道RdLane
			else if(obj instanceof RdLane){
				RdLane rdLane = (RdLane)obj;
				checkRdLane(rdLane);
			}
			//修改详细车道RdLaneCondition
			else if(obj instanceof RdLaneCondition){
				RdLaneCondition rdLaneCondition = (RdLaneCondition)obj;
				checkRdLaneCondition(rdLaneCondition);
			}
		}
		
	}

	/**
	 * @param rdLaneCondition
	 * @throws Exception 
	 */
	private void checkRdLaneCondition(RdLaneCondition rdLaneCondition) throws Exception {
		//车道车辆类型不为2147483786,2147483509触发检查
		long vehicle = 0;
		if(rdLaneCondition.changedFields().containsKey("vehicle")){
			vehicle = Integer.parseInt(rdLaneCondition.changedFields().get("vehicle").toString()) ;
		}else{
			vehicle = rdLaneCondition.getVehicle();
		}
		if(vehicle!=2147483786L&&vehicle!=2147483509L){
//		if(rdLaneCondition.getVehicle()!=2147483786L&&rdLaneCondition.getVehicle()!=2147483509L){
			StringBuilder sb = new StringBuilder();

			sb.append("SELECT 1");
			sb.append(" FROM RD_LINK_FORM F,RD_LANE A");
			sb.append(" WHERE F.LINK_PID = A.LINK_PID");
			sb.append(" AND F.FORM_OF_WAY = 20");
			sb.append(" AND F.U_RECORD <> 2") ;
			sb.append(" AND A.U_RECORD <> 2") ;
			sb.append(" AND A.LANE_PID = " + rdLaneCondition.getLanePid()) ;

			String sql = sb.toString();
			log.info("RdLane后检查GLM32021:" + sql);

			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);

			if(!resultList.isEmpty()){
				this.setCheckResult("", "[RD_LANE,"+rdLaneCondition.getLanePid()+"]", 0);
			}
		}
		
	}

	/**
	 * @param rdLane
	 * @throws Exception 
	 */
	private void checkRdLane(RdLane rdLane) throws Exception {
		StringBuilder sb = new StringBuilder();

		sb.append("SELECT 1");
		sb.append(" FROM RD_LINK_FORM F,RD_LANE A,RD_LANE_CONDITION C");
		sb.append(" WHERE F.LINK_PID = A.LINK_PID");
		sb.append(" AND F.FORM_OF_WAY = 20");
		sb.append(" AND A.LANE_PID = C.LANE_PID");
		sb.append(" AND C.VEHICLE NOT IN (2147483786,2147483509)");
		sb.append(" AND F.U_RECORD <> 2") ;
		sb.append(" AND A.U_RECORD <> 2") ;
		sb.append(" AND C.U_RECORD <> 2") ;
		sb.append(" AND A.LANE_PID = " + rdLane.getPid()) ;

		String sql = sb.toString();
		log.info("RdLane后检查GLM32021:" + sql);

		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if(!resultList.isEmpty()){
			this.setCheckResult("", "[RD_LANE,"+rdLane.getPid()+"]", 0);
		}
		
	}

	/**
	 * @param rdLinkForm
	 * @throws Exception 
	 */
	private void checkRdLinkForm(RdLinkForm rdLinkForm) throws Exception {
		//道路属性为20，触发检查
		int formOfWay = 0;
		if(rdLinkForm.changedFields().containsKey("formOfWay")){
			formOfWay = Integer.parseInt(rdLinkForm.changedFields().get("formOfWay").toString()) ;
		}else{
			formOfWay = rdLinkForm.getFormOfWay();
		}
		if(formOfWay==20){	
			StringBuilder sb = new StringBuilder();

			sb.append("SELECT 1");
			sb.append(" FROM RD_LANE A,RD_LANE_CONDITION C");
			sb.append(" WHERE A.LANE_PID = C.LANE_PID");
			sb.append(" AND C.VEHICLE NOT IN (2147483786,2147483509)");
			sb.append(" AND A.U_RECORD <> 2") ;
			sb.append(" AND C.U_RECORD <> 2") ;
			sb.append(" AND A.LINK_PID = " + rdLinkForm.getLinkPid()) ;

			String sql = sb.toString();
			log.info("RdLinkForm后检查GLM32021:" + sql);

			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);

			if(!resultList.isEmpty()){
				this.setCheckResult("", "[RD_LINK,"+rdLinkForm.getLinkPid()+"]", 0);
			}

		}
		
	}

	/**
	 * @param rdLink
	 * @throws Exception 
	 */
	private void checkRdLink(RdLink rdLink) throws Exception {
		StringBuilder sb = new StringBuilder();

		sb.append("SELECT L.GEOMETRY,'[RD_LINK,' || L.LINK_PID || ']',L.MESH_ID");
		sb.append(" FROM RD_LINK L,RD_LINK_FORM F,RD_LANE A,RD_LANE_CONDITION C");
		sb.append(" WHERE L.LINK_PID = A.LINK_PID");
		sb.append(" AND L.LINK_PID = F.LINK_PID");
		sb.append(" AND F.FORM_OF_WAY = 20");
		sb.append(" AND A.LANE_PID = C.LANE_PID");
		sb.append(" AND C.VEHICLE NOT IN (2147483786,2147483509)");
		sb.append(" AND L.U_RECORD <> 2");
		sb.append(" AND F.U_RECORD <> 2") ;
		sb.append(" AND A.U_RECORD <> 2") ;
		sb.append(" AND C.U_RECORD <> 2") ;
		sb.append(" AND L.LINK_PID = " + rdLink.getPid()) ;

		String sql = sb.toString();
		log.info("RdLink后检查GLM32021:" + sql);

		DatabaseOperatorResultWithGeo getObj = new DatabaseOperatorResultWithGeo();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if(!resultList.isEmpty()){
			this.setCheckResult(resultList.get(0).toString(), resultList.get(1).toString(), (int)resultList.get(2));
		}
		
	}

}
