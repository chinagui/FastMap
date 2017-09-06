package com.navinfo.dataservice.engine.fcc;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.nirobot.common.utils.GridUtils;

/** 
 * @ClassName: Grid2WktTest.java
 * @author y
 * @date 2017-9-4 上午11:07:57
 * @Description: TODO
 *  
 */
public class Grid2WktTest {
	
	
	public static void main(String[] args){
		
		try{
		
		List<String> grids=new ArrayList<String>();
		grids.add("45604102");
		grids.add("45604101");
		grids.add("45604100");
		grids.add("45603130");
		grids.add("45604112");
		
		String wkt=GridUtils.grids2Wkt(grids);
		
		System.out.println(wkt);
		
		}catch (Exception e) {
			e.printStackTrace();
		}

	}

}
