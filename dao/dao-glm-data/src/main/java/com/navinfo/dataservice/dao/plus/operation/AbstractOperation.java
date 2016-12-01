package com.navinfo.dataservice.dao.plus.operation;

import java.sql.Connection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.plus.editman.PoiEditStatus;
import com.navinfo.dataservice.dao.plus.log.LogGenerator;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.navicommons.database.sql.RunnableSQL;

/** 
 * @ClassName: Operation
 * @author xiaoxiaowen4127
 * @date 2016年11月14日
 * @Description: Operation.java
 */
public abstract class AbstractOperation {
	protected Logger log = Logger.getLogger(this.getClass());
	protected OperationResult result;
	protected Connection conn;
	protected boolean oneOperation=false;
	
	public AbstractOperation(Connection conn,OperationResult preResult){
		this.conn=conn;
		if(preResult==null){
			result=new OperationResult();
		}else{
			this.result=preResult;
		}
	}
	public abstract String getName();
	
	public OperationResult getResult() {
		return result;
	}
	public abstract void operate(AbstractCommand cmd)throws Exception;

	/**
	 * 持久化一次操作的变更，持久化包括数据和履历
	 * 把本次操作有变更的对象写入result中，不需要加入delete状态的对象
	 * 把变更的对象的变更信息写hisChangeLogs中，并设置所有对象当前操作状态为update
	 * SQL执行再优化
	 * @param conn
	 * @param objs
	 * @throws Exception
	 */
	public void persistChangeLog(int opSg,long userId)throws Exception{
		if(result==null||result.getAllObjs().size()==0)return;
		//持久化一次操作的变更，持久化包括数据和履历
		//新增IX_POI对象向poi_edit_status表中插入记录
		PoiEditStatus.insertPoiEditStatus(conn,result);
		//持久化履历
		LogGenerator.writeLog(conn,oneOperation, result.getAllObjs(),getName(), opSg, userId);
		//持久化数据
		for(Iterator<BasicObj> it=result.getAllObjs().iterator(); it.hasNext();){
			BasicObj obj = it.next();
			List<RunnableSQL> sqls = obj.generateSql();
			if(sqls!=null){
				for(RunnableSQL sql:sqls){
					sql.run(conn);
				}
			}
			//持久化把新增后删除的对象移出objs
			if(obj.getMainrow().getOpType().equals(OperationType.INSERT_DELETE)){
				it.remove();
			}else{//如果不为删除，则将修改加入历史变更，给下一操作阶段做参考
				obj.afterPersist();
			}
		}
	}
}
