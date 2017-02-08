package com.navinfo.dataservice.engine.fcc.patternImage;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.util.FileUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class PatternImageImporter {
	
	private static final Logger logger = Logger
			.getLogger(PatternImageImporter.class);

	private static Connection conn;

	private static PreparedStatement pstmt;
	private static PreparedStatement pstmtSvg;

	private static String sql = "update SC_MODEL_MATCH_G set format=:1,file_content=:2 where file_name=:3";
	private static String sqlSvg = "update SC_VECTOR_MATCH set format=:1,file_content=:2 where file_name=:3";
	private static int counter = 0;
	
	
	static class ErrorType {
		static int DataExists = 1;

		static int SUCESS = 0;

		static int FAIL = 3;

	}
	

	public static void main(String[] args) throws Exception {

		String username = args[0];

		String password = args[1];

		String ip = args[2];

		int port = Integer.parseInt(args[3]);

		String serviceName = args[4];

		String path = args[5];

		Class.forName("oracle.jdbc.driver.OracleDriver");

		conn = DriverManager.getConnection("jdbc:oracle:thin:@" + ip + ":"
				+ port + ":" + serviceName, username, password);

		conn.setAutoCommit(false);

		pstmt = conn.prepareStatement(sql);

		readDataImg(path);

		//readData(path);

		pstmtSvg = conn.prepareStatement(sqlSvg);
		readDataSvg(path);
		
		conn.commit();

		conn.close();
		
		System.out.println("Done. Total:"+counter);
	}
	
	/**
	 * @Title: readDataImg
	 * @Description: 向数据库表SC_MODEL_MATCH_G 中导入图片
	 * @param path
	 * @throws Exception  void
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年12月26日 下午3:14:18 
	 */
	private static void readDataImg(String path) throws Exception {
	//public  void readDataImg(String path) throws Exception {
		File file = new File(path);

		File[] files = file.listFiles();

		for (File f : files) {
			if (f.isDirectory()) {
				//2D、3D、HEG、CRCG、CRPG、Dsign
				if(f.getName().contains("2D") || f.getName().contains("3D") || f.getName().contains("HEG")
						|| f.getName().contains("CRCG") || f.getName().contains("CRPG") || f.getName().contains("Dsign")
						|| f.getName().contains("SCHEMATIC") || f.getName().contains("arrow") || f.getName().contains("pattern")){
					readDataImg(f.getAbsolutePath());
				}
			} else {
				
				if (isImage(f)) {

					String[] splits = f.getName().split("\\.");

					InputStream in = null;
					
					String format = splits[splits.length - 1];

					pstmt.setString(1, format);

					in = new FileInputStream(f.getAbsoluteFile());

					pstmt.setBlob(2, in);

					pstmt.setString(3, splits[0]);

					pstmt.execute();

					counter++;

					if (counter % 1000 == 0) {
						System.out.println(counter);

						conn.commit();
					}
				}
			}
		}
	}
	
	/**
	 * @Title: readDataSvg
	 * @Description: 向数据库表SC_VECTOR_MATCH中导入svg图片
	 * @param path
	 * @throws Exception  void
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年12月26日 下午7:39:35 
	 */
	private static void readDataSvg(String path) throws Exception {
	//public  void readDataSvg(String path) throws Exception {
		File file = new File(path);

		File[] files = file.listFiles();

		for (File f : files) {
			if (f.isDirectory()) {
				if(f.getName().contains("Realsign") || f.getName().contains("SVG")){
					readDataSvg(f.getAbsolutePath());
				}
			} else {
				
				if (isImage(f)) {

					String[] splits = f.getName().split("\\.");
						//if(splits[1].equals("svg")){
							InputStream in = null;
							
							String format = splits[splits.length - 1];

							pstmtSvg.setString(1, format);

							in = new FileInputStream(f.getAbsoluteFile());

							pstmtSvg.setBlob(2, in);

							pstmtSvg.setString(3, splits[0]);

							pstmtSvg.execute();

							counter++;

							if (counter % 10000 == 0) {
								System.out.println(counter);

								conn.commit();
							}
						//}
				}
			}
		}
	}
	
	
	
	private static boolean isImage(File file)  
	//public static boolean isImage(File file)  
    {  
        boolean flag = false;   
        try  
        {  
            ImageInputStream is = ImageIO.createImageInputStream(file);  
            if(null == is)  
            {  
                return flag;  
            }  
            is.close();  
            flag = true;  
        } catch (Exception e)  
        {  
            //e.printStackTrace();  
        }  
        return flag;  
    }  

	private static void readData(String path) throws Exception {
		File file = new File(path);

		File[] files = file.listFiles();

		for (File f : files) {
			if (f.isDirectory()) {
				readData(f.getAbsolutePath());
			} else {
				
				if (isImage(f)) {

					String[] splits = f.getName().split("\\.");

					InputStream in = null;
					
					String format = splits[splits.length - 1];

					pstmt.setString(1, format);

					in = new FileInputStream(f.getAbsoluteFile());

					pstmt.setBlob(2, in);

					pstmt.setString(3, splits[0]);

					pstmt.execute();

					counter++;

					if (counter % 1000 == 0) {
						System.out.println(counter);

						conn.commit();
					}
				}
			}
		}
	}

	/**
	 * @Description:导入模式图（fcc模式图导入）
	 * @param filePath
	 * @param imageDirectory
	 * @author: y
	 * @throws Exception
	 * @time:2016-12-29 下午2:30:53
	 */
	public static JSONArray importImage(String filePath, String imageDirectory)
			throws Exception {

		JSONArray result=new JSONArray();
		
		String fileName4Msg=null;
		try {
			File file1 = new File(filePath);

			if (!file1.exists()&&!file1.isFile()) {
				return null;
			}
			
			// 1. 读取txt获取模式图信息
			Map<String, PatternImage> imageMap = loadFileData(filePath);

			if (imageMap.size() == 0) {
				return null;
			}

			File file = new File(imageDirectory);

			if (!file.exists()&&!file.isDirectory()) {
				return null;
			}

			// 2. readPhotos 读取同照片 这里不用修改

			Map<String, byte[]> mapImageContentMap = FileUtils
					.readPhotos(imageDirectory);

			// 3.获取每个照片的具体内容

			PatternImageSelector selector = new PatternImageSelector();

			Set<String> keys = mapImageContentMap.keySet();
			
			List<PatternImage> importImages = new ArrayList<PatternImage>();

			for (String fileName : keys) {
				
				fileName4Msg=fileName;//异常信息使用

				PatternImage image = imageMap.get(fileName);
				
				image.setContent(mapImageContentMap.get(fileName));

				String name = image.getName();

				// 判断照片是否已存在不存在则导入
				if (!selector.isFileExists(name)) {

					importImages.add(image);
					
					result.add(newReasonObject(name,ErrorType.SUCESS));
					
				}else{
					
					result.add(newReasonObject(name,ErrorType.DataExists));
				}
			}
			
			// 导入照片数据
			PatternImageUploader uploader = new PatternImageUploader();
			uploader.run(importImages);
			
			logger.info("模式图导入成功");
			
			return result;
		} catch (Exception e) {
			logger.error("模式图导入失败：" + e.getMessage(),e);
			
			result.add(newReasonObject(fileName4Msg,ErrorType.SUCESS));
			
			throw new Exception("模式图导入失败：" + e.getMessage(),e);
		}

	}
	
	
	private static JSONObject newReasonObject(String rowkey, int type) {

		JSONObject json = new JSONObject();

		json.put("name", rowkey);

		json.put("type", type);

		return json;
	}

	/**
	 * @Description:读取模式图的 txt文件
	 * @param filePath
	 * @return
	 * @author: y
	 * @throws Exception
	 * @time:2016-12-29 下午3:26:58
	 */
	private static Map<String, PatternImage> loadFileData(String filePath)
			throws Exception {

		Scanner scanner = null;

		Map<String, PatternImage> resultMap = new HashMap<String, PatternImage>();

		try {

			scanner = new Scanner(new FileInputStream(filePath));

			while (scanner.hasNextLine()) {

				String line = scanner.nextLine();

				JSONObject json = JSONObject.fromObject(line);

				String name = json.getString("name");// 文件名称

				String format = json.getString("format"); // 文件后缀

				String fileName = name + "." + format;

				PatternImage image = new PatternImage();

				image.setName(json.getString("name"));

				image.setFormat(json.getString("format"));

				image.setbType(json.getString("bType"));

				image.setmType(json.getString("mType"));

				image.setUserId(json.getInt("userId"));

				image.setOperateDate(json.getString("operateDate"));

				image.setUploadDate(json.getString("uploadDate"));

				image.setDownloadDate(json.getString("downloadDate"));

				image.setStatus(json.getInt("status"));

				resultMap.put(fileName, image);

			}
		} catch (Exception e) {

			throw new Exception ("read "+filePath+" error:"+e.getMessage(),e);
		}

		return resultMap;

	}
}
