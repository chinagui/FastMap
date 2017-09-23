package com.navinfo.dataservice.job.statics.manJob;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.dbutils.DbUtils;
import org.bson.Document;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Region;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.engine.statics.tools.MongoDao;
import com.navinfo.dataservice.job.statics.AbstractStatJob;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.navicommons.exception.ServiceException;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName ProductMonitorJob
 * @author Han Shaoming
 * @date 2017年9月22日 下午4:09:28
 * @Description TODO
 */
public class ProductMonitorJob extends AbstractStatJob {
	
	private static final String task = "task";
	private static final String day_produce = "day_produce";
	private static final String product_monitor = "product_monitor";

	private static final String dbName = SystemConfigFactory.getSystemConfig().getValue(PropConstant.fmStat);
	
	public ProductMonitorJob(JobInfo jobInfo) {
		super(jobInfo);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String stat() throws JobException {
		try {
			log.info("start stat ");
			long t = System.currentTimeMillis();
			ManApi manApi = (ManApi)ApplicationContextUtil.getBean("manApi");
			List<Region> regionList = manApi.queryRegionList();
			Set<Integer> dbIds = new HashSet<Integer>();
			Set<Integer> monDbIds = new HashSet<Integer>();
			for (Region region : regionList) {
				if(region.getDailyDbId() != null && region.getDailyDbId() != 0){
					dbIds.add(region.getDailyDbId());
				}
				if(region.getMonthlyDbId() != null && region.getMonthlyDbId() != 0){
					monDbIds.add(region.getMonthlyDbId());
				}
			}
			log.info("统计的大区库:"+dbIds.toString());
			log.info("统计的月库:"+monDbIds.toString());
			//处理数据
			//任务统计数据
			List<Map<String, Object>> statList = new ArrayList<Map<String, Object>>();
			Map<String,Object> stat = new HashMap<String,Object>();
			long roadLen = 0;
			int poiNum = 0;
			//rd_link非删除link长度总和
			for(int monDbId : monDbIds){
				long RdLinkTotal = getUnDelRdLinkTotal(monDbId);
				roadLen += RdLinkTotal;
			}
			//ix_poi非删除总数
			for(int dbId : dbIds){
				int poiTotal = getUnDelPoiTotal(dbId);
				poiNum += poiTotal;
			}
			stat.put("roadLen", roadLen);
			stat.put("poiNum", poiNum);
			//查询mongo中product_monitor的最新统计数据
			Map<String, Object> productMonitorStatPre = getProductMonitorStatPreData();
			//查询mongo中day_produce的最新统计数据
			Map<String, Object> dayProduceStat = getDayProduceStatData();
			//查询mongo中task的最新统计数据(中线)
			Map<String, Object> taskStat = getTaskStatData();
			//处理数据
			long perUpdateRoad = 0;
			long perAddRoad = 0;
			int perUpdatePoi = 0;
			int perAddPoi = 0;
			//处理数据
			perUpdateRoad = (long)taskStat.get("cUpdateRoad") - (long)productMonitorStatPre.get("cUpdateRoad");
			perAddRoad = (long)taskStat.get("cAddRoad") - (long)productMonitorStatPre.get("cAddRoad");
			perUpdatePoi = (int)taskStat.get("cUpdatePoi") - (int)productMonitorStatPre.get("cUpdatePoi");
			perAddPoi = (int)taskStat.get("cAddPoi") - (int)productMonitorStatPre.get("cAddPoi");
			stat.put("perUpdateRoad", perUpdateRoad);
			stat.put("perAddRoad", perAddRoad);
			stat.put("perUpdatePoi", perUpdatePoi);
			stat.put("perAddPoi", perAddPoi);
			
			stat.putAll(dayProduceStat);
			stat.putAll(taskStat);
			JSONObject result = new JSONObject();
			result.put("product_monitor",statList);

			log.info("end stat ");
			log.debug("所有任务数据统计完毕。用时："+((System.currentTimeMillis()-t)/1000)+"s.");
			
			return result.toString();
			
		} catch (Exception e) {
			log.error("大屏任务统计:"+e.getMessage(), e);
			throw new JobException("大屏任务统计:"+e.getMessage(),e);
		}
	}

	/**
	 * 查询日库中非删除的poi数量
	 * @author Han Shaoming
	 * @param dbId
	 * @return
	 * @throws Exception
	 */
	private  int getUnDelPoiTotal(int dbId) throws Exception{
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int poiTotal = 0;
		try{
			conn = DBConnector.getInstance().getConnectionById(dbId);
			String sql = "SELECT COUNT(1) NUM FROM IX_POI WHERE U_RECORD <>2";

			pstmt = conn.prepareStatement(sql);
			
			rs = pstmt.executeQuery();
			
			while(rs.next()){
				poiTotal = rs.getInt("NUM");
			}
		}catch(Exception e){
			log.error("dbId("+dbId+")查询日库中非删除的poi数量报错:" + e.getMessage(), e);
			throw new Exception("dbId("+dbId+")查询日库中非删除的poi数量报错:" + e.getMessage(), e);
		}finally{
			DbUtils.closeQuietly(conn, pstmt, rs);
		}
		return poiTotal;
	}
	
	/**
	 * 查询月库中非删除link长度总和
	 * @author Han Shaoming
	 * @param dbId
	 * @return
	 * @throws Exception
	 */
	private  long getUnDelRdLinkTotal(int dbId) throws Exception{
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		long lengthTotal = 0;
		try{
			conn = DBConnector.getInstance().getConnectionById(dbId);
			String sql = "SELECT SUM(LENGTH) TOTAL FROM RD_LINK WHERE U_RECORD <>2";

			pstmt = conn.prepareStatement(sql);
			
			rs = pstmt.executeQuery();
			
			while(rs.next()){
				lengthTotal = Math.round(rs.getDouble("TOTAL"));
			}
		}catch(Exception e){
			log.error("dbId("+dbId+")查询月库中非删除link长度总和报错:" + e.getMessage(), e);
			throw new Exception("dbId("+dbId+")查询月库中非删除link长度总和报错:" + e.getMessage(), e);
		}finally{
			DbUtils.closeQuietly(conn, pstmt, rs);
		}
		return lengthTotal;
	}

	/**
	 * 查询mongo中task的最新统计数据(中线)
	 * @throws ServiceException 
	 */
	private Map<String,Object> getTaskStatData() throws Exception{
		double cUpdateRoad = 0;
		double cAddRoad = 0;
		int cUpdatePoi = 0;
		int cAddPoi = 0;
		try {
			//获取最新统计数据(中线)
			MongoDao mongoDao = new MongoDao(dbName);
			BasicDBObject filter = new BasicDBObject("programType", 1);
			FindIterable<Document> findIterable = mongoDao.find(task, filter).projection(new Document("_id",0)).sort(new BasicDBObject("timestamp",-1));
			MongoCursor<Document> iterator = findIterable.iterator();
			Map<String,Object> stat = new HashMap<String,Object>();
			String timestampLast="";
			//处理数据
			while(iterator.hasNext()){
				//获取统计数据
				JSONObject jso = JSONObject.fromObject(iterator.next());
				String timestampOrigin=String.valueOf(jso.get("timestamp"));
				if(StringUtils.isEmpty(timestampLast)){
					timestampLast=timestampOrigin;
					log.info("最近一次的统计日期为："+timestampLast);
				}
				if(!timestampLast.equals(timestampOrigin)){
					break;
				}
				if(jso.has("collectLinkUpdateTotal")){
					cUpdateRoad += (double) jso.get("collectLinkUpdateTotal");
				}
				if(jso.has("collectLinkAddTotal")){
					cAddRoad += (double) jso.get("collectLinkAddTotal");
				}
				if(jso.has("poiActualUpdateNum")){
					cUpdatePoi += (int) jso.get("poiActualUpdateNum");
				}
				if(jso.has("poiActualAddNum")){
					cAddPoi += (int) jso.get("poiActualAddNum");
				}
			}
			stat.put("cUpdateRoad", Math.round(cUpdateRoad));
			stat.put("cAddRoad", Math.round(cAddRoad));
			stat.put("cUpdatePoi", cUpdatePoi);
			stat.put("cAddPoi", cAddPoi);
			return stat;
		} catch (Exception e) {
			log.error("查询mongo中task的最新统计数据(中线)报错"+e.getMessage());
			throw new Exception("查询mongo中task的最新统计数据(中线)报错"+e.getMessage(),e);
		}
	}
	
	/**
	 * 查询mongo中day_produce的最新统计数据
	 * @throws ServiceException 
	 */
	private Map<String,Object> getDayProduceStatData() throws Exception{
		double dpUpdateRoad = 0;
		double dpAddRoad = 0;
		int dpUpdatePoi = 0;
		int dpAddPoi = 0;
		JSONObject dpAverage = new JSONObject();
		try {
			//获取最新统计数据(中线)
			MongoDao mongoDao = new MongoDao(dbName);
			FindIterable<Document> findIterable = mongoDao.find(day_produce, null).projection(new Document("_id",0)).sort(new BasicDBObject("timestamp",-1));
			MongoCursor<Document> iterator = findIterable.iterator();
			Map<String,Object> stat = new HashMap<String,Object>();
			String timestampLast="";
			//处理数据
			while(iterator.hasNext()){
				//获取统计数据
				JSONObject jso = JSONObject.fromObject(iterator.next());
				String timestampOrigin=String.valueOf(jso.get("timestamp"));
				if(StringUtils.isEmpty(timestampLast)){
					timestampLast=timestampOrigin;
					log.info("最近一次的统计日期为："+timestampLast);
				}
				if(!timestampLast.equals(timestampOrigin)){
					break;
				}
				if(jso.has("dpUpdateRoad")){
					dpUpdateRoad = (double) jso.get("dpUpdateRoad");
				}
				if(jso.has("dpAddRoad")){
					dpAddRoad = (double) jso.get("dpAddRoad");
				}
				if(jso.has("dpUpdatePoi")){
					dpUpdatePoi = (int) jso.get("dpUpdatePoi");
				}
				if(jso.has("dpAddPoi")){
					dpAddPoi = (int) jso.get("dpAddPoi");
				}
				if(jso.has("dpAverage") && jso.getJSONObject("dpAverage") != null){
					dpAverage.putAll(jso.getJSONObject("dpAverage")); 
				}
			}
			stat.put("dpUpdateRoad", Math.round(dpUpdateRoad));
			stat.put("dpAddRoad", Math.round(dpAddRoad));
			stat.put("dpUpdatePoi", dpUpdatePoi);
			stat.put("dpAddPoi", dpAddPoi);
			stat.put("dpAverage", dpAverage);
			return stat;
		} catch (Exception e) {
			log.error("查询mongo中day_produce的最新统计数据报错"+e.getMessage());
			throw new Exception("查询mongo中day_produce的最新统计数据报错"+e.getMessage(),e);
		}
	}

	/**
	 * 查询mongo中前一天product_monitor的最新统计数据
	 * @throws ServiceException 
	 */
	private Map<String,Object> getProductMonitorStatPreData() throws Exception{
		
		try {
			Date lastDate = DateUtils.getDayBefore(new Date());
			String lastTime = DateUtils.format(lastDate, DateUtils.DATE_YMD);
			MongoDao mongoDao = new MongoDao(dbName);
			//获取前一天最新统计数据
			long cUpdateRoad = 0;
			long cAddRoad = 0;
			int cUpdatePoi = 0;
			int cAddPoi = 0;
			Pattern pattern = Pattern.compile("^"+lastTime);
			BasicDBObject filter = new BasicDBObject("timestamp", pattern);
			FindIterable<Document> findIterable = mongoDao.find(product_monitor, filter).projection(new Document("_id",0)).sort(new BasicDBObject("timestamp",-1));
			MongoCursor<Document> iterator = findIterable.iterator();
			Map<String,Object> stat = new HashMap<String,Object>();
			String timestampLast="";
			//处理数据
			while(iterator.hasNext()){
				//获取统计数据
				JSONObject jso = JSONObject.fromObject(iterator.next());
				String timestampOrigin=String.valueOf(jso.get("timestamp"));
				if(StringUtils.isEmpty(timestampLast)){
					timestampLast=timestampOrigin;
					log.info("最近一次的统计日期为："+timestampLast);
				}
				if(!timestampLast.equals(timestampOrigin)){
					break;
				}
				if(jso.has("cUpdateRoad")){
					cUpdateRoad = (long) jso.get("cUpdateRoad");
				}
				if(jso.has("cAddRoad")){
					cAddRoad = (long) jso.get("cAddRoad");
				}
				if(jso.has("cUpdatePoi")){
					cUpdatePoi = (int) jso.get("cUpdatePoi");
				}
				if(jso.has("cAddPoi")){
					cAddPoi = (int) jso.get("cAddPoi");
				}
			}
			
			stat.put("cUpdateRoad", cUpdateRoad);
			stat.put("cAddRoad", cAddRoad);
			stat.put("cUpdatePoi", cUpdatePoi);
			stat.put("cAddPoi", cAddPoi);
			return stat;
		} catch (Exception e) {
			log.error("查询mongo中product_monitor的最新统计数据(中线)报错"+e.getMessage());
			throw new Exception("查询mongo中product_monitor的最新统计数据(中线)报错"+e.getMessage(),e);
		}
	}

	
}
