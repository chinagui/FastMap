package com.navinfo.dataservice.expcore.sql.replacer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.expcore.sql.ExpSQL;

/**
 * User: liuqing Date: 2010-9-29 Time: 14:31:06
 */
public class DefauleSqlReplacer implements SqlReplacer {

	/**
	 * 通过将参数存储在临时表中，然后使用临时表中的参数执行导出
	 * 
	 * @param expSQL
	 * @param statmentArgsMap
	 * @return
	 */
	public ExpSQL replaceByTempTable(ExpSQL expSQL, String condition) {
		String sql = expSQL.getSql();
		Map<String, String> replacedVars = new HashMap<String, String>();

		String meshReplaced = "in (select to_number(param_value) from TEMP_EXP_PARAMTERS where param_name='" + condition + "')";
		if (condition.equals("kind")||condition.equals("vmTaskId")) {
			meshReplaced = "in (select param_value from TEMP_EXP_PARAMTERS where param_name='" + condition + "')";
		}
		if (sql.indexOf(condition) > -1) {

			replacedVars.put(condition, meshReplaced);

		}
		sql = StringUtils.expandVariables(sql, replacedVars, "[", "]");
		expSQL.setSql(sql);

		return expSQL;
	}

	/**
	 * 通过将参数绑定的方式执行导出
	 * 
	 * @param expSQL
	 * @param statmentArgsMap
	 * @return
	 */
//	public ExpSQL replace(ExpSQL expSQL, Map<String, StatmentArgs> statmentArgsMap) {
//		String sql = expSQL.getSql();
//		Iterator<String> iterator = statmentArgsMap.keySet().iterator();
//		Map<String, String> replacedVars = new HashMap<String, String>();
//		while (iterator.hasNext()) {
//			String paramName = iterator.next();
//			StatmentArgs args = statmentArgsMap.get(paramName);
//
//			String sqlWhereCause = args.getBindVarsSql();
//
//			if (sql.indexOf(paramName) > -1) {
//				expSQL.addArgTypes(args.getArgTypes());
//				expSQL.addArgs(args.getArgs());
//				replacedVars.put(paramName, sqlWhereCause);
//
//			}
//
//		}
//		sql = StringUtils.expandVariables(sql, replacedVars, "[", "]");
//		expSQL.setSql(sql);
//
//		return expSQL;
//	}

}
