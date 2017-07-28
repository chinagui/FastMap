package com.navinfo.dataservice.control.dealership.service;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.edit.model.IxDealershipResult;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.control.dealership.service.model.ExpClientConfirmResult;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.navicommons.database.QueryRunner;
import com.vividsolutions.jts.geom.Geometry;

import oracle.sql.STRUCT;

public class IxDealershipResultSelector {
	protected static Logger log = LoggerRepos.getLogger(IxDealershipResultSelector.class);

	public static Map<Integer,IxDealershipResult> getByResultIds(Connection conn,Collection<Integer> resultIds)throws Exception{
		if(resultIds==null|resultIds.size()==0)return new HashMap<Integer,IxDealershipResult>();

		if(resultIds.size()>1000){
			String sql= "SELECT * FROM IX_DEALERSHIP_RESULT WHERE RESULT_ID IN (SELECT COLUMN_VALUE FROM TABLE(CLOB_TO_TABLE(?)))";
			Clob clob = ConnectionUtil.createClob(conn);
			clob.setString(1, StringUtils.join(resultIds, ","));
			return new QueryRunner().query(conn, sql, getResultHander(),clob);
		}else{
			String sql= "SELECT * FROM IX_DEALERSHIP_RESULT WHERE RESULT_ID IN ('"+StringUtils.join(resultIds, "','")+"')";
			return new QueryRunner().query(conn,sql,getResultHander());
		}
	}
	/**
	 * key是IxDealershipResult对象的resultId
	 * @return
	 */
	private static ResultSetHandler<Map<Integer, IxDealershipResult>> getResultHander(){
		return new ResultSetHandler<Map<Integer,IxDealershipResult>>() {

			@Override
			public Map<Integer, IxDealershipResult> handle(ResultSet rs)
					throws SQLException {
				Map<Integer, IxDealershipResult> sourceIdMap=new HashMap<Integer, IxDealershipResult>();
				while(rs.next()){
					IxDealershipResult tmp=getBean(rs);
					tmp.setOldValues(null);
					sourceIdMap.put(tmp.getResultId(), tmp);
				}
				return sourceIdMap;
			}
		};
	}
	
	public static Map<Integer,IxDealershipResult> getBySourceIds(Connection conn,Collection<Integer> sourceIds)throws Exception{
		if(sourceIds==null|sourceIds.size()==0)return new HashMap<Integer,IxDealershipResult>();

		if(sourceIds.size()>1000){
			String sql= "SELECT * FROM IX_DEALERSHIP_RESULT WHERE SOURCE_ID IN (SELECT COLUMN_VALUE FROM TABLE(CLOB_TO_TABLE(?)))";
			Clob clob = ConnectionUtil.createClob(conn);
			clob.setString(1, StringUtils.join(sourceIds, ","));
			return new QueryRunner().query(conn, sql, getSourceHander(),clob);
		}else{
			String sql= "SELECT * FROM IX_DEALERSHIP_RESULT WHERE SOURCE_ID IN ('"+StringUtils.join(sourceIds, "','")+"')";
			return new QueryRunner().query(conn,sql,getSourceHander());
		}
	}
	/**
	 * key是IxDealershipResult对象的sourceId
	 * @return
	 */
	private static ResultSetHandler<Map<Integer, IxDealershipResult>> getSourceHander(){
		return new ResultSetHandler<Map<Integer,IxDealershipResult>>() {

			@Override
			public Map<Integer, IxDealershipResult> handle(ResultSet rs)
					throws SQLException {
				Map<Integer, IxDealershipResult> sourceIdMap=new HashMap<Integer, IxDealershipResult>();
				while(rs.next()){
					IxDealershipResult tmp=getBean(rs);
					tmp.setOldValues(null);
					sourceIdMap.put(tmp.getSourceId(), tmp);
				}
				return sourceIdMap;
			}
		};
	}
	
