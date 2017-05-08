package com.navinfo.dataservice.scripts;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.navinfo.dataservice.commons.springmvc.ClassPathXmlAppContextInit;
import com.navinfo.dataservice.scripts.tmp.service.CollectConvertUtils;
import net.sf.json.JSONObject;

public class CollectionConvertTest extends ClassPathXmlAppContextInit {

//	@Before
//	public void before(){
//		initContext(new String[]{"dubbo-app-scripts.xml","dubbo-scripts.xml"});
//	}
	
	@Test
	public void testInit() throws Exception {
		try {
			String pathFile = "C:/Users/Administrator/Desktop/IncrementalData_2368_10029_20161110154543/Datum_Point.json";
			List<JSONObject> jsonObjects = CollectConvertUtils.readJsonObjects(pathFile);
			System.out.println(jsonObjects.toString());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void createMkdir() throws Exception {
		try {
			String pathFile = "f:/IncrementalData_2368_10029_20161110154543/101/";
			boolean b = CollectConvertUtils.createMkdir(pathFile);
			System.out.println(b);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void test01() throws Exception {
		try {
			String pathFile = "C:/Users/Administrator/Desktop/IncrementalData_2368_10029_20161110154543/Datum_Point.json";
			List<JSONObject> jsonObjects = CollectConvertUtils.readJsonObjects(pathFile);
			System.out.println(jsonObjects.toString());
			String dirFile = "f:/20170410/101/";
			boolean b = CollectConvertUtils.createMkdir(dirFile);
			System.out.println(b);
			if(b){
				CollectConvertUtils.writeJSONObject2TxtFile(dirFile+"poi.txt", jsonObjects);
				List<Integer> newListJson = new ArrayList<Integer>();
				newListJson.add(1);
				newListJson.add(2);
				newListJson.add(3);
				newListJson.add(4);
				newListJson.add(5);
				newListJson.add(6);
				newListJson.add(7);
				CollectConvertUtils.writeInteger2TxtFile(dirFile+"outConvert.txt", newListJson);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
