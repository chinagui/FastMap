package com.navinfo.dataservice.control.dealership.service;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import oracle.sql.STRUCT;

import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.edit.model.IxDealershipResult;
import com.navinfo.dataservice.api.edit.model.IxDealershipSource;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.QueryRunner;

public class IxDealershipSourceSelector {
	protected static Logger log = LoggerRepos.getLogger(IxDealershipSourceSelector.class);
	
	public static IxDealershipSource getBySourceId(Connection conn,int sourceId)throws Exception{
		String sql= "SELECT * FROM IX_DEALERSHIP_SOURCE WHERE SOURCE_ID ="+sourceId;
		return new QueryRunner().query(conn,sql,getSourceHander());
	}
	
	public static Map<Integer, IxDealershipSource> getBySourceIds(Connection conn,Collection<Integer> sourceIds)throws Exception{
		if(sourceIds==null|sourceIds.size()==0)return new HashMap<Integer,IxDealershipSource>();

		if(sourceIds.size()>1000){
			String sql= "SELECT * FROM IX_DEALERSHIP_SOURCE WHERE SOURCE_ID IN (SELECT COLUMN_VALUE FROM TABLE(CLOB_TO_TABLE(?)))";
			Clob clob = ConnectionUtil.createClob(conn);
			clob.setString(1, StringUtils.join(sourceIds, ","));
			return new QueryRunner().query(conn, sql, getSourcesMapHander(),clob);
		}else{
			String sql= "SELECT * FROM IX_DEALERSHIP_SOURCE WHERE SOURCE_ID IN ('"+StringUtils.join(sourceIds, "','")+"')";
			return new QueryRunner().query(conn,sql,getSourcesMapHander());
		}
	}
	
	public static Map<Integer, IxDealershipSource> getAllIxDealershipSource(Connection conn)throws Exception{
		String sql= "SELECT * FROM IX_DEALERSHIP_SOURCE";
		return new QueryRunner().query(conn,sql,getSourcesMapHander());
	}
	/**
	 * key是IxDealershipResult对象的sourceId
	 * @return
	 */
	private static ResultSetHandler<Map<Integer, IxDealershipSource>> getSourcesMapHander(){
		return new ResultSetHandler<Map<Integer,IxDealershipSource>>() {

			@Override
			public Map<Integer, IxDealershipSource> handle(ResultSet rs)
					throws SQLException {
				Map<Integer, IxDealershipSource> sourceIdMap=new HashMap<Integer, IxDealershipSource>();
				while(rs.next()){
					IxDealershipSource tmp=getBean(rs);
					sourceIdMap.put(tmp.getSourceId(), tmp);
				}
				return sourceIdMap;
			}
		};
	}
	
	public static Map<Integer, IxDealershipSource> getSourceIdsByChain(Connection conn,Collection<String> chainSet)throws Exception{
		if(chainSet==null|chainSet.size()==0)return new HashMap<Integer,IxDealershipSource>();

		if(chainSet.size()>1000){
			String sql= "SELECT * FROM IX_DEALERSHIP_SOURCE WHERE CHAIN IN (SELECT COLUMN_VALUE FROM TABLE(CLOB_TO_TABLE(?)))";
			Clob clob = ConnectionUtil.createClob(conn);
			clob.setString(1, StringUtils.join(chainSet, ","));
			return new QueryRunner().query(conn, sql, getSourcesMapHander(),clob);
		}else{
			String sql= "SELECT * FROM IX_DEALERSHIP_SOURCE WHERE CHAIN IN ('"+StringUtils.join(chainSet, "','")+"')";
			return new QueryRunner().query(conn,sql,getSourcesMapHander());
		}
	}
	
	/**
	 * key是IxDealershipResult对象的sourceId
	 * @return
	 */
	private static ResultSetHandler<IxDealershipSource> getSourceHander(){
		return new ResultSetHandler<IxDealershipSource>() {

			@Override
			public IxDealershipSource handle(ResultSet rs)
					throws SQLException {
				if(rs.next()){
					IxDealershipSource tmp=getBean(rs);
					return tmp;
				}
				return null;
			}
		};
	}
	
