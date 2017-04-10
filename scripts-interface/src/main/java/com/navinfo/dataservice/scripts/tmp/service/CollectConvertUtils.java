package com.navinfo.dataservice.scripts.tmp.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.apache.solr.common.StringUtils;

import net.sf.json.JSONNull;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;

public class CollectConvertUtils {
	/**
	 * 输入的zip包路径排序list
	 * 输入：incremental.zip解压后的路径，例如：data/incremental
	 * 输出：List<String> pathList，例如：data/incremental/20161114/10047/IncrementalData_3565_10047_20161114203047/
	 * 原则：
	 * 1.incremental.zip解压
	 * 2.IncrementalData_开头的文件夹IncrementalData_3565_10047_20161114203047，
	 * 以_分割数组后，按照数组的最后一位排序，时间靠前的在txt的最上面。
	 * @param inPath
	 * @return
	 */
	public static List<String> listPath(String inPath) {
		return null;
	}
	/**
	 * 读取模块
	 * 文件：com.navinfo.dataservice.scripts.tmp.service.CollectConvertUtils.java
	 * Static List<JSONObject> readJsonFile(String jsonFilePath)
	 * 实现方式：java
	 * 输入：json文件路径，例如：/data/IncrementalData_3565_10047_20161114203047/Datum_Point.json
	 * 输出：List< JSONObject>  文件中的记录
	 * 原则：
	 * 1.读取json文件数据
	 * @param jsonFilePath
	 * @return
	 */
	public static List<JSONObject> readJsonObjects(String jsonFilePath){
		Scanner lines = null;
		List<JSONObject> datas = new ArrayList<JSONObject>();
		try {
			if(StringUtils.isEmpty(jsonFilePath)){
				throw new Exception("路径为:"+jsonFilePath+",文件目录不存在");
			}
			File file = new File(jsonFilePath);
			if(!file.exists()){
				throw new Exception("路径为:"+jsonFilePath+",文件目录不存在");
			}
			//判断文件类型
			if(file.isFile()&&file.getName().equals("Datum_Point.json")){
				//获取转换json
				lines = new Scanner(new FileInputStream(file));
				while(lines.hasNextLine()){
					String line = lines.nextLine();
					//处理字段值为"null"字符串
					JsonConfig jsonConfig = new JsonConfig();
					jsonConfig.registerJsonValueProcessor(String.class, new JsonValueProcessor() {
						
						@Override
						public Object processObjectValue(String paramString, Object paramObject, JsonConfig paramJsonConfig) {
							// TODO Auto-generated method stub
							if ("\"null\"".equals(paramObject)) {
								paramObject = JSONNull.getInstance();
							}
							return paramObject;
						}
						
						@Override
						public Object processArrayValue(Object paramObject, JsonConfig paramJsonConfig) {
							// TODO Auto-generated method stub
							return null;
						}
					});
					JSONObject jsonObject = JSONObject.fromObject(line,jsonConfig);
					datas.add(jsonObject);
				}
			}
		} catch(Exception e){
			e.printStackTrace();
		}finally{
			if(lines!=null){
				lines.close();
			}
		}
		return datas;
	}
	/**
	 * 写入模块
	 * 文件：com.navinfo.dataservice.scripts.tmp.service.CollectConvertUtils.java
	 * Static void writeTxtFile(String txtPath,List<JSONObject> data)
	 * 实现方式：java
	 * 输入：
	 *     List<JSONObject>数据
	 *     String :txt文件路径，例如/data/resources/upload/12/poi.txt
	 * 输出：无 
	 * 应用场景：poi数据写入poi.txt;转换后文件夹列表写入outConvert.txt
	 * 原则：
	 * 1.	记录写入到txtPath文件中
	 * @param txtPath
	 * @param newListJson
	 */
	public static void writeJSONObject2TxtFile(String txtPath,List<JSONObject> newListJson) {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(txtPath);
			for (JSONObject jso : newListJson) {
				pw.println(jso.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(pw!=null){
				pw.close();
			}
		}
	}
	/**
	 * 写入模块
	 * 文件：com.navinfo.dataservice.scripts.tmp.service.CollectConvertUtils.java
	 * Static void writeTxtFile(String txtPath,List<JSONObject> data)
	 * 实现方式：java
	 * 输入：
	 *     List<JSONObject>数据
	 *     String :txt文件路径，例如/data/resources/upload/12/poi.txt
	 * 输出：无 
	 * 应用场景：poi数据写入poi.txt;转换后文件夹列表写入outConvert.txt
	 * 原则：
	 * 1.	记录写入到txtPath文件中
	 * @param txtPath
	 * @param newListJson
	 */
	public static void writeInteger2TxtFile(String txtPath,List<Integer> newListJson) {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(txtPath);
			for (Integer seq : newListJson) {
				pw.println(String.valueOf(seq));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(pw!=null){
				pw.close();
			}
		}
	}
	/**
	 * 照片拷贝模块
	 * 文件：com.navinfo.dataservice.scripts.tmp.service.CollectConvertUtils.java
	 * Static void copyPhoto(String outPath,String inPath)
	 * 实现方式：java
	 * 输入：
	 * String outPath 照片当前路径
	 * String inPath 照片拷贝后的路径，即/data/resource/upload/序列号
	 * 输出：无 
	 * 原则：outPath中的所有后缀为jpg的照片均拷贝到路径inPath下
	 * @param outPath
	 * @param inPath
	 */
	public static void copyPhoto(String outPath,String inPath) {
		
	}
	/**
	 * 照片重命名模块
	 * 文件：com.navinfo.dataservice.scripts.tmp.service.CollectConvertUtils.java
	 * Static void reNamePhoto(String photoPath,String newName)
	 * 实现方式：java
	 * 输入：
	 * String photoPath 照片路径，即/data/resource/upload/序列号/123.jpg
	 * 输出：无 
	 * 原则：photoPath对应的照片重命名
	 * @param photoPath
	 * @param newName
	 */
	public static void reNamePhoto(String photoPath,String newName) {
		
	}
	/**
	 * 获取序列号模块
	 * 文件：com.navinfo.dataservice.scripts.tmp.service.CollectConvertUtils.java
	 * Static int getUploadSeq()
	 * 实现方式：java
	 * 输入：无
	 * 输出：int序列号
	 * 原则：
	 * 1.	登陆sys库获取select seq_upload.nextval from dual
	 * @return
	 */
	public static int getUploadSeq() {
		return 0;
	}
	/**
	 * geo获取dbid模块
	 * 文件：com.navinfo.dataservice.scripts.tmp. service.CollectConvertUtils.java
	 * Static int getDbIdByGeo(String geoStr)
	 * 实现方式：java
	 * 输入：String geoStr 一体化规格geometry "geometry":"POINT (120.17332 32.35849)"
	 * 输出：int dbId
	 * 原则：
	 * 1.	通过geoStr计算gridId
	 * 2.	通过gridId计算dbid。登陆man库， SELECT daily_db_id FROM grid g,region r 
	 * WHERE g.region_id=r.region_id and grid_id=:1
	 * 3.	参照方法
	 *    UploadOperationByGather.java:calDbDataMapping
	 * @param geoWkt
	 * @return
	 */
	public static int getDbidByGeo(String geoWkt) {
		return 0;
	}
	/**
	 * "null"转成""；否则，直接赋值
	 * @param oldStr
	 * @return
	 */
	public static String convertStr(String oldStr){
		if(oldStr.equals("null")){
			return "";
		}else{return oldStr;}
	}
	
	/**
	 * 四舍五入，保留scale个小数，例如，scale=2则返回值为 ***.00(小数点后保留2位)
	 * @param old
	 * @param scale
	 * @return
	 */
	public static double convertDouble(double old,int scale){
		BigDecimal   b   =   new   BigDecimal(old);  
		double   f1   =   b.setScale(scale,   BigDecimal.ROUND_HALF_UP).doubleValue();
		return f1;
	}
	
	/**
	 * 创建导入路径
	 * false:存在当前转换路径,true:没有路径,创建成功
	 * @param inPath
	 * @return
	 */
	public static boolean createMkdir(String inPath){
		if(inPath != null){
			File dirFile = new File(inPath);
			boolean mkdirs = dirFile.mkdirs();
			return mkdirs;
		}
		return false;
	}
}
