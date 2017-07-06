package com.navinfo.dataservice.commons.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * list内的map根据map的key排序
 * 
 * */
public class QuikSortListUtils {
	  public static List<Map<String,Object>> sortListInMapByMapKey(List<Map<String, Object>> list, final String key){
	        Collections.sort(list, new Comparator<Map<String, Object>>(){  
	        public int compare(Map<String, Object> o1, Map<String, Object> o2) {  
	        	String name1 =o1.get(key).toString();//name1是从你list里面拿出来的一个  
	        	String name2= o2.get(key).toString(); //name1是从你list里面拿出来的第二个name      
	        	return name2.compareTo(name1);    
	        }  
	     });  
	        return list;
	  }
}
