package com.navinfo.dataservice.engine.edit.edit.operation;

import com.navinfo.dataservice.engine.edit.edit.model.Result;

/**
 * 操作基类
 */
public interface IOperation {

	/**
	 * 执行操作
	 * 
	 * @param result
	 *            操作结果
	 * @return 操作后的对象
	 * @throws Exception
	 */
	public String run(Result result) throws Exception;
}
