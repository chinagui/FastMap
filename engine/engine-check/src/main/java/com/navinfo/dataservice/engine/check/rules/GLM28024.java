package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.crf.RdObject;
import com.navinfo.dataservice.dao.glm.model.rd.crf.RdObjectInter;
import com.navinfo.dataservice.dao.glm.model.rd.crf.RdObjectLink;
import com.navinfo.dataservice.dao.glm.model.rd.crf.RdObjectRoad;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.road.RdRoad;
import com.navinfo.dataservice.dao.glm.model.rd.road.RdRoadLink;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: GLM28024
 * @author songdongyan
 * @date 2017年1月5日
 * @Description: 如果一条LINK两端所挂接的接续LINK属于同一个CRFO，则这条LINK也要属于这个CRFO，否则报log
 * 新增CRFO:RdObject
 * 修改CRFO:RdObjectRoad,RdObjectInter,RdObjectLink
 */
public class GLM28024 extends baseRule{

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#preCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for(IRow obj : checkCommand.getGlmList()){
			if (obj instanceof RdObject){
				RdObject rdObject = (RdObject)obj;
				checkRdObject(rdObject);
			}
			else if (obj instanceof RdObjectRoad){
				RdObjectRoad rdObjectRoad = (RdObjectRoad)obj;
				checkRdObjectRoad(rdObjectRoad);
			}
			else if (obj instanceof RdObjectInter){
				RdObjectInter rdObjectInter = (RdObjectInter)obj;
				checkRdObjectInter(rdObjectInter);
			}
			else if (obj instanceof RdObjectLink){
				RdObjectLink rdObjectLink = (RdObjectLink)obj;
				checkRdObjectLink(rdObjectLink);
			}
		}
		
	}

	/**
	 * @param rdObjectLink
	 */
	private void checkRdObjectLink(RdObjectLink rdObjectLink) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @param rdObjectInter
	 */
	private void checkRdObjectInter(RdObjectInter rdObjectInter) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @param rdObjectRoad
	 */
	private void checkRdObjectRoad(RdObjectRoad rdObjectRoad) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @param rdObject
	 * @throws Exception 
	 */
	private void checkRdObject(RdObject rdObject) throws Exception {
		if(rdObject.status().equals(ObjStatus.INSERT)){
			
			Set<Integer> interPidSet = new HashSet<Integer>();
			Set<Integer> roadPidSet = new HashSet<Integer>();
			Set<Integer> linkPidSet = new HashSet<Integer>();
			
			for(IRow iRow:rdObject.getInters()){
				RdObjectInter rdObjectInter = (RdObjectInter)iRow;
				interPidSet.add(rdObjectInter.getInterPid());
			}
			for(IRow iRow:rdObject.getRoads()){
				RdObjectRoad rdObjectRoad = (RdObjectRoad)iRow;
				roadPidSet.add(rdObjectRoad.getRoadPid());
			}
			
			for(IRow iRow:rdObject.getLinks()){
				RdLink rdLink = (RdLink)iRow;
				linkPidSet.add(rdLink.getPid());
			}
			if(interPidSet.isEmpty()&&roadPidSet.isEmpty()&&linkPidSet.isEmpty()){
				return;
			}
			
			String sql = "";
			List<String> sqlList = new ArrayList<String>();
			if(!interPidSet.isEmpty()){
				String sqlT = "SELECT RIL.LINK_PID FROM RD_INTER_LINK RIL"
						+ " WHERE RIL.U_RECORD <> 2"
						+ " AND RIL.PID IN (" + StringUtils.join(interPidSet.toArray(),",") + ")";
				sqlList.add(sqlT);
			}
			if(!roadPidSet.isEmpty()){
				String sqlT = "SELECT RRL.LINK_PID FROM RD_ROAD_LINK RRL"
						+ " WHERE RRL.U_RECORD <> 2"
						+ " AND RRL.PID IN (" + StringUtils.join(roadPidSet.toArray(),",") + ")";
				sqlList.add(sqlT);
			}
			if(!linkPidSet.isEmpty()){
				String sqlT = "SELECT R.LINK_PID FROM RD_LINK R"
						+ " WHERE R.U_RECORD <> 2"
						+ " AND R.LINK_PID IN (" + StringUtils.join(linkPidSet.toArray(),",") + ")"	;					
				sqlList.add(sqlT);
			}

			sql = StringUtils.join(sqlList.toArray()," UNION ");
			log.info("RdObject前检查GLM28024:" + sql);

			StringBuilder sb = new StringBuilder();

			sb.append("SELECT 1 FROM RD_LINK R1,RD_LINK R2,RD_LINK R3");
			sb.append(" WHERE (R1.S_NODE_PID = R2.S_NODE_PID OR R1.S_NODE_PID = R2.E_NODE_PID)");
			sb.append(" AND(R1.E_NODE_PID = R3.S_NODE_PID OR R1.E_NODE_PID = R3.E_NODE_PID)");
			sb.append(" AND R1.U_RECORD <> 2");
			sb.append(" AND R2.U_RECORD <> 2");
			sb.append(" AND R3.U_RECORD <> 2");
			sb.append(" AND R2.LINK_PID <> R3.LINK_PID");
			sb.append(" AND R1.LINK_PID <> R2.LINK_PID");
			sb.append(" AND R1.LINK_PID <> R3.LINK_PID");
			sb.append(" AND R2.LINK_PID IN (" + sql + ")");
			sb.append(" AND R3.LINK_PID IN (" + sql + ")");
			sb.append(" AND R1.LINK_PID NOT IN (" + sql + ")");
			String sql2 = sb.toString();
			log.info("RdInter前检查GLM28024:" + sql2);
			
			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql2);
			
			if(resultList.size()>0){
				this.setCheckResult("", "", 0);
			}
		}
		
		
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#postCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
