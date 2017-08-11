package com.navinfo.dataservice.engine.photo;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;

import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.photo.Photo;
import com.navinfo.dataservice.commons.photo.RotateImageUtils;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.FileUtils;
import com.navinfo.dataservice.dao.photo.HBaseConnector;
import com.navinfo.dataservice.dao.photo.HBaseController;

public class CollectorImport {
	
	public static void importPhoto(String dir) throws Exception{
		
		File file = new File(dir);

		if (!file.exists()) {
			return;
		}
		
		File[] files = file.listFiles();

		HBaseController controller = new HBaseController();
		
		for (File f : files) {
			if(f.isFile() && f.getName().endsWith(".jpg")){
				FileInputStream in = new FileInputStream(f);
				System.out.println(f.getName());
				/*
				 * 2017.08.10 zl 取消图片自动旋转功能,移动端对应人员:肖岩
				 * //******zl 2016.12.09添加自动图片旋转**************
				 
				FileInputStream inn = new FileInputStream(f);
				int rotateAngle = RotateImageUtils.rotateOrientatione(inn);//获取图片旋转角度
				InputStream newIn =new FileInputStream(f);
		    	if(rotateAngle > 0){
					BufferedImage image= ImageIO.read(in); 
			    	Image newImage = RotateImageUtils.rotateImage(image,rotateAngle);
			    	if(newImage !=null){
			    		newIn = RotateImageUtils.getImageStream((BufferedImage) newImage);
			    	}
		    	}
				//********************
			controller.putPhoto(f.getName().replace(".jpg", ""),newIn);
				in.close();
				newIn.close();
				*/
				controller.putPhoto(f.getName().replace(".jpg", ""),in);
				in.close();
			}
		}
	}

	public static void importPhoto(Map<String,Photo> map,String dir) throws Exception{
		
		if (map.size()==0){
			return;
		}
		
		File file = new File(dir);

		if (!file.exists()) {
			return;
		}
		//******zl 2016.12.07 添加自动图片旋转**************
		//需要旋转的图片,旋转后替换原有图片
		//20161210, modifiedby liya 增加判断，如果路径不是个Directory，才进行旋转代码。因为tips传入的是文件路径，会报错
		if(!file.isDirectory()){
			RotateImageUtils.rotateImage(dir);
		}
		//********************
		
		Map<String,byte[]> mapPhoto = FileUtils.readPhotos(dir);
		
		Map<String, Integer> exitstPhoto=getAllExitsPhoto(map); //找到所有的在数据库中已存在的照片,已存在则不再导入
		
		//Map<String,byte[]> mapSltPhoto = FileUtils.genSmallImageMap(dir);
		
		Table photoTab = HBaseConnector.getInstance().getConnection().getTable(
				TableName.valueOf(HBaseConstant.photoTab));
		
		List<Put> puts = new ArrayList<Put>();
		
		Set<Entry<String,Photo>> set = map.entrySet();
		
		Iterator<Entry<String,Photo>> it = set.iterator();
		
		int num = 0;
		
		while(it.hasNext()){
			Entry<String,Photo> entry = it.next();
			//缩略图不存储，参3为null
			Put put = enclosedPut(entry,mapPhoto,null,exitstPhoto);
			
			if(put == null){
				continue;
			}
			
			puts.add(put);
			
			num++;
			
			if (num >=1000){
				photoTab.put(puts);
				
				puts.clear();
				
				num = 0;
			}
		}
		
		photoTab.put(puts);
		
		photoTab.close();
	}
	
