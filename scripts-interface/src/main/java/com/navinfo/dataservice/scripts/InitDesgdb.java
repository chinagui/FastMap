package com.navinfo.dataservice.scripts;

import net.sf.json.JSONObject;

import org.springframework.util.Assert;

import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.bizcommons.glm.GlmTable;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.datahub.service.DbService;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.dataservice.jobframework.runjob.JobCreateStrategy;

/**
 * @ClassName: InitProjectScriptsInterface
 * @author Xiao Xiaowen
 * @date 2016-1-15 下午3:40:32
 * @Description: TODO
 */
public class InitDesgdb {

	public static JSONObject execute(JSONObject request) throws Exception{
		JSONObject response = new JSONObject();
		try {
			DbInfo fmgdb = DbService.getInstance().getOnlyDbByBizType("nationRoad");
			String gdbVersion = SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion);
			Assert.notNull(gdbVersion, "gdbVersion不能为空,检查是否sys_config表中未配置当前gdb版本");
			
			//
			String[] bizTypes = new String[]{"desDayPoi,desDayAll,desMon"};
			String[] names = new String[]{"desgdb_day_p","desgdb_day_rp","desgdb_month"};
			for(int i=0;i<names.length;i++){
				//创建db
				JobInfo info1 = new JobInfo(0, "");
				info1.setType("createDb");
				JSONObject req1 = new JSONObject();
				req1.put("dbName", "orcl");
				req1.put("userName", names[i]);
				req1.put("userPasswd", names[i]);
				req1.put("bizType", bizTypes[i]);
				req1.put("descp", bizTypes[i]+" db");
				req1.put("gdbVersion", gdbVersion);
				info1.setRequest(req1);
				AbstractJob job1 = JobCreateStrategy.createAsMethod(info1);
				job1.run();
				int dbDay = job1.getJobInfo().getResponse().getInt("outDbId");
				response.put(names[i], dbDay);
				JobInfo info2 = new JobInfo(0,"");
				info2.setType("gdbFullCopy");
				JSONObject req2 = new JSONObject();
				req2.put("sourceDbId", fmgdb.getDbId());
				req2.put("targetDbId", dbDay);
				req2.put("gdbVersion", gdbVersion);
				req2.put("featureType", GlmTable.FEATURE_TYPE_ALL);
				info2.setRequest(req2);
				AbstractJob job2 = JobCreateStrategy.createAsMethod(info2);
				job2.run();
			}
			response.put("msg", "执行成功");
		} catch (Exception e) {
			response.put("msg", "ERROR:" + e.getMessage());
			throw e;
		}
		return response;
	}

}
