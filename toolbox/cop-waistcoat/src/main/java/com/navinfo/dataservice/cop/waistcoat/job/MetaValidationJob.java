package com.navinfo.dataservice.cop.waistcoat.job;

import java.sql.Connection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.sql.DataSource;
import org.apache.commons.dbutils.DbUtils;
import com.navinfo.dataservice.api.datahub.iface.DatahubApi;
import com.navinfo.dataservice.api.datahub.model.BizType;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.bizcommons.datarow.CkResultTool;
import com.navinfo.dataservice.bizcommons.datarow.PhysicalDeleteRow;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.dataservice.jobframework.runjob.JobCreateStrategy;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.sql.DbLinkCreator;
import com.navinfo.navicommons.database.sql.PackageExec;
import com.navinfo.navicommons.database.sql.SqlExec;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
* @ClassName: GdbValidationJob 
* @author zl 
* @date  2017年4月16日  
* @Description: TODO
*  
*/
public class MetaValidationJob extends AbstractJob {

	public MetaValidationJob(JobInfo jobInfo) {
		super(jobInfo);
	}

	@Override
	public void execute() throws JobException {
		MetaValidationJobRequest req = (MetaValidationJobRequest)request;
		System.out.println(" begin MetaValidationJob  ");
		try {
			// 1. 创建检查子版本库
			OracleSchema valSchema = null;
			DatahubApi datahub = (DatahubApi)ApplicationContextUtil.getBean("datahubApi");
			int valDbId = 0;
			DbInfo valDb = datahub.getReuseDb(BizType.DB_COP_VERSION);
			if(req.getSubJobRequest("createValDb")!=null){
				JobInfo createValDbJobInfo = new JobInfo(jobInfo.getId(), jobInfo.getGuid());
				AbstractJob createValDbJob = JobCreateStrategy.createAsSubJob(createValDbJobInfo,
						req.getSubJobRequest("createValDb"), this);
				createValDbJob.run();
				if (createValDbJob.getJobInfo().getStatus() != 3) {
					String msg = (createValDbJob.getException()==null)?"未知错误。":"错误："+createValDbJob.getException().getMessage();
					throw new Exception("创建检查子版本库时job内部发生"+msg);
				}
				//新建子版本库的dbid
				valDbId = createValDbJob.getJobInfo().getResponse().getInt("outDbId");
				System.out.println("新建子版本库的dbid :" +valDbId);
				//根据返回的自版本库的dbid 获取子版本库的DbInfo
				valDb = datahub.getDbById(valDbId);
			}else{
				throw new Exception("未设置创建检查子版本库request参数。");
			}
			jobInfo.addResponse("valDbId", valDbId);
			valSchema = new OracleSchema(
					DbConnectConfig.createConnectConfig(valDb.getConnectParam()));
			
			//给检查子版本库导数据
			//获取元数据库的dbID
			DbInfo metaDb =  datahub.getOnlyDbByType("metaRoad");
			installPckUtils(req.getName(),req.getNameGroupid(),req.getAdminId(),req.getRoadTypes(),metaDb,valDb);	
				
			/*//System.out.println("metaDbid: "+metaDbid);
			JSONObject metaValidationRequestJSON=new JSONObject();
			metaValidationRequestJSON.put("executeDBId", metaDbid);//元数据库dbId
			metaValidationRequestJSON.put("kdbDBId", metaDbid);//元数据库dbId
			metaValidationRequestJSON.put("ruleIds", ruleList);
			metaValidationRequestJSON.put("timeOut", 600);
			jobId=apiService.createJob("checkCore", metaValidationRequestJSON, userId,subtaskId, jobName);*/
			// 2. 在检查子版本上执行检查
			System.out.println("在检查子版本上执行检查");
			req.getSubJobRequest("val").setAttrValue("executeDBId", valDbId);//子版本元数据库的dbid
			req.getSubJobRequest("val").setAttrValue("ruleIds", req.getRules());
			req.getSubJobRequest("val").setAttrValue("kdbDBId", valDbId);//子版本元数据库的dbid
			req.getSubJobRequest("val").setAttrValue("timeOut", 600);
			JobInfo valJobInfo = new JobInfo(jobInfo.getId(),jobInfo.getGuid());
			AbstractJob valJob = JobCreateStrategy.createAsSubJob(valJobInfo, req.getSubJobRequest("val"), this);
			valJob.run();
			if(valJob.getJobInfo().getStatus()!=3){
				String msg = (valJob.getException()==null)?"未知错误。":"错误："+valJob.getException().getMessage();
				throw new Exception("检查job内部发生"+msg);
			}
			// 3. 检查结果搬迁
			System.out.println("检查结果搬迁");
			CkResultTool.generateCkMd5(valSchema);
			CkResultTool.generateCkResultObject(valSchema);
			CkResultTool.generateCkResultGrid(valSchema);
			DbInfo tarDb = datahub.getDbById(metaDb.getDbId());
			OracleSchema tarSchema = new OracleSchema(
					DbConnectConfig.createConnectConfig(tarDb.getConnectParam()));
			CkResultTool.moveNiVal(valSchema, tarSchema, null);
			response("检查生成的检查结果后处理及搬迁完毕。",null);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new JobException(e.getMessage(), e);
		}
	}
	
	
	/**
	 * @Title: installPckUtils
	 * @Description: 初始化子版本元数据库
	 * @param name
	 * @param nameGroupid
	 * @param adminId
	 * @param roadTypes
	 * @param rules
	 * @param metaDb
	 * @param subMetaDb
	 * @throws Exception  void
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年4月18日 下午3:21:23 
	 */
	private void installPckUtils(String name, String nameGroupid, String adminId, String roadTypes,
			DbInfo metaDb,DbInfo subMetaDb) throws Exception {
	
		Connection conn = null;
		try{
			//子版本元数据库的connConfig
			DbConnectConfig connConfig = DbConnectConfig.createConnectConfig(subMetaDb.getConnectParam());
			//创建元数据库dblink  DBLINK_SMM
			createMetaDbLink(MultiDataSourceFactory.getInstance().getDataSource(connConfig),metaDb);

			
			conn = MultiDataSourceFactory.getInstance().getDataSource(connConfig).getConnection();
			//修改log_action默认值
//			new QueryRunner().execute(conn, "ALTER TABLE LOG_ACTION MODIFY SRC_DB DEFAULT "+dbType);
			//
			SqlExec sqlExec = new SqlExec(conn);
			//创建 子版本元数据库中的表
			String sqlFile = "/com/navinfo/dataservice/scripts/resources/meta_cop_init.sql";
			sqlExec.executeIgnoreError(sqlFile);
			//创建 exception 依赖表的表结构
			String exceptionSqlFile = "/com/navinfo/dataservice/scripts/resources/meta_exception_table_init.sql";
			sqlExec.executeIgnoreError(exceptionSqlFile);
			
			//为子版本库导入数据
			StringBuilder insertRdnameDataSql = new StringBuilder();
				insertRdnameDataSql.append( " insert into rd_name select * from rd_name@DBLINK_SMM where 1=1 ");
			// 添加过滤器条件
				if(name != null && StringUtils.isNotEmpty(name) && !name.equals("null")){
					insertRdnameDataSql.append(" and name like '%");
					insertRdnameDataSql.append(name);
					insertRdnameDataSql.append("%'");
				}
				if(nameGroupid != null && StringUtils.isNotEmpty(nameGroupid) && !nameGroupid.equals("null")){
					insertRdnameDataSql.append(" and name_groupid = ");
					insertRdnameDataSql.append(nameGroupid);
					insertRdnameDataSql.append(" ");
				}
				if(adminId != null && StringUtils.isNotEmpty(adminId) && !adminId.equals("null")){
					insertRdnameDataSql.append(" and admin_id = ");
					insertRdnameDataSql.append(adminId);
					insertRdnameDataSql.append(" ");
				}
				if(roadTypes != null && StringUtils.isNotEmpty(roadTypes) && !roadTypes.equals("null")){
					insertRdnameDataSql.append(" and road_type ");
					insertRdnameDataSql.append("  in( ");
					insertRdnameDataSql.append(roadTypes);
					insertRdnameDataSql.append(") ");
				}
			//向子版本导入查询出的rd_name 表数据					
			new QueryRunner().execute(conn,insertRdnameDataSql.toString());
			
			//向子版本导入其他依赖表数据	
			String insertOtherTable = "/com/navinfo/dataservice/scripts/resources/meta_table_insert.sql";
			sqlExec.executeIgnoreError(sqlFile);
			
			conn.commit();
		}finally{
			DbUtils.closeQuietly(conn);
		}
	}
	private static void createMetaDbLink(DataSource dataSource,DbInfo metaDb)throws Exception{
		DbLinkCreator cr = new DbLinkCreator();
		cr.create("DBLINK_SMM", false, dataSource,metaDb.getDbUserName(),metaDb.getDbUserPasswd(),metaDb.getDbServer().getIp(),String.valueOf(metaDb.getDbServer().getPort()),metaDb.getDbServer().getServiceName());
	}

}
