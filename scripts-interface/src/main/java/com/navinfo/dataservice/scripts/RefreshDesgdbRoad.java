package com.navinfo.dataservice.scripts;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.util.Assert;

import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.man.model.Grid;
import com.navinfo.dataservice.api.man.model.Region;
import com.navinfo.dataservice.bizcommons.glm.GlmTable;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.datahub.service.DbService;
import com.navinfo.dataservice.engine.man.grid.GridService;
import com.navinfo.dataservice.engine.man.region.RegionService;
import com.navinfo.dataservice.expcore.ExportConfig;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.dataservice.jobframework.runjob.JobCreateStrategy;

import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

/** 
* @ClassName: refreshFmgdbRoad 
* @author Xiao Xiaowen 
* @date 2016年5月26日 下午5:12:06 
* @Description: TODO
*  
*/
public class RefreshDesgdbRoad {
	
	public static JSONObject execute(JSONObject request)throws Exception{
		JSONObject response = new JSONObject();
		try{
			//
			String gdbVersion = SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion);
			Assert.notNull(gdbVersion, "gdbVersion不能为空,检查是否sys_config表中未配置当前gdb版本");
			String[] bizTypes = new String[]{"desDayPoi,desDayAll,desMon"};
			DbInfo fmgdb = DbService.getInstance().getOnlyDbByBizType("nationRoad");
			for(String type:bizTypes){
				//
				DbInfo desdb = DbService.getInstance().getOnlyDbByBizType(type);
				JobInfo info1 = new JobInfo(0,"");
				info1.setType("gdbFullCopy");
				JSONObject req1 = new JSONObject();
				req1.put("sourceDbId", fmgdb.getDbId());
				req1.put("targetDbId", desdb.getDbId());
				req1.put("featureType", GlmTable.FEATURE_TYPE_ROAD);
				req1.put("gdbVersion", gdbVersion);
				req1.put("truncateData", true);
				info1.setRequest(req1);
				AbstractJob job1 = JobCreateStrategy.createAsMethod(info1);
				job1.run();
				if(job1.getJobInfo().getStatus()!=3){
					String msg = (job1.getException()==null)?"未知错误。":"错误："+job1.getException().getMessage();
					throw new Exception("出品库job内部发生"+msg);
				}
				response.put(type+"_refresh", "success");
			}
			response.put("msg", "执行成功");
		}catch (Exception e) {
			response.put("msg", "ERROR:" + e.getMessage());
			throw e;
		}
		return response;
	}
}
