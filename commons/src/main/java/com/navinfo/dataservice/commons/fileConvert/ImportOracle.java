package com.navinfo.dataservice.commons.fileConvert;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import oracle.sql.STRUCT;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.navicommons.database.QueryRunner;

public class ImportOracle {
	private static Logger log = LogManager.getLogger(ImportOracle.class);

	public ImportOracle() {
		// TODO Auto-generated constructor stub
	}
	
	public static void writeOracle(Connection conn,String oracleTableName,List<Map<String, Object>> dataList,List<String> tableColumns,List<String>dataColumns) throws Exception{
		try{
			log.info("start writeOracle,oracleTableName="+oracleTableName);
			ImportOracle importOracle =new ImportOracle();
			List<List<String>> result=importOracle.getTableColumns(conn,oracleTableName);
			List<Object[]> dataValues=new ArrayList<Object[]>();
			String columnStr="";
			String columnStrWenHao="";
			boolean flag=true;
			//按照oracleTableName表的字段，查询对应的value。需要保证dataList中map的key所存的字段名与数据库表名一致
			for (Map<String, Object> dataMap:dataList) {
				Object[] value=new Object[result.size()];
				for(int i=0;i<result.size();i++){
					List<String> columnName=result.get(i);
					String ObjectName=columnName.get(0);
					try{
						if(ObjectName.equals("GEOMETRY")){
							STRUCT struct = GeoTranslator.wkt2Struct(conn, (String) dataMap.get(ObjectName));
							value[i]=struct;
						}else{value[i]=dataMap.get(ObjectName);}
						if(flag){
							if(!columnStr.isEmpty()){columnStr=columnStr+",";columnStrWenHao=columnStrWenHao+",";}
							columnStr=columnStr+columnName.get(0);
							columnStrWenHao=columnStrWenHao+"?";}
					}
					catch(Exception e){
						log.warn("没有该列columnName="+ObjectName,e);
						value[i]=null;
					}
				}
				flag=false;
				dataValues.add(value);
				break;
			}
				
			String insertSql="insert into "+oracleTableName+" ("+columnStr+")"
					+ " values ("+columnStrWenHao+")";
			QueryRunner run=new QueryRunner();
			Object[][] valueList=new Object[dataValues.size()][result.size()];
			for(int i=0;i<dataValues.size();i++){
				valueList[i]=dataValues.get(i);
			}
			String dropSql="truncate table "+oracleTableName;
			run.update(conn, dropSql);
			run.batch(conn, insertSql,valueList);
			log.info("end writeOracle,oracleTableName="+oracleTableName);
		}catch (Exception e){
			log.error("fail writeOracle 入库失败",e);
			DbUtils.rollbackAndCloseQuietly(conn);
			throw e;
		}
	}

	private List<List<String>> getTableColumns(Connection conn,String tableName) throws SQLException{
		log.info("start 获取表字段列表,oracleTableName="+tableName);
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
		log.info("end 获取表字段列表,oracleTableName="+tableName);
		return result;
	}
}
