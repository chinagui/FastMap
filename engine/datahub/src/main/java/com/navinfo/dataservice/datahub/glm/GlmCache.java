package com.navinfo.dataservice.datahub.glm;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.datahub.service.DbService;
import com.navinfo.navicommons.database.QueryRunner;


/** 
 * @ClassName: GlmCache 
 * @author Xiao Xiaowen 
 * @date 2016-1-12 下午2:41:28 
 * @Description: TODO
 */
public class GlmCache {
	protected Logger log = Logger.getLogger(this.getClass());
	private static class SingletonHolder{
		private static final GlmCache INSTANCE = new GlmCache();
	}
	public static GlmCache getInstance(){
		return SingletonHolder.INSTANCE;
	}
	private Map<String,Glm> cache = new HashMap<String,Glm>();
	private GlmCache(){
		
	}
	public Glm getGlm(String gdbVersion){
		Glm glm = cache.get(gdbVersion);
		if(glm==null){
			synchronized(this){
				glm = cache.get(gdbVersion);
				if(glm==null){
					glm = loadGlm(gdbVersion);
					cache.put(gdbVersion, glm);
				}
			}
		}
		return glm;
	}
	private Glm loadGlm(String gdbVerison){
		Glm glm = new Glm(gdbVerison);
		Connection manConn = null;
		Connection conn = null;
		try{
			QueryRunner runner = new QueryRunner();
			//load tables
			manConn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			//1. 先确定加载哪些表
			String ignore = SystemConfigFactory.getSystemConfig().getValue("glm.ignore.table.prefix");
			StringBuilder loadSql = new StringBuilder();
			loadSql.append("SELECT TABLE_NAME,EDITABLE FROM GLM_TABLE WHERE GDB_VERSION=?");
			if(StringUtils.isNotEmpty(ignore)){
				Set<String> ignoreSqlSet = new HashSet<String>();
				for(String prefix:ignore.split(",")){
					ignoreSqlSet.add("TABLE_NAME NOT LIKE '"+prefix+"%'");
				}
				loadSql.append(" AND ");
				loadSql.append(StringUtils.join(ignoreSqlSet," AND "));
			}
			Map<String,Integer> names = runner.query(manConn, loadSql.toString(), new ResultSetHandler<Map<String,Integer>>(){
				@Override
				public Map<String,Integer> handle(ResultSet rs)throws SQLException{
					Map<String,Integer> set = new HashMap<String,Integer>();;
					while(rs.next()){
						String tableName = rs.getString("TABLE_NAME");
					
						set.put(rs.getString("TABLE_NAME"),rs.getInt("EDITABLE"));
					}
					return set;
				}
			},gdbVerison);
			if(names.size()<1){
				log.warn("******注意******");
				log.warn("Glm缓存过程出现错误，无法获取glm,请检查问题来源。");
				log.warn("错误来源:从管理库中未读取到模型版本为"+gdbVerison+"的表配置信息。");
			}else{
				StringBuilder tableSql = new StringBuilder();
				tableSql.append("SELECT T.TABLE_NAME, C.COLUMN_NAME, C.DATA_TYPE,C.COLUMN_ID FROM USER_TABLES T, USER_TAB_COLUMNS C WHERE T.TABLE_NAME = C.TABLE_NAME");
				tableSql.append(" AND T.TABLE_NAME IN ('");
				tableSql.append(StringUtils.join(names.keySet(),"','"));
				tableSql.append("') ORDER BY T.TABLE_NAME,C.COLUMN_ID");
				DbInfo db = DbService.getInstance().getOnlyDbByType("nationRoad");
				conn = MultiDataSourceFactory.getInstance().getDataSource(db.getConnectParam()).getConnection();
				Map<String,GlmTable> tables = runner.query(conn, tableSql.toString(), new ResultSetHandler<Map<String,GlmTable>>(){
					@Override
					public Map<String,GlmTable> handle(ResultSet rs)throws SQLException{
						Map<String,GlmTable> map = new HashMap<String,GlmTable>();
						while(rs.next()){
							String tableName = rs.getString("TABLE_NAME");
							GlmTable table = map.get(tableName);
							if(table==null){
								table = new GlmTable(tableName);
								table.setColumns(new ArrayList<GlmColumn>());
								map.put(tableName, table);
							}
							List<GlmColumn> cols = table.getColumns();
							String colName = rs.getString("COLUMN_NAME");
							GlmColumn col = new GlmColumn(colName);
							col.setDataType(rs.getString("DATA_TYPE"));
							cols.add(col);
							
						}
						return map;
					}
				});
				//赋可编辑状态
				for(String key:tables.keySet()){
					GlmTable table = tables.get(key);
					table.setEditable((names.get(key)==1?true:false));
				}
				//load pks
				StringBuilder pkSql = new StringBuilder();
				pkSql.append("SELECT T.TABLE_NAME,T.COLUMN_NAME FROM USER_CONS_COLUMNS T,USER_CONSTRAINTS P WHERE T.CONSTRAINT_NAME=P.CONSTRAINT_NAME AND P.CONSTRAINT_TYPE='P' AND T.TABLE_NAME IN('");
				pkSql.append(StringUtils.join(tables.keySet(),"','"));
				pkSql.append("')");
				runner.query(conn, pkSql.toString(),new GetPkHandler(tables));
				//批找图幅号的外键或者参考字段
				//...
				glm.setAllTables(tables);
			}
		}catch (Exception e) {
			log.error(e.getMessage(), e);
			log.warn("******注意******");
			log.warn("Glm缓存过程出现错误，无法获取glm,请检查问题来源。");
		} finally {
			DbUtils.closeQuietly(manConn);
			DbUtils.closeQuietly(conn);
		}
		return glm;
	}

	class GetPkHandler implements ResultSetHandler<String>{
		Map<String,GlmTable> tables;
		GetPkHandler(Map<String,GlmTable> tables){
			this.tables=tables;
		}
		@Override
		public String handle(ResultSet rs)throws SQLException{
			Set<String> hasPkTables = new HashSet<String>();
			while(rs.next()){
				String tableName = rs.getString("TABLE_NAME");
				String colName = rs.getString("COLUMN_NAME");
				GlmTable table = tables.get(tableName);
				GlmColumn col = table.getColumnByName(colName);
				col.setPk(true);
				hasPkTables.add(tableName);
			}
			//将没有主键的表的row_id字段批成主键
			for(String name:tables.keySet()){
				if(!hasPkTables.contains(name)){
					GlmTable table = tables.get(name);
					for(GlmColumn col:table.getColumns()){
						if("ROW_ID".equals(col.getName())){
							col.setPk(true);
							break;
						}
					}
				}
			}
			return null;
		}
	}
	
	public static void main(String[] args){
		Glm glm = GlmCache.getInstance().getGlm("240+");
		Map<String,GlmTable> tables = glm.getEditTables();
		
		for(String name:tables.keySet()){
			System.out.println("INSERT INTO GLM_TABLE VALUES(GLM_TABLE_SEQ.NEXTVAL,'"+name+"',NULL,0,0,'240+');");
//			GlmTable table = tables.get(name);
//			List<GlmColumn> cols = table.getColumns();
//			for(GlmColumn col:cols){
//				System.out.println("--"+col.getName()+":"+col.getDataType()+":"+col.isPk());
//			}
		}
		System.out.println("Over.");
	}
}
