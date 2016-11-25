package com.navinfo.dataservice.engine.fcc;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
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

			
			parameter="{\"condition\":[{\"grid\":60561722,\"date\":\"20161121154107\"}]}";
			
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
			
			System.out.println("导出成功");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
