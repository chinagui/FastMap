package com.navinfo.dataservice.dao.glm.iface;



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

}
