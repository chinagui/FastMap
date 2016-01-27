package com.navinfo.dataservice.FosEngine.edit.operation.obj.rdlink.create;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import net.sf.json.JSONObject;

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
}
