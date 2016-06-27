package com.navinfo.dataservice.impcore.flushbylog;
/*
 * @author MaYunFei
 * 2016年6月16日
 * 描述：import-coreILogWriteListener.java
 */
public interface ILogWriteListener {

	void preInsert();

	void insertFail(EditLog editLog);

	void preUpdate();

	void updateFailed(EditLog editLog);

	void preDelete();

	void deleteFailed(EditLog editLog);

}

