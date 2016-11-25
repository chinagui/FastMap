package com.navinfo.dataservice.commons.fileConvert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gdal.gdal.gdal;
import org.gdal.ogr.DataSource;
import org.gdal.ogr.Driver;
import org.gdal.ogr.Feature;
import org.gdal.ogr.FeatureDefn;
import org.gdal.ogr.FieldDefn;
import org.gdal.ogr.Geometry;
import org.gdal.ogr.Layer;
import org.gdal.ogr.ogr;

public class LoadTab {
	private static Logger log = Logger.getLogger(LoadTab.class);

	public LoadTab() {
		// TODO Auto-generated constructor stub
	}
	/**
	 * 根据文件路径，需读取的列名，返回tab文件中的数据
	 * @param filePath "D:/temp/block_tab2json/bj.TAB"
	 * @param columnNameList 需要读取的列名list
	 * @return List<Map<String,Object>> ：List<Map<列名,列值>>，坐标列固定为GEOMETRY,值域为wkt格式
	 * @throws Exception
	 */
	public static List<Map<String,Object>> readTab(String filePath,List<String> columnNameList) throws Exception{
		log.info("start readTab,filePath="+filePath);
		//为了支持中文路径，请添加下面这句代码
		gdal.SetConfigOption("GDAL_FILENAME_IS_UTF8","NO");
		//为了使属性表字段支持中文，请添加下面这句
		gdal.SetConfigOption("SHAPE_ENCODING","");
		ogr.RegisterAll();
		Driver driver=ogr.GetDriverByName("MapInfo File");
		DataSource dataSet = driver.Open(filePath);
		if(dataSet==null){
			log.error("未连接tab，请检查文件正确性。filePath="+filePath);
			return null;}
		Layer layer = dataSet.GetLayerByIndex(0);
		if(layer==null){
			log.error("未获取tab图层，请检查文件正确性。filePath="+filePath);
			return null;}
		FeatureDefn oDefn =layer.GetLayerDefn(); 
		/*System.out.println("属性表结构信息："); 
		int iFieldCount =oDefn.GetFieldCount(); 
		for (int iAttr = 0; iAttr <iFieldCount; iAttr++) { 
			FieldDefn oField =oDefn.GetFieldDefn(iAttr);
		} */
		List<Map<String, Object>> tabDataList=new ArrayList<Map<String,Object>>();
		 // 输出图层中的要素个数 
		log.info("要素个数 = " + layer.GetFeatureCount(0)); 
		Feature oFeature = null; 
		 // 下面开始遍历图层中的要素 
		while ((oFeature =layer.GetNextFeature()) != null) {
			log.debug("当前处理第" + oFeature.GetFID() + "个:\n属性值："); 
			Map<String, Object> oneColumnMap=new HashMap<String, Object>();
			// 获取要素中的属性表内容 
			for (String columnName:columnNameList) { 
				String upperName=columnName.toUpperCase();
				int columnIndex=oDefn.GetFieldIndex(columnName);
				FieldDefn oFieldDefn= oDefn.GetFieldDefn(columnIndex); 
				int type =oFieldDefn.GetFieldType(); 
				switch (type) { 
					case ogr.OFTString:	
						String source=oFeature.GetFieldAsString(columnName);
						String mid3=new String(source.getBytes("ISO-8859-1"),"gbk");
						oneColumnMap.put(upperName, mid3);
						break; 
					case ogr.OFTReal: 
						oneColumnMap.put(upperName,oFeature.GetFieldAsDouble(columnName)); 
						break; 
					case ogr.OFTInteger: 
						oneColumnMap.put(upperName,oFeature.GetFieldAsInteger(columnName)); 
						break; 
					default: 
						oneColumnMap.put(upperName,oFeature.GetFieldAsString(columnName)); 
						break; 
						} 
				} 
		 // 获取要素中的几何体 
			Geometry oGeometry =oFeature.GetGeometryRef(); 
			String geoWkt=oGeometry.ExportToWkt();
			oneColumnMap.put("GEOMETRY",geoWkt);
			tabDataList.add(oneColumnMap);
		 } 
		driver.delete();
		System.out.println("数据集关闭！"); 
		log.info("end readTab,filePath="+filePath);
		return tabDataList;
		}
	
