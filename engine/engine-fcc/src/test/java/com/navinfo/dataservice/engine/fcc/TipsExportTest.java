package com.navinfo.dataservice.engine.fcc;

import java.io.File;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.engine.fcc.tips.TipsExporter;
import com.navinfo.dataservice.engine.fcc.tips.TipsUpload;

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

	//private Connection conn;

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

			op.export(condition, filePath, "tips.txt", images);
			
			System.out.println("导出成功:"+filePath);
			System.out.println(op.export(condition, filePath, "tips.txt", images));
			
			JSONObject result=new JSONObject();
			result.put("url", filePath);
			
			result.put("downloadDate",  DateUtils.dateToString(new Date(),
					DateUtils.DATE_COMPACTED_FORMAT));
			
			System.out.println(result);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
