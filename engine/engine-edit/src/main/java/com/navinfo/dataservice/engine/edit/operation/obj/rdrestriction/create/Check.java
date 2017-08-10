package com.navinfo.dataservice.engine.edit.operation.obj.rdrestriction.create;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

public class Check {

	public void checkGLM01017(Connection conn, Set<Integer> linkPids) throws Exception {

		String sql = "select link_pid from rd_link where kind in (11,13) and link_pid in ("
				+ StringUtils.join(linkPids, ",") + ") and rownum=1";

		Statement pstmt = null;

		ResultSet resultSet = null;
		try{
			pstmt = conn.createStatement();
			resultSet = pstmt.executeQuery(sql);
			
			if (resultSet.next()) {
				throwException("“轮渡”和“人渡”的link不能作为交限（包括路口和线线结构里的所有交限）的进入线、退出线和经过线");
			}
		}finally{
			releaseStatementAndResultSet(pstmt, resultSet);
		}
		

		

	}

	private void releaseStatementAndResultSet(Statement pstmt, ResultSet resultSet) {
		try{
			if(resultSet!=null) resultSet.close();
		}catch(Exception e){
			//do nothing
		}
		try{
			if(pstmt!=null) pstmt.close();
		}catch(Exception e){
			//do nothing
		}
	}

	public void checkGLM26017(Connection conn, int nodePid) throws Exception {

		String sql = "select node_pid from rd_cross_node where node_pid=:1 and u_record != 2";

		PreparedStatement pstmt = null;
		ResultSet resultSet =null;
		try{
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, nodePid);

			resultSet = pstmt.executeQuery();

			boolean flag = false;

			if (resultSet.next()) {
				flag = true;
			}
			if (!flag) {

				throwException("如果交限进入线和退出线挂接在同一点上，而且这个点未登记路口（不属于任何路口），则不允许制作和修改");
			}
		}finally{
			releaseStatementAndResultSet(pstmt, resultSet);
		}

		

		

		

	}

	public void checkGLM08033(Connection conn, int inLinkPid, int outLinkPid) throws Exception {

		String sql = "select link_pid from rd_cross_link where link_pid in (:1,:2) and u_record != 2";

		PreparedStatement pstmt = null;
		ResultSet resultSet =null;
		try{
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, inLinkPid);
	
			pstmt.setInt(2, outLinkPid);
	
			resultSet = pstmt.executeQuery();
	
			boolean flag = false;
	
			if (resultSet.next()) {
				flag = true;
			}
	
			if (flag) {
	
				throwException("路口交限的进入线，退出线不能是交叉口内link");
			}
		}finally{
			this.releaseStatementAndResultSet(pstmt, resultSet);	
		}

	}

	public void checkGLM08004(Connection conn, int inLinkPid, List<Integer> outLinkPids) throws Exception {

		String str = String.valueOf(inLinkPid);

		for (int i = 0; i < outLinkPids.size(); i++) {
			int pid = outLinkPids.get(i);

			str += ",";

			str += pid;
		}
		PreparedStatement pstmt =null;
		ResultSet resultSet =null;
		String sql = "select form_of_way from rd_link_form where link_pid in (" + str + ") and form_of_way in (20,22)";
		try{
			pstmt = conn.prepareStatement(sql);
	
			resultSet = pstmt.executeQuery();
	
			int formOfWay = 0;
	
			if (resultSet.next()) {
	
				formOfWay = resultSet.getInt("form_of_way");
			}
	
	
			if (formOfWay == 20) {
	
				throwException("步行街不能作为交限（包括路口和线线结构里的所有交限）的进入线或者退出线");
			} else if (formOfWay == 22) {
				throwException("公交车专用道不能作为交限（包括路口和线线结构里的所有交限）的进入线或者退出线");
			}
		}finally{
			this.releaseStatementAndResultSet(pstmt, resultSet);	
		}

	}

	public void checkSameInAndOutLink(int inLinkPid, Map<Integer, String> infoMap) throws Exception {
		if(infoMap.size()>0)
		{
			if (infoMap.containsKey(inLinkPid)) {
				throwException("进入线和退出线不能相同");
			}
		}
	}

	private void throwException(String msg) throws Exception {
		throw new Exception(msg);
	}
}
