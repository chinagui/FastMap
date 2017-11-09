package com.navinfo.dataservice.column.job;

import java.sql.Connection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.dbutils.DbUtils;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.bizcommons.sys.SysLogConstant;
import com.navinfo.dataservice.bizcommons.sys.SysLogOperator;
import com.navinfo.dataservice.bizcommons.sys.SysLogStats;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.ServiceInvokeUtil;
import com.navinfo.dataservice.dao.plus.editman.PoiEditStatus;
import com.navinfo.dataservice.dao.plus.operation.OperationSegment;
import com.navinfo.dataservice.engine.editplus.operation.imp.InfoPoiMultiSrcDayImportor;
import com.navinfo.dataservice.engine.editplus.operation.imp.InfoPoiMultiSrcDayImportorCommand;
import com.navinfo.dataservice.engine.editplus.operation.imp.MultiSrcUploadPois;
import com.navinfo.dataservice.engine.editplus.operation.imp.PoiRelationImportor;
import com.navinfo.dataservice.engine.editplus.operation.imp.PoiRelationImportorCommand;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import net.sf.json.JSONObject;

/** 
 * @ClassName: InfoPoiMultiSrc2FmDayJob
 * @author zl
 * @date 2017年11月1日
 * @Description: InfoPoiMultiSrc2FmDayJob.java
 */
public class InfoPoiMultiSrc2FmDayJob extends AbstractJob {
	
	
	Map<String,String> errLog=new ConcurrentHashMap<String,String>();
	
	JSONObject resJson=new JSONObject();
	
	public InfoPoiMultiSrc2FmDayJob(JobInfo jobInfo) {
		super(jobInfo);
	}

	@Override
	public void execute() throws JobException {
		
		try{
			
			String beginTime = DateUtils.getSysDateFormat();
			InfoPoiMultiSrc2FmDayJobRequest req = (InfoPoiMultiSrc2FmDayJobRequest)request;
			//1.获取一级数据型poi情报数据 参数:
			int dbid = req.getDbId();
			int taskId = req.getTaskId();
			int subtaskId = req.getSubtaskId();
			String bSourceId = req.getBSourceId();    
			JSONObject poiJobj = req.getData();
			
			String fid = null;
			if(poiJobj != null){
				fid = poiJobj.getString("fid");
			}
			//执行导入
			Set<Long> pids = imp(dbid,taskId,subtaskId,poiJobj);
			long pid = 0;
			if(pids != null && pids.size() > 0){
				for(Long p : pids){
					pid = p;
				}
			}
			response("导入完成",null);
			
			//增加log 日志
			insertStatisticsInfoNoException(beginTime);
			
			response("生成统计结果完成",null);
			//通知多源
			notifyMultiSrc(fid,pid,bSourceId,subtaskId);
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new JobException(e.getMessage(),e);
		}finally {
			
		}
	}
	

	/**
	 * @Title: insertStatisticsInfoNoException
	 * @Description: 增加统计日志
	 * @param jobId
	 * @param subtaskId
	 * @param userId
	 * @param result  void
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年8月10日 下午12:29:41 
	 */
	private void insertStatisticsInfoNoException(String beginTime)  {
		try{
			//设置导入成功状态
			long jobId = jobInfo.getId();
			
			SysLogStats log = new SysLogStats();
			log.setLogType(SysLogConstant.INFO_POI_MULTI_IMPORT_TYPE);
			log.setLogDesc(SysLogConstant.INFO_POI_MULTI_IMPORT_DESC+",jobId :"+jobId+"");
			log.setFailureTotal(errLog.size());
			log.setSuccessTotal(resJson.getInt("success"));  
			log.setTotal(errLog.size()+resJson.getInt("success"));
			log.setBeginTime(beginTime);
			log.setEndTime(DateUtils.getSysDateFormat());
			log.setErrorMsg(resJson.getJSONObject("fail").toString());
			log.setUserId("0");
			SysLogOperator.getInstance().insertSysLog(log);
		
		}catch (Exception e) {
			log.error("记录多源日志出错："+e.getMessage(), e);
		}
	}
	
