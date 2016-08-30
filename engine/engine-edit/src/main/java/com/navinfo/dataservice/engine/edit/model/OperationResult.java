package com.navinfo.dataservice.engine.edit.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.glm.iface.ISerializable;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 
 * 基本操作结果类，各个操作结果有特殊业务可以继承此类
 */
public class OperationResult implements ISerializable {

	private long primaryPid;//一次操作的主要对象，前台需要用于操作结束后显示属性栏的，一次操作涉及多个Obj时，没有给定具体的原则设置为哪个Obj的pid

	public long getPrimaryPid() {
		return primaryPid;
	}

	public void setPrimaryPid(long primaryPid) {
		this.primaryPid = primaryPid;
	}
	
	/**
	 * 新增对象列表
	 */
	private List<BasicObj> addObjs = new ArrayList<BasicObj>();

	/**
	 * 删除对象集合
	 */
	private List<BasicObj> delObjs = new ArrayList<BasicObj>();

	/**
	 * 修改对象列表
	 */
	private List<BasicObj> updateObjs = new ArrayList<BasicObj>();

	private JSONArray logs = new JSONArray();//操作业务逻辑完成后统一计算
	
	/**
	 * 新增、删除和修改的对象添加到result中
	 * @param obj
	 */
	public void putObj(BasicObj obj)throws OperationResultException{
		if(obj.getOpType().equals(OperationType.INITIALIZE)){
			throw new OperationResultException("未设置操作类型的对象");
		}
		if(obj.getOpType().equals(OperationType.INSERT)){
			addObjs.add(obj);
		}else if(obj.getOpType().equals(OperationType.DELETE)){
			delObjs.add(obj);
		}else if(obj.getOpType().equals(OperationType.UPDATE)){
			updateObjs.add(obj);
//			if(obj.getOldValues()!=null){//主表也有修改
//				
//			}
//			//遍历子表
		}
	}

	/**
	 * @return 新增记录列表
	 */
	public List<BasicObj> getAddObjs() {
		return addObjs;
	}

	/**
	 * @return 删除记录列表
	 */
	public List<BasicObj> getDelObjs() {
		return delObjs;
	}

	/**
	 * @return 修改记录列表
	 */
	public List<BasicObj> getUpdateObjs() {
		return updateObjs;
	}

	@Override
	public boolean Unserialize(JSONObject json) throws Exception {

		return false;
	}

	public void clear() {
		this.addObjs.clear();
		this.delObjs.clear();
		this.updateObjs.clear();
	}
	
	public void putAll(OperationResult result) {
		this.addObjs.addAll(result.getAddObjs());
		this.updateObjs.addAll(result.getUpdateObjs());
		this.delObjs.addAll(result.getDelObjs());
	}

	@Override
	public JSONObject Serialize(ObjLevel objLevel) throws Exception {
		return null;
	}
}
