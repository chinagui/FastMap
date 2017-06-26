package com.navinfo.dataservice.scripts.tmp.diff;

public class DiffField {
	String field ;
	Object mongoValue;
	Object oracleValue;
	public String getField() {
		return field;
	}
	public void setField(String field) {
		this.field = field;
	}
	public Object getMongoValue() {
		return mongoValue;
	}
	public void setMongoValue(Object mongoValue) {
		this.mongoValue = mongoValue;
	}
	public Object getOracleValue() {
		return oracleValue;
	}
	public void setOracleValue(Object oracleValue) {
		this.oracleValue = oracleValue;
	}
	
	
}
