package com.navinfo.dataservice.engine.fcc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.spi.AudioFileWriter;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.solr.common.util.ContentStreamBase.FileStream;
import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.FileUtils;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.engine.audio.AudioController;
import com.navinfo.dataservice.engine.audio.AudioImport;
import com.navinfo.dataservice.engine.fcc.photo.HBaseController;
import com.navinfo.dataservice.engine.fcc.tips.TipsExporter;
import com.navinfo.dataservice.engine.fcc.tips.TipsUpload;
import com.navinfo.navicommons.geo.computation.CompGridUtil;
import com.navinfo.navicommons.geo.computation.GridUtils;

/** 
 * @ClassName: TipsExportTest.java
 * @author y
 * @date 2016-11-1 上午10:37:59
 * @Description: TODO
 *  
 */
public class TipsExportTest extends InitApplication{
	
	
	@Override
	@Before
	public void init() {
		initContext();
	}


	public TipsExportTest() throws Exception {
	}

	@Test
	public void tesDownload() {
		try {
			String  parameter="{\"condition\":[{\"grid\":59567502,\"date\":\"20161030174626\"},{\"grid\":59567511,\"date\":\"20161030174626\"},{\"grid\":59567501,\"date\":\"20161030174626\"}]}";
			//parameter="{\"condition\":[{\"grid\":59567502,\"date\":\"\"},{\"grid\":59567511,\"date\":\"\"},{\"grid\":59567501,\"date\":\"\"}]}";
			
			//parameter="{\"condition\":[{\"grid\":59565721,\"date\":\"20161103090651\"},{\"grid\":59565623,\"date\":\"20161103090651\"},{\"grid\":59565632,\"date\":\"20161103090651\"},{\"grid\":59566700,\"date\":\"20161103090651\"},{\"grid\":59565731,\"date\":\"20161103090651\"},{\"grid\":59565633,\"date\":\"20161103090651\"},{\"grid\":59565720,\"date\":\"20161103090651\"},{\"grid\":59566701,\"date\":\"20161103090651\"},{\"grid\":59565622,\"date\":\"20161103090651\"},{\"grid\":59565730,\"date\":\"20161103090651\"},{\"grid\":59566602,\"date\":\"20161103090651\"},{\"grid\":59566603,\"date\":\"20161103090651\"}]}";
			
			//parameter="{\"condition\":[{\"grid\":60562531,\"date\":\"\"},{\"grid\":60562530,\"date\":\"\"},{\"grid\":60562510,\"date\":\"\"},{\"grid\":60562502,\"date\":\"\"},{\"grid\":60562512,\"date\":\"\"},{\"grid\":60562520,\"date\":\"\"},{\"grid\":60562503,\"date\":\"\"},{\"grid\":60562533,\"date\":\"\"},{\"grid\":60562511,\"date\":\"\"},{\"grid\":60562501,\"date\":\"\"},{\"grid\":60562500,\"date\":\"\"},{\"grid\":60562532,\"date\":\"\"},{\"grid\":60562522,\"date\":\"\"},{\"grid\":60562513,\"date\":\"\"},{\"grid\":60562523,\"date\":\"\"},{\"grid\":60562521,\"date\":\"\"}]}";

			
			//parameter="{\"condition\":[{\"grid\":60561722,\"date\":\"20161121154107\"}]}";
			
			
			//parameter="{\"condition\":[{\"grid\":59567200,\"date\":\"\"},{\"grid\":59567201,\"date\":\"\"},{\"grid\":59567202,\"date\":\"\"},{\"grid\":59567203,\"date\":\"\"},{\"grid\":59567210,\"date\":\"\"},{\"grid\":59567211,\"date\":\"\"},{\"grid\":59567212,\"date\":\"\"},{\"grid\":59567213,\"date\":\"\"},{\"grid\":59567220,\"date\":\"\"},{\"grid\":59567221,\"date\":\"\"},{\"grid\":59567222,\"date\":\"\"},{\"grid\":59567223,\"date\":\"\"},{\"grid\":59567230,\"date\":\"\"},{\"grid\":59567231,\"date\":\"\"},{\"grid\":59567232,\"date\":\"\"},{\"grid\":59567233,\"date\":\"\"}]}";
			
			parameter="{\"condition\":[{\"grid\":59567213,\"date\":\"\"},{\"grid\":59567212,\"date\":\"\"},{\"grid\":59567220,\"date\":\"\"},{\"grid\":59567203,\"date\":\"\"},{\"grid\":59567202,\"date\":\"\"},{\"grid\":59567230,\"date\":\"\"},{\"grid\":59567222,\"date\":\"\"},{\"grid\":59567233,\"date\":\"\"},{\"grid\":59567210,\"date\":\"\"},{\"grid\":59567231,\"date\":\"\"},{\"grid\":59567211,\"date\":\"\"},{\"grid\":59567201,\"date\":\"\"},{\"grid\":59567223,\"date\":\"\"},{\"grid\":59567221,\"date\":\"\"},{\"grid\":59567232,\"date\":\"\"},{\"grid\":59567200,\"date\":\"\"}]}";
			
			parameter="{\"condition\":[{\"grid\":60564421,\"date\":\"20161202154416\"},{\"grid\":60564402,\"date\":\"20161202154416\"},{\"grid\":60564422,\"date\":\"20161202154416\"},{\"grid\":60564413,\"date\":\"20161202154416\"},{\"grid\":60564401,\"date\":\"20161202154416\"},{\"grid\":60564411,\"date\":\"20161202154416\"},{\"grid\":60564412,\"date\":\"20161202154416\"},{\"grid\":60564423,\"date\":\"20161202154416\"}]}";
			
			parameter="{\"condition\":[{\"grid\":60561300,\"date\":\"\"}]}";
			
			parameter="{\"condition\":[{\"grid\":60562500,\"date\":\"\"},{\"grid\":60562501,\"date\":\"\"},{\"grid\":60562502,\"date\":\"\"},{\"grid\":60562503,\"date\":\"\"},{\"grid\":60562510,\"date\":\"\"},{\"grid\":60562511,\"date\":\"\"},{\"grid\":60562512,\"date\":\"\"},{\"grid\":60562513,\"date\":\"\"},{\"grid\":60562520,\"date\":\"\"},{\"grid\":60562521,\"date\":\"\"},{\"grid\":60562522,\"date\":\"\"},{\"grid\":60562523,\"date\":\"\"},{\"grid\":60562530,\"date\":\"\"},{\"grid\":60562531,\"date\":\"\"},{\"grid\":60562532,\"date\":\"\"},{\"grid\":60562533,\"date\":\"\"}]}";
			
			//http://192.168.4.188:8000/service/fcc/tip/export?access_token=000001A8IWK0EVJSBD50286D509D3B1320F5C38F5C5D9C0E&parameter={"condition":[{"grid":60560220,"date":"20161210104905"}]}
			
			parameter="{\"condition\":[{\"grid\":60560232,\"date\":\"20161210120406\"}]}";
			
			parameter="{\"condition\":[{\"grid\":60560233,\"date\":\"20161210114441\"}]}";
			
			parameter="{\"condition\":[{\"grid\":59567220,\"date\":\"20161210112535\"}]}";
			
			parameter="{\"condition\":[{\"grid\":60561212,\"date\":\"\"},{\"grid\":60561203,\"date\":\"\"},{\"grid\":60561222,\"date\":\"\"},{\"grid\":60561221,\"date\":\"\"},{\"grid\":60561211,\"date\":\"\"},{\"grid\":60561230,\"date\":\"\"},{\"grid\":60561201,\"date\":\"\"},{\"grid\":60561213,\"date\":\"\"},{\"grid\":60561231,\"date\":\"\"},{\"grid\":60561233,\"date\":\"\"},{\"grid\":60561220,\"date\":\"\"},{\"grid\":60561200,\"date\":\"\"},{\"grid\":60561232,\"date\":\"\"},{\"grid\":60561210,\"date\":\"\"},{\"grid\":60561223,\"date\":\"\"},{\"grid\":60561202,\"date\":\"\"}]}";
			
			//parameter="{\"condition\":[{\"grid\":60561222,\"date\":\"20161226130604\"}]}";
			
			//parameter="{\"condition\":[{\"grid\":60561201,\"date\":\"\"}]}";
			
			String uuid = UuidUtils.genUuid();
			
			JSONObject jsonReq=JSONObject.fromObject(parameter);

			String filePath = "e:/testE/" + uuid + "/";

			File file = new File(filePath);

			if (!file.exists()) {
				file.mkdirs();
			}
			//grid和date的对象数组
			JSONArray condition = jsonReq.getJSONArray("condition");

			TipsExporter op = new TipsExporter();
			
			Set<String> images = new HashSet<String>();

			int expCount=op.export(condition, filePath, "tips.txt", images);
			
			System.out.println("导出成功:"+filePath);
			
			JSONObject data=null;
			if(expCount>0){
				data=new JSONObject();
				data.put("url", filePath);
				
				data.put("downloadDate",  DateUtils.dateToString(new Date(),
						DateUtils.DATE_COMPACTED_FORMAT));	
			}
			
			Map<String,Object> result = new HashMap<String,Object>();
	    	result.put("errcode", 0);
	    	result.put("errmsg", "success");
	    	result.put("data", data);
			
			System.out.println(JSONObject.fromObject(result));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void transFile(byte[] fileBt,String fileName){
        String filePath = "E:\\testE\\exp\\";   //文件路径
        FileOutputStream fstream =null;
        try
        {
        	fstream=new FileOutputStream(filePath+fileName);
            fstream.write(fileBt);
        }
        catch (Exception ex)
        {
            //抛出异常信息
        }
        finally
        {
        	try {
				fstream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
 
    }
	
	
	public static void transAudioFile(byte[] fileBt,String fileName){
        String filePath = "E:\\testE\\exp\\";   //文件路径
        
        OutputStream fstream =null;
        
        try
        {
        	fstream=new FileOutputStream(filePath+fileName);
            fstream.write(fileBt);
        }
        catch (Exception ex)
        {
            //抛出异常信息
        }
        finally
        {
        	try {
				fstream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
 
    }
	public static void main(String[] args) {
		String rowkey="702A3C888FC34A1CAABCE498E188AEE1";
		/*HBaseController  photoCon=new HBaseController();*/
		AudioController  photoCon=new AudioController();
		byte[] photoBytye;
		byte[] photoBytye2;
		try {
			//photoBytye = photoCon.getPhotoByRowkey(rowkey);
			photoBytye = photoCon.getAudioByRowkey(rowkey);
			
			photoBytye2 = FileUtils.readPhotos("E:\\testE\\exp\\").get("702A3C888FC34A1CAABCE498E188AEE1.amr");
			System.out.println(photoBytye.length==photoBytye2.length);
			System.out.println(photoBytye==photoBytye2);
			System.out.println("photoBytye.len(数据库的):"+photoBytye.length);
			System.out.println("photoBytye2.len:"+photoBytye2.length);
			transAudioFile(photoBytye,"test.amr");
			System.out.println("导出成功");
			
			//POINT (116.27175 39.97312)
			String grid_id = CompGridUtil.point2Grids(116.27175, 39.97312)[0];
			
			System.out.println(grid_id);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			
		}
		
		
		
		
	}

}
