package com.navinfo.dataservice.engine.limit.glm.iface;


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