	/**
	 * @Title: imp
	 * @Description: 开始执行导入
	 * @param dbId
	 * @param taskId
	 * @param subtaskId
	 * @param poiJobj
	 * @return
	 * @throws Exception  Set<Long>
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年11月3日 下午1:53:25 
	 */
	private Set<Long> imp(int dbId, int taskId, int subtaskId, JSONObject poiJobj)throws Exception{
		Connection conn=null;
		Set<Long> pids = null;
		try{
			conn=DBConnector.getInstance().getConnectionById(dbId);
			log.info("dbId: "+dbId);
			long t = System.currentTimeMillis();
			MultiSrcUploadPois upoi = new MultiSrcUploadPois();
			upoi.addJsonPoi(poiJobj);
			//导入数据
			InfoPoiMultiSrcDayImportorCommand cmd = new InfoPoiMultiSrcDayImportorCommand(dbId,upoi);
			InfoPoiMultiSrcDayImportor imp = new InfoPoiMultiSrcDayImportor(conn,null);
			imp.operate(cmd);
			imp.persistChangeLog(OperationSegment.SG_ROW, jobInfo.getUserId());
			
			pids = imp.getPids();
			//数据打多源标识
			Date uploadDate = new Date();
			PoiEditStatus.insertPoiEditStatus(conn, imp.getInsertPids(),2);//status = 2 已作业(待提交)
			PoiEditStatus.updatePoiEditStatus(conn, imp.getPids(), 2, 1, uploadDate);//status = 2 已作业(待提交)
			PoiEditStatus.tagMultiSrcPoi(conn, imp.getPids(),taskId,subtaskId);
			//导入父子关系
			PoiRelationImportorCommand relCmd = new PoiRelationImportorCommand();
			relCmd.setPoiRels(imp.getParentPid());
			PoiRelationImportor relImp = new PoiRelationImportor(conn,imp.getResult());
			relImp.operate(relCmd);
			relImp.persistChangeLog(OperationSegment.SG_ROW, jobInfo.getUserId());
			
			//处理同一关系：删除数据删除同一关系
			PoiRelationImportorCommand relCmd2 = new PoiRelationImportorCommand();
			relCmd2.setPoiRels(imp.getSamePoiPid());
			relImp.operate(relCmd2);
			relImp.persistChangeLog(OperationSegment.SG_ROW, jobInfo.getUserId());
			
			errLog.putAll(imp.getErrLog());
			log.debug("dbId("+dbId+")转入成功。");			
		
			//写统计结果
			resJson.put("success", 1-errLog.size());
			JSONObject failJson=new JSONObject();
			failJson.put("count", errLog.size());
			failJson.put("fids", errLog);
			resJson.put("fail", failJson);
			//设置导入成功状态
//			syncApi.updateMultiSrcFmSyncStatus(MultiSrcFmSync.STATUS_IMP_SUCCESS,jobInfo.getId());
			log.debug("导入完成，用时"+((System.currentTimeMillis()-t)/1000)+"s");
			return pids;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(),e);
			//设置导入失败状态
//			syncApi.updateMultiSrcFmSyncStatus(MultiSrcFmSync.STATUS_IMP_FAIL,jobInfo.getId());
			throw e;
		}finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}


	private void notifyMultiSrc(String fid, long pid, String bSourceId, int subtaskId){
		try{
			//
			log.debug("开始通知info ");
			int isAdopted = errLog.size() > 0?1:2;
			String denyRemark = resJson.getJSONObject("fail").toString();
			log.debug("isAdopted :"+isAdopted);
			log.debug("denyRemark :"+denyRemark);
			
			String infoUrl = SystemConfigFactory.getSystemConfig().getValue(PropConstant.infoPoiNotifyUrl);
			log.info("infoUrl: "+infoUrl);
			Map<String,String> parMap = new HashMap<String, String>();
	        	parMap.put("parameter","{\"fid\":\""+fid+"\",\"pid\":"+pid+",\"bSourceId\":\""+bSourceId+"\",\"subtaskId\":"+subtaskId+",\"isAdopted\":"+isAdopted+",\"denyRemark\":"+denyRemark+"}");
	        log.info("parameter: "+parMap.get("parameter"));
	        String result = ServiceInvokeUtil.invokeByGet(infoUrl,parMap);
			
			log.info("notify info poi result:"+result);
//			syncApi.updateMultiSrcFmSyncStatus(MultiSrcFmSync.STATUS_NOTIFY_SUCCESS,jobInfo.getId());
		}catch(Exception e){
			try{
//				syncApi.updateMultiSrcFmSyncStatus(MultiSrcFmSync.STATUS_NOTIFY_FAIL,jobInfo.getId());
			}catch(Exception ex){
				log.error(ex.getMessage(),ex);
			}
			log.warn("通知info 库时发生错误，请联系运维!");
			log.error(e.getMessage(),e);
		}
	}

}
