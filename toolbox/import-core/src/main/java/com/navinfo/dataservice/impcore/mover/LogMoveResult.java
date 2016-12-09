package com.navinfo.dataservice.impcore.mover;
/*
 * @author MaYunFei
 * 2016年6月21日
 * 描述：import-coreLogMoveResult.java
 */
public class LogMoveResult {
	private int logActionMoveCount;
	private int logDetailMoveCount;
	private int logDetailGridMoveCount;
	private int logOperationMoveCount;
	private String logOperationTempTable;
	
	public int getLogActionMoveCount() {
		return logActionMoveCount;
	}
	public void setLogActionMoveCount(int logActionMoveCount) {
		this.logActionMoveCount = logActionMoveCount;
	}
	public int getLogDetailMoveCount() {
		return logDetailMoveCount;
	}
	public void setLogDetailMoveCount(int logDetailMoveCount) {
		this.logDetailMoveCount = logDetailMoveCount;
	}
	public int getLogDetailGridMoveCount() {
		return logDetailGridMoveCount;
	}
	public void setLogDetailGridMoveCount(int logDetailGridMoveCount) {
		this.logDetailGridMoveCount = logDetailGridMoveCount;
	}
	public int getLogOperationMoveCount() {
		return logOperationMoveCount;
	}
	public void setLogOperationMoveCount(int logOperationMoveCount) {
		this.logOperationMoveCount = logOperationMoveCount;
	}
	public String getLogOperationTempTable() {
		return logOperationTempTable;
	}
	/**设置目标库的log_operation临时表(记录从日库搬移的log_operation的op_Id,op_dt)
	 * @param tarTempTable
	 */
	public void setLogOperationTempTable(String tarTempTable) {
		this.logOperationTempTable = tarTempTable;
		
	}
	
	
	
}

