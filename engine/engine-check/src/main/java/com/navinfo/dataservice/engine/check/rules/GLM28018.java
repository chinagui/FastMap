package com.navinfo.dataservice.engine.check.rules;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInter;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.road.RdRoad;
import com.navinfo.dataservice.dao.glm.model.rd.road.RdRoadLink;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: GLM28018
 * @author songdongyan
 * @date 2017年1月4日
 * @Description: CRFRoad必须是在两个CRFIntersection中形成闭合回路
 * 新增CRFR
 * 修改CRFR
 */
public class GLM28018 extends baseRule{

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
		for(IRow obj : checkCommand.getGlmList()){
			if (obj instanceof RdRoad){
				RdRoad rdRoad = (RdRoad)obj;
				checkRdRoad(rdRoad);
			}
			else if (obj instanceof RdRoadLink){
				RdRoadLink rdRoadLink = (RdRoadLink)obj;
				checkRdRoadLink(rdRoadLink);
			}
			//删除CRFI
			else if (obj instanceof RdInter){
				RdInter rdInter = (RdInter)obj;
				checkRdInter(rdInter);
			}
		}
	}

	/**
	 * @param rdInter
	 * @throws SQLException 
	 */
	private void checkRdInter(RdInter rdInter) throws SQLException {
		if(!rdInter.status().equals(ObjStatus.DELETE)){
			return;
		}
		
		//获取该CRFI所涉及的CRFRoadPid
		StringBuilder sb = new StringBuilder();

		sb.append("SELECT DISTINCT RRL.PID FROM RD_ROAD_LINK RRL,RD_LINK RL,RD_INTER_NODE RIN");
		sb.append(" WHERE RIN.PID = " + rdInter.getPid() );
		sb.append(" AND (RIN.NODE_PID = RL.S_NODE_PID OR RIN.NODE_PID = RL.E_NODE_PID) ");
		sb.append(" AND RRL.LINK_PID = RL.LINK_PID");
		sb.append(" AND RRL.U_RECORD <> 2 ");
		sb.append(" AND RL.U_RECORD <> 2");
		sb.append(" AND RIN.U_RECORD <> 2");
		String sql = sb.toString();
		log.info("RdInter前检查GLM28009:" + sql);
		
		PreparedStatement pstmt = this.getConn().prepareStatement(sql);	
			
		ResultSet resultSet2 = pstmt.executeQuery();
		List<Long> rdRoadPidList=new ArrayList<Long>();

		while (resultSet2.next()){
			rdRoadPidList.add(resultSet2.getLong("PID"));
		} 
		resultSet2.close();
		pstmt.close();
		
		for(long pid:rdRoadPidList){
			if(!isClosedLoop(pid)){
				String target = "[RD_ROAD," + pid + "]";
				this.setCheckResult("", target, 0);
			}
		}
	}

	/**
	 * @param rdRoadLink
	 * @throws SQLException 
	 */
	private void checkRdRoadLink(RdRoadLink rdRoadLink) throws SQLException {
		long pid = rdRoadLink.getPid();
		if(!isClosedLoop(pid)){
			String target = "[RD_ROAD," + pid + "]";
			this.setCheckResult("", target, 0);
		}
	}

	/**
	 * @param rdRoad
	 * @throws SQLException 
	 */
	private void checkRdRoad(RdRoad rdRoad) throws SQLException {
		//新增RdRoad
		if(rdRoad.status().equals(ObjStatus.INSERT)){
			long pid = rdRoad.getPid();
			if(!isClosedLoop(pid)){
				String target = "[RD_ROAD," + pid + "]";
				this.setCheckResult("", target, 0);
			}
		}
		
	}

	/**
	 * @param pid
	 * @return
	 * @throws SQLException 
	 */
	private boolean isClosedLoop(long pid) throws SQLException {
		
		//查询所有RD_ROAD关联link信息
		List<RdLink> rdLinkList=new ArrayList<RdLink>();
		Set<Integer> nodePidSet = new HashSet<Integer>();
		getRdLinkInfor(pid,rdLinkList,nodePidSet);
		
		//查询link上所有RD_INTER_NODE内node
		Map<Integer,Integer> rdInterNodeMap=getRdInterNodeInfor(nodePidSet);

		//查出两个起点
		Map<Integer,Integer> startEndNodeMap = new HashMap<Integer,Integer>();
		for(RdLink rdLink:rdLinkList){
			if(rdInterNodeMap.containsKey(rdLink.getsNodePid())){
				startEndNodeMap.put(rdLink.getsNodePid(), 0);
			}
		}
		//不是两个起点，报log
		if(startEndNodeMap.keySet().size() != 2){
			return false;
		}
		//查出两条路链
		Set<Integer> linkNodePidSet1 = new HashSet<Integer>();
		Set<Integer> linkNodePidSet2 = new HashSet<Integer>();
		Iterator<Integer> it = startEndNodeMap.keySet().iterator();
		int startNode1 = it.next();
		int startNode2 = it.next();
		linkNodePidSet1.add(startNode1);
		linkNodePidSet2.add(startNode2);
		
		List<Integer> list1 = new ArrayList<Integer>(linkNodePidSet1);
		List<Integer> list2 = new ArrayList<Integer>(linkNodePidSet2);

		
		//所有的终点
		Set<Integer> EndNodePidSet = new HashSet<Integer>();

		//第一条路链
		int i = 0;
		while(i<list1.size()){
			int nodePid = list1.get(i);
			i++;
			for(RdLink rdLink:rdLinkList){
				if(rdLink.getsNodePid() == nodePid){
					if(!rdInterNodeMap.containsKey(rdLink.geteNodePid())){
						list1.add(rdLink.geteNodePid());
						break;
					}else{
						//保存终点
						startEndNodeMap.put(startNode1, rdLink.geteNodePid());
						EndNodePidSet.add(rdLink.geteNodePid());
						break;
					}	
				}
			}
		}
		//第二条路链
		i = 0;
		while(i<list2.size()){
			int nodePid = list2.get(i);
			i++;
			for(RdLink rdLink:rdLinkList){
				if(rdLink.getsNodePid() == nodePid){
					if(!rdInterNodeMap.containsKey(rdLink.geteNodePid())){
						list2.add(rdLink.geteNodePid());
						break;
					}else{
						//保存终点
						startEndNodeMap.put(startNode2, rdLink.geteNodePid());
						EndNodePidSet.add(rdLink.geteNodePid());
						break;
					}	
				}
			}
		}
		
		//如果终点不为两个报log
		if(EndNodePidSet.size() != 2){
			return false;
		}
		//查终点与起点是否属于一个CRFI
		int a = rdInterNodeMap.get(startNode1);
		int b = startEndNodeMap.get(startNode2);
		int c = rdInterNodeMap.get(b);
		if(a!=c){
			return false;
		}
		
		a = rdInterNodeMap.get(startNode2);
		b = startEndNodeMap.get(startNode1);
		c = rdInterNodeMap.get(b);
		if(a!=c){
			return false;
		}
//		if((rdInterNodeMap.get(startNode1))!=(rdInterNodeMap.get(startEndNodeMap.get(startNode2)))){
//			return false;
//		}
//		if((rdInterNodeMap.get(startNode2))!=(rdInterNodeMap.get(startEndNodeMap.get(startNode1)))){
//			return false;
//		}
		
		return true;
	}

	/**
	 * @param nodePidSet
	 * @return 
	 * @throws SQLException 
	 */
	private Map<Integer, Integer> getRdInterNodeInfor(Set<Integer> nodePidSet) throws SQLException {
		StringBuilder sb2 = new StringBuilder();
		
		sb2.append("SELECT RIN.PID,RIN.NODE_PID FROM RD_INTER_NODE RIN");
		sb2.append(" WHERE RIN.NODE_PID IN (" + StringUtils.join(nodePidSet, ",") + ")");
		sb2.append(" AND RIN.U_RECORD <> 2");

		String sql2 = sb2.toString();
		log.info("查询所有RD_INTER_NODENode信息:" + sql2);
		PreparedStatement pstmt = this.getConn().prepareStatement(sql2);	
	
		
		ResultSet resultSet2 = pstmt.executeQuery();
		Map<Integer,Integer> rdInterNodeMap=new HashMap<Integer,Integer>();

		while (resultSet2.next()){
			rdInterNodeMap.put(resultSet2.getInt("NODE_PID"), resultSet2.getInt("PID"));
		} 
		resultSet2.close();
		pstmt.close();
		return rdInterNodeMap;
	}

	/**
	 * @param pid 
	 * @param rdLinkList
	 * @param nodePidSet
	 * @throws SQLException 
	 */
	private void getRdLinkInfor(long pid, List<RdLink> rdLinkList, Set<Integer> nodePidSet) throws SQLException {
		StringBuilder sb = new StringBuilder();
		//RD_ROAD内所有link都不能是双向
		sb.append("SELECT R.LINK_PID, R.S_NODE_PID S_NODE_PID, R.E_NODE_PID E_NODE_PID");
		sb.append(" FROM RD_LINK R");
		sb.append(" WHERE R.DIRECT = 2");
		sb.append(" AND R.U_RECORD <> 2");
		sb.append(" AND R.LINK_PID IN (SELECT RRL.LINK_PID FROM RD_ROAD_LINK RRL WHERE RRL.U_RECORD <> 2 AND RRL.PID = " + pid +")");
		sb.append(" UNION ALL");
		sb.append(" SELECT R.LINK_PID, R.E_NODE_PID S_NODE_PID, R.S_NODE_PID E_NODE_PID");
		sb.append(" FROM RD_LINK R");
		sb.append(" WHERE R.DIRECT = 3");
		sb.append(" AND R.U_RECORD <> 2");
		sb.append(" AND R.LINK_PID IN (SELECT RRL.LINK_PID FROM RD_ROAD_LINK RRL WHERE RRL.U_RECORD <> 2 AND RRL.PID = " + pid +")");

		String sql = sb.toString();
		log.info("查询所有link及Node信息:" + sql);
		PreparedStatement pstmt = this.getConn().prepareStatement(sql);		
		
		ResultSet resultSet = pstmt.executeQuery();
		while (resultSet.next()){
			RdLink rdLink = new RdLink();
			rdLink.setPid(resultSet.getInt("LINK_PID"));
			rdLink.seteNodePid(resultSet.getInt("E_NODE_PID"));
			rdLink.setsNodePid(resultSet.getInt("S_NODE_PID"));
			rdLinkList.add(rdLink);
			nodePidSet.add(resultSet.getInt("E_NODE_PID"));
			nodePidSet.add(resultSet.getInt("S_NODE_PID"));
		} 
		resultSet.close();
		pstmt.close();
	}
	
	
	public void main() throws SQLException{
		isClosedLoop(6493);
	}
}
