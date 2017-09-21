package com.navinfo.dataservice.engine.man.infor;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.man.model.Infor;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.json.JsonOperation;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.StringConverter;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.vividsolutions.jts.geom.Geometry;
import net.sf.json.JSONObject;

/** 
 * @ClassName: InforService
 * @author songdongyan
 * @date 2017年4月19日
 * @Description: InforService.java
 */
public class InforService {
private Logger log = LoggerRepos.getLogger(this.getClass());
	
	private InforService() {
	}

	private static class SingletonHolder {
		private static final InforService INSTANCE = new InforService();
	}

	public static InforService getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	public Infor create(JSONObject dataJson, long userId) throws Exception {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			
			int inforId = getInforId(conn);
			
			Infor infor = (Infor) JsonOperation.jsonToBean(dataJson,Infor.class);
			infor.setInforId(inforId);
			infor.setAdminName(infor.getAdminName());
			
			infor.setExpectDate(new Timestamp(DateUtils.stringToLong(dataJson.getString("expectDate"), DateUtils.DATE_WITH_SPLIT_YMD)));
			infor.setPublishDate(new Timestamp(DateUtils.stringToLong(dataJson.getString("publishDate"), DateUtils.DATE_WITH_SPLIT_YMD)));
			infor.setNewsDate(new Timestamp(DateUtils.stringToLong(dataJson.getString("newsDate"), DateUtils.DATE_WITH_SPLIT_YMD)));
			
			Timestamp date1=infor.getExpectDate();
			String d2=DateUtils.dateToString(new Date(), DateUtils.DATE_WITH_SPLIT_YMD);
			Timestamp date2=new Timestamp(DateUtils.stringToLong(d2, DateUtils.DATE_WITH_SPLIT_YMD));
			
		    long diff = date2.getTime() - date1.getTime();
		    long days = diff / (1000 * 60 * 60 * 24);			

		    //modify by songhe 2017/09/20   13迭代method字段赋值原则变更
			if(days == -21){
				infor.setMethod("预采集");
			}else if(days >= -6){
				infor.setMethod("正式采集");
			}
			int sourceCode = 0;
			int featureKind = 0;
			int inforStage = 0;
			if(dataJson.containsKey("sourceCode")){
				sourceCode = dataJson.getInt("sourceCode");
			}
			if(dataJson.containsKey("featureKind")){
				inforStage = dataJson.getInt("featureKind");
			}
			if(dataJson.containsKey("inforStage")){
				inforStage = dataJson.getInt("inforStage");
			}
			if(sourceCode == 1 && featureKind == 2 && inforStage == 2){
				infor.setMethod("矢量制作");
			}
			if(sourceCode == 3){
				infor.setMethod("正式采集");
			}
			
			/*
			 * 1.adminCode：
			 * 如果前两位为“11(北京)，12(天津)，31(上海)，50(重庆)”，则取前2位+0000
			 * 如果前两位不为“11(北京)，12(天津)，31(上海)，50(重庆)”，则取前4位+00
			 */
			String adminCode=infor.getAdminCode();
			String before2=adminCode.substring(0, 2);
			if(before2.equals("11")||before2.equals("12")||before2.equals("31")||before2.equals("50")){
				infor.setAdminCode(before2+"0000");
			}else{
				infor.setAdminCode(adminCode.substring(0, 4)+"00");
			}			

			createWithBean(conn,infor);
			
			//初始化infor_grid_mapping关系表
			QueryRunner run = new QueryRunner();
			String inforGeo = dataJson.getString("geometry");
			String insertSql = "INSERT INTO infor_grid_mapping(infor_id,grid_id) VALUES(?,?)";
			String[] inforGeoList = inforGeo.split(";");
			for (String geoTmp : inforGeoList) {
				Geometry inforTmp = GeoTranslator.wkt2Geometry(geoTmp);
				Set<?> grids = (Set<?>) CompGeometryUtil.geo2GridsWithoutBreak(inforTmp);
				Iterator<String> it = (Iterator<String>) grids.iterator();
				Object[][] inforGridValues=new Object[grids.size()][2];
				int num=0;
				while (it.hasNext()) {
					List<Object> tmpObjects = new ArrayList<Object>();
					tmpObjects.add(inforId);
					tmpObjects.add(Integer.parseInt(it.next()));
					run.update(conn, insertSql, tmpObjects.toArray());
					//inforGridValues[num]=tmpObjects;
					num=num+1;
				}
			}
			return infor;
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	/**
	 * @param conn 
	 * @param infor
	 * @throws Exception 
	 */
	private void createWithBean(Connection conn, Infor bean) throws Exception {
		try{
			if(bean.getGeometry() == null || StringUtils.isEmpty(bean.getGeometry().toString())){
				log.error("情报对应Geometry不能为空");
				return;
//				throw new Exception("情报对应Geometry不能为空");
			}
			QueryRunner run = new QueryRunner();
			
			StringBuffer insert = new StringBuffer();
			StringBuffer value = new StringBuffer();
			
			if(bean != null && bean.getInforId() != 0){
				StringConverter.manCreatDataUtils(insert, value);
				insert.append(" INFOR_ID ");
				value.append(bean.getInforId());
			};
			if(bean != null && StringUtils.isNotEmpty(bean.getInforName())){
				StringConverter.manCreatDataUtils(insert, value);
				insert.append(" INFOR_NAME ");
				value.append("'" + bean.getInforName() + "'");
			};
			if(bean != null && bean.getInforLevel() != null && StringUtils.isNotEmpty(bean.getInforLevel().toString())){
				StringConverter.manCreatDataUtils(insert, value);
				insert.append(" INFOR_LEVEL ");
				value.append(bean.getInforLevel());
			};
			if(bean != null && bean.getPlanStatus() != null && StringUtils.isNotEmpty(bean.getPlanStatus().toString())){
				StringConverter.manCreatDataUtils(insert, value);
				insert.append(" PLAN_STATUS ");
				value.append(bean.getPlanStatus());
			};
			if(bean!=null && bean.getFeedbackType() != null && StringUtils.isNotEmpty(bean.getFeedbackType().toString())){
				StringConverter.manCreatDataUtils(insert, value);
				insert.append(" FEEDBACK_TYPE ");
				value.append(bean.getFeedbackType());
			};
			if(bean !=null && bean.getInsertTime() != null && StringUtils.isNotEmpty(bean.getInsertTime().toString())){
				StringConverter.manCreatDataUtils(insert, value);
				insert.append(" INSERT_TIME ");
				value.append(" to_date('" +bean.getInsertTime()+ "','yyyy-MM-dd HH24:MI:ss') ");
			};
			if(bean != null && bean.getFeatureKind() != 0){
				StringConverter.manCreatDataUtils(insert, value);
				insert.append(" FEATURE_KIND ");
				value.append(bean.getFeatureKind());
			};
			if(bean != null && StringUtils.isNotEmpty(bean.getAdminName())){
				StringConverter.manCreatDataUtils(insert, value);
				insert.append(" ADMIN_NAME ");
				value.append("'" + bean.getAdminName() + "'");
			};
			if(bean != null && StringUtils.isNotEmpty(bean.getInforCode())){
				StringConverter.manCreatDataUtils(insert, value);
				insert.append(" INFOR_CODE ");
				value.append("'" + bean.getInforCode() + "'");
			};
			if(bean != null && bean.getPublishDate() != null && StringUtils.isNotEmpty(bean.getPublishDate().toString())){
				StringConverter.manCreatDataUtils(insert, value);
				insert.append(" PUBLISH_DATE ");
				value.append(" to_date('"+bean.getPublishDate().toString().substring(0, 10)+"','yyyy-MM-dd HH24:MI:ss') ");
			};
			if(bean != null && bean.getExpectDate() != null && StringUtils.isNotEmpty(bean.getExpectDate().toString())){
				StringConverter.manCreatDataUtils(insert, value);
				insert.append(" EXPECT_DATE ");
				value.append(" to_date('"+bean.getExpectDate().toString().substring(0, 10)+"','yyyy-MM-dd HH24:MI:ss') ");
			};
			if(bean != null && StringUtils.isNotEmpty(bean.getTopicName())){
				StringConverter.manCreatDataUtils(insert, value);
				insert.append(" TOPIC_NAME ");
				value.append("'" + bean.getTopicName() + "'");
			};
			if(bean != null && StringUtils.isNotEmpty(bean.getMethod())){
				StringConverter.manCreatDataUtils(insert, value);
				insert.append(" METHOD ");
				value.append("'" + bean.getMethod() + "'");
			};
			if(bean != null && bean.getNewsDate() != null && StringUtils.isNotEmpty(bean.getNewsDate().toString())){
				StringConverter.manCreatDataUtils(insert, value);
				insert.append(" NEWS_DATE ");
				value.append(" to_date('"+bean.getNewsDate().toString().substring(0, 10)+"','yyyy-MM-dd HH24:MI:ss') ");
			};
			if(bean != null && bean.getRoadLength() != null && bean.getRoadLength() != 0 && StringUtils.isNotEmpty(bean.getRoadLength().toString())){
				StringConverter.manCreatDataUtils(insert, value);
				insert.append(" ROAD_LENGTH ");
				value.append(bean.getRoadLength());
			};
			if(bean != null && bean.getReportUserId() != 0){
				StringConverter.manCreatDataUtils(insert, value);
				insert.append(" REPORT_USER_ID ");
				value.append(bean.getReportUserId());
			};
			if(bean != null && bean.getSourceCode() != 0 ){
				StringConverter.manCreatDataUtils(insert, value);
				insert.append(" SOURCE_CODE ");
				value.append(bean.getSourceCode());
			};
			if(bean != null && StringUtils.isNotEmpty(bean.getInfoTypeName())){
				StringConverter.manCreatDataUtils(insert, value);
				insert.append(" INFO_TYPE_NAME ");
				value.append("'" + bean.getInfoTypeName() +"'");
			};
			
			if(bean != null && StringUtils.isNotEmpty(bean.getAdminCode())){
				StringConverter.manCreatDataUtils(insert, value);
				insert.append(" ADMIN_CODE ");
				value.append("'" + bean.getAdminCode() + "'");
			};
			
			StringConverter.manCreatDataUtils(insert, value);
			insert.append(" PLAN_STATUS ");
			value.append(0);
			
			StringConverter.manCreatDataUtils(insert, value);
			insert.append(" GEOMETRY ");
			Clob c = ConnectionUtil.createClob(conn);
			c.setString(1, bean.getGeometry());
			value.append("?");
			
			String createSql = "insert into infor ("+insert.toString()+") values("+value.toString()+")";
			
			run.update(conn, createSql, c);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}
	}

	/**
	 * @param conn
	 * @return
	 * @throws Exception 
	 */
	private int getInforId(Connection conn) throws Exception {
		try{
			QueryRunner run = new QueryRunner();

			String querySql = "select INFOR_SEQ.NEXTVAL as inforId from dual";

			int inforId = Integer.valueOf(run
					.query(conn, querySql, new MapHandler()).get("inforId")
					.toString());
			return inforId;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}
	}

	/**
	 * @param conn2 
	 * @param programId
	 * @return
	 * @throws ServiceException 
	 */
	public Infor getInforByProgramId(Connection conn, Integer programId) throws ServiceException {
		try {
			QueryRunner run = new QueryRunner();
			
			StringBuilder sb = new StringBuilder();
			
			sb.append(" SELECT I.INFOR_NAME,I.ADMIN_NAME,I.PUBLISH_DATE,i.admin_code,i.infor_code,i.method,i.source_code,i.feature_kind,i.road_length");
			sb.append("   FROM PROGRAM P, INFOR I       ");
			sb.append("  WHERE P.INFOR_ID = I.INFOR_ID  ");
			sb.append("    AND P.PROGRAM_ID = " + programId);
			
			String sql = sb.toString();
			
			log.info("getInforNameByProgramId sql :" + sql);
			
			ResultSetHandler<Infor> rsHandler = new ResultSetHandler<Infor>() {
				public Infor handle(ResultSet rs) throws SQLException {
					Infor infor = new Infor();
					if(rs.next()) {
						infor.setAdminName(rs.getString("ADMIN_NAME"));
						infor.setAdminCode(rs.getString("ADMIN_CODE"));
						infor.setInforName(rs.getString("INFOR_NAME"));
						infor.setInforCode(rs.getString("INFOR_CODE"));
						infor.setPublishDate(rs.getTimestamp("PUBLISH_DATE"));
						infor.setMethod(rs.getString("METHOD"));
						infor.setSourceCode(rs.getInt("source_code"));
						//feature_kind,road_length
						infor.setFeatureKind(rs.getInt("feature_kind"));
						infor.setRoadLength(rs.getInt("road_length"));
						return infor;
					}
					return null;
				}
			};
			Infor result =  run.query(conn, sql,rsHandler);
			return result;
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new ServiceException("getInforNameByProgramId失败，原因为:" + e.getMessage(), e);
		}
	}
	
	/**
	 * @param conn2 
	 * @param programId
	 * @return
	 * @throws ServiceException 
	 */
	public Infor getInforByInforId(Connection conn, Integer inforId) throws ServiceException {
		try {
			QueryRunner run = new QueryRunner();
			
			StringBuilder sb = new StringBuilder();
			
			sb.append(" SELECT I.INFOR_NAME,I.ADMIN_NAME,I.PUBLISH_DATE");
			sb.append("   FROM INFOR I       ");
			sb.append("  WHERE I.INFOR_ID   = " + inforId);
			
			String sql = sb.toString();
			
			log.info("getInforByInforId sql :" + sql);
			
			ResultSetHandler<Infor> rsHandler = new ResultSetHandler<Infor>() {
				public Infor handle(ResultSet rs) throws SQLException {
					Infor infor = new Infor();
					if(rs.next()) {
						infor.setAdminName(rs.getString("ADMIN_NAME"));
						infor.setInforName(rs.getString("INFOR_NAME"));
						infor.setPublishDate(rs.getTimestamp("PUBLISH_DATE"));
						return infor;
					}
					return null;
				}
			};
			Infor result =  run.query(conn, sql,rsHandler);
			
			return result;
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new ServiceException("getInforByInforId失败，原因为:" + e.getMessage(), e);
		}
	}
}
