package com.navinfo.dataservice.engine.edit.bo;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.engine.edit.model.BasicObj;

/**
 * 只有Obj级别才需要定义Bo
 * Bo需要实现对应Po中涉及模型字段的所有get，set方法，方便后面调用，也方便增加一些业务逻辑
 * AbstractBo会重写BasicRow对应的字段
 * 每一个具体的Bo要实现对应BasicRow的具体类中的模型字段的get，set方法
 * @ClassName: AbstractBo
 * @author xiaoxiaowen4127
 * @date 2016年8月17日
 * @Description: AbstractBo.java
 */
public abstract class AbstractBo {
	protected Logger log = Logger.getLogger(this.getClass());
	
	public String getRowId(){
		return getObj().getRowId();
	}
	public void setRowId(String rowId){
		//rowId不会作为修改操作的修改字段，不需要记录oldValues
		getObj().setRowId(rowId);
	}
	
	public long getPid(){
		return getObj().getPid();
	}
	public void setPid(long pid){
		//PID不会作为修改操作的修改字段，不需要记录oldValues
		getObj().setPid(pid);
	}
    public abstract AbstractBo copy()throws Exception;
	public abstract void setObj(BasicObj obj);
	public abstract BasicObj getObj();
}
