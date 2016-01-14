package com.navinfo.dataservice.FosEngine.patternimg;

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

public class ImportPatternImage {

	private static Connection conn;

	private static PreparedStatement pstmt;

	private static String sql = "update SC_MODEL_MATCH_G set format=:1,file_content=:2 where file_name=:3";

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

		conn = DriverManager.getConnection("jdbc:oracle:thin:@" + ip + ":"
				+ port + ":" + serviceName, username, password);

		conn.setAutoCommit(false);

		pstmt = conn.prepareStatement(sql);

		readData(path);

		conn.commit();

		conn.close();
	}

	private static void readData(String path) throws Exception {
		File file = new File(path);

		File[] files = file.listFiles();

		for (File f : files) {
			if (f.isDirectory()) {
				readData(f.getAbsolutePath());
			} else {
				if (f.getName().toLowerCase().endsWith(".bmp")
						|| f.getName().toLowerCase().endsWith(".png")) {

					String[] splits = f.getName().split("\\.");

					InputStream in = null;
					try {
						pstmt.setString(1, "jpg");

						in = getJpgStream(f.getAbsolutePath());
					} catch (Exception e) {
						String format = splits[splits.length - 1];

						pstmt.setString(1, format);

						in = new FileInputStream(f.getAbsoluteFile());
					}

					pstmt.setBlob(2, in);

					pstmt.setString(3, splits[0]);

					pstmt.execute();

					counter++;

					if (counter % 100 == 0) {
						System.out.println(counter);

						conn.commit();
					}
				}
			}
		}
	}

}
