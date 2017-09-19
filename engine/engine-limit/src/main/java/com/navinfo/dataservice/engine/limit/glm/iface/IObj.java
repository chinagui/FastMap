package com.navinfo.dataservice.engine.limit.glm.iface;

import java.util.List;
import java.util.Map;

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
	public String primaryKeyValue();
	
	
	public String primaryKey();
	
	/**
	 * 主表对应的子表list。key：Class.class value:模型中子表的list
	 * @return
	 */
	public Map<Class<? extends IRow>,List<IRow>> childList(); 
	
	/**
	 * 主表对应的子表list。key：Class.class value:模型中子表的Map
	 * @return
	 */
	public Map<Class<? extends IRow>,Map<String,?>> childMap(); 
}
