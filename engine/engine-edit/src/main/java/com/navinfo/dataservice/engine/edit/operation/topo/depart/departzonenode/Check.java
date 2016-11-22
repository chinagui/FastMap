package com.navinfo.dataservice.engine.edit.operation.topo.depart.departzonenode;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;

import com.navinfo.dataservice.commons.mercator.MercatorProjection;

public class Check {

	// 形状点和形状点不能重合
	public void checkPointCoincide(double[][] ps) throws Exception {

		Set<String> set = new HashSet<String>();

		for (double[] p : ps) {
			set.add(p[0] + "," + ps[1]);
		}

		if (ps.length != set.size()) {
			throwException("形状点和形状点不能重合");
		}
	}

	// 对组成路口的node挂接的link线进行编辑操作时，不能分离组成路口的node点
	public void checkIsCrossNode(Connection conn, int nodePid) throws Exception {
		
		if(nodePid <= 0){
			return;
		}

		String sql = "select node_pid from rd_cross_node where node_pid = :1 and rownum =1";

		PreparedStatement pstmt = conn.prepareStatement(sql);

		pstmt.setInt(1, nodePid);

		ResultSet resultSet = pstmt.executeQuery();

		boolean flag = false;

		if (resultSet.next()) {
			flag = true;
		}
		
		resultSet.close();

		pstmt.close();

		if (flag) {
			
			throwException("对组成路口的node挂接的link线进行编辑操作时，不能分离组成路口的node点");
		}
	}
	
	//该线是经过线，移动该线造成线线关系（车信、线线交限、线线语音引导、线线分歧、线线顺行）从inLink到outlink的不连续
	public void checkIsVia(Connection conn,int linkPid) throws Exception
	{
		String sql = "select link_pid from rd_lane_via where link_pid =:1 and rownum=1 union all select link_pid from rd_restriction_via where link_pid =:2 and rownum=1 union all select link_pid from RD_VOICEGUIDE_VIA where link_pid =:3 and rownum=1 union all select link_pid from rd_branch_via where link_pid =:4 and rownum=1 union all select link_pid from rd_directroute_via where link_pid =:5 and rownum=1";

		PreparedStatement pstmt = conn.prepareStatement(sql);

		pstmt.setInt(1, linkPid);
		
		pstmt.setInt(2, linkPid);
		
		pstmt.setInt(3, linkPid);
		
		pstmt.setInt(4, linkPid);
		
		pstmt.setInt(5, linkPid);

		ResultSet resultSet = pstmt.executeQuery();

		boolean flag = false;

		if (resultSet.next()) {
			flag = true;
		}

		resultSet.close();

		pstmt.close();
		
		if (flag) {

			throwException("该线是经过线，移动该线造成线线关系（车信、线线交限、线线语音引导、线线分歧、线线顺行）从inLink到outlink的不连续");
		}
	}
	
	//相邻形状点不可过近，不能小于2m
	public void checkShapePointDistance(double[][] ps) throws Exception {
		
		for(int i=0;i<ps.length -1;i++){
			
			double smx = MercatorProjection.longitudeToMetersX(ps[i][0]);
			
			double smy = MercatorProjection.latitudeToMetersY(ps[i][0]);
			
			double emx = MercatorProjection.longitudeToMetersX(ps[i+1][0]);
			
			double emy = MercatorProjection.latitudeToMetersY(ps[i+1][0]);
			
			double distance = Math.pow(smx - emx, 2) + Math.pow(smy - emy, 2);
			
			if (distance <4){
				throwException("相邻形状点不可过近，不能小于2m");
			}
			
		}
		
	}

	private void throwException(String msg) throws Exception {
		throw new Exception(msg);
	}

}
