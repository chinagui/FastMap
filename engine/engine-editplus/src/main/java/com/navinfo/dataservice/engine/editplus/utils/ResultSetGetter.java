package com.navinfo.dataservice.engine.editplus.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.navinfo.dataservice.engine.editplus.glm.GlmColumn;

/** 
 * @ClassName: ResultSetGetter
 * @author xiaoxiaowen4127
 * @date 2016年11月16日
 * @Description: ResultSetGetter.java
 */
public class ResultSetGetter {
	public static <T> T getValue(ResultSet rs,GlmColumn glmColumn)throws SQLException{
		if(GlmColumn.TYPE_NUMBER.equals(glmColumn.getType())){
			if(glmColumn.getDataPrecision()>8){
			}
		}
		return null;
	}
	public static void main(String[] args) {
		System.out.println(int.class.getName());
	}
}
