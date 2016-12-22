package com.navinfo.dataservice.engine.check.rules;


import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLane;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResultWithGeo;


/** 
 * @ClassName: GLM32006
 * @author songdongyan
 * @date 2016年12月10日
 * @Description: 双方向道路车道方向只能为“顺方向”或“逆方向”，否则报log
 * 道路方向编辑服务端后检查：RdLink
 * 新增详细车道服务端后检查:RdLane
 * 修改详细车道服务端后检查:RdLane
 */
public class GLM32006 extends baseRule {

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
		for(IRow row:checkCommand.getGlmList()){
			//新增详细车道、修改详细车道
			if(row instanceof RdLane){
				RdLane rdLane = (RdLane)row;
				checkRdLane(rdLane);
			}
			//道路方向编辑
			if(row instanceof RdLink){
				RdLink rdLink = (RdLink)row;
				checkRdLink(rdLink);
			}
		}
		
	}

	/**
	 * @param rdLink
	 * @throws Exception 
	 */
	private void checkRdLink(RdLink rdLink) throws Exception {
		//RdLink为双方向，触发检查
		if(rdLink.getDirect()==1){
			StringBuilder sb = new StringBuilder();
			
			sb.append("SELECT R.GEOMETRY,'[RD_LINK,' || R.LINK_PID || ']',R.MESH_ID");
			sb.append(" FROM RD_LINK R, RD_LANE A");
			sb.append(" WHERE R.LINK_PID = A.LINK_PID");
			sb.append(" AND A.LANE_DIR <> 1");
			sb.append(" AND A.U_RECORD <> 2");
			sb.append(" AND R.LINK_PID =" + rdLink.getPid());
			
			String sql = sb.toString();
			log.info("RdLink后检查GLM32006sql:" + sql);
			
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
	 * @throws Exception 
	 */
	private void checkRdLane(RdLane rdLane) throws Exception {
		//车道方向为“无”，触发检查
		if(rdLane.getLaneDir()==1){
			StringBuilder sb = new StringBuilder();

			sb.append("SELECT 1 FROM RD_LINK L");
			sb.append(" WHERE L.LINK_PID = " + rdLane.getLinkPid());
			sb.append(" AND L.DIRECT = 1");
			sb.append(" AND L.U_RECORD <> 2");
			
			String sql = sb.toString();
			log.info("RdLane后检查GLM32006sql:" + sql);
			
			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);

			if(resultList.size()>0){
				String target = "[RD_LANE," + rdLane.getPid() + "]";
				this.setCheckResult("", target, 0);
			}
		}
	}

}
