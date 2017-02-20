package com.navinfo.dataservice.engine.edit.operation.obj.adface.create;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.check.NiValExceptionOperator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class Check {

	public void checkDupilicateNode(JSONObject geometry) throws Exception{
		
		Geometry geo = GeoTranslator.geojson2Jts(geometry);
		
		Coordinate[] coords = geo.getCoordinates();
		
		for(int i=0;i<coords.length;i++){
			if(i+2<coords.length){
				
				Coordinate current = coords[i];
				
				Coordinate next = coords[i+1];
				
				Coordinate next2 = coords[i+2];
				
				if(current.compareTo(next)==0 || current.compareTo(next2)==0){
					throw new Exception("一根link上不能存在坐标相同的形状点");
				}
			}
		}
	}
	
	public void checkLinkLength(double length) throws Exception{
		
		if(length<=2){
			throw new Exception("道路link长度应大于2米");
		}
	}
	
	private void throwException(String msg) throws Exception {
		throw new Exception(msg);
	}
	
	public void postCheck(Connection conn,Result result, int projectId) throws Exception
	{
		
		for(IRow obj : result.getAddObjects()){
			if (obj instanceof RdLink){
				RdLink rdLink = (RdLink)obj;
				
				NiValExceptionOperator check = new NiValExceptionOperator(conn);
				
				//获取link中间点
				Geometry geo = GeoTranslator.transform(rdLink.getGeometry(),0.00001,5);
				
				Coordinate[] cs = geo.getCoordinates();
				
				int midP = (int)Math.round(cs.length/2.0);
				
				double x = cs[midP].x;
				
				double y = cs[midP].y;
				
				String pointWkt = "Point ("+x+" "+y+")";
				
				//SHAPING_CHECK_CROSS_RDLINK_RDLINK
				
				String sql = "select a.link_pid from rd_link a,rd_link b where a.link_pid = :1 and a.u_record!=2 and b.link_pid != :2 and b.u_record!=2 and b.s_node_pid not in (a.s_node_pid,a.e_node_pid) and b.e_node_pid not in (a.s_node_pid,a.e_node_pid) and sdo_relate(b.geometry,a.geometry,'mask=anyinteract')='TRUE'";
				
				PreparedStatement pstmt = conn.prepareStatement(sql);
				
				pstmt.setInt(1, rdLink.getPid());
				
				pstmt.setInt(2, rdLink.getPid());
				
				ResultSet resultSet = pstmt.executeQuery();
				
				if (resultSet.next()){
					check.insertCheckLog("SHAPING_CHECK_CROSS_RDLINK_RDLINK", pointWkt, "[RD_LINK,"+rdLink.getPid()+"]", rdLink.getMeshId(), "TEST");
				}
				
				this.releaseSource(pstmt, resultSet);
				
				//GLM01015
				sql = "select a.link_pid from rd_link a where a.link_pid = :1 and  exists (select null from rd_link b where a.link_pid != b.link_pid and a.s_node_pid in (b.s_node_pid,b.e_node_pid) and a.e_node_pid in (b.s_node_pid,b.e_node_pid) and b.u_record!=2)";
				
				pstmt = conn.prepareStatement(sql);
				
				pstmt.setInt(1, rdLink.getPid());
				
				resultSet = pstmt.executeQuery();
				
				if (resultSet.next()){
					check.insertCheckLog("GLM01015", pointWkt, "[RD_LINK,"+rdLink.getPid()+"]", rdLink.getMeshId(), "TEST");
				}
				
				this.releaseSource(pstmt, resultSet);
				
				//GLM56004
				if (!rdLink.getGeometry().isSimple()){
					check.insertCheckLog("GLM56004", pointWkt, "[RD_LINK,"+rdLink.getPid()+"]", rdLink.getMeshId(), "TEST");
				}
				
				//GLM01014
				//if (rdLink.getsNodePid() == rdLink.geteNodePid()){
				//	check.insertCheckLog("GLM01014", pointWkt, "[RD_LINK,"+rdLink.getPid()+"]", rdLink.getMeshId(), "TEST");
				//}
				
				//GLM01025
				double sx,sy,ex,ey;
				
				if (rdLink.getDirect() == 3){
					sx = cs[cs.length-1].x;
					sy = cs[cs.length-1].y;
					ex = cs[0].x;
					ey = cs[0].y;
				}else{
					ex = cs[cs.length-1].x;
					ey = cs[cs.length-1].y;
					sx = cs[0].x;
					sy = cs[0].y;
				}
				
				sql = "select a.node_pid from rd_node a where a.node_pid = :1 and a.geometry.sdo_point.x = :2 and a.geometry.sdo_point.y = :3 union all select a.node_pid from rd_node a where a.node_pid = :4 and a.geometry.sdo_point.x = :5 and a.geometry.sdo_point.y = :6 ";
				
				pstmt = conn.prepareStatement(sql);
				
				pstmt.setInt(1, rdLink.getsNodePid());
				
				pstmt.setDouble(2, sx);
				
				pstmt.setDouble(3, sy);
				
				pstmt.setInt(4, rdLink.geteNodePid());
				
				pstmt.setDouble(5, ex);
				
				pstmt.setDouble(6, ey);
				
				resultSet = pstmt.executeQuery();
				
				boolean hasEnode=false;
				boolean hasSnode=false;
				
				while (resultSet.next()){
					int nodePid = resultSet.getInt("node_pid");
					
					if(nodePid == rdLink.getsNodePid()){
						hasSnode=true;
					}
					else if(nodePid == rdLink.geteNodePid()){
						hasEnode=true;
					}
				}
				
				this.releaseSource(pstmt, resultSet);
				
				if(!hasEnode || !hasSnode){
					check.insertCheckLog("GLM01025", pointWkt, "[RD_LINK,"+rdLink.getPid()+"]", rdLink.getMeshId(), "TEST");	
				}
				
				//GLM01027
				int pointCount = rdLink.getGeometry().getCoordinates().length;
				
				if(pointCount >= 490){
					check.insertCheckLog("GLM01027", pointWkt, "[RD_LINK,"+rdLink.getPid()+"]", rdLink.getMeshId(), "TEST");
				}
				
				//PERMIT_CHECK_NO_REPEAT
				sql = "select a.node_pid from rd_node a,rd_node b where sdo_within_distance(a.geometry, b.geometry, 'DISTANCE=0')='TRUE' and b.node_pid in (:1,:2) and a.node_pid not in (:3,:4) and a.u_record!=2";
				
				pstmt = conn.prepareStatement(sql);
				
				pstmt.setInt(1, rdLink.geteNodePid());
				
				pstmt.setInt(2, rdLink.getsNodePid());
				
				pstmt.setInt(3, rdLink.geteNodePid());
				
				pstmt.setInt(4, rdLink.getsNodePid());
				
				resultSet = pstmt.executeQuery();
				
				if (resultSet.next()){
					check.insertCheckLog("PERMIT_CHECK_NO_REPEAT", pointWkt, "[RD_LINK,"+rdLink.getPid()+"]", rdLink.getMeshId(), "TEST");
				}
				
				this.releaseSource(pstmt, resultSet);
				
				//GLM03001
//				sql = "select count(1) count,node_pid from rd_node where node_pid in (:1,:2) group by node_pid";
//				
//				pstmt = conn.prepareStatement(sql);
//				
//				pstmt.setInt(1, rdLink.geteNodePid());
//				
//				pstmt.setInt(2, rdLink.getsNodePid());
//				
//				resultSet = pstmt.executeQuery();
//				
//				boolean flag=false;
//				
//				while (resultSet.next()){
//					
//					if(resultSet.getInt("count")>7)
//					{
//						flag=true;
//					}
//					
//				}
//				
//				if(flag){
//					check.insertCheckLog("GLM03001", pointWkt, "[RD_LINK,"+rdLink.getPid()+"]", rdLink.getMeshId(), "TEST");
//				}
//				
//				this.releaseSource(pstmt, resultSet);
				
				//GLM03056
//				sql = "select count(a.link_pid) count, b.node_pid   from rd_link a, rd_node_form b  where (a.e_node_pid = b.node_pid or a.s_node_pid = b.node_pid)    and b.node_pid in (:1, :2) and b.form_of_way=15  group by b.node_pid";
//				
//				pstmt = conn.prepareStatement(sql);
//				
//				pstmt.setInt(1, rdLink.geteNodePid());
//				
//				pstmt.setInt(2, rdLink.getsNodePid());
//				
//				resultSet = pstmt.executeQuery();
//				
//				flag=false;
//				
//				while (resultSet.next()){
//					
//					if(resultSet.getInt("count")!=2)
//					{
//						flag=true;
//					}
//					
//				}
//				
//				if(flag){
//					check.insertCheckLog("GLM03056", pointWkt, "[RD_LINK,"+rdLink.getPid()+"]", rdLink.getMeshId(), "TEST");
//				}
//				
//				this.releaseSource(pstmt, resultSet);
//				
			}
		}
		
		
	}
	
	private void releaseSource(Statement stmt,ResultSet resultSet) throws SQLException{
		resultSet.close();
		
		stmt.close();
	}
}