	private static IxDealershipResult getBean(ResultSet rs) throws SQLException{
		IxDealershipResult result=new IxDealershipResult();
		result.setAddress(rs.getString("ADDRESS"));
		result.setAddressEng(rs.getString("ADDRESS_ENG"));
		result.setCfmMemo(rs.getString("CFM_MEMO"));
		result.setCfmPoiNum(rs.getString("CFM_POI_NUM"));
		result.setCfmStatus(rs.getInt("CFM_STATUS"));
		result.setCfmIsAdopted(rs.getInt("CFM_IS_ADOPTED"));
		result.setChain(rs.getString("CHAIN"));
		result.setCity(rs.getString("CITY"));
		result.setDealCfmDate(rs.getString("DEAL_CFM_DATE"));
		result.setDealSrcDiff(rs.getInt("DEAL_SRC_DIFF"));
		result.setDealStatus(rs.getInt("DEAL_STATUS"));
		result.setFbAuditRemark(rs.getString("FB_AUDIT_REMARK"));
		result.setFbContent(rs.getString("FB_CONTENT"));
		result.setFbDate(rs.getString("FB_DATE"));
		result.setFbSource(rs.getInt("FB_SOURCE"));
		STRUCT geoStruct=(STRUCT) rs.getObject("GEOMETRY");
		try {
			result.setGeometry(GeoTranslator.struct2Jts(geoStruct));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		result.setIsDeleted(rs.getInt("IS_DELETED"));
		result.setKindCode(rs.getString("KIND_CODE"));
		result.setMatchMethod(rs.getInt("MATCH_METHOD"));
		result.setName(rs.getString("NAME"));
		result.setNameEng(rs.getString("NAME_ENG"));
		result.setNameShort(rs.getString("NAME_SHORT"));
		result.setPoiAddress(rs.getString("POI_ADDRESS"));
		result.setPoiChain(rs.getString("POI_CHAIN"));
		result.setPoiKindCode(rs.getString("POI_KIND_CODE"));
		result.setPoiName(rs.getString("POI_NAME"));
		result.setPoiNameShort(rs.getString("POI_NAME_SHORT"));
		result.setPoiNum1(rs.getString("POI_NUM_1"));
		result.setPoiNum2(rs.getString("POI_NUM_2"));
		result.setPoiNum3(rs.getString("POI_NUM_3"));
		result.setPoiNum4(rs.getString("POI_NUM_4"));
		result.setPoiNum5(rs.getString("POI_NUM_5"));
		result.setPoiPostCode(rs.getString("POI_POST_CODE"));
		result.setPoiTel(rs.getString("POI_TEL"));
		result.setPoiXDisplay(rs.getInt("POI_X_DISPLAY"));
		result.setPoiXGuide(rs.getInt("POI_X_GUIDE"));
		result.setPoiYDisplay(rs.getInt("POI_Y_DISPLAY"));
		result.setPoiYGuide(rs.getInt("POI_Y_GUIDE"));
		result.setPostCode(rs.getString("POST_CODE"));
		result.setProject(rs.getString("PROJECT"));
		result.setProvideDate(rs.getString("PROVIDE_DATE"));
		result.setProvince(rs.getString("PROVINCE"));
		result.setRegionId(rs.getInt("REGION_ID"));
		result.setResultId(rs.getInt("RESULT_ID"));
		result.setSimilarity(rs.getString("SIMILARITY"));
		result.setSourceId(rs.getInt("SOURCE_ID"));
		result.setTelOther(rs.getString("TEL_OTHER"));
		result.setTelSale(rs.getString("TEL_SALE"));
		result.setTelService(rs.getString("TEL_SERVICE"));
		result.setToClientDate(rs.getString("TO_CLIENT_DATE"));
		result.setToInfoDate(rs.getString("TO_INFO_DATE"));
		result.setUserId(rs.getInt("USER_ID"));
		result.setWorkflowStatus(rs.getInt("WORKFLOW_STATUS"));
		result.setOldValues(null);
		return result;
	}
	
	/**
	 * 根据chain得到待提交差分结果列表
	 * @param chainCode
	 * @param conn
	 * @param userId
	 * @return
	 * @throws Exception 
	 */
	public static List<IxDealershipResult> getResultIdListByChain(String chainCode, Connection conn, long userId) throws Exception {
		String sql = "SELECT RESULT_ID,CFM_POI_NUM,REGION_ID FROM IX_DEALERSHIP_RESULT t"
				+ " WHERE t.CHAIN=:1 AND t.USER_ID=:2 AND t.DEAL_STATUS = 2";

		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		
		 List<IxDealershipResult> resultIdList = new ArrayList<>();
		
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, chainCode);
			pstmt.setLong(2, userId);
			resultSet = pstmt.executeQuery();

			
			while (resultSet.next()) {
				IxDealershipResult result = new IxDealershipResult();
				result.setResultId(resultSet.getInt(1));
				result.setCfmPoiNum(resultSet.getString(2));
				result.setRegionId(resultSet.getInt(3));
				result.setOldValues(null);
				resultIdList.add(result);
			}

			return resultIdList;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
	}
	
	
	/**
	 * 根据resultId主键查询IxDealershipResult
	 * @param resultId
	 * @return
	 * @throws Exception 
	 */
	public static IxDealershipResult getIxDealershipResultById(Integer resultId, Connection conn) throws Exception {
		StringBuffer sb  = new StringBuffer();
		sb.append("SELECT RESULT_ID,WORKFLOW_STATUS,DEAL_STATUS,USER_ID,TO_INFO_DATE,TO_CLIENT_DATE,PROVINCE,CITY,");
		sb.append("PROJECT,KIND_CODE,CHAIN,NAME,NAME_SHORT,ADDRESS,TEL_SALE,TEL_SERVICE,TEL_OTHER,POST_CODE,NAME_ENG, ADDRESS_ENG,");
		sb.append("PROVIDE_DATE,IS_DELETED,MATCH_METHOD,POI_NUM_1,POI_NUM_2,POI_NUM_3,POI_NUM_4,POI_NUM_5,SIMILARITY,FB_SOURCE,");
		sb.append("FB_CONTENT,FB_AUDIT_REMARK,FB_DATE,CFM_STATUS,CFM_POI_NUM,CFM_MEMO,NVL(SOURCE_ID,0) SOURCE_ID,DEAL_SRC_DIFF,");
		sb.append("DEAL_CFM_DATE,POI_KIND_CODE,POI_CHAIN,POI_NAME,POI_NAME_SHORT,POI_ADDRESS,POI_TEL,POI_POST_CODE,POI_X_DISPLAY,");
		sb.append("POI_Y_DISPLAY,POI_X_GUIDE,POI_Y_GUIDE,GEOMETRY,REGION_ID,CFM_IS_ADOPTED FROM IX_DEALERSHIP_RESULT WHERE RESULT_ID=:1");
     		

		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		
		
		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setInt(1, resultId);
			resultSet = pstmt.executeQuery();

			IxDealershipResult result = new IxDealershipResult();
			
			if (resultSet.next()) {
				ReflectionAttrUtils.executeResultSet(result, resultSet);
			}
			result.setOldValues(null);

			return result;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
	}
	
