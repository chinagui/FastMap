package com.navinfo.dataservice.engine.editplus.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;

import com.mongodb.client.model.geojson.Geometry;

/** 
 * @ClassName: ResultSetGetter
 * @author xiaoxiaowen4127
 * @date 2016年11月16日
 * @Description: ResultSetGetter.java
 */
public class ResultSetGetter {
	/**
	 * 
	 * @param rs
	 * @param colName
	 * @param dataType
	 * @return
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 */
	public static <T> T getValue(ResultSet rs,String colName, String dataType) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		Class<?>[] argtypes = null;
		if(dataType.equals("NUMBER")){
			argtypes= new Class[]{long.class};
			Method method = ResultSet.class.getMethod("getLong",argtypes);
			return (T) method.invoke(rs, colName);
		}else if(dataType.equals("VARCHAR2")){
			argtypes= new Class[]{String.class};
			Method method = ResultSet.class.getMethod("getString",argtypes);
			return (T) method.invoke(rs, colName);
		}else if(dataType.equals("SDO_GEOMETRY")){
			argtypes= new Class[]{Geometry.class};
			Method method = ResultSet.class.getMethod("getGeometry",argtypes);
			return (T) method.invoke(rs, colName);
		}else{
			return null;
		}
	}
	public static void main(String[] args) {
		System.out.println(int.class.getName());
	}
}
