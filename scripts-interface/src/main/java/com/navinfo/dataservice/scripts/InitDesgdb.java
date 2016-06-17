package com.navinfo.dataservice.scripts;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;

import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.man.model.Grid;
import com.navinfo.dataservice.bizcommons.glm.GlmTable;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.datahub.service.DbService;
import com.navinfo.dataservice.engine.man.grid.GridService;
import com.navinfo.dataservice.expcore.ExportConfig;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.dataservice.jobframework.runjob.JobCreateStrategy;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.sql.PackageExec;
import com.navinfo.navicommons.database.sql.SqlExec;
import com.navinfo.navicommons.geo.computation.MeshUtils;

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
			String fmgdbId = (String) request.get("fmgdbId");
			Assert.notNull(fmgdbId, "fmgdbId不能为空");
			String gdbVersion = (String) request.get("gdbVersion");
			Assert.notNull(gdbVersion, "gdbVersion不能为空");
			
			//
			String[] names = new String[]{"desgdb_d_p","desgdb_d_rp","desgdb_m"};
			for(String name:names){
				//创建db
				JobInfo info1 = new JobInfo(0, "");
				info1.setType("createDb");
				JSONObject req1 = new JSONObject();
				req1.put("dbName", "orcl");
				req1.put("userName", name);
				req1.put("userPasswd", name);
				req1.put("bizType", "desRoad");
				req1.put("descp", "des db");
				req1.put("gdbVersion", "250+");
				info1.setRequest(req1);
				AbstractJob job1 = JobCreateStrategy.createAsMethod(info1);
				job1.run();
				int dbDay = job1.getJobInfo().getResponse().getInt("outDbId");
				response.put(name, dbDay);
				JobInfo info2 = new JobInfo(0,"");
				info2.setType("gdbFullCopy");
				JSONObject req2 = new JSONObject();
				req2.put("sourceDbId", fmgdbId);
				req2.put("targetDbId", dbDay);
				req2.put("gdbVersion", "250+");
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
