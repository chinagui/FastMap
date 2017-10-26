package com.navinfo.dataservice.scripts;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.bson.Document;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.ExportExcel;
import com.navinfo.dataservice.engine.statics.tools.MongoDao;
import com.navinfo.navicommons.exception.ServiceException;

import net.sf.json.JSONObject;

/**
 * 导出统计结果
 * @ClassName ExportStatFromMongo
 * @author Han Shaoming
 * @date 2017年8月21日 上午10:48:31
 * @Description TODO
 */
public class ExportStatFromMongo {
	private static Logger log = LogManager.getLogger(ExportStatFromMongo.class);

	public static void initContext() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo-app-scripts.xml", "dubbo-scripts.xml" });
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
	
	public static void execute(String path,String collectionName,String startTime,String endTime) throws Exception {
		try {
			String fileName = "统计表_"+collectionName ;
			
			//设置编码,解决乱码问题
			System.out.println("old fileName: "+fileName);
			String encoding = System.getProperty("file.encoding");
			System.out.println("encoding : "+encoding);
			fileName = new String(fileName.getBytes("UTF-8"),encoding);
			System.out.println("fileName: "+fileName);
			//查询mongo库
			List<Map<String, Object>> statData = getStatData(startTime,endTime,collectionName);
			log.info("export stat num:"+statData.size());
			//导出统计数据到Excel
			//若数据量超过,则分sheet页放
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
			int i=1;
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
				if(statList.size()>65530){
					ex.createSheetForList("fm_stat_data_"+i, workbook, headers, statList, out, null, colorMap, null, null);
					i++;
					statList = new ArrayList<List<Object>>();
				}
				statList.add(list);
			}
			//若数据量超过最大量65536，则拆分不同sheet
			if(statList.size()!=0){
				ex.createSheetForList("fm_stat_data_"+i, workbook, headers, statList, out, null, colorMap, null, null);
			}

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
	 * startTime,endTime:只用于person表,其余表导出最新的数据(循环到查到数据)
	 * startTime:为0,则所有的
	 * endTime:为0,则当前系统时间
	 * @param collectionName2 
	 * @throws ServiceException 
	 */
	@SuppressWarnings("unchecked")
	public static List<Map<String,Object>> getStatData(String startTime,String endTime,String collectionName) throws Exception{
		String dbName = SystemConfigFactory.getSystemConfig().getValue(PropConstant.fmStat);
		System.out.println(dbName);
		try {
			//处理时间
			String timestamp=DateUtils.dateToString(DateUtils.getSysdate(), "yyyyMMddHH0000");
			log.info("timestamp:"+timestamp);
			//获取上一次的统计时间
			MongoDao mongoDao = new MongoDao(dbName);
			List<Map<String,Object>> stat = new ArrayList<Map<String,Object>>();
			if("task_day_plan".equals(collectionName)){
				BasicDBObject filter = new BasicDBObject();
				FindIterable<Document> findIterable = mongoDao.find(collectionName, filter);
				MongoCursor<Document> iterator = findIterable.iterator();
				//处理数据
				while(iterator.hasNext()){
					//获取统计数据
					JSONObject json = JSONObject.fromObject(iterator.next());
					Map<String,Object> mapData = json;
					mapData.remove("_id");
					stat.add(mapData);
				}
			}else if("person".equals(collectionName)){
				String lastTimestamp = "0";
				if(StringUtils.isNotEmpty(startTime) && !"0".equals(startTime)){
					lastTimestamp = startTime.substring(0, 8);
				}else{
					FindIterable<Document> findIterable = mongoDao.find(collectionName, null).projection(new Document("_id",0)).sort(new BasicDBObject("workDay",1));
					MongoCursor<Document> iterator = findIterable.iterator();
					//处理数据
					while(iterator.hasNext()){
						//获取统计数据
						JSONObject jso = JSONObject.fromObject(iterator.next());
						lastTimestamp=String.valueOf(jso.get("workDay"));
						if(StringUtils.isNotEmpty(lastTimestamp)&&!lastTimestamp.equals("0")){
							break;
						}
					}
				}
				if(StringUtils.isNotEmpty(endTime) && !"0".equals(endTime)){
					timestamp = endTime.substring(0, 8);
				}else{
					FindIterable<Document> findIterable = mongoDao.find(collectionName, null).projection(new Document("_id",0)).sort(new BasicDBObject("workDay",-1));
					MongoCursor<Document> iterator = findIterable.iterator();
					//处理数据
					while(iterator.hasNext()){
						//获取统计数据
						JSONObject jso = JSONObject.fromObject(iterator.next());
						timestamp=String.valueOf(jso.get("workDay"));
						if(StringUtils.isNotEmpty(lastTimestamp)&&!lastTimestamp.equals("0")){
							break;
						}
					}
				}
				
				Timestamp start = DateUtils.stringToTimestamp(lastTimestamp, DateUtils.DATE_YMD);
				Timestamp end = DateUtils.stringToTimestamp(timestamp, DateUtils.DATE_YMD);
				Date startDate = DateUtils.stringToDate(lastTimestamp, DateUtils.DATE_YMD);
				long days=DateUtils.diffDay(start, end);
				System.out.println("date:"+lastTimestamp+"--"+timestamp+";diffDay:"+days); 
				for(int i=0;i<=days;i++){
					String workDay=DateUtils.dateToString(DateUtils.addDay(startDate, i),  DateUtils.DATE_YMD);
					BasicDBObject filter = new BasicDBObject("workDay", workDay);
					FindIterable<Document> findIterable = mongoDao.find(collectionName, filter).projection(new Document("_id",0)).sort(new BasicDBObject("timestamp",-1));
					MongoCursor<Document> iterator = findIterable.iterator();
					String timestampLast="";
					//处理数据
					while(iterator.hasNext()){
						//获取统计数据
						JSONObject jso = JSONObject.fromObject(iterator.next());
						String timestampOrigin=String.valueOf(jso.get("timestamp"));
						if(StringUtils.isEmpty(timestampLast)){
							timestampLast=timestampOrigin;
							System.out.println(workDay+"最近一次的统计日期为："+timestampLast);
						}
						if(!timestampLast.equals(timestampOrigin)){
							break;
						}
						stat.add(jso);
					}
				}
			}else if("task".equals(collectionName)){//查询最新数据
				//中线
				BasicDBObject filter = new BasicDBObject("programType", 1);
				String lastTime = timestamp;
				FindIterable<Document> findIterable = mongoDao.find(collectionName, filter).projection(new Document("_id",0)).sort(new BasicDBObject("timestamp",-1));
				MongoCursor<Document> iterator = findIterable.iterator();
				//最近一次的统计时间戳
				while(iterator.hasNext()){
					//获取统计数据
					JSONObject jso = JSONObject.fromObject(iterator.next());
					lastTime=String.valueOf(jso.get("timestamp"));
				}
				log.info("中线最新的统计日期："+lastTime);
				filter = new BasicDBObject();
				filter.append("timestamp", lastTime);
				findIterable = mongoDao.find(collectionName, filter);
				iterator = findIterable.iterator();
				//处理数据
				while(iterator.hasNext()){
					//获取统计数据
					JSONObject json = JSONObject.fromObject(iterator.next());
					Map<String,Object> mapData = json;
					mapData.remove("_id");
					stat.add(mapData);
				}
				
				//快线
				filter = new BasicDBObject("programType",4);
				lastTime = timestamp;
				findIterable = mongoDao.find(collectionName, filter).projection(new Document("_id",0)).sort(new BasicDBObject("timestamp",-1));
				iterator = findIterable.iterator();
				//最近一次的统计时间戳
				while(iterator.hasNext()){
					//获取统计数据
					JSONObject jso = JSONObject.fromObject(iterator.next());
					lastTime=String.valueOf(jso.get("timestamp"));
				}
				log.info("快线最新的统计日期："+lastTime);
				filter = new BasicDBObject();
				filter.append("timestamp", lastTime);
				findIterable = mongoDao.find(collectionName, filter);
				iterator = findIterable.iterator();
				//处理数据
				while(iterator.hasNext()){
					//获取统计数据
					JSONObject json = JSONObject.fromObject(iterator.next());
					Map<String,Object> mapData = json;
					mapData.remove("_id");
					stat.add(mapData);
				}
			}else{
				//查询最新的数据
				if("0".equals(startTime) && "0".equals(endTime)){
					String lastTime = timestamp;
					FindIterable<Document> findIterable = mongoDao.find(collectionName, null).projection(new Document("_id",0)).sort(new BasicDBObject("timestamp",-1));
					MongoCursor<Document> iterator = findIterable.iterator();
					//最近一次的统计时间戳
					while(iterator.hasNext()){
						//获取统计数据
						JSONObject jso = JSONObject.fromObject(iterator.next());
						lastTime=String.valueOf(jso.get("timestamp"));
					}
					log.info("最新的统计日期："+lastTime);
					BasicDBObject filter = new BasicDBObject();
					filter.append("timestamp", lastTime);
					findIterable = mongoDao.find(collectionName, filter);
					iterator = findIterable.iterator();
					//处理数据
					while(iterator.hasNext()){
						//获取统计数据
						JSONObject json = JSONObject.fromObject(iterator.next());
						Map<String,Object> mapData = json;
						mapData.remove("_id");
						stat.add(mapData);
					}
				}
				//查询时间段内的数据
				else{
					String lastTimestamp = "0";
					if(StringUtils.isNotEmpty(startTime)){
						lastTimestamp = startTime;
					}
					if(StringUtils.isNotEmpty(endTime) && !"0".equals(endTime)){
						timestamp = endTime;
					}
					BasicDBList valueAnd = new BasicDBList();
					valueAnd.add(new BasicDBObject("timestamp", new BasicDBObject("$gte", lastTimestamp)));
					valueAnd.add(new BasicDBObject("timestamp", new BasicDBObject("$lte", timestamp)));
					BasicDBObject filter = new BasicDBObject();
					filter.put("$and", valueAnd);
					
					FindIterable<Document> findIterable = mongoDao.find(collectionName, filter);
					MongoCursor<Document> iterator = findIterable.iterator();
					//处理数据
					while(iterator.hasNext()){
						//获取统计数据
						JSONObject json = JSONObject.fromObject(iterator.next());
						Map<String,Object> mapData = json;
						mapData.remove("_id");
						stat.add(mapData);
					}
				}
			}
			return stat;
		} catch (Exception e) {
			throw new Exception("查询mongo中统计数据报错"+e.getMessage(),e);
		}
	}
	
	public static void main(String[] args) throws Exception {
		initContext();
		System.out.println("args.length:" + args.length);
		if (args == null || args.length != 4) {
			System.out.println("ERROR:need args:路径,表名,开始时间,结束时间");
			return;
		}
		//0-路径,1-表名,2-开始时间(没有startTime的字段赋值"0"),3-结束时间(没有endTime的字段赋值"0")
		execute(args[0],args[1],args[2],args[3]);
		//execute("D:/temp","block_notask ","20170801000000","20171001000000");
		System.out.println("Over.");
		System.exit(0);
	}
}
