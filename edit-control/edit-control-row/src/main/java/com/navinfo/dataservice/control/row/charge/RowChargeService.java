package com.navinfo.dataservice.control.row.charge;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;

import com.navinfo.dataservice.api.job.iface.JobApi;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Region;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.jobframework.service.JobService;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName RowChargeController
 * @author Han Shaoming
 * @date 2017年7月17日 下午6:47:10
 * @Description TODO
 */
@Controller
public class RowChargeService {
	protected Logger log = LoggerRepos.getLogger(this.getClass());
//	private volatile static RowChargeService instance;
//	public static RowChargeService getInstance(){
//		if(instance==null){
//			synchronized(RowChargeService.class){
//				if(instance==null){
//					instance=new RowChargeService();
//				}
//			}
//		}
//		return instance;
//	}
//	private RowChargeService(){}
	
	/**
	 * 处理充电站数据
	 * @author Han Shaoming
	 * @param type
	 * @param time
	 * @param syncTime 
	 * @return
	 * @throws Exception 
	 */
	public JSONObject chargePoiConvertor(int type, String lastSyncTime, String syncTime,List<Integer> dbIdList) throws Exception {
		log.info("开始数据转化,获取所有的大区库");
		JSONObject result = null;
		ManApi manApi = (ManApi)ApplicationContextUtil.getBean("manApi");
		List<Region> regionList = manApi.queryRegionList();
		
		if(type==1){
			//初始化
			Fm2ChargeInit fm2ChargeInit = new Fm2ChargeInit();
			result = fm2ChargeInit.excute(regionList,dbIdList);
		}else if(type==2){
			//增量
			Fm2ChargeAdd fm2ChargeAdd = new Fm2ChargeAdd();
			result = fm2ChargeAdd.excute(regionList,lastSyncTime,syncTime,dbIdList);
		}
		log.info("数据转化结束");
		return result;
	}
	
	/**
	 * 处理充电站照片数据
	 * @author Han Shaoming
	 * @param type
	 * @param time
	 * @param syncTime 
	 * @return
	 * @throws Exception 
	 */
	public String chargePhotoConvertor(int type, String lastSyncTime, String syncTime,List<Integer> dbIdList) throws Exception {
		log.info("开始照片数据转化,获取所有的大区库");
		String result = null;
		JobApi jobApi = (JobApi) ApplicationContextUtil.getBean("jobApi");
		//目录
		String rootDownloadPath = SystemConfigFactory.getSystemConfig().getValue(PropConstant.downloadFilePathRoot);
		String curYm = DateUtils.getCurYyyymm();
		String monthDir = rootDownloadPath+"chargeHome"+File.separator+curYm+File.separator;
		
		if(type==1){
			String jobType = "fm2ChargePhotoInit";
			//判断是否有未执行完的导入任务
			boolean running = isRunning(jobType);
			if(running){
				return "有正在执行的job任务("+jobType+"),本次导出照片的初始化包的任务不执行";
			}
			//创建初始化job任务
			JSONObject job = new JSONObject();
			job.put("dbIds", dbIdList);
			//创建job任务,获取jobId
			log.info("create job:jobType="+jobType+",request="+job.toString());
			long jobId = jobApi.createJob(jobType, job, 0, 0,"创建FM大区库导入桩家的照片初始化包");
			result = "正在导出照片的初始化包,请稍等!jobId("+jobId+"),目录为("+monthDir+")";
		}else if(type==2){
			String jobType = "fm2ChargePhotoAdd";
			//判断是否有未执行完的导入任务
			boolean running = isRunning(jobType);
			if(running){
				return "有正在执行的job任务("+jobType+"),本次导出照片的增量包的任务不执行";
			}
			//创建增量job任务
			JSONObject job = new JSONObject();
			job.put("dbIds", dbIdList);
			job.put("syncTime", syncTime);
			job.put("lastSyncTime", lastSyncTime);
			//创建job任务,获取jobId
			log.info("create job:jobType="+jobType+",request="+job.toString());
			long jobId = jobApi.createJob(jobType, job, 0, 0,"创建FM大区库导入桩家的照片增量包");
			result = "正在导出照片的增量包,请稍等!jobId("+jobId+"),目录为("+monthDir+")";
		}
		log.info("照片数据转化结束");
		return result;
	}
	
	/**
	 * 判断是否已经有相同类型的job正在执行
	 * @return
	 * @throws Exception 
	 */
	protected boolean isRunning(String jobType) throws Exception{
		Map<String, Object> jobDetail = JobService.getInstance().getJobByTask(0,Long.valueOf(0),jobType);
		if(jobDetail==null||jobDetail.size()==0){return false;}
		int status=(int) jobDetail.get("status");
		int jobId = (int) jobDetail.get("jobId");
		if(status==3||status==4){
			return false;
		}
		log.info("有正在执行的"+jobType+"任务jobId("+jobId+"),本次照片导出任务不执行");
		return true;
	};
	
}
