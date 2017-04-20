<<<<<<< HEAD
package com.navinfo.dataservice.scripts.tmp.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.apache.solr.common.StringUtils;

import java.util.Map;

import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;

import net.sf.json.JSONNull;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.JtsGeometryFactory;
import com.navinfo.dataservice.scripts.JobScriptsInterface;
import com.navinfo.dataservice.scripts.tmp.CollectConvert;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;
import com.navinfo.navicommons.geo.computation.CompGridUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;

public class CollectConvertUtils {
	private static Logger log = LoggerRepos.getLogger(CollectConvertUtils.class);

	/**
	 * 输入的zip包路径排序list
	 * 输入：incremental.zip解压后的路径，例如：data/incremental
	 * 输出：List<String> pathList，例如：data/incremental/20161114/10047/IncrementalData_3565_10047_20161114203047/
	 * 原则：
	 * 1.incremental.zip解压--此步骤删除，默认已经解压好
	 * 2.IncrementalData_开头的文件夹IncrementalData_3565_10047_20161114203047，
	 * 以_分割数组后，按照数组的最后一位排序，时间靠前的在txt的最上面。
	 * @param inPath
	 * @return
	 */
	public static List<String> listPath(String inPath) {
		File file = new File(inPath);
		List<String> pathList = new ArrayList<String>();
		List<String> resultPathList = new ArrayList<String>();
		getDirectory(file,pathList);
		Map<String,String> map = new HashMap<String,String>();
		List<String> keys = new ArrayList<String>();
		for(String pathStr:pathList){
			Path path = Paths.get(pathStr);

			String fileName = path.getFileName().toString();
			String[] cells = fileName.split("_");
			String cell = cells[cells.length-1];
			keys.add(cell);
			map.put(cell, pathStr);
		}
		
		Collections.sort(keys);
		for(String key:keys){
			resultPathList.add(map.get(key));
		}
		return resultPathList;
	}
	
