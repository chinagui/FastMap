package com.navinfo.dataservice.commons.thread;

public class ThreadSharedObjectExt extends ThreadSharedObject {
	StringBuilder warnStringBuilder = new StringBuilder();
	int warnTotal = 0;
	int sqlExeTotal = 0;

	public ThreadSharedObjectExt(int totalTaskNum) {
		super(totalTaskNum);
		// TODO Auto-generated constructor stub
	}

	public synchronized void addWarn(Exception e) {
		warnStringBuilder.append(e);
		warnTotal++;
	}

	public StringBuilder getWarns() {
		return warnStringBuilder;
	}

	public synchronized void sqlExeTotalPlus1() {
		sqlExeTotal++;
	}

	public int getSqlExeTotal() {
		return sqlExeTotal;
	}

	public int getWarnTotal() {
		return warnTotal;
	}

	public static void main(String[] args) {
		ThreadSharedObjectExt threadSharedObjectExt = new ThreadSharedObjectExt(
				1);
		threadSharedObjectExt.addWarn(new Exception("test"));
		System.out.println(threadSharedObjectExt.warnStringBuilder.toString());
	}
}
