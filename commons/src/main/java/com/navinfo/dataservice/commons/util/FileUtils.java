package com.navinfo.dataservice.commons.util;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import com.navinfo.dataservice.commons.photo.RotateImageUtils;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

public class FileUtils {

	/**
	 * 读取目录中图片，生成字节数据到map中
	 * 
	 * @param dir
	 * @return
	 * @throws Exception
	 */
	public static Map<String, byte[]> readPhotos(String dir) throws Exception {
		Map<String, byte[]> map = new HashMap<String, byte[]>();

		File file = new File(dir);

		if (!file.exists()) {
			return map;
		}

		File[] files = file.listFiles();

		for (File f : files) {

			if (f.isDirectory()) {
				Map<String, byte[]> submap = readPhotos(f.getAbsolutePath());

				map.putAll(submap);
			} else {
				FileInputStream in = new FileInputStream(f);

				byte[] bytes = new byte[(int) f.length()];

				in.read(bytes);

				map.put(f.getName(), bytes);

				in.close();
			}
		}

		return map;
	}

	public static Map<String, byte[]> genSmallImageMap(String dir)
			throws Exception {

		Map<String, byte[]> map = new HashMap<String, byte[]>();

		File file = new File(dir);

		File[] files = file.listFiles();

		for (File f : files) {
			if(f.isFile() && (f.getName().endsWith(".jpg")||f.getName().endsWith(".png"))){
				map.put(f.getName(), makeSmallImage(f));
			}
		}

		return map;
	}

	/**
	 * 创建缩略图
	 * 
	 * @param srcImageFile
	 * @return
	 * @throws Exception
	 */
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

	/**
	 * @Title: makeSmallImage
	 * @Description: 创建缩略图 
	 * (修)(第七迭代) 变更:增加图片自动旋转
	 * @param bytes
	 * @return
	 * @throws Exception  byte[]
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年12月9日 上午10:43:14 
	 */
	public static byte[] makeSmallImage(byte[] bytes) throws Exception {
		JPEGImageEncoder encoder = null;
		BufferedImage tagImage = null;
		Image srcImage = null;
		//ByteInputStream bis = new ByteInputStream(bytes, bytes.length);
		//srcImage = ImageIO.read(bis);
		
		//**********2016.12.09 zl 添加图片自动旋转功能 **************
		Image newImage = null;
	    InputStream newIn = new ByteInputStream(bytes, bytes.length);//为计算图片旋转度准备的 in
	    int rotateAngle = RotateImageUtils.rotateOrientatione(newIn);//获取图片旋转角度
    	if(rotateAngle > 0){
    		InputStream newImageIn = new ByteInputStream(bytes, bytes.length);//为计算生成新的图片Image准备的 in
	    	 newImage = RotateImageUtils.rotateImage(ImageIO.read(newImageIn),rotateAngle);
    	}
    	if(newImage != null){
    		srcImage = newImage;
    	}else{
    		ByteInputStream bis = new ByteInputStream(bytes, bytes.length);
    		srcImage = ImageIO.read(bis);
    	}
		//*****************************************************
		
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

	public static void makeSmallImage(byte[] bytes, String dstImageFileName)
			throws Exception {
		JPEGImageEncoder encoder = null;
		BufferedImage tagImage = null;
		Image srcImage = null;
		ByteInputStream bis = new ByteInputStream(bytes, bytes.length);

		srcImage = ImageIO.read(bis);
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
		FileOutputStream fileOutputStream = new FileOutputStream(
				dstImageFileName);

		// ByteOutputStream bos = new ByteOutputStream();

		encoder = JPEGCodec.createJPEGEncoder(fileOutputStream);
		encoder.encode(tagImage);

	}
	
	/**
	 * 创建全貌照片
	 * 
	 * @param srcImageFile
	 * @return
	 * @throws Exception
	 */
	public static byte[] makeFullViewImage(byte[] bytes) throws Exception {

		JPEGImageEncoder encoder = null;
		BufferedImage tagImage = null;
		Image srcImage = null;
		
		int dstWidth = 400;// 全貌图宽度
		int dstHeight = 300;// 全貌图高度
		
		ByteInputStream bis = new ByteInputStream(bytes, bytes.length);
		srcImage = ImageIO.read(bis);
		// 生成全貌图
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

	public static void main(String[] args) throws Exception {
		System.out.println(new Date());

		Map<String, byte[]> map = FileUtils
				.genSmallImageMap("C:\\Users\\lilei3774\\Desktop\\20151027");

		System.out.println(new Date());
	}
}