	public static void getDirectory(File file, List<String> list){
		File flist[] = file.listFiles();
		if (flist == null || flist.length == 0) {
		    return;
		}
		for (File f : flist) {
		    if (f.isDirectory()) {
		        for(File fileInner:f.listFiles()){
		        	if(fileInner.getAbsoluteFile().toString().contains(".json")){
		        		list.add(f.getAbsolutePath());
		        		log.info("Dir==>" + f.getAbsolutePath());
		        		break;
		        	}
		        }
		        getDirectory(f,list);
		    } else {
		    }
		}
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
	public static List<JSONObject> readJsonObjects(String jsonFilePath) {
		Scanner lines = null;
		List<JSONObject> datas = new ArrayList<JSONObject>();
		try {
			if(StringUtils.isEmpty(jsonFilePath)){
				log.info("路径为:"+jsonFilePath+",文件目录不存在");
			}
			File file = new File(jsonFilePath);
			if(!file.exists()){
				log.info("路径为:"+jsonFilePath+",文件目录不存在");
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
			log.error(e.getMessage(), e);
		}finally{
			try {
				if(lines!=null){
					lines.close();
				}
			} catch (Exception e2) {
				log.error(e2.getMessage(), e2);
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
			try {
				if(pw!=null){
					pw.close();
				}
			} catch (Exception e2) {
				log.error(e2.getMessage(), e2);
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
			try {
				if(pw!=null){
					pw.close();
				}
			} catch (Exception e2) {
				log.error(e2.getMessage(), e2);
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
	public static void writeStringTxtFile(String txtPath,List<String> newListJson) {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(txtPath);
			for (String seq : newListJson) {
				pw.println(seq);
	}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			try {
				if(pw!=null){
					pw.close();
				}
			} catch (Exception e2) {
				log.error(e2.getMessage(), e2);
			}
		}
	}
	/**
	 * 照片拷贝模块
	 * 文件：com.navinfo.dataservice.scripts.tmp.service.CollectConvertUtils.java
	 * Static void copyPhoto(String outPath,String inPath)
	 * 实现方式：java
	 * 输入：
	 * String outPathStr 照片当前路径
	 * String inPathStr 照片拷贝后的路径，即/data/resource/upload/序列号
	 * 输出：无 
	 * 原则：outPathStr中的所有后缀为jpg的照片均拷贝到路径inPath下
	 * @param outPathStr
	 * @param inPathStr
	 * @throws IOException 
	 */
	public static void copyPhoto(String outPathStr,String inPathStr) throws IOException {
		File file = new File(outPathStr);
		File flist[] = file.listFiles();
		if (flist == null || flist.length == 0) {
		    return;
		}
		for (File srcFile : flist) {
		    if (srcFile.isDirectory()) {
        		log.info("Dir==>" + srcFile.getAbsolutePath());
		    	copyPhoto(srcFile.getAbsolutePath(),inPathStr);
		    } else {
		    	if(srcFile.getAbsoluteFile().toString().contains(".jpg")){
	        		log.info("photo==>" + srcFile.getAbsolutePath());
		    		Path outPath = Paths.get(srcFile.getAbsolutePath());
		    		String photoName = outPath.getFileName().toString();
		    		
		    		Path inPath = Paths.get(inPathStr,photoName);
		    		File destFile = new File(inPath.toString());
		    		 // 判断源文件是否存在  
		            if (!srcFile.exists()) { 
		            	log.info("photo:" + outPath + " not exists");
		            } else if (!srcFile.isFile()) {  
		            	log.info("photo:" + outPath + " is not a file"); 
		            }
		            
		            // 复制文件  
		            int byteread = 0; // 读取的字节数  
		            InputStream in = null;  
		            OutputStream out = null;  
		      
		            try {  
		                in = new FileInputStream(srcFile);  
		                out = new FileOutputStream(destFile);  
		                byte[] buffer = new byte[1024];  
		      
		                while ((byteread = in.read(buffer)) != -1) {  
		                    out.write(buffer, 0, byteread);  
		                }  
		            } catch (FileNotFoundException e) {  
		                log.info("file not exists");
		            } catch (IOException e) {  
		            	throw  e;  
		            } finally {  
		                try {  
		                    if (out != null)  
		                        out.close();  
		                    if (in != null)  
		                        in.close();  
		                } catch (IOException e) {  
		                	throw e; 
		                }  
		            }
	        	}
		    }
		}
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
		Path filePath = Paths.get(photoPath);
		Path path = filePath.getParent();
		String oldName = filePath.getFileName().toString();
		if(oldName.equals(newName)){
			log.info("oldName equals newName");
			return;
		}
		File oldfile = new File(photoPath);
		Path newfilePath = Paths.get(path.toString(),newName);
		File newfile = new File(newfilePath.toString()); 
        if(!oldfile.exists()){
        	return;//重命名文件不存在
       	}
       	if(newfile.exists()){
       		//若在该目录下已经有一个文件和新文件名相同，则不允许重命名 
			log.info("newName already exists");
       	}
       	else{ 
       		oldfile.renameTo(newfile); 
       	} 
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
	 * @throws ServiceException 
	 */
	public static int getUploadSeq() throws ServiceException {
		Connection conn = null;
		try {
			QueryRunner queryRunner = new QueryRunner();
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			String sql = "select seq_upload.nextval from dual ";

			ResultSetHandler<Integer> rsh = new ResultSetHandler<Integer>() {
				@Override
				public Integer handle(ResultSet rs) throws SQLException {
					if (rs.next()) {
						int jobId = rs.getInt("nextval");
						return jobId;
					}
					return 0;
				}
			};
			return queryRunner.query(conn, sql, rsh);

		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("failed to get upload sequence:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
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
	 * @throws ParseException 
	 * @throws ServiceException 
	 */
	public static int getDbidByGeo(String geoWkt) throws ParseException, ServiceException {
		Connection conn = null;
		try {
			QueryRunner queryRunner = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();

			Geometry geo = JtsGeometryFactory.read(geoWkt);
			
			//通过 geo 获取 grid 
			Coordinate[] coordinate = geo.getCoordinates();
			CompGridUtil gridUtil = new CompGridUtil();
			

			long gridId = Long.parseLong(gridUtil.point2Grids(coordinate[0].x, coordinate[0].y)[0]);
			log.info(" gridId :"+gridId);
			
			String sql = "SELECT daily_db_id FROM grid g,region r WHERE g.region_id=r.region_id and grid_id=" + gridId;
			
			ResultSetHandler<Integer> rsh = new ResultSetHandler<Integer>() {
				@Override
				public Integer handle(ResultSet rs) throws SQLException {
					if (rs.next()) {
						int dbId = rs.getInt("daily_db_id");
						return dbId;
					}
					return 0;
				}
			};
			return queryRunner.query(conn, sql, rsh);

		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("failed to get DBId:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
				
	}
	/**
	 * "null"转成""；否则，直接赋值
	 * @param oldStr
	 * @return
	 */
	public static String convertStr(String oldStr){
		if(oldStr==null||oldStr.isEmpty()||oldStr.equals("null")){
			return "";
		}else{return oldStr;}
	}
	
	/**
	 * "null"转成0；否则，字符串转数字后赋值
	 * @param oldStr
	 * @return
	 */
	public static int convertInt(String oldStr){
		if(oldStr==null||oldStr.isEmpty()||oldStr.equals("null")){
			return 0;
		}else{return Integer.valueOf(oldStr);}
	}
	/**
	 * 对比字符串是否相等
	 * null,""相同。
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean compareStr(String a,String b){
		if(a==null||a.isEmpty()||a.equals("null")){
			if(b==null||b.isEmpty()||b.equals("null")){return true;}
			else{return false;}
		}else {return a.equals(b);}
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
	
	public static void main(String[] args) throws Exception {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(  
                new String[] { "dubbo-app-scripts.xml","dubbo-scripts.xml" }); 
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
//		String path = "E:\\Users\\upload";
//		List<String> resultPathList = listPath(path);
		
//		String outPathStr = "E:\\Users\\upload\\";
//		String inPathStr = "E:\\Users\\upload\\photo";
//		copyPhoto( outPathStr, inPathStr);
		
//		String photoPath = "E:\\Users\\upload\\photo\\366620161022152631.jpg";
//		String photoPath = "E:\\Users\\upload\\photo\\419020161103145434.jpg";
//		String newName = "aaa.jpg";
//		reNamePhoto( photoPath, newName);
		
//		int seq = getUploadSeq();
//		System.out.println(seq);
		
		String geo = "POINT (120.17332 32.35849)";
		int dbid = getDbidByGeo(geo);
		System.out.println(dbid);

	}

	/**
	 * 待导入的数据fid信息写入数据库
	 * @param map
	 * @throws Exception 
	 */
	public static void importConvertFids(Map<Integer, List<String>> map) throws Exception {
		for(Map.Entry<Integer, List<String>> entry:map.entrySet()){
			Connection conn = DBConnector.getInstance().getConnectionById(entry.getKey());
			try {
				QueryRunner queryRunner = new QueryRunner();

				StringBuilder sb = new StringBuilder();
				sb.append(" declare                                          ");
				sb.append("   vl_num        number(8);                       ");
				sb.append("   STRSQL_CREATE varchar2(1024);                  ");
				sb.append(" begin                                            ");
				sb.append("   select count(*)                                ");
				sb.append("     into vl_num                                  ");
				sb.append("     from user_tables                             ");
				sb.append("    where table_name = 'CONVERT_FIDS';            ");
				sb.append("   if (vl_num = 0) then                           ");
				sb.append("     strSQL_CREATE := 'create table CONVERT_FIDS  ");
				sb.append(" (                                                ");
				sb.append("   fid          VARCHAR2(36) not null,            ");
				sb.append("   program_id   NUMBER(10) not null,              ");
				sb.append("   convert_time VARCHAR2(14) not null             ");
				sb.append(" )';                                              ");
				sb.append("     EXECUTE IMMEDIATE strSQL_CREATE;             ");
				sb.append("   end if;                                        ");
				sb.append(" end;                                             ");
				
				String sql = sb.toString();
				queryRunner.update(conn, sql);
				
				QueryRunner run = new QueryRunner();

				String createMappingSql = "insert into CONVERT_FIDS (FID, PROGRAM_ID,CONVERT_TIME) VALUES (?,?,?)";
				Object[][] inParam = new Object[entry.getValue().size()][];
				int i = 0;
				for(String data:entry.getValue()){
					String[] cells = data.split("_");
					Object[] temp = new Object[3];
					temp[0] = cells[0];
					temp[1] = cells[1];
					temp[2] = cells[2];
					inParam[i] = temp;
					i++;
				}
				run.batch(conn, createMappingSql, inParam);

			} catch (Exception e) {
				DbUtils.rollbackAndCloseQuietly(conn);
				log.error(e.getMessage(), e);
				throw new ServiceException("failed to get DBId:" + e.getMessage(), e);
			} finally {
				DbUtils.commitAndCloseQuietly(conn);
			}
		}
		
	}
}
=======
package com.navinfo.dataservice.scripts.tmp.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import org.apache.solr.common.StringUtils;
import java.util.Map;

import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import net.sf.json.JSONNull;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.JtsGeometryFactory;
import com.navinfo.dataservice.scripts.JobScriptsInterface;
import com.navinfo.dataservice.scripts.tmp.CollectConvert;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;
import com.navinfo.navicommons.geo.computation.CompGridUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;

public class CollectConvertUtils {
	private static Logger log = LogManager.getLogger(CollectConvert.class);

	/**
	 * 输入的zip包路径排序list
	 * 输入：incremental.zip解压后的路径，例如：data/incremental
	 * 输出：List<String> pathList，例如：data/incremental/20161114/10047/IncrementalData_3565_10047_20161114203047/
	 * 原则：
	 * 1.incremental.zip解压--此步骤删除，默认已经解压好
	 * 2.IncrementalData_开头的文件夹IncrementalData_3565_10047_20161114203047，
	 * 以_分割数组后，按照数组的最后一位排序，时间靠前的在txt的最上面。
	 * @param inPath
	 * @return
	 */
	public static List<String> listPath(String inPath) {
		File file = new File(inPath);
		List<String> pathList = new ArrayList<String>();
		List<String> resultPathList = new ArrayList<String>();
		getDirectory(file,pathList);
		Map<String,String> map = new HashMap<String,String>();
		List<String> keys = new ArrayList<String>();
		for(String pathStr:pathList){
			Path path = Paths.get(pathStr);

			String fileName = path.getFileName().toString();
			String[] cells = fileName.split("_");
			String cell = cells[cells.length-1];
			keys.add(cell);
			map.put(cell, pathStr);
		}
		
		Collections.sort(keys);
		for(String key:keys){
			resultPathList.add(map.get(key));
		}
		return resultPathList;
	}
	
	public static void getDirectory(File file, List<String> list){
		File flist[] = file.listFiles();
		if (flist == null || flist.length == 0) {
		    return;
		}
		for (File f : flist) {
		    if (f.isDirectory()) {
		        for(File fileInner:f.listFiles()){
		        	if(fileInner.getAbsoluteFile().toString().contains(".json")){
		        		list.add(f.getAbsolutePath());
		        		log.info("Dir==>" + f.getAbsolutePath());
		        		break;
		        	}
		        }
		        getDirectory(f,list);
		    } else {
		    }
		}
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
	public static List<JSONObject> readJsonObjects(String jsonFilePath) {
		Scanner lines = null;
		List<JSONObject> datas = new ArrayList<JSONObject>();
		try {
			if(StringUtils.isEmpty(jsonFilePath)){
				log.info("路径为:"+jsonFilePath+",文件目录不存在");
			}
			File file = new File(jsonFilePath);
			if(!file.exists()){
				log.info("路径为:"+jsonFilePath+",文件目录不存在");
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
			log.error(e.getMessage(), e);
		}finally{
			try {
				if(lines!=null){
					lines.close();
				}
			} catch (Exception e2) {
				log.error(e2.getMessage(), e2);
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
			try {
				if(pw!=null){
					pw.close();
				}
			} catch (Exception e2) {
				log.error(e2.getMessage(), e2);
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
			try {
				if(pw!=null){
					pw.close();
				}
			} catch (Exception e2) {
				log.error(e2.getMessage(), e2);
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
	public static void writeStringTxtFile(String txtPath,List<String> newListJson) {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(txtPath);
			for (String seq : newListJson) {
				pw.println(seq);
	}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			try {
				if(pw!=null){
					pw.close();
				}
			} catch (Exception e2) {
				log.error(e2.getMessage(), e2);
			}
		}
	}
	/**
	 * 照片拷贝模块
	 * 文件：com.navinfo.dataservice.scripts.tmp.service.CollectConvertUtils.java
	 * Static void copyPhoto(String outPath,String inPath)
	 * 实现方式：java
	 * 输入：
	 * String outPathStr 照片当前路径
	 * String inPathStr 照片拷贝后的路径，即/data/resource/upload/序列号
	 * 输出：无 
	 * 原则：outPathStr中的所有后缀为jpg的照片均拷贝到路径inPath下
	 * @param outPathStr
	 * @param inPathStr
	 * @throws IOException 
	 */
	public static void copyPhoto(String outPathStr,String inPathStr) throws IOException {
		File file = new File(outPathStr);
		File flist[] = file.listFiles();
		if (flist == null || flist.length == 0) {
		    return;
		}
		for (File srcFile : flist) {
		    if (srcFile.isDirectory()) {
        		log.info("Dir==>" + srcFile.getAbsolutePath());
		    	copyPhoto(srcFile.getAbsolutePath(),inPathStr);
		    } else {
		    	if(srcFile.getAbsoluteFile().toString().contains(".jpg")){
	        		log.info("photo==>" + srcFile.getAbsolutePath());
		    		Path outPath = Paths.get(srcFile.getAbsolutePath());
		    		String photoName = outPath.getFileName().toString();
		    		
		    		Path inPath = Paths.get(inPathStr,photoName);
		    		File destFile = new File(inPath.toString());
		    		 // 判断源文件是否存在  
		            if (!srcFile.exists()) { 
		            	log.info("photo:" + outPath + " not exists");
		            } else if (!srcFile.isFile()) {  
		            	log.info("photo:" + outPath + " is not a file"); 
		            }
		            
		            // 复制文件  
		            int byteread = 0; // 读取的字节数  
		            InputStream in = null;  
		            OutputStream out = null;  
		      
		            try {  
		                in = new FileInputStream(srcFile);  
		                out = new FileOutputStream(destFile);  
		                byte[] buffer = new byte[1024];  
		      
		                while ((byteread = in.read(buffer)) != -1) {  
		                    out.write(buffer, 0, byteread);  
		                }  
		            } catch (FileNotFoundException e) {  
		                log.info("file not exists");
		            } catch (IOException e) {  
		            	throw  e;  
		            } finally {  
		                try {  
		                    if (out != null)  
		                        out.close();  
		                    if (in != null)  
		                        in.close();  
		                } catch (IOException e) {  
		                	throw e; 
		                }  
		            }
	        	}
		    }
		}
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
		Path filePath = Paths.get(photoPath);
		Path path = filePath.getParent();
		String oldName = filePath.getFileName().toString();
		if(oldName.equals(newName)){
			log.info("oldName equals newName");
			return;
		}
		File oldfile = new File(photoPath);
		Path newfilePath = Paths.get(path.toString(),newName);
		File newfile = new File(newfilePath.toString()); 
        if(!oldfile.exists()){
        	return;//重命名文件不存在
       	}
       	if(newfile.exists()){
       		//若在该目录下已经有一个文件和新文件名相同，则不允许重命名 
			log.info("newName already exists");
       	}
       	else{ 
       		oldfile.renameTo(newfile); 
       	} 
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
	 * @throws ServiceException 
	 */
	public static int getUploadSeq() throws ServiceException {
		Connection conn = null;
		try {
			QueryRunner queryRunner = new QueryRunner();
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			String sql = "select seq_upload.nextval from dual ";

			ResultSetHandler<Integer> rsh = new ResultSetHandler<Integer>() {
				@Override
				public Integer handle(ResultSet rs) throws SQLException {
					if (rs.next()) {
						int jobId = rs.getInt("nextval");
						return jobId;
					}
					return 0;
				}
			};
			return queryRunner.query(conn, sql, rsh);

		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("failed to get upload sequence:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
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
	 * @throws ParseException 
	 * @throws ServiceException 
	 */
	public static int getDbidByGeo(String geoWkt) throws ParseException, ServiceException {
		Connection conn = null;
		try {
			QueryRunner queryRunner = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();

			Geometry geo = JtsGeometryFactory.read(geoWkt);
			
			//通过 geo 获取 grid 
			Coordinate[] coordinate = geo.getCoordinates();
			CompGridUtil gridUtil = new CompGridUtil();
			

			long gridId = Long.parseLong(gridUtil.point2Grids(coordinate[0].x, coordinate[0].y)[0]);
			log.info(" gridId :"+gridId);
			
			String sql = "SELECT daily_db_id FROM grid g,region r WHERE g.region_id=r.region_id and grid_id=" + gridId;
			
			ResultSetHandler<Integer> rsh = new ResultSetHandler<Integer>() {
				@Override
				public Integer handle(ResultSet rs) throws SQLException {
					if (rs.next()) {
						int dbId = rs.getInt("daily_db_id");
						return dbId;
					}
					return 0;
				}
			};
			return queryRunner.query(conn, sql, rsh);

		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("failed to get DBId:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
				
	}
	/**
	 * "null"转成""；否则，直接赋值
	 * @param oldStr
	 * @return
	 */
	public static String convertStr(String oldStr){
		if(oldStr==null||oldStr.isEmpty()||oldStr.equals("null")){
			return "";
		}else{return oldStr;}
	}
	
	/**
	 * "null"转成0；否则，字符串转数字后赋值
	 * @param oldStr
	 * @return
	 */
	public static int convertInt(String oldStr){
		if(oldStr==null||oldStr.isEmpty()||oldStr.equals("null")){
			return 0;
		}else{return Integer.valueOf(oldStr);}
	}
	/**
	 * 对比字符串是否相等
	 * null,""相同。
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean compareStr(String a,String b){
		if(a==null||a.isEmpty()||a.equals("null")){
			if(b==null||b.isEmpty()||b.equals("null")){return true;}
			else{return false;}
		}else {return a.equals(b);}
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
	
	public static void main(String[] args) throws Exception {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(  
                new String[] { "dubbo-app-scripts.xml","dubbo-scripts.xml" }); 
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
//		String path = "E:\\Users\\upload";
//		List<String> resultPathList = listPath(path);
		
//		String outPathStr = "E:\\Users\\upload\\";
//		String inPathStr = "E:\\Users\\upload\\photo";
//		copyPhoto( outPathStr, inPathStr);
		
//		String photoPath = "E:\\Users\\upload\\photo\\366620161022152631.jpg";
//		String photoPath = "E:\\Users\\upload\\photo\\419020161103145434.jpg";
//		String newName = "aaa.jpg";
//		reNamePhoto( photoPath, newName);
		
//		int seq = getUploadSeq();
//		System.out.println(seq);
		
		String geo = "POINT (120.17332 32.35849)";
		int dbid = getDbidByGeo(geo);
		System.out.println(dbid);

	}

	/**
	 * 待导入的数据fid信息写入数据库
	 * @param map
	 * @throws Exception 
	 */
	public static void importConvertFids(Map<Integer, List<String>> map) throws Exception {
		for(Map.Entry<Integer, List<String>> entry:map.entrySet()){
			Connection conn = DBConnector.getInstance().getConnectionById(entry.getKey());
			try {
				QueryRunner queryRunner = new QueryRunner();

				StringBuilder sb = new StringBuilder();
				sb.append(" declare                                          ");
				sb.append("   vl_num        number(8);                       ");
				sb.append("   STRSQL_CREATE varchar2(1024);                  ");
				sb.append(" begin                                            ");
				sb.append("   select count(*)                                ");
				sb.append("     into vl_num                                  ");
				sb.append("     from user_tables                             ");
				sb.append("    where table_name = 'CONVERT_FIDS';            ");
				sb.append("   if (vl_num = 0) then                           ");
				sb.append("     strSQL_CREATE := 'create table CONVERT_FIDS  ");
				sb.append(" (                                                ");
				sb.append("   fid          VARCHAR2(36) not null,            ");
				sb.append("   program_id   NUMBER(10) not null,              ");
				sb.append("   convert_time VARCHAR2(14) not null             ");
				sb.append(" )';                                              ");
				sb.append("     EXECUTE IMMEDIATE strSQL_CREATE;             ");
				sb.append("   end if;                                        ");
				sb.append(" end;                                             ");
				
				String sql = sb.toString();
				queryRunner.update(conn, sql);
				
				QueryRunner run = new QueryRunner();

				String createMappingSql = "insert into CONVERT_FIDS (FID, PROGRAM_ID,CONVERT_TIME) VALUES (?,?,?)";
				Object[][] inParam = new Object[entry.getValue().size()][];
				int i = 0;
				for(String data:entry.getValue()){
					String[] cells = data.split("_");
					Object[] temp = new Object[3];
					temp[0] = cells[0];
					temp[1] = cells[1];
					temp[2] = cells[2];
					inParam[i] = temp;
					i++;
				}
				run.batch(conn, createMappingSql, inParam);

			} catch (Exception e) {
				DbUtils.rollbackAndCloseQuietly(conn);
				log.error(e.getMessage(), e);
				throw new ServiceException("failed to get DBId:" + e.getMessage(), e);
			} finally {
				DbUtils.commitAndCloseQuietly(conn);
			}
		}
		
	}
}
>>>>>>> origin/master
