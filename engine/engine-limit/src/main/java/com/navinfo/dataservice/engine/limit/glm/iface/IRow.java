package com.navinfo.dataservice.engine.limit.glm.iface;

import net.sf.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * 模型的基类
 */
public interface IRow extends ISerializable {

	/**
	 * @return 数据库表名
	 */
	public String tableName();

	/**
	 * @return 增删改状态
	 */
	public ObjStatus status();

	/**
	 * 更新状态
	 * 
	 * @param os
	 */
	public void setStatus(ObjStatus os);

	/**
	 * @return 要素类型
	 */
	public ObjType objType();

	/**
	 * @return 变化的字段
	 */
	public Map<String, Object> changedFields();

	/**
	 * @return 所属主表的主键字段名
	 */
	public String parentPKName();

	/**
	 * @return 所属主表的主键字段值
	 */
	public int parentPKValue();

	/**
	 * @return 所属主表的表名
	 */
	public String parentTableName();

	/**
	 * @return 所有子表的集合
	 */
	public List<List<IRow>> children();
	
	/**
	 * 填充修改过的属性
	 * @param json
	 */
	public boolean fillChangeFields(JSONObject json) throws Exception;
}
