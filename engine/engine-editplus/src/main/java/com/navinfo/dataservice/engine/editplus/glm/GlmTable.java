package com.navinfo.dataservice.engine.editplus.glm;

import java.util.HashMap;
import java.util.Map;

/** 
 * @ClassName: GlmTable
 * @author xiaoxiaowen4127
 * @date 2016年11月8日
 * @Description: GlmTable.java
 */
public class GlmTable {
	protected String objType;//所属glm对象名
	protected String name;//表名，大写
	protected String pkColumn;//主键字段，大写，没有主键为null
	protected String modelClassName;//表对应的模型类名
	protected GlmRef objRef;//表所属的要素对象参考信息，只记录自己上一层的参考信息
	protected GlmRef geoRef;//表所属几何对象参考信息，只记录自己上一层的参考信息
	protected Map<String,String> columns=new HashMap<String,String>();
	public String getObjType() {
		return objType;
	}
	public void setObjType(String objType) {
		this.objType = objType;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPkColumn() {
		return pkColumn;
	}
	public void setPkColumn(String pkColumn) {
		this.pkColumn = pkColumn;
	}
	public String getModelClassName() {
		return modelClassName;
	}
	public void setModelClassName(String modelClassName) {
		this.modelClassName = modelClassName;
	}
	public GlmRef getObjRef() {
		return objRef;
	}
	public void setObjRef(GlmRef objRef) {
		this.objRef = objRef;
	}
	public GlmRef getGeoRef() {
		return geoRef;
	}
	public void setGeoRef(GlmRef geoRef) {
		this.geoRef = geoRef;
	}
	public Map<String, String> getColumns() {
		return columns;
	}
	public void setColumns(Map<String, String> columns) {
		this.columns = columns;
	}
	
	public String getGeoObjType(){
		if(geoRef==null||geoRef.isRefMain()){
			return objType;
		}else{
			return GlmFactory.getInstance().getTableByName(geoRef.getRefTable()).getGeoObjType();
		}
	}
}
