package com.navinfo.dataservice.commons.fileConvert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.gdal.gdal.gdal;
import org.gdal.ogr.DataSource;
import org.gdal.ogr.Driver;
import org.gdal.ogr.Feature;
import org.gdal.ogr.FeatureDefn;
import org.gdal.ogr.FieldDefn;
import org.gdal.ogr.Layer;
import org.gdal.ogr.ogr;
import com.navinfo.dataservice.commons.util.JtsGeometryFactory;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.GridUtils;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;

public class LoadAndCreateTab {
	private static Logger log = Logger.getLogger(LoadAndCreateTab.class);

	public LoadAndCreateTab() {
		// TODO Auto-generated constructor stub
	}
	/**
	 * @Title: readTab
	 * @Description: 读取tab文件
	 * @param filePath
	 * @param columnNameList
	 * @return
	 * @throws Exception  List<Map<String,Object>>
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年2月15日 下午3:31:58 
	 */
	public static List<City> readTab(String filePath,List<String> columnNameList) throws Exception{
//		log.info("start readTab,filePath="+filePath);
		//为了支持中文路径，请添加下面这句代码
		gdal.SetConfigOption("GDAL_FILENAME_IS_UTF8","NO");
		//为了使属性表字段支持中文，请添加下面这句
//		gdal.SetConfigOption("SHAPE_ENCODING", "UTF8");
		//gdal.SetConfigOption("SHAPE_ENCODING", "gb2312");
		gdal.SetConfigOption("SHAPE_ENCODING", "");
		
		//gdal.SetConfigOption("SHAPE_ENCODING","DXF_ENCODING");
		//String encode = gdal.GetConfigOption("SHAPE_ENCODING");
		//System.out.println("encode:  "+encode);
		/*int wei = gdal.GetCacheMax();
		gdal.SetConfigOption("GDAL_CACHEMAX", "64");
		int wei1 = gdal.GetCacheMax();*/
		
		ogr.RegisterAll();
		Driver driver=ogr.GetDriverByName("MapInfo File");
		DataSource dataSet = driver.Open(filePath);
		if(dataSet==null){
			System.out.println("未连接tab，请检查文件正确性。filePath="+filePath);
//			log.error("未连接tab，请检查文件正确性。filePath="+filePath);
			return null;}
		Layer layer = dataSet.GetLayerByIndex(0);
		if(layer==null){
			System.out.println("未获取tab图层，请检查文件正确性。filePath="+filePath);
//			log.error("未获取tab图层，请检查文件正确性。filePath="+filePath);
			return null;}
		FeatureDefn oDefn =layer.GetLayerDefn(); 
		/*System.out.println("属性表结构信息："); 
		int iFieldCount =oDefn.GetFieldCount(); 
		for (int iAttr = 0; iAttr <iFieldCount; iAttr++) { 
			FieldDefn oField =oDefn.GetFieldDefn(iAttr);
		} */
		//List<Map<String, Object>> tabDataList=new ArrayList<Map<String,Object>>();
		List<City> cityList = new ArrayList<City>();
		 // 输出图层中的要素个数 
		System.out.println("要素个数 = " + layer.GetFeatureCount(0));
//		log.info("要素个数 = " + layer.GetFeatureCount(0)); 
		Feature oFeature = null; 
		
	
		
		//记录已经被占用的meshId
		Set<String> usedmeshIds = new HashSet<String>();
		
		
		Map<String,City> citys = new HashMap<String,City>();//城市名,city
		while((oFeature =layer.GetNextFeature()) != null){
			//
			String geoWkt=oFeature.GetGeometryRef().ExportToWkt();
			Geometry jtsGeo = JtsGeometryFactory.read(geoWkt);
			Set<String> meshIds = CompGeometryUtil.geoToMeshesWithoutBreak(jtsGeo);
			if(oFeature.GetFieldAsString("BlockCode").equals("1102292200") || oFeature.GetFieldAsString("BlockCode").equals("1101172200")){
				System.out.println(meshIds.size());
			}
			meshIds.removeAll(usedmeshIds);//去除已经分配的meshId
			usedmeshIds.addAll(meshIds);//将已分配的meshId 放入已占用set
			if(meshIds != null && meshIds.size() > 0){
				String feaName = oFeature.GetFieldAsString("Name");
				String feaCity = oFeature.GetFieldAsString("city");
				String feaProvince = oFeature.GetFieldAsString("province");
				String feaCounty = oFeature.GetFieldAsString("County");
				String feaArea = oFeature.GetFieldAsString("Area");
				String feaNumber = oFeature.GetFieldAsString("Number");
				//String Job1 = oFeature.GetFieldAsString("Job1");
				
				if(citys.containsKey(feaCity)){
					citys.get(feaCity).getMeshIds().addAll(meshIds);
				}else{
					//cityMeshMap.put(feaName, meshIds);
					City city = new City();
					city.setName(feaName);
					city.setCity(feaCity);;
					city.setArea(feaArea);
					city.setCounty(feaCounty);
					//city.setNumber(feaNumber);
					city.setProvince(feaProvince);
					city.setMeshIds(meshIds);
					citys.put(feaCity, city);
				}
			}
			
		}
		if(citys != null && citys.size() > 0){
			for(String cityName : citys.keySet()){
//				City cityObj = citys.get(cityName);
				cityList.add(citys.get(cityName));
				/*for(String meshId:cityObj.getMeshIds()){
					
				}*/
			}
		}
		
		/* // 下面开始遍历图层中的要素 
		while ((oFeature =layer.GetNextFeature()) != null) {
			//log.debug("当前处理第" + oFeature.GetFID() + "个:\n属性值："); 
			Map<String, Object> oneColumnMap=new HashMap<String, Object>();
			// 获取要素中的属性表内容 
			for (String columnName:columnNameList) { 
				String upperName=columnName.toUpperCase();
				int columnIndex=oDefn.GetFieldIndex(columnName);
				FieldDefn oFieldDefn= oDefn.GetFieldDefn(columnIndex); 
				if(oFieldDefn==null){continue;}
				int type =oFieldDefn.GetFieldType(); 
				switch (type) { 
					case ogr.OFTString:	
						String source=oFeature.GetFieldAsString(columnName);
						byte[] b = source.getBytes();
						String mid31=new String(source.getBytes(),"gbk");
						String mid32=new String(source.getBytes(),"ISO-8859-1");
						//String b1=new String(source.getBytes(),"gb-2312");
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
			WKTReader reader=new WKTReader();
			//Geometry oGeometry =oFeature.GetGeometryRef(); 
			String geoWkt=oFeature.GetGeometryRef().ExportToWkt();
			//String geoWkt=oGeometry.ExportToWkt();
			//oneColumnMap.put("GEOMETRY",geoWkt);
			Geometry geo = reader.read(geoWkt);
			Set<String> meshIds = CompGeometryUtil.geoToMeshesWithoutBreak(geo);
			meshIds.removeAll(usedmeshIds);//去除已经分配的meshId
			usedmeshIds.addAll(meshIds);//将已分配的meshId 放入已占用set
			
			Map<String, Object> fmCityMap=new HashMap<String, Object>();
			for(String meshId : meshIds){
				
				fmCityMap.putAll(oneColumnMap);
				fmCityMap.put("MESH_ID", meshId);
				
				tabDataList.add(fmCityMap);
			}
		 } */
		driver.delete();
		System.out.println("数据集关闭！"); 
		System.out.println("end readTab,filePath="+filePath);
//		log.info("end readTab,filePath="+filePath);
		 //根据mesh_id 排重
		//System.out.println("数据集排重！"); 
//        List<Map<String, Object>> newList = distinctListByMeshId(tabDataList);
//		return newList;
		return cityList;
		}
	
