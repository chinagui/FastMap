package com.navinfo.dataservice.scripts;

import java.util.List;

import org.springframework.util.Assert;

import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.man.model.Region;
import com.navinfo.dataservice.bizcommons.glm.GlmTable;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.datahub.service.DbService;
import com.navinfo.dataservice.engine.man.region.RegionService;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.dataservice.jobframework.runjob.JobCreateStrategy;

import net.sf.json.JSONObject;

/** 
* @ClassName: refreshFmgdbRoad 
* @author Xiao Xiaowen 
* @date 2016年5月26日 下午5:12:06 
* @Description: TODO
*  
*/
public class RefreshRegiongdbRoad {
	
	public static JSONObject execute(JSONObject request)throws Exception{
		JSONObject response = new JSONObject();
		try{
			//
			String gdbVersion = SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion);
			Assert.notNull(gdbVersion, "gdbVersion不能为空,检查是否sys_config表中未配置当前gdb版本");
			List<Region> regions =RegionService.getInstance().list();
			DbInfo fmgdb = DbService.getInstance().getOnlyDbByBizType("nationRoad");
			for(Region r:regions){
				//日db
				JobInfo info1 = new JobInfo(0,"");
				info1.setType("gdbFullCopy");
				JSONObject req1 = new JSONObject();
				req1.put("sourceDbId", fmgdb.getDbId());
				req1.put("targetDbId", r.getDailyDbId());
				req1.put("featureType", GlmTable.FEATURE_TYPE_ROAD);
				req1.put("gdbVersion", gdbVersion);
				req1.put("truncateData", true);
				info1.setRequest(req1);
				AbstractJob job1 = JobCreateStrategy.createAsMethod(info1);
				job1.run();
				if(job1.getJobInfo().getResponse().getInt("exeStatus")!=3){
					throw new Exception("日库job执行失败。region:"+r.getRegionId());
				}
				response.put("region_"+r.getRegionId()+"_day_refresh", "success");
				//月db
				JobInfo info2 = new JobInfo(0,"");
				info2.setType("gdbFullCopy");
				JSONObject req2 = new JSONObject();
				req2.put("sourceDbId", fmgdb.getDbId());
				req2.put("targetDbId", r.getMonthlyDbId());
				req2.put("featureType", GlmTable.FEATURE_TYPE_ROAD);
				req2.put("gdbVersion", gdbVersion);
				req2.put("truncateData", true);
				info2.setRequest(req2);
				AbstractJob job2 = JobCreateStrategy.createAsMethod(info2);
				job2.run();
				if(job2.getJobInfo().getResponse().getInt("exeStatus")!=3){
					throw new Exception("月库job执行失败。region:"+r.getRegionId());
				}
				response.put("region_"+r.getRegionId()+"_month_refresh", "success");
			}
			response.put("msg", "执行成功");
		}catch (Exception e) {
			response.put("msg", "ERROR:" + e.getMessage());
			throw e;
		}
		return response;
	}
}
