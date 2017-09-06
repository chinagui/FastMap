package com.navinfo.dataservice.impcore.flushbylog;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.commons.thread.ThreadSharedObjectExt;

public class ThreadSharedObjectExtResult extends ThreadSharedObjectExt {

	private List<FlushResult> flushResults = new ArrayList<FlushResult>();

	public ThreadSharedObjectExtResult(int totalTaskNum) {
		super(totalTaskNum);
	}

	public void addFlushResult(FlushResult flushResult) {
		flushResults.add(flushResult);
	}

	public List<FlushResult> getaddFlushResults() {
		return flushResults;
	}

	public FlushResult combineFlushResults(List<FlushResult> flushResults) {
		FlushResult flushResult = new FlushResult();
		int deleteFailed = 0;
		int deleteTotal = 0;
		int failedTotal = 0;
		int insertFailed = 0;
		int insertTotal = 0;
		int updateFailed = 0;
		int updateTotal = 0;
		int total = 0;

		List<String> deleteFailedList = new ArrayList<String>();
		@SuppressWarnings("rawtypes")
		List<List> failedLog = new ArrayList<List>();
		List<String> insertFailedList = new ArrayList<String>();
		List<String> updateFailedList = new ArrayList<String>();
		for (FlushResult result : flushResults) {
			deleteFailed += result.getDeleteFailed();
			deleteFailedList.addAll(result.getDeleteFailedList());
			failedTotal += result.getFailedTotal();
			failedLog.addAll(result.getFailedLog());
			deleteTotal += result.getDeleteTotal();
			insertFailed += result.getInsertFailed();
			insertFailedList.addAll(result.getInsertFailedList());
			insertTotal += result.getInsertFailed();
			updateFailed += result.getUpdateFailed();
			updateFailedList.addAll(result.getUpdateFailedList());
			updateTotal += result.getUpdateTotal();
			total += result.getTotal();

		}
		flushResult.setDeleteFailed(deleteFailed);
		flushResult.setDeleteFailedList(deleteFailedList);

		flushResult.setDeleteTotal(deleteTotal);
		flushResult.setInsertFailed(insertFailed);
		flushResult.setInsertFailedList(insertFailedList);
		flushResult.setInsertTotal(insertTotal);
		flushResult.setUpdateFailed(updateFailed);
		flushResult.setUpdateFailedList(updateFailedList);
		flushResult.setUpdateTotal(updateTotal);
		flushResult.setTotal(total);
		flushResult.setFailedLog(failedLog);
		flushResult.setFailedTotal(failedTotal);

		return flushResult;

	}

}
