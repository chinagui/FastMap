package com.navinfo.dataservice.engine.edit.operation.obj.rdcross.create;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

public class Check {

	public void checkSideNode(Connection conn, List<Integer> nodePids)
			throws Exception {

		String sql = "select count(1) count from rd_link where e_node_pid=:1 or s_node_pid=:2";

		PreparedStatement pstmt = null;
		try{
			pstmt = conn.prepareStatement(sql);
			for (int nodePid : nodePids) {
	
				pstmt.setInt(1, nodePid);
	
				pstmt.setInt(2, nodePid);
	
				ResultSet resultSet = null;
				try{
					resultSet = pstmt.executeQuery();
		
					boolean flag = false;
		
					if (resultSet.next()) {
		
						int count = resultSet.getInt("count");
		
						if (count <= 1) {
							flag = true;
						}
					}
					if (flag) {
						throwException("盲端不允许创建路口");
					}
				}finally{
					this.releaseStatementAndResultSet(null, resultSet);
				}
			}
		}finally{
			this.releaseStatementAndResultSet(pstmt, null);
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
	public void checkNodeForm(Connection conn, List<Integer> nodePids) throws Exception {

		String s = "";
		for(int i=0;i<nodePids.size();i++){
			s+=nodePids.get(i);
			if(i!=nodePids.size()-1){
				s+=",";
			}
		}
		
		String sql = "select count(1) count from rd_node_form where node_pid in ("+s+") and form_of_way=15";
		
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try{
			pstmt= conn.prepareStatement(sql);
			resultSet = pstmt.executeQuery();
			boolean flag = false;
	
			if (resultSet.next()) {
	
				int count = resultSet.getInt("count");
	
				if (count>0) {
					flag = true;
				}
			}
	
			if (flag) {
				throwException("障碍物属性的点不能与路口共存");
			}
		}finally{
			this.releaseStatementAndResultSet(pstmt, resultSet);
		}

	}

	public void checkNodeInCross(Connection conn, List<Integer> nodePids) throws Exception {

		String s = "";
		for(int i=0;i<nodePids.size();i++){
			s+=nodePids.get(i);
			if(i!=nodePids.size()-1){
				s+=",";
			}
		}
		
		String sql = "select count(1) count from rd_cross_node a where a.node_pid in ("+s+") and a.u_record!=2 and exists (select null from rd_cross c where c.pid=a.pid and c.kg_flag=0 and c.u_record!=2)";
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try{	
			pstmt= conn.prepareStatement(sql);
	
			resultSet = pstmt.executeQuery();
	
			boolean flag = false;
	
			if (resultSet.next()) {
	
				int count = resultSet.getInt("count");
	
				if (count>0) {
					flag = true;
				}
			}
			if (flag) {
				throwException("存在不合理数据，无法提交，请继续选择或者放弃编辑");
			}
		}finally{
			this.releaseStatementAndResultSet(pstmt, resultSet);
		}
	}

	private void throwException(String msg) throws Exception {
		throw new Exception(msg);
	}

}
