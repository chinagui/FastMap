package com.navinfo.dataservice.FosEngine.edit.operation.obj.rdrestriction.create;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.navinfo.navicommons.utils.StringUtils;


public class Check {

	public void checkGLM01017(Connection conn, Set<Integer> linkPids) throws Exception{
		
		String sql = "select link_pid from rd_link where kind in (11,13) and link_pid in ("+StringUtils.collection2String(linkPids, ",")+") and rownum=1";
		
		PreparedStatement pstmt = conn.prepareStatement(sql);

		ResultSet resultSet = pstmt.executeQuery();

		boolean flag = false;

		if (resultSet.next()) {
			flag = true;
		}

		resultSet.close();

		pstmt.close();
		
		if (flag) {

			throwException("“轮渡”和“人渡”的link不能作为交限（包括路口和线线结构里的所有交限）的进入线、退出线和经过线");
		}

	}
	
	public void checkGLM26017(Connection conn, int nodePid) throws Exception{
		
		String sql = "select node_pid from rd_cross_node where node_pid=:1";
		
		PreparedStatement pstmt = conn.prepareStatement(sql);

		pstmt.setInt(1, nodePid);
		
		ResultSet resultSet = pstmt.executeQuery();

		boolean flag = false;

		if (resultSet.next()) {
			flag = true;
		}

		resultSet.close();

		pstmt.close();
		
		if (!flag) {

			throwException("如果交限进入线和退出线挂接在同一点上，而且这个点未登记路口（不属于任何路口），则不允许制作和修改");
		}

	}
	
	public void checkNoSameRelation(Connection conn, int linkPid, int nodePid) throws Exception{
		
		String sql = "select pid from rd_restriction where in_link_pid=:1 and node_pid=:2";
		
		PreparedStatement pstmt = conn.prepareStatement(sql);

		pstmt.setInt(1, linkPid);
		
		pstmt.setInt(2, nodePid);
		
		ResultSet resultSet = pstmt.executeQuery();

		boolean flag = false;

		if (resultSet.next()) {
			flag = true;
		}

		resultSet.close();

		pstmt.close();
		
		if (flag) {

			throwException("相同的进入线、进入点不能创建两组车信");
		}

	}
	
	public void checkGLM08033(Connection conn, int inLinkPid, int outLinkPid) throws Exception{
		
		String sql = "select link_pid from rd_cross_link where link_pid in (:1,:2)";
		
		PreparedStatement pstmt = conn.prepareStatement(sql);

		pstmt.setInt(1, inLinkPid);
		
		pstmt.setInt(2, outLinkPid);
		
		ResultSet resultSet = pstmt.executeQuery();

		boolean flag = false;

		if (resultSet.next()) {
			flag = true;
		}

		resultSet.close();

		pstmt.close();
		
		if (flag) {

			throwException("路口交限的进入线，退出线不能是交叉口内link");
		}

	}
	
	public void checkGLM08004(Connection conn, int inLinkPid, List<Integer> outLinkPids) throws Exception{
		
		String str= String.valueOf(inLinkPid);
			
		for(int i=0;i<outLinkPids.size();i++){
			int pid = outLinkPids.get(i);
			
			str+=",";
			
			str+=pid;
		}
		
		String sql = "select form_of_way from rd_link_form where link_pid in ("+str+") and form_of_way in (20,22)";
		
		PreparedStatement pstmt = conn.prepareStatement(sql);

		ResultSet resultSet = pstmt.executeQuery();

		int formOfWay = 0;

		if (resultSet.next()) {
			
			formOfWay = resultSet.getInt("form_of_way");
		}

		resultSet.close();

		pstmt.close();
		
		if (formOfWay==20) {

			throwException("步行街不能作为交限（包括路口和线线结构里的所有交限）的进入线或者退出线");
		}
		else if(formOfWay == 22){
			throwException("公交车专用道不能作为交限（包括路口和线线结构里的所有交限）的进入线或者退出线");
		}

	}
	
	private void throwException(String msg) throws Exception {
		throw new Exception(msg);
	}
}