	private static IxDealershipSource getBean(ResultSet rs) throws SQLException{
		IxDealershipSource result=new IxDealershipSource();
		result.setSourceId(rs.getInt("SOURCE_ID"));
		result.setProvince(rs.getString("PROVINCE"));
		result.setCity(rs.getString("CITY"));
		result.setProject(rs.getString("PROJECT"));
		result.setKindCode(rs.getString("KIND_CODE"));
		result.setChain(rs.getString("CHAIN"));
		result.setName(rs.getString("NAME"));
		result.setNameShort(rs.getString("NAME_SHORT"));
		result.setAddress(rs.getString("ADDRESS"));
		result.setTelSale(rs.getString("TEL_SALE"));
		result.setTelService(rs.getString("TEL_SERVICE"));
		result.setTelOther(rs.getString("TEL_OTHER"));
		result.setPostCode(rs.getString("POST_CODE"));
		result.setNameEng(rs.getString("NAME_ENG"));
		result.setAddressEng(rs.getString("ADDRESS_ENG"));
		result.setProvideDate(rs.getString("PROVIDE_DATE"));
		result.setIsDeleted(rs.getInt("IS_DELETED"));
		result.setFbSource(rs.getInt("FB_SOURCE"));
		result.setFbContent(rs.getString("FB_CONTENT"));
		result.setFbAuditRemark(rs.getString("FB_AUDIT_REMARK"));
		result.setFbDate(rs.getString("FB_DATE"));
		result.setCfmPoiNum(rs.getString("CFM_POI_NUM"));
		result.setCfmMemo(rs.getString("CFM_MEMO"));
		result.setDealCfmDate(rs.getString("DEAL_CFM_DATE"));
		result.setPoiKindCode(rs.getString("POI_KIND_CODE"));
		result.setPoiChain(rs.getString("POI_CHAIN"));
		result.setPoiName(rs.getString("POI_NAME"));
		result.setPoiNameShort(rs.getString("POI_NAME_SHORT"));
		result.setPoiAddress(rs.getString("POI_ADDRESS"));
		result.setPoiPostCode(rs.getString("POI_POST_CODE"));
		result.setPoiXDisplay(rs.getDouble("POI_X_DISPLAY"));
		result.setPoiYDisplay(rs.getDouble("POI_Y_DISPLAY"));
		result.setPoiXGuide(rs.getDouble("POI_X_GUIDE"));
		result.setPoiYGuide(rs.getDouble("POI_Y_GUIDE"));
		STRUCT geoStruct=(STRUCT) rs.getObject("GEOMETRY");
		try {
			result.setGeometry(GeoTranslator.struct2Jts(geoStruct));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		result.setPoiTel(rs.getString("POI_TEL"));
		return result;
	}
	
	
	/**
	 * 同步根据RESULT更新SOURCE表
	 * @param sourceId
	 * @param conn
	 * @throws Exception 
	 */
	public static int saveOrUpdateSourceByResult(IxDealershipResult result, Connection conn) throws Exception {
		if(result.getSourceId()!=0){//更新操作
			if(result.getDealSrcDiff()==2){//旧版有新版没有，需删除
				StringBuilder sb = new StringBuilder();
				sb.append("UPDATE IX_DEALERSHIP_SOURCE SET IS_DELETED = ?,FB_SOURCE = ?,FB_CONTENT = ?,FB_AUDIT_REMARK = ?,");
				sb.append("FB_DATE = ?,CFM_POI_NUM = ?,CFM_MEMO = ?,DEAL_CFM_DATE = ?,POI_KIND_CODE = ?,");
				sb.append("POI_CHAIN = ?,POI_NAME = ?,POI_NAME_SHORT = ?,POI_ADDRESS = ?,POI_TEL = ?,POI_POST_CODE = ?,");
				sb.append("POI_X_DISPLAY = ?,POI_Y_DISPLAY = ?,POI_X_GUIDE = ?,POI_Y_GUIDE = ?,GEOMETRY = ? WHERE SOURCE_ID = ?");
				
				//持久化
				QueryRunner run = new QueryRunner();
	
				try {
					run.update(conn, 
							   sb.toString(), 
							   1, result.getFbSource(), result.getFbContent(), result.getFbAuditRemark(), 
							   result.getFbDate(), result.getCfmPoiNum(), result.getCfmMemo(), result.getDealCfmDate(), 
							   result.getPoiKindCode(), result.getPoiChain(), result.getPoiName(), result.getPoiNameShort(), 
							   result.getPoiAddress(),result.getPoiTel(), result.getPoiPostCode(), result.getPoiXDisplay(),
							   result.getPoiYDisplay(),result.getPoiXGuide(), result.getPoiYGuide(), 
							   GeoTranslator.wkt2Struct(conn, GeoTranslator.jts2Wkt(result.getGeometry(),0.00001, 5)),result.getSourceId()
							   );
				} catch (Exception e) {
					throw e;
				}
			}else{
			
				StringBuilder sb = new StringBuilder();
				sb.append("UPDATE IX_DEALERSHIP_SOURCE SET PROVINCE = ?,CITY = ?,PROJECT = ?,");
				sb.append("KIND_CODE = ?,CHAIN = ?,NAME = ?,NAME_SHORT = ?,ADDRESS = ?,TEL_SALE = ?,");
				sb.append("TEL_SERVICE = ?,TEL_OTHER = ?,POST_CODE = ?,NAME_ENG = ?,ADDRESS_ENG = ?,");
				sb.append("PROVIDE_DATE = ?,IS_DELETED = ?,FB_SOURCE = ?,FB_CONTENT = ?,FB_AUDIT_REMARK = ?,");
				sb.append("FB_DATE = ?,CFM_POI_NUM = ?,CFM_MEMO = ?,DEAL_CFM_DATE = ?,POI_KIND_CODE = ?,");
				sb.append("POI_CHAIN = ?,POI_NAME = ?,POI_NAME_SHORT = ?,POI_ADDRESS = ?,POI_TEL = ?,POI_POST_CODE = ?,");
				sb.append("POI_X_DISPLAY = ?,POI_Y_DISPLAY = ?,POI_X_GUIDE = ?,POI_Y_GUIDE = ?,GEOMETRY = ? WHERE SOURCE_ID = ?");
				
				//持久化
				QueryRunner run = new QueryRunner();
	
				try {
					run.update(conn, 
							   sb.toString(), 
							   result.getProvince(), result.getCity(), result.getProject(), 
							   result.getKindCode(), result.getChain(), result.getName(), result.getNameShort(), 
							   result.getAddress(), result.getTelSale(), result.getTelService(), result.getTelOther(), 
							   result.getPostCode(), result.getNameEng(), result.getAddressEng(), result.getProvideDate(), 
							   result.getIsDeleted(), result.getFbSource(), result.getFbContent(), result.getFbAuditRemark(), 
							   result.getFbDate(), result.getCfmPoiNum(), result.getCfmMemo(), result.getDealCfmDate(), 
							   result.getPoiKindCode(), result.getPoiChain(), result.getPoiName(), result.getPoiNameShort(), 
							   result.getPoiAddress(),result.getPoiTel(), result.getPoiPostCode(), result.getPoiXDisplay(),
							   result.getPoiYDisplay(),result.getPoiXGuide(), result.getPoiYGuide(), 
							   GeoTranslator.wkt2Struct(conn, GeoTranslator.jts2Wkt(result.getGeometry(),0.00001, 5)),result.getSourceId()
							   );
				} catch (Exception e) {
					throw e;
				}
				
			}
			return result.getSourceId();
		}else{//插入操作
			QueryRunner run = new QueryRunner();
			Integer nextVal = run.query(conn, "SELECT SOURCE_SEQ.NEXTVAL FROM dual",new ResultSetHandler<Integer>() {
				@Override
				public Integer handle(ResultSet rs)
						throws SQLException {
					if(rs.next()){
						return rs.getInt(1);
					}
					return 0;
				}
			});
			String createSql = "insert into IX_DEALERSHIP_SOURCE (SOURCE_ID, PROVINCE, CITY, PROJECT, KIND_CODE, CHAIN, NAME, NAME_SHORT, ADDRESS, TEL_SALE, TEL_SERVICE, TEL_OTHER, POST_CODE, NAME_ENG, ADDRESS_ENG, PROVIDE_DATE, IS_DELETED, FB_SOURCE, FB_CONTENT, FB_AUDIT_REMARK, FB_DATE, CFM_POI_NUM, CFM_MEMO, DEAL_CFM_DATE, POI_KIND_CODE, POI_CHAIN, POI_NAME, POI_NAME_SHORT, POI_ADDRESS, POI_POST_CODE, POI_X_DISPLAY, POI_Y_DISPLAY, POI_X_GUIDE, POI_Y_GUIDE, GEOMETRY, POI_TEL) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
					+ "?,?)";	
			try {
			run.update(conn, 
						   createSql,nextVal,result.getProvince(), result.getCity(), result.getProject(), 
						   result.getKindCode(), result.getChain(), result.getName(), result.getNameShort(), 
						   result.getAddress(), result.getTelSale(), result.getTelService(), result.getTelOther(), 
						   result.getPostCode(), result.getNameEng(), result.getAddressEng(), result.getProvideDate(), 
						   0, result.getFbSource(), result.getFbContent(), result.getFbAuditRemark(), 
						   result.getFbDate(), result.getCfmPoiNum(), result.getCfmMemo(), result.getDealCfmDate(), 
						   result.getPoiKindCode(), result.getPoiChain(), result.getPoiName(), result.getPoiNameShort(), 
						   result.getPoiAddress(), result.getPoiPostCode(), result.getPoiXDisplay(), result.getPoiYDisplay(), 
						   result.getPoiXGuide(), result.getPoiYGuide(),GeoTranslator.wkt2Struct(conn, GeoTranslator.jts2Wkt(result.getGeometry(),0.00001, 5)),
						   result.getPoiTel()
					   );
			} catch (Exception e) {
				throw e;
			}
			return nextVal;
		}
		
	}

	/**
	 * @param conn
	 * @return
	 * @throws Exception 
	 */
	public static Map<String, List<IxDealershipSource>> getAllIxDealershipSourceByChain(Connection conn) throws Exception {
		String sql= "SELECT * FROM IX_DEALERSHIP_SOURCE S WHERE S.IS_DELETED <> 1  ORDER BY S.CHAIN";
		log.info("getAllIxDealershipSourceByChain sql:" + sql);
		return new QueryRunner().query(conn,sql,getSourcesByChainHander());
	}
	
	/**
	 * @param conn
	 * @param chainList
	 * @return
	 * @throws Exception 
	 */
	public static Map<String, List<IxDealershipSource>> getAllIxDealershipSourceByChain(Connection conn,Set<String> chainList) throws Exception {
		String sql= "SELECT * FROM IX_DEALERSHIP_SOURCE S WHERE S.IS_DELETED <> 1 AND S.CHAIN IN ('" + StringUtils.join(chainList,"','") + "') ORDER BY S.CHAIN";
		log.info("getAllIxDealershipSourceByChain sql:" + sql);
		return new QueryRunner().query(conn,sql,getSourcesByChainHander());
	}
	
	/**
	 * 查询CHAIN.work_type为“一览表”状态(work_type=2)的品牌，根据品牌关联SOURCE表，且SOUCE.is_delete为“非总表删除记录”
	 * @param conn
	 * @param chainList
	 * @return
	 * @throws Exception 
	 */
	public static Map<String, List<IxDealershipSource>> getAllIxDealershipSourceByChainWorkType(Connection conn) throws Exception {
		String sql= "SELECT ds.* FROM ix_dealership_source ds,ix_dealership_chain dc WHERE ds.chain = dc.chain_code AND dc.work_type = 2 AND ds.is_deleted <> 1"
				+ " ORDER BY ds.chain,ds.source_id ";
		log.info("getAllIxDealershipSourceByChain sql:" + sql);
		return new QueryRunner().query(conn,sql,getSourcesByChainHander());
	}

	/**
	 * @return
	 */
	private static ResultSetHandler<Map<String, List<IxDealershipSource>>> getSourcesByChainHander() {
		return new ResultSetHandler<Map<String,List<IxDealershipSource>>>() {

			@Override
			public Map<String, List<IxDealershipSource>> handle(ResultSet rs)
					throws SQLException {
				Map<String, List<IxDealershipSource>> sourceIdMap=new HashMap<String, List<IxDealershipSource>>();
				while(rs.next()){
					IxDealershipSource tmp=getBean(rs);
					if(sourceIdMap.containsKey(tmp.getChain())){
						sourceIdMap.get(tmp.getChain()).add(tmp);
					}else{
						List<IxDealershipSource> list = new ArrayList<IxDealershipSource>();
						list.add(tmp);
						sourceIdMap.put(tmp.getChain(), list);
					}
				}
				return sourceIdMap;
			}
		};
	}
}
