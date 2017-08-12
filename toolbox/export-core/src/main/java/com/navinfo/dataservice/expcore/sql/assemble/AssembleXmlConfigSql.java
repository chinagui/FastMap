package com.navinfo.dataservice.expcore.sql.assemble;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.util.Assert;

import com.navinfo.dataservice.bizcommons.sql.ExpSQL;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.expcore.exception.ExportException;
import com.navinfo.dataservice.expcore.sql.ExpXMLSQLManager;
import com.navinfo.dataservice.expcore.sql.replacer.SqlReplacer;

/**
 * Created by IntelliJ IDEA. User: liuqing Date: 11-5-9 Time: 下午6:25
 * 装配某个Expsource 的导出sql
 */
public class AssembleXmlConfigSql implements AssembleSql {

	protected static String TEMP_SQL_FIND_REGEX = "(TEMP_\\w+)|(TEMP_\\w+$) ";

	protected Logger log = LoggerRepos.getLogger(this.getClass());
	protected String feature;
	protected String condition;
	protected List<String> conditionParams;
	protected String mainFile;
	protected SqlReplacer replacer;
	


	/**
	 * @param exportSource
	 * @throws Exception
	 */

	public AssembleXmlConfigSql(String feature,String condition,List<String> conditionParams) throws ClassNotFoundException,Exception{
		this.feature=feature;
		this.condition=condition;
		this.conditionParams=conditionParams;
		ExpMainConfig expMainConf=ExpMainConfigManager.getInstance().getExpMainConfig(feature, condition);
		log.info(expMainConf.getReplacerClassName());
		Class repClass = Class.forName(expMainConf.getReplacerClassName());
		this.mainFile=expMainConf.getMainScript();
		this.replacer = (SqlReplacer) repClass.newInstance();
	}


	/**
	 * 根据导出参数配置文件，将符合配置文件的sql按执行步骤step进行合并，并将具体的参数替换sql中的参数变量
	 * @param gdbVersion
	 * @param tempTableSuffix
	 * @return
	 * @throws ExportException
	 */
	public Map<Integer, List<ExpSQL>> assemble(String gdbVersion,String tempTableSuffix) throws ExportException {
		long t1 = System.currentTimeMillis();

		Map<Integer, List<ExpSQL>> execSqlMap=new TreeMap<Integer, List<ExpSQL>>();;
		try {
			Set<String> sqlSet = new HashSet<String>();
			log.debug("按配置文件导出:" + mainFile);
			
			Map<String, Map<Integer, List<ExpSQL>>> fileSqlMap = ExpXMLSQLManager.getInstance().getFileSqlMap(
					gdbVersion);

			Set<Map.Entry<Integer, List<ExpSQL>>> sqlEntrySet = fileSqlMap.get(mainFile).entrySet();

			for (Iterator iterator = sqlEntrySet.iterator(); iterator.hasNext();) {
				Map.Entry sqlEntry = (Map.Entry) iterator.next();
				Integer step = (Integer) sqlEntry.getKey();
				List<ExpSQL> sqlList = (List<ExpSQL>) sqlEntry.getValue();
				//根据condition过滤sql
				sqlList = filterExpSqlByExpType(condition, sqlList);
				// log.debug("装配步骤" + step + " 的sql");
				List<ExpSQL> expandVarSqlList = expandSqlVariables(
						sqlList,
						sqlSet,
						tempTableSuffix);
				List<ExpSQL> existSqlList = execSqlMap.get(step);
				if (existSqlList == null) {
					existSqlList = expandVarSqlList;
				} else {
					existSqlList.addAll(expandVarSqlList);
				}

				execSqlMap.put(step, existSqlList);

			}
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new ExportException("根据导出参数和导出xml配置，生成最终导出的sql集合异常。",e);
		}
		long t2 = System.currentTimeMillis();
		log.debug((t2 - t1) + "ms");
		return execSqlMap;
	}

	private List<ExpSQL> filterExpSqlByExpType(String expType, List<ExpSQL> sqlList) {
		List<ExpSQL> sqlLi1 = new ArrayList<ExpSQL>();
		// 根据导出类型，过滤不符合导出类型的sql
		for (int j = 0; j < sqlList.size(); j++) {
			ExpSQL expSQL = sqlList.get(j);
			String sqlCondition = expSQL.getCondition();
			if (StringUtils.isBlank(sqlCondition) || sqlCondition.equals(expType)) {
				sqlLi1.add(expSQL);
			}
		}
		return sqlLi1;
	}

	/**
	 * 将参数替换sql中需要替换的部分 并将重复sql去重
	 * 
	 * @param condition
	 * @param sqlList
	 * @param tempTableSuffix
	 * @return
	 */
	private List<ExpSQL> expandSqlVariables(
			List<ExpSQL> sqlList,
			Set<String> sqlSet,
			String tempTableSuffix) {
		// 
		List<ExpSQL> formatedSqlList = new ArrayList<ExpSQL>();
		for (int j = 0; j < sqlList.size(); j++) {
			ExpSQL expSQL = sqlList.get(j);
			ExpSQL newExpSQL = new ExpSQL(expSQL.getSqlId(), expSQL.getSql());
			newExpSQL.setSqlType(expSQL.getSqlType());
			newExpSQL.setSqlExtendType(expSQL.getSqlExtendType());
			newExpSQL = replacer.replaceByTempTable(newExpSQL, condition);
			String executableSql = replaceTempTable(newExpSQL.getSql(), tempTableSuffix);
			newExpSQL.setSql(executableSql);
			/*
			 * log.debug("////////////////////////////////////////////");
			 * log.debug(executableSql+" "+statmentArgs.getBindVarsSql());
			 * log.debug("////////////////////////////////////////////");
			 */
			if (!dupSql(sqlSet, executableSql)) {
				// sql去重
				sqlSet.add(executableSql);
				formatedSqlList.add(newExpSQL);
			}

		}
		return formatedSqlList;
	}

	/**
	 * 过滤重复select 语句
	 * 
	 * @param sqlSet
	 * @param executableSql
	 * @return
	 */
	private boolean dupSql(Set<String> sqlSet, String executableSql) {
		if (executableSql.startsWith("SELECT") && sqlSet.contains(executableSql)) {
			return true;
		}
		return false;

	}

	/**
	 * 将sql中的临时表名替换正每个导出应用的使用的临时表表名
	 * 
	 * @param sql
	 */
	public String replaceTempTable(String sql, String tempTableSuffix) {
		Assert.notNull(tempTableSuffix);
		return sql.replaceAll(TEMP_SQL_FIND_REGEX, "$0" + "_" + tempTableSuffix);
	}

	public static void main(String[] args) {
		String sql = "INTO TEMP_IX_POI\n" +
				"                          (PID)\n" +
				"                          (SELECT  /*+INDEX(T TEMP_IDX_FIPOI)*/ C.CHILD_POI_PID\n" +
				"                              FROM IX_POI_PARENT P, IX_POI_CHILDREN C, TEMP_FILTER_IX_POI T\n" +
				"                             WHERE P.GROUP_ID = C.GROUP_ID\n" +
				"                               AND P.PARENT_POI_PID = T.PID)";
		sql = sql.replaceAll(TEMP_SQL_FIND_REGEX, "$0" + "_" + "1");
		System.out.println(sql);

	}

}
