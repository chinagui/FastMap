package com.navinfo.dataservice.engine.editplus.model.obj;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.engine.editplus.diff.ObjectDiffConfig;
import com.navinfo.dataservice.engine.editplus.glm.GlmTableNotFoundException;
import com.navinfo.dataservice.engine.editplus.model.BasicRow;
import com.navinfo.dataservice.engine.editplus.model.selector.ObjSelector;
import com.navinfo.dataservice.engine.editplus.operation.OperationType;
import com.navinfo.navicommons.database.sql.RunnableSQL;


/** 
 * @ClassName: BasicObj:有主键PID即为一个对象
 * @author xiaoxiaowen4127
 * @date 2016年8月17日
 * @Description: BasicObj.java
 */
public abstract class BasicObj {
	protected int lifeCycle=0;
	protected BasicRow mainrow;
	//protected Map<Class<? extends BasicObj>, List<BasicObj>> childobjs;//存储对象下面的子对象，不包含子表
//	protected Map<Class<? extends BasicRow>, List<BasicRow>> childrows;//存储对象下的子表,包括二级、三级子表...
	protected Map<String,List<BasicRow>> subrows=new HashMap<String,List<BasicRow>>();//key:table_name,value:rows
	
	public BasicObj(BasicRow mainrow){
		this.mainrow=mainrow;
	}
	public int getLifeCycle() {
		return lifeCycle;
	}
	public void setLifeCycle(int lifeCycle) {
		this.lifeCycle = lifeCycle;
	}
	public BasicRow getMainrow() {
		return mainrow;
	}
	public Map<String, List<BasicRow>> getSubrows() {
		return subrows;
	}
	public void insertSubrows(String tableName,List<BasicRow> basicRowList) {
		subrows.put(tableName, basicRowList);
	}
	public void insertSubrow(BasicRow subrow){
		subrow.setOpType(OperationType.INSERT);
		//todo
	}
	
	public abstract String objType();
	
	public long objPid() {
		return mainrow.getObjPid();
	}
	public OperationType opType(){
		return mainrow.getOpType();
	}
	/**
	 * 设置所有表的操作状态
	 * @param opType
	 */
	public void setOpType(OperationType opType){
		this.mainrow.setOpType(opType);
		for(List<BasicRow> rows:subrows.values()){
			if(rows!=null){
				for(BasicRow row:rows){
					row.setOpType(opType);
				}
			}
		}
	}
	
	/**
	 * 主表对应的子表list。key：Class.class value:模型中子表的list
	 * @return
	 * @throws SQLException 
	 * @throws GlmTableNotFoundException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InvocationTargetException 
	 * @throws NoSuchMethodException 
	 */
//	public abstract Map<Class<? extends BasicRow>,List<BasicRow>> childRows(); 


	//public abstract Map<Class<? extends BasicObj>,List<BasicObj>> childObjs(); 
	

//	public boolean checkChildren(List<?> oldValue,List<?> newValue){
//		if(oldValue==null&&newValue==null)return false;
//		if(oldValue!=null&&oldValue.equals(newValue))return false;
//		//...TODO
//		return true;
//	}
	
	public List<BasicRow> selectRowsByName(Connection conn,String tableName) throws GlmTableNotFoundException, SQLException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, IllegalArgumentException{
		List<BasicRow> rows = subrows.get(tableName);
		if(rows==null){
			//ObjSelector
			ObjSelector.selectChildren(conn,this,tableName);
		}
		return subrows.get(tableName);
	}

	public List<BasicRow> getRowsByName(String tableName){
		List<BasicRow> rows = subrows.get(tableName);
		return rows;
	}
	/**
	 * 判断如果是修改状态下，加载所有子表并全部设置为删除
	 */
	public void deleteObj(){
		if(mainrow.getOpType().equals(OperationType.UPDATE)){
			//加载所有子表并设置删除
		}
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
	public void diff(BasicObj obj,ObjectDiffConfig diffConfig)throws Exception{
		//todo
//		boolean isDefer=false;
		//根据差分配置
		if(this.getClass().getName().equals(obj.getClass().getName())){
			this.mainrow.setAttrByCol("col1", obj.mainrow.getAttrByColName("col1"));
		}
//		return isDefer;
	}
	

}
