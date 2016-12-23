package com.navinfo.dataservice.expcore.job;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.api.datahub.iface.DatahubApi;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.thread.ThreadLocalContext;
import com.navinfo.dataservice.expcore.ExportConfig;
import com.navinfo.dataservice.expcore.input.OracleInput;
import com.navinfo.dataservice.expcore.output.Oracle2OracleDataOutput;
import com.navinfo.dataservice.expcore.sql.ExecuteSql;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.navicommons.geo.computation.MeshUtils;

/** 
* @ClassName: GdbExportJob 
* @author Xiao Xiaowen 
* @date 2016年6月14日 上午9:48:03 
* @Description: TODO
*  
*/
public class GdbExportJob extends AbstractJob {

	public GdbExportJob(JobInfo jobInfo) {
		super(jobInfo);
	}

	@Override
	public void execute() throws JobException {
		OracleInput input = null;
		try{
			GdbExportJobRequest req = (GdbExportJobRequest)request;
			//1. 导出源预处理
			DatahubApi datahub = (DatahubApi)ApplicationContextUtil.getBean("datahubApi");
			DbInfo sourceDb = datahub.getDbById(req.getSourceDbId());
			OracleSchema sourceSchema = new OracleSchema(DbConnectConfig.createConnectConfig(sourceDb.getConnectParam()));
			//如果condition是mesh,处理扩圈
			Set<String> coreMeshes = null;
			Set<String> allMeshes =null;
			if(req.getCondition().equals(ExportConfig.CONDITION_BY_MESH)
					&&req.getMeshExtendCount()>0){
				coreMeshes = new HashSet<String>(req.getConditionParams());
				allMeshes = MeshUtils.getNeighborMeshSet(coreMeshes,req.getMeshExtendCount());
				input = new OracleInput(sourceSchema,req.getFeatureType()
						,req.getCondition(),new ArrayList<String>(allMeshes),req.getGdbVersion());
			}else{
				input = new OracleInput(sourceSchema,req.getFeatureType()
						,req.getCondition(),req.getConditionParams(),req.getGdbVersion());
			}
			input.initSource();
			input.serializeParameters();
			input.loadScripts();
			response("导出源预处理完成",null);
			//2.导出目标预处理
			DbInfo targetDb = datahub.getDbById(req.getTargetDbId());
			OracleSchema targetSchema = new OracleSchema(DbConnectConfig.createConnectConfig(targetDb.getConnectParam()));
			ThreadLocalContext ctx = new ThreadLocalContext(log);
			Oracle2OracleDataOutput output = new Oracle2OracleDataOutput(targetSchema,req.getCheckExistTables(),req.getWhenExist(),req.getTableReNames(),ctx);
			response("导出目标预处理完成",null);
			//3.执行导出脚本
			ExecuteSql exportSqlExecutor = new ExecuteSql(input,output,req.getMode(),req.isDataIntegrity(),req.isMultiThread4Input(),req.isMultiThread4Output());
			exportSqlExecutor.execute();
			response("导出脚本执行完成",null);
			//4. 维护M_MESH_TYPE
			if(req.getCondition().equals(ExportConfig.CONDITION_BY_MESH)){
				if(req.getMeshExtendCount()>0){
					allMeshes.removeAll(coreMeshes);
					writeMeshType(targetSchema,coreMeshes,allMeshes);
					jobInfo.getResponse().put("extendMeshes", allMeshes);
				}else{
					writeMeshType(targetSchema,req.getConditionParams(),null);
					jobInfo.getResponse().put("extendMeshes", new String[]{});
				}
			}
		}catch(Exception e){
			log.error(e.getMessage(),e);
			throw new JobException("job执行过程出错："+e.getMessage(),e);
		}finally{
			//释放临时表资源
			if(input!=null){
				input.releaseSource();
			}
		}
	}
	
	private void writeMeshType(OracleSchema targetSchema, Collection<String> coreMeshes,Collection<String> extendMeshes)throws SQLException{
		Connection conn = null;
		PreparedStatement stmt = null;
		try{
			conn = targetSchema.getPoolDataSource().getConnection();
			String sqlMesh = "INSERT INTO M_MESH_TYPE(MESH_ID,\"TYPE\")VALUES(?,?)";
			stmt = conn.prepareStatement(sqlMesh);
			for(String mesh:coreMeshes){
				stmt.setInt(1, Integer.valueOf(mesh));
				stmt.setInt(2, 1);
				stmt.addBatch();
			}
			if(extendMeshes!=null){
				for(String mesh:extendMeshes){
					stmt.setInt(1, Integer.valueOf(mesh));
					stmt.setInt(2, 2);
					stmt.addBatch();
				}
			}
			stmt.executeBatch();
			stmt.clearBatch();
			conn.commit();
		}catch(SQLException e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error("导出结束，但维护M_MESH_TYPE表发生错误，"+e.getMessage(),e);
			throw e;
		}finally{
			DbUtils.closeQuietly(stmt);
			DbUtils.closeQuietly(conn);
		}
		
	}

}
