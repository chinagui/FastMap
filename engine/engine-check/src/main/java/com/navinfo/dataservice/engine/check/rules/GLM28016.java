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

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInter;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInterLink;
import com.navinfo.dataservice.dao.glm.model.rd.road.RdRoad;
import com.navinfo.dataservice.dao.glm.model.rd.road.RdRoadLink;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

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

//	/**
//	 * @param rdRoad
//	 * @throws SQLException 
//	 */
//	private void checkRdRoad(RdRoad rdRoad) throws SQLException {
//		if(rdRoad.status().equals(ObjStatus.INSERT)){
//			Set<Integer> linkPidSet = new HashSet<Integer>();
//			for(IRow iRow:rdRoad.getLinks()){
//				RdRoadLink rdRoadLink = (RdRoadLink)iRow;
//				linkPidSet.add(rdRoadLink.getLinkPid());
//			}
//			if(!linkPidSet.isEmpty()){
//				StringBuilder sb = new StringBuilder();
//
//				sb.append("SELECT RIN.PID, RIN.NODE_PID,1 AS TYPE FROM RD_INTER_NODE RIN");
//				sb.append(" WHERE RIN.NODE_PID IN (");
//				sb.append(" SELECT R.S_NODE_PID FROM RD_LINK R WHERE R.U_RECORD <> 2 AND R.LINK_PID IN (" + StringUtils.join(linkPidSet.toArray(),",") + ")");
//				sb.append(" UNION");
//				sb.append(" SELECT R.E_NODE_PID FROM RD_LINK R WHERE R.U_RECORD <> 2 AND R.LINK_PID IN (" + StringUtils.join(linkPidSet.toArray(),",") + ")");
//				sb.append(" )");
//				sb.append(" AND RIN.U_RECORD <> 2");
//				sb.append(" UNION");
//				sb.append(" SELECT 0 AS PID, RN.NODE_PID,2 AS TYPE FROM RD_NODE RN,RD_NODE_FORM F");
//				sb.append(" WHERE RN.NODE_PID IN (");
//				sb.append(" SELECT R.S_NODE_PID FROM RD_LINK R WHERE R.U_RECORD <> 2 AND R.LINK_PID IN (" + StringUtils.join(linkPidSet.toArray(),",") + ")");
//				sb.append(" UNION");
//				sb.append(" SELECT R.E_NODE_PID FROM RD_LINK R WHERE R.U_RECORD <> 2 AND R.LINK_PID IN (" + StringUtils.join(linkPidSet.toArray(),",") + ")");
//				sb.append(" )");
//				sb.append(" AND RN.U_RECORD <> 2");
//				sb.append(" AND RN.NODE_PID = F.NODE_PID");
//				sb.append(" AND F.FORM_OF_WAY = 2");
//				
//				String sql = sb.toString();
//				log.info("RdInter前检查GLM28016:" + sql);
//				
//				PreparedStatement pstmt = this.getConn().prepareStatement(sql);	
//				
//				ResultSet resultSet = pstmt.executeQuery();
//				Map<Integer,Integer> rdInterNodeMap=new HashMap<Integer,Integer>();
//				Set<Integer> rdInterPidSet = new HashSet<Integer>();
//				Set<Integer> NodeMap=new HashSet<Integer>();
//
//				while (resultSet.next()){
//					if(resultSet.getInt("TYPE")==1){
//						//CRFI端点
//						rdInterNodeMap.put(resultSet.getInt("NODE_PID"), resultSet.getInt("PID"));
//						rdInterPidSet.add(resultSet.getInt("PID"));
//					}else if(resultSet.getInt("TYPE")==2){
//						//图廓点
//						NodeMap.add(resultSet.getInt("NODE_PID"));
//						rdInterPidSet.add(resultSet.getInt("PID"));
//					}
//				} 
//				resultSet.close();
//				pstmt.close();
//				//由上查出了CRFRoad涉及的所有CRFInode信息
//				
//				
////				StringBuilder sb = new StringBuilder();
////
////				sb.append("SELECT RIN.PID, RIN.NODE_PID,1 AS TYPE FROM RD_INTER_NODE RIN");
////				sb.append(" WHERE RIN.NODE_PID IN (");
////				sb.append(" SELECT R.S_NODE_PID FROM RD_LINK R WHERE R.U_RECORD <> 2 AND R.LINK_PID IN (" + StringUtils.join(linkPidSet.toArray(),",") + ")");
////				sb.append(" UNION");
////				sb.append(" SELECT R.E_NODE_PID FROM RD_LINK R WHERE R.U_RECORD <> 2 AND R.LINK_PID IN (" + StringUtils.join(linkPidSet.toArray(),",") + ")");
////				sb.append(" )");
////				sb.append(" AND RIN.U_RECORD <> 2");
////				sb.append(" UNION");
////				sb.append(" SELECT 0 AS PID, RN.NODE_PID,2 AS TYPE FROM RD_NODE RN,RD_NODE_FORM F");
////				sb.append(" WHERE RN.NODE_PID IN (");
////				sb.append(" SELECT R.S_NODE_PID FROM RD_LINK R WHERE R.U_RECORD <> 2 AND R.LINK_PID IN (" + StringUtils.join(linkPidSet.toArray(),",") + ")");
////				sb.append(" UNION");
////				sb.append(" SELECT R.E_NODE_PID FROM RD_LINK R WHERE R.U_RECORD <> 2 AND R.LINK_PID IN (" + StringUtils.join(linkPidSet.toArray(),",") + ")");
////				sb.append(" )");
////				sb.append(" AND RN.U_RECORD <> 2");
////				sb.append(" AND RN.NODE_PID = F.NODE_PID");
////				sb.append(" AND F.FORM_OF_WAY = 2");
////				
////				String sql = sb.toString();
////				log.info("RdInter前检查GLM28016:" + sql);
////				
////				PreparedStatement pstmt = this.getConn().prepareStatement(sql);	
////				
////				ResultSet resultSet = pstmt.executeQuery();
////				Map<Integer,Integer> rdInterNodeMap=new HashMap<Integer,Integer>();
////				Set<Integer> rdInterPidSet = new HashSet<Integer>();
////				Set<Integer> NodeMap=new HashSet<Integer>();
////
////				while (resultSet.next()){
////					if(resultSet.getInt("TYPE")==1){
////						//CRFI端点
////						rdInterNodeMap.put(resultSet.getInt("NODE_PID"), resultSet.getInt("PID"));
////						rdInterPidSet.add(resultSet.getInt("PID"));
////					}else if(resultSet.getInt("TYPE")==2){
////						//图廓点
////						NodeMap.add(resultSet.getInt("NODE_PID"));
////						rdInterPidSet.add(resultSet.getInt("PID"));
////					}
////				} 
////				resultSet.close();
////				pstmt.close();
////				//由上查出了CRFRoad涉及的所有CRFInode信息
//				
//				//查出CRFRoad内所有link及端点信息
//				StringBuilder sb2 = new StringBuilder();
//				
//				sb2.append(" SELECT R.LINK_PID,R.S_NODE_PID,R.E_NODE_PID FROM RD_LINK R WHERE R.U_RECORD <> 2 AND R.LINK_PID IN (" + StringUtils.join(linkPidSet.toArray(),",") + ")");
//				
//				String sql2 = sb2.toString();
//				
//				PreparedStatement pstmt2 = this.getConn().prepareStatement(sql);	
//				
//				ResultSet resultSet2 = pstmt.executeQuery();
//				
//				Map<Integer,List<Integer>> rdLinks=new HashMap<Integer,List<Integer>>();
//
//				while (resultSet2.next()){
//					List<Integer> nodes = new ArrayList<Integer>();
//					nodes.add(resultSet2.getInt("S_NODE_PID"));
//					nodes.add(resultSet2.getInt("E_NODE_PID"));
//					rdLinks.put(resultSet2.getInt("LINK_PID"), nodes);
//				} 
//				resultSet2.close();
//				pstmt2.close();
//				
//
//				//如果涉及到的CRFI个数不等于2，报log
//				if(rdInterPidSet.size()!=2){
//					this.setCheckResult("", "", 0);
//					return;
//				}else{
//					//遍历两个CRFI
//					//构成CRFR的link，有且只有两根不同link落在改CRFR上
//					for(Integer rdInterPid:rdInterPidSet){
//						Set<Integer> linkPidSetTemp = new HashSet<Integer>();
//						for(Map.Entry<Integer, Integer> entry:rdInterNodeMap.entrySet()){
//							if(entry.getValue() == rdInterPid){
//								for(Map.Entry<Integer, List<Integer>> entryInner:rdLinks.entrySet()){
//									List<Integer> nodePids = entryInner.getValue();
//									if(nodePids.contains(entry.getKey())){
//										linkPidSetTemp.add(entryInner.getKey());//link的Pid
//									}
//								}
//							}
//						}
//						if(linkPidSetTemp.size()!=2){
//							this.setCheckResult("", "", 0);
//							return;
//						}
//					}
//				}
//				
////				
////				Set<Integer> pidSet = new HashSet<Integer>();
////				for(Map.Entry<Integer,Integer> entry:rdInterNodeMap.entrySet()){
////					pidSet.add(entry.getValue());
////					if(NodeMap.contains(entry.getKey())){
////						NodeMap.remove(entry.getKey());
////					}
////				}
////				if(pidSet.size()>2){
////					this.setCheckResult("", "", 0);
////					return;
////				}else if(pidSet.size() + NodeMap.size()<2){
////					this.setCheckResult("", "", 0);
////					return;
////				}
//			}
//		}
//		
//	}
	/**
	 * @param rdRoad
	 * @throws SQLException 
	 */
	private void checkRdRoad(RdRoad rdRoad) throws SQLException {
		if(rdRoad.status().equals(ObjStatus.INSERT)){
			Set<Integer> linkPidSet = new HashSet<Integer>();
			for(IRow iRow:rdRoad.getLinks()){
				RdRoadLink rdRoadLink = (RdRoadLink)iRow;
				linkPidSet.add(rdRoadLink.getLinkPid());
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
				
				PreparedStatement pstmt = this.getConn().prepareStatement(sql);	
				ResultSet resultSet = pstmt.executeQuery();
				
				while (resultSet.next()){
					//CRFI端点
					rdInterNodeMap.put(resultSet.getInt("NODE_PID"), resultSet.getInt("PID"));
					rdInterPidSet.add(resultSet.getInt("PID"));

				} 
				resultSet.close();
				pstmt.close();

				//查出CRFRoad内所有link及端点信息
				Map<Integer,List<Integer>> rdLinks=new HashMap<Integer,List<Integer>>();
				StringBuilder sb2 = new StringBuilder();
				
				sb2.append(" SELECT R.LINK_PID,R.S_NODE_PID,R.E_NODE_PID FROM RD_LINK R WHERE R.U_RECORD <> 2 AND R.LINK_PID IN (" + StringUtils.join(linkPidSet.toArray(),",") + ")");
				
				String sql2 = sb2.toString();
				
				PreparedStatement pstmt2 = this.getConn().prepareStatement(sql2);	
				ResultSet resultSet2 = pstmt2.executeQuery();

				while (resultSet2.next()){
					List<Integer> nodes = new ArrayList<Integer>();
					nodes.add(resultSet2.getInt("S_NODE_PID"));
					nodes.add(resultSet2.getInt("E_NODE_PID"));
					rdLinks.put(resultSet2.getInt("LINK_PID"), nodes);
				} 
				resultSet2.close();
				pstmt2.close();
				
//				//如果涉及到的CRFI个数不等于2，报log
//				if(rdInterPidSet.size()<2){
//					this.setCheckResult("", "", 0);
//					return;
//				}else{
					//遍历CRFI
					//构成CRFR的link，有且只有两根不同link落在改CRFR上
					for(Integer rdInterPid:rdInterPidSet){
						Set<Integer> linkPidSetTemp = new HashSet<Integer>();
						for(Map.Entry<Integer, Integer> entry:rdInterNodeMap.entrySet()){
							int a = entry.getValue();
							int b = rdInterPid;
							if(a == b){
//							if(entry.getValue() == rdInterPid){
								for(Map.Entry<Integer, List<Integer>> entryInner:rdLinks.entrySet()){
									List<Integer> nodePids = entryInner.getValue();
									if(nodePids.contains(entry.getKey())){
										linkPidSetTemp.add(entryInner.getKey());//link的Pid
									}
								}
							}
						}
						if(linkPidSetTemp.size()<2){
							this.setCheckResult("", "", 0);
							return;
						}
					}
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
