package com.navinfo.dataservice.diff.scanner;


import java.math.BigDecimal;
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
import com.navinfo.dataservice.datahub.glm.GlmCache;
import com.navinfo.dataservice.datahub.glm.GlmColumn;
import com.navinfo.dataservice.datahub.glm.GlmTable;
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
public class FillLogMeshDetail implements ResultSetHandler<String> {
	protected Logger log = Logger.getLogger(this.getClass());
	private GlmTable table;
    protected OracleSchema diffServer;
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public FillLogMeshDetail(GlmTable table,OracleSchema diffServer){
		this.table=table;
		this.diffServer=diffServer;
	}

	@Override
	public String handle(ResultSet rs) throws SQLException {
//		ResultSetMetaData mData = rs.getMetaData();
//		List<ColumnMetaData> tmdList = DataBaseUtils.getTableMetaData(table.getName(), mData);
		String updateSql = "UPDATE LOG_DETAIL SET OB_PID=?,MESH_ID=? WHERE TB_ROW_ID=? AND TB_NM='"+table.getName()+"'";

		Connection conn = null;
		PreparedStatement stmt = null;
		try{
			conn = diffServer.getPoolDataSource().getConnection();
			stmt = conn.prepareStatement(updateSql);
			int batchCount=0;
			while(rs.next()){
				long pid = rs.getLong("PID");
				long meshId = rs.getLong("MESH_ID");
				String tb_row_id = rs.getString("ROW_ID");
			    stmt.setLong(1, pid);
			    stmt.setLong(2, meshId);
			    stmt.setString(3, tb_row_id);
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
