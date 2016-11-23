package com.navinfo.dataservice.engine.editplus.batchAndCheck.check;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.navinfo.dataservice.engine.editplus.model.batchAndCheck.CheckRule;

public class CheckRuleLoader {
	
	private Map<String, CheckRule> checkRuleMap = new HashMap<String, CheckRule>();

	private static class SingletonHolder {
		private static final CheckRuleLoader INSTANCE = new CheckRuleLoader();
	}

	public static final CheckRuleLoader getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	public CheckRule loadByRuleId(String ruleId) throws Exception {
		if (!checkRuleMap.containsKey(ruleId)) {
			synchronized (this) {
				if (!checkRuleMap.containsKey(ruleId)) {
					try {
						
					} catch (Exception e) {
						throw new SQLException("获取检查规则"+ruleId+"失败："
								+ e.getMessage(), e);
					}
				}
			}
		}
		return checkRuleMap.get(ruleId);
	}

}
