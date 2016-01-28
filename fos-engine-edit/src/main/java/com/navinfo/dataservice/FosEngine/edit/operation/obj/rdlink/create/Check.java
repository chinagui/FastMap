package com.navinfo.dataservice.FosEngine.edit.operation.obj.rdlink.create;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.FosEngine.edit.check.NiValCheckOperator;
import com.navinfo.dataservice.FosEngine.edit.model.IRow;
import com.navinfo.dataservice.FosEngine.edit.model.Result;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLink;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
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
	
	public void checkGLM04002(Connection conn, int eNodePid, int sNodePid) throws Exception {

		String sql = "select count(a.link_pid) count,b.node_pid from rd_link a,rd_gate b where (a.e_node_pid=b.node_pid or a.s_node_pid=b.node_pid) and b.node_pid in (:1,:2) group by b.node_pid";
		
		PreparedStatement pstmt = conn.prepareStatement(sql);
		
		pstmt.setInt(1, eNodePid);
		
		pstmt.setInt(2, sNodePid);

		ResultSet resultSet = pstmt.executeQuery();

		boolean flag = false;

		while (resultSet.next()) {

			int count = resultSet.getInt("count");

			if (count!=2) {
				flag = true;
			}
		}

		resultSet.close();

		pstmt.close();

		if (flag) {
			throwException("大门点的挂接link数必须是2");
		}

	}
	
	public void checkGLM13002(Connection conn, int eNodePid, int sNodePid) throws Exception {

		String sql = "select count(a.link_pid) count,b.node_pid from rd_link a,rd_tollgate b where (a.e_node_pid=b.node_pid or a.s_node_pid=b.node_pid) and b.node_pid in (:1,:2) group by b.node_pid";
		
		PreparedStatement pstmt = conn.prepareStatement(sql);
		
		pstmt.setInt(1, eNodePid);
		
		pstmt.setInt(2, sNodePid);

		ResultSet resultSet = pstmt.executeQuery();

		boolean flag = false;

		while (resultSet.next()) {

			int count = resultSet.getInt("count");

			if (count!=2) {
				flag = true;
			}
		}

		resultSet.close();

		pstmt.close();

		if (flag) {
			throwException("关系型收费站主点的挂接link数必须是2");
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
	
	public void postCheck(Connection conn,Result result) throws Exception
	{
		
		for(IRow obj : result.getAddObjects()){
			if (obj instanceof RdLink){
				RdLink rdLink = (RdLink)obj;
				
				NiValCheckOperator check = new NiValCheckOperator(conn);
				//获取link的中间点
				Coordinate[] cs = rdLink.getGeometry().getCoordinates();
				
				int midP = (int)Math.round(cs.length/2);
				
				double x = cs[midP].x;
				
				double y = cs[midP].y;
				
				String pointWkt = "Point ("+x+" "+y+")";
				
				String sql = "select a.link_pid from rd_link a,rd_link b where a.link_pid = :1 and b.link_pid != :2 and b.s_node_pid not in (a.s_node_pid,a.e_node_pid) and b.e_node_pid not in (a.s_node_pid,a.e_node_pid) and sdo_relate(b.geometry,a.geometry,'mask=anyinteract')='TRUE'";
				
				PreparedStatement pstmt = conn.prepareStatement(sql);
				
				pstmt.setInt(1, rdLink.getPid());
				
				pstmt.setInt(2, rdLink.getPid());
				
				ResultSet resultSet = pstmt.executeQuery();
				
				if (resultSet.next()){
					check.insertCheckLog("SHAPING_CHECK_CROSS_RDLINK_RDLINK", pointWkt, "[RD_LINK,"+rdLink.getPid()+"]", rdLink.getMeshId(), "TEST");
				}
				
				this.releadSource(pstmt, resultSet);
				
				sql = "select a.link_pid from rd_link a where a.link_pid = :1 and  exists (select null from rd_link b where a.link_pid != b.link_pid and a.s_node_pid in (b.s_node_pid,b.e_node_pid) and a.e_node_pid in (b.s_node_pid,b.e_node_pid)";
				
				pstmt = conn.prepareStatement(sql);
				
				pstmt.setInt(1, rdLink.getPid());
				
				resultSet = pstmt.executeQuery();
				
				if (resultSet.next()){
					check.insertCheckLog("GLM01015", pointWkt, "[RD_LINK,"+rdLink.getPid()+"]", rdLink.getMeshId(), "TEST");
				}
				
				this.releadSource(pstmt, resultSet);
				
				if (!rdLink.getGeometry().isSimple()){
					check.insertCheckLog("GLM56004", pointWkt, "[RD_LINK,"+rdLink.getPid()+"]", rdLink.getMeshId(), "TEST");
				}
				
				if (rdLink.getsNodePid() == rdLink.geteNodePid()){
					check.insertCheckLog("GLM01014", pointWkt, "[RD_LINK,"+rdLink.getPid()+"]", rdLink.getMeshId(), "TEST");
				}
				
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
				
				if (resultSet.next()){
					check.insertCheckLog("GLM01025", pointWkt, "[RD_LINK,"+rdLink.getPid()+"]", rdLink.getMeshId(), "TEST");
				}
				
				this.releadSource(pstmt, resultSet);
			}
		}
		
		
	}
	
	private void releadSource(Statement stmt,ResultSet resultSet) throws SQLException{
		resultSet.close();
		
		stmt.close();
	}
}
