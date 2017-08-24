package com.navinfo.dataservice.job.statics.manJob;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Region;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.thread.VMThreadPoolExecutor;
import com.navinfo.dataservice.dao.log.LogReader;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.job.statics.AbstractStatJob;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceRtException;
import com.navinfo.navicommons.exception.ThreadExecuteException;
import com.navinfo.navicommons.geo.computation.CompGridUtil;
import com.vividsolutions.jts.geom.Point;

import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

/**
 * POI月库作业数据统计
 * @ClassName MonthPoiJob
 * @author Han Shaoming
 * @date 2017年7月31日 上午11:04:08
 * @Description TODO
 */
public class MonthPoiJob extends AbstractStatJob {
	protected VMThreadPoolExecutor threadPoolExecutor;

	public MonthPoiJob(JobInfo jobInfo) {
		super(jobInfo);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String stat() throws JobException {
		try {
			ManApi manApi = (ManApi)ApplicationContextUtil.getBean("manApi");
			List<Region> regionList = manApi.queryRegionList();
			Set<Integer> dbIds = new HashSet<Integer>();
			for (Region region : regionList) {
				if(region.getMonthlyDbId() != null && region.getMonthlyDbId() != 0){
					dbIds.add(region.getMonthlyDbId());
				}
			}
			log.info("统计的大区库:"+dbIds.toString());
			Map<Integer, Map<String,List<Map<String, Integer>>>> stats = new ConcurrentHashMap<Integer,Map<String,List<Map<String,Integer>>>>();
			long t = System.currentTimeMillis();
			int dbSize = dbIds.size();
			if(dbSize==1){
				new PoiMonthStatThread(null,dbIds.iterator().next(),stats).run();
			}else{
				if(dbSize>10){
					initThreadPool(10);
				}else{
					initThreadPool(dbSize);
				}
				final CountDownLatch latch = new CountDownLatch(dbSize);
				threadPoolExecutor.addDoneSignal(latch);
				// 执行转数据
				for(int dbId:dbIds){
					threadPoolExecutor.execute(new PoiMonthStatThread(latch,dbId,stats));
				}
				latch.await();
				if (threadPoolExecutor.getExceptions().size() > 0) {
					throw new Exception(threadPoolExecutor.getExceptions().get(0));
				}
			}
			log.debug("所有poi月库作业数据统计完毕。用时："+((System.currentTimeMillis()-t)/1000)+"s.");
			//处理数据
			Map<String,List<Map<String,Integer>>> result = new HashMap<String,List<Map<String,Integer>>>();
			result.put("grid_month_poi", new ArrayList<Map<String,Integer>>());

			for(Entry<Integer, Map<String, List<Map<String, Integer>>>> entry:stats.entrySet()){
				result.get("grid_month_poi").addAll(entry.getValue().get("grid_month_poi"));
			}
			return JSONObject.fromObject(result).toString();
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new JobException(e.getMessage(),e);
		}finally{
			shutDownPoolExecutor();
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void initThreadPool(int poolSize)throws Exception{
		log.debug("开始初始化线程池");
        threadPoolExecutor = new VMThreadPoolExecutor(poolSize,
        		poolSize,
				3,
				TimeUnit.SECONDS,
				new LinkedBlockingQueue(),
				new ThreadPoolExecutor.CallerRunsPolicy());
	}
	private void shutDownPoolExecutor(){
		if (threadPoolExecutor != null && !threadPoolExecutor.isShutdown()) {
			log.debug("关闭线程池");
			threadPoolExecutor.shutdownNow();
			try {
				while (!threadPoolExecutor.isTerminated()) {
					log.debug("等待线程结束：线程数为" + threadPoolExecutor.getActiveCount());
					Thread.sleep(2000);
				}
			} catch (InterruptedException e) {
				log.error("关闭线程池失败");
				throw new ServiceRtException("关闭线程池失败", e);
			}
		}
	}
	
	class PoiMonthStatThread implements Runnable{
		CountDownLatch latch = null;
		int dbId=0;
		Map<Integer, Map<String,List<Map<String, Integer>>>> stats;
		PoiMonthStatThread(CountDownLatch latch,int dbId,Map<Integer, Map<String,List<Map<String, Integer>>>> stat){
			this.latch=latch;
			this.dbId=dbId;
			this.stats = stat;
		}
		
		@Override
		public void run() {
			Connection conn=null;
			try{
				
				conn=DBConnector.getInstance().getConnectionById(dbId);
				QueryRunner run = new QueryRunner();
				
				StringBuilder sb = new StringBuilder();
				sb.append("SELECT S.PID, S.SECOND_WORK_STATUS, P.GEOMETRY, P.MESH_ID  ");
				sb.append("  FROM POI_COLUMN_STATUS S, IX_POI P                       ");
				sb.append(" WHERE S.PID = P.PID                                       ");
				
				String selectSql = sb.toString();

				ResultSetHandler<Map<Integer, Map<String, Integer>>> rsHandler = new ResultSetHandler<Map<Integer, Map<String, Integer>>>() {
					public Map<Integer, Map<String, Integer>> handle(ResultSet rs) throws SQLException {
						Map<Integer,Map<String,Integer>> gridStat = new HashMap<Integer,Map<String,Integer>>();
						//处理poi的统计,key:gridId,value:map(key:pid,value:状态)
						Map<Integer,Map<Long,List<Integer>>> pids = new HashMap<Integer,Map<Long,List<Integer>>>();
						while (rs.next()) {
						    int secondWorkStatus = rs.getInt("SECOND_WORK_STATUS");
						    STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
					    	Point geo;
					    	long pid = rs.getLong("PID");
							try {
								geo = (Point)GeoTranslator.struct2Jts(struct);
								int gridId = Integer.parseInt(CompGridUtil.point2Grid(geo.getX(), geo.getY(), rs.getString("MESH_ID")));
								Map<String,Integer> value = new HashMap<String,Integer>();
							    int logAllNum = 0 ;
							    int logFinishNum = 0;
							    value.put("logAllNum", 0);
							    value.put("logFinishNum", 0);
							    if(gridStat.containsKey(gridId)){
							    	value = gridStat.get(gridId);
							    	logAllNum = value.get("logAllNum");
							    	logFinishNum = value.get("logFinishNum");
							    }
							    if(secondWorkStatus==1||secondWorkStatus==2||secondWorkStatus==3){
							    	logAllNum++;
							    	//记录pid所有状态
							    	if(!pids.containsKey(gridId)){
							    		pids.put(gridId, new HashMap<Long,List<Integer>>());
							    	}
							    	Map<Long, List<Integer>> map = pids.get(gridId);
							    	if(!map.containsKey(pid)){
							    		map.put(pid, new ArrayList<Integer>());
							    	}
							    	map.get(pid).add(secondWorkStatus);
							    }
							    if(secondWorkStatus==3){
							    	logFinishNum++;
							    }
							    value.put("logAllNum", logAllNum);
							    value.put("logFinishNum", logFinishNum);
							    	
							    gridStat.put(gridId,value);
							} catch (Exception e) {
								System.out.println("pid:" + pid);
//								e.printStackTrace();
							}
						}
						
						//处理poi完成统计
						for(Map.Entry<Integer,Map<String,Integer>> entry : gridStat.entrySet()){
							int gridId = entry.getKey();
							Map<String, Integer> value = entry.getValue();
							int poiFinishNum = 0;
							//如果该pid有状态为1或2的,则移除
							Set<Long> poiFinishSet = new HashSet<Long>();
							if(pids.containsKey(gridId)){
								Map<Long, List<Integer>> map = pids.get(gridId);
								for(Entry<Long, List<Integer>> en : map.entrySet()){
									long pid = en.getKey();
									List<Integer> statusList = en.getValue();
									if(statusList.contains(1) || statusList.contains(2)){
										continue;
									}
									poiFinishSet.add(pid);
								}
								poiFinishNum = poiFinishSet.size();
							}
							value.put("poiFinishNum", poiFinishNum);
						}
						return gridStat;
					}	
				};
				log.info("POI月库作业数据统计sql:" + selectSql);
				Map<Integer, Map<String, Integer>> result = run.query(conn, selectSql,rsHandler);
				
				LogReader lr = new LogReader(conn);
				//获取所有采集子任务集合
				ManApi manApi = (ManApi)ApplicationContextUtil.getBean("manApi");
				Set<Integer> subtasks = manApi.allCollectSubtaskId();
				Map<Integer, Integer> poiNum = lr.getPoiNumBySubtaskId(ObjectName.IX_POI,subtasks);
				for(Entry<Integer, Integer> entry : poiNum.entrySet()){
					int gridId=entry.getKey();
					result.get(gridId).put("day2MonthPoiNum",0);
					if(result.containsKey(gridId)){
						result.get(gridId).put("day2MonthPoiNum", entry.getValue());
					}else{
						Map<String,Integer> value = new HashMap<String,Integer>();
					    value.put("logAllNum", 0);
					    value.put("logFinishNum", 0);
					    value.put("poiFinishNum", 0);
					    value.put("day2MonthPoiNum", entry.getValue());
						result.put(gridId, value);
					}
				}
				
				List<Map<String,Integer>> grid_month_poi = new ArrayList<Map<String,Integer>>();
				for(Map.Entry<Integer, Map<String,Integer>> entry:result.entrySet()){
					Map<String,Integer> cell = new HashMap<String,Integer>();
					cell.put("gridId", entry.getKey());
					cell.put("logAllNum", entry.getValue().get("logAllNum"));
					cell.put("logFinishNum", entry.getValue().get("logFinishNum"));
					cell.put("poiFinishNum", entry.getValue().get("poiFinishNum"));
					cell.put("day2MonthPoiNum", entry.getValue().get("day2MonthPoiNum"));
					grid_month_poi.add(cell);
				}
				
				Map<String,List<Map<String,Integer>>> temp = new HashMap<String,List<Map<String,Integer>>>();
				temp.put("grid_month_poi", grid_month_poi);
				stats.put(dbId, temp);

			}catch(Exception e){
				log.error(e.getMessage(),e);
				throw new ThreadExecuteException("dbId("+dbId+")POI月库作业数据统计失败");
			}finally{
				DbUtils.closeQuietly(conn);
				if(latch!=null){
					latch.countDown();
				}
			}
		}
		
	}

}
