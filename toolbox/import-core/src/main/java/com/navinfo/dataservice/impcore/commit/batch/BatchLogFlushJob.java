package com.navinfo.dataservice.impcore.commit.batch;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.api.datahub.iface.DatahubApi;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Region;
import com.navinfo.dataservice.bizcommons.glm.Glm;
import com.navinfo.dataservice.bizcommons.glm.GlmTable;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.impcore.flushbylog.FlushResult;
import com.navinfo.dataservice.impcore.flushbylog.LogFlushUtil;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;

/*
 * @author MaYunFei
 * 2016年6月21日
 * 描述：import-coreBatchLogFlushJob.java
 */
public class BatchLogFlushJob extends AbstractJob {

	public BatchLogFlushJob(JobInfo jobInfo) {
		super(jobInfo);
	}

	@Override
	public void execute() throws JobException {
		HashMap<String,FlushResult> jobResponse = new HashMap<String,FlushResult> ();
		try{
			BatchLogFlushJobRequest req = (BatchLogFlushJobRequest)this.request;
			int batchDbId = req.getBatchDbId();
			DatahubApi databhubApi = (DatahubApi) ApplicationContextUtil.getBean("datahubApi");
			DbInfo sourceDbInfo = databhubApi.getDbById(batchDbId);
			DbInfo targetDbInfo = databhubApi.getDbById(req.getTargetDbId());
			this.log.info("开始进行批处理刷库（源库:"+sourceDbInfo+",目标库："+targetDbInfo+")");
			
			//获取目标库对应的大区信息
			ManApi manApi = (ManApi) ApplicationContextUtil.getBean("manApi");
			Region regionInfo = manApi.queryRegionByDbId(req.getTargetDbId());
			
			LogFlushUtil logUtils = LogFlushUtil.getInstance();
			Connection sourceDbConn = logUtils.intiConenction(sourceDbInfo, true);
			Connection targetDbConn =logUtils.intiConenction(targetDbInfo, true);
			String tempTable = logUtils.createTempTable(sourceDbConn);//创建临时表，
			logUtils.createTargetDbLink(sourceDbInfo, targetDbInfo);//创建指向目标库的dblink
			this.response("数据库初始化完毕", jobResponse);
			
			List<Object> prepareParaValues = new ArrayList<Object>();
			Glm glm = new Glm(sourceDbInfo.getGdbVersion());
			List ixTables = ListUtils.union(glm.getEditTableNames(GlmTable.FEATURE_TYPE_POI), glm.getExtendTableNames(GlmTable.FEATURE_TYPE_POI));
			List rdTables = ListUtils.union(glm.getEditTableNames(GlmTable.FEATURE_TYPE_ROAD), glm.getExtendTableNames(GlmTable.FEATURE_TYPE_ROAD));
			SqlClause preparaSqlClause = getPrepareSqlClause(sourceDbConn,tempTable,req.getGrids(),ixTables,rdTables);
			logUtils.prepareAndLockLog(sourceDbConn, 
					preparaSqlClause.getSql(), 
					preparaSqlClause.getValues() ,
					false,//不阔履历 
					null,//无扩履历sql 
					false, //不锁定日志
					null);
			this.response("履历准备、履历加锁完毕", jobResponse);
			
			this.response("执行完毕", jobResponse);
		}catch(Exception e){
			throw new JobException(e);
		}

	}
	private  SqlClause getPrepareSqlClause(Connection conn,String tempTable,List<Integer> grids,List<String> ixTables,List<String> rdTables) throws Exception{
		SqlClause gridSqlClause = genInClauseWithMulInt(conn, grids,"GRID_ID");
		SqlClause ixTablesSqlClause = genInClauseWithMulString(conn, ixTables,"TB_NM");
		SqlClause rdTablesSqlClause = genInClauseWithMulString(conn, rdTables,"TB_NM");
		
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO ");
		sb.append(tempTable);
		sb.append("select l.op_id,l.op_dt  from log_operation l ,log_detail d ,log_detail_grid g ,ix_poi  p ,poi_edit_status s\r\n" + 
					"where l.op_id = d.op_id and d.row_id = g.log_row_id\r\n" + 
					"and "+(gridSqlClause==null?"1=1":gridSqlClause.getSql())+"\r\n" + 
					"and "+(ixTablesSqlClause==null?"1=1":ixTablesSqlClause.getSql())+"\r\n" + 
					"and d.row_id = p.row_id\r\n" + 
					"and p.pid = s.pid\r\n" + 
					"and s.status=2\r\n") ;
		sb.append(" union all\r\n");
		sb.append("select l.op_id,l.op_dt  from log_operation l ,log_detail d ,log_detail_grid g ,ix_poi  p \r\n" + 
				"where l.op_id = d.op_id and d.row_id = g.log_row_id\r\n" + 
				"and "+(gridSqlClause==null?"1=1":gridSqlClause.getSql())+"\r\n" + 
				"and "+(rdTablesSqlClause==null?"1=1":rdTablesSqlClause.getSql())+"\r\n" + 
				"and d.row_id = p.row_id");
		List<Object> ixSqlValue = ListUtils.union(gridSqlClause.getValues(), ixTablesSqlClause.getValues());
		List<Object> rdSqlValue = ListUtils.union(gridSqlClause.getValues(), rdTablesSqlClause.getValues());
		return new  SqlClause(sb.toString(),ListUtils.union(ixSqlValue,rdSqlValue));
	}
	private SqlClause genInClauseWithMulInt(Connection conn,List<Integer> inValues,String column) throws SQLException{
		String gridInClause=null;
		List<Object> prepareParaValues = new ArrayList<Object>();
		if(CollectionUtils.isNotEmpty(inValues)){
			if(inValues.size()>1000){
				Clob clobGrids = conn.createClob();
				clobGrids.setString(1, StringUtils.join(inValues, ","));
				gridInClause = column+"  IN (select column_value from table(clob_to_table(?)))";
				prepareParaValues.add(clobGrids);
			}else{
				gridInClause = column+"  IN ("+StringUtils.join(inValues, ",")+")";
			}
			return new SqlClause(gridInClause,prepareParaValues);
		}
		return null;
		
	}
	private SqlClause genInClauseWithMulString(Connection conn,List<String> inValues,String column) throws SQLException{
		String ixTablesInClause = null;
		List<Object> prepareParaValues = new ArrayList<Object>();
		if(CollectionUtils.isNotEmpty(inValues)){
			if(inValues.size()>1000){
				Clob clobTables = conn.createClob();
				clobTables.setString(1, StringUtils.join(inValues, ","));
				ixTablesInClause = column+"  IN (select column_value from table(clob_to_table(?)))";
				prepareParaValues.add(clobTables);
			}else{
				ixTablesInClause = column+"  IN ('"+StringUtils.join(inValues, "','")+"')";
			}
			return new SqlClause(ixTablesInClause,prepareParaValues);
		}
		return null;
		
	}
	class SqlClause{
		private String sql;
		private List<Object> values;
		public String getSql() {
			return sql;
		}
		public List<Object> getValues() {
			return values;
		}
		public SqlClause(String sql, List<Object> values) {
			super();
			this.sql = sql;
			this.values = values;
		}
		
	}

}

