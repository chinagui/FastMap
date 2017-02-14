package com.navinfo.dataservice.impcore.flushbylog;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.commons.util.NaviListUtils;
import com.navinfo.navicommons.database.QueryRunner;

public class FlushResult {
	
	private String resultMsg;
	
	private int total;

	private int updateTotal;

	private int deleteTotal;

	private int insertTotal;

	private int failedTotal=0;

	private int updateFailed;

	private int deleteFailed;

	private int insertFailed;

	private int logOpMoved;
	private int logDetailMoved;
	private int logDetailGridMoved;
	private List<List> failedLog=new ArrayList<List>();

	private List<String> insertFailedList = new ArrayList<String>();

	private List<String> updateFailedList = new ArrayList<String>();

	private List<String> deleteFailedList = new ArrayList<String>();

	private String tempFailLogTable;

	public void insertFailedLog(String opId,String rowId,String log){
		List<String> row = new ArrayList<String>();
		row.add(opId);
		row.add(rowId);
		row.add(log);
		failedLog.add(row);
	}
	public List<List> getFailedLog() {
		return failedLog;
	}
	public String getResultMsg() {
		return resultMsg;
	}

	public void setResultMsg(String resultMsg) {
		this.resultMsg = resultMsg;
	}

	public int getLogOpMoved() {
		return logOpMoved;
	}

	public void setLogOpMoved(int logOpMoved) {
		this.logOpMoved = logOpMoved;
	}

	public int getLogDetailMoved() {
		return logDetailMoved;
	}

	public void setLogDetailMoved(int logDetailMoved) {
		this.logDetailMoved = logDetailMoved;
	}

	public int getLogDetailGridMoved() {
		return logDetailGridMoved;
	}

	public void setLogDetailGridMoved(int logDetailGridMoved) {
		this.logDetailGridMoved = logDetailGridMoved;
	}

	public void addInsertFailedRowId(String rowId) {
		insertFailedList.add(rowId);
	}

	public void addDeleteFailedRowId(String rowId) {
		deleteFailedList.add(rowId);
	}

	public void addUpdateFailedRowId(String rowId) {
		updateFailedList.add(rowId);
	}

	public int getFailedTotal() {
		return this.getDeleteFailed()+this.getInsertFailed()+this.getUpdateFailed();
	}

//	public void setFailedTotal(int failedTotal) {
//		this.failedTotal = failedTotal;
//	}

	public List<String> getInsertFailedList() {
		return insertFailedList;
	}

	public void setInsertFailedList(List<String> insertFailedList) {
		this.insertFailedList = insertFailedList;
	}

	public List<String> getUpdateFailedList() {
		return updateFailedList;
	}

	public void setUpdateFailedList(List<String> updateFailedList) {
		this.updateFailedList = updateFailedList;
	}

	public List<String> getDeleteFailedList() {
		return deleteFailedList;
	}

	public void setDeleteFailedList(List<String> deleteFailedList) {
		this.deleteFailedList = deleteFailedList;
	}

	public void addTotal() {
		total += 1;
	}

	public void addUpdateTotal() {
		updateTotal += 1;
	}

	public void addDeleteTotal() {
		deleteTotal += 1;
	}

	public void addInsertTotal() {
		insertTotal += 1;
	}

	public void addUpdateFailed() {
		updateFailed += 1;
	}

	public void addDeleteFailed() {
		deleteFailed += 1;
	}

	public void addInsertFailed() {
		insertFailed += 1;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public int getUpdateTotal() {
		return updateTotal;
	}

	public void setUpdateTotal(int updateTotal) {
		this.updateTotal = updateTotal;
	}

	public int getDeleteTotal() {
		return deleteTotal;
	}

	public void setDeleteTotal(int deleteTotal) {
		this.deleteTotal = deleteTotal;
	}

	public int getInsertTotal() {
		return insertTotal;
	}

	public void setInsertTotal(int insertTotal) {
		this.insertTotal = insertTotal;
	}

	public int getUpdateFailed() {
		return updateFailed;
	}

	public void setUpdateFailed(int updateFailed) {
		this.updateFailed = updateFailed;
	}

	public int getDeleteFailed() {
		return deleteFailed;
	}

	public void setDeleteFailed(int deleteFailed) {
		this.deleteFailed = deleteFailed;
	}

	public int getInsertFailed() {
		return insertFailed;
	}

	public void setInsertFailed(int insertFailed) {
		this.insertFailed = insertFailed;
	}
	
	public boolean isSuccess() {
		return getFailedTotal()==0;
	}
	
	public void print() {
		System.out.println("Flush Status:"+this.getResultMsg());
		System.out.println("Total:" + this.getTotal());

		System.out.println("Insert total:" + this.getInsertTotal());

		System.out.println("Update total:" + this.getUpdateTotal());

		System.out.println("Delete total:" + this.getDeleteTotal());

		System.out.println("Failed total:" + this.getFailedTotal());

		System.out.println("Insert failed:" + this.getInsertFailed());

		if (this.getInsertFailed() > 0) {
			System.out.println("RowIds:" + this.getInsertFailedList());
		}

		System.out.println("Update failed:" + this.getUpdateFailed());

		if (this.getUpdateFailed() > 0) {
			System.out.println("RowIds:" + this.getUpdateFailedList());
		}

		System.out.println("Delete failed:" + this.getDeleteFailed());

		if (this.getDeleteFailed() > 0) {
			System.out.println("RowIds:" + this.getDeleteFailedList());
		}

		System.out.println("Log op moved:" + this.getLogOpMoved());
		System.out.println("Log detail moved:" + this.getLogDetailMoved());
		System.out.println("Log detail grid moved:" + this.getLogDetailGridMoved());
	}
	
	public String getTempFailLogTable() {
		return tempFailLogTable;
	}
	public void setTempFailLogTable(String tempFailLogTable) {
		this.tempFailLogTable = tempFailLogTable;
	}
	public void recordFailLog2Temptable(Connection conn) throws Exception{
		if (this.isSuccess()) return ;
		QueryRunner run = new QueryRunner();
		String sql = "insert into "+tempFailLogTable+" values(?,?,?)";
		Object[][] batchParams = NaviListUtils.toArrayMatrix(this.getFailedLog());
		run.batch(conn, sql, batchParams);
	}
}