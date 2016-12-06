package com.navinfo.dataservice.column.job;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.api.datahub.iface.DatahubApi;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.edit.iface.FmMultiSrcSyncApi;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.man.iface.Day2MonthSyncApi;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.FmDay2MonSync;
import com.navinfo.dataservice.api.man.model.Region;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.day2mon.Day2MonPoiLogSelector;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;

import net.sf.json.JSONObject;

/** 
 * @ClassName: Day2MonthPoiMergeJob
 * @author MaYunFei
 * @date 下午8:27:56
 * @Description: POI 日落月融合Job
 * 1.找到那些开关属于打开状态的城市
 * 2.按照大区将这些城市进行分组；key=cityid，value=regionid;
 * 3.按city开始找满足条件的履历
 * 4.分析履历，增加、修改的poi要放到OperationResult 的列表中，为后续的精编批处理、检查做准备
 * 5.将3得到的履历刷到月库；
 * 6.搬履历到月库；
 * 7.月库执行精编批处理检查：根据4得到的OperationResult
 * 8.月库打重分类的标记
 * 9.深度信息打标记；
 * 10.修改day_mon_sync 状态为成功
 * 11.按照城市统计日落月的数据量
 */
public class Day2MonthPoiMergeJob extends AbstractJob {

	public Day2MonthPoiMergeJob(JobInfo jobInfo) {
		super(jobInfo);
	}

	@Override
	public void execute() throws JobException {
		ManApi manApi = (ManApi)ApplicationContextUtil
				.getBean("manApi");
		DatahubApi datahubApi = (DatahubApi)ApplicationContextUtil
				.getBean("datahubApi");
		Day2MonthSyncApi d2mSyncApi = (Day2MonthSyncApi)ApplicationContextUtil
				.getBean("day2MonthSyncApi");
		try {
			log.info("开始获取日落月开关控制信息");
			JSONObject conditionJson = new JSONObject().element("status", 0);
			List<Map<String, Object>> d2mInfoList= manApi.queryDay2MonthList(conditionJson );
			response("获取日落月开关控制信息ok",null);
			
			log.info("开始获取日落月城市信息:城市的基础信息、城市的grid，城市的大区库");
			for(Map<String,Object> d2mInfo:d2mInfoList){
				Integer cityId = (Integer) d2mInfo.get("cityId");
				Map<String, Object> cityInfo = manApi.getCityById(cityId);
				log.info("得到城市基础信息:"+cityInfo);
				List<Integer> gridsOfCity = manApi.queryGridOfCity(cityId);
				log.info("得到城市的grids");
				Integer regionId = (Integer) cityInfo.get("regionId");
				Region regionInfo = manApi.queryByRegionId(regionId);
				log.info("获取大区信息:"+regionInfo);
				Integer dailyDbId = regionInfo.getDailyDbId();
				DbInfo dailyDbInfo = datahubApi.getDbById(dailyDbId);
				log.info("获取dailyDbInfo信息:"+dailyDbInfo);
				Integer monthDbId = regionInfo.getMonthlyDbId();
				DbInfo monthDbInfo = datahubApi.getDbById(monthDbId);
				log.info("获取monthDbInfo信息:"+monthDbInfo);
				FmDay2MonSync lastSyncInfo = d2mSyncApi.queryLastedSyncInfo(cityId);
				log.info("获取最新的成功同步信息："+lastSyncInfo);
				Date syncTimeStamp= new Date();
				FmDay2MonSync curSyncInfo = createSyncInfo(d2mSyncApi, cityId,syncTimeStamp);//记录本次的同步信息
				d2mSyncApi.insertSyncInfo(curSyncInfo);
				Day2MonPoiLogSelector logSelector = new Day2MonPoiLogSelector(dailyDbInfo,gridsOfCity,lastSyncInfo,curSyncInfo);
				String tempTable = logSelector.select();
				
			}
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new JobException(e.getMessage(),e);
		}finally {
		}
		

	}

	private FmDay2MonSync createSyncInfo(Day2MonthSyncApi d2mSyncApi, Integer cityId, Date syncTimeStamp) throws Exception {
		FmDay2MonSync info = new FmDay2MonSync();
		info.setCityId(cityId);
		info.setSyncStatus(FmDay2MonSync.SyncStatusEnum.CREATE.getValue());
		info.setJobId(this.getJobInfo().getId());
		Long sid = d2mSyncApi.insertSyncInfo(info );//写入本次的同步信息
		info.setSid(sid);
		info.setSyncTime(syncTimeStamp);
		return info;
	}
	public static void main(String[] args) throws JobException{
		new Day2MonthPoiMergeJob(null).execute();
	}

}
