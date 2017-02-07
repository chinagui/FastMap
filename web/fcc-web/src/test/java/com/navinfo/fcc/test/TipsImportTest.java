package com.navinfo.fcc.test;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.commons.photo.Photo;
import com.navinfo.dataservice.engine.audio.Audio;
import com.navinfo.dataservice.engine.audio.AudioImport;
import com.navinfo.dataservice.engine.dropbox.manger.UploadService;
import com.navinfo.dataservice.engine.fcc.tips.CopyOfTipsUpload;
import com.navinfo.dataservice.engine.fcc.tips.TipsUpload;
import com.navinfo.dataservice.engine.photo.CollectorImport;

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
			String filePath = "E:\\03 ni_robot\\Nav_Robot\\10测试数据\\01上传下载\\音频测试数据\\2677"; // 本地测试用

			// String
			// filePath="E:\\03 ni_robot\\Nav_Robot\\10测试数据\\01上传下载\\upload\\893";

			Map<String, Photo> photoMap = new HashMap<String, Photo>();

			Map<String, Audio> audioMap = new HashMap<String, Audio>();

			TipsUpload tipsUploader = new TipsUpload();

			tipsUploader.run(filePath + "\\tips.txt", photoMap, audioMap);

			CollectorImport.importPhoto(photoMap, filePath);

			AudioImport.importAudio(audioMap, filePath);

			// PatternImageImporter.importImage(filePath + "/"+
			// "JVImage.txt",filePath+"/JVImage"); //JVImage为模式图的文件夹

			JSONObject result = new JSONObject();

			result.put("total", tipsUploader.getTotal());

			result.put("failed", tipsUploader.getFailed());

			result.put("reasons", tipsUploader.getReasons());

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

}
