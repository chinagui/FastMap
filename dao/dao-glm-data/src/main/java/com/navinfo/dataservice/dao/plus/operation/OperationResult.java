package com.navinfo.dataservice.dao.plus.operation;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.plus.log.LogGenerator;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.navicommons.database.sql.RunnableSQL;


/**
 * 
 * 基本操作结果类，一次操作的变更结果，用于各个操作间传递对象使用
 * 删除的数据不允许流入下一个环节，所以result中不会有删除的对象
 * 新增和删除列表中的对象实际上是指的这次操作的状态
 */
public class OperationResult{
	
	//key:对象d
//	private List<BasicObj> allObjs=new ArrayList<BasicObj>();
	private Map<String,Map<Long,BasicObj>> allObjs= new HashMap<String,Map<Long,BasicObj>>();
	
	public boolean isObjExist(BasicObj obj){
		Map<Long,BasicObj> objs = getObjsMapByType(obj.objName());
		if(objs!=null&&objs.containsKey(obj.objPid())){
			return true;
		}
		return false;
	}
	
	public boolean isObjExist(String objectType,long pid){
		Map<Long,BasicObj> objs = getObjsMapByType(objectType);
		if(objs!=null&&objs.containsKey(pid)){
			return true;
		}
		return false;
	}
	public Map<String,Map<Long,BasicObj>> getAllObjsMap(){
		return allObjs;
	}
	
	public List<BasicObj> getAllObjs() {
		List<BasicObj> objList = new ArrayList<BasicObj>();
		for(Map.Entry<String, Map<Long,BasicObj>> entryByObjType:allObjs.entrySet()){
			for(Map.Entry<Long, BasicObj> entry:entryByObjType.getValue().entrySet()){
				objList.add(entry.getValue());
			}
		}
		return objList;
	}
	public Map<Long,BasicObj> getObjsMapByType(String objType){
		return allObjs.get(objType);
	}

//	private JSONArray logs = new JSONArray();//操作业务逻辑完成后统一计算
	
	/**
	 * 新增、删除和修改的对象添加到result中
	 * @param obj
	 */
	public void putObj(BasicObj obj)throws OperationResultException{
		if(obj.opType().equals(OperationType.INITIALIZE)){
			throw new OperationResultException("未设置操作类型的对象");
		}
		if(allObjs.get(obj.objName())==null){
			allObjs.put(obj.objName(), new HashMap<Long,BasicObj>());
		}
		allObjs.get(obj.objName()).put(obj.objPid(), obj);
	}

	public void clear() {
		this.allObjs.clear();
	}
	/**
	 * 
	 * @param result
	 */
	public void putAll(List<? extends BasicObj> objs) throws OperationResultException{
		//key:objType,value:key:objPid,value:obj
		for(BasicObj basicObj:objs){
			putObj(basicObj);
		}
	}
	
	//合并两个map,如果有相同，后者覆盖前者
	public void putAll(Map<String,Map<Long,BasicObj>> objs) {
		//key:objType,value:key:objPid,value:obj
		if(objs!=null&&objs.size()>0){
			//
			allObjs.putAll(objs);
		}
	}
	
	/**
	 * 分析履历得到变更使用，暂未想清楚
	 * @param conn
	 * @param tempLogTable
	 * @throws Exception
	 */
	public void parseChangeLog(Connection conn,String tempLogTable)throws Exception{
		
	}
}
