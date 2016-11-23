package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.navinfo.dataservice.engine.editplus.model.batchAndCheck.BatchRule;

public class BatchRuleLoader {
	
	private Map<String, BatchRule> batchRuleMap = new HashMap<String, BatchRule>();

	private static class SingletonHolder {
		private static final BatchRuleLoader INSTANCE = new BatchRuleLoader();
	}

	public static final BatchRuleLoader getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	public BatchRule loadByRuleId(String ruleId) throws Exception {
		if (!batchRuleMap.containsKey(ruleId)) {
			synchronized (this) {
				if (!batchRuleMap.containsKey(ruleId)) {
					try {
						
					} catch (Exception e) {
						throw new SQLException("获取批处理规则"+ruleId+"失败："
								+ e.getMessage(), e);
					}
				}
			}
		}
		return batchRuleMap.get(ruleId);
	}

}
