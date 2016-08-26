package com.navinfo.dataservice.engine.edit.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 
 * @ClassName: BasicObj:有主键PID即为一个对象
 * @author xiaoxiaowen4127
 * @date 2016年8月17日
 * @Description: BasicObj.java
 */
public abstract class BasicObj extends BasicRow {
	protected long pid;
	public long getPid() {
		return pid;
	}

	public void setPid(long pid) {
		this.pid = pid;
	}

	public abstract String primaryKey();
	

	/**
	 * 主表对应的子表list。key：Class.class value:模型中子表的list
	 * @return
	 */
	public abstract Map<Class<? extends BasicRow>,List<BasicRow>> childRows(); 

	/**
	 * 只返回对象下二级对象列表
	 * @return
	 */
	public abstract Map<Class<? extends BasicObj>,List<BasicObj>> childObjs(); 
	

	public boolean checkChildren(List<?> oldValue,List<?> newValue){
		if(oldValue==null&&newValue==null)return false;
		if(oldValue!=null&&oldValue.equals(newValue))return false;
		//...TODO
		return true;
	}
	
	public BasicObj copyObj(long newPid){
		return null;
	}
	@Override
	public String identity(){
		return tableName()+pid;
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
		if(anObject instanceof BasicRow
				&&pid>0&&identity().equals(((BasicRow) anObject).identity())){
			return true;
		}else{
			return false;
		}
	}

}
