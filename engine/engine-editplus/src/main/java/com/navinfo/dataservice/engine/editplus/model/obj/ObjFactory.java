package com.navinfo.dataservice.engine.editplus.model.obj;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.plus.glm.GlmFactory;
import com.navinfo.dataservice.dao.plus.glm.GlmObject;
import com.navinfo.dataservice.dao.plus.glm.GlmTable;
import com.navinfo.dataservice.engine.editplus.model.BasicRow;
import com.navinfo.dataservice.engine.editplus.operation.OperationType;

/** 
 * @ClassName: ObjFactory
 * @author xiaoxiaowen4127
 * @date 2016年9月13日
 * @Description: ObjFactory.java
 */
public class ObjFactory {
	protected Logger log = Logger.getLogger(this.getClass());
	private volatile static ObjFactory instance=null;
	public static ObjFactory getInstance(){
		if(instance==null){
			synchronized(ObjFactory.class){
				if(instance==null){
					instance=new ObjFactory();
				}
			}
		}
		return instance;
	}
	private ObjFactory(){}
	
	/**
	 * 
	 * @param clazz:主表的模型类
	 * @param isSetPid：是否申请pid
	 * @return
	 */
	public <T> T create(Class<T> clazz,boolean isSetPid){
		return null;
	}
	/**
	 * 根据对象类型创建一个新的对象，所有属性子表会初始化，但list.size()==0
	 * @param objType:对象类型
	 * @param isSetPid：
	 * @return
	 */
	public BasicObj create(String objType)throws Exception{
		GlmObject glmObj = GlmFactory.getInstance().getObjByType(objType);
		GlmTable mainTab = glmObj.getMainTable();
		long pid = PidUtil.getInstance().applyPidByTableName(mainTab.getName());
		BasicRow mainrow = (BasicRow)Class.forName(mainTab.getModelClassName()).getConstructor(long.class).newInstance(pid);
		mainrow.setRowId(UuidUtils.genUuid());
		String pkCol = mainTab.getPkColumn();
		mainrow.setAttrByCol(pkCol, pid);
		mainrow.setOpType(OperationType.INSERT);
		return (BasicObj)Class.forName(glmObj.getModelClassName()).getConstructor(BasicRow.class).newInstance(mainrow);
	}
	
	/**
	 * 
	 * @param row
	 * @return
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public BasicObj create4Select(BasicRow row)throws ClassNotFoundException,NoSuchMethodException,InvocationTargetException,IllegalAccessException,InstantiationException{
		GlmObject glmObj =
		GlmFactory.getInstance().getObjByType(row.getObjType());
		BasicObj bObj = (BasicObj)Class.forName(glmObj.getModelClassName()).getConstructor(BasicRow.class).newInstance(row);
		return bObj;
	}
	/**
	 * 新建一个表记录,只用于子表创建
	 * @param tableName：表名
	 * @param objPid：表所属对象的对象pid
	 * @return
	 * @throws Exception
	 */
	public BasicRow createRow(String tableName,long objPid)throws Exception{
		GlmTable glmTab=GlmFactory.getInstance().getTableByName(tableName);
		BasicRow row = (BasicRow)Class.forName(glmTab.getModelClassName()).getConstructor(long.class).newInstance(objPid);
		row.setRowId(UuidUtils.genUuid());
		row.setOpType(OperationType.INSERT);
		//申请pid
		String pkCol = glmTab.getPkColumn();
		if(StringUtils.isNotEmpty(pkCol)){
			row.setAttrByCol(pkCol, PidUtil.getInstance().applyPidByTableName(tableName));
		}
		return row;
	}
}
