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
 * @ClassName: GLM32005
 * @author songdongyan
 * @date 2016年12月9日
 * @Description: 单方向道路车道方向值只能为“无”，否则报log
 * 道路方向编辑：如果道路方向为单向，出发检查；如果有车道方向值不为“无”，则检查不通过
 * 新增详细车道：车道所在link方向为单向且车道方向值不为“无”，则检查不通过
 * 修改详细车道：车道所在link方向为单向且车道方向值不为“无”，则检查不通过            
 */
public class GLM32005 extends baseRule{

	/**
	 * 
	 */
	public GLM32005() {
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
				checkRdLane(rdLane);
			}
			// Link方向编辑
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
		if(rdLink.changedFields().containsKey("direct")){
			int direct = Integer.parseInt(rdLink.changedFields().get("direct").toString()) ;
			//非单向道路，不触发检查
			if(direct!=2&&direct!=3){
				return;
			}
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT R.GEOMETRY,'[RD_LINK,' || R.LINK_PID || ']',R.MESH_ID FROM RD_LINK R ");
			sb.append("WHERE R.LINK_PID = " + rdLink.getPid());
			sb.append("AND R.U_RECORD <> 2 ");
			sb.append("AND EXISTS( ");
			sb.append("SELECT 1 FROM RD_LANE L ");
			sb.append("WHERE L.U_RECORD <> 2 ");
			sb.append("AND L.LANE_DIR <> 1 )");

			String sql = sb.toString();
			log.info("RdLink后检查GLM32005:" + sql);

			DatabaseOperatorResultWithGeo getObj = new DatabaseOperatorResultWithGeo();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);

			if(!resultList.isEmpty()){
				this.setCheckResult(resultList.get(0).toString(), resultList.get(1).toString(), (int)resultList.get(2));
			}	
		}	
	}

	/**
	 * @param rdLane
	 * @param operType
	 * @throws Exception 
	 */
	private void checkRdLane(RdLane rdLane) throws Exception {
		//如果车道方向不为1（无），触发检查
		int laneDir = 1;
		if(rdLane.changedFields().containsKey("laneDir")){
			laneDir = Integer.parseInt(rdLane.changedFields().get("laneDir").toString()) ;
		}else{
			laneDir = rdLane.getLaneDivider();
		}
		if(laneDir!=1){
//		if(rdLane.getLaneDir()!=1){
			StringBuilder sb = new StringBuilder();

			sb.append("SELECT 1 FROM RD_LINK L1");
			sb.append(" WHERE L1.LINK_PID = " + rdLane.getLinkPid());
			sb.append(" AND L1.DIRECT IN (2, 3)");
			sb.append(" AND L1.U_RECORD <> 2");

			String sql = sb.toString();
			log.info("RdLane后检查GLM32005:" + sql);

			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);

			if(!resultList.isEmpty()){
				this.setCheckResult("", "[RD_LANE," + rdLane.getPid() + "]", 0);
			}	
		}
	}

}
