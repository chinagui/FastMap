package com.navinfo.dataservice.engine.statics;


import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.springframework.context.annotation.CommonAnnotationBeanPostProcessor;

import com.alibaba.dubbo.rpc.Result;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.engine.statics.overview.OverviewBlockMain;
import com.navinfo.dataservice.engine.statics.overview.OverviewMain;
import com.navinfo.dataservice.engine.statics.overview.OverviewSubtaskMain;
import com.navinfo.dataservice.engine.statics.overview.OverviewTaskMain;
import com.navinfo.dataservice.engine.statics.tools.MongoDao;
import com.navinfo.dataservice.engine.statics.tools.StatInit;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.sql.StringUtil;

public class Import2Oracle {
	private static Logger log = LogManager.getLogger(Import2Oracle.class);

	public Import2Oracle() {
		StatInit.initDatahubDb();
	}
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {	
		System.out.println("start import2Oracle");
		Import2Oracle importObj=new Import2Oracle();
		System.out.println("start import2Oracle subtask");
		importObj.import2OracleByTableName(OverviewSubtaskMain.col_name_subtask);
		System.out.println("start import2Oracle blockMan");
		importObj.import2OracleByTableName(OverviewBlockMain.col_name_blockman);	
		System.out.println("start import2Oracle task");
		importObj.import2OracleByTableName(OverviewTaskMain.col_name_task);
		System.out.println("start import2Oracle overview");
		importObj.import2OracleByTableName(OverviewMain.col_name_overview_main);
		System.out.println("end import2Oracle");
		System.exit(0);
	}
	
	public void import2OracleByTableName(String tableName) throws Exception{
		Connection conn=null;
		try{
			conn=DBConnector.getInstance().getManConnection();
			List<List<String>> result=getTableColumns(conn,tableName);
			//获取mongo数据
			String statTime = new SimpleDateFormat("yyyyMMdd").format(new Date());
			MongoDao md = new MongoDao(StatMain.db_name);
			MongoCursor<Document> iter = md
					.find(tableName, Filters.in("statDate", statTime))
					.iterator();
			List<Object[]> mongoValues=new ArrayList<Object[]>();
			String columnStr="";
			String columnStrWenHao="";
			boolean flag=true;
			while (iter.hasNext()) {
				JSONObject json = JSONObject.fromObject(iter.next());
				System.out.println(json);
				Object[] value=new Object[result.size()];
				for(int i=0;i<result.size();i++){
					List<String> columnName=result.get(i);
					//月编任务统计接口用了该字段，暂时留用，不做处理
					if("NUMBER".equals(columnName.get(1))){						
						String ObjectName=StringUtil.getObjectName(columnName.get(0));
						try{
							value[i]=json.getInt(ObjectName);}
						catch(Exception e){
							log.warn("数字型字段值域错误，columnName="+ObjectName,e);
							value[i]=0;
						}
					}else if ("VARCHAR2".equals(columnName.get(1))){
						String ObjectName=StringUtil.getObjectName(columnName.get(0));
						value[i]=json.getString(ObjectName);
					}else if (columnName.get(1).length()>9 && "TIMESTAMP".equals(columnName.get(1).substring(0, 9))){
						String ObjectName=StringUtil.getObjectName(columnName.get(0));
						SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");  
				        Calendar cal = Calendar.getInstance();   
				        String timeStr=json.getString(ObjectName);
				        if(null!=timeStr && !timeStr.isEmpty() && !timeStr.equals("null")){cal.setTime(sdf.parse(timeStr));}
				        Timestamp timestamp=new Timestamp(cal.getTimeInMillis());
						value[i]=timestamp;
					}else if ("CLOB".equals(columnName.get(1))){
						if(!"".isEmpty()){value[i]="";}else{
						String ObjectName=StringUtil.getObjectName(columnName.get(0));
						Clob clobs = ConnectionUtil.createClob(conn);
						clobs.setString(1, json.getString(ObjectName));
						value[i]=clobs;}
					}else{
						if(!columnStr.isEmpty()){
						String ObjectName=StringUtil.getObjectName(columnName.get(0));
						value[i]=json.get(ObjectName);
					}}
					if(flag){
						if(!columnStr.isEmpty()){columnStr=columnStr+",";columnStrWenHao=columnStrWenHao+",";}
						columnStr=columnStr+columnName.get(0);
						columnStrWenHao=columnStrWenHao+"?";
						}
				}
				flag=false;
				mongoValues.add(value);
			}
			
			String insertSql="insert into "+tableName+" ("+columnStr+")"
					+ " values ("+columnStrWenHao+")";
			QueryRunner run=new QueryRunner();
			Object[][] valueList=new Object[mongoValues.size()][result.size()];
			for(int i=0;i<mongoValues.size();i++){
				//run.update(conn, insertSql,mongoValues.get(i));
				valueList[i]=mongoValues.get(i);
			}
			String dropSql="truncate table "+tableName;
			
			run.execute(conn, dropSql);
			run.batch(conn, insertSql,valueList);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	private List<List<String>> getTableColumns(Connection conn,String tableName) throws SQLException{
		//获取表字段
		String getTableColumsSql="SELECT COLUMN_NAME,DATA_TYPE FROM USER_TAB_COLUMNS WHERE TABLE_NAME = '"+tableName.toUpperCase()+"'";
		ResultSetHandler<List<List<String>>> rsHandler=new ResultSetHandler<List<List<String>>>() {

			@Override
			public List<List<String>> handle(ResultSet rs) throws SQLException {
				List<List<String>> result=new ArrayList<List<String>>();
				while(rs.next()){
					List<String> tmp=new ArrayList<String>();
					tmp.add(rs.getString("COLUMN_NAME"));
					tmp.add(rs.getString("DATA_TYPE"));
					result.add(tmp);
				}
				return result;
			}
			
		};
		QueryRunner run=new QueryRunner();
		List<List<String>> result=run.query(conn, getTableColumsSql,rsHandler);
		return result;
	}

}
