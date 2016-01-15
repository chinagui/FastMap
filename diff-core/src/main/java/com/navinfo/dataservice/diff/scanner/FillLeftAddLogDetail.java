package com.navinfo.dataservice.diff.scanner;


import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import net.sf.json.JSONObject;

import oracle.sql.CLOB;
import oracle.sql.STRUCT;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.datahub.model.OracleSchema;
import com.navinfo.dataservice.diff.config.DiffTableCache;
import com.navinfo.dataservice.diff.config.Table;
import com.navinfo.navicommons.database.ColumnMetaData;
import com.navinfo.navicommons.database.DataBaseUtils;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.navinfo.navicommons.geo.SpatialAdapters;
import com.navinfo.navicommons.utils.StringUtils;

/** 
 * @ClassName: FillLeftAddLogDetail 
 * @author Xiao Xiaowen 
 * @date 2016-1-14 下午2:54:40 
 * @Description: TODO
 */
public class FillLeftAddLogDetail implements ResultSetHandler<String> {
	protected Logger log = Logger.getLogger(this.getClass());
	private Table table;
    protected OracleSchema diffServer;
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public FillLeftAddLogDetail(Table table,OracleSchema diffServer){
		this.table=table;
		this.diffServer=diffServer;
	}

	@Override
	public String handle(ResultSet rs) throws SQLException {
		ResultSetMetaData mData = rs.getMetaData();
		List<ColumnMetaData> tmdList = DataBaseUtils.getTableMetaData(table.getName(), mData);
		String updateSql = "UPDATE LOG_DETAIL SET MESH_ID=?,\"NEW\"=? WHERE ROW_ID=?";
		Connection conn = null;
		PreparedStatement stmt = null;
		try{
			conn = diffServer.getPoolDataSource().getConnection();
			stmt = conn.prepareStatement(updateSql);
			int batchCount=0;
			while(rs.next()){
				int meshId = 0;
				JSONObject json = new JSONObject();
			    for(int i=0;i<tmdList.size();i++){
			    	ColumnMetaData tmd = tmdList.get(i);
			    	String name = tmd.getColumnName();
					Object value = rs.getObject(name);
					//获取mesh_id
			    	if("MESH_ID".equals(name)){
			    		meshId = (int)value;
			    	}
			    	//获取new json
					if(value!=null){
						if(tmd.isGeometryColumn()){
							STRUCT geom = (STRUCT)value;
							try{
								String wkt = SpatialAdapters.struct2Wkt(geom);
								json.put(name,wkt);
							}catch(Exception e){
								log.error(e.getMessage(),e);
								throw new SQLException("Geometry字段转换成wkt出错。"+e.getMessage(),e);
							}
						}else if(tmd.isClobColumn()){
							CLOB clob = (CLOB)value;
							String clobStr = DataBaseUtils.clob2String(clob);
							json.put(name, clobStr);
						}else if(tmd.isDateColumn()){
							Date date = (Date)value;
							String dateStr = sdf.format(date);
							json.put(name, dateStr);
						}else{
							json.put(name, value);
						}
					}else{
						json.put(name, null);
					}
			    }
			    if(meshId>0){
			    	stmt.setInt(1, meshId);
			    }else{
			    	stmt.setNull(1, Types.INTEGER);
			    }
				Clob clob = conn.createClob();
				clob.setString(1, json.toString());
			    stmt.setClob(2, clob);
			    stmt.addBatch();
			    batchCount++;
			    if (batchCount % 1000 == 0) {
					stmt.executeBatch();
					stmt.clearBatch();
				}
			}
			//剩余不到1000的执行掉
			stmt.executeBatch();
			stmt.clearBatch();
		}catch(Exception e){
			log.error(e.getMessage(),e);
			DbUtils.rollbackAndCloseQuietly(conn);
			throw e;
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
		return null;
	}

}
