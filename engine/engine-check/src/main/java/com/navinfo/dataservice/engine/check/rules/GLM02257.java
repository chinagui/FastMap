package com.navinfo.dataservice.engine.check.rules;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.check.CheckEngine;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.GeoHelper;
import com.vividsolutions.jts.geom.Geometry;

/** 
 * @ClassName: GLM02257
 * @author songdongyan
 * @date 2016年5月18日
 * @Description: GLM02257.java
 */
public class GLM02257 extends baseRule {

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
		// TODO Auto-generated method stub
//		道路形态含有“隧道”属性，官方名中,名称包含“隧道”的NAME_ID，TYPE
//		SELECT DISTINCT N.NAME_ID, N.TYPE
//		  FROM RD_LINK R, RD_LINK_FORM F, RD_NAME N
//		 WHERE R.LINK_PID = RDLINK_PID
//		   AND F.LINK_PID = R.LINK_PID
//		   AND F.FORM_OF_WAY = 31
//		   AND N.NAME_GROUPID IN 
//			(
//				SELECT DISTINCT LN.NAME_GROUPID 
//			      FROM RD_LINK_NAME LN
//			     WHERE LN.LINK_PID = R.LINK_PID 
//			       AND LN.NAME_CLASS = 1
//			) 
//		   AND N.LANG_CODE = 'CHI'
//		   AND N.NAME LIKE '%隧道%'
		String sql1 = "SELECT DISTINCT N.NAME_ID, N.TYPE FROM RD_LINK R, RD_LINK_FORM F, RD_NAME N "
				+ "WHERE R.LINK_PID = :1 AND F.LINK_PID = R.LINK_PID AND F.FORM_OF_WAY = 31 "
				+ "AND R.U_RECORD != 2 AND F.U_RECORD != 2 AND N.U_RECORD != 2 "
				+ "AND N.NAME_GROUPID IN (SELECT DISTINCT LN.NAME_GROUPID FROM RD_LINK_NAME LN "
				+ "WHERE LN.LINK_PID = R.LINK_PID AND LN.NAME_CLASS = 1 AND LN.U_RECORD != 2 ) "
				+ "AND N.LANG_CODE = 'CHI' AND N.NAME LIKE '%隧道%'";
		
//		官方名称中包含隧道类型的个数
//		SELECT DISTINCT LN.NAME_GROUPID
//		  FROM RD_LINK R, RD_LINK_FORM F, RD_LINK_NAME LN
//		 WHERE R.LINK_PID = RDLINK_PID
//		   AND F.LINK_PID = R.LINK_PID
//		   AND F.FORM_OF_WAY = 31
//		   AND LN.LINK_PID = R.LINK_PID
//		   AND LN.NAME_CLASS = 1
//		   AND LN.NAME_TYPE = 5
		String sql6 = "SELECT DISTINCT LN.NAME_GROUPID FROM RD_LINK R, RD_LINK_FORM F, "
				+ "RD_LINK_NAME LN "
				+ "WHERE R.LINK_PID = :1 AND F.LINK_PID = R.LINK_PID "
				+ "AND F.FORM_OF_WAY = 31 AND LN.LINK_PID = R.LINK_PID "
				+ "AND LN.NAME_CLASS = 1 AND LN.NAME_TYPE = 5 "
				+ "AND R.U_RECORD != 2 AND F.U_RECORD != 2 AND LN.U_RECORD != 2";
		
//		SELECT N.NAME, N.TYPE
//		  FROM RD_LINK R, RD_LINK_FORM F, RD_NAME N, RD_LINK_NAME LN
//		 WHERE F.LINK_PID = R.LINK_PID
//		   AND F.FORM_OF_WAY = 31
//		   AND LN.LINK_PID = R.LINK_PID
//		   AND LN.NAME_CLASS = 1
//		   AND N.NAME_GROUPID = LN.NAME_GROUPID
//		   AND N.LANG_CODE = 'CHI'
//		   AND R.LINK_PID IN 
//		   (
//				SELECT RL.LINK_PID 
//				  FROM RD_LINK RL 
//				 WHERE RL.S_NODE_PID IN (:1,:2) 
//				    OR RL.E_NODE_PID IN (:3,:4)
//		   )
//		MINUS
//		SELECT N.NAME, N.TYPE
//		  FROM RD_LINK R, RD_LINK_FORM F, RD_NAME N, RD_LINK_NAME LN
//		 WHERE F.LINK_PID = R.LINK_PID
//		   AND F.FORM_OF_WAY = 31
//		   AND LN.LINK_PID = R.LINK_PID
//		   AND LN.NAME_CLASS = 1
//		   AND N.NAME_GROUPID = LN.NAME_GROUPID
//		   AND N.LANG_CODE = 'CHI'
//		   AND R.LINK_PID IN 
//		   (
//				SELECT RL.LINK_PID 
//				  FROM RD_LINK RL 
//				 WHERE RL.LINK_PID != :5
//				   AND (RL.S_NODE_PID IN (:6,:7) 
//				    OR RL.E_NODE_PID IN (:8,:9))
//		   )
		String sql4 = "SELECT N.NAME, N.TYPE FROM RD_LINK R, RD_LINK_FORM F, RD_NAME N, "
				+ "RD_LINK_NAME LN "
				+ "WHERE F.LINK_PID = R.LINK_PID "
				+ "AND F.FORM_OF_WAY = 31 AND LN.LINK_PID = R.LINK_PID "
				+ "AND LN.NAME_CLASS = 1 AND N.NAME_GROUPID = LN.NAME_GROUPID "
				+ "AND R.U_RECORD != 2 AND F.U_RECORD != 2 AND N.U_RECORD != 2 "
				+ "AND LN.U_RECORD != 2 "
				+ "AND N.LANG_CODE = 'CHI' "
				+ "AND R.LINK_PID IN (SELECT RL.LINK_PID "
								+ "FROM RD_LINK RL WHERE (RL.S_NODE_PID IN (:1,:2) "
									+ "OR RL.E_NODE_PID IN (:3,:4)) AND RL.U_RECORD != 2) "
				+ "MINUS "
				+ "SELECT N.NAME, N.TYPE "
				+ "FROM RD_LINK R, RD_LINK_FORM F, RD_NAME N, RD_LINK_NAME LN "
				+ "WHERE F.LINK_PID = R.LINK_PID AND F.FORM_OF_WAY = 31 "
				+ "AND LN.LINK_PID = R.LINK_PID AND LN.NAME_CLASS = 1 "
				+ "AND R.U_RECORD != 2 AND F.U_RECORD != 2 AND N.U_RECORD != 2 AND LN.U_RECORD != 2"
				+ "AND N.NAME_GROUPID = LN.NAME_GROUPID "
				+ "AND N.LANG_CODE = 'CHI' AND R.LINK_PID "
				+ "IN (SELECT RL.LINK_PID FROM RD_LINK RL "
				+ "WHERE RL.LINK_PID != :5 AND RL.U_RECORD != 2"
				+ "AND (RL.S_NODE_PID IN (:6,:7) OR RL.E_NODE_PID IN (:8,:9)))";
		
