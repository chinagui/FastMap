package com.navinfo.dataservice.engine.editplus.operation.edit;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.plus.operation.AbstractCommand;

import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

/** 
 * @ClassName: AbstractCommand
 * @author xiaoxiaowen4127
 * @date 2016年11月27日
 * @Description: AbstractCommand.java
 */
public abstract class EditCommand extends AbstractCommand{

	protected int dbId=0;
	
	protected OperType opType;
	
	protected ObjType objType;

	public int getDbId() {
		return dbId;
	}

	public void setDbId(int dbId) {
		this.dbId = dbId;
	}

	public OperType getOpType() {
		return opType;
	}

	public void setOpType(OperType opType) {
		this.opType = opType;
	}

	public ObjType getObjType() {
		return objType;
	}

	public void setObjType(ObjType objType) {
		this.objType = objType;
	}
	
	public abstract void validate() throws Exception;
	
	public void parse(JSONObject data) throws CommandCreateException {
		if (data == null) {
			log.warn("注意：未传入的解析json对象，request未被初始化");
		} else {
			for (Iterator it = data.keys(); it.hasNext();) {
				String attName = (String) it.next();
				Object attValue = data.get(attName);
				if (attValue == null
						|| StringUtils.isEmpty(attName)
						|| (attValue instanceof String && StringUtils
								.isEmpty((String) attValue))) {
					log.warn("注意：request的json中存在name或者value为空的属性，已经被忽略。");
					continue;
				}
				setAttrValue(attName, attValue);
			}
		}
	}

	private void setAttrValue(String attName, Object attValue)
			throws CommandCreateException {
		if (StringUtils.isEmpty(attName) || attValue == null
				|| (attValue instanceof JSONNull)) {
			log.warn("注意：request的json中存在name或者value为空的属性，已经被忽略。");
			return;
		}
		try {
			String methodName = "set" + (char) (attName.charAt(0) - 32)
					+ attName.substring(1, attName.length());
			Class[] argtypes = null;// 默认String

			if (attValue instanceof String) {
				argtypes = new Class[] { String.class };
			} else if (attValue instanceof Integer) {
				argtypes = new Class[] { int.class };
			} else if (attValue instanceof Double) {
				argtypes = new Class[] { double.class };
			} else if (attValue instanceof Boolean) {
				argtypes = new Class[] { boolean.class };
			} else if (attValue instanceof JSONArray) {
				JSONArray attArr = (JSONArray) attValue;
				if (attArr.size() > 0) {
					Object subObj = attArr.get(0);
					if (subObj instanceof String || subObj instanceof Integer
							|| subObj instanceof Double
							|| subObj instanceof Boolean) {
						argtypes = new Class[] { List.class };
					} else if (subObj instanceof JSONObject) {
						argtypes = new Class[] { Map.class };
						Map newAttValue = new HashMap();
						for (Object o : attArr) {
							JSONObject jo = (JSONObject) o;
							Object key = jo.get("key");
							Object value = jo.get("value");
							if (key != null && value != null) {
								newAttValue.put(key, value);
							}
						}
						attValue = newAttValue;
					} else {
						throw new Exception(attName + "为数组类型，其内部格式为不支持的json结构");
					}
				} else {
					return;
				}

			} else if (attValue instanceof JSONObject) {

				// TODO

			}
			Method method = this.getClass().getMethod(methodName, argtypes);
			method.invoke(this, attValue);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new CommandCreateException("Command解析过程中可能未找到方法,原因为:"
					+ e.getMessage(), e);
		}
	}
}