	/**
	 * 更新deal_status
	 * @param resultId
	 * @param conn
	 * @throws Exception
	 */
	public static void updateResultDealStatus(Integer resultId,Integer dealStatus,Connection conn) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(" UPDATE IX_DEALERSHIP_RESULT SET DEAL_STATUS = :1 WHERE RESULT_ID = :2");

		PreparedStatement pstmt = null;
		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setInt(1, dealStatus);
			pstmt.setInt(2, resultId);
			pstmt.executeUpdate();
			
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
	}
	/**
	 * @param conn
	 * @param chain
	 * @return
	 * @throws SQLException 
	 */
	public static Map<Integer, IxDealershipResult> getIxDealershipResultMapByChain(Connection conn, String chain) throws SQLException {
		String sql= "SELECT * FROM IX_DEALERSHIP_RESULT R WHERE R.CHAIN = '" + chain + "'";
		log.info("getIxDealershipResultMapByChain:" + sql);
		return new QueryRunner().query(conn,sql,getSourceHander());		

	}
	
	/**
	 * 通过sql
	 * @param conn
	 * @param sql
	 * @return
	 * @throws Exception
	 */
	public static Map<Integer, IxDealershipResult> getIxDealershipResultsBySql(Connection conn,String sql) throws Exception{
		return new QueryRunner().query(conn, sql, getSourceHander());
	}
	/**
	 * 得到客户确认-待发布中品牌数据
	 * @param chainCode
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public static List<ExpClientConfirmResult> getClientConfirmResultList(String chainCode,Connection conn) throws SQLException {
		QueryRunner run = new QueryRunner();
		ResultSetHandler<List<ExpClientConfirmResult>> rs = null;
		String selectSql = "select r.result_id,r.province , r.city, r.project , r.kind_code, r.chain , r.name ,"
				+ " r.name_short, r.address,r.tel_sale,r.tel_service ,r.tel_other,r.post_code,r.name_eng,r.address_eng,"
				+ " r.cfm_poi_num , r.workflow_status,r.deal_src_diff,r.cfm_memo,r.region_id"
				+ " from IX_DEALERSHIP_RESULT r "
				+ " where r.chain = '"+chainCode+"' and r.workflow_status  = 5 and r.cfm_status = 1";
		log.info("selectSql: "+selectSql);
		rs =  new ResultSetHandler<List<ExpClientConfirmResult>>() {
		@Override
			public List<ExpClientConfirmResult> handle(ResultSet rs) throws SQLException {
				
				List<ExpClientConfirmResult> clientConfirmList = new ArrayList<ExpClientConfirmResult>();
				while (rs.next()) {
					ExpClientConfirmResult result = new ExpClientConfirmResult();
						result.setResultId(rs.getInt("result_id"));
						result.setProvince( rs.getString("province"));
						result.setCity( rs.getString("city"));
						result.setProject( rs.getString("project"));
						result.setKindCode( rs.getString("kind_code"));
						result.setChain( rs.getString("chain"));
						result.setName( rs.getString("name"));
						result.setNameShort( rs.getString("name_short"));
						result.setAddress( rs.getString("address"));
						result.setTelSale( rs.getString("tel_sale"));
						result.setTelService( rs.getString("tel_service"));
						result.setTelOther( rs.getString("tel_other"));
						result.setPostCode( rs.getString("post_code"));
						result.setNameEng( rs.getString("name_eng"));
						result.setAddressEng( rs.getString("address_eng"));
						result.setFid( rs.getString("cfm_poi_num"));
						result.setWorkFlowStatus(rs.getString("workflow_status"));
						result.setDealSrcDiff(rs.getString("deal_src_diff"));
						result.setCfmMemo(rs.getString("cfm_memo"));
						
						clientConfirmList.add(result);
				}
				return clientConfirmList;
			}
		};
			
		return run.query(conn, selectSql, rs);
		
	}
	
	/**
	 * 更新cfm_status状态改为“待确认”即2
	 * @param resultId
	 * @param conn
	 * @throws Exception
	 */
	public static void updateResultCfmStatus(Integer resultId,Integer cfmStatus,Connection conn) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(" UPDATE IX_DEALERSHIP_RESULT SET CFM_STATUS = :1 WHERE RESULT_ID = :2");

		PreparedStatement pstmt = null;
		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setInt(1, cfmStatus);
			pstmt.setInt(2, resultId);
			pstmt.executeUpdate();
			
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
	}
	
	/**
	 * 根据poiNum赋值日库中对应的字段
	 * @param poiNum
	 * @param result
	 * @param regionConn
	 * @throws Exception 
	 */
	public static int setRegionFiledByPoiNum(ExpClientConfirmResult result,Connection regionConn) throws Exception{
		StringBuilder sb = new StringBuilder();
		sb.append(" SELECT P.PID,P.CHAIN,P.KIND_CODE,P.POST_CODE FROM IX_POI P "
				+ "WHERE P.POI_NUM = :1");

		PreparedStatement pstmt = null;
		ResultSet resultSet = null;

		try {
			pstmt = regionConn.prepareStatement(sb.toString());
			pstmt.setString(1,result.getFid());
			resultSet = pstmt.executeQuery();
			
			if(resultSet.next()){
				result.setPoiPid(resultSet.getInt("PID"));
				result.setPoiChain(resultSet.getString("CHAIN"));
				result.setPoiKindCode(resultSet.getString("KIND_CODE"));
				result.setPoiPostCode(resultSet.getString("POST_CODE"));
				return resultSet.getInt("PID");
			}
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
		return 0;
			
	}
	
	public static void setPoiContactByPid(ExpClientConfirmResult result, Connection regionConn) throws Exception {
		
		StringBuilder sb = new StringBuilder();
		sb.append(" select listagg(contact,'|') within GROUP (order by priority DESC)  AS contact");
		sb.append(" from ix_poi_contact WHERE poi_pid = :1 group by poi_pid");

		PreparedStatement pstmt = null;
		ResultSet resultSet = null;

		try {
			pstmt = regionConn.prepareStatement(sb.toString());
			pstmt.setInt(1,result.getPoiPid());
			resultSet = pstmt.executeQuery();
			
			if(resultSet.next()){
				result.setPoiContact(resultSet.getString("contact"));
			}
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
	}
	
	public static void setPoiAddressByPid(ExpClientConfirmResult result, Connection regionConn) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(" SELECT FULLNAME FROM ix_poi_address "
				+ "WHERE LANG_CODE = 'CHI' AND poi_pid = :1");

		PreparedStatement pstmt = null;
		ResultSet resultSet = null;

		try {
			pstmt = regionConn.prepareStatement(sb.toString());
			pstmt.setInt(1,result.getPoiPid());
			resultSet = pstmt.executeQuery();
			
			if(resultSet.next()){
				result.setPoiAddress(resultSet.getString("FULLNAME"));
			}
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
	}
	public static void setPoiAliasNameByPid(ExpClientConfirmResult result, Connection regionConn) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(" SELECT NAME FROM ix_poi_name "
				+ "WHERE name_class = 3 AND name_type = 1 AND LANG_CODE = 'CHI' AND poi_pid = :1");

		PreparedStatement pstmt = null;
		ResultSet resultSet = null;

		try {
			pstmt = regionConn.prepareStatement(sb.toString());
			pstmt.setInt(1,result.getPoiPid());
			resultSet = pstmt.executeQuery();
			
			if(resultSet.next()){
				result.setPoiAliasName(resultSet.getString("NAME"));
			}
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
		
	}
	public static void setPoiStandrandNameByPid(ExpClientConfirmResult result, Connection regionConn) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(" SELECT NAME FROM ix_poi_name "
				+ "WHERE name_class = 1 AND name_type = 1 AND LANG_CODE = 'CHI' AND poi_pid = :1");

		PreparedStatement pstmt = null;
		ResultSet resultSet = null;

		try {
			pstmt = regionConn.prepareStatement(sb.toString());
			pstmt.setInt(1,result.getPoiPid());
			resultSet = pstmt.executeQuery();
			
			if(resultSet.next()){
				result.setPoiName(resultSet.getString("NAME"));
			}
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
		
	}
	public static void updateResultToClientDate(Integer resultId, Connection conn) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(" UPDATE IX_DEALERSHIP_RESULT SET TO_CLIENT_DATE = :1 WHERE RESULT_ID = :2");

		PreparedStatement pstmt = null;
		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setString(1, DateUtils.dateToString(new Date(), "yyyyMMddHHmmss"));
			pstmt.setInt(2, resultId);
			pstmt.executeUpdate();
			
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
	}
	
	/**
	 * 更新result相应的状态当关闭作业时
	 * @param resultId
	 * @param conn
	 * @throws Exception
	 */
	public static void updateResultStatusWhenCloseWork(List<Integer> resultIds,Connection conn) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(" UPDATE ix_dealership_result r SET r.cfm_status = 3,r.workflow_status = 9,r.deal_status = 3 WHERE ");
		sb.append(" r.result_id in (");
		String temp = "";
		for (int resultId:resultIds) {
			sb.append(temp);
			sb.append(resultId);
			temp = ",";
		}
		sb.append(")");

		PreparedStatement pstmt = null;
		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.executeUpdate();
			
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
	}
	
	public static void updateResultFbSource(Integer resultId, Connection conn) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(" UPDATE IX_DEALERSHIP_RESULT SET FB_SOURCE = :1 WHERE RESULT_ID = :2");

		PreparedStatement pstmt = null;
		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setInt(1, 2);
			pstmt.setInt(2, resultId);
			pstmt.executeUpdate();
			
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
		
	}
	
	
	/**
	 * 根据resultId取得Geometry
	 * @param resultId
	 * @return
	 * @throws Exception 
	 */
	public static Geometry getGeometryByResultId(Integer resultId) throws Exception{
		Connection dealerConn = DBConnector.getInstance().getDealershipConnection();
		IxDealershipResult result = getIxDealershipResultById(resultId, dealerConn);
		return result.getGeometry();
	}
	
	
	/**
	 * 更新sourecrId
	 * @param resultId
	 * @param conn
	 * @throws Exception
	 */
	public static void updateResultSourceId(Integer resultId, Integer sourceId, Connection conn) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(" UPDATE IX_DEALERSHIP_RESULT SET SOURCE_ID = :1 WHERE RESULT_ID = :2");

		PreparedStatement pstmt = null;
		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setInt(1, sourceId);
			pstmt.setInt(2, resultId);
			pstmt.executeUpdate();
			
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
	}
}
