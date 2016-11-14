package com.navinfo.dataservice.engine.editplus.model.obj;

import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.engine.editplus.diff.Diffable;
import com.navinfo.dataservice.engine.editplus.diff.ObjectDiffConfig;
import com.navinfo.dataservice.engine.editplus.model.BasicRow;
import com.navinfo.dataservice.engine.editplus.operation.OperationType;
import com.navinfo.navicommons.database.sql.RunnableSQL;


/** 
 * @ClassName: BasicObj:有主键PID即为一个对象
 * @author xiaoxiaowen4127
 * @date 2016年8月17日
 * @Description: BasicObj.java
 */
public abstract class BasicObj implements Diffable {
	
	protected BasicRow mainrow;
	//protected Map<Class<? extends BasicObj>, List<BasicObj>> childobjs;//存储对象下面的子对象，不包含子表
	protected Map<Class<? extends BasicRow>, List<BasicRow>> childrows;//存储对象下的子表,包括二级、三级子表...
	
	public BasicObj(BasicRow mainrow){
		this.mainrow=mainrow;
	}
	public abstract String objType();
	
	public long objPid() {
		return mainrow.getObjPid();
	}
	public OperationType opType(){
		return mainrow.getOpType();
	}
	/**
	 * 主表对应的子表list。key：Class.class value:模型中子表的list
	 * @return
	 */
	public abstract Map<Class<? extends BasicRow>,List<BasicRow>> childRows(); 


	//public abstract Map<Class<? extends BasicObj>,List<BasicObj>> childObjs(); 
	

	public boolean checkChildren(List<?> oldValue,List<?> newValue){
		if(oldValue==null&&newValue==null)return false;
		if(oldValue!=null&&oldValue.equals(newValue))return false;
		//...TODO
		return true;
	}
	
	public BasicObj copy(){
		return null;
	}
	
	public String identity(){
		return objType()+objPid();
	}
	@Override
	public int hashCode(){
		return identity().hashCode();
	}
	/**
	 * 如果pid<=0,不比较
	 */
	@Override
	public boolean equals(Object anObject){
		if(anObject==null)return false;
		if(anObject instanceof BasicObj
				&&objPid()>0&&identity().equals(((BasicObj) anObject).identity())){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * 生成这个对象写入库中的sql
	 * @return
	 */
	public List<RunnableSQL> generateSql(){
		//todo
		return null;
	}
	
	/**
	 * 根据传入的diffConfig差分更新对象属性
	 * 主表不差分pid，所有表不差分rowid
	 * @param obj：参考的对象
	 * @return：是否有更新
	 * @throws Exception
	 */
	public boolean diff(Diffable obj,ObjectDiffConfig diffConfig)throws Exception{
		//todo
		return false;
	}
	

}
