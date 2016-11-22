package com.navinfo.dataservice.engine.editplus.model;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.engine.editplus.glm.GlmColumn;
import com.navinfo.dataservice.engine.editplus.glm.GlmFactory;
import com.navinfo.dataservice.engine.editplus.glm.GlmRef;
import com.navinfo.dataservice.engine.editplus.glm.GlmTable;
import com.navinfo.dataservice.engine.editplus.glm.NonGeoPidException;
import com.navinfo.dataservice.engine.editplus.log.Logable;
import com.navinfo.dataservice.engine.editplus.operation.OperationType;
import com.navinfo.navicommons.database.sql.RunnableSQL;
import com.vividsolutions.jts.geom.Geometry;

/** 
 * @ClassName: BasicRow
 * @author xiaoxiaowen4127
 * @date 2016年8月17日
 * @Description: BasicRow.java
 */
public abstract class BasicRow implements Logable{
	protected Logger log = Logger.getLogger(this.getClass());
	protected OperationType opType=OperationType.INITIALIZE;//表记录的操作状态
	protected String rowId;//row类型的代表就是有row_id
	protected long objPid;
	protected Map<String,Object> oldValues=null;//存储变化字段的旧值，key:col_name,value：旧值
	
	
	public BasicRow() {
		super();
		// TODO Auto-generated constructor stub
	}

	public BasicRow(long objPid){
		this.objPid=objPid;
	}
	
	public long getObjPid(){
		return objPid;
	}
	public void setObjPid(long objPid){
		this.objPid=objPid;
	}

	public abstract String tableName();
	/**
	 * 编辑认为的表所属对象
	 * @return
	 */
	public String getObjType(){
		return GlmFactory.getInstance().getTableByName(tableName()).getObjType();
	}

	public long getGeoPid()throws NonGeoPidException,Exception{
		GlmRef ref = GlmFactory.getInstance().getTableByName(tableName()).getGeoRef();
		if(ref.isRefMain()){
			return (long)getAttrByColName(ref.getCol());
		}else{
			throw new NonGeoPidException("表未直接参考几何对象，不能通过记录获取，请尝试通过obj方式获取。");
		}
	}
	
	public String getGeoType(){
		return GlmFactory.getInstance().getTableByName(tableName()).getGeoObjType();
	}
	
	public String primaryKey(){
		return GlmFactory.getInstance().getTableByName(tableName()).getPkColumn();
	}
	
//  //need override
//	public boolean isGeoChanged() {
//		return false;
//	}
	
	public OperationType getOpType() {
		return opType;
	}
	public void setOpType(OperationType opType) {
		this.opType = opType;
	}
	public String getRowId() {
		return rowId;
	}
	public void setRowId(String rowId) {
		this.rowId = rowId;
	}
	public Map<String, Object> getOldValues() {
		return oldValues;
	}
	public void setOldValues(Map<String, Object> oldValues) {
		this.oldValues = oldValues;
	}

