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
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: GLM28026
 * @author songdongyan
 * @date 2017年1月5日
 * @Description:一个Node的所有接续link中，不能存在大于1个的CRFO号码
 * 新增CRFO前检查:RdObject
 * 拓扑编辑CRFO:新增RdObjectInter,RdObjectRoad,RdObjectLink
 */
public class GLM28026 extends baseRule{

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#preCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for(IRow obj : checkCommand.getGlmList()){
			//RdRoad新增触发
			if (obj instanceof RdObject){
				RdObject rdObject = (RdObject)obj;
				checkRdObject(rdObject);
			}
			//RdObjectInter新增会触发
			else if (obj instanceof RdObjectInter){
				RdObjectInter rdObjectInter = (RdObjectInter)obj;
				checkRdObjectInter(rdObjectInter);
			}
			//RdObjectRoad新增会触发
			else if (obj instanceof RdObjectRoad){
				RdObjectRoad rdObjectRoad = (RdObjectRoad)obj;
				checkRdObjectRoad(rdObjectRoad);
			}
			//RdObjectLink新增会触发
			else if (obj instanceof RdObjectLink){
				RdObjectLink rdObjectLink = (RdObjectLink)obj;
				checkRdObjectLink(rdObjectLink);
			}

		}
		
	}

	/**
	 * @param rdObjectLink
	 * @throws Exception 
	 */
	private void checkRdObjectLink(RdObjectLink rdObjectLink) throws Exception {
		if(rdObjectLink.status().equals(ObjStatus.INSERT)){
			String sql = "SELECT DISTINCT RIL2.PID FROM RD_ROAD_LINK RRL1, RD_INTER_LINK RIL2"
					+ " WHERE RRL1.U_RECORD <> 2"
					+ " AND RIL2.U_RECORD <> 2"
					+ " AND RRL1.PID <> RIL2.PID"
					+ " AND RRL1.LINK_PID = RIL2.LINK_PID"
					+ " AND RRL1.LINK_PID = " + rdObjectLink.getLinkPid()
					+ " UNION "
					+ " SELECT DISTINCT RRL2.PID"
					+ " FROM RD_ROAD_LINK RRL1, RD_ROAD_LINK RRL2"
					+ " WHERE RRL1.U_RECORD <> 2"
					+ " AND RRL2.U_RECORD <> 2"
					+ " AND RRL1.PID <> RRL2.PID"
					+ " AND RRL1.LINK_PID = RRL2.LINK_PID"
					+ " AND RRL1.LINK_PID = " + rdObjectLink.getLinkPid();
			
			log.info("RdObjectLink前检查GLM28026:" + sql);
			
			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);
			
			if(resultList.size()>0){
				this.setCheckResult("", "", 0);
			}
		}	
		
	}

	/**
	 * @param rdObjectRoad
	 * @throws Exception 
	 */
	private void checkRdObjectRoad(RdObjectRoad rdObjectRoad) throws Exception {
		if(rdObjectRoad.status().equals(ObjStatus.INSERT)){
			String sql = "SELECT DISTINCT RIL2.PID FROM RD_ROAD_LINK RRL1, RD_INTER_LINK RIL2"
					+ " WHERE RRL1.U_RECORD <> 2"
					+ " AND RIL2.U_RECORD <> 2"
					+ " AND RRL1.PID <> RIL2.PID"
					+ " AND RRL1.LINK_PID = RIL2.LINK_PID"
					+ " AND RRL1.PID = " + rdObjectRoad.getRoadPid()
					+ " UNION "
					+ " SELECT DISTINCT RRL2.PID"
					+ " FROM RD_ROAD_LINK RRL1, RD_ROAD_LINK RRL2"
					+ " WHERE RRL1.U_RECORD <> 2"
					+ " AND RRL2.U_RECORD <> 2"
					+ " AND RRL1.PID <> RRL2.PID"
					+ " AND RRL1.LINK_PID = RRL2.LINK_PID"
					+ " AND RRL1.PID = " + rdObjectRoad.getRoadPid();
			
			log.info("RdObjectRoad前检查GLM28026:" + sql);
			
			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);
			
			if(resultList.size()>0){
				this.setCheckResult("", "", 0);
			}
		}	
		
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
				String sqlT = "SELECT DISTINCT RIL2.PID FROM RD_INTER_LINK RIL1, RD_INTER_LINK RIL2"
						+ " WHERE RIL1.U_RECORD <> 2"
						+ " AND RIL2.U_RECORD <> 2"
						+ " AND RIL1.PID <> RIL2.PID"
						+ " AND RIL1.LINK_PID = RIL2.LINK_PID"
						+ " AND RIL1.PID  IN(" + StringUtils.join(interPidSet.toArray(),",") + ")"
						+ " UNION "
						+ " SELECT DISTINCT RRL.PID"
						+ " FROM RD_INTER_LINK RIL1, RD_ROAD_LINK RRL"
						+ " WHERE RIL1.U_RECORD <> 2"
						+ " AND RRL.U_RECORD <> 2"
						+ " AND RIL1.PID <> RRL.PID"
						+ " AND RIL1.LINK_PID = RRL.LINK_PID"
						+ " AND RIL1.PID IN (" + StringUtils.join(interPidSet.toArray(),",") + ")";
				sqlList.add(sqlT);
			}
			if(!roadPidSet.isEmpty()){
				String sqlT = "SELECT DISTINCT RIL2.PID FROM RD_ROAD_LINK RRL1, RD_INTER_LINK RIL2"
						+ " WHERE RRL1.U_RECORD <> 2"
						+ " AND RIL2.U_RECORD <> 2"
						+ " AND RRL1.PID <> RIL2.PID"
						+ " AND RRL1.LINK_PID = RIL2.LINK_PID"
						+ " AND RRL1.PID  IN(" + StringUtils.join(roadPidSet.toArray(),",") + ")"
						+ " UNION "
						+ " SELECT DISTINCT RRL2.PID"
						+ " FROM RD_ROAD_LINK RRL1, RD_ROAD_LINK RRL2"
						+ " WHERE RRL1.U_RECORD <> 2"
						+ " AND RRL2.U_RECORD <> 2"
						+ " AND RRL1.PID <> RRL2.PID"
						+ " AND RRL1.LINK_PID = RRL2.LINK_PID"
						+ " AND RRL1.PID IN (" + StringUtils.join(roadPidSet.toArray(),",") + ")";
				sqlList.add(sqlT);
			}
			if(!linkPidSet.isEmpty()){
				String sqlT = "SELECT DISTINCT RIL2.PID FROM RD_INTER_LINK RIL2"
						+ " WHERE RIL2.PID <> " + rdObject.getPid()
						+ " AND RIL2.U_RECORD <> 2"
						+ " AND RIL2.LINK_PID IN(" + StringUtils.join(linkPidSet.toArray(),",") + ")"
						+ " UNION "
						+ " SELECT DISTINCT RRL2.PID"
						+ " FROM RD_ROAD_LINK RRL2"
						+ " WHERE RRL2.U_RECORD <> 2"
						+ " AND RRL2.PID <> " + rdObject.getPid()
						+ " AND RRL2.LINK_PID IN (" + StringUtils.join(linkPidSet.toArray(),",") + ")";
				sqlList.add(sqlT);
			}
			
			
			sql = StringUtils.join(sqlList.toArray()," UNION ");
			log.info("RdObject前检查GLM28026:" + sql);
			
			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);
			
			if(resultList.size()>0){
				this.setCheckResult("", "", 0);
			}
		}
		
	}

	/**
	 * @param rdObjectInter
	 * @throws Exception 
	 */
	private void checkRdObjectInter(RdObjectInter rdObjectInter) throws Exception {
		if(rdObjectInter.status().equals(ObjStatus.INSERT)){
			String sql = "SELECT DISTINCT RIL2.PID FROM RD_INTER_LINK RIL1, RD_INTER_LINK RIL2"
						+ " WHERE RIL1.U_RECORD <> 2"
						+ " AND RIL2.U_RECORD <> 2"
						+ " AND RIL1.PID <> RIL2.PID"
						+ " AND RIL1.LINK_PID = RIL2.LINK_PID"
						+ " AND RIL1.PID =" + rdObjectInter.getInterPid()
						+ " UNION "
						+ " SELECT DISTINCT RRL.PID"
						+ " FROM RD_INTER_LINK RIL1, RD_ROAD_LINK RRL"
						+ " WHERE RIL1.U_RECORD <> 2"
						+ " AND RRL.U_RECORD <> 2"
						+ " AND RIL1.PID <> RRL.PID"
						+ " AND RIL1.LINK_PID = RRL.LINK_PID"
						+ " AND RIL1.PID IN =" + rdObjectInter.getInterPid();
			
			log.info("RdObjectInter前检查GLM28026:" + sql);
			
			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);
			
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
