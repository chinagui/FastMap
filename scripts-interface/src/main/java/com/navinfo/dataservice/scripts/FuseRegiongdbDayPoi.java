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
public class FuseRegiongdbDayPoi {
	
	public static JSONObject execute(JSONObject request)throws Exception{
		JSONObject response = new JSONObject();
		try{
			String gen2DbInfo = request.getString("gen2DbInfo");
			Assert.notNull(gen2DbInfo, "gen2DbInfo不能为空");
			int fmgdbId = request.getInt("fmgdbId");//get如果没取到会报错
//			Assert.notNull(fmgdbId, "fmgdbId不能为空");
			String gdbVersion = (String) request.get("gdbVersion");
			Assert.notNull(gdbVersion, "gdbVersion不能为空");
			//整表复制道路数据
			JobInfo info2 = new JobInfo(0,"");
			info2.setType("gdbFullCopy");
			JSONObject req2 = new JSONObject();
			req2.put("sourceDbInfo", gen2DbInfo);
			req2.put("targetDbId", fmgdbId);
			req2.put("gdbVersion", "250+");
			req2.put("featureType", GlmTable.FEATURE_TYPE_ROAD);
			info2.setRequest(req2);
			AbstractJob job2 = JobCreateStrategy.createAsMethod(info2);
			job2.run();
			response.put("full_copy_road", "success");
		}catch (Exception e) {
			response.put("msg", "ERROR:" + e.getMessage());
			throw e;
		}
		return response;
	}
}
