package com.navinfo.dataservice.diff.dataaccess;

/** 
 * @ClassName: Column 
 * @author Xiao Xiaowen 
 * @date 2016-1-13 下午6:09:10 
 * @Description: TODO
 */
public class Column {
	private String name;
	private String type;
	private boolean pk;
	public static final String TYPE_NUMBER = "NUMBER";
	public static final String TYPE_VARCHAR2 = "VARCHAR2";
	public static final String TYPE_DATA = "DATA";
	public static final String TYPE_SDO_GEOMETRY = "SDO_GEOMETRY";
	public static final String TYPE_CLOB = "CLOB";
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public boolean isPk() {
		return pk;
	}
	public void setPk(boolean pk) {
		this.pk = pk;
	}
}
