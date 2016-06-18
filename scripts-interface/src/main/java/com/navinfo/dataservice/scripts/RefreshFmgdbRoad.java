package com.navinfo.dataservice.scripts;

import java.io.File;
import java.util.Iterator;

import org.springframework.util.Assert;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.bizcommons.glm.GlmTable;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.dataservice.jobframework.runjob.JobCreateStrategy;

import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

/** 
* @ClassName: refreshFmgdbRoad 
* @author Xiao Xiaowen 
* @date 2016年5月26日 下午5:42:06 
* @Description: TODO
*  
*/
public class RefreshFmgdbRoad {
	
	public static JSONObject execute(JSONObject request)throws Exception{
		JSONObject response = new JSONObject();
		try{
			String gen2GdbIp = request.getString("gen2GdbIp");
			Assert.notNull(gen2GdbIp, "gen2GdbIp不能为空");
			int gen2GdbPort = request.getInt("gen2GdbPort");
			String gen2GdbSid = request.getString("gen2GdbSid");
			Assert.notNull(gen2GdbSid, "gen2GdbSid不能为空");
			String gen2GdbUserName = request.getString("gen2GdbUserName");
			Assert.notNull(gen2GdbUserName, "gen2GdbUserName不能为空");
			String gen2GdbUserPasswd = request.getString("gen2GdbUserPasswd");
			Assert.notNull(gen2GdbUserPasswd, "gen2GdbUserPasswd不能为空");
			int fmgdbId = request.getInt("fmgdbId");//get如果没取到会报错
//			Assert.notNull(fmgdbId, "fmgdbId不能为空");
			String gdbVersion = (String) request.get("gdbVersion");
			Assert.notNull(gdbVersion, "gdbVersion不能为空");
			//整表复制道路数据
			JobInfo info2 = new JobInfo(0,"");
			info2.setType("gdbFullCopy");
			JSONObject req2 = new JSONObject();
			req2.put("sourceDbId", fmgdbId);
			req2.put("targetDbId", 1);
			req2.put("gdbVersion", "250+");
			req2.put("featureType", GlmTable.FEATURE_TYPE_ALL);
			info2.setRequest(req2);
			AbstractJob job2 = JobCreateStrategy.createAsMethod(info2);
			job2.run();
		}catch (Exception e) {
			response.put("msg", "ERROR:" + e.getMessage());
			throw e;
		}
		return response;
	}
}
