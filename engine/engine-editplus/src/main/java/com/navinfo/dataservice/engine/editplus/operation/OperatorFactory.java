package com.navinfo.dataservice.engine.editplus.operation;

import net.sf.json.JSONObject;

/**
 * @ClassName: OperatorFactory
 * @author xiaoxiaowen4127
 * @date 2016年7月15日
 * @Description: OperatorFactory.java
 */
public class OperatorFactory {
	private volatile static OperatorFactory instance;

	public static OperatorFactory getInstance() {
		if (instance == null) {
			synchronized (OperatorFactory.class) {
				if (instance == null) {
					instance = new OperatorFactory();
				}
			}
		}
		return instance;
	}

	private OperatorFactory() {

	}

	public AbstractOperation create(String opType, String objType,
			JSONObject data) throws Exception {

		Class<?> clazz = Class.forName(objType + opType + "Operator");
		AbstractOperation op = (AbstractOperation) clazz.newInstance();

		op.createCmd(data);
		
		return op;
	}

}
