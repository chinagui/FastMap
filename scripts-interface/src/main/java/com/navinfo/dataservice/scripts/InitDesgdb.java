package com.navinfo.dataservice.scripts;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.util.Assert;

import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.bizcommons.glm.GlmTable;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.log.LoggerRepos;
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
	protected static Logger log = LoggerRepos.getLogger(InitDesgdb.class);
	public static JSONObject execute(JSONObject request) throws Exception{
		JSONObject response = new JSONObject();
		try {
			DbInfo fmgdb = DbService.getInstance().getOnlyDbByBizType("nationRoad");
			String gdbVersion = SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion);
			Assert.notNull(gdbVersion, "gdbVersion不能为空,检查是否sys_config表中未配置当前gdb版本");
			Map<String,String> names = new HashMap<String,String>();
			if(request.get("typeNameMap")!=null){
				JSONArray ja = request.getJSONArray("typeNameMap");
				for(Object o:ja){
					JSONObject jo = (JSONObject)o;
					names.put(jo.getString("type"), jo.getString("name"));
				}
			}else{
				names.put("desDayPoi", "desgdb_day_p");
				names.put("desDayAll", "desgdb_day_rp");
				names.put("desMon", "desgdb_month");
			}
			//
			
			for(String key:names.keySet()){
				//创建db
				log.debug("Starting "+key+"......");
				JobInfo info1 = new JobInfo(0, "");
				info1.setType("createDb");
				JSONObject req1 = new JSONObject();
				req1.put("serverType", "ORACLE");
				req1.put("userName", names.get(key));
				req1.put("userPasswd", names.get(key));
				req1.put("bizType", key);
				req1.put("descp", key+" db");
				req1.put("gdbVersion", gdbVersion);
				info1.setRequest(req1);
				AbstractJob job1 = JobCreateStrategy.createAsMethod(info1);
				job1.run();
				if(job1.getJobInfo().getStatus()!=3){
					String msg = (job1.getException()==null)?"未知错误。":"错误："+job1.getException().getMessage();
					throw new Exception("创建出品库过程中job内部发生"+msg);
				}
				int dbDay = job1.getJobInfo().getResponse().getInt("outDbId");
				response.put(key, dbDay);
				JobInfo info2 = new JobInfo(0,"");
				info2.setType("gdbFullCopy");
				JSONObject req2 = new JSONObject();
				req2.put("sourceDbId", fmgdb.getDbId());
//				req2.put("sourceDbId", 43);
				req2.put("targetDbId", dbDay);
				req2.put("gdbVersion", gdbVersion);
				req2.put("featureType", GlmTable.FEATURE_TYPE_ALL);
				info2.setRequest(req2);
				AbstractJob job2 = JobCreateStrategy.createAsMethod(info2);
				job2.run();
				if(job2.getJobInfo().getStatus()!=3){
					String msg = (job2.getException()==null)?"未知错误。":"错误："+job2.getException().getMessage();
					throw new Exception("出品库复制数据过程中job内部发生"+msg);
				}
				log.debug(key+"End......");
			}
			response.put("msg", "执行成功");
		} catch (Exception e) {
			response.put("msg", "ERROR:" + e.getMessage());
			throw e;
		}
		return response;
	}

}
