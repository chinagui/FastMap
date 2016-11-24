package com.navinfo.dataservice.engine.editplus.operation;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.engine.editplus.log.LogGenerator;
import com.navinfo.dataservice.engine.editplus.model.obj.BasicObj;
import com.navinfo.navicommons.database.sql.RunnableSQL;


/**
 * 
 * 基本操作结果类，一次操作的变更结果，用于各个操作间传递对象使用
 * 删除的数据不允许流入下一个环节，所以result中不会有删除的对象
 * 新增和删除列表中的对象实际上是指的这次操作的状态
 */
public class OperationResult{
	
	private List<BasicObj> allObjs=new ArrayList<BasicObj>();
	
	public List<BasicObj> getAllObjs() {
		return allObjs;
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
		allObjs.add(obj);
	}

	public void clear() {
		this.allObjs.clear();
	}
	/**
	 * 
	 * @param result
	 */
	public void putAll(List<BasicObj> objs) {
		//key:objType,value:key:objPid,value:obj
		if(objs!=null&&objs.size()>0){
			//
			allObjs.addAll(objs);
		}
	}
	
	/**
	 * 持久化一次操作的变更，持久化包括数据和履历
	 * 把本次操作有变更的对象写入result中，不需要加入delete状态的对象
	 * 把变更的对象的变更信息写hisChangeLogs中，并设置所有对象当前操作状态为update
	 * SQL执行再优化
	 * @param conn
	 * @param objs
	 * @throws Exception
	 */
	public void persistChangeLog(Connection conn,String opCmd,int opSg,long userId)throws Exception{
		if(allObjs.size()==0)return;
		//持久化一次操作的变更，持久化包括数据和履历
		//持久化履历
		LogGenerator.writeLog(conn, allObjs, opCmd, opSg, userId);
		//持久化数据
		for(Iterator<BasicObj> it=allObjs.iterator(); it.hasNext();){
			BasicObj obj = it.next();
			List<RunnableSQL> sqls = obj.generateSql();
			if(sqls!=null){
				for(RunnableSQL sql:sqls){
					sql.run(conn);
				}
			}
			//持久化把删除的数据移出objs
			if(obj.getMainrow().getOpType().equals(OperationType.DELETE)
					||obj.getMainrow().getOpType().equals(OperationType.INSERT_DELETE)){
				it.remove();
			}else{//如果不为删除，则将修改加入历史变更，给下一操作阶段做参考
				obj.afterPersist();
			}
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
