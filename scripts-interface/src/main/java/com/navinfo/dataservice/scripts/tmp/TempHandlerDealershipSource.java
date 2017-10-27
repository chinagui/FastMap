package com.navinfo.dataservice.scripts.tmp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.datahub.service.DbService;
import com.navinfo.dataservice.engine.editplus.diff.FastPoi;
import com.navinfo.dataservice.scripts.JobScriptsInterface;
import com.navinfo.dataservice.scripts.model.IxDealershipSource;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

public class TempHandlerDealershipSource {
	private static Logger log = LogManager.getLogger(TempHandlerDealershipSource.class);
	
	public static Map<Integer, Connection> queryAllRegionConn() throws SQLException {
		log.info("queryAllRegionConn start...");
		Map<Integer,Connection> mapConn = new HashMap<Integer, Connection>();
		String sql = "select t.daily_db_id,region_id from region t";
		log.info("sql:"+sql);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection conn = null;
		try {
				conn = DBConnector.getInstance().getManConnection();
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				while (rs.next()) {
					Connection regionConn = DBConnector.getInstance().getConnectionById(rs.getInt("daily_db_id"));
					mapConn.put(rs.getInt("region_id"), regionConn);
					log.info("大区库region_id:"+rs.getInt("region_id")+"获取数据库连接成功");
				}
				log.info("queryAllRegionConn end...");
				return mapConn;

		} catch (Exception e) {
			for (Connection value : mapConn.values()) {
				DbUtils.rollbackAndCloseQuietly(value);
			}
			throw new SQLException("加载region失败：" + e.getMessage(), e);
		}finally {
			DbUtils.closeQuietly(conn, pstmt, rs);
		}
	}
	public static void updateDealershipSource(Map<Integer, JSONObject>  sourceIdMap, Map<Integer, Connection> dbMap,Connection conn) throws ServiceException {
		QueryRunner run = new QueryRunner();
		try {

			// 更新result表
			String updateSourceSql = "UPDATE ix_dealership_source r SET r.poi_kind_code=?,r.poi_chain=?,r.poi_name=?,r.poi_name_short=?,r.poi_address=?,r.poi_tel=?,"
					+ " r.poi_post_code=?,r.poi_x_display=?,r.poi_y_display=?,r.poi_y_guide=?,r.poi_x_guide=?,r.GEOMETRY=? where r.source_id=?";

			Object[][] param = new Object[sourceIdMap.size()][];
			int i=0;
			for (Map.Entry<Integer, JSONObject> entry : sourceIdMap.entrySet()) {
				JSONObject valueObj=entry.getValue();
				IxDealershipSource dealSource = new IxDealershipSource();
				dealSource.setSourceId(entry.getKey());
				dealSource.setCfmPoiNum(valueObj.getString("cfmPoINum"));
				Connection regionConn = (Connection) dbMap.get(valueObj.getInt("regionId"));
				updateResultObj(dealSource,regionConn);
				log.info("sourceId:" + dealSource.getSourceId() + ",poi_kind_code:" + dealSource.getPoiKindCode() + ",poi_chain:"
						+ dealSource.getPoiChain()+ ",poi_name:" + dealSource.getPoiName() + ",poi_address:"
						+ dealSource.getPoiAddress());

				Object[] obj = new Object[] { dealSource.getPoiKindCode(), dealSource.getPoiChain(),
						dealSource.getPoiName(), dealSource.getPoiNameShort(), dealSource.getPoiAddress(),
						dealSource.getPoiTel(), dealSource.getPoiPostCode(), dealSource.getPoiXDisplay(),
						dealSource.getPoiYDisplay(), dealSource.getPoiXGuide(), dealSource.getPoiYGuide(),GeoTranslator.wkt2Struct(conn,GeoTranslator.jts2Wkt(dealSource.getGeometry())),
						dealSource.getSourceId() };
				param[i] = obj;
				i++;
			}

			int[] rows = null;
			if (param.length != 0 && param[0] != null) {
				rows = run.batch(conn, updateSourceSql, param);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new ServiceException("更新失败:" + e.getMessage(), e);
		} 
	}

	public static FastPoi queryPoiByPoiNum(String cfmNum, Connection conn) throws Exception {
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		FastPoi fastPoi = new FastPoi();

		try {
			StringBuilder sb = new StringBuilder();
			sb.append("WITH A AS ");
			sb.append(" (SELECT I.POI_NUM,");
			sb.append("         I.PID,");
			sb.append("         I.KIND_CODE,");
			sb.append("         I.CHAIN,");
			sb.append("         I.POST_CODE,");
			sb.append("         I.X_GUIDE,");
			sb.append("         I.Y_GUIDE,");
			sb.append("         I.GEOMETRY,");
			sb.append("         P1.NAME OFFICENAME,");
			sb.append("        (SELECT NAME");
			sb.append("            FROM IX_POI_NAME");
			sb.append("           WHERE POI_PID = I.PID");
			sb.append("             AND NAME_CLASS = 3");
			sb.append("             AND NAME_TYPE = 1");
			sb.append("             AND U_RECORD <> 2");
			sb.append("             AND LANG_CODE IN ('CHI', 'CHT')) SHORT_NAME,");
			sb.append("         A.FULLNAME");
			sb.append("    FROM IX_POI I, IX_POI_NAME P1, IX_POI_ADDRESS A");
			sb.append("   WHERE I.POI_NUM = :1");
			sb.append("     AND I.PID = P1.POI_PID");
			sb.append("     AND P1.U_RECORD <> 2");
			sb.append("     AND P1.NAME_CLASS = 1");
			sb.append("     AND P1.NAME_TYPE = 1");
			sb.append("    AND P1.LANG_CODE IN ('CHI', 'CHT')");
			sb.append("     AND I.PID = A.POI_PID");
			sb.append("     AND A.U_RECORD <> 2");
			sb.append("    AND A.LANG_CODE IN ('CHI', 'CHT')),");
			sb.append(" B AS");
			sb.append(" (SELECT C.POI_PID,");
			sb.append("         LISTAGG(C.CONTACT, '|') WITHIN GROUP(ORDER BY C.POI_PID) AS TEL");
			sb.append("    FROM IX_POI_CONTACT C,IX_POI I");
			sb.append("   WHERE  C.POI_PID = I.PID AND I.POI_NUM=:2 ");
			sb.append("      AND C.CONTACT_TYPE IN (1,2,3,4) ");
			sb.append("     AND C.U_RECORD <> 2");
			sb.append("   GROUP BY C.POI_PID)");
			sb.append(" SELECT POI_NUM,");
			sb.append("       PID,");
			sb.append("       KIND_CODE,");
			sb.append("       CHAIN,");
			sb.append("       POST_CODE,");
			sb.append("       X_GUIDE,");
			sb.append("       Y_GUIDE,");
			sb.append("       GEOMETRY,");
			sb.append("       OFFICENAME,");
			sb.append("       SHORT_NAME,");
			sb.append("       FULLNAME,");
			sb.append("       TEL");
			sb.append("  FROM A, B");
			sb.append(" WHERE A.PID = B.POI_PID(+)");
			String str=sb.toString();
			log.info(str);
			pstmt = conn.prepareStatement(str);
			pstmt.setString(1, cfmNum);
			pstmt.setString(2, cfmNum);

			resultSet = pstmt.executeQuery();
			if (resultSet.next()) {
				fastPoi.setAddr(resultSet.getString("FULLNAME") != null ? resultSet.getString("FULLNAME") : "");
				fastPoi.setChain(resultSet.getString("CHAIN") != null ? resultSet.getString("CHAIN") : "");
				fastPoi.setGeometry(GeoTranslator.struct2Jts((STRUCT) resultSet.getObject("GEOMETRY")));
				fastPoi.setKindCode(resultSet.getString("KIND_CODE") != null ? resultSet.getString("KIND_CODE") : "");
				fastPoi.setName(resultSet.getString("OFFICENAME") != null ? resultSet.getString("OFFICENAME") : "");
				fastPoi.setPid(resultSet.getInt("PID"));
				fastPoi.setPoiNum(resultSet.getString("POI_NUM"));
				fastPoi.setPostCode(resultSet.getString("POST_CODE") != null ? resultSet.getString("POST_CODE") : "");
				fastPoi.setShortName(
						resultSet.getString("SHORT_NAME") != null ? resultSet.getString("SHORT_NAME") : "");
				fastPoi.setTel(resultSet.getString("TEL") != null ? resultSet.getString("TEL") : "");
				fastPoi.setxGuide(resultSet.getDouble("X_GUIDE"));
				fastPoi.setyGuide(resultSet.getDouble("Y_GUIDE"));
			}

			return fastPoi;

		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
	}

	/**
	 * 
	 * @param dealershipMR
	 * @param obj
	 * @return 
	 * @return
	 * @throws Exception
	 */
	public static void updateResultObj(IxDealershipSource dealSource, Connection regionConn ) throws Exception {

		FastPoi obj = queryPoiByPoiNum(dealSource.getCfmPoiNum(), regionConn);
		log.info("开始查询cfmPoiNum:"+dealSource.getCfmPoiNum());
			if (obj != null) {
				log.info("poi_name:"+obj.getName());
				dealSource.setPoiKindCode(obj.getKindCode());
				dealSource.setPoiChain(obj.getChain());

				dealSource.setPoiName(obj.getName());
				dealSource.setPoiNameShort(obj.getShortName());
				dealSource.setAddress(obj.getAddr());

				dealSource.setPoiTel(obj.getTel());
				dealSource.setPoiPostCode(obj.getPostCode());
				dealSource.setPoiXGuide(obj.getxGuide());
				dealSource.setPoiYGuide(obj.getyGuide());
					
				if (obj.getGeometry() != null) {
					dealSource.setGeometry(obj.getGeometry());
					JSONArray array = GeoTranslator.jts2JSONArray(obj.getGeometry());
					dealSource.setPoiXDisplay(array.getDouble(0));
					dealSource.setPoiXDisplay(array.getDouble(1));
				}

			}

	}
	
	public static  Map<Integer, JSONObject> querySourceMap(String chain, Connection conn) throws Exception {
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		Map<Integer, JSONObject>  sourceIdMap=new HashMap<Integer, JSONObject>();
		try {
			StringBuilder sb = new StringBuilder();
			sb.append(" WITH A AS ");
			sb.append(" (SELECT T.SOURCE_ID,T.CFM_POI_NUM ");
			sb.append(" FROM IX_DEALERSHIP_SOURCE T ");
			sb.append(" WHERE T.CHAIN = '"+chain+"' ");
			sb.append(" AND T.CFM_POI_NUM IS NOT NULL AND T.POI_CHAIN IS NULL  AND T.POI_NAME IS NULL),");
			sb.append(" B AS "); 
			sb.append(" (SELECT MAX(X.RESULT_ID) OVER(PARTITION BY 1) RESULT_ID, X.SOURCE_ID ");    
			sb.append(" FROM IX_DEALERSHIP_RESULT X ");    
			sb.append(" WHERE X.SOURCE_ID IN (SELECT A.SOURCE_ID FROM A)) ");   
			sb.append(" SELECT B.SOURCE_ID, B.RESULT_ID, X.REGION_ID,A.CFM_POI_NUM ");     
			sb.append(" FROM B, IX_DEALERSHIP_RESULT X,A "); 
			sb.append(" WHERE B.RESULT_ID = X.RESULT_ID  AND B.SOURCE_ID=A.SOURCE_ID"); 
			
			String str=sb.toString();
		    log.info(str);
			pstmt = conn.prepareStatement(str);
			resultSet = pstmt.executeQuery();
			if (resultSet.next()) {
				JSONObject json=new JSONObject();
				 log.info("sourceId:"+resultSet.getInt("source_id")+",resultId:"+resultSet.getInt("result_id")+",regionId:"+resultSet.getInt("region_id"));
				 json.put("regionId", resultSet.getInt("region_id"));
				 json.put("cfmPoINum", resultSet.getString("CFM_POI_NUM"));
				 sourceIdMap.put(resultSet.getInt("source_id"), json);
			}

			return sourceIdMap;

		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		log.info("start");
		JobScriptsInterface.initContext();
		String chains = String.valueOf(args[0]);
//		String chains="4035";
		Connection conn = null;
		Map<Integer,Connection> dbMap=new HashMap<Integer, Connection>();
	  try{
		if (chains==null||chains.isEmpty()){
			log.info("chains is null");
		}else{
			String[] splitChains=chains.split(","); 
			//获取代理店数据库连接
			DbInfo manInfo = DbService.getInstance().getOnlyDbByBizType("dealership");

			OracleSchema manSchema = new OracleSchema(
					DbConnectConfig.createConnectConfig(manInfo.getConnectParam()));
			conn = manSchema.getPoolDataSource().getConnection();
			dbMap=queryAllRegionConn();
			 for(int i = 0; i < splitChains.length; i++){
				 log.info("start chain:"+splitChains[i]);
				 Map<Integer, JSONObject>  sourceIdMap=querySourceMap(splitChains[i],conn);
				 updateDealershipSource(sourceIdMap,dbMap,conn);
				 
			 }
		}
		
	  }catch (Exception e) {
	    log.error(e.getMessage(), e);
		throw new ServiceException("更新失败:" + e.getMessage(), e);}
		finally {
			DbUtils.commitAndCloseQuietly(conn);
			for (Connection value : dbMap.values()) {  
				DbUtils.commitAndCloseQuietly(value);
			}  
	}
	  System.exit(0);
	  log.info("end");
		
	}
}
