package com.navinfo.dataservice.dao.glm.iface;

import java.util.List;

/**
 * 查询类的接口
 */
public interface ISelector {
	/**
	 * 根据主键id获取一行记录
	 * 
	 * @param id
	 *            主键id
	 * @param isLock
	 *            是否加锁
	 * @return IRow
	 * @throws Exception
	 */
	public IRow loadById(int id, boolean isLock) throws Exception;

	/**
	 * 根据rowid获取一行记录
	 * 
	 * @param rowId
	 *            rowId
	 * @param isLock
	 *            是否加锁
	 * @return IRow
	 * @throws Exception
	 */
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception;

	/**
	 * 根据主表主键id获取所有的子表
	 * 
	 * @param id
	 *            主表的主键id
	 * @param isLock
	 *            是否加锁
	 * @return 子表的列表
	 * @throws Exception
	 */
	public List<IRow> loadRowsByParentId(int id, boolean isLock)
			throws Exception;

}
