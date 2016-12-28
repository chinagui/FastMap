package com.navinfo.dataservice.engine.meta.patternimage;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

public class PatternImageImporter {

	private static Connection conn;

	private static PreparedStatement pstmt;
	private static PreparedStatement pstmtSvg;

	private static String sql = "update SC_MODEL_MATCH_G set format=:1,file_content=:2 where file_name=:3";
	private static String sqlSvg = "update SC_VECTOR_MATCH set format=:1,file_content=:2 where file_name=:3";
	private static int counter = 0;
	
	/*public  PatternImageImporter(PreparedStatement pstmt1,PreparedStatement pstmtSvg1){
		this.pstmt=pstmt1;
		this.pstmtSvg=pstmtSvg1;
	}*/
//
//	public static InputStream getJpgStream(String filePath) throws Exception {
//		ByteArrayOutputStream out = new ByteArrayOutputStream();
//
//		BufferedImage bufferedImage = ImageIO.read(new File(filePath));
//
//		BufferedImage newBufferedImage = new BufferedImage(
//				bufferedImage.getWidth(), bufferedImage.getHeight(),
//				BufferedImage.TYPE_INT_RGB);
//		newBufferedImage.createGraphics().drawImage(bufferedImage, 0, 0,
//				Color.WHITE, null);
//
//		ImageIO.write(newBufferedImage, "jpg", out);
//
//		InputStream in = new ByteArrayInputStream(out.toByteArray());
//
//		return in;
//	}

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

}
