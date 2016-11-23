package com.navinfo.dataservice.engine.editplus.batchAndCheck.common;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckRuleFactory {

	public CheckRuleFactory() {
		// TODO Auto-generated constructor stub
	}
	
	private Map<String, List<String>> operationMap = new HashMap<String, List<String>>();

	private static class SingletonHolder {
		private static final CheckRuleFactory INSTANCE = new CheckRuleFactory();
	}

	public static final CheckRuleFactory getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	public List<String> loadByOperationName(String operationName) throws Exception {
		if (!operationMap.containsKey(operationName)) {
			synchronized (this) {
				if (!operationMap.containsKey(operationName)) {
					try {
						
					} catch (Exception e) {
						throw new SQLException("获取检查规则"+operationName+"失败："
								+ e.getMessage(), e);
					}
				}
			}
		}
		return operationMap.get(operationName);
	}

}