	/**
	 * 根据文件路径，返回tab文件中的所有数据
	 * @param filePath "D:/temp/block_tab2json/bj.TAB"
	 * @return List<Map<String,Object>> ：List<Map<列名,列值>>，坐标列固定为GEOMETRY,值域为wkt格式
	 * @throws Exception
	 */
	public static List<Map<String,Object>> readTabReturnAllData(String filePath) throws Exception{
		log.info("start readTabReturnAllData,filePath="+filePath);
		//为了支持中文路径，请添加下面这句代码
		gdal.SetConfigOption("GDAL_FILENAME_IS_UTF8","NO");
		//为了使属性表字段支持中文，请添加下面这句
		gdal.SetConfigOption("SHAPE_ENCODING","");
		ogr.RegisterAll();
		Driver driver=ogr.GetDriverByName("MapInfo File");
		DataSource dataSet = driver.Open(filePath);
		if(dataSet==null){
			log.error("未连接tab，请检查文件正确性。filePath="+filePath);
			return null;}
		Layer layer = dataSet.GetLayerByIndex(0);
		if(layer==null){
			log.error("未获取tab图层，请检查文件正确性。filePath="+filePath);
			return null;}
		FeatureDefn oDefn =layer.GetLayerDefn(); 
		System.out.println("属性表结构信息："); 
		int iFieldCount =oDefn.GetFieldCount(); 
//		for (int iAttr = 0; iAttr <iFieldCount; iAttr++) { 
//			FieldDefn oField =oDefn.GetFieldDefn(iAttr);
//		}
		List<Map<String, Object>> tabDataList=new ArrayList<Map<String,Object>>();
		 // 输出图层中的要素个数 
		log.info("要素个数 = " + layer.GetFeatureCount(0)); 
		Feature oFeature = null; 
		 // 下面开始遍历图层中的要素 
		while ((oFeature =layer.GetNextFeature()) != null) {
			log.debug("当前处理第" + oFeature.GetFID() + "个:\n属性值："); 
			Map<String, Object> oneColumnMap=new HashMap<String, Object>();
			// 获取要素中的属性表内容 
			for (int iAttr = 0; iAttr <iFieldCount; iAttr++) { 
				FieldDefn oFieldDefn= oDefn.GetFieldDefn(iAttr); 
				int type =oFieldDefn.GetFieldType(); 
				String columnName=oFieldDefn.GetName();
				String upperName=columnName.toUpperCase();
				switch (type) { 
					case ogr.OFTString:	
						String source=oFeature.GetFieldAsString(columnName);
						String mid3=new String(source.getBytes("ISO-8859-1"),"gbk");
						oneColumnMap.put(upperName, mid3);
						break; 
					case ogr.OFTReal: 
						oneColumnMap.put(upperName,oFeature.GetFieldAsDouble(columnName)); 
						break; 
					case ogr.OFTInteger: 
						oneColumnMap.put(upperName,oFeature.GetFieldAsInteger(columnName)); 
						break; 
					default: 
						oneColumnMap.put(upperName,oFeature.GetFieldAsString(columnName)); 
						break; 
						} 
				} 
		 // 获取要素中的几何体 
			Geometry oGeometry =oFeature.GetGeometryRef(); 
			String geoWkt=oGeometry.ExportToWkt();
			oneColumnMap.put("GEOMETRY",geoWkt);
			tabDataList.add(oneColumnMap);
		 } 
		driver.delete();
		System.out.println("数据集关闭！"); 
		log.info("end readTabReturnAllData,filePath="+filePath);
		return tabDataList;
		}
	
	public static void main(String[] args) throws Exception{
		System.out.println("start"); 
		String filePathString="D:/temp/block_tab2json/bj.TAB";
		List<String> columnNameList=new ArrayList<String>();
		LoadTab.readTab(filePathString,columnNameList);
		System.out.println("end"); 
	}

}
