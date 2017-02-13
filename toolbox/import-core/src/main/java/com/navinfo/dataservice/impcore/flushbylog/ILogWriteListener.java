package com.navinfo.dataservice.impcore.flushbylog;
/*
 * @author MaYunFei
 * 2016年6月16日
 * 描述：import-coreILogWriteListener.java
 */
public interface ILogWriteListener {

	void preInsert();

	void insertFail(EditLog editLog,String log);

	void preUpdate();

	void updateFailed(EditLog editLog,String log);

	void preDelete();

	void deleteFailed(EditLog editLog,String log);

}

