package com.navinfo.dataservice.datahub.glm;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.sql.RunnableSQL;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;


/** 
* @ClassName: GlmGridCalculator 
* @author Xiao Xiaowen 
* @date 2016年4月13日 下午4:25:39 
* @Description: TODO
*/
public class GlmGridCalculator {

	protected Logger log = LoggerRepos.getLogger(this.getClass());
	public GlmGridCalculator(String gdbVersion,GlmGridCalculatorLock lock){
		this.gdbVersion=gdbVersion;
	}
	private String gdbVersion;
	private Map<String,GlmGridRefInfo> glmGridRefInfoMap = null;//key:表名，value：表配置信息
	private QueryRunner run = new QueryRunner();
	private Map<String,GlmGridRefInfo> getGlmGridRefInfoMap(){
		if(glmGridRefInfoMap==null){
			synchronized(this){
				if(glmGridRefInfoMap==null){
					glmGridRefInfoMap = loadGlmGridRefInfoMap();
				}
			}
		}
		return glmGridRefInfoMap;
	}
	private Map<String,GlmGridRefInfo> loadGlmGridRefInfoMap(){
		Map<String,GlmGridRefInfo> map = new HashMap<String,GlmGridRefInfo>();
		Connection conn = null;
		try{
			conn = MultiDataSourceFactory.getInstance().getManDataSource().getConnection();
			QueryRunner runner = new QueryRunner();
			String sql = "SELECT TABLE_NAME,REF_COL,REF_INFO";
			//...
		}catch(Exception e){
			
		}
		return map;
	}
	public GlmGridRefInfo getGlmGridRefInfo(String tableName){
		return getGlmGridRefInfoMap().get(tableName);
	}
	/**
	 * 给定表的row_id查询所属grid
	 * @param tableName
	 * @param rowIds
	 * @param dataConn：数据所在库的连接
	 * @return:key-value:key-rowId,value-grid号码字符串数组
	 */
	public String[] calc(String tableName,String rowId,Connection dataConn){
		//...
		return null;
	}
	/**
	 * 给定表的row_id查询所属grid
	 * @param tableName
	 * @param rowIds
	 * @param dataConn：数据所在库的连接
	 * @return:key-value:key-rowId,value-grid号码字符串数组
	 */
	public Map<String,String[]> calc(String tableName,Set<String> rowIds,Connection dataConn){
		//...
		return null;
	}
	/**
	 * 给定履历表的过滤条件，通过履历表查询grid号码
	 * 数据和履历在同一个库
	 * @param tableName
	 * @param filterSql:除了默认的L.TB_NM=tableName,自定义添加的其他过滤条件SQL子句，字段前的L.需要加上
	 * @param logConn：数据和履历所在库的连接
	 * @return
	 */
	public Map<String,String[]> calc(String tableName,int[] opTypes,Connection logConn)throws SQLException{
		String sql = assembleQueryGeoSql(tableName);
		Map<String,String[]> grids = run.query(logConn, sql, new ResultSetHandler<Map<String,String[]>>(){

			@Override
			public Map<String, String[]> handle(ResultSet rs) throws SQLException {
				Map<String,String[]> gs = new HashMap<String,String[]>();
				while(rs.next()){
					String rowId = rs.getString("ROW_ID");
					JGeometry geom = JGeometry.load((STRUCT)(rs.getObject("GEOMETRY")));
					String[] rowGrids = null;
					int meshId = rs.getInt("MESH_ID");
					if(meshId>0){
					}
					
					
				}
				return gs;
			}
			
		});
		return grids;
	}
	/**
	 * 给定履历表的过滤条件，通过履历表查询grid号码
	 * 数据和履历不在同一个库，通过连接到履历查询
	 * 若remoteType=CROSS_USER，那么需要履历所在库和cross user库在同一台服务器上
	 * 若remoteType=DB_LINK,，那么前提是履历所在库已经存在指向参考的数据库的database link
	 * @param tableName
	 * @param filterSql：除了默认的L.TB_NM=tableName,自定义添加的其他过滤条件SQL子句，字段前的L.需要加上
	 * @param logConn:履历库所在库的连接
	 * @param remoteType：CROSS_USER/DB_LINK
	 * @param remoteParam:cross user name/database link name
	 * @return
	 * @throws SQLException
	 */
	public Map<String,String[]> calc(String tableName,int[] opTypes,Connection logConn,String remoteType,String remoteParam)throws SQLException{
		Map<String,String[]> grids = new HashMap<String,String[]>();
		return grids;
	}

	/**
	 * 
	 * @param type:rowid/log
	 * @return
	 */
	private String assembleQueryGeoSql(String tableName,String rowId){
		StringBuilder sb = new StringBuilder();
		GlmGridRefInfo refInfo = glmGridRefInfoMap.get(tableName);
		sb.append(refInfo.getSelectSqlPart());
		sb.append(refInfo.getConditionSqlPart());
		sb.append(" AND P.ROW_ID = HEXTORAW('");
		sb.append(rowId);
		sb.append("')");
		return sb.toString();
		
	}
	/**
	 * 
	 * @param type:rowid/log
	 * @return
	 */
	private String assembleQueryGeoSql(String tableName,Set<String> rowIds){
		//...
		return null;
		
	}
	private String assembleQueryGeoSql(String tableName){
		StringBuilder sb = new StringBuilder();
		GlmGridRefInfo refInfo = glmGridRefInfoMap.get(tableName);
		sb.append(refInfo.getSelectSqlPart());
		sb.append(",LOG_DETAIL L ");
		sb.append(refInfo.getConditionSqlPart());
		sb.append(" AND P.ROW_ID=L.TB_ROW_ID AND L.TB_NM='"+tableName+"'");
		return sb.toString();
	}
	private String assembleQueryGeoSql_Dblink(String tableName,String dbLinkName){
		StringBuilder sb = new StringBuilder();
		GlmGridRefInfo refInfo = glmGridRefInfoMap.get(tableName);
		sb.append(refInfo.replaceSelectSqlPartByDbLink(dbLinkName));
		sb.append(",LOG_DETAIL L ");
		sb.append(refInfo.getConditionSqlPart());
		sb.append(" AND P.ROW_ID=L.TB_ROW_ID AND L.TB_NM='"+tableName+"'");
		return sb.toString();
	}
	private String assembleQueryGeoSqlByCrossUser(String tableName,String crossUserName){
		StringBuilder sb = new StringBuilder();
		GlmGridRefInfo refInfo = glmGridRefInfoMap.get(tableName);
		sb.append(refInfo.replaceSelectSqlPartByCrossUser(crossUserName));
		sb.append(",LOG_DETAIL L ");
		sb.append(refInfo.getConditionSqlPart());
		sb.append(" AND P.ROW_ID=L.TB_ROW_ID AND L.TB_NM='"+tableName+"'");
		return sb.toString();
	}
	public static void main(String[] args){
	}
	
}
