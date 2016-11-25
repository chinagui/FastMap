package com.navinfo.dataservice.codegen.db;

import com.navinfo.dataservice.codegen.StringHelper;

/** 
 * @ClassName: Column
 * @author MaYunFei
 * @date 上午11:44:38
 * @Description: Column.java
 */
public class Column {
	private String name;
	private String camelName;
	private int type;
	private String javaType;
	private String sqlType;
	
	/**
	 * @param name
	 * @param type
	 */
	public Column(String name, int type) {
		super();
		this.name = name;
		this.type = type;
		this.camelName= StringHelper.toCamelCase(this.name);
		this.javaType= initJavaType(type);
		this.sqlType = initSqlType(type);
	}
	/**
	 * @param type2
	 * @return
	 */
	private String initSqlType(int type2) {
		switch (type2)  {
			case 2:return "Int";
			case 12:return "String";
			default:return "Object";
		}
	}
	/**
	 * @param type2
	 * @return
	 */
//	private String initJavaType(int type2) {
//		switch (type2)  {
//			case 2:return "Integer";
//			case 12:return "String";
//			default:return "Object";
//		}
//	}
	private String initJavaType(int type2) {
		switch (type2)  {
			case 2:return "int";
			case 12:return "String";
			default:return "Object";
		}
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}
	/**
	 * @return the camelName
	 */
	public String getCamelName() {
		return StringHelper.toCamelCase(this.name);
	}
	/**
	 * @return the sqlType
	 */
	public String getSqlType() {
		return sqlType;
	}
	/**
	 * @return the javaType
	 */
	public String getJavaType() {
		return javaType;
	}
}
