package com.navinfo.dataservice.engine.editplus.model.batchAndCheck;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CheckRuleCommand extends BasicRuleCommand {
	/* IX_POI对象的检查错误，按照pid查找规则号的集合
	 * key:ObjectName.java中的对象名 
	 * value：Map<Long, Set<String>> key:pid value:检查规则号集合
	 */
	private Map<String, Map<Long, Set<String>>> errorPidRuleMap;
	public CheckRuleCommand() {
		// TODO Auto-generated constructor stub
	}
	public Map<String, Map<Long, Set<String>>> getErrorPidRuleMap() {
		return errorPidRuleMap;
	}
	public void setErrorPidRuleMap(Map<String, Map<Long, Set<String>>> errorPidRuleMap) {
		this.errorPidRuleMap = errorPidRuleMap;
	}
	public void setErrorPidRuleMap(String objName,Long pid,String ruleId) {
		if(this.errorPidRuleMap==null){this.errorPidRuleMap=new HashMap<String, Map<Long,Set<String>>>();}
		if(!this.errorPidRuleMap.containsKey(objName)){
			this.errorPidRuleMap.put(objName, new HashMap<Long, Set<String>>());
		}
		Map<Long, Set<String>> objMap=this.errorPidRuleMap.get(objName);
		if(!objMap.containsKey(pid)){
			objMap.put(pid, new HashSet<String>());
		}
		objMap.get(pid).add(ruleId);
	}

}
