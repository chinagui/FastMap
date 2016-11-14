package com.navinfo.dataservice.engine.editplus.diff;


/** 
 * @ClassName: Diffable
 * @author xiaoxiaowen4127
 * @date 2016年11月14日
 * @Description: Diffable.java
 */
public interface Diffable {
	/**
	 * 根据传入的diffConfig差分更新对象属性
	 * 主表不差分pid，所有表不差分rowid
	 * @param obj：参考的对象
	 * @return：是否有更新
	 * @throws Exception
	 */
	public boolean diff(Diffable obj,ObjectDiffConfig diffConfig)throws Exception;
}
