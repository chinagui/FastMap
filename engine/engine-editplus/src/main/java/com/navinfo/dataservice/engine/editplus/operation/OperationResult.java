package com.navinfo.dataservice.engine.editplus.operation;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.engine.editplus.model.obj.BasicObj;


/**
 * 
 * 基本操作结果类，一次操作的变更结果，用于各个操作间传递对象使用
 * 删除的数据不允许流入下一个环节，所以result中不会有删除的对象
 * 新增和删除列表中的对象实际上是指的这次操作的状态
 */
public class OperationResult{
	
	/**
	 * 新增对象列表:key:objType,value:key:objPid,value:obj
	 */
	private Map<String,Map<Long,BasicObj>> addObjs = new HashMap<String,Map<Long,BasicObj>>();

	/**
	 * 修改对象列表
	 */
	private Map<String,Map<Long,BasicObj>> updateObjs = new HashMap<String,Map<Long,BasicObj>>();

	private List<BasicObj> allObjs=new ArrayList<BasicObj>();
	
	public List<BasicObj> getAllObjs() {
		return allObjs;
	}

//	private JSONArray logs = new JSONArray();//操作业务逻辑完成后统一计算
	
	/**
	 * 新增、删除和修改的对象添加到result中
	 * @param obj
	 */
	protected void putObj(BasicObj obj)throws OperationResultException{
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
	 * key:objType,value:key:objPid,value:obj
	 */
	public Map<String,Map<Long,BasicObj>> getAddObjs() {
		return addObjs;
	}

	/**
	 * @return 修改记录列表
	 * key:objType,value:key:objPid,value:obj
	 */
	public Map<String,Map<Long,BasicObj>> getUpdateObjs() {
		return updateObjs;
	}

	protected void clear() {
		this.addObjs.clear();
		this.updateObjs.clear();
	}
	/**
	 * 
	 * @param result
	 */
	protected void putAll(List<BasicObj> objs) {
		//key:objType,value:key:objPid,value:obj
		if(objs!=null&&objs.size()>0){
			//
		}
	}
	
	/**
	 * 持久化一次操作的变更，持久化包括数据和履历
	 * 把本次操作有变更的对象写入result中，不需要加入delete状态的对象
	 * 把变更的对象的变更信息写hisChangeLogs中，并设置所有对象当前操作状态为update
	 * @param conn
	 * @param objs
	 * @throws Exception
	 */
	public void persistChangeLog(Connection conn,List<BasicObj> objs)throws Exception{
		if(objs==null||objs.size()==0)return;
		//持久化一次操作的变更，持久化包括数据和履历
		for(BasicObj obj:objs){
			obj.generateSql();
		}
		//...
		//把本次操作有变更的对象写入result中，不需要加入delete状态的对象
		//...
		//把result中的对象的变更信息写hisChangeLogs中
		//...
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
