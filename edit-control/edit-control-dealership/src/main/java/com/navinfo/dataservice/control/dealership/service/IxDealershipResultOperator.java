package com.navinfo.dataservice.control.dealership.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.edit.model.IxDealershipResult;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiAddress;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiContact;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiName;
import com.navinfo.dataservice.dao.log.LogReader;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

public class IxDealershipResultOperator {
	private static Logger log = LoggerRepos.getLogger(DataEditService.class);
	public static void createIxDealershipResult(Connection conn,IxDealershipResult  bean)throws ServiceException{
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			
			String createSql = "insert into IX_DEALERSHIP_RESULT ";			
			List<String> columns = new ArrayList<String>();
			List<String> placeHolder = new ArrayList<String>();
			List<Object> values = new ArrayList<Object>();
			
			columns.add(" RESULT_ID ");
			placeHolder.add("RESULT_SEQ.NEXTVAL");
				
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("WORKFLOW_STATUS")){
				columns.add(" WORKFLOW_STATUS ");
				placeHolder.add("?");
				values.add(bean.getWorkflowStatus());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("DEAL_STATUS")){
				columns.add(" DEAL_STATUS ");
				placeHolder.add("?");
				values.add(bean.getDealStatus());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("USER_ID")){
				columns.add(" USER_ID ");
				placeHolder.add("?");
				values.add(bean.getUserId());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("TO_INFO_DATE")){
				columns.add(" TO_INFO_DATE ");
				placeHolder.add("?");
				values.add(bean.getToInfoDate());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("TO_CLIENT_DATE")){
				columns.add(" TO_CLIENT_DATE ");
				placeHolder.add("?");
				values.add(bean.getToClientDate());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("PROVINCE")){
				columns.add(" PROVINCE ");
				placeHolder.add("?");
				values.add(bean.getProvince());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("CITY")){
				columns.add(" CITY ");
				placeHolder.add("?");
				values.add(bean.getCity());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("PROJECT")){
				columns.add(" PROJECT ");
				placeHolder.add("?");
				values.add(bean.getProject());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("KIND_CODE")){
				columns.add(" KIND_CODE ");
				placeHolder.add("?");
				values.add(bean.getKindCode());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("CHAIN")){
				columns.add(" CHAIN ");
				placeHolder.add("?");
				values.add(bean.getChain());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("NAME")){
				columns.add(" NAME ");
				placeHolder.add("?");
				values.add(bean.getName());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("NAME_SHORT")){
				columns.add(" NAME_SHORT ");
				placeHolder.add("?");
				values.add(bean.getNameShort());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("ADDRESS")){
				columns.add(" ADDRESS ");
				placeHolder.add("?");
				values.add(bean.getAddress());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("TEL_SALE")){
				columns.add(" TEL_SALE ");
				placeHolder.add("?");
				values.add(bean.getTelSale());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("TEL_SERVICE")){
				columns.add(" TEL_SERVICE ");
				placeHolder.add("?");
				values.add(bean.getTelService());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("TEL_OTHER")){
				columns.add(" TEL_OTHER ");
				placeHolder.add("?");
				values.add(bean.getTelOther());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("POST_CODE")){
				columns.add(" POST_CODE ");
				placeHolder.add("?");
				values.add(bean.getPostCode());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("NAME_ENG")){
				columns.add(" NAME_ENG ");
				placeHolder.add("?");
				values.add(bean.getNameEng());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("ADDRESS_ENG")){
				columns.add(" ADDRESS_ENG ");
				placeHolder.add("?");
				values.add(bean.getAddressEng());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("PROVIDE_DATE")){
				columns.add(" PROVIDE_DATE ");
				placeHolder.add("?");
				values.add(bean.getProvideDate());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("IS_DELETED")){
				columns.add(" IS_DELETED ");
				placeHolder.add("?");
				values.add(bean.getIsDeleted());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("MATCH_METHOD")){
				columns.add(" MATCH_METHOD ");
				placeHolder.add("?");
				values.add(bean.getMatchMethod());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("POI_NUM_1")){
				columns.add(" POI_NUM_1 ");
				placeHolder.add("?");
				values.add(bean.getPoiNum1());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("POI_NUM_2")){
				columns.add(" POI_NUM_2 ");
				placeHolder.add("?");
				values.add(bean.getPoiNum2());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("POI_NUM_3")){
				columns.add(" POI_NUM_3 ");
				placeHolder.add("?");
				values.add(bean.getPoiNum3());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("POI_NUM_4")){
				columns.add(" POI_NUM_4 ");
				placeHolder.add("?");
				values.add(bean.getPoiNum4());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("POI_NUM_5")){
				columns.add(" POI_NUM_5 ");
				placeHolder.add("?");
				values.add(bean.getPoiNum5());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("SIMILARITY")){
				columns.add(" SIMILARITY ");
				placeHolder.add("?");
				values.add(bean.getSimilarity());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("FB_SOURCE")){
				columns.add(" FB_SOURCE ");
				placeHolder.add("?");
				values.add(bean.getFbSource());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("FB_CONTENT")){
				columns.add(" FB_CONTENT ");
				placeHolder.add("?");
				values.add(bean.getFbContent());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("FB_AUDIT_REMARK")){
				columns.add(" FB_AUDIT_REMARK ");
				placeHolder.add("?");
				values.add(bean.getFbAuditRemark());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("FB_DATE")){
				columns.add(" FB_DATE ");
				placeHolder.add("?");
				values.add(bean.getFbDate());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("CFM_STATUS")){
				columns.add(" CFM_STATUS ");
				placeHolder.add("?");
				values.add(bean.getCfmStatus());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("CFM_POI_NUM")){
				columns.add(" CFM_POI_NUM ");
				placeHolder.add("?");
				values.add(bean.getCfmPoiNum());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("CFM_MEMO")){
				columns.add(" CFM_MEMO ");
				placeHolder.add("?");
				values.add(bean.getCfmMemo());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("SOURCE_ID")){
				columns.add(" SOURCE_ID ");
				placeHolder.add("?");
				values.add(bean.getSourceId());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("DEAL_SRC_DIFF")){
				columns.add(" DEAL_SRC_DIFF ");
				placeHolder.add("?");
				values.add(bean.getDealSrcDiff());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("DEAL_CFM_DATE")){
				columns.add(" DEAL_CFM_DATE ");
				placeHolder.add("?");
				values.add(bean.getDealCfmDate());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("POI_KIND_CODE")){
				columns.add(" POI_KIND_CODE ");
				placeHolder.add("?");
				values.add(bean.getPoiKindCode());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("POI_CHAIN")){
				columns.add(" POI_CHAIN ");
				placeHolder.add("?");
				values.add(bean.getPoiChain());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("POI_NAME")){
				columns.add(" POI_NAME ");
				placeHolder.add("?");
				values.add(bean.getPoiName());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("POI_NAME_SHORT")){
				columns.add(" POI_NAME_SHORT ");
				placeHolder.add("?");
				values.add(bean.getPoiNameShort());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("POI_ADDRESS")){
				columns.add(" POI_ADDRESS ");
				placeHolder.add("?");
				values.add(bean.getPoiAddress());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("POI_TEL")){
				columns.add(" POI_TEL ");
				placeHolder.add("?");
				values.add(bean.getPoiTel());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("POI_POST_CODE")){
				columns.add(" POI_POST_CODE ");
				placeHolder.add("?");
				values.add(bean.getPoiPostCode());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("POI_X_DISPLAY")){
				columns.add(" POI_X_DISPLAY ");
				placeHolder.add("?");
				values.add(bean.getPoiXDisplay());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("POI_Y_DISPLAY")){
				columns.add(" POI_Y_DISPLAY ");
				placeHolder.add("?");
				values.add(bean.getPoiYDisplay());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("POI_X_GUIDE")){
				columns.add(" POI_X_GUIDE ");
				placeHolder.add("?");
				values.add(bean.getPoiXGuide());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("POI_Y_GUIDE")){
				columns.add(" POI_Y_GUIDE ");
				placeHolder.add("?");
				values.add(bean.getPoiYGuide());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("GEOMETRY")){
				columns.add(" GEOMETRY ");
				placeHolder.add("?");
				STRUCT struct = GeoTranslator.wkt2Struct(conn,  GeoTranslator.jts2Wkt(bean.getGeometry()));
				values.add(struct);
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("REGION_ID")){
				columns.add(" REGION_ID ");
				placeHolder.add("?");
				values.add(bean.getRegionId());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("CFM_IS_ADOPTED")){
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
	
	
	public static void updateIxDealershipResult(Connection conn,IxDealershipResult bean,Long userId)throws ServiceException{
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			
			String updateSql = "update IX_DEALERSHIP_RESULT set ";
			List<String> columns = new ArrayList<String>();
			List<Object> values=new ArrayList<Object>();
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("RESULT_ID")){
				columns.add(" RESULT_ID=? ");
				values.add(bean.getResultId());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("WORKFLOW_STATUS")){
				columns.add(" WORKFLOW_STATUS=? ");
				values.add(bean.getWorkflowStatus());
				IxDealershipHistoryOperator.addWorkflowStatusHistory(conn, bean, userId);
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
				STRUCT struct = GeoTranslator.wkt2Struct(conn,  GeoTranslator.jts2Wkt(bean.getGeometry()));
				values.add(struct);
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
				updateSql = updateSql + columsStr + "  where RESULT_ID=" + bean.getResultId();
				run.update(conn, 
							updateSql, 
						   values.toArray() );
			}


		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("修改失败，原因为:"+e.getMessage(),e);
		}
	}
	
	
	/**
	 * 根据POI组成包含名称、地址、联系方式的json格式
	 * @param pois
	 * @return
	 */
	public static JSONArray componentPoiData(List<IxPoi> pois, Connection connPoi) throws Exception {
		JSONArray poiJson = new JSONArray();

		for (IxPoi poi : pois) {
			Map<String, Object> poiObj = new HashMap<>();
			poiObj.put("poiNum", poi.getPoiNum() == null ? "" : poi.getPoiNum());
			poiObj.put("pid", poi.getPid());
			poiObj.put("kindCode", poi.getKindCode() == null ? "" : poi.getKindCode());
			poiObj.put("postCode", poi.getPostCode() == null ? "" : poi.getPostCode());
			poiObj.put("chain", poi.getChain() == null ? "" : poi.getChain());
			poiObj.put("rowId", poi.getRowId() == null ? "" : poi.getRowId());
			poiObj.put("level", poi.getLevel());
			poiObj.put("geometry", GeoTranslator.jts2Geojson(poi.getGeometry(), 0.00001, 5));
			poiObj.put("xGuide", poi.getxGuide());
			poiObj.put("yGuide", poi.getyGuide());
			poiObj.put("open24h", poi.getOpen24h());
			poiObj.put("regionId", poi.getRegionId());
			poiObj.put("meshId", poi.getMeshId());
			
			if(connPoi != null){
				LogReader logRead = new LogReader(connPoi);
				int state = logRead.getObjectState(poi.getPid(), "IX_POI");
				poiObj.put("status", state);
			}

			// 名称
			JSONArray names = new JSONArray();
			for (IRow row : poi.getNames()) {
				Map<String, Object> nameMap = new HashMap<>();
				IxPoiName name = (IxPoiName) row;

				if (!name.getLangCode().equals("CHI") && !name.getLangCode().equals("CHT")) {
					continue;
				}

				nameMap.put("rowId", name.getRowId() == null ? "" : name.getRowId());
				nameMap.put("nameStrPinyin", name.getNamePhonetic() == null ? "" : name.getNamePhonetic());
				nameMap.put("nameGrpId", name.getNameGroupid());
				nameMap.put("nameId", name.getPid());
				nameMap.put("langCode", name.getLangCode() == null ? "" : name.getLangCode());
				nameMap.put("nameClass", name.getNameClass());
				nameMap.put("name", name.getName() == null ? "" : name.getName());
				nameMap.put("nameType", name.getNameType());

				JSONObject nameobj = JSONObject.fromObject(nameMap);
				names.add(nameobj);
			}
			poiObj.put("names", names);

			// 地址
			JSONArray addresses = new JSONArray();
			for (IRow row : poi.getAddresses()) {
				Map<String, Object> addressMap = new HashMap<>();
				IxPoiAddress address = (IxPoiAddress) row;

				if (!address.getLangCode().equals("CHI") && !address.getLangCode().equals("CHT")) {
					continue;
				}

				addressMap.put("rowId", address.getRowId() == null ? "" : address.getRowId());
				addressMap.put("roadname", address.getRoadname() == null ? "" : address.getRoadname());
				addressMap.put("langCode", address.getLangCode() == null ? "" : address.getLangCode());
				addressMap.put("fullNamePinyin",
						address.getFullnamePhonetic() == null ? "" : address.getFullnamePhonetic());
				addressMap.put("addrname", address.getAddrname() == null ? "" : address.getAddrname());
				addressMap.put("roadNamePinyin",
						address.getRoadnamePhonetic() == null ? "" : address.getRoadnamePhonetic());
				addressMap.put("addrNamePinyin",
						address.getAddrnamePhonetic() == null ? "" : address.getAddrnamePhonetic());
				addressMap.put("fullname", address.getFullname() == null ? "" : address.getFullname());

				JSONObject addressobj = JSONObject.fromObject(addressMap);
				addresses.add(addressobj);
			}
			poiObj.put("addresses", addresses);

			// 电话
			JSONArray contacts = new JSONArray();
			for (IRow row : poi.getContacts()) {
				Map<String, Object> contactMap = new HashMap<>();
				IxPoiContact contact = (IxPoiContact) row;

				contactMap.put("contact", contact.getContact() == null ? "" : contact.getContact());
				contactMap.put("contactDepart", contact.getContactDepart());
				contactMap.put("contactType", contact.getContactType());
				contactMap.put("poiPid", contact.getPoiPid());
				contactMap.put("priority", contact.getPriority());
				contactMap.put("rowId", contact.getRowId() == null ? "" : contact.getRowId());
				contactMap.put("uDate", contact.getuDate() == null ? "" : contact.getuDate());
				contactMap.put("uRecord", contact.getuRecord());

				JSONObject contactobj = JSONObject.fromObject(contactMap);
				contacts.add(contactobj);
			}
			poiObj.put("contacts", contacts);

			JSONObject obj = JSONObject.fromObject(poiObj);
			poiJson.add(obj);
		}

		return poiJson;
	}


	public static void createIxDealershipResultWithId(Connection conn, IxDealershipResult bean) throws Exception {
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			
			String createSql = "insert into IX_DEALERSHIP_RESULT ";			
			List<String> columns = new ArrayList<String>();
			List<String> placeHolder = new ArrayList<String>();
			List<Object> values = new ArrayList<Object>();
			
			columns.add(" RESULT_ID ");
			placeHolder.add("?");
			values.add(bean.getResultId());
			
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("WORKFLOW_STATUS")){
				columns.add(" WORKFLOW_STATUS ");
				placeHolder.add("?");
				values.add(bean.getWorkflowStatus());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("DEAL_STATUS")){
				columns.add(" DEAL_STATUS ");
				placeHolder.add("?");
				values.add(bean.getDealStatus());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("USER_ID")){
				columns.add(" USER_ID ");
				placeHolder.add("?");
				values.add(bean.getUserId());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("TO_INFO_DATE")){
				columns.add(" TO_INFO_DATE ");
				placeHolder.add("?");
				values.add(bean.getToInfoDate());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("TO_CLIENT_DATE")){
				columns.add(" TO_CLIENT_DATE ");
				placeHolder.add("?");
				values.add(bean.getToClientDate());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("PROVINCE")){
				columns.add(" PROVINCE ");
				placeHolder.add("?");
				values.add(bean.getProvince());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("CITY")){
				columns.add(" CITY ");
				placeHolder.add("?");
				values.add(bean.getCity());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("PROJECT")){
				columns.add(" PROJECT ");
				placeHolder.add("?");
				values.add(bean.getProject());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("KIND_CODE")){
				columns.add(" KIND_CODE ");
				placeHolder.add("?");
				values.add(bean.getKindCode());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("CHAIN")){
				columns.add(" CHAIN ");
				placeHolder.add("?");
				values.add(bean.getChain());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("NAME")){
				columns.add(" NAME ");
				placeHolder.add("?");
				values.add(bean.getName());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("NAME_SHORT")){
				columns.add(" NAME_SHORT ");
				placeHolder.add("?");
				values.add(bean.getNameShort());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("ADDRESS")){
				columns.add(" ADDRESS ");
				placeHolder.add("?");
				values.add(bean.getAddress());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("TEL_SALE")){
				columns.add(" TEL_SALE ");
				placeHolder.add("?");
				values.add(bean.getTelSale());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("TEL_SERVICE")){
				columns.add(" TEL_SERVICE ");
				placeHolder.add("?");
				values.add(bean.getTelService());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("TEL_OTHER")){
				columns.add(" TEL_OTHER ");
				placeHolder.add("?");
				values.add(bean.getTelOther());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("POST_CODE")){
				columns.add(" POST_CODE ");
				placeHolder.add("?");
				values.add(bean.getPostCode());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("NAME_ENG")){
				columns.add(" NAME_ENG ");
				placeHolder.add("?");
				values.add(bean.getNameEng());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("ADDRESS_ENG")){
				columns.add(" ADDRESS_ENG ");
				placeHolder.add("?");
				values.add(bean.getAddressEng());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("PROVIDE_DATE")){
				columns.add(" PROVIDE_DATE ");
				placeHolder.add("?");
				values.add(bean.getProvideDate());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("IS_DELETED")){
				columns.add(" IS_DELETED ");
				placeHolder.add("?");
				values.add(bean.getIsDeleted());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("MATCH_METHOD")){
				columns.add(" MATCH_METHOD ");
				placeHolder.add("?");
				values.add(bean.getMatchMethod());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("POI_NUM_1")){
				columns.add(" POI_NUM_1 ");
				placeHolder.add("?");
				values.add(bean.getPoiNum1());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("POI_NUM_2")){
				columns.add(" POI_NUM_2 ");
				placeHolder.add("?");
				values.add(bean.getPoiNum2());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("POI_NUM_3")){
				columns.add(" POI_NUM_3 ");
				placeHolder.add("?");
				values.add(bean.getPoiNum3());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("POI_NUM_4")){
				columns.add(" POI_NUM_4 ");
				placeHolder.add("?");
				values.add(bean.getPoiNum4());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("POI_NUM_5")){
				columns.add(" POI_NUM_5 ");
				placeHolder.add("?");
				values.add(bean.getPoiNum5());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("SIMILARITY")){
				columns.add(" SIMILARITY ");
				placeHolder.add("?");
				values.add(bean.getSimilarity());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("FB_SOURCE")){
				columns.add(" FB_SOURCE ");
				placeHolder.add("?");
				values.add(bean.getFbSource());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("FB_CONTENT")){
				columns.add(" FB_CONTENT ");
				placeHolder.add("?");
				values.add(bean.getFbContent());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("FB_AUDIT_REMARK")){
				columns.add(" FB_AUDIT_REMARK ");
				placeHolder.add("?");
				values.add(bean.getFbAuditRemark());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("FB_DATE")){
				columns.add(" FB_DATE ");
				placeHolder.add("?");
				values.add(bean.getFbDate());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("CFM_STATUS")){
				columns.add(" CFM_STATUS ");
				placeHolder.add("?");
				values.add(bean.getCfmStatus());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("CFM_POI_NUM")){
				columns.add(" CFM_POI_NUM ");
				placeHolder.add("?");
				values.add(bean.getCfmPoiNum());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("CFM_MEMO")){
				columns.add(" CFM_MEMO ");
				placeHolder.add("?");
				values.add(bean.getCfmMemo());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("SOURCE_ID")){
				columns.add(" SOURCE_ID ");
				placeHolder.add("?");
				values.add(bean.getSourceId());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("DEAL_SRC_DIFF")){
				columns.add(" DEAL_SRC_DIFF ");
				placeHolder.add("?");
				values.add(bean.getDealSrcDiff());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("DEAL_CFM_DATE")){
				columns.add(" DEAL_CFM_DATE ");
				placeHolder.add("?");
				values.add(bean.getDealCfmDate());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("POI_KIND_CODE")){
				columns.add(" POI_KIND_CODE ");
				placeHolder.add("?");
				values.add(bean.getPoiKindCode());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("POI_CHAIN")){
				columns.add(" POI_CHAIN ");
				placeHolder.add("?");
				values.add(bean.getPoiChain());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("POI_NAME")){
				columns.add(" POI_NAME ");
				placeHolder.add("?");
				values.add(bean.getPoiName());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("POI_NAME_SHORT")){
				columns.add(" POI_NAME_SHORT ");
				placeHolder.add("?");
				values.add(bean.getPoiNameShort());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("POI_ADDRESS")){
				columns.add(" POI_ADDRESS ");
				placeHolder.add("?");
				values.add(bean.getPoiAddress());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("POI_TEL")){
				columns.add(" POI_TEL ");
				placeHolder.add("?");
				values.add(bean.getPoiTel());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("POI_POST_CODE")){
				columns.add(" POI_POST_CODE ");
				placeHolder.add("?");
				values.add(bean.getPoiPostCode());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("POI_X_DISPLAY")){
				columns.add(" POI_X_DISPLAY ");
				placeHolder.add("?");
				values.add(bean.getPoiXDisplay());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("POI_Y_DISPLAY")){
				columns.add(" POI_Y_DISPLAY ");
				placeHolder.add("?");
				values.add(bean.getPoiYDisplay());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("POI_X_GUIDE")){
				columns.add(" POI_X_GUIDE ");
				placeHolder.add("?");
				values.add(bean.getPoiXGuide());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("POI_Y_GUIDE")){
				columns.add(" POI_Y_GUIDE ");
				placeHolder.add("?");
				values.add(bean.getPoiYGuide());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("GEOMETRY")){
				columns.add(" GEOMETRY ");
				placeHolder.add("?");
				STRUCT struct = GeoTranslator.wkt2Struct(conn,  GeoTranslator.jts2Wkt(bean.getGeometry()));
				values.add(struct);
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("REGION_ID")){
				columns.add(" REGION_ID ");
				placeHolder.add("?");
				values.add(bean.getRegionId());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("CFM_IS_ADOPTED")){
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
	
	
	public static int getResultBySequence(Connection conn) throws Exception{
		QueryRunner run = new QueryRunner();
		Integer nextVal = run.query(conn, "SELECT RESULT_SEQ.NEXTVAL FROM dual",new ResultSetHandler<Integer>() {
			@Override
			public Integer handle(ResultSet rs)
					throws SQLException {
				if(rs.next()){
					return rs.getInt(1);
				}
				return 0;
			}
		});
		return nextVal;
	}
}
