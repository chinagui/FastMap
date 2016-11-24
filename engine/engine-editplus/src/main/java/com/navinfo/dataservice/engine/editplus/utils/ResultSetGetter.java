package com.navinfo.dataservice.engine.editplus.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.engine.editplus.glm.GlmColumn;
import com.navinfo.dataservice.engine.editplus.glm.GlmTable;
import com.navinfo.dataservice.engine.editplus.model.BasicRow;
import com.vividsolutions.jts.geom.Geometry;

import oracle.sql.STRUCT;

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
	
	/**
	 * 
	 * @param rs
	 * @param row
	 * @param glmColumn
	 * @throws Exception 
	 */
	public static void setAttrByCol(ResultSet rs,BasicRow row,GlmColumn glmColumn)throws Exception{
		String columName = glmColumn.getName();
//		if(columName.equals("U_RECORD")||columName.equals("U_FIELDS")||columName.equals("U_DATE")){
//			return;
//		}
		/*--------测试使用start------*/
//		System.out.println(columName);
//		if(columName.equals("GEOMETRY")){
//			System.out.println(columName);
//		}
		/*--------测试使用end------*/
		String type = glmColumn.getType();
		int dataPrecision = glmColumn.getDataPrecision();
		int dataScale = glmColumn.getDataScale();
		
		if(type.equals(GlmColumn.TYPE_NUMBER)){
			if(dataScale > 0){
				row.setAttrByCol(columName, rs.getDouble(columName));
			}else{
				if(dataPrecision>8){
					row.setAttrByCol(columName, rs.getLong(columName));
				}else{
					row.setAttrByCol(columName, rs.getInt(columName));
				}
			}
		}else if(type.equals(GlmColumn.TYPE_VARCHAR)){
			row.setAttrByCol(columName, rs.getString(columName));
		}else if(type.equals(GlmColumn.TYPE_GEOMETRY)){
			STRUCT struct = (STRUCT) rs.getObject(columName);
			row.setAttrByCol(columName, GeoTranslator.struct2Jts(struct));
		}else if(type.equals(GlmColumn.TYPE_RAW)){
			row.setAttrByCol(columName, rs.getString(columName));
		}else if(type.equals(GlmColumn.TYPE_TIMESTAMP)){
			Date date = new Date();
			date = rs.getTimestamp(columName);
			row.setAttrByCol(columName, date);
		}
	}
	
	
	
	public static void main(String[] args) {
		System.out.println(int.class.getName());
	}
}
