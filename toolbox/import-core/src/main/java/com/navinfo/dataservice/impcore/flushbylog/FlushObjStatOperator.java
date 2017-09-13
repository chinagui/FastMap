package com.navinfo.dataservice.impcore.flushbylog;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;

/**
 * 
 * @ClassName FlushObjStat
 * @author Han Shaoming
 * @date 2017年9月7日 下午2:19:55
 * @Description TODO
 */
public class FlushObjStatOperator {
	
	private static Logger log = LoggerRepos.getLogger(FlushObjStatOperator.class);
	
	/**
	 * 进行对象级履历的统计
	 * @author Han Shaoming
	 * @param conn
	 * @param kindCode
	 * @return
	 * @throws ServiceException
	 */
	public static Map<String,Integer> getTotalObjStatByLog(Connection conn,String tempOpTable) throws Exception{
		try{
			StringBuilder sql = new StringBuilder();
			
			sql.append("WITH T AS(SELECT DISTINCT D.OB_PID,D.OB_NM ");
			sql.append(" FROM "+tempOpTable+" P,LOG_DETAIL D WHERE P.OP_ID = D.OP_ID)");
			sql.append(" SELECT T.OB_NM , COUNT(1) SUM FROM T GROUP BY T.OB_NM");
			
			ResultSetHandler<Map<String,Integer>> rsHandler = new ResultSetHandler<Map<String,Integer>>() {
				public Map<String,Integer> handle(ResultSet rs) throws SQLException {
					Map<String,Integer> result = new HashMap<String,Integer>();
					while (rs.next()) {
						String objName = rs.getString("OB_NM");
						int sum = rs.getInt("SUM");
						result.put(objName, sum);
					}
					return result;
				}
			};
			
			log.info("getTotalObjStatByLog查询sql："+sql.toString());
			Map<String, Integer> result = new QueryRunner().query(conn,sql.toString(), rsHandler);
			return result;
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败，原因为:"+e.getMessage(),e);
		}
	}
}
