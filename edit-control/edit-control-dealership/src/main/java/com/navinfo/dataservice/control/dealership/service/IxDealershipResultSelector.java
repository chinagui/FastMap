package com.navinfo.dataservice.control.dealership.service;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.edit.model.IxDealershipResult;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.QueryRunner;

public class IxDealershipResultSelector {
	protected static Logger log = LoggerRepos.getLogger(IxDealershipResultSelector.class);

	public static Map<Integer,IxDealershipResult> getByResultIds(Connection conn,Collection<Integer> resultIds)throws Exception{
		if(resultIds==null|resultIds.size()==0)return new HashMap<Integer,IxDealershipResult>();

		if(resultIds.size()>1000){
			String sql= "SELECT * FROM IX_DEALERSHIP_RESULT WHERE RESULT_ID IN (SELECT COLUMN_VALUE FROM TABLE(CLOB_TO_TABLE(?))) AND U_RECORD <>2";
			Clob clob = ConnectionUtil.createClob(conn);
			clob.setString(1, StringUtils.join(resultIds, ","));
			return new QueryRunner().query(conn, sql, getResultHander(),clob);
		}else{
			String sql= "SELECT * FROM IX_DEALERSHIP_RESULT WHERE RESULT_ID IN ('"+StringUtils.join(resultIds, "','")+"') AND U_RECORD <>2";
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
					sourceIdMap.put(tmp.getResultId(), tmp);
				}
				return sourceIdMap;
			}
		};
	}
	
	public static Map<Integer,IxDealershipResult> getBySourceIds(Connection conn,Collection<Integer> sourceIds)throws Exception{
		if(sourceIds==null|sourceIds.size()==0)return new HashMap<Integer,IxDealershipResult>();

		if(sourceIds.size()>1000){
			String sql= "SELECT * FROM IX_DEALERSHIP_RESULT WHERE SOURCE_ID IN (SELECT COLUMN_VALUE FROM TABLE(CLOB_TO_TABLE(?))) AND U_RECORD <>2";
			Clob clob = ConnectionUtil.createClob(conn);
			clob.setString(1, StringUtils.join(sourceIds, ","));
			return new QueryRunner().query(conn, sql, getSourceHander(),clob);
		}else{
			String sql= "SELECT * FROM IX_DEALERSHIP_RESULT WHERE SOURCE_ID IN ('"+StringUtils.join(sourceIds, "','")+"') AND U_RECORD <>2";
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
		result.setChain(rs.getString("CHAIN"));
		result.setCity(rs.getString("CITY"));
		result.setDealCfmDate(rs.getString("DEAL_CFM_DATE"));
		result.setDealSrcDiff(rs.getInt("DEAL_SRC_DIFF"));
		result.setDealStatus(rs.getInt("DEAL_STATUS"));
		result.setFbAuditRemark(rs.getString("FB_AUDIT_REMARK"));
		result.setFbContent(rs.getString("FB_CONTENT"));
		result.setFbDate(rs.getString("FB_DATE"));
		result.setFbSource(rs.getInt("FB_SOURCE"));
		result.setGeometry(rs.getString("GEOMETRY"));
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
		return result;
	}
}
