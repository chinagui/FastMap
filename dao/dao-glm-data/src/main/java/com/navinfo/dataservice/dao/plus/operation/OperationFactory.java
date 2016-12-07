package com.navinfo.dataservice.dao.plus.operation;

import java.sql.Connection;

import net.sf.json.JSONObject;

/**
 * @ClassName: OperatorFactory
 * @author xiaoxiaowen4127
 * @date 2016年7月15日
 * @Description: OperatorFactory.java
 */
public class OperationFactory {
	private volatile static OperationFactory instance;

	public static OperationFactory getInstance() {
		if (instance == null) {
			synchronized (OperationFactory.class) {
				if (instance == null) {
					instance = new OperationFactory();
				}
			}
		}
		return instance;
	}

	private OperationFactory() {

	}

	public <T extends Connection> AbstractOperation create(String name, T conn) throws Exception {

//		Class<?> clazz = Class.forName(objType + opName + "Operator");
//		EditOperation op = (EditOperation) clazz.newInstance();
//
//		op.createCmd(data);
		
		return null;
	}

}
