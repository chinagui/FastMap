package com.navinfo.fcc.test;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.commons.photo.Photo;
import com.navinfo.dataservice.commons.util.FileUtils;
import com.navinfo.dataservice.dao.photo.HBaseController;
import com.navinfo.dataservice.engine.audio.Audio;
import com.navinfo.dataservice.engine.audio.AudioImport;
import com.navinfo.dataservice.engine.dropbox.manger.UploadService;
import com.navinfo.dataservice.engine.fcc.patternImage.PatternImageImporter;
import com.navinfo.dataservice.engine.fcc.tips.CopyOfTipsUpload;
import com.navinfo.dataservice.engine.fcc.tips.TipsUpload;
import com.navinfo.dataservice.engine.photo.CollectorImport;
import com.navinfo.dataservice.engine.photo.PhotoGetter;
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;

public class TipsImportTest extends InitApplication {

	@Override
	@Before
	public void init() {
		initContext();
	}

	 @Test
	public void test() {
		String parameter = "{\"jobId\":2677}";
		try {

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			int jobId = jsonReq.getInt("jobId");

			UploadService upload = UploadService.getInstance();

			// String filePath = upload.unzipByJobId(jobId); //服务测试

			//E:\03 ni_robot\Nav_Robot\10测试数据\01上传下载\音频测试数据\2677  2677道路名
			//String filePath = "E:\\03 ni_robot\\Nav_Robot\\10测试数据\\01上传下载\\音频测试数据\\2677"; // 本地测试用
			
			String filePath = "E:\\03 ni_robot\\Nav_Robot\\10测试数据\\01上传下载\\模式图测试数据\\1664"; // 本地测试用

			// String
			// filePath="E:\\03 ni_robot\\Nav_Robot\\10测试数据\\01上传下载\\upload\\893";

			Map<String, Photo> photoMap = new HashMap<String, Photo>();

			Map<String, Audio> audioMap = new HashMap<String, Audio>();

			TipsUpload tipsUploader = new TipsUpload();

			tipsUploader.run(filePath + "\\tips.txt", photoMap, audioMap);

			CollectorImport.importPhoto(photoMap, filePath);

			AudioImport.importAudio(audioMap, filePath);

			JSONArray patternImageResultImpResult=PatternImageImporter.importImage(filePath + "/"+ "JVImage.txt",filePath +"/JVImage"); //JVImage为模式图的文件夹
			
			JSONObject result = new JSONObject();

			result.put("total", tipsUploader.getTotal());

			result.put("failed", tipsUploader.getFailed());

			result.put("reasons", tipsUploader.getReasons());
			
			result.put("JVImageResult", patternImageResultImpResult);
			

			System.out.println("开始上传tips完成，jobId:" + jobId + "\tresult:"
					+ result);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 单纯的上传txt测试
	//@Test
	public  void testBeforeCut() {

		Map<String, Photo> photoMap = new HashMap<String, Photo>();

		Map<String, Audio> audioMap = new HashMap<String, Audio>();

		try {
			TipsUpload tipsUploader = new TipsUpload();
			long t1 = System.currentTimeMillis();
			System.out.println("t1:" + t1);
			tipsUploader
					.run("E:\\03 ni_robot\\Nav_Robot\\10测试数据\\01上传下载\\音频测试数据\\1425_1\\tip_1000.txt",
							photoMap, audioMap);
			long t2 = System.currentTimeMillis();
			System.out.println("打断前上传完成,h耗时：" + (t2 - t1));

			System.out.println("---------------------");

			/*
			 * CopyOfTipsUpload tipsUploader2=new CopyOfTipsUpload(); long
			 * t3=System.currentTimeMillis(); tipsUploader2.run(
			 * "E:\\03 ni_robot\\Nav_Robot\\10测试数据\\01上传下载\\音频测试数据\\1425_1\\tip_1000.txt"
			 * ,photoMap,audioMap); long t4=System.currentTimeMillis();
			 * System.out.println("打断后上传完成,h耗时："+(t4-t3));
			 */

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// 单纯的上传txt测试
	//@Test
	public void testAfterCut() {

		Map<String, Photo> photoMap = new HashMap<String, Photo>();

		Map<String, Audio> audioMap = new HashMap<String, Audio>();

		try {
			/*
			 * TipsUpload tipsUploader=new TipsUpload(); long
			 * t1=System.currentTimeMillis(); System.out.println("t1:"+t1);
			 * tipsUploader.run(
			 * "E:\\03 ni_robot\\Nav_Robot\\10测试数据\\01上传下载\\音频测试数据\\1425_1\\tip_1000.txt"
			 * ,photoMap,audioMap); long t2=System.currentTimeMillis();
			 * System.out.println("打断前上传完成,h耗时："+(t2-t1));
			 */

			System.out.println("---------------------");

			CopyOfTipsUpload tipsUploader2 = new CopyOfTipsUpload();
			long t3 = System.currentTimeMillis();
			tipsUploader2
					.run("E:\\03 ni_robot\\Nav_Robot\\10测试数据\\01上传下载\\音频测试数据\\1425_1\\tip_1000.txt",
							photoMap, audioMap);
			long t4 = System.currentTimeMillis();
			System.out.println("打断后上传完成,h耗时：" + (t4 - t3));

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	// 设置全貌照片
	@Test
	public void setDeepPhoto() {
		
		String parameter = "{\"newFccPid\":\"0489a85acb7e4b0ca5e09360215718c9\",\"oldFccPid\":\"\",\"flag\":1}";
		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);
			
			JSONObject result = new JSONObject();
			int flag = jsonReq.getInt("flag");
			String oldFccPid = jsonReq.getString("oldFccPid");
			String newFccPid = jsonReq.getString("newFccPid");
			
			PhotoGetter getter = new PhotoGetter();
			
			//新增或修改全貌照片
			if(flag==1){
				//获取照片
				byte[] bytes = getter.getPhotoByRowkey(newFccPid, "origin");
				//设置全貌
				byte[] photo=FileUtils.makeFullViewImage(bytes); 
				//上传全貌照片
				HBaseController hbaseController = new HBaseController();
				InputStream newIn = new ByteInputStream(photo, photo.length);
				//调用hadoop方法传输文件流，获取photo_id
				String photoId = hbaseController.putPhoto(newIn);
				
				result.put("PID", photoId);
			}else{
				//删除全景照片
				//TODO
			}
			
			System.out.println("result = "+result);
			
		} catch (Exception e) {
			e.getMessage();
		}

	}

}
