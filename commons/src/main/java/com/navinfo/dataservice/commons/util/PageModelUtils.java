package com.navinfo.dataservice.commons.util;

import java.util.List;
 
public class PageModelUtils {
	/** 
	* List分页   
	* 实现：利用List的获取子List方法，实现对List的分页   
	* 说明：pageNum页码;pageSize每页条数
	* @return List<?>
	* @author songhe   
	*   
	*/  
    public static List<?> ListSplit(List<?> list, int pageNum, int pageSize) { 
        List<?> newList=null; 
        if(pageNum == 0 || pageSize == 0){
        	return newList;
        }
        if((pageNum-1) * pageSize > list.size()){
        	return newList;
        }
        int total = list.size();  
        newList = list.subList(pageSize*(pageNum-1), ((pageSize*pageNum) > total ? total:(pageSize*pageNum)));  
        return newList;  
    }  
}
