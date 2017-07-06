package com.navinfo.dataservice.control.row.quality;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.navicommons.database.QueryRunner;
import com.vividsolutions.jts.geom.Geometry;

import oracle.sql.STRUCT;

public class PoiQuality {
	private static final Logger logger = Logger.getLogger(PoiQuality.class);

	public void initQualityData(int qualityId, int dbId) throws Exception {

		Connection conn = null;
		try {
			
		}catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	public Geometry getGeometryByQualityId(int qualityId) throws Exception {
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			
			QueryRunner run = new QueryRunner();
			String sql = "SELECT GEOMETRY FROM SUBTASK_QUALITY WHERE QUALITY_ID  = "+qualityId;
			ResultSetHandler<Geometry> rs = new ResultSetHandler<Geometry>(){
				public Geometry handle(ResultSet rs) throws SQLException {
					if(rs.next()){
						try {
							return GeoTranslator.struct2Jts((STRUCT) rs.getObject("GEOMETRY"), 0.00001, 5);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					return null;
				}
			};
			return run.query(conn, sql, rs);
		}catch(Exception e){
			logger.error(e.getMessage(), e);
			throw e;
		}finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

}
