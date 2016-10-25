package com.navinfo.dataservice.dao.glm.iface;

import java.util.Collection;
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
	 * @param loadChild 
	 * 			是否加载子表 （不传：加载子表；传true，不加载子表;传false,加载子表
	 * @return IRow
	 * @throws Exception
	 */
	public IRow loadById(int id, boolean isLock,boolean ... noChild) throws Exception;
	
	/**
	 * 查询主表和对应的Class的子表
	 * @param id
	 * @param isLock
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public IRow loadByIdAndChildClass(int id,boolean isLock,Class<? extends IRow> ... childClass) throws Exception;
	
	/**
	 * 查询多个pid的数据
	 * @param idList pid集合
	 * @param isLock 是否加锁
	 * @param childClass 加载的子表class
	 * @return
	 * @throws Exception
	 */
	public List<IRow> loadByIds(List<Integer> idList, boolean isLock,boolean loadChild) throws Exception;

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
	public List<IRow> loadRowsByParentId(int id, boolean isLock,boolean ... delFlag)
			throws Exception;
	
	
	/**
	 * 根据主键id获取一行记录,不过滤删除的记录
	 * 
	 * @param id
	 *            主键id
	 * @param isLock
	 *            是否加锁
	 * @param loadChild 
	 * 			是否加载子表 （不传：加载子表；传true，不加载子表;传false,加载子表
	 * @return IRow
	 * @throws Exception
	 */
	public IRow loadAllById(int id, boolean isLock,boolean ... noChild) throws Exception;


	



}
