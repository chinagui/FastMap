package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLane;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResultWithGeo;

/** 
 * @ClassName: GLM32038
 * @author songdongyan
 * @date 2016年12月9日
 * @Description: GLM32038.java
 * 车道分隔带所在的link不能是交叉点内道路，否则报log
 * 道路属性编辑后检查：RdLink,RdLinkForm
 * 新增详细车道后检查
 * 编辑详细车道后检查
 */
public class GLM32038 extends baseRule{

	/**
	 * 
	 */
	public GLM32038() {
		// TODO Auto-generated constructor stub
	}

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
			// 详细车道RdLane
			if (obj instanceof RdLane) {
				RdLane rdLane = (RdLane) obj;
				checkRdLane(rdLane,checkCommand.getOperType());
			}
			// Link属性编辑
			else if (obj instanceof RdLink) {
				RdLink rdLink = (RdLink) obj;
				checkRdLink(rdLink,checkCommand.getOperType());
			}	
			// Link属性编辑
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
		//rdLink为交叉点内道路
		if(rdLinkForm.getFormOfWay()==50){
			StringBuilder sb = new StringBuilder();

			sb.append("SELECT 1 FROM RD_LINK L,RD_LANE RL");
			sb.append(" WHERE L.LINK_PID = " + rdLinkForm.getLinkPid());
			sb.append(" AND L.LINK_PID = RL.LINK_PID");
			sb.append(" AND RL.LANE_DIVIDER <> 0");
			sb.append(" AND L.U_RECORD <> 2");
			sb.append(" AND RL.U_RECORD <> 2");

			String sql = sb.toString();
			log.info("RdLinkForm后检查GLM32038:" + sql);

			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);

			if(!resultList.isEmpty()){
				this.setCheckResult("", "[RD_LINK," + rdLinkForm.getLinkPid() + "]", 0);
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

		sb.append("SELECT R.GEOMETRY,'[RD_LINK,' || R.LINK_PID || ']',R.MESH_ID FROM RD_LINK R, RD_LINK_FORM F, RD_LANE RL ");
		sb.append(" WHERE R.LINK_PID = " + rdLink.getPid());
		sb.append(" AND R.LINK_PID = F.LINK_PID ");
		sb.append(" AND F.FORM_OF_WAY = 50");
		sb.append(" AND RL.LINK_PID = R.LINK_PID ");
		sb.append(" AND RL.LANE_DIVIDER <> 0");
		sb.append(" AND R.U_RECORD <> 2");
		sb.append(" AND F.U_RECORD <> 2");
		sb.append(" AND RL.U_RECORD <> 2");

		String sql = sb.toString();
		log.info("RdLink后检查GLM32038:" + sql);

		DatabaseOperatorResultWithGeo getObj = new DatabaseOperatorResultWithGeo();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if(!resultList.isEmpty()){
			this.setCheckResult(resultList.get(0).toString(), resultList.get(1).toString(), (int)resultList.get(2));
		}	
		
	}

	/**
	 * @param rdLane
	 * @param operType
	 * @throws Exception 
	 */
	private void checkRdLane(RdLane rdLane, OperType operType) throws Exception {
		//存在车道分隔带
		if(rdLane.getLaneDivider()!=0){
			StringBuilder sb = new StringBuilder();

			sb.append("SELECT 1 FROM RD_LINK L,RD_LINK_FORM F");
			sb.append(" WHERE L.LINK_PID = " + rdLane.getLinkPid());
			sb.append(" AND L.LINK_PID = F.LINK_PID");
			sb.append(" AND F.FORM_OF_WAY = 50");
			sb.append(" AND L.U_RECORD <> 2");
			sb.append(" AND F.U_RECORD <> 2");

			String sql = sb.toString();
			log.info("RdLane后检查GLM32038:" + sql);

			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);

			if(!resultList.isEmpty()){
				this.setCheckResult("", "[RD_LANE," + rdLane.getPid() + "]", 0);
			}	
		}
		
	}

}
