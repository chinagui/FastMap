package com.navinfo.dataservice.dao.log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class ObjHandlerUtils {
	/** 
	 * 根据属性名获取属性值 
	 * */  
	   @SuppressWarnings("unused")
	public Object getFieldValueByName(String fieldName, Object o) {  
	       try {    
	           String firstLetter = fieldName.substring(0, 1).toUpperCase();    
	           String getter = "get" + firstLetter + fieldName.substring(1);    
	           Method method = o.getClass().getMethod(getter, new Class[] {});    
	           Object value = method.invoke(o, new Object[] {});    
	           return value;    
	       } catch (Exception e) {    
	    	   e.printStackTrace(); 
	    	   return null;
	       } 
	   }   
	   
	   /** 
		 * 根据属性名设置属性值 
		 * */  
		   @SuppressWarnings("unused")
		public void setFieldValueByName(Field field, Object o,List valueList) {  
		       try {    
		           String firstLetter = field.getName().substring(0, 1).toUpperCase();    
		           String setter = "set" + firstLetter + field.getName().substring(1);    
		           Method method = o.getClass().getMethod(setter, field.getType());   
		           method.invoke(o, valueList); 
		       } catch (Exception e) {    
		    	   e.printStackTrace(); 
		       } 
		   }   
  
}