	/**
	 * 根据文件路径，返回tab文件中的所有数据
	 * @param filePath "D:/temp/block_tab2json/bj.TAB"
	 * @return List<Map<String,Object>> ：List<Map<列名,列值>>，坐标列固定为GEOMETRY,值域为wkt格式；返回值列名均大写
	 * @throws Exception
	 */
	public static List<Map<String,Object>> readTabReturnAllData(String filePath) throws Exception{
		log.info("start readTabReturnAllData,filePath="+filePath);
		//为了支持中文路径，请添加下面这句代码
		gdal.SetConfigOption("GDAL_FILENAME_IS_UTF8","NO");
		//为了使属性表字段支持中文，请添加下面这句
		gdal.SetConfigOption("SHAPE_ENCODING","CP936");
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
		List<Map<String, Object>> tabDataList=new ArrayList<Map<String,Object>>();
		 // 输出图层中的要素个数 
		log.info("要素个数 = " + layer.GetFeatureCount(0)); 
		
		//记录已经被占用的meshId
				Set<String> usedmeshIds = new HashSet<String>();
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
//						String source=oFeature.GetFieldAsString(columnName);
						String source=oFeature.GetFieldAsString(iAttr);
						System.out.println(new String(oFeature.GetFieldAsString(iAttr).getBytes("ISO-8859-1"),"gbk"));
						System.out.println("source "+source);
						String mid3=new String(source.getBytes("ISO-8859-1"),"gbk");
						System.out.println("mid3 "+mid3);
						
						System.out.println("mid31 "+new String(source.getBytes("gbk"),"utf-8"));
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
//			Geometry oGeometry =oFeature.GetGeometryRef(); 
//			String geoWkt=oGeometry.ExportToWkt();
			WKTReader reader=new WKTReader();
			String geoWkt=oFeature.GetGeometryRef().ExportToWkt();
			oneColumnMap.put("GEOMETRY",geoWkt);
			Geometry geo = reader.read(geoWkt);
			Set<String> meshIds = CompGeometryUtil.geoToMeshesWithoutBreak(geo);
			meshIds.removeAll(usedmeshIds);//去除已经分配的meshId
			usedmeshIds.addAll(meshIds);//将已分配的meshId 放入已占用set
			
			Map<String, Object> fmCityMap=new HashMap<String, Object>();
			for(String meshId : meshIds){
				fmCityMap.putAll(oneColumnMap);
				fmCityMap.put("MESH_ID", meshId);
				tabDataList.add(fmCityMap);
			}
		 } 
		driver.delete();
		System.out.println("数据集关闭！"); 
		log.info("end readTabReturnAllData,filePath="+filePath);
		return tabDataList;
		}
	
	
	
	public static String createCityMeshTabFile(List<City> dataList) {
//		String strVectorFile ="F:\\gdal_file\\123.TAB";    
		String strVectorFile ="F:\\gdal_file\\45612.shp";    
        
        // 注册所有的驱动  
        ogr.RegisterAll();  
        
        // 为了支持中文路径，请添加下面这句代码  
        gdal.SetConfigOption("GDAL_FILENAME_IS_UTF8","NO");  
        // 为了使属性表字段支持中文，请添加下面这句  
      //  System.out.println(" encode : "+ gdal.GetConfigOption("SHAPE_ENCODING"));
       // gdal.SetConfigOption("SHAPE_ENCODING","CP936");  
        gdal.SetConfigOption("SHAPE_ENCODING","");  
        System.out.println(" encode2 : "+ gdal.GetConfigOption("SHAPE_ENCODING"));
  
       
        //创建数据，这里以创建ESRI的shp文件为例  
//        String strDriverName = "MapInfo File";//"ESRI Shapefile";  
        String strDriverName = "ESRI Shapefile";//"ESRI Shapefile";  
//        org.gdal.ogr.Driver oDriver =ogr.GetDriverByName(strDriverName);  
        org.gdal.ogr.Driver oDriver =ogr.GetDriverByName(strDriverName);  
        if (oDriver == null)  
        {  
                  System.out.println(strVectorFile+ " 驱动不可用！\n");  
                  return null;  
        }  
  
        // 创建数据源  
        DataSource oDS = oDriver.CreateDataSource(strVectorFile,null);  
        if (oDS == null)  
        {  
                  System.out.println("创建矢量文件【"+ strVectorFile +"】失败！\n" );  
                  return null;  
        }  
  
        // 创建图层，创建一个多边形图层，这里没有指定空间参考，如果需要的话，需要在这里进行指定  
        Layer oLayer =oDS.CreateLayer("001", null, ogr.wkbPolygon, null); 
        if (oLayer == null)  
        {  
                  System.out.println("图层创建失败！\n");  
                  return null;  
        }  
  
        // 下面创建属性表  
        // 先创建一个叫属性  
        FieldDefn oFieldName = new FieldDefn("Name", ogr.OFTString); 
        oFieldName.SetWidth(100);
        oLayer.CreateField(oFieldName, 1);  
  
        // 再创建一个叫oFieldProvince的字符型属性，字符长度为50  
        FieldDefn oFieldProvince = new FieldDefn("province", ogr.OFTString);  
        oFieldName.SetWidth(100);  
        oLayer.CreateField(oFieldProvince, 1);  
        
        // 再创建一个叫oFieldCity的字符型属性，字符长度为50  
        FieldDefn oFieldCity = new FieldDefn("city", ogr.OFTString);  
        oFieldName.SetWidth(100);  
        oLayer.CreateField(oFieldCity, 1); 
        
        // 再创建一个叫oFieldCounty的字符型属性，字符长度为50  
        FieldDefn oFieldCounty = new FieldDefn("Country", ogr.OFTString);  
        oFieldName.SetWidth(100);  
        oLayer.CreateField(oFieldCounty, 1); 
        
        // 再创建一个叫oFieldNumber的字符型属性
        FieldDefn oFieldNumber = new FieldDefn("Number", ogr.OFTString);  
        oFieldName.SetWidth(100);
        oLayer.CreateField(oFieldNumber, 1); 
        
        // 再创建一个叫oFieldMemo的字符型属性，字符长度为500  
        /*FieldDefn oFieldMemo = new FieldDefn("Memo", ogr.OFTString);  
        oFieldName.SetWidth(1000);  
        oLayer.CreateField(oFieldMemo, 1); */
        
        // 再创建一个叫oFieldJob1的字符型属性，字符长度为50  
        /*FieldDefn oFieldJob1 = new FieldDefn("Job1", ogr.OFTString);  
        oFieldName.SetWidth(100);  
        oLayer.CreateField(oFieldJob1, 1); */
  

        // 再创建一个叫oFieldNumber的字符型属性，字符长度为50  
        FieldDefn oFieldMeshId = new FieldDefn("MESH_ID", ogr.OFTString);  
        oFieldName.SetWidth(100); 
        oLayer.CreateField(oFieldMeshId, 1);    
   
        System.out.println("oLayer.GetRefCount() : "+oLayer.GetRefCount());
        FeatureDefn oDefn =oLayer.GetLayerDefn();  
        // 创建要素  
        if(dataList != null  && dataList.size()>0){
//        	 for(Map<String, Object> mapColumn : dataList){
        	for(City cityObj : dataList){
        		 String wktStr = null;
        		 for(String meshId : cityObj.getMeshIds()){
        			 Feature oFeature = new Feature(oDefn);  
         	        oFeature.SetField(0, cityObj.getName());  
         	        oFeature.SetField(1, cityObj.getProvince());  
         	        oFeature.SetField(2, cityObj.getCity());
         	        oFeature.SetField(3, cityObj.getCounty());
         	      //  oFeature.SetField(4, cityObj.getNumber());
         	        oFeature.SetField(5, meshId);
         	        
         	    if(meshId != null && StringUtils.isNotEmpty(meshId)){
       	        	wktStr = MeshUtils.mesh2WKT(meshId); 
       	        	System.out.println("meshId: "+meshId+" wktStr: "+wktStr);
       	        }
       	        
       	        if(wktStr != null && StringUtils.isNotEmpty(wktStr)){
       	        	org.gdal.ogr.Geometry geomTriangle =org.gdal.ogr.Geometry.CreateFromWkt(wktStr);  
       	        	oFeature.SetGeometry(geomTriangle);
       	        	oLayer.CreateFeature(oFeature); 
       	        }
        		 }
             }
        }    
          
        //写入文件  
        oLayer.SyncToDisk();  
        oDS.SyncToDisk();  
  
        System.out.println("\n数据集创建完成！\n");  
        return null;
	}    
	
	/**
	 * @Title: distinctListByMeshId
	 * @Description: 根据mesh_id 排重
	 * @param dataList
	 * @return  List<Map<String,Object>>
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年2月21日 下午2:56:47 
	 */
	private static List<Map<String, Object>> distinctListByMeshId(List<Map<String, Object>> dataList) {
		List<Map<String, Object>> newList = new ArrayList<Map<String, Object>>();
		if(dataList != null  && dataList.size()>0){//
			Map<String,Map<String, Object>> meshMap = new HashMap<String,Map<String, Object>>();
       	 for(Map<String, Object> mapColumn : dataList){
       		String meshId = (String) mapColumn.get("MESH_ID");
       		if(!meshMap.keySet().contains(meshId)){//如果没重复
       			meshMap.put(meshId, mapColumn);
       		}else{//已存在,去重 合并city
       			Map<String, Object> cityMay = meshMap.get(meshId);//获取已有的
       			if(mapColumn.get("NAME") != null && StringUtils.isNotEmpty((String) mapColumn.get("NAME")) 
       					&& !mapColumn.get("NAME").equals("NULL") && !((String)cityMay.get("NAME")).equals(((String)mapColumn.get("NAME")))){
       				cityMay.put("NAME", cityMay.get("NAME")+","+mapColumn.get("NAME"));
       			}
       			if(mapColumn.get("CITY") != null && StringUtils.isNotEmpty((String) mapColumn.get("CITY")) 
       					&& !mapColumn.get("CITY").equals("NULL") && !((String)cityMay.get("CITY")).equals(((String)mapColumn.get("CITY")))){
       				cityMay.put("CITY", cityMay.get("CITY")+","+mapColumn.get("CITY"));
       			}
       			if(mapColumn.get("PROVINCE") != null && StringUtils.isNotEmpty((String) mapColumn.get("PROVINCE")) 
       					&& !mapColumn.get("PROVINCE").equals("NULL") && !((String)cityMay.get("PROVINCE")).equals(((String)mapColumn.get("PROVINCE")))){
       				cityMay.put("PROVINCE", cityMay.get("PROVINCE")+","+mapColumn.get("PROVINCE"));
       			}
       			if(mapColumn.get("COUNTY") != null && StringUtils.isNotEmpty((String) mapColumn.get("COUNTY")) 
       					&& !mapColumn.get("COUNTY").equals("NULL") && !((String)cityMay.get("COUNTY")).equals(((String)mapColumn.get("COUNTY")))){
       				cityMay.put("COUNTY", cityMay.get("COUNTY")+","+mapColumn.get("COUNTY"));
       			}
       			if(cityMay.get("NUMBER") != mapColumn.get("NUMBER")){
       				cityMay.put("NUMBER", cityMay.get("NUMBER")+","+mapColumn.get("NUMBER"));
       			}
       				
       		}
       	 }
       	Set set = meshMap.entrySet();
        
        for(Iterator iter = set.iterator(); iter.hasNext();)
        {
         Map.Entry entry = (Map.Entry)iter.next();
         newList.add((Map<String, Object>) entry.getValue());
        }
      // 	newList = (List<Map<String, Object>>) meshMap.values();
		}
		return newList;
	}
	/**
	 * @Title: createTabFile
	 * @Description: 创建 tab 文件
	 * @param dataList
	 * @return  String
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年2月17日 下午2:46:54 
	 */
	public static String createTabFile(List<Map<String, Object>> dataList) {
		String strVectorFile ="F:\\gdal_file";    
        
        // 注册所有的驱动  
        ogr.RegisterAll();  
        
        // 为了支持中文路径，请添加下面这句代码  
        gdal.SetConfigOption("GDAL_FILENAME_IS_UTF8","NO");  
        // 为了使属性表字段支持中文，请添加下面这句  
        System.out.println(" encode : "+ gdal.GetConfigOption("SHAPE_ENCODING"));
        gdal.SetConfigOption("SHAPE_ENCODING","CP936");  
        System.out.println(" encode2 : "+ gdal.GetConfigOption("SHAPE_ENCODING"));
  
       
        //创建数据，这里以创建ESRI的shp文件为例  
        String strDriverName = "MapInfo File";//"ESRI Shapefile";  
        org.gdal.ogr.Driver oDriver =ogr.GetDriverByName(strDriverName);  
        if (oDriver == null)  
        {  
                  System.out.println(strVectorFile+ " 驱动不可用！\n");  
                  return null;  
        }  
  
        // 创建数据源  
        DataSource oDS = oDriver.CreateDataSource(strVectorFile,null);  
        if (oDS == null)  
        {  
                  System.out.println("创建矢量文件【"+ strVectorFile +"】失败！\n" );  
                  return null;  
        }  
  
        // 创建图层，创建一个多边形图层，这里没有指定空间参考，如果需要的话，需要在这里进行指定  
        Layer oLayer =oDS.CreateLayer("001", null, ogr.wkbPolygon, null); 
//        Layer oLayer =oDS.CreateLayer("TestMap");  
        if (oLayer == null)  
        {  
                  System.out.println("图层创建失败！\n");  
                  return null;  
        }  
  
        // 下面创建属性表  
        // 先创建一个叫FieldID的整型属性  
        FieldDefn oFieldID = new FieldDefn("FieldID", ogr.OFTInteger);  
        oLayer.CreateField(oFieldID, 1);  
  
        // 再创建一个叫FeatureName的字符型属性，字符长度为50  
        FieldDefn oFieldName = new FieldDefn("FieldName", ogr.OFTString);  
        oFieldName.SetWidth(100);  
        oLayer.CreateField(oFieldName, 1);  
  
        FeatureDefn oDefn =oLayer.GetLayerDefn();  
  
        // 创建三角形要素  
        Feature oFeatureTriangle = new Feature(oDefn);  
        oFeatureTriangle.SetField(0, 0);  
        oFeatureTriangle.SetField(1, "三角形");  
        org.gdal.ogr.Geometry geomTriangle =org.gdal.ogr.Geometry.CreateFromWkt("POLYGON ((0 0,20 0,10 15,0 0))");  
        oFeatureTriangle.SetGeometry(geomTriangle);  
  
        oLayer.CreateFeature(oFeatureTriangle);  
  
        // 创建矩形要素  
        Feature oFeatureRectangle = new Feature(oDefn);  
        oFeatureRectangle.SetField(0, 1);  
        oFeatureRectangle.SetField(1, "矩形");  
        org.gdal.ogr.Geometry geomRectangle =org.gdal.ogr.Geometry.CreateFromWkt("POLYGON ((30 0,60 0,60 30,30 30,30 0))");  
        oFeatureRectangle.SetGeometry(geomRectangle);  
  
        oLayer.CreateFeature(oFeatureRectangle);  
  
        // 创建五角形要素  
        Feature oFeaturePentagon = new Feature(oDefn);  
        oFeaturePentagon.SetField(0, 2);  
        oFeaturePentagon.SetField(1, "五角形");  
        org.gdal.ogr.Geometry geomPentagon =org.gdal.ogr.Geometry.CreateFromWkt("POLYGON ((70 0,85 0,90 15,80 30,65 15,70 0))");  
        oFeaturePentagon.SetGeometry(geomPentagon);  
  
        oLayer.CreateFeature(oFeaturePentagon);  
          
        //写入文件  
        oLayer.SyncToDisk();  
        oDS.SyncToDisk();  
  
        System.out.println("\n数据集创建完成！\n");  
        return null;
	}    
	
	
	/*public static  void createShpFile(){
		String strVectorFile ="F:\\gdal_file\\testShap05.shp";    
        
        // 注册所有的驱动  
        ogr.RegisterAll();  
         
        // 为了支持中文路径，请添加下面这句代码  
        gdal.SetConfigOption("GDAL_FILENAME_IS_UTF8","NO");  
        // 为了使属性表字段支持中文，请添加下面这句  
        gdal.SetConfigOption("SHAPE_ENCODING","");  
  
        //创建数据，这里以创建ESRI的shp文件为例  
        String strDriverName = "ESRI Shapefile";  
        org.gdal.ogr.Driver oDriver =ogr.GetDriverByName(strDriverName);  
        if (oDriver == null)  
        {  
                  System.out.println(strVectorFile+ " 驱动不可用！\n");  
                  return;  
        }  
  
        // 创建数据源  
        DataSource oDS = oDriver.CreateDataSource(strVectorFile,null);  
        if (oDS == null)  
        {  
                  System.out.println("创建矢量文件【"+ strVectorFile +"】失败！\n" );  
                  return;  
        }  
  
        // 创建图层，创建一个多边形图层，这里没有指定空间参考，如果需要的话，需要在这里进行指定  
        Layer oLayer =oDS.CreateLayer("TestPolygon", null, ogr.wkbPolygon, null);  
        if (oLayer == null)  
        {  
                  System.out.println("图层创建失败！\n");  
                  return;  
        }  
  
        // 下面创建属性表  
        // 先创建一个叫FieldID的整型属性  
        FieldDefn oFieldID = new FieldDefn("FieldID", ogr.OFTInteger);  
        oLayer.CreateField(oFieldID, 1);  
  
        // 再创建一个叫FeatureName的字符型属性，字符长度为50  
        FieldDefn oFieldName = new FieldDefn("FieldName", ogr.OFTString);  
        oFieldName.SetWidth(100);  
        oLayer.CreateField(oFieldName, 1);  
  
        FeatureDefn oDefn =oLayer.GetLayerDefn();  
  
        // 创建三角形要素  
        Feature oFeatureTriangle = new Feature(oDefn);  
        oFeatureTriangle.SetField(0, 0);  
        oFeatureTriangle.SetField(1, "三角形");  
        org.gdal.ogr.Geometry geomTriangle =org.gdal.ogr.Geometry.CreateFromWkt("POLYGON ((0 0,20 0,10 15,0 0))");  
        oFeatureTriangle.SetGeometry(geomTriangle);  
  
        oLayer.CreateFeature(oFeatureTriangle);  
  
        // 创建矩形要素  
        Feature oFeatureRectangle = new Feature(oDefn);  
        oFeatureRectangle.SetField(0, 1);  
        oFeatureRectangle.SetField(1, "矩形");  
        org.gdal.ogr.Geometry geomRectangle =org.gdal.ogr.Geometry.CreateFromWkt("POLYGON ((30 0,60 0,60 30,30 30,30 0))");  
        oFeatureRectangle.SetGeometry(geomRectangle);  
  
        oLayer.CreateFeature(oFeatureRectangle);  
  
        // 创建五角形要素  
        Feature oFeaturePentagon = new Feature(oDefn);  
        oFeaturePentagon.SetField(0, 2);  
        oFeaturePentagon.SetField(1, "五角形");  
        org.gdal.ogr.Geometry geomPentagon =org.gdal.ogr.Geometry.CreateFromWkt("POLYGON ((70 0,85 0,90 15,80 30,65 15,70 0))");  
        oFeaturePentagon.SetGeometry(geomPentagon);  
  
        oLayer.CreateFeature(oFeaturePentagon);  
          
        //写入文件  
        oLayer.SyncToDisk();  
        oDS.SyncToDisk();  
  
        System.out.println("\n数据集创建完成！\n");  
}    */
	
	
	/**
	 * @Title: readBlockTab
	 * @Description: 读取 tab文件获取blockGrid
	 * @param filePath
	 * @param columnNameList
	 * @return
	 * @throws Exception  List<City>
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年2月22日 下午7:20:02 
	 */
	public static List<Block> readBlockTab(String filePath,List<String> columnNameList) throws Exception{
//		log.info("start readTab,filePath="+filePath);
		//为了支持中文路径，请添加下面这句代码
		gdal.SetConfigOption("GDAL_FILENAME_IS_UTF8","NO");
		//为了使属性表字段支持中文，请添加下面这句
//		gdal.SetConfigOption("SHAPE_ENCODING", "UTF8");
		//gdal.SetConfigOption("SHAPE_ENCODING", "gb2312");
		gdal.SetConfigOption("SHAPE_ENCODING", "");
		
		ogr.RegisterAll();
		Driver driver=ogr.GetDriverByName("MapInfo File");
		DataSource dataSet = driver.Open(filePath);
		if(dataSet==null){
			System.out.println("未连接tab，请检查文件正确性。filePath="+filePath);
//			log.error("未连接tab，请检查文件正确性。filePath="+filePath);
			return null;}
		Layer layer = dataSet.GetLayerByIndex(0);
		if(layer==null){
			System.out.println("未获取tab图层，请检查文件正确性。filePath="+filePath);
//			log.error("未获取tab图层，请检查文件正确性。filePath="+filePath);
			return null;}
		FeatureDefn oDefn =layer.GetLayerDefn(); 
		List<Block> blockList = new ArrayList<Block>();
		 // 输出图层中的要素个数 
		System.out.println("要素个数 = " + layer.GetFeatureCount(0));
		Feature oFeature = null; 
		
		//记录已经被占用的grids
		Set<String> usedGridIds = new HashSet<String>();
		
		
		Map<String,Block> blocks = new HashMap<String,Block>();//BlockCode,Block
		while((oFeature =layer.GetNextFeature()) != null){
			//
			String geoWkt=oFeature.GetGeometryRef().ExportToWkt();
			Geometry jtsGeo = JtsGeometryFactory.read(geoWkt);
			Set<String> gridIds = CompGeometryUtil.geo2GridsWithoutBreak(jtsGeo);
			
			gridIds.removeAll(usedGridIds);//去除已经分配的meshId
			usedGridIds.addAll(gridIds);//将已分配的meshId 放入已占用set
			if(gridIds != null && gridIds.size() > 0){
				String feaName = oFeature.GetFieldAsString("Name");
				String feaCity = oFeature.GetFieldAsString("city");
				String feaProvince = oFeature.GetFieldAsString("province");
				String feaCounty = oFeature.GetFieldAsString("County");
				String feaArea = oFeature.GetFieldAsString("Area");
				String feaNumber = oFeature.GetFieldAsString("Number");
				String feaBlockCode = oFeature.GetFieldAsString("BlockCode");
				System.out.println("feaBlockCode: "+feaBlockCode);
				if(blocks.containsKey(feaBlockCode)){
					blocks.get(feaBlockCode).getGridIds().addAll(gridIds);
				}else{
					//cityMeshMap.put(feaName, meshIds);
					Block block = new Block();
					block.setName(feaName);
					block.setCity(feaCity);;
					block.setArea(feaArea);
					block.setCounty(feaCounty);
					block.setNumber(feaNumber);
					block.setProvince(feaProvince);
					block.setGridIds(gridIds);
					blocks.put(feaBlockCode, block);
				}
			}
			
		}
		if(blocks != null && blocks.size() > 0){
			for(String blockCode : blocks.keySet()){
				blockList.add(blocks.get(blockCode));
			}
		}
		driver.delete();
		System.out.println("数据集关闭！"); 
		System.out.println("end readTab,filePath="+filePath);
		return blockList;
		}
	
	public static String createBlockGridTabFile(List<Block> dataList) {
//		String strVectorFile ="F:\\gdal_file\\123.TAB";    
		String strVectorFile ="F:\\gdal_file\\56722.shp";    
        
        // 注册所有的驱动  
        ogr.RegisterAll();  
        
        // 为了支持中文路径，请添加下面这句代码  
        gdal.SetConfigOption("GDAL_FILENAME_IS_UTF8","NO");  
        // 为了使属性表字段支持中文，请添加下面这句  
        System.out.println(" encode : "+ gdal.GetConfigOption("SHAPE_ENCODING"));
        gdal.SetConfigOption("SHAPE_ENCODING","CP936");  
        System.out.println(" encode2 : "+ gdal.GetConfigOption("SHAPE_ENCODING"));
  
       
        //创建数据，这里以创建ESRI的shp文件为例  
//        String strDriverName = "MapInfo File";//"ESRI Shapefile";  
        String strDriverName = "ESRI Shapefile";//"ESRI Shapefile";  
//        org.gdal.ogr.Driver oDriver =ogr.GetDriverByName(strDriverName);  
        org.gdal.ogr.Driver oDriver =ogr.GetDriverByName(strDriverName);  
        if (oDriver == null)  
        {  
                  System.out.println(strVectorFile+ " 驱动不可用！\n");  
                  return null;  
        }  
  
        // 创建数据源  
        DataSource oDS = oDriver.CreateDataSource(strVectorFile,null);  
        if (oDS == null)  
        {  
                  System.out.println("创建矢量文件【"+ strVectorFile +"】失败！\n" );  
                  return null;  
        }  
  
        // 创建图层，创建一个多边形图层，这里没有指定空间参考，如果需要的话，需要在这里进行指定  
        Layer oLayer =oDS.CreateLayer("001", null, ogr.wkbPolygon, null); 
        if (oLayer == null)  
        {  
                  System.out.println("图层创建失败！\n");  
                  return null;  
        }  
  
        // 下面创建属性表  
        // 先创建一个叫属性  
        FieldDefn oFieldName = new FieldDefn("Name", ogr.OFTString); 
        oFieldName.SetWidth(100);
        oLayer.CreateField(oFieldName, 1);  
  
        // 再创建一个叫oFieldProvince的字符型属性，字符长度为50  
        FieldDefn oFieldProvince = new FieldDefn("province", ogr.OFTString);  
        oFieldName.SetWidth(100);  
        oLayer.CreateField(oFieldProvince, 1);  
        
        // 再创建一个叫oFieldCity的字符型属性，字符长度为50  
        FieldDefn oFieldCity = new FieldDefn("city", ogr.OFTString);  
        oFieldName.SetWidth(100);  
        oLayer.CreateField(oFieldCity, 1); 
        
        // 再创建一个叫oFieldCounty的字符型属性，字符长度为50  
        FieldDefn oFieldCounty = new FieldDefn("Country", ogr.OFTString);  
        oFieldName.SetWidth(100);  
        oLayer.CreateField(oFieldCounty, 1); 
        
        // 再创建一个叫oFieldNumber的字符型属性
        FieldDefn oFieldNumber = new FieldDefn("Number", ogr.OFTString);  
        oFieldName.SetWidth(100);
        oLayer.CreateField(oFieldNumber, 1); 
        
        // 再创建一个叫oFieldMemo的字符型属性，字符长度为500  
        FieldDefn oFieldBlockCode = new FieldDefn("BlockCode", ogr.OFTString);  
        oFieldName.SetWidth(1000);  
        oLayer.CreateField(oFieldBlockCode, 1); 
        
        // 再创建一个叫oFieldJob1的字符型属性，字符长度为50  
        /*FieldDefn oFieldJob1 = new FieldDefn("Job1", ogr.OFTString);  
        oFieldName.SetWidth(100);  
        oLayer.CreateField(oFieldJob1, 1); */
  

        // 再创建一个叫oFieldNumber的字符型属性，字符长度为50  
        /*FieldDefn oFieldMeshId = new FieldDefn("MESH_ID", ogr.OFTString);  
        oFieldName.SetWidth(100); 
        oLayer.CreateField(oFieldMeshId, 1);*/    
   
        System.out.println("oLayer.GetRefCount() : "+oLayer.GetRefCount());
        FeatureDefn oDefn =oLayer.GetLayerDefn();  
        // 创建要素  
        if(dataList != null  && dataList.size()>0){
//        	 for(Map<String, Object> mapColumn : dataList){
        	for(Block blockObj : dataList){
        		 String wktStr = null;
        		 for(String gridId : blockObj.getGridIds()){
        			 Feature oFeature = new Feature(oDefn);  
         	        oFeature.SetField(0, blockObj.getName());  
         	        oFeature.SetField(1, blockObj.getProvince());  
         	        oFeature.SetField(2, blockObj.getCity());
         	        oFeature.SetField(3, blockObj.getCounty());
         	        oFeature.SetField(4, blockObj.getNumber());
         	        oFeature.SetField(5, gridId);
         	        
	         	    if(gridId != null && StringUtils.isNotEmpty(gridId)){
	       	        	wktStr = GridUtils.grid2Wkt(gridId); 
	       	        	System.out.println("gridId: "+gridId+" wktStr: "+wktStr);
	       	        }
	       	        
	       	        if(wktStr != null && StringUtils.isNotEmpty(wktStr)){
	       	        	org.gdal.ogr.Geometry geomTriangle =org.gdal.ogr.Geometry.CreateFromWkt(wktStr);  
	       	        	oFeature.SetGeometry(geomTriangle);
	       	        	oLayer.CreateFeature(oFeature); 
	       	        }
        		 }
             }
        }    
          
        //写入文件  
        oLayer.SyncToDisk();  
        oDS.SyncToDisk();  
  
        System.out.println("\n数据集创建完成！\n");  
        return null;
	}    
	
	
	
	public static void main(String[] args) throws Exception{
		System.out.println("start"); 
		/*String filePathString="D:/temp/block_tab2json/bj.TAB";
		List<String> columnNameList=new ArrayList<String>();
		LoadAndCreateTab.readTab(filePathString,columnNameList);*/
		createTabFile(null);
		//createShpFile();
		
		System.out.println("end"); 
	}
	

}
