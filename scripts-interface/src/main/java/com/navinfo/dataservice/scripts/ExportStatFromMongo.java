package com.navinfo.dataservice.scripts;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.bson.Document;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.ExportExcel;
import com.navinfo.dataservice.engine.statics.tools.MongoDao;
import com.navinfo.navicommons.exception.ServiceException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 导出统计结果
 * @ClassName ExportStatFromMongo
 * @author Han Shaoming
 * @date 2017年8月21日 上午10:48:31
 * @Description TODO
 */
public class ExportStatFromMongo {

	public static void initContext() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo-app-scripts.xml", "dubbo-scripts.xml" });
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
	
	public static void execute(String path,String collectionName,String timestamp) throws Exception {
		try {
			String fileName = "统计表_"+collectionName ;
			
			//设置编码,解决乱码问题
			System.out.println("old fileName: "+fileName);
			String encoding = System.getProperty("file.encoding");
			System.out.println("encoding : "+encoding);
			fileName = new String(fileName.getBytes("UTF-8"),encoding);
			System.out.println("fileName: "+fileName);
			//查询mongo库
			List<Map<String, Object>> statData = getStatData(timestamp,collectionName);
			//导出统计数据到Excel
			exportExcelPoi(path,fileName,statData);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void exportExcelPoi(String path, String fileName, List<Map<String, Object>> statData) throws Exception {
		if(statData.size() == 0){
			System.out.println("没有要写入的数据");
			return;
		}
		ExportExcel ex = new ExportExcel();
		//获取标题
		Map<String, Object> headerMap = statData.get(0);
		Set<String> set = headerMap.keySet();
		String[] headers = set.toArray(new String [set.size()]);

		OutputStream out = null ;
		try {
			File file = new File(path+"/stat/");
			if(!file.exists()){
				file.mkdir();
			}
			out = new FileOutputStream(file.getCanonicalPath()+"/" + fileName + ".xls");
			HSSFWorkbook workbook = new HSSFWorkbook();
			Map<String, Integer> colorMap = new HashMap<>();
			colorMap.put("red", 255);
			colorMap.put("green", 0);
			colorMap.put("blue", 255);
			//处理写入的数据
			List<List<Object>> statList = new ArrayList<List<Object>>();
			for (Map<String, Object> dataMap : statData) {
				List<Object> list = new ArrayList<Object>();
				for(String key : headers){
					if(dataMap.containsKey(key)){
						Object value = dataMap.get(key);
						list.add(value);
					}else{
						list.add("");
					}
				}
				statList.add(list);
			}
			ex.createSheetForList("fm_stat_data", workbook, headers, statList, out, null, colorMap, null, null);

			workbook.write(out);
			out.close();

			System.out.println("统计数据excel导出成功！");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			if(out != null){
				out.close();
			}
		}
	}

	/**
	 * 查询mongo中统计数据
	 * @throws ServiceException 
	 */
	@SuppressWarnings("unchecked")
	public static List<Map<String,Object>> getStatData(String timestamp,String collectionName) throws Exception{
		String dbName = "fm_stat";
		try {
			//获取上一次的统计时间
			MongoDao mongoDao = new MongoDao(dbName);
			BasicDBObject filter = new BasicDBObject();
			if(StringUtils.isNotEmpty(timestamp)){
				filter.put("timestamp", timestamp);
			}
			FindIterable<Document> findIterable = mongoDao.find(collectionName, filter);
			MongoCursor<Document> iterator = findIterable.iterator();
			List<Map<String,Object>> stat = new ArrayList<Map<String,Object>>();
			//处理数据
			while(iterator.hasNext()){
				//获取统计数据
				JSONObject json = JSONObject.fromObject(iterator.next());
				Map<String,Object> mapData = json;
				mapData.remove("_id");
				stat.add(mapData);
//				if(json.containsKey("content")){
//					JSONArray content = json.getJSONArray("content");
//					for(int i=0;i<content.size();i++){
//						JSONObject jso = content.getJSONObject(i);
//						Map<String,Object> map = jso;
//						map.put("timestamp", mapData.get("timestamp"));
//						stat.add(map);
//					}
//				}
			}
			return stat;
		} catch (Exception e) {
			throw new Exception("查询mongo中统计数据报错"+e.getMessage(),e);
		}
	}
	
	public static void main(String[] args) throws Exception {
		initContext();
		System.out.println("args.length:" + args.length);
		if (args == null || args.length != 3) {
			System.out.println("ERROR:need args:路径,表名,时间");
			return;
		}
		//0-路径,1-表名,2-时间(没有timestamp的字段赋值"")
		execute(args[0],args[1],args[2]);
//		execute("D:/temp","task","20170820160000");
		System.out.println("Over.");
		System.exit(0);
	}
}
