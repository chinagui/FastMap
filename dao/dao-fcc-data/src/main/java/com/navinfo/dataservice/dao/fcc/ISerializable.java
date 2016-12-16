package com.navinfo.dataservice.dao.fcc;

import net.sf.json.JSONObject;

/**
 * 序列化接口
 */
public interface ISerializable {

	/**
	 * 根据不同等级，将对象序列化为JSON对象
	 * 
	 * @param objLevel
	 *            FULL，SHORT，BRIEF
	 * @return JSON对象
	 * @throws Exception
	 */
	public JSONObject Serialize(ObjLevel objLevel) throws Exception;

	/**
	 * 反序列化JSON对象
	 * 
	 * @param json
	 * @return
	 */
	public boolean Unserialize(JSONObject json) throws Exception;
}
