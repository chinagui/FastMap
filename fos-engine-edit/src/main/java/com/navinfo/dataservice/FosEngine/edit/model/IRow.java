package com.navinfo.dataservice.FosEngine.edit.model;

import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

/**
 * 模型的基类
 */
public interface IRow extends ISerializable {

	/**
	 * @return rowId
	 */
	public String rowId();

	/**
	 * 更新rowId
	 * 
	 * @param rowId
	 */
	public void setRowId(String rowId);

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
	 * 复制参数对象的信息到自身
	 * 
	 * @param row
	 *            被复制的对象
	 */
	public void copy(IRow row);

	/**
	 * @return 变化的字段
	 */
	public Map<String, Object> changedFields();

	/**
	 * @return 所属主表的主键字段名
	 */
	public String primaryKey();

	/**
	 * @return 所属主表的主键字段值
	 */
	public int primaryValue();

	/**
	 * @return 所属主表的表名
	 */
	public String primaryTableName();

	/**
	 * @return 所有子表的集合
	 */
	public List<List<IRow>> children();
	
	/**
	 * 填充修改过的属性
	 * @param json
	 */
	public boolean fillChangeFields(JSONObject json) throws Exception;
	
	public int mesh();
	
	public void setMesh(int mesh);
}
