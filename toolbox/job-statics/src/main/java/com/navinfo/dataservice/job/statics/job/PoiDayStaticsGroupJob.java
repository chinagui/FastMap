package com.navinfo.dataservice.job.statics.job;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.springframework.aop.ThrowsAdvice;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.engine.statics.poicollect.PoiCollectStat;
import com.navinfo.dataservice.engine.statics.tools.OracleDao;
import com.navinfo.dataservice.job.statics.AbstractStatJob;
import com.navinfo.dataservice.job.statics.job.model.PoiDailyDbObj;
import com.navinfo.dataservice.job.statics.job.model.PoiTaskDbObj;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.navicommons.exception.ServiceException;

public class PoiDayStaticsGroupJob  extends AbstractStatJob{

	public PoiDayStaticsGroupJob(JobInfo jobInfo) {
		super(jobInfo);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String stat() throws JobException {
		
		String dbName=SystemConfigFactory.getSystemConfig()
				.getValue(PropConstant.fmStat);
		//String statTime = new SimpleDateFormat("yyyyMMddkkmmss").format(new Date());
		
		PoiDayStaticsGroupJobRequest req=(PoiDayStaticsGroupJobRequest) request;
		log.info("start stat "+req.getJobType());
		// 获得 大区库的db_id
		List<Integer> listDbId=null;
		try {
			listDbId = OracleDao.getDbIdDaily();
		} catch (ServiceException e1) {
			log.error("", e1);
			throw new JobException(e1.getMessage());
		}

		//开启3个线程
		ExecutorService threadPool = Executors.newFixedThreadPool(3); 
		List<JSONObject> staticsResultDaily=new ArrayList<JSONObject>();
		List<JSONObject> staticsResultTask=new ArrayList<JSONObject>();
		Map<String, List<JSONObject>> staticsResult=new HashMap<String, List<JSONObject>>();
		try   
        {   
			Map<Integer,Future<Map<String, List<JSONObject>>>> futureList=new HashMap<Integer,Future<Map<String, List<JSONObject>>>>();
			//添加任务
	        for(int dbId:listDbId){
				futureList.put(dbId,threadPool.submit(new SubPoiDailyDb(dbId, dbName,req.getTimestamp())));
			}
	        
	        for(int dbId:futureList.keySet()){
	        	Future<Map<String, List<JSONObject>>> futureTmp = futureList.get(dbId);
	        	Map<String, List<JSONObject>> returnTmp=futureTmp.get();
	        	if(returnTmp!=null&&returnTmp.size()>0){
        			staticsResultDaily.addAll(returnTmp.get("daily"));
        			staticsResultTask.addAll(returnTmp.get("task"));
	        	}
	        }
	        
	        staticsResult.put("dailyGroup", staticsResultDaily);
	        staticsResult.put("taskGroup", staticsResultTask);
        }  
        catch (Exception e)  
        {   
        	log.error("", e);
			throw new JobException(e.getMessage()); 
        }  
        finally  
        {  
            threadPool.shutdownNow();  
        }
		log.info(staticsResult);
		log.info("end stat "+req.getJobType());
		return staticsResult.toString();
	}

}