		List<IRow> objList = checkCommand.getGlmList();
		
		for(int i=0; i<objList.size(); i++){
			IRow obj = objList.get(i);
			if (obj instanceof RdLink){
				
				RdLink rdLink = (RdLink)obj;
				
				PreparedStatement pstmt = getConn().prepareStatement(sql6,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
	            
				pstmt.setInt(1, rdLink.getPid());
				
				ResultSet resultSet = pstmt.executeQuery();
				
				resultSet.last();
				
	            int rowCount = resultSet.getRow();
	            
	          //官方名称中类型为隧道的》1
	            if(rowCount > 1){
	            	
	            	Geometry pointGeo=GeoHelper.getPointFromGeo(rdLink.getGeometry());
					String pointWkt = GeoTranslator.jts2Wkt(pointGeo, 0.00001, 5);
					
					this.setRuleLog("官方名称中有多个隧道类型");
					this.setCheckResult(pointWkt, "[RD_LINK,"+rdLink.getPid()+"]", rdLink.getMeshId());	
	            }
	            
	            resultSet.close();
	            
	            pstmt.close();
				
				pstmt = getConn().prepareStatement(sql1,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);

				pstmt.setInt(1, rdLink.getPid());
				
				resultSet = pstmt.executeQuery();
				
				resultSet.last();
				
				
	            rowCount = resultSet.getRow();
	            
	            //官方名称中包含“隧道”的记录》1
	            if(rowCount > 1){
	            	resultSet.close();

	            	Geometry pointGeo=GeoHelper.getPointFromGeo(rdLink.getGeometry());
					String pointWkt = GeoTranslator.jts2Wkt(pointGeo, 0.00001, 5);
					
					this.setRuleLog("隧道link上有多个隧道名");
					this.setCheckResult(pointWkt, "[RD_LINK,"+rdLink.getPid()+"]", rdLink.getMeshId());
					pstmt.close();
					return;
	            }
	            //官方名称中包含“隧道”的记录==1
	            else if(rowCount == 1){
	            	if(!resultSet.getString("TYPE").equals("隧道")){
	            		resultSet.close();
		            	Geometry pointGeo=GeoHelper.getPointFromGeo(rdLink.getGeometry());
						String pointWkt = GeoTranslator.jts2Wkt(pointGeo, 0.00001, 5);
						
						this.setRuleLog("隧道link上的隧道名称类型错误");
						this.setCheckResult(pointWkt, "[RD_LINK,"+rdLink.getPid()+"]", rdLink.getMeshId());
						pstmt.close();
						return;
	            	}

	            }
	          //官方名称中包含“隧道”的记录==0
	            else{
	            	pstmt.close();
	            	
		            pstmt = getConn().prepareStatement(sql4,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);

		            pstmt.setInt(1, rdLink.getsNodePid());
		            pstmt.setInt(2, rdLink.geteNodePid());
		            pstmt.setInt(3, rdLink.getsNodePid());
		            pstmt.setInt(4, rdLink.geteNodePid());
					pstmt.setInt(5, rdLink.getPid());
					pstmt.setInt(6, rdLink.getsNodePid());
		            pstmt.setInt(7, rdLink.geteNodePid());
		            pstmt.setInt(8, rdLink.getsNodePid());
		            pstmt.setInt(9, rdLink.geteNodePid());
					
					resultSet = pstmt.executeQuery();
					
					resultSet.last();
		            rowCount = resultSet.getRow();
		            
		            //官方名称与隧道串两端挂接的link的官方名都不相同的个数==1
		            if(rowCount == 1){
		            	
		            	if(!resultSet.getString("TYPE").equals("隧道")){
			            	resultSet.close();
			            	Geometry pointGeo=GeoHelper.getPointFromGeo(rdLink.getGeometry());
							String pointWkt = GeoTranslator.jts2Wkt(pointGeo, 0.00001, 5);
							
							this.setRuleLog("隧道link上的隧道名称类型错误");
							this.setCheckResult(pointWkt, "[RD_LINK,"+rdLink.getPid()+"]", rdLink.getMeshId());
							
							pstmt.close();
							return;	
		            	}
		            }
		          //官方名称与隧道串两端挂接的link的官方名都不相同的个数》1	
		            else if(rowCount > 1){
			            	resultSet.close();
			            	Geometry pointGeo=GeoHelper.getPointFromGeo(rdLink.getGeometry());
							String pointWkt = GeoTranslator.jts2Wkt(pointGeo, 0.00001, 5);
							
							this.setRuleLog("隧道link上有多个隧道名");
							this.setCheckResult(pointWkt, "[RD_LINK,"+rdLink.getPid()+"]", rdLink.getMeshId());
							
							pstmt.close();
							return;	
						}
		            if (resultSet != null) {
						try {
							resultSet.close();
						} catch (Exception e) {

						}
					}
		            if (pstmt != null) {
						try {
							pstmt.close();
						} catch (Exception e) {

						}
					}
		           
	            }

			}

		}

	}
	
	public static void main(String[] args) throws Exception{
		RdLink link=new RdLink();
		String str= "{ \"type\": \"LineString\",\"coordinates\": [ [116.17659, 39.97508], [116.16144, 39.94844],[116.20427, 39.94322],[116.20427, 39.94322], [116.17659, 39.97508] ]}";
		JSONObject geometry = JSONObject.fromObject(str);
		Geometry geometry2=GeoTranslator.geojson2Jts(geometry, 1, 5);
		link.setGeometry(geometry2);
//		link.setPid(262558);
//		link.setsNodePid(173460);
//		link.seteNodePid(173462);
		link.setPid(12962846);
		link.setsNodePid(532307);
		link.seteNodePid(11048003);
		List<IRow> objList=new ArrayList<IRow>();
		objList.add(link);
		
		Connection conn = DBConnector.getInstance().getConnectionById(11);
		
		//检查调用
		CheckCommand checkCommand=new CheckCommand();
		checkCommand.setGlmList(objList);
		checkCommand.setOperType(OperType.UPDATE);
		checkCommand.setObjType(link.objType());
		
		CheckEngine checkEngine=new CheckEngine(checkCommand,conn);
		checkEngine.postCheck();
		
	}

}

