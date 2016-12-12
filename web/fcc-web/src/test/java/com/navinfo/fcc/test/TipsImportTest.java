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
import com.navinfo.dataservice.engine.fcc.tips.TipsUpload;
import com.navinfo.dataservice.engine.photo.CollectorImport;

public class TipsImportTest extends InitApplication {

	@Override
	//@Before
	public void init() {
	//	initContext();
	}

	@Test
	public void test() {
		String parameter = "{\"jobId\":123}";
		try {
			
			JSONObject jsonReq=JSONObject.fromObject(parameter);
			
			int jobId=jsonReq.getInt("jobId");
			
			UploadService upload = UploadService.getInstance();

			//String filePath = upload.unzipByJobId(jobId); //服务测试

			String filePath = "E:\\03 ni_robot\\Nav_Robot\\10测试数据\\01上传下载\\音频测试数据\\1423"; //本地测试用
			
			//String filePath="E:\\03 ni_robot\\Nav_Robot\\10测试数据\\01上传下载\\upload\\893";
			
			Map<String, Photo> photoMap=new HashMap<String, Photo>();
			
			Map<String, Audio> audioMap=new HashMap<String, Audio>();
			
			TipsUpload tipsUploader = new TipsUpload();

			tipsUploader.run(filePath+"\\tips.txt",photoMap,audioMap);
			
			CollectorImport.importPhoto(photoMap, filePath );
			
			AudioImport.importAudio(audioMap,filePath);
			
			JSONObject result = new JSONObject();

			result.put("total", tipsUploader.getTotal());

			result.put("failed", tipsUploader.getFailed());

			result.put("reasons", tipsUploader.getReasons());
			
			System.out.println("开始上传tips完成，jobId:"+jobId+"\tresult:"+result);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
