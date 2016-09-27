package com.navinfo.dataservice.scripts;


import org.springframework.util.Assert;

import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.bizcommons.glm.GlmTable;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.datahub.service.DbService;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.dataservice.jobframework.runjob.JobCreateStrategy;

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
			String gen2DbInfo = request.getString("gen2DbInfo");
			Assert.notNull(gen2DbInfo, "gen2DbInfo不能为空");
			String gdbVersion = SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion);
			Assert.notNull(gdbVersion, "gdbVersion不能为空,检查是否sys_config表中未配置当前gdb版本");
			DbInfo fmgdb = DbService.getInstance().getOnlyDbByBizType("nationRoad");
			//整表复制道路数据
			JobInfo info2 = new JobInfo(0,"");
			info2.setType("gdbFullCopy");
			JSONObject req2 = new JSONObject();
			req2.put("sourceDbInfo", gen2DbInfo);
			req2.put("targetDbId", fmgdb.getDbId());
			req2.put("gdbVersion", gdbVersion);
			req2.put("truncateData", true);
			req2.put("featureType", GlmTable.FEATURE_TYPE_ROAD);
			info2.setRequest(req2);
			AbstractJob job2 = JobCreateStrategy.createAsMethod(info2);
			job2.run();
			if(job2.getJobInfo().getStatus()!=3){
				String msg = (job2.getException()==null)?"未知错误。":"错误："+job2.getException().getMessage();
				throw new Exception("刷新gdb+库job内部发生"+msg);
			}
			response.put("full_copy_road", "success");
		}catch (Exception e) {
			response.put("msg", "ERROR:" + e.getMessage());
			throw e;
		}
		return response;
	}
}