	public static void importPhotoNew(Map<String, Photo> map, String dir) throws Exception {

		if (map.size() == 0) {
			return;
		}

		File file = new File(dir);

		if (!file.exists()) {
			return;
		}

		if (!file.isDirectory()) {
			RotateImageUtils.rotateImage(dir);
		}

		//传入map，按需读取二进制流，减小内存消耗
		Map<String, byte[]> mapPhoto = FileUtils.readPhotosNew(map,dir);

		Map<String, Integer> exitstPhoto = getAllExitsPhoto(map); // 找到所有的在数据库中已存在的照片,已存在则不再导入

		Table photoTab = HBaseConnector.getInstance().getConnection()
				.getTable(TableName.valueOf(HBaseConstant.photoTab));

		List<Put> puts = new ArrayList<Put>();

		Set<Entry<String, Photo>> set = map.entrySet();

		Iterator<Entry<String, Photo>> it = set.iterator();

		int num = 0;

		while (it.hasNext()) {
			Entry<String, Photo> entry = it.next();
			// 缩略图不存储，参3为null
			Put put = enclosedPut(entry, mapPhoto, null, exitstPhoto);

			if (put == null) {
				continue;
			}

			puts.add(put);

			num++;

			if (num >= 1000) {
				photoTab.put(puts);

				puts.clear();

				num = 0;
			}
		}

		photoTab.put(puts);

		photoTab.close();
	}

	
	/**
	 * @Description:TOOD
	 * @param map
	 * @return
	 * @author: y
	 * @throws Exception 
	 * @time:2016-12-14 下午6:19:10
	 */
	private static Map<String, Integer> getAllExitsPhoto(Map<String, Photo> map) throws Exception {
		Set<Entry<String,Photo>> set = map.entrySet();
		
		Iterator<Entry<String,Photo>> it = set.iterator();
		
		int num = 0;
		
		JSONArray keysArr=new JSONArray();
		
		while(it.hasNext()){
			
			Entry<String,Photo> entry = it.next();
			
			Photo pht = entry.getValue();
			
			String rowkey=pht.getRowkey();
			
			keysArr.add(rowkey);
		}
		
		Map<String, Integer> result=new PhotoGetter().getPhotosExistByRowkey(keysArr);
		
		return result;
	}

	private static Put enclosedPut(Entry<String,Photo> entry,Map<String,byte[]> mapPhoto,Map<String,byte[]> mapSltPhoto, Map<String, Integer> exitstPhoto) throws Exception
	{
		Photo pht = entry.getValue();
		
		String rowkey=pht.getRowkey();
		
		//如果数据库中已经存在改id，则不再导入
		if(exitstPhoto.get(rowkey)!=null){
			return  null;
		}
		
		String name = entry.getKey();
		
		byte[] photo = mapPhoto.get(name);
		
		if(photo == null){
			return null;
		}
		
		//byte[] sltPhoto = mapSltPhoto.get(name);
		
		Put put = new Put(pht.getRowkey().getBytes());
		
		put.addColumn("data".getBytes(), "attribute".getBytes(), JSONObject
				.fromObject(pht).toString().getBytes());
		
		put.addColumn("data".getBytes(), "origin".getBytes(), photo);
		
		//put.addColumn("data".getBytes(), "thumbnail".getBytes(), sltPhoto);
		
		return put;
	}
	
	/**
	 * 众包照片导入
	 * @param inputStream
	 * @param angle
	 * @param fileName
	 * @param x
	 * @param y
	 * @throws Exception
	 * 修改众包照片入fcc库photo维护字段
	 */
	public static void importCrowdPhoto(InputStream inputStream, int angle, String fileName, double x, double y) throws Exception{
		HBaseController controller = new HBaseController();
		InputStream newIn = inputStream;
    	if(angle > 0){
			BufferedImage image= ImageIO.read(inputStream); 
	    	Image newImage = RotateImageUtils.rotateImage(image, angle);
	    	if(newImage !=null){
	    		newIn = RotateImageUtils.getImageStream((BufferedImage) newImage);
	    	}
    	}
    	String rowKey = fileName.replace(".jpg", "");
		
    	Photo photo = new Photo();
		photo.setRowkey(rowKey);
		// a_uuid和rowkey相同
		photo.setA_uuid(rowKey);
		photo.setA_version(SystemConfigFactory.getSystemConfig()
				.getValue(PropConstant.seasonVersion));
		// a_content照片内容为：设施 2
		photo.setA_content(2);
		// a_sourceId来源为：众包POI 5
		photo.setA_sourceId(5);
		// a_latitude和a_longitude: 照片的经度和纬度
		photo.setA_longitude(x);
		photo.setA_latitude(y);
		// 设置上传时间
		String a_uploadDate = DateUtils.dateToString(new Date(), DateUtils.DATE_COMPACTED_FORMAT);
		photo.setA_uploadDate(a_uploadDate);
		
		controller.putPhoto(rowKey, newIn, photo);
		
		newIn.close();
	}
}
