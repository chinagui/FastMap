package com.navinfo.dataservice.engine.statics;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.bson.Document;
import org.springframework.context.annotation.CommonAnnotationBeanPostProcessor;

import com.alibaba.dubbo.rpc.Result;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.engine.statics.overview.OverviewSubtaskMain;
import com.navinfo.dataservice.engine.statics.tools.MongoDao;
import com.navinfo.dataservice.engine.statics.tools.StatInit;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.sql.StringUtil;

public class Import2Oracle {

	public Import2Oracle() {
		StatInit.initDatahubDb();
	}
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {	
		System.out.println("start import2Oracle");
		String subtaskName=OverviewSubtaskMain.col_name_subtask;
		Import2Oracle importObj=new Import2Oracle();
		importObj.import2OracleByTableName(subtaskName);
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
			while (iter.hasNext()) {
				JSONObject json = JSONObject.fromObject(iter.next());
				Object[] value=new Object[result.size()];
				for(int i=0;i<result.size();i++){
					List<String> columnName=result.get(i);
					if("NUMBER".equals(columnName.get(1))){
						if(!columnStr.isEmpty()){columnStr=columnStr+",";columnStrWenHao=columnStrWenHao+",";}
						columnStr=columnStr+columnName.get(0);
						columnStrWenHao=columnStrWenHao+"?";
						String ObjectName=StringUtil.getObjectName(columnName.get(0));
						value[i]=json.getInt(ObjectName);
					}else if ("VARCHAR2".equals(columnName.get(1))){
						if(!columnStr.isEmpty()){columnStr=columnStr+",";columnStrWenHao=columnStrWenHao+",";}
						columnStr=columnStr+columnName.get(0);
						columnStrWenHao=columnStrWenHao+"?";
						String ObjectName=StringUtil.getObjectName(columnName.get(0));
						value[i]=json.getString(ObjectName);
					}else{
						if(!columnStr.isEmpty()){columnStr=columnStr+",";columnStrWenHao=columnStrWenHao+",";}
						columnStr=columnStr+columnName.get(0);
						columnStrWenHao=columnStrWenHao+"?";
						String ObjectName=StringUtil.getObjectName(columnName.get(0));
						value[i]=json.get(ObjectName);
					}
				}
				mongoValues.add(value);
			}
			
			String insertSql="insert into "+tableName+" ("+columnStr+")"
					+ " values ("+columnStrWenHao+")";
			Object[][] valueList=new Object[mongoValues.size()][result.size()];
			for(int i=0;i<mongoValues.size();i++){
				valueList[i]=mongoValues.get(i);
			}
			String dropSql="truncate table "+tableName;
			QueryRunner run=new QueryRunner();
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
