package com.navinfo.dataservice.commons.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.ListUtils;

/*
 * @author MaYunFei
 * 2016年6月14日
 * 描述：commonsNaviCollectionUtils.java
 */
public class NaviListUtils extends ListUtils {
	public static Object[][] toArrayMatrix(List<List> listMatrix){
		int rowLength = listMatrix.size();
		Object[][] arrayMatrix = new Object[rowLength][2];
		for (int i=0;i<rowLength;i++){
			List<?> row = listMatrix.get(i);
			arrayMatrix[i]=row.toArray();
		}
		return arrayMatrix;
	} ;	
	public static void main(String[] args){
		List<List> listMaxtrix = new ArrayList<List>();
		List<String> row = new ArrayList<String>();
		row.add("123");
		row.add("234");
		listMaxtrix.add(row);
		listMaxtrix.add(row);
		Object[][] arrayMatrix = toArrayMatrix(listMaxtrix );
		
		for (Object[] r : arrayMatrix){
			for (Object col :r){
				System.out.println(col);
			}
		}
	}
}

