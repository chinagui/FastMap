package com.navinfo.dataservice.engine.check.rules;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.road.RdRoad;
import com.navinfo.dataservice.dao.glm.model.rd.road.RdRoadLink;
import com.navinfo.dataservice.engine.check.core.baseRule;

/** 
 * @ClassName: GLM28016
 * @author songdongyan
 * @date 2017年1月5日
 * @Description: CRFRoad的两端必须是CRFIntersection，且每端node必须组成同一个CRFI（图廓点端除外）
 * 新增CRFR前检查:RdRoad
 * 理解，三种情况：1：有且只有两组（两根LINK端点落在同一CRFI上）；2：存在一组（两根LINK端点落在同一CRFI上）+ 一组（两根LINK端点挂接在同一图廓点上）；3：两组（两根LINK端点挂接在同一图廓点上）
 */
public class GLM28016 extends baseRule{

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#preCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for(IRow obj : checkCommand.getGlmList()){
			//RdInter新增触发
			if (obj instanceof RdRoad){
				RdRoad rdRoad = (RdRoad)obj;
				checkRdRoad(rdRoad);
			}
		}
	}


	/**
	 * @param rdRoad
	 * @throws SQLException 
	 */
	private void checkRdRoad(RdRoad rdRoad) throws Exception {
		if(rdRoad.status().equals(ObjStatus.INSERT)){
			Set<Integer> linkPidSet = new HashSet<>();
			for(IRow iRow:rdRoad.getLinks()){
				RdRoadLink rdRoadLink = (RdRoadLink)iRow;
				linkPidSet.add(rdRoadLink.getLinkPid());
			}
			List<RdLink> links = new RdLinkSelector(this.getConn()).loadByPids(new ArrayList(linkPidSet), false);
			if (linkPidSet.size() != links.size()) {
			    return;
            }

			if(!linkPidSet.isEmpty()){

				//查出CRFRoad涉及的所有CRFInode信息
				Map<Integer,Integer> rdInterNodeMap=new HashMap<Integer,Integer>();
				Set<Integer> rdInterPidSet = new HashSet<Integer>();
				StringBuilder sb = new StringBuilder();

				sb.append("SELECT RIN.PID, RIN.NODE_PID,1 AS TYPE FROM RD_INTER_NODE RIN");
				sb.append(" WHERE RIN.NODE_PID IN (");
				sb.append(" SELECT R.S_NODE_PID FROM RD_LINK R WHERE R.U_RECORD <> 2 AND R.LINK_PID IN (" + StringUtils.join(linkPidSet.toArray(),",") + ")");
				sb.append(" UNION");
				sb.append(" SELECT R.E_NODE_PID FROM RD_LINK R WHERE R.U_RECORD <> 2 AND R.LINK_PID IN (" + StringUtils.join(linkPidSet.toArray(),",") + ")");
				sb.append(" )");
				sb.append(" AND RIN.U_RECORD <> 2");
				
				String sql = sb.toString();
				log.info("RdInter前检查GLM28016:" + sql);
				
				PreparedStatement pstmt = null;
				ResultSet resultSet = null;
				try {
					pstmt = this.getConn().prepareStatement(sql);	
					resultSet = pstmt.executeQuery();
					
					while (resultSet.next()){
						//CRFI端点
						rdInterNodeMap.put(resultSet.getInt("NODE_PID"), resultSet.getInt("PID"));
						rdInterPidSet.add(resultSet.getInt("PID"));

					} 
				}catch (SQLException e) {
					throw e;
				} finally {
					DbUtils.closeQuietly(resultSet);
					DbUtils.closeQuietly(pstmt);
				}
				
				//CRFRoad内link涉及到的CRFI数量小于2，报错
				if(rdInterPidSet.size()<2){
					this.setCheckResult("", "", 0);
					return;
				}

				//查出CRFRoad内所有link及端点信息
				Map<Integer,List<Integer>> rdLinks=new HashMap<Integer,List<Integer>>();
				Map<Integer,Integer> rdLinkNodePidMap = new HashMap<Integer,Integer>();
				StringBuilder sb2 = new StringBuilder();
				
				sb2.append(" SELECT R.LINK_PID,R.S_NODE_PID,R.E_NODE_PID FROM RD_LINK R WHERE R.U_RECORD <> 2 AND R.LINK_PID IN (" + StringUtils.join(linkPidSet.toArray(),",") + ")");
				
				String sql2 = sb2.toString();
				
				PreparedStatement pstmt2 = null;
				ResultSet resultSet2 = null;
				try {
					pstmt2 = this.getConn().prepareStatement(sql2);	
					resultSet2 = pstmt2.executeQuery();

					while (resultSet2.next()){
						List<Integer> nodes = new ArrayList<Integer>();
						int s_node_pid = resultSet2.getInt("S_NODE_PID");
						int e_node_pid = resultSet2.getInt("E_NODE_PID");
						nodes.add(s_node_pid);
						nodes.add(e_node_pid);
						
						if(rdLinkNodePidMap.containsKey(s_node_pid)){
							int num = rdLinkNodePidMap.get(s_node_pid);
							rdLinkNodePidMap.put(s_node_pid, num+1);
						}else{
							rdLinkNodePidMap.put(s_node_pid, 1);
						}
						if(rdLinkNodePidMap.containsKey(e_node_pid)){
							int num = rdLinkNodePidMap.get(e_node_pid);
							rdLinkNodePidMap.put(e_node_pid, num+1);
						}else{
							rdLinkNodePidMap.put(e_node_pid, 1);
						}
						rdLinks.put(resultSet2.getInt("LINK_PID"), nodes);
					} 
				}catch (SQLException e) {
					throw e;
				} finally {
					DbUtils.closeQuietly(resultSet2);
					DbUtils.closeQuietly(pstmt2);
				}
				
				Set<Integer> rdInterPidSet2 = new HashSet<Integer>();

				//判断CRFRoad是否有单独的点,且该点不落在CRFI中，有则报错
				for(Map.Entry<Integer, Integer> entry:rdLinkNodePidMap.entrySet()){
					if(entry.getValue()==1){
						if(!rdInterNodeMap.keySet().contains(entry.getKey())){
							this.setCheckResult("", "", 0);
							return;
						}
						rdInterPidSet2.add(rdInterNodeMap.get(entry.getKey()));
					}
				}
				//查出CRFRoad内所有link单点所在CRFI个数大于2
				if(rdInterPidSet2.size()>2){
					this.setCheckResult("", "", 0);
					return;
				}
				
//				//如果涉及到的CRFI个数为0，报log
//				if(rdInterPidSet.size()!=2){
//					this.setCheckResult("", "", 0);
//					return;
//				}else{
////					遍历CRFI
////					构成CRFR的link，有且只有两根不同link落在改CRFR上
//					for(Integer rdInterPid:rdInterPidSet){
//						Set<Integer> linkPidSetTemp = new HashSet<Integer>();
//						for(Map.Entry<Integer, Integer> entry:rdInterNodeMap.entrySet()){
//							int a = entry.getValue();
//							int b = rdInterPid;
//							if(a == b){
////							if(entry.getValue() == rdInterPid){
//								for(Map.Entry<Integer, List<Integer>> entryInner:rdLinks.entrySet()){
//									List<Integer> nodePids = entryInner.getValue();
//									if(nodePids.contains(entry.getKey())){
//										linkPidSetTemp.add(entryInner.getKey());//link的Pid
//									}
//								}
//							}
//						}
//						if(linkPidSetTemp.size()<2){
//							this.setCheckResult("", "", 0);
//							return;
//						}
//					}
//				}
				
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
