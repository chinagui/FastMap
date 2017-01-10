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
			//所有涉及的node
			String sql = "SELECT R.S_NODE_PID NODE_PID FROM RD_LINK R"
						+ " WHERE R.U_RECORD <> 2"
						+ " AND R.LINK_PID =" + rdObjectLink.getLinkPid()
						+ " UNION "
						+ " SELECT R.E_NODE_PID NODE_PID FROM RD_LINK R"
						+ " WHERE R.U_RECORD <> 2"
						+ " AND R.LINK_PID =" + rdObjectLink.getLinkPid() ;
			
			//所有涉及的link
			String sql_p = "SELECT RR.LINK_PID FROM RD_LINK RR WHERE RR.S_NODE_PID IN (" + sql +")"
					+ " UNION"
					+ " SELECT RR.LINK_PID FROM RD_LINK RR WHERE RR.E_NODE_PID IN (" + sql +")";
			
			//所涉及link是不是属于其他RD_OBJECT
			StringBuilder sb = new StringBuilder();

			sb.append("SELECT ROI.PID FROM RD_OBJECT_INTER ROI,RD_INTER_LINK RIL1");
			sb.append(" WHERE ROI.U_RECORD <> 2");
			sb.append(" AND RIL1.U_RECORD <> 2");
			sb.append(" AND RIL1.PID = ROI.INTER_PID");
			sb.append(" AND ROI.PID <>" + rdObjectLink.getPid());
			sb.append(" AND RIL1.LINK_PID IN (" + sql_p + ")");
			sb.append(" UNION");
			sb.append(" SELECT ROR.PID FROM RD_OBJECT_ROAD ROR,RD_ROAD_LINK RRL1");
			sb.append(" WHERE ROR.U_RECORD <> 2");
			sb.append(" AND RRL1.U_RECORD <> 2");
			sb.append(" AND ROR.ROAD_PID = RRL1.PID");
			sb.append(" AND ROR.PID <>" + rdObjectLink.getPid());
			sb.append(" AND RRL1.LINK_PID IN (" + sql_p + ")");
			sb.append(" UNION");
			sb.append(" SELECT ROL.PID FROM RD_OBJECT_LINK ROL");
			sb.append(" WHERE ROL.U_RECORD <> 2");
			sb.append(" AND ROL.PID <>" + rdObjectLink.getPid());
			sb.append(" AND ROL.LINK_PID IN (" + sql_p + ")");

			String sqlCheck = sb.toString();
			log.info("RdObjectLink后检查GLM28026:" + sqlCheck);
			
			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sqlCheck);
			
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
			//所有涉及的node
			String sql = "SELECT R.S_NODE_PID NODE_PID FROM RD_ROAD_LINK RRL,RD_LINK R"
						+ " WHERE RRL.U_RECORD <> 2"
						+ " AND R.U_RECORD <> 2"
						+ " AND RRL.LINK_PID = R.LINK_PID"
						+ " AND RRL.PID =" + rdObjectRoad.getRoadPid()
						+ " UNION "
						+ " SELECT R.E_NODE_PID NODE_PID FROM RD_ROAD_LINK RRL,RD_LINK R"
						+ " WHERE RRL.U_RECORD <> 2"
						+ " AND R.U_RECORD <> 2"
						+ " AND RRL.LINK_PID = R.LINK_PID"
						+ " AND RRL.PID =" + rdObjectRoad.getRoadPid();
				
			//所有涉及的link
			String sql_p = "SELECT RR.LINK_PID FROM RD_LINK RR WHERE RR.S_NODE_PID IN (" + sql +")"
					+ " UNION"
					+ " SELECT RR.LINK_PID FROM RD_LINK RR WHERE RR.E_NODE_PID IN (" + sql +")";
			
			//所涉及link是不是属于其他RD_OBJECT
			StringBuilder sb = new StringBuilder();

			sb.append("SELECT ROI.PID FROM RD_OBJECT_INTER ROI,RD_INTER_LINK RIL1");
			sb.append(" WHERE ROI.U_RECORD <> 2");
			sb.append(" AND RIL1.U_RECORD <> 2");
			sb.append(" AND RIL1.PID = ROI.INTER_PID");
			sb.append(" AND ROI.PID <>" + rdObjectRoad.getPid());
			sb.append(" AND RIL1.LINK_PID IN (" + sql_p + ")");
			sb.append(" UNION");
			sb.append(" SELECT ROR.PID FROM RD_OBJECT_ROAD ROR,RD_ROAD_LINK RRL1");
			sb.append(" WHERE ROR.U_RECORD <> 2");
			sb.append(" AND RRL1.U_RECORD <> 2");
			sb.append(" AND ROR.ROAD_PID = RRL1.PID");
			sb.append(" AND ROR.PID <>" + rdObjectRoad.getPid());
			sb.append(" AND RRL1.LINK_PID IN (" + sql_p + ")");
			sb.append(" UNION");
			sb.append(" SELECT ROL.PID FROM RD_OBJECT_LINK ROL");
			sb.append(" WHERE ROL.U_RECORD <> 2");
			sb.append(" AND ROL.PID <>" + rdObjectRoad.getPid());
			sb.append(" AND ROL.LINK_PID IN (" + sql_p + ")");

			String sqlCheck = sb.toString();
			log.info("RdObjectRoad前检查GLM28026:" + sqlCheck);
			
			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sqlCheck);
			
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
				RdObjectLink rdLink = (RdObjectLink)iRow;
				linkPidSet.add(rdLink.getLinkPid());
			}
			if(interPidSet.isEmpty()&&roadPidSet.isEmpty()&&linkPidSet.isEmpty()){
				return;
			}
			
			//所有涉及的node
			String sql = "";
			List<String> sqlList = new ArrayList<String>();
			if(!interPidSet.isEmpty()){
				String sqlT = "SELECT R.S_NODE_PID NODE_PID FROM RD_INTER_LINK RIL,RD_LINK R"
						+ " WHERE RIL.U_RECORD <> 2"
						+ " AND R.U_RECORD <> 2"
						+ " AND RIL.LINK_PID = R.LINK_PID"
						+ " AND RIL.PID  IN(" + StringUtils.join(interPidSet.toArray(),",") + ")"
						+ " UNION "
						+ " SELECT R.E_NODE_PID NODE_PID FROM RD_INTER_LINK RIL,RD_LINK R"
						+ " WHERE RIL.U_RECORD <> 2"
						+ " AND R.U_RECORD <> 2"
						+ " AND RIL.LINK_PID = R.LINK_PID"
						+ " AND RIL.PID  IN(" + StringUtils.join(interPidSet.toArray(),",") + ")";
				sqlList.add(sqlT);
			}
			if(!roadPidSet.isEmpty()){
				String sqlT = "SELECT R.S_NODE_PID NODE_PID FROM RD_ROAD_LINK RRL,RD_LINK R"
						+ " WHERE RRL.U_RECORD <> 2"
						+ " AND R.U_RECORD <> 2"
						+ " AND RRL.LINK_PID = R.LINK_PID"
						+ " AND RRL.PID  IN(" + StringUtils.join(roadPidSet.toArray(),",") + ")"
						+ " UNION "
						+ " SELECT R.E_NODE_PID NODE_PID FROM RD_ROAD_LINK RRL,RD_LINK R"
						+ " WHERE RRL.U_RECORD <> 2"
						+ " AND R.U_RECORD <> 2"
						+ " AND RRL.LINK_PID = R.LINK_PID"
						+ " AND RRL.PID  IN(" + StringUtils.join(roadPidSet.toArray(),",") + ")";
				sqlList.add(sqlT);
			}
			if(!linkPidSet.isEmpty()){
				String sqlT = "SELECT R.S_NODE_PID NODE_PID FROM RD_LINK R"
						+ " WHERE R.U_RECORD <> 2"
						+ " AND R.LINK_PID IN (" + StringUtils.join(linkPidSet.toArray(),",") + ")" 
						+ " UNION "
						+ " SELECT R.E_NODE_PID NODE_PID FROM RD_LINK R"
						+ " WHERE R.U_RECORD <> 2"
						+ " AND R.LINK_PID IN (" + StringUtils.join(linkPidSet.toArray(),",") + ")" ;
				sqlList.add(sqlT);
			}
			
			
			sql = StringUtils.join(sqlList.toArray()," UNION ");
			//所有涉及的link
			String sql_p = "SELECT RR.LINK_PID FROM RD_LINK RR WHERE RR.S_NODE_PID IN (" + sql +")"
					+ " UNION"
					+ " SELECT RR.LINK_PID FROM RD_LINK RR WHERE RR.E_NODE_PID IN (" + sql +")";
			log.info("RdObject前检查GLM28026:" + sql_p);
			
			//所涉及link是不是属于其他RD_OBJECT
			StringBuilder sb = new StringBuilder();

			sb.append("SELECT ROI.PID FROM RD_OBJECT_INTER ROI,RD_INTER_LINK RIL1");
			sb.append(" WHERE ROI.U_RECORD <> 2");
			sb.append(" AND RIL1.U_RECORD <> 2");
			sb.append(" AND RIL1.PID = ROI.INTER_PID");
			sb.append(" AND RIL1.LINK_PID IN (" + sql_p + ")");
			sb.append(" UNION");
			sb.append(" SELECT ROR.PID FROM RD_OBJECT_ROAD ROR,RD_ROAD_LINK RRL1");
			sb.append(" WHERE ROR.U_RECORD <> 2");
			sb.append(" AND RRL1.U_RECORD <> 2");
			sb.append(" AND ROR.ROAD_PID = RRL1.PID");
			sb.append(" AND RRL1.LINK_PID IN (" + sql_p + ")");
			sb.append(" UNION");
			sb.append(" SELECT ROL.PID FROM RD_OBJECT_LINK ROL");
			sb.append(" WHERE ROL.U_RECORD <> 2");
			sb.append(" AND ROL.LINK_PID IN (" + sql_p + ")");

			String sqlCheck = sb.toString();
			log.info("RdObject后检查GLM28026:" + sqlCheck);

			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sqlCheck);
			
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
			//所有涉及的node
			String sql = "SELECT R.S_NODE_PID NODE_PID FROM RD_INTER_LINK RIL,RD_LINK R"
						+ " WHERE RIL.U_RECORD <> 2"
						+ " AND R.U_RECORD <> 2"
						+ " AND RIL.LINK_PID = R.LINK_PID"
						+ " AND RIL.PID = " + rdObjectInter.getInterPid()
						+ " UNION "
						+ " SELECT R.E_NODE_PID NODE_PID FROM RD_INTER_LINK RIL,RD_LINK R"
						+ " WHERE RIL.U_RECORD <> 2"
						+ " AND R.U_RECORD <> 2"
						+ " AND RIL.LINK_PID = R.LINK_PID"
						+ " AND RIL.PID = " + rdObjectInter.getInterPid();
				
			//所有涉及的link
			String sql_p = "SELECT RR.LINK_PID FROM RD_LINK RR WHERE RR.S_NODE_PID IN (" + sql +")"
					+ " UNION"
					+ " SELECT RR.LINK_PID FROM RD_LINK RR WHERE RR.E_NODE_PID IN (" + sql +")";
			
			//所涉及link是不是属于其他RD_OBJECT
			StringBuilder sb = new StringBuilder();

			sb.append("SELECT ROI.PID FROM RD_OBJECT_INTER ROI,RD_INTER_LINK RIL1");
			sb.append(" WHERE ROI.U_RECORD <> 2");
			sb.append(" AND RIL1.U_RECORD <> 2");
			sb.append(" AND RIL1.PID = ROI.INTER_PID");
			sb.append(" AND ROI.PID <>" + rdObjectInter.getPid());
			sb.append(" AND RIL1.LINK_PID IN (" + sql_p + ")");
			sb.append(" UNION");
			sb.append(" SELECT ROR.PID FROM RD_OBJECT_ROAD ROR,RD_ROAD_LINK RRL1");
			sb.append(" WHERE ROR.U_RECORD <> 2");
			sb.append(" AND RRL1.U_RECORD <> 2");
			sb.append(" AND ROR.ROAD_PID = RRL1.PID");
			sb.append(" AND ROR.PID <>" + rdObjectInter.getPid());
			sb.append(" AND RRL1.LINK_PID IN (" + sql_p + ")");
			sb.append(" UNION");
			sb.append(" SELECT ROL.PID FROM RD_OBJECT_LINK ROL");
			sb.append(" WHERE ROL.U_RECORD <> 2");
			sb.append(" AND ROL.PID <>" + rdObjectInter.getPid());
			sb.append(" AND ROL.LINK_PID IN (" + sql_p + ")");

			String sqlCheck = sb.toString();
			log.info("RdObjectInter前检查GLM28026:" + sqlCheck);
			
			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sqlCheck);
			
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
