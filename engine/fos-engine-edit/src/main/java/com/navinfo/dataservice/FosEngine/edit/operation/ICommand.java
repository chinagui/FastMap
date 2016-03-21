package com.navinfo.dataservice.FosEngine.edit.operation;

import com.navinfo.dataservice.FosEngine.edit.model.ObjType;

/**
 * 保存操作的相关参数
 */
public interface ICommand {

	/**
	 * @return 操作类型
	 */
	public OperType getOperType();

	/**
	 * @return 请求参数
	 */
	public String getRequester();
	
	
	/**
	 * @return 操作对象类型
	 */
	public ObjType getObjType();
}
