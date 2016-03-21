package com.navinfo.dataservice.photo;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.imageio.ImageIO;

import net.sf.json.JSONObject;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;

import ch.hsr.geohash.GeoHash;

import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.commons.db.HBaseAddress;
import com.navinfo.dataservice.commons.db.SolrAddress;
import com.navinfo.dataservice.commons.photo.Photo;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

public class SDFPhotoImporter {

	/**
	 * @param args
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws Exception {

		HBaseAddress.initHBaseAddress("192.168.3.156");

		Table photoTab = HBaseAddress.getHBaseConnection().getTable(
				TableName.valueOf(HBaseConstant.photoTab));

		String dir = args[0];

		String prefixPath = args[1];

		SDFPhotoImporter.importPhoto(null, photoTab, dir, prefixPath);
	}

	public static void importSDFFIle(File file, String prefixPath,
			Connection conn, Table photoTab) throws Exception {
		Scanner scanner = new Scanner(new FileInputStream(file));

		List<Put> puts = new ArrayList<Put>();

		int num = 0;

		while (scanner.hasNextLine()) {
			num++;

			String line = scanner.nextLine();

			line = line.replaceAll(";", " ; ");

			Put put = fillPut(line, prefixPath);

			if (put != null) {
				puts.add(put);
			}
			if (num % 1000 == 0) {
				photoTab.put(puts);

				System.out.println(num);

				puts.clear();
			}

		}

		photoTab.put(puts);
	}

	public static Put fillPut(String line, String prefixPath) throws Exception {

		String[] splits = line.split(";");

		String uuid = splits[0].trim();

		int type = Integer.parseInt(splits[1].trim());

		double lng = Double.parseDouble(splits[5].trim());

		double lat = Double.parseDouble(splits[6].trim());

		if (lng == 0.0 || lat == 0.0) {
			return null;
		}

		double direct = Double.parseDouble(splits[7].trim());

		String deviceNum = splits[8].trim();

		String shootDate = splits[9].trim();

		Photo photo = SDFPhotoImporter.convertPhoto(uuid, type, lng, lat,
				direct, deviceNum, shootDate);

		String rowkey = SDFPhotoImporter.covertRowkey(lng, lat, uuid);

		photo.setRowkey(rowkey);

		Put put = new Put(rowkey.getBytes());

		put.addColumn("data".getBytes(), "attribute".getBytes(), JSONObject
				.fromObject(photo).toString().getBytes());

		put.addColumn("data".getBytes(), "brief".getBytes(), photo
				.convert2Brief().getBytes());

		String jpg = prefixPath + splits[2].trim();

		File jpgFile = new File(jpg);

		byte[] jbs = getFileBytes(jpgFile);

		put.addColumn("data".getBytes(), "origin".getBytes(), jbs);

		byte[] jpgbyte = makeSmallImage(jpgFile);

		put.addColumn("data".getBytes(), "thumbnail".getBytes(), jpgbyte);

		return put;
	}

	public static String covertRowkey(double lng, double lat, String uuid) {

		String rowkey = GeoHash.geoHashStringWithCharacterPrecision(lat, lng,
				12) + uuid;

		return rowkey;

	}

	public static Photo convertPhoto(String uuid, int type, double lng,
			double lat, double direct, String deviceNum, String shootDate) {
		Photo p = new Photo();

		p.setA_uuid(uuid);

		p.setA_longitude(lng);

		p.setA_latitude(lat);

		p.setA_direction(direct);

		p.setA_deviceNum(deviceNum);

		p.setA_shootDate(shootDate);

		return p;
	}

	public static byte[] getFileBytes(File jpgFile) throws Exception {

		byte[] bytes = new byte[(int) jpgFile.length()];

		InputStream in = new FileInputStream(jpgFile);

		in.read(bytes);

		return bytes;
	}

	public static byte[] makeSmallImage(File srcImageFile) throws Exception {
		JPEGImageEncoder encoder = null;
		BufferedImage tagImage = null;
		Image srcImage = null;
		srcImage = ImageIO.read(srcImageFile);
		int srcWidth = srcImage.getWidth(null);// 原图片宽度
		int srcHeight = srcImage.getHeight(null);// 原图片高度
		int dstMaxSize = 120;// 目标缩略图的最大宽度/高度，宽度与高度将按比例缩写
		int dstWidth = srcWidth;// 缩略图宽度
		int dstHeight = srcHeight;// 缩略图高度
		float scale = 0;
		// 计算缩略图的宽和高
		if (srcWidth > dstMaxSize) {
			dstWidth = dstMaxSize;
			scale = (float) srcWidth / (float) dstMaxSize;
			dstHeight = Math.round((float) srcHeight / scale);
		}
		srcHeight = dstHeight;
		if (srcHeight > dstMaxSize) {
			dstHeight = dstMaxSize;
			scale = (float) srcHeight / (float) dstMaxSize;
			dstWidth = Math.round((float) dstWidth / scale);
		}
		// 生成缩略图
		tagImage = new BufferedImage(dstWidth, dstHeight,
				BufferedImage.TYPE_INT_RGB);
		tagImage.getGraphics().drawImage(srcImage, 0, 0, dstWidth, dstHeight,
				null);
		// fileOutputStream = new FileOutputStream(dstImageFileName);

		ByteOutputStream bos = new ByteOutputStream();

		encoder = JPEGCodec.createJPEGEncoder(bos);
		encoder.encode(tagImage);

		return bos.getBytes();
	}

	public static boolean importPhoto(SolrAddress sa, Table photoTab,
			String dir, String prefixPath) throws Exception {

		File file = new File(dir);

		File[] files = file.listFiles();

		for (File f : files) {
			if (f.getName().endsWith(".txt")) {
				SDFPhotoImporter.importSDFFIle(f, prefixPath,
						HBaseAddress.getHBaseConnection(), photoTab);
			}
		}

		return true;
	}

}
