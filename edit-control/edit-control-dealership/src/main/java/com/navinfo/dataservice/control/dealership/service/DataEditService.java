package com.navinfo.dataservice.control.dealership.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.edit.iface.EditApi;
import com.navinfo.dataservice.api.edit.model.IxDealershipResult;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.glm.search.IxPoiSearch;
import com.navinfo.dataservice.dao.glm.selector.poi.deep.IxPoiColumnStatusSelector;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.navinfo.navicommons.exception.ServiceException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 代理店数据编辑类
 * 
 * @author jicaihua
 *
 */
public class DataEditService {

	private QueryRunner run = new QueryRunner();

	private static Logger log = LoggerRepos.getLogger(DataEditService.class);

	/**
	 * 申请数据
	 * 
	 * @param chainCode
	 * @param conn
	 * @param useId
	 */
	public int applyDataService(String chainCode, Connection conn, long userId) throws Exception {
		String haveDataSql = String.format(
				"SELECT COUNT(*) FROM IX_DEALERSHIP_RESULT WHERE USERID = %d AND WORKFLOW_STATUS = %d AND DEAL_STAUTS = %d AND CHAIN = %s FOR UPDATE NOWAIT;",
				userId, 3, 1, chainCode);
		int count = run.queryForInt(conn, haveDataSql);

		if (count >= 50)
			return 0;

		String queryListSql = String.format(
				"SELECT RESULT_ID FROM IX_DEALERSHIP_RESULT WHERE USERID = %d AND WORKFLOW_STATUS = %d AND DEAL_STAUTS = %d AND CHAIN = %s AND ROWNUM <= %d FOR UPDATE NOWAIT;",
				0, 3, 1, chainCode, 50 - count);
		List<Integer> resultID = ExecuteQuery(queryListSql, conn);

		String updateSql = "UPDATE IX_DEALERSHIP_RESULT SET USER_ID = " + userId + " ,DEAL_STATUS = " + 1
				+ " WHERE RESULT_ID IN (" + StringUtils.join(resultID, ",") + ")";
		run.execute(conn, updateSql);

		return 50 - count;
	}

