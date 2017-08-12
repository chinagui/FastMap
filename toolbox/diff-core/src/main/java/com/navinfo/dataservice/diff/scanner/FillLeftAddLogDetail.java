package com.navinfo.dataservice.diff.scanner;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


import net.sf.json.JSONObject;

import oracle.sql.CLOB;
import oracle.sql.STRUCT;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.glm.GlmColumn;
import com.navinfo.dataservice.bizcommons.glm.GlmTable;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.navicommons.database.DataBaseUtils;
import com.navinfo.navicommons.geo.SpatialAdapters;

/** 
 * @ClassName: FillLeftAddLogDetail 
 * @author Xiao Xiaowen 
 * @date 2016-1-14 下午2:54:40 
 * @Description: TODO
 */
public class FillLeftAddLogDetail implements ResultSetHandler<String> {
	protected Logger log = Logger.getLogger(this.getClass());
	private GlmTable table;
    protected OracleSchema diffServer;
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public FillLeftAddLogDetail(GlmTable table,OracleSchema diffServer){
		this.table=table;
		this.diffServer=diffServer;
	}

	@Override
	public String handle(ResultSet rs) throws SQLException {
//		ResultSetMetaData mData = rs.getMetaData();
//		List<ColumnMetaData> tmdList = DataBaseUtils.getTableMetaData(table.getName(), mData);
		String updateSql = "UPDATE LOG_DETAIL SET \"NEW\"=? WHERE TB_ROW_ID=HEXTORAW(?) AND TB_NM='"+table.getName()+"'";

		Connection conn = null;
		PreparedStatement stmt = null;
		try{
			conn = diffServer.getPoolDataSource().getConnection();
			stmt = conn.prepareStatement(updateSql);
			int batchCount=0;
			List<GlmColumn> cols = table.getColumns();
			while(rs.next()){
				String tb_row_id = null;
				JSONObject json = new JSONObject();
			    for(GlmColumn col:cols){
			    	String name = col.getName();
					Object value = rs.getObject(name);
					if("ROW_ID".equals(name)){
			    		tb_row_id = rs.getString(name);
			    		json.put(name, tb_row_id);
			    	}else{
				    	//获取new json
						if(value!=null){
							if(col.isGeometryColumn()){
								STRUCT geom = (STRUCT)value;
								try{
									String wkt = SpatialAdapters.struct2Wkt(geom);
									json.put(name,wkt);
								}catch(Exception e){
									log.error(e.getMessage(),e);
									throw new SQLException("Geometry字段转换成wkt出错。"+e.getMessage(),e);
								}
							}else if(col.isClobColumn()){
								CLOB clob = (CLOB)value;
								String clobStr = DataBaseUtils.clob2String(clob);
								json.put(name, clobStr);
							}else if(col.isDateColumn()||col.isTimestampColumn()){
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
			    }
				Clob clob = conn.createClob();
				clob.setString(1, json.toString());
			    stmt.setClob(1, clob);
			    stmt.setString(2, tb_row_id);
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
		}catch(SQLException e){
			log.error(e.getMessage(),e);
			DbUtils.rollbackAndCloseQuietly(conn);
			throw e;
		}finally{
			DbUtils.closeQuietly(stmt);
			DbUtils.commitAndCloseQuietly(conn);
		}
		return null;
	}

}

