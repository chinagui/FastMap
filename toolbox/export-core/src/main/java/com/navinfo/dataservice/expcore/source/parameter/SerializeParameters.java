package com.navinfo.dataservice.expcore.source.parameter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.WKT;
import oracle.sql.STRUCT;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;

import com.navinfo.dataservice.expcore.ExportConfig;
import com.navinfo.dataservice.commons.database.oracle.MyDriverManagerConnectionWrapper;
import com.navinfo.dataservice.commons.database.oracle.MyPoolGuardConnectionWrapper;
import com.navinfo.dataservice.commons.database.oracle.MyPoolableConnection;
import com.navinfo.navicommons.database.sql.ProcedureBase;
import com.navinfo.navicommons.database.sql.PackageExec;

/**
 * Created by IntelliJ IDEA. User: liuqing Date: 11-11-18 Time: 下午3:30
 * 序列化导出参数，通过数据表存储参数，使得扩展性能更强
 */
public class SerializeParameters {
	/**
	 * @param ds
	 * @param params
	 * @param suffix
	 */
	public void serialize(DataSource ds, Map<String,List<String>> params, String suffix,String versionCode) throws Exception {
		if (params == null || params.isEmpty())
			return;
		Connection conn = null;
		PreparedStatement ps = null;
		String sql = "insert into TEMP_EXP_PARAMTERS_" + suffix + "(param_value,area,param_name) values(?,?,?)";
		String sql2 = "delete from TEMP_EXP_PARAMTERS_" + suffix;
		String sql3 = "BEGIN PK_TABLE_STATS.gather('TEMP_EXP_PARAMTERS_"+suffix+"'); END;";
		String tableName = "TEMP_EXP_PARAMTERS_" + suffix ;
		try {
			conn = ds.getConnection();
			ps = conn.prepareStatement(sql);
			Object[] oracleDescriptors = null;
			Connection delegateConn = null;
			if (conn instanceof MyDriverManagerConnectionWrapper) {
				delegateConn = ((MyDriverManagerConnectionWrapper) conn).getDelegate();
				oracleDescriptors = JGeometry.getOracleDescriptors(delegateConn);
			} else if (conn instanceof MyPoolGuardConnectionWrapper) {
				delegateConn = ((MyPoolGuardConnectionWrapper) conn).getDelegate();
				if (delegateConn instanceof MyPoolableConnection) {
					delegateConn = ((MyPoolableConnection) delegateConn).getDelegate();
				}
				oracleDescriptors = JGeometry.getOracleDescriptors(delegateConn);
			}

			QueryRunner runner = new QueryRunner();
			runner.update(conn, sql2);
			boolean needCreateSpatialIndex=false;
			for (String key:params.keySet()) {
				List<String> paramSet = params.get(key);
				if (paramSet == null || paramSet.isEmpty())
					continue;
				if(key.equals(ExportConfig.CONDITION_BY_MESH)){
					for(String mesh:paramSet){
						ps.setString(1, mesh);
						ps.setNull(2, Types.STRUCT, "MDSYS.SDO_GEOMETRY");
						ps.setString(3, key);
						ps.addBatch();
					}
				}else if(key.equals(ExportConfig.CONDITION_BY_AREA)||key.equals(ExportConfig.CONDITION_BY_POLYGON)){
					needCreateSpatialIndex=true;
					//理论上area不会很多，也不允许很多
					for(String param:paramSet){
						JGeometry jGeometry = new WKT().toJGeometry(param.getBytes());
						jGeometry.setSRID(8307);
						ps.setInt(1, 0);
						STRUCT struct = JGeometry.store(jGeometry, delegateConn, oracleDescriptors);
						ps.setObject(2, struct);
						ps.setString(3, key);
						ps.addBatch();
					}
				}
			}
			ps.executeBatch();
			ProcedureBase procedureBase = new ProcedureBase(conn);
            procedureBase.callProcedure(sql3);
            
            if(needCreateSpatialIndex){
				String mergePkg = "/com/navinfo/dataservice/expcore/resources/" + versionCode + "/scripts/create_spatial_utils.pck";
				PackageExec mergePkgExec = new PackageExec(conn);
				mergePkgExec.execute(mergePkg);
				procedureBase.callProcedure("{call SPATIAL_UTILS.DROP_SPATIAL_INDEX(?)}",tableName);
				procedureBase.callProcedure("{call SPATIAL_UTILS.CREATE_SPATIAL_INDEX(?,?)}",tableName,"AREA");
            }
		} catch (Exception e) {
			conn.rollback();
			throw e;
		} finally {
			DbUtils.closeQuietly(ps);
			DbUtils.commitAndCloseQuietly(conn);
		}

	}
}
