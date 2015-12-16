package com.navinfo.dataservice.FosEngine.patterimg;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import javax.imageio.ImageIO;

public class ImportPatterImage {
	
	private static Connection conn;
	
	private static PreparedStatement pstmt;
	
	private static String sql = "insert into pattern_image values (:1,:2)";
	
	private static int counter = 0;

	public static InputStream getJpgStream(String filePath) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		BufferedImage bufferedImage = ImageIO.read(new File(filePath));

		BufferedImage newBufferedImage = new BufferedImage(
				bufferedImage.getWidth(), bufferedImage.getHeight(),
				BufferedImage.TYPE_INT_RGB);
		newBufferedImage.createGraphics().drawImage(bufferedImage, 0, 0,
				Color.WHITE, null);

		ImageIO.write(newBufferedImage, "jpg", out);
		
		InputStream in = new ByteArrayInputStream(out.toByteArray());

		return in;
	}
	
	public static void main(String[] args) throws Exception {

		String username = args[0];
		
		String password = args[1];
		
		String ip = args[2];
		
		int port = Integer.parseInt(args[3]);
		
		String serviceName = args[4];
		
		String path = args[5];
		
		Class.forName("oracle.jdbc.driver.OracleDriver");

		conn = DriverManager
				.getConnection("jdbc:oracle:thin:@"+ip+":"+port+":"+serviceName,
						username, password);
		
		conn.setAutoCommit(false);
		
		pstmt = conn.prepareStatement(sql);
		
		readData(path);
		
		conn.commit();
		
		conn.close();
	}
	
	private static void readData(String path)throws Exception
	{
		File file = new File(path);
		
		File[] files = file.listFiles();
		
		for(File f : files){
			if (f.isDirectory()){
				readData(f.getAbsolutePath());
			}else{
				if (f.getName().toLowerCase().endsWith(".bmp") || f.getName().toLowerCase().endsWith(".png")){
					
					pstmt.setString(1, f.getName());
					
					InputStream in = null;
					try {
						in = getJpgStream(f.getAbsolutePath());
					} catch (Exception e) {
						
						in = new FileInputStream(f.getAbsoluteFile());
					}
					
					pstmt.setBlob(2, in);
					
					pstmt.execute();
					
					counter++;
					
					if (counter % 10000 == 0){
						System.out.println(counter);
						
						conn.commit();
					}
				}
			}
		}
	}

}
