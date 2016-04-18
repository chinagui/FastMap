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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import net.sf.json.JSONObject;

import oracle.sql.CLOB;
import oracle.sql.STRUCT;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.datahub.glm.GlmCache;
import com.navinfo.dataservice.datahub.glm.GlmColumn;
import com.navinfo.dataservice.datahub.glm.GlmTable;
import com.navinfo.dataservice.datahub.model.OracleSchema;
import com.navinfo.navicommons.database.ColumnMetaData;
import com.navinfo.navicommons.database.DataBaseUtils;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.navinfo.navicommons.geo.SpatialAdapters;

/** 
 * @ClassName: FillLeftUpdateLogDetail 
 * @author Xiao Xiaowen 
 * @date 2016-1-14 下午2:54:40 
 * @Description: TODO
 */
public class FillLeftUpdateLogDetail implements ResultSetHandler<String> {
	protected Logger log = Logger.getLogger(this.getClass());
	private GlmTable table;
    protected OracleSchema diffServer;
	private QueryRunner runner = new QueryRunner();
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public FillLeftUpdateLogDetail(GlmTable table,OracleSchema diffServer){
		this.table=table;
		this.diffServer=diffServer;
	}

	@Override
	public String handle(ResultSet rs) throws SQLException {
		//ResultSetMetaData mData = rs.getMetaData();
		//List<ColumnMetaData> tmdList = DataBaseUtils.getTableMetaData(table.getName(), mData);
//		int tmdSize = tmdList.size();
//		if(tmdSize%3!=0){
//			throw new SQLException("填充时查询sql出错");
//		}
//		int colSize = tmdSize/3;
		List<GlmColumn> cols = table.getColumns();
		int colsSize = cols.size();
		String updateSql = "UPDATE LOG_DETAIL SET MESH_ID=?,\"NEW\"=?,\"OLD\"=?,FD_LST=? WHERE TB_ROW_ID=?";

		Connection conn = null;
		PreparedStatement stmt = null;
		try{
			conn = diffServer.getPoolDataSource().getConnection();
			stmt = conn.prepareStatement(updateSql);
			int batchCount=0;
			while(rs.next()){
				BigDecimal meshId = null;
				String tb_row_id = null;
				JSONObject jsonLeft = new JSONObject();
				JSONObject jsonRight = new JSONObject();
				List<String> fdLst = new ArrayList<String>();
			    for(int i=1;i<=cols.size();i++){
			    	GlmColumn col = cols.get(i-1);

		    		//mesh_id
		    		if("MESH_ID".equals(col.getName())){
		    			meshId = (BigDecimal)rs.getObject(i+colsSize);
		    		}else if("ROW_ID".equals((col.getName()))){
		    			tb_row_id = rs.getString(i+colsSize);
			    	}
			    	//前1/3的字段为比较值，1/3到2/3的字段为左表的全部字段，2/3到3/3为右表的全部字段
			    	//==0则左右表值不同
			    	if(rs.getInt(i)==0){
			    		//fd_lst
			    		fdLst.add(col.getName());
			    		//new,old
			    		Object valueLeft = rs.getObject(i+colsSize);
			    		Object valueRight = rs.getObject(i+colsSize+colsSize);
			    		if(col.isGeometryColumn()){
			    			String wktLeft = SpatialAdapters.struct2Wkt((STRUCT)valueLeft);
			    			String wktRight = SpatialAdapters.struct2Wkt((STRUCT)valueRight);
			    			jsonLeft.put(col.getName(), wktLeft);
			    			jsonRight.put(col.getName(), wktRight);
			    		}else if(col.isClobColumn()){
			    			String clobStrLeft = DataBaseUtils.clob2String((CLOB)valueLeft);
			    			String clobStrRight = DataBaseUtils.clob2String((CLOB)valueRight);
			    			jsonLeft.put(col.getName(), clobStrLeft);
			    			jsonRight.put(col.getName(), clobStrRight);
			    		}else if(col.isDateColumn()||col.isTimestampColumn()){
			    			String dateStrLeft = sdf.format((Date)valueLeft);
			    			String dateStrRight = sdf.format((Date)valueRight);
			    			jsonLeft.put(col.getName(), dateStrLeft);
			    			jsonRight.put(col.getName(), dateStrRight);
			    		}else{
			    			jsonLeft.put(col.getName(), valueLeft);
			    			jsonRight.put(col.getName(), valueRight);
			    		}
			    	}
			    }
			    if(meshId!=null){
			    	stmt.setBigDecimal(1, meshId);
			    }else{
			    	stmt.setNull(1, Types.INTEGER);
			    }
				Clob clobLeft = conn.createClob();
				clobLeft.setString(1, jsonLeft.toString());
				Clob clobRight = conn.createClob();
				clobRight.setString(1, jsonRight.toString());
			    stmt.setClob(2, clobLeft);
			    stmt.setClob(3, clobRight);
			    stmt.setString(4, StringUtils.join(fdLst,","));
			    stmt.setString(5, tb_row_id);
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
			throw new SQLException("填充修改类型的履历出现错误。",e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
		return null;
	}

}
