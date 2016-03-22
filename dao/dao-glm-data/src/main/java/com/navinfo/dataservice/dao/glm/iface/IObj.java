package com.navinfo.dataservice.dao.glm.iface;

import java.util.List;

/**
 * 主表模型接口
 */
public interface IObj extends IRow {

	/**
	 * @return 关联要素的类型和pid列表
	 */
	public List<IRow> relatedRows();

	/**
	 * @return pid
	 */
	public int pid();
	
	
	public String primaryKey();
}