	/**
	 * 开始作业：加载分配数据列表
	 * 
	 * @param chainCode
	 * @param conn
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	public JSONArray startWorkService(String chainCode, Connection conn, long userId, int dealStatus) throws Exception {
		//待作业，待提交→内页录入作业3；已提交→出品9
		int flowStatus = 3;
		if(dealStatus == 3 || dealStatus == 2) flowStatus = 9;
		
		String queryListSql = String.format(
				"SELECT RESULT_ID,NAME,KIND_CODE,WORKFLOW_STATUS,DEAL_SRC_DIFF FROM IX_DEALERSHIP_RESULT WHERE USERID = %d AND WORKFLOW_STATUS = %d AND DEAL_STAUTS = %d AND CHAIN = %s FOR UPDATE NOWAIT;",
				userId, flowStatus, dealStatus, chainCode);
		List<Map<String, Object>> resultCol = ExecuteQueryForDetail(queryListSql, conn);

		JSONArray result = new JSONArray();

		for (Map<String, Object> item : resultCol) {
			JSONObject obj = new JSONObject();
			obj.put("resultId", item.get("RESULT_ID"));
			obj.put("name", item.get("NAME"));
			obj.put("kindCode", item.get("KIND_CODE"));
			obj.put("workflowStatus", item.get("WORKFLOW_STATUS"));
			obj.put("dealSrcDiff", item.get("DEAL_SRC_DIFF"));
			
			// TODO:checkErrorNum需要计算
			String queryPoi = String.format(
					"SELECT CFM_POI_NUM FROM IX_DEALERSHIP_RESULT WHERE RESULT_ID = %d AND CFM_STATUS = %d",
					item.get("RESULT_ID"), 2);
			String poiPid = run.queryForString(conn, queryPoi);
			obj.put("checkErrorNum", GetCheckResultCount(poiPid, conn));
		}
		return result;
	}

	private Integer GetCheckResultCount(String poiPid, Connection conn) throws Exception {
		if (poiPid.isEmpty())
			return 0;

		String checkSqlStr = String.format(
				"SELECT COUNT(*) FROM NI_VAL_EXCEPTION NE,CK_RESULT_OBJECT CK WHERE NE.MD5_CODE = CK.MD5_CODE AND CK.PID = %d",
				Integer.valueOf(poiPid));
		int count = run.queryForInt(conn, checkSqlStr);

		return count;
	}

	/**
	 * 传入sql，返回需要的详细信息（RESULT_ID,NAME,KIND_CODE,WORKFLOW_STATUS,DEAL_SRC_DIFF）集合
	 * 
	 * @param sql
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	private List<Map<String, Object>> ExecuteQueryForDetail(String sql, Connection conn) throws Exception {
		List<Map<String, Object>> result = new ArrayList<>();
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);
			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				Map<String, Object> detail = new HashMap<>();

				detail.put("RESULT_ID", resultSet.getInt(1));
				detail.put("NAME", resultSet.getString(2));
				detail.put("KIND_CODE", resultSet.getString(3));
				detail.put("WORKFLOW_STATUS", resultSet.getInt(4));
				detail.put("DEAL_SRC_DIFF", resultSet.getInt(5));

				result.add(detail);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return result;
	}

	/**
	 * 传入sql，返回代理店号码集合
	 * 
	 * @param sql
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	private List<Integer> ExecuteQuery(String sql, Connection conn) throws Exception {
		List<Integer> resultID = new ArrayList<>();
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);
			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				int value = resultSet.getInt(1);
				resultID.add(value);
			} // while
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return resultID;
	}
	
	
	public void createIxDealershipResult(Connection conn,IxDealershipResult  bean)throws ServiceException{
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			
			String createSql = "insert into IX_DEALERSHIP_RESULT ";			
			List<String> columns = new ArrayList<String>();
			List<String> placeHolder = new ArrayList<String>();
			List<Object> values = new ArrayList<Object>();
			
			if (bean!=null){
				columns.add(" RESULT_ID ");
				placeHolder.add("?");
				values.add("RESULT_SEQ..NEXTVAL");
			};
			if (bean!=null&&bean.getWorkflowStatus()!=0){
				columns.add(" WORKFLOW_STATUS ");
				placeHolder.add("?");
				values.add(bean.getWorkflowStatus());
			};
			if (bean!=null&&bean.getDealStatus()!=0){
				columns.add(" DEAL_STATUS ");
				placeHolder.add("?");
				values.add(bean.getDealStatus());
			};
			if (bean!=null&&bean.getUserId()!=0){
				columns.add(" USER_ID ");
				placeHolder.add("?");
				values.add(bean.getUserId());
			};
			if (bean!=null&&bean.getToInfoDate()!=null && StringUtils.isNotEmpty(bean.getToInfoDate().toString())){
				columns.add(" TO_INFO_DATE ");
				placeHolder.add("?");
				values.add(bean.getToInfoDate());
			};
			if (bean!=null&&bean.getToClientDate()!=null && StringUtils.isNotEmpty(bean.getToClientDate().toString())){
				columns.add(" TO_CLIENT_DATE ");
				placeHolder.add("?");
				values.add(bean.getToClientDate());
			};
			if (bean!=null&&bean.getProvince()!=null && StringUtils.isNotEmpty(bean.getProvince().toString())){
				columns.add(" PROVINCE ");
				placeHolder.add("?");
				values.add(bean.getProvince());
			};
			if (bean!=null&&bean.getCity()!=null && StringUtils.isNotEmpty(bean.getCity().toString())){
				columns.add(" CITY ");
				placeHolder.add("?");
				values.add(bean.getCity());
			};
			if (bean!=null&&bean.getProject()!=null && StringUtils.isNotEmpty(bean.getProject().toString())){
				columns.add(" PROJECT ");
				placeHolder.add("?");
				values.add(bean.getProject());
			};
			if (bean!=null&&bean.getKindCode()!=null && StringUtils.isNotEmpty(bean.getKindCode().toString())){
				columns.add(" KIND_CODE ");
				placeHolder.add("?");
				values.add(bean.getKindCode());
			};
			if (bean!=null&&bean.getChain()!=null && StringUtils.isNotEmpty(bean.getChain().toString())){
				columns.add(" CHAIN ");
				placeHolder.add("?");
				values.add(bean.getChain());
			};
			if (bean!=null&&bean.getName()!=null && StringUtils.isNotEmpty(bean.getName().toString())){
				columns.add(" NAME ");
				placeHolder.add("?");
				values.add(bean.getName());
			};
			if (bean!=null&&bean.getNameShort()!=null && StringUtils.isNotEmpty(bean.getNameShort().toString())){
				columns.add(" NAME_SHORT ");
				placeHolder.add("?");
				values.add(bean.getNameShort());
			};
			if (bean!=null&&bean.getAddress()!=null && StringUtils.isNotEmpty(bean.getAddress().toString())){
				columns.add(" ADDRESS ");
				placeHolder.add("?");
				values.add(bean.getAddress());
			};
			if (bean!=null&&bean.getTelSale()!=null && StringUtils.isNotEmpty(bean.getTelSale().toString())){
				columns.add(" TEL_SALE ");
				placeHolder.add("?");
				values.add(bean.getTelSale());
			};
			if (bean!=null&&bean.getTelService()!=null && StringUtils.isNotEmpty(bean.getTelService().toString())){
				columns.add(" TEL_SERVICE ");
				placeHolder.add("?");
				values.add(bean.getTelService());
			};
			if (bean!=null&&bean.getTelOther()!=null && StringUtils.isNotEmpty(bean.getTelOther().toString())){
				columns.add(" TEL_OTHER ");
				placeHolder.add("?");
				values.add(bean.getTelOther());
			};
			if (bean!=null&&bean.getPostCode()!=null && StringUtils.isNotEmpty(bean.getPostCode().toString())){
				columns.add(" POST_CODE ");
				placeHolder.add("?");
				values.add(bean.getPostCode());
			};
			if (bean!=null&&bean.getNameEng()!=null && StringUtils.isNotEmpty(bean.getNameEng().toString())){
				columns.add(" NAME_ENG ");
				placeHolder.add("?");
				values.add(bean.getNameEng());
			};
			if (bean!=null&&bean.getAddressEng()!=null && StringUtils.isNotEmpty(bean.getAddressEng().toString())){
				columns.add(" ADDRESS_ENG ");
				placeHolder.add("?");
				values.add(bean.getAddressEng());
			};
			if (bean!=null&&bean.getProvideDate()!=null && StringUtils.isNotEmpty(bean.getProvideDate().toString())){
				columns.add(" PROVIDE_DATE ");
				placeHolder.add("?");
				values.add(bean.getProvideDate());
			};
			if (bean!=null&&bean.getIsDeleted()!=0){
				columns.add(" IS_DELETED ");
				placeHolder.add("?");
				values.add(bean.getIsDeleted());
			};
			if (bean!=null&&bean.getMatchMethod()!=0){
				columns.add(" MATCH_METHOD ");
				placeHolder.add("?");
				values.add(bean.getMatchMethod());
			};
			if (bean!=null&&bean.getPoiNum1()!=null && StringUtils.isNotEmpty(bean.getPoiNum1().toString())){
				columns.add(" POI_NUM_1 ");
				placeHolder.add("?");
				values.add(bean.getPoiNum1());
			};
			if (bean!=null&&bean.getPoiNum2()!=null && StringUtils.isNotEmpty(bean.getPoiNum2().toString())){
				columns.add(" POI_NUM_2 ");
				placeHolder.add("?");
				values.add(bean.getPoiNum2());
			};
			if (bean!=null&&bean.getPoiNum3()!=null && StringUtils.isNotEmpty(bean.getPoiNum3().toString())){
				columns.add(" POI_NUM_3 ");
				placeHolder.add("?");
				values.add(bean.getPoiNum3());
			};
			if (bean!=null&&bean.getPoiNum4()!=null && StringUtils.isNotEmpty(bean.getPoiNum4().toString())){
				columns.add(" POI_NUM_4 ");
				placeHolder.add("?");
				values.add(bean.getPoiNum4());
			};
			if (bean!=null&&bean.getPoiNum5()!=null && StringUtils.isNotEmpty(bean.getPoiNum5().toString())){
				columns.add(" POI_NUM_5 ");
				placeHolder.add("?");
				values.add(bean.getPoiNum5());
			};
			if (bean!=null&&bean.getSimilarity()!=null && StringUtils.isNotEmpty(bean.getSimilarity().toString())){
				columns.add(" SIMILARITY ");
				placeHolder.add("?");
				values.add(bean.getSimilarity());
			};
			if (bean!=null&&bean.getFbSource()!=0){
				columns.add(" FB_SOURCE ");
				placeHolder.add("?");
				values.add(bean.getFbSource());
			};
			if (bean!=null&&bean.getFbContent()!=null && StringUtils.isNotEmpty(bean.getFbContent().toString())){
				columns.add(" FB_CONTENT ");
				placeHolder.add("?");
				values.add(bean.getFbContent());
			};
			if (bean!=null&&bean.getFbAuditRemark()!=null && StringUtils.isNotEmpty(bean.getFbAuditRemark().toString())){
				columns.add(" FB_AUDIT_REMARK ");
				placeHolder.add("?");
				values.add(bean.getFbAuditRemark());
			};
			if (bean!=null&&bean.getFbDate()!=null && StringUtils.isNotEmpty(bean.getFbDate().toString())){
				columns.add(" FB_DATE ");
				placeHolder.add("?");
				values.add(bean.getFbDate());
			};
			if (bean!=null&&bean.getCfmStatus()!=0){
				columns.add(" CFM_STATUS ");
				placeHolder.add("?");
				values.add(bean.getCfmStatus());
			};
			if (bean!=null&&bean.getCfmPoiNum()!=null && StringUtils.isNotEmpty(bean.getCfmPoiNum().toString())){
				columns.add(" CFM_POI_NUM ");
				placeHolder.add("?");
				values.add(bean.getCfmPoiNum());
			};
			if (bean!=null&&bean.getCfmMemo()!=null && StringUtils.isNotEmpty(bean.getCfmMemo().toString())){
				columns.add(" CFM_MEMO ");
				placeHolder.add("?");
				values.add(bean.getCfmMemo());
			};
			if (bean!=null&&bean.getSourceId()!=0){
				columns.add(" SOURCE_ID ");
				placeHolder.add("?");
				values.add(bean.getSourceId());
			};
			if (bean!=null&&bean.getDealSrcDiff()!=1){
				columns.add(" DEAL_SRC_DIFF ");
				placeHolder.add("?");
				values.add(bean.getDealSrcDiff());
			};
			if (bean!=null&&bean.getDealCfmDate()!=null && StringUtils.isNotEmpty(bean.getDealCfmDate().toString())){
				columns.add(" DEAL_CFM_DATE ");
				placeHolder.add("?");
				values.add(bean.getDealCfmDate());
			};
			if (bean!=null&&bean.getPoiKindCode()!=null && StringUtils.isNotEmpty(bean.getPoiKindCode().toString())){
				columns.add(" POI_KIND_CODE ");
				placeHolder.add("?");
				values.add(bean.getPoiKindCode());
			};
			if (bean!=null&&bean.getPoiChain()!=null && StringUtils.isNotEmpty(bean.getPoiChain().toString())){
				columns.add(" POI_CHAIN ");
				placeHolder.add("?");
				values.add(bean.getPoiChain());
			};
			if (bean!=null&&bean.getPoiName()!=null && StringUtils.isNotEmpty(bean.getPoiName().toString())){
				columns.add(" POI_NAME ");
				placeHolder.add("?");
				values.add(bean.getPoiName());
			};
			if (bean!=null&&bean.getPoiNameShort()!=null && StringUtils.isNotEmpty(bean.getPoiNameShort().toString())){
				columns.add(" POI_NAME_SHORT ");
				placeHolder.add("?");
				values.add(bean.getPoiNameShort());
			};
			if (bean!=null&&bean.getPoiAddress()!=null && StringUtils.isNotEmpty(bean.getPoiAddress().toString())){
				columns.add(" POI_ADDRESS ");
				placeHolder.add("?");
				values.add(bean.getPoiAddress());
			};
			if (bean!=null&&bean.getPoiTel()!=null && StringUtils.isNotEmpty(bean.getPoiTel().toString())){
				columns.add(" POI_TEL ");
				placeHolder.add("?");
				values.add(bean.getPoiTel());
			};
			if (bean!=null&&bean.getPoiPostCode()!=null && StringUtils.isNotEmpty(bean.getPoiPostCode().toString())){
				columns.add(" POI_POST_CODE ");
				placeHolder.add("?");
				values.add(bean.getPoiPostCode());
			};
			if (bean!=null&&bean.getPoiXDisplay()!=0){
				columns.add(" POI_X_DISPLAY ");
				placeHolder.add("?");
				values.add(bean.getPoiXDisplay());
			};
			if (bean!=null&&bean.getPoiYDisplay()!=0){
				columns.add(" POI_Y_DISPLAY ");
				placeHolder.add("?");
				values.add(bean.getPoiYDisplay());
			};
			if (bean!=null&&bean.getPoiXGuide()!=0){
				columns.add(" POI_X_GUIDE ");
				placeHolder.add("?");
				values.add(bean.getPoiXGuide());
			};
			if (bean!=null&&bean.getPoiYGuide()!=0){
				columns.add(" POI_Y_GUIDE ");
				placeHolder.add("?");
				values.add(bean.getPoiYGuide());
			};
			if (bean!=null&&bean.getGeometry()!=null && StringUtils.isNotEmpty(bean.getGeometry().toString())){
				columns.add(" GEOMETRY ");
				placeHolder.add("?");
				values.add(bean.getGeometry());
			};
			if (bean!=null&&bean.getRegionId()!=0){
				columns.add(" REGION_ID ");
				placeHolder.add("?");
				values.add(bean.getRegionId());
			};
			if (bean!=null&&bean.getCfmIsAdopted()!=0){
				columns.add(" CFM_IS_ADOPTED ");
				placeHolder.add("?");
				values.add(bean.getCfmIsAdopted());
			};
			if(!columns.isEmpty()){
				String columsStr = "(" + StringUtils.join(columns.toArray(),",") + ")";
				String placeHolderStr = "(" + StringUtils.join(placeHolder.toArray(),",") + ")";
				createSql = createSql + columsStr + " values " + placeHolderStr;
			}
			run.update(conn, 
					   createSql, 
					   values.toArray() );
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("创建失败，原因为:"+e.getMessage(),e);
		}
	}
	
	
	public void updateIxDealershipResult(Connection conn,IxDealershipResult bean)throws ServiceException{
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			
			String updateSql = "update IX_DEALERSHIP_RESULT set  where 1=1 ";
			List<String> columns = new ArrayList<String>();
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("RESULT_ID")){
				columns.add(" RESULT_ID=? ");
				values.add(bean.getResultId());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("WORKFLOW_STATUS")){
				columns.add(" WORKFLOW_STATUS=? ");
				values.add(bean.getWorkflowStatus());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("DEAL_STATUS")){
				columns.add(" DEAL_STATUS=? ");
				values.add(bean.getDealStatus());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("USER_ID")){
				columns.add(" USER_ID=? ");
				values.add(bean.getUserId());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("TO_INFO_DATE")){
				columns.add(" TO_INFO_DATE=? ");
				values.add(bean.getToInfoDate());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("TO_CLIENT_DATE")){
				columns.add(" TO_CLIENT_DATE=? ");
				values.add(bean.getToClientDate());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("PROVINCE")){
				columns.add(" PROVINCE=? ");
				values.add(bean.getProvince());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("CITY")){
				columns.add(" CITY=? ");
				values.add(bean.getCity());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("PROJECT")){
				columns.add(" PROJECT=? ");
				values.add(bean.getProject());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("KIND_CODE")){
				columns.add(" KIND_CODE=? ");
				values.add(bean.getKindCode());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("CHAIN")){
				columns.add(" CHAIN=? ");
				values.add(bean.getChain());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("NAME")){
				columns.add(" NAME=? ");
				values.add(bean.getName());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("NAME_SHORT")){
				columns.add(" NAME_SHORT=? ");
				values.add(bean.getNameShort());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("ADDRESS")){
				columns.add(" ADDRESS=? ");
				values.add(bean.getAddress());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("TEL_SALE")){
				columns.add(" TEL_SALE=? ");
				values.add(bean.getTelSale());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("TEL_SERVICE")){
				columns.add(" TEL_SERVICE=? ");
				values.add(bean.getTelService());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("TEL_OTHER")){
				columns.add(" TEL_OTHER=? ");
				values.add(bean.getTelOther());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("POST_CODE")){
				columns.add(" POST_CODE=? ");
				values.add(bean.getPostCode());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("NAME_ENG")){
				columns.add(" NAME_ENG=? ");
				values.add(bean.getNameEng());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("ADDRESS_ENG")){
				columns.add(" ADDRESS_ENG=? ");
				values.add(bean.getAddressEng());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("PROVIDE_DATE")){
				columns.add(" PROVIDE_DATE=? ");
				values.add(bean.getProvideDate());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("IS_DELETED")){
				columns.add(" IS_DELETED=? ");
				values.add(bean.getIsDeleted());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("MATCH_METHOD")){
				columns.add(" MATCH_METHOD=? ");
				values.add(bean.getMatchMethod());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("POI_NUM_1")){
				columns.add(" POI_NUM_1=? ");
				values.add(bean.getPoiNum1());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("POI_NUM_2")){
				columns.add(" POI_NUM_2=? ");
				values.add(bean.getPoiNum2());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("POI_NUM_3")){
				columns.add(" POI_NUM_3=? ");
				values.add(bean.getPoiNum3());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("POI_NUM_4")){
				columns.add(" POI_NUM_4=? ");
				values.add(bean.getPoiNum4());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("POI_NUM_5")){
				columns.add(" POI_NUM_5=? ");
				values.add(bean.getPoiNum5());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("SIMILARITY")){
				columns.add(" SIMILARITY=? ");
				values.add(bean.getSimilarity());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("FB_SOURCE")){
				columns.add(" FB_SOURCE=? ");
				values.add(bean.getFbSource());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("FB_CONTENT")){
				columns.add(" FB_CONTENT=? ");
				values.add(bean.getFbContent());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("FB_AUDIT_REMARK")){
				columns.add(" FB_AUDIT_REMARK=? ");
				values.add(bean.getFbAuditRemark());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("FB_DATE")){
				columns.add(" FB_DATE=? ");
				values.add(bean.getFbDate());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("CFM_STATUS")){
				columns.add(" CFM_STATUS=? ");
				values.add(bean.getCfmStatus());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("CFM_POI_NUM")){
				columns.add(" CFM_POI_NUM=? ");
				values.add(bean.getCfmPoiNum());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("CFM_MEMO")){
				columns.add(" CFM_MEMO=? ");
				values.add(bean.getCfmMemo());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("SOURCE_ID")){
				columns.add(" SOURCE_ID=? ");
				values.add(bean.getSourceId());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("DEAL_SRC_DIFF")){
				columns.add(" DEAL_SRC_DIFF=? ");
				values.add(bean.getDealSrcDiff());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("DEAL_CFM_DATE")){
				columns.add(" DEAL_CFM_DATE=? ");
				values.add(bean.getDealCfmDate());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("POI_KIND_CODE")){
				columns.add(" POI_KIND_CODE=? ");
				values.add(bean.getPoiKindCode());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("POI_CHAIN")){
				columns.add(" POI_CHAIN=? ");
				values.add(bean.getPoiChain());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("POI_NAME")){
				columns.add(" POI_NAME=? ");
				values.add(bean.getPoiName());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("POI_NAME_SHORT")){
				columns.add(" POI_NAME_SHORT=? ");
				values.add(bean.getPoiNameShort());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("POI_ADDRESS")){
				columns.add(" POI_ADDRESS=? ");
				values.add(bean.getPoiAddress());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("POI_TEL")){
				columns.add(" POI_TEL=? ");
				values.add(bean.getPoiTel());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("POI_POST_CODE")){
				columns.add(" POI_POST_CODE=? ");
				values.add(bean.getPoiPostCode());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("POI_X_DISPLAY")){
				columns.add(" POI_X_DISPLAY=? ");
				values.add(bean.getPoiXDisplay());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("POI_Y_DISPLAY")){
				columns.add(" POI_Y_DISPLAY=? ");
				values.add(bean.getPoiYDisplay());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("POI_X_GUIDE")){
				columns.add(" POI_X_GUIDE=? ");
				values.add(bean.getPoiXGuide());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("POI_Y_GUIDE")){
				columns.add(" POI_Y_GUIDE=? ");
				values.add(bean.getPoiYGuide());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("GEOMETRY")){
				columns.add(" GEOMETRY=? ");
				values.add(bean.getGeometry());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("REGION_ID")){
				columns.add(" REGION_ID=? ");
				values.add(bean.getRegionId());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("CFM_IS_ADOPTED")){
				columns.add(" CFM_IS_ADOPTED=? ");
				values.add(bean.getCfmIsAdopted());
			};

			if(!columns.isEmpty()){
				String columsStr = StringUtils.join(columns.toArray(),",");
				updateSql = updateSql + columsStr + "  where 1=1 and RESULT_ID=" + bean.getResultId();
			}
			run.update(conn, 
						updateSql, 
					   values.toArray() );

		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("修改失败，原因为:"+e.getMessage(),e);
		}
	}
}
