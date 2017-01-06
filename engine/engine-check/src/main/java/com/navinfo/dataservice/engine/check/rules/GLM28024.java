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
		
	}

	/**
	 * @param rdObjectLink
	 * @throws Exception 
	 */
	private void checkRdObjectLink(RdObjectLink rdObjectLink) throws Exception {
		//所有涉及的link
		String sql = "SELECT RIL.LINK_PID FROM RD_OBJECT_INTER ROI,RD_INTER_LINK RIL"
				+ " WHERE ROI.INTER_PID = RIL.PID"
				+ " AND ROI.U_RECORD <> 2"
				+ " AND RIL.U_RECORD <> 2"
				+ " AND ROI.PID = " + rdObjectLink.getPid()
				+ " UNION"
				+ " SELECT RRL.LINK_PID FROM RD_OBJECT_ROAD ROR,RD_ROAD_LINK RRL"
				+ " WHERE ROR.ROAD_PID = RRL.PID"
				+ " AND ROR.U_RECORD <> 2"
				+ " AND RRL.U_RECORD <> 2"
				+ " AND ROR.PID = " + rdObjectLink.getPid()
				+ " UNION"
				+ " SELECT ROL.LINK_PID FROM RD_OBJECT_LINK ROL"
				+ " WHERE ROL.U_RECORD <> 2"
				+ " AND ROL.PID = " + rdObjectLink.getPid();
		
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
		log.info("RdObjectLink前检查GLM28024:" + sql2);
		
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql2);
		
		if(resultList.size()>0){
			String target = "[RD_OBJECT," + rdObjectLink.getPid() + "]";
			this.setCheckResult("", target, 0);
		}
		
	}

	/**
	 * @param rdObjectInter
	 * @throws Exception 
	 */
	private void checkRdObjectInter(RdObjectInter rdObjectInter) throws Exception {
		//所有涉及的link
		String sql = "SELECT RIL.LINK_PID FROM RD_OBJECT_INTER ROI,RD_INTER_LINK RIL"
				+ " WHERE ROI.INTER_PID = RIL.PID"
				+ " AND ROI.U_RECORD <> 2"
				+ " AND RIL.U_RECORD <> 2"
				+ " AND ROI.PID = " + rdObjectInter.getPid()
				+ " UNION"
				+ " SELECT RRL.LINK_PID FROM RD_OBJECT_ROAD ROR,RD_ROAD_LINK RRL"
				+ " WHERE ROR.ROAD_PID = RRL.PID"
				+ " AND ROR.U_RECORD <> 2"
				+ " AND RRL.U_RECORD <> 2"
				+ " AND ROR.PID = " + rdObjectInter.getPid()
				+ " UNION"
				+ " SELECT ROL.LINK_PID FROM RD_OBJECT_LINK ROL"
				+ " WHERE ROL.U_RECORD <> 2"
				+ " AND ROL.PID = " + rdObjectInter.getPid();
		
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
		log.info("RdObjectInter前检查GLM28024:" + sql2);
		
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql2);
		
		if(resultList.size()>0){
			String target = "[RD_OBJECT," + rdObjectInter.getPid() + "]";
			this.setCheckResult("", target, 0);
		}
		
	}

	/**
	 * @param rdObjectRoad
	 * @throws Exception 
	 */
	private void checkRdObjectRoad(RdObjectRoad rdObjectRoad) throws Exception {
		//所有涉及的link
		String sql = "SELECT RIL.LINK_PID FROM RD_OBJECT_INTER ROI,RD_INTER_LINK RIL"
				+ " WHERE ROI.INTER_PID = RIL.PID"
				+ " AND ROI.U_RECORD <> 2"
				+ " AND RIL.U_RECORD <> 2"
				+ " AND ROI.PID = " + rdObjectRoad.getPid()
				+ " UNION"
				+ " SELECT RRL.LINK_PID FROM RD_OBJECT_ROAD ROR,RD_ROAD_LINK RRL"
				+ " WHERE ROR.ROAD_PID = RRL.PID"
				+ " AND ROR.U_RECORD <> 2"
				+ " AND RRL.U_RECORD <> 2"
				+ " AND ROR.PID = " + rdObjectRoad.getPid()
				+ " UNION"
				+ " SELECT ROL.LINK_PID FROM RD_OBJECT_LINK ROL"
				+ " WHERE ROL.U_RECORD <> 2"
				+ " AND ROL.PID = " + rdObjectRoad.getPid();
		
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
		log.info("RdObjectRoad前检查GLM28024:" + sql2);
		
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql2);
		
		if(resultList.size()>0){
			String target = "[RD_OBJECT," + rdObjectRoad.getPid() + "]";
			this.setCheckResult("", target, 0);
		}
		
	}

	/**
	 * @param rdObject
	 * @throws Exception 
	 */
	private void checkRdObject(RdObject rdObject) throws Exception {
		if(rdObject.status().equals(ObjStatus.INSERT)){
			//所有涉及的link
			String sql = "SELECT RIL.LINK_PID FROM RD_OBJECT_INTER ROI,RD_INTER_LINK RIL"
					+ " WHERE ROI.INTER_PID = RIL.PID"
					+ " AND ROI.U_RECORD <> 2"
					+ " AND RIL.U_RECORD <> 2"
					+ " AND ROI.PID = " + rdObject.getPid()
					+ " UNION"
					+ " SELECT RRL.LINK_PID FROM RD_OBJECT_ROAD ROR,RD_ROAD_LINK RRL"
					+ " WHERE ROR.ROAD_PID = RRL.PID"
					+ " AND ROR.U_RECORD <> 2"
					+ " AND RRL.U_RECORD <> 2"
					+ " AND ROR.PID = " + rdObject.getPid()
					+ " UNION"
					+ " SELECT ROL.LINK_PID FROM RD_OBJECT_LINK ROL"
					+ " WHERE ROL.U_RECORD <> 2"
					+ " AND ROL.PID = " + rdObject.getPid();
			
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
			log.info("RdObject前检查GLM28024:" + sql2);
			
			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql2);
			
			if(resultList.size()>0){
				String target = "[RD_OBJECT," + rdObject.getPid() + "]";
				this.setCheckResult("", target, 0);
			}
		}
		
		
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#postCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
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

}
