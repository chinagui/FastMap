package com.navinfo.dataservice.engine.edit.edit.model;

import java.sql.Statement;
import java.util.List;

/**
 * 操作类的接口
 */
public interface IOperator {

	/**
	 * 插入一行
	 * 
	 * @throws Exception
	 */
	public void insertRow() throws Exception;

	/**
	 * 更新一行
	 * 
	 * @throws Exception
	 */
	public void updateRow() throws Exception;

	/**
	 * 删除一行
	 * 
	 * @throws Exception
	 */
	public void deleteRow() throws Exception;

	/**
	 * 组装插入sql，并添加到statement
	 * 
	 * @param stmt
	 * @throws Exception
	 */
	public void insertRow2Sql(Statement stmt) throws Exception;

	/**
	 * 组装更新sql，并添加到statment
	 * 
	 * @param fieldNames
	 *            变化的字段列表
	 * @param stmt
	 * @throws Exception
	 */
	public void updateRow2Sql(List<String> fieldNames, Statement stmt)
			throws Exception;

	/**
	 * 组装删除sql，并添加到statement
	 * 
	 * @param stmt
	 * @throws Exception
	 */
	public void deleteRow2Sql(Statement stmt) throws Exception;
}
