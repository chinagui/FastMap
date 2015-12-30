package com.navinfo.dataservice.FosEngine.edit.operation.obj.rdcross.create;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.navinfo.dataservice.FosEngine.edit.log.LogWriter;
import com.navinfo.dataservice.FosEngine.edit.model.IRow;
import com.navinfo.dataservice.FosEngine.edit.model.Result;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.branch.RdBranchName;
import com.navinfo.dataservice.FosEngine.edit.model.selector.rd.branch.RdBranchNameSelector;
import com.navinfo.dataservice.FosEngine.edit.operation.ICommand;
import com.navinfo.dataservice.FosEngine.edit.operation.IOperation;
import com.navinfo.dataservice.FosEngine.edit.operation.IProcess;
import com.navinfo.dataservice.FosEngine.edit.operation.OperatorFactory;
import com.navinfo.dataservice.commons.db.DBOraclePoolManager;
import com.navinfo.dataservice.commons.exception.DataNotFoundException;

public class Process implements IProcess {

	private Command command;

	private Result result;

	private Connection conn;

	private String postCheckMsg;

	public Process(ICommand command) throws Exception {
		this.command = (Command) command;

		this.result = new Result();

		this.conn = DBOraclePoolManager.getConnection(this.command
				.getProjectId());

	}

	@Override
	public ICommand getCommand() {

		return command;
	}

	@Override
	public Result getResult() {

		return result;
	}

	@Override
	public boolean prepareData() throws Exception {

		return false;
	}

	@Override
	public String preCheck() throws Exception {
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			
			String s = "";
			for(int i=0;i<command.getNodePids().size();i++){
				s+=command.getNodePids().get(i);
				if(i!=command.getNodePids().size()-1){
					s+=",";
				}
			}
			
			String sql = "select count(1) count from rd_node_form where node_pid in ("+s+") and form_of_way=15";
			
			pstmt = this.conn.prepareStatement(sql);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {
				
				if(resultSet.getInt("count") > 0){
					return "障碍物属性的点不能与路口共存";
				}
			}
			
			pstmt.close();
			
			resultSet.close();
			
			sql = "select count(1) count from rd_cross_node a where a.node_pid in ("+s+") and exists (select * from rd_cross c where c.pid=a.pid and c.kg_flag=0)";
			
			pstmt = this.conn.prepareStatement(sql);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {
				
				if(resultSet.getInt("count") > 0){
					return "存在不合理数据，无法提交，请继续选择或者放弃编辑";
				}
			}

		} catch (Exception e) {

			throw e;

		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
			} catch (Exception e) {

			}

			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (Exception e) {

			}

		}

		return null;
	}

	@Override
	public String run() throws Exception {
		String msg;
		try {
			conn.setAutoCommit(false);

			this.prepareData();

			String preCheckMsg = this.preCheck();

			if (preCheckMsg != null) {
				throw new Exception(preCheckMsg);
			}

			IOperation operation = new Operation(command, conn);

			msg = operation.run(result);

			this.recordData();

			this.postCheck();

			conn.commit();

		} catch (Exception e) {

			conn.rollback();

			throw e;
		} finally {
			try {
				conn.close();
			} catch (Exception e) {

			}
		}

		return msg;
	}

	@Override
	public void postCheck() throws Exception {
	}

	@Override
	public String getPostCheck() throws Exception {

		return postCheckMsg;
	}

	@Override
	public boolean recordData() throws Exception {

		OperatorFactory.recordData(conn, result);

		LogWriter lw = new LogWriter(conn);

		lw.recordLog(command, result);

		return true;
	}
	
}
