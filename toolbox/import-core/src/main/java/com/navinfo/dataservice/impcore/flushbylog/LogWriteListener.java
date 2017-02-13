package com.navinfo.dataservice.impcore.flushbylog;
/*
 * @author MaYunFei
 * 2016年6月16日
 * 描述：import-coreLogWriteListener.java
 */
public class LogWriteListener implements ILogWriteListener {
	FlushResult flushResult;
	public LogWriteListener(FlushResult flushResult) {
		super();
		this.flushResult = flushResult;
	}

	@Override
	public void preInsert() {
		flushResult.addInsertTotal();
	}

	@Override
	public void insertFail(EditLog editLog,String log) {
		flushResult.addInsertFailed();
		flushResult.addInsertFailedRowId(editLog.getRowId());
		flushResult.insertFailedLog(editLog.getOpId(), editLog.getRowId(),log);
	}

	@Override
	public void preUpdate() {
		flushResult.addUpdateTotal();

	}

	@Override
	public void updateFailed(EditLog editLog,String log) {
		flushResult.addUpdateFailed();
		flushResult.addUpdateFailedRowId(editLog.getRowId());
		flushResult.insertFailedLog(editLog.getOpId(), editLog.getRowId(),log);

	}

	@Override
	public void preDelete() {
		flushResult.addDeleteTotal();

	}

	@Override
	public void deleteFailed(EditLog editLog,String log) {
		flushResult.addDeleteFailed();
		flushResult.addDeleteFailedRowId(editLog.getRowId());
		flushResult.insertFailedLog(editLog.getOpId(), editLog.getRowId(),log);

	}

}