	/**
	 * 行级记录复制,新Row的OperationType为insert
	 * 
	 * 新生成rowId
	 * @return
	 */
	public BasicRow copy(){
		
		return null;
	}
	/**
	 * 根据OperationType生成相应的新增、删除和修改sql
	 * @return
	 */
	protected RunnableSQL toSql(){
		RunnableSQL sql = new RunnableSQL();
		StringBuilder sb = new StringBuilder();
		String tbName = tableName();
		GlmTable tab = GlmFactory.getInstance().getTableByName(tbName);
		if(OperationType.INSERT.equals(this.opType)){
			sb.append("INSERT INTO "+tbName+"(");
			for(Map.Entry<String, GlmColumn> entry:tab.getColumns().entrySet()){
				
			}
		}
		return null;
	}
	/**
	 * colNames为空会获取全部属性值
	 * @param colNames
	 * @return
	 */
	public Map<String,Object> getAttrs(Collection<String> colNames){
		//todo
		Map<String,Object> attrs = new HashMap<String,Object>();
		return attrs;
	}
	public Object getAttrByColName(String colName)throws NoSuchMethodException,InvocationTargetException, IllegalAccessException, IllegalArgumentException{
		//todo
		String mName = this.colName2Getter(colName);
		Method method = this.getClass().getMethod(mName);
		return method.invoke(this);
	}
	public boolean checkValue(String colName,int oldValue,int newValue){
		if(newValue==oldValue)return false;
		if(opType.equals(OperationType.UPDATE)){//update的row才需要记录old值
			if(oldValues==null){
				oldValues = new HashMap<String,Object>();
				oldValues.put(colName, oldValue);
			}else{
				//old值已经保存下来的不要再覆盖，防止多次修改时丢失原始值
				if(!oldValues.containsKey(colName)){
					oldValues.put(colName, oldValue);
				}
			}
		}
		return true;
	}
	public boolean checkValue(String colName,double oldValue,double newValue){
		if(newValue==oldValue)return false;
		if(opType.equals(OperationType.UPDATE)){//update的row才需要记录old值
			if(oldValues==null){
				oldValues = new HashMap<String,Object>();
				oldValues.put(colName, oldValue);
			}else{
				//old值已经保存下来的不要再覆盖，防止多次修改时丢失原始值
				if(!oldValues.containsKey(colName)){
					oldValues.put(colName, oldValue);
				}
			}
		}
		return true;
	}
	public boolean checkValue(String colName,float oldValue,float newValue){
		if(newValue==oldValue)return false;
		if(opType.equals(OperationType.UPDATE)){//update的row才需要记录old值
			if(oldValues==null){
				oldValues = new HashMap<String,Object>();
				oldValues.put(colName, oldValue);
			}else{
				//old值已经保存下来的不要再覆盖，防止多次修改时丢失原始值
				if(!oldValues.containsKey(colName)){
					oldValues.put(colName, oldValue);
				}
			}
		}
		return true;
	}
	public boolean checkValue(String colName,boolean oldValue,boolean newValue){
		if(newValue==oldValue)return false;
		if(opType.equals(OperationType.UPDATE)){//update的row才需要记录old值
			if(oldValues==null){
				oldValues = new HashMap<String,Object>();
				oldValues.put(colName, oldValue);
			}else{
				//old值已经保存下来的不要再覆盖，防止多次修改时丢失原始值
				if(!oldValues.containsKey(colName)){
					oldValues.put(colName, oldValue);
				}
			}
		}
		return true;
	}
	public boolean checkValue(String colName,long oldValue,long newValue){
		if(newValue==oldValue)return false;
		if(opType.equals(OperationType.UPDATE)){//update的row才需要记录old值
			if(oldValues==null){
				oldValues = new HashMap<String,Object>();
				oldValues.put(colName, oldValue);
			}else{
				//old值已经保存下来的不要再覆盖，防止多次修改时丢失原始值
				if(!oldValues.containsKey(colName)){
					oldValues.put(colName, oldValue);
				}
			}
		}
		return true;
	}
	public boolean checkValue(String colName,Object oldValue,Object newValue){
		if(oldValue==null&&newValue==null)return false;
		if(oldValue!=null&&oldValue.equals(newValue))return false;
		if(opType.equals(OperationType.UPDATE)){//update的row才需要记录old值
			if(oldValues==null){
				oldValues = new HashMap<String,Object>();
				oldValues.put(colName, oldValue);
			}else{
				//old值已经保存下来的不要再覆盖，防止多次修改时丢失原始值
				if(!oldValues.containsKey(colName)){
					oldValues.put(colName, oldValue);
				}
			}
		}
		return true;
	}
	/**
	 * 传入基本类型会自动升级成对象类型
	 * @param colName
	 * @param newValue
	 * @throws Exception
	 */
	public <T> void setAttrByCol(String colName,T newValue)throws Exception{
		//colName->setter
		String setter=colName2Setter(colName);
		Class<?>[] argtypes = null;
		//如果newValue非空，则执行setter
		if(newValue!=null){
			if(newValue instanceof Integer){
				argtypes= new Class[]{int.class};
			}else if(newValue instanceof Double){
				argtypes = new Class[]{double.class};
			}else if(newValue instanceof Boolean){
				argtypes= new Class[]{boolean.class};
			}else if(newValue instanceof Float){
				argtypes= new Class[]{float.class};
			}else if(newValue instanceof Long){
				argtypes= new Class[]{long.class};
			}else{
				argtypes = new Class[]{newValue.getClass()};
			}
			Method method = this.getClass().getMethod(setter,argtypes);
			method.invoke(this, newValue);
		}else{
			GlmTable table = GlmFactory.getInstance().getTableByName(tableName());
			GlmColumn column = table.getColumByName(colName);
			if(column.getType().equals(GlmColumn.TYPE_VARCHAR)){
				argtypes = new Class[]{String.class};	
			}else if(column.getType().equals(GlmColumn.TYPE_GEOMETRY)){
				argtypes = new Class[]{Geometry.class};
			}else if(column.getType().equals(GlmColumn.TYPE_TIMESTAMP)){
				argtypes = new Class[]{Date.class};
			}
			Method method = this.getClass().getMethod(setter,argtypes);
			method.invoke(this, newValue);
		}

	}
	/**
	 * 有特殊字段的表重写此方法
	 * @param colName
	 * @return
	 */
//	public String colName2Getter(String colName){
//		StringBuilder sb = new StringBuilder();
//		sb.append("get");
//		for(String s:colName.split("_")){
//			
//			char c = s.charAt(0);
//			c=(char)(c-32);
//			sb.append(c);
//			sb.append(s.substring(1, s.length()));
//		}
//		return sb.toString();
//	}
	public String colName2Getter(String colName){
		StringBuilder sb = new StringBuilder();
		sb.append("get");
		for(String s:colName.split("_")){
			char c = s.charAt(0);
			sb.append(c);
			sb.append(s.toLowerCase().substring(1, s.length()));
		}
		return sb.toString();
	}
	/**
	 * 有特殊字段的表重写此方法
	 * @param colName
	 * @return
	 */
	public String colName2Setter(String colName){
		StringBuilder sb = new StringBuilder();
		sb.append("set");
		for(String s:colName.split("_")){
			char c = s.charAt(0);
			sb.append(c);
			sb.append(s.toLowerCase().substring(1, s.length()));
		}
		return sb.toString();
	}
//	public String colName2Setter(String colName){
//		StringBuilder sb = new StringBuilder();
//		sb.append("set");
//		for(String s:colName.split("_")){
//			char c = s.charAt(0);
//			c=(char)(c-32);
//			sb.append(c);
//			sb.append(s.substring(1, s.length()));
//		}
//		return sb.toString();
//	}
	public String identity(){
		return rowId;
	}
	public int hashCode(){
		return rowId==null?"".hashCode():rowId.hashCode();
	}
	/**
	 * 如果rowId==null,不比较
	 */
	public boolean equals(Object anObject){
		if(anObject==null)return false;
		if(anObject instanceof BasicRow
				&&rowId!=null&&rowId.equals(((BasicRow) anObject).getRowId())){
			return true;
		}else{
			return false;
		}
	}
	
	public static void main(String[] args) {
		System.out.println((long)new Long(100L));
		
	}
	

}
