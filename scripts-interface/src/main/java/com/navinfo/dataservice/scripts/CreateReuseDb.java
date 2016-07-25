package com.navinfo.dataservice.scripts;



import net.sf.json.JSONObject;

import java.sql.Connection;

import org.apache.commons.dbutils.DbUtils;
import org.springframework.util.Assert;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.database.DbServerType;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.dataservice.jobframework.runjob.JobCreateStrategy;
import com.navinfo.navicommons.database.QueryRunner;

/**
 * @ClassName: InitProjectScriptsInterface
 * @author Xiao Xiaowen
 * @date 2016-1-15 下午3:40:32
 * @Description: TODO
 */
public class CreateReuseDb {

	public static JSONObject execute(JSONObject request) throws Exception{
		JSONObject response = new JSONObject();
		Connection conn=null;
		try {
			String gdbVersion = SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion);
			Assert.notNull(gdbVersion, "gdbVersion不能为空,检查是否sys_config表中未配置当前gdb版本");
			int count = request.getInt("count");
			if(count<1){
				throw new Exception("count必须大于0");
			}
			String bizType = (String) request.get("bizType");
			Assert.notNull(bizType, "bizType不能为空");
			
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			String sql = "INSERT INTO DB_REUSE(DB_ID,USING_STATUS) VALUES(?,0)";
			QueryRunner run = new QueryRunner();
			for(int i=0;i<count;i++){
				//创建job
				JobInfo info1 = new JobInfo(0, "");
				info1.setType("createDb");
				JSONObject req1 = new JSONObject();
				req1.put("serverType", DbServerType.TYPE_ORACLE);
				req1.put("bizType", bizType);
				req1.put("descp", "pre-created db");
				info1.setRequest(req1);
				AbstractJob job1 = JobCreateStrategy.createAsMethod(info1);
				job1.run();
				if(job1.getJobInfo().getResponse().getInt("exeStatus")!=3){
					throw new Exception("job1执行失败");
				}
				int dbId = job1.getJobInfo().getResponse().getInt("outDbId");
				run.update(conn, sql, dbId);
				response.put("db_"+i, dbId);
			}
			response.put("msg", "执行成功");
			return response;
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			response.put("msg", "ERROR:" + e.getMessage());
			throw e;
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
}
