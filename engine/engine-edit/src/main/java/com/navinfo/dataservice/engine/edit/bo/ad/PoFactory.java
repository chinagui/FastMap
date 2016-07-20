package com.navinfo.dataservice.engine.edit.bo.ad;

import java.sql.Connection;
import java.util.List;

public class PoFactory {

	private volatile static PoFactory instance;

	public static PoFactory getInstance() {
		if (instance == null) {
			synchronized (OperatorFactory.class) {
				if (instance == null) {
					instance = new PoFactory();
				}
			}
		}
		return instance;
	}

	private PoFactory() {

	}
	

	public <T> T getByPK(Connection conn, Class<T> clazz, int pkValue, boolean isLock){
		return null;
	}
	
	public <T> T getByRowId(Connection conn, Class<T> clazz, String rowId, boolean isLock){
		return null;
	}
	
	public <T> List<T> getByFK(Connection conn, Class<T> clazz, String fkName, int fkValue, boolean isLock){
		return null;
	}
}
