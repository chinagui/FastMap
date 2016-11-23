package com.navinfo.dataservice.engine.editplus.batchAndCheck.common;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BatchRuleFactory {

	public BatchRuleFactory() {
		// TODO Auto-generated constructor stub
	}
	
	private Map<String, List<String>> operationMap = new HashMap<String, List<String>>();

	private static class SingletonHolder {
		private static final BatchRuleFactory INSTANCE = new BatchRuleFactory();
	}

	public static final BatchRuleFactory getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	public List<String> loadByOperationName(String operationName) throws Exception {
		if (!operationMap.containsKey(operationName)) {
			synchronized (this) {
				if (!operationMap.containsKey(operationName)) {
					try {
						
					} catch (Exception e) {
						throw new SQLException("获取批处理规则"+operationName+"失败："
								+ e.getMessage(), e);
					}
				}
			}
		}
		return operationMap.get(operationName);
	}

}
