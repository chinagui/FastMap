package com.navinfo.dataservice.engine.editplus.operation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.glm.iface.ISerializable;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.engine.editplus.log.Logable;
import com.navinfo.dataservice.engine.editplus.model.obj.BasicObj;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 
 * 基本操作结果类，各个操作结果有特殊业务可以继承此类
 */
public class OperationResult{
	
	/**
	 * 新增对象列表:key:objType,value:key:objPid,value:obj
	 */
	private Map<String,Map<Long,BasicObj>> addObjs = new HashMap<String,Map<Long,BasicObj>>();

	/**
	 * 删除对象集合
	 */
	private Map<String,Map<Long,BasicObj>> delObjs = new HashMap<String,Map<Long,BasicObj>>();

	/**
	 * 修改对象列表
	 */
	private Map<String,Map<Long,BasicObj>> updateObjs = new HashMap<String,Map<Long,BasicObj>>();

	private List<BasicObj> allObjs=new ArrayList<BasicObj>();
	
	private JSONArray logs = new JSONArray();//操作业务逻辑完成后统一计算
	
	/**
	 * 新增、删除和修改的对象添加到result中
	 * @param obj
	 */
	public void putObj(BasicObj obj)throws OperationResultException{
		if(obj.opType().equals(OperationType.INITIALIZE)){
			throw new OperationResultException("未设置操作类型的对象");
		}
		allObjs.add(obj);
		//分类，方便后续使用
		Map<Long,BasicObj> objs=null;
		String obType = obj.objType();
		if(obj.opType().equals(OperationType.INSERT)){
			if(addObjs.containsKey(obType)){
				addObjs.get(obType).put(obj.objPid(), obj);
			}else{
				objs = new HashMap<Long,BasicObj>();
				objs.put(obj.objPid(), obj);
				addObjs.put(obType, objs);
			}
		}else if(obj.opType().equals(OperationType.DELETE)){
			if(delObjs.containsKey(obType)){
				delObjs.get(obType).put(obj.objPid(), obj);
			}else{
				objs = new HashMap<Long,BasicObj>();
				objs.put(obj.objPid(), obj);
				delObjs.put(obType, objs);
			}
		}else if(obj.opType().equals(OperationType.UPDATE)){
			if(updateObjs.containsKey(obType)){
				updateObjs.get(obType).put(obj.objPid(), obj);
			}else{
				objs = new HashMap<Long,BasicObj>();
				objs.put(obj.objPid(), obj);
				updateObjs.put(obType, objs);
			}
		}
	}

	/**
	 * @return 新增记录列表
	 */
	public Map<String,Map<Long,BasicObj>> getAddObjs() {
		return addObjs;
	}

	/**
	 * @return 删除记录列表
	 */
	public Map<String,Map<Long,BasicObj>> getDelObjs() {
		return delObjs;
	}

	/**
	 * @return 修改记录列表
	 */
	public Map<String,Map<Long,BasicObj>> getUpdateObjs() {
		return updateObjs;
	}

	public void clear() {
		this.addObjs.clear();
		this.delObjs.clear();
		this.updateObjs.clear();
	}
	/**
	 * 用于两次操作结果集合并
	 * @param result
	 */
	public void putAll(OperationResult result) {
		//key:objType,value:key:objPid,value:obj
		for(Map.Entry<String, Map<Long,BasicObj>> entry:result.getAddObjs().entrySet()){
			if(addObjs.containsKey(entry.getKey())){
				addObjs.get(entry.getKey()).putAll(entry.getValue());
			}else{
				addObjs.put(entry.getKey(),entry.getValue());
			}
		}
		for(Map.Entry<String, Map<Long,BasicObj>> entry:result.getUpdateObjs().entrySet()){
			if(updateObjs.containsKey(entry.getKey())){
				updateObjs.get(entry.getKey()).putAll(entry.getValue());
			}else{
				updateObjs.put(entry.getKey(),entry.getValue());
			}
		}
		for(Map.Entry<String, Map<Long,BasicObj>> entry:result.getDelObjs().entrySet()){
			if(delObjs.containsKey(entry.getKey())){
				delObjs.get(entry.getKey()).putAll(entry.getValue());
			}else{
				delObjs.put(entry.getKey(),entry.getValue());
			}
		}
	}
	
	public Collection<Logable> allRows4Log(){
		
	}
}
