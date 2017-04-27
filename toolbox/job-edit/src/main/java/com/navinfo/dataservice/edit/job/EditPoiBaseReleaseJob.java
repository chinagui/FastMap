package com.navinfo.dataservice.edit.job;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.control.column.core.DeepCoreControl;
import com.navinfo.dataservice.dao.plus.log.LogDetail;
import com.navinfo.dataservice.dao.plus.log.ObjHisLogParser;
import com.navinfo.dataservice.dao.plus.log.PoiLogDetailStat;
import com.navinfo.dataservice.dao.plus.log.SamepoiLogDetailStat;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.operation.OperationSegment;
import com.navinfo.dataservice.dao.plus.selector.ObjBatchSelector;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.Batch;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.BatchCommand;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.check.Check;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.check.CheckCommand;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.exception.LockException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.geo.computation.GridUtils;

import net.sf.json.JSONArray;

/**
 * 
 * @author zhangxiaoyi
 * 按grid提交时，如检查中报log，则该grid下的POI不能一起提交：poi提交时，按单个poi进行处理-------没有检查错误的提交；有检查错误的不提交。
 * 粗编poi提交接口
 */
public class EditPoiBaseReleaseJob extends AbstractJob{
	protected int lockSeq = -1;// 加锁时得到值
	protected String dbType;// 加锁时得到值

	public EditPoiBaseReleaseJob(JobInfo jobInfo) {
		super(jobInfo);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void execute() throws JobException {
		EditPoiBaseReleaseJobRequest myRequest=(EditPoiBaseReleaseJobRequest) request;
		Connection conn=null;
		try{
			conn=DBConnector.getInstance().getConnectionById(myRequest.getTargetDbId());
			log.info("check exception1");
			//1对grids提取数据执行gdb检查
			log.info("EditPoiBaseReleaseJob:获取要检查的数据pid");
			//获取要检查的数据pid
			List<Long> poiPids = getCheckPidList(conn,myRequest);
			log.info("EditPoiBaseReleaseJob:获取要检查的数据的履历");
			//获取log
			Map<Long, List<LogDetail>> logs = PoiLogDetailStat.loadByRowEditStatus(conn, poiPids);
			Map<String, Set<String>> tabNames=ObjHisLogParser.getChangeTableSet(logs);
			log.info("EditPoiBaseReleaseJob:加载检查对象");
			//获取poi对象			
			Map<Long, BasicObj> objs = ObjBatchSelector.selectByPids(conn, ObjectName.IX_POI, tabNames.get(ObjectName.IX_POI), false,
					poiPids, false, false);
			//将poi对象与履历合并起来
			ObjHisLogParser.parse(objs, logs);
			log.info("EditPoiBaseReleaseJob:加载同一关系检查对象");
			//获取poi对象			
			List<Long> groupIds = IxPoiSelector.getIxSamePoiGroupIdsByPids(conn, poiPids);
			log.info("EditPoiBaseReleaseJob:获取要检查的同一关系数据的履历");
			//获取log
			Map<Long, List<LogDetail>> samelogs = SamepoiLogDetailStat.loadByRowEditStatus(conn, poiPids);
			Map<String, Set<String>> sametabNames=ObjHisLogParser.getChangeTableSet(samelogs);
			Map<Long, BasicObj> sameobjs = ObjBatchSelector.selectByPids(conn, ObjectName.IX_SAMEPOI, sametabNames.get(ObjectName.IX_SAMEPOI), false,
					groupIds, false, false);
			//将poi对象与履历合并起来
			ObjHisLogParser.parse(sameobjs, samelogs);
			log.info("EditPoiBaseReleaseJob:执行检查");
			//构造检查参数，执行批处理检查
			OperationResult operationResult=new OperationResult();
			OperationResult changeReferData=new OperationResult();
			Map<String,Map<Long,BasicObj>> objsMap=new HashMap<String, Map<Long,BasicObj>>();
			objsMap.put(ObjectName.IX_POI, objs);
			objsMap.put(ObjectName.IX_SAMEPOI, sameobjs);
			operationResult.putAll(objsMap);
			
			log.info("执行批处理");
			BatchCommand batchCommand=new BatchCommand();		
			batchCommand.setOperationName("BATCH_POI_RELEASE");
			Batch batch=new Batch(conn,operationResult);
			batch.operate(batchCommand);
			changeReferData= batch.getChangeReferData();
			batch.persistChangeLog(OperationSegment.SG_ROW, 0);
			
			CheckCommand checkCommand=new CheckCommand();
			checkCommand.setOperationName(getOperationName());
			
			// 清理检查结果
			log.info("清理检查结果");
			DeepCoreControl deepControl = new DeepCoreControl();
			List<Integer> pidIntList=new ArrayList<Integer>();
			for(Long pidTmp:poiPids){
				pidIntList.add(Integer.valueOf(pidTmp.toString()));
			}
			deepControl.cleanExByCkRule(conn, pidIntList, checkCommand.getRuleIdList(), ObjectName.IX_POI);
			
			Check check=new Check(conn, operationResult);
			check.operate(checkCommand);
			log.info("end EditPoiBaseReleaseJob");
			
			/*JobInfo valJobInfo = new JobInfo(jobInfo.getId(), jobInfo.getGuid());
			releaseJobRequest.getSubJobRequest("validation").setAttrValue("grids",releaseJobRequest.getGridIds());
			AbstractJob valJob = JobCreateStrategy.createAsSubJob(valJobInfo,
					releaseJobRequest.getSubJobRequest("validation"), this);
			valJob.run();
			if (valJob.getJobInfo().getStatus() != 3) {
				String msg = (valJob.getException()==null)?"未知错误。":"错误："+valJob.getException().getMessage();
				throw new Exception("执行检查job内部发生"+msg);
			}
			int valDbId=valJob.getJobInfo().getResponse().getInt("valDbId");
			jobInfo.addResponse("val&BatchDbId", valDbId);*/
			//对grids执行批处理
//			log.info("start gdb batch");
//			JobInfo batchJobInfo = new JobInfo(jobInfo.getId(), jobInfo.getGuid());
//			releaseJobRequest.getSubJobRequest("batch").setAttrValue("grids", allGrid);
//			releaseJobRequest.getSubJobRequest("batch").setAttrValue("batchDbId", valDbId);
//			AbstractJob batchJob = JobCreateStrategy.createAsSubJob(batchJobInfo,
//					releaseJobRequest.getSubJobRequest("batch"), this);
//			batchJob.run();
//			if (batchJob.getJobInfo().getStatus() != 3) {
//				String msg = (batchJob.getException()==null)?"未知错误。":"错误："+batchJob.getException().getMessage();
//				throw new Exception("执行批处理job内部发生"+msg);
//			}
//			log.info("end gdb batch");
			
			//修改父子关系关联批到的数据任务号及状态
			if(changeReferData!=null){changeRefeDataStatus(changeReferData,conn);}
			//修改数据提交状态:将没有检查错误的已作业poi进行提交
			log.info("start change poi_edit_status=3 commit");
			commitPoi(conn);
			log.info("end change poi_edit_status=3 commit");
			super.response("POI行编提交成功！",null);
		}catch(Exception e){
			log.error("EditPoiBaseReleaseJob错误", e);
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new JobException(e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	public String getOperationName() {
		return "POI_ROW_COMMIT";
	}
	
	
	@Override
	public void lockResources() throws LockException {
		/*EditPoiBaseReleaseJobRequest req = (EditPoiBaseReleaseJobRequest) request;
		// 预处理
		try {
			ManApi man = (ManApi) ApplicationContextUtil.getBean("manApi");
			Region r = man.queryRegionByDbId(req.getTargetDbId());
			if (r == null) {
				throw new Exception("根据batchDbId未查询到匹配的区域");
			}
			if (r.getDailyDbId() == req.getTargetDbId()) {
				dbType = FmEditLock.DB_TYPE_DAY;
			} else if (r.getMonthlyDbId() == req.getTargetDbId()) {
				dbType = FmEditLock.DB_TYPE_MONTH;
			}
			DatalockApi datalock = (DatalockApi) ApplicationContextUtil.getBean("datalockApi");
			lockSeq = datalock.lockGrid(r.getRegionId(), FmEditLock.LOCK_OBJ_POI, req.getGridIds(), FmEditLock.TYPE_EDIT_POI_BASE_RELEASE,
					dbType,jobInfo.getId());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new LockException("加锁发生错误," + e.getMessage(), e);
		}*/
	}

	@Override
	public void unlockResources() throws LockException {
		/*if (lockSeq < 0)
			return;
		try {
			DatalockApi datalock = (DatalockApi) ApplicationContextUtil.getBean("datalockApi");
			datalock.unlockGrid(lockSeq, dbType);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new LockException("加锁发生错误," + e.getMessage(), e);
		}*/
	}
	
	/**
	 * 获取行编检查对象pid
	 * 1.pids有值，则直接针对改pid进行检查
	 * 2.pids无值,根据子任务圈查询，待作业/已作业状态的非删除poi列表
	 * @param conn
	 * @param myRequest
	 * @return 
	 * @throws JobException
	 */
	private List<Long> getCheckPidList(Connection conn,
			EditPoiBaseReleaseJobRequest myRequest) throws JobException {
		try{
			ManApi apiService = (ManApi) ApplicationContextUtil
					.getBean("manApi");
			//Subtask subtask = apiService.queryBySubtaskId((int)jobInfo.getTaskId());
			//行编提交由针对删除数据的检查，此处要全部加载
			String sql="SELECT ip.pid"
					+ "  FROM ix_poi ip, poi_edit_status ps"
					+ " WHERE ip.pid = ps.pid"
					+ "   AND ps.status =2"
					+ "   AND ps.FRESH_VERIFIED=0"
					//+ "   and ip.u_record!=2"
					+ " AND (ps.QUICK_SUBTASK_ID="+(int)jobInfo.getTaskId()+" or ps.MEDIUM_SUBTASK_ID="+(int)jobInfo.getTaskId()+") ";
			QueryRunner run=new QueryRunner();
			return run.query(conn, sql,new ResultSetHandler<List<Long>>(){

				@Override
				public List<Long> handle(ResultSet rs) throws SQLException {
					List<Long> pids =new ArrayList<Long>();
					while (rs.next()) {
						pids.add(rs.getLong("PID"));						
					}
					return pids;
				}});
		}catch(Exception e){
			log.error("行编获取检查数据报错", e);
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new JobException(e);
		}
	}
	
	/**
	 * grids范围内poi编辑状态修改:2已作业-->3已提交
	 * @param releaseJobRequest
	 * @throws Exception
	 */
	public void commitPoi(Connection conn) throws Exception{
		//Connection conn = null;
		try{
			//String wkt = GridUtils.grids2Wkt((JSONArray) releaseJobRequest.GET);
			String sql="UPDATE POI_EDIT_STATUS E"
					+ "   SET E.STATUS = 3,E.SUBMIT_DATE=SYSDATE,E.COMMIT_HIS_STATUS = 1 "
					+ " WHERE E.STATUS = 2"
					+ "   AND NOT EXISTS (SELECT 1"
					+ "          FROM CK_RESULT_OBJECT R"
					+ "         WHERE R.TABLE_NAME = 'IX_POI'"
					+ "           AND R.PID = E.PID)"
					+ "    AND (E.QUICK_SUBTASK_ID="+(int)jobInfo.getTaskId()+" or E.MEDIUM_SUBTASK_ID="+(int)jobInfo.getTaskId()+") ";
			
			//conn = DBConnector.getInstance().getConnectionById(releaseJobRequest.getTargetDbId());
	    	QueryRunner run = new QueryRunner();		
	    	run.execute(conn, sql);
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw e;
		}
//		} finally {
//			DbUtils.commitAndCloseQuietly(conn);
//		}
	}
	
	/**
	 * 将status=0且subtaskId=0的数据(采集端无任务数据)，改为status=3 且subtaskIs=当前任务号
	 * 将status=3且subtaskId=“别的任务号” 的数据，改为status=3 且subtaskIs=“当前任务号”
	 * @param releaseJobRequest
	 * @throws Exception
	 */
	public void changeRefeDataStatus(OperationResult data, Connection conn) throws Exception{
		try{
			ManApi apiService = (ManApi) ApplicationContextUtil.getBean("manApi");
			Map<String, Integer> taskInfo = apiService.getTaskBySubtaskId((int)jobInfo.getTaskId());
			Map<String, Integer> newTaskInfo= changeTaskInfo((int)jobInfo.getTaskId(),taskInfo);
			
			StringBuffer sb = new StringBuffer();
			List<BasicObj> objList =data.getAllObjs();
			int i=0;
			for(BasicObj obj:objList){
				IxPoi poi = (IxPoi) obj.getMainrow();
				i++;
				if(i==1){
					sb.append(poi.getPid());
				}else{
					sb.append(",").append(poi.getPid());
				}
			}
			
			int qst=newTaskInfo.get("QUICK_SUBTASK_ID");
			int qt=newTaskInfo.get("QUICK_TASK_ID");
			int mst=newTaskInfo.get("MEDIUM_SUBTASK_ID");
			int mt=newTaskInfo.get("MEDIUM_TASK_ID");
			
			String sql="MERGE INTO poi_edit_status T1 "
					+ "USING (SELECT "
					+ "	(CASE WHEN "+mst+" = 0 THEN T.MEDIUM_SUBTASK_ID WHEN T.STATUS=1 AND T.MEDIUM_SUBTASK_ID=0 THEN "+mst+" WHEN T.STATUS=3 AND T.MEDIUM_SUBTASK_ID NOT IN (0,"+mst+") THEN "+mst+" ELSE T.MEDIUM_SUBTASK_ID END) MST, "
					+ "	(CASE WHEN "+mt+" = 0 THEN T.MEDIUM_TASK_ID WHEN T.STATUS=1 AND T.MEDIUM_TASK_ID=0 THEN "+mt+" WHEN T.STATUS=3 AND T.MEDIUM_TASK_ID NOT IN (0,"+mt+") THEN "+mt+" ELSE T.MEDIUM_TASK_ID END) MT, "
					+ "	(CASE WHEN "+qst+" = 0 THEN T.QUICK_SUBTASK_ID WHEN T.STATUS=1 AND T.QUICK_SUBTASK_ID=0 THEN "+qst+" WHEN T.STATUS=3 AND T.QUICK_SUBTASK_ID NOT IN (0,"+qst+") THEN "+qst+" ELSE T.QUICK_SUBTASK_ID END) QST,"
					+ "	(CASE WHEN "+qt+" = 0 THEN T.QUICK_TASK_ID WHEN T.STATUS=1 AND T.QUICK_TASK_ID=0 THEN "+qt+" WHEN T.STATUS=3 AND T.QUICK_TASK_ID NOT IN (0,"+qt+") THEN "+qt+" ELSE T.QUICK_TASK_ID END) QT,"
					+ "	(CASE WHEN "+mst+" <> 0 AND T.STATUS=0 AND T.MEDIUM_SUBTASK_ID= 0 THEN 3 "
					+ "		  WHEN "+qst+" <> 0 AND T.STATUS=0 AND T.QUICK_SUBTASK_ID= 0 THEN 3 "
					+ "		  ELSE T.STATUS END) B, "
					+ "	0 AS C, "
					+ "	IX.PID AS D "
					+ "	FROM IX_POI IX, POI_EDIT_STATUS T WHERE IX.PID = T.PID(+) AND IX.PID IN ("+ sb.toString() + ")) T2 "
					+ "ON ( T1.pid=T2.d) "
					+ "WHEN MATCHED THEN  "
					+ "UPDATE SET T1.status = T2.b,T1.fresh_verified= T2.c,T1.QUICK_SUBTASK_ID=T2.QST,T1.QUICK_TASK_ID=T2.QT,T1.MEDIUM_SUBTASK_ID=T2.MST,T1.MEDIUM_TASK_ID=T2.MT ";

	    	QueryRunner run = new QueryRunner();		
	    	run.execute(conn, sql);
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw e;
		}
	}
	private Map<String, Integer> changeTaskInfo(int subtaskId,Map<String, Integer> taskInfo) throws Exception {
		Map<String, Integer> newTaskInfo =new HashMap<String, Integer>();
		if(taskInfo.get("programType")==1){
			newTaskInfo.put("MEDIUM_SUBTASK_ID",subtaskId);
			newTaskInfo.put("MEDIUM_TASK_ID",taskInfo.get("taskId"));
			newTaskInfo.put("QUICK_SUBTASK_ID",0);
			newTaskInfo.put("QUICK_TASK_ID",0);
		}else{
			newTaskInfo.put("MEDIUM_SUBTASK_ID",0);
			newTaskInfo.put("MEDIUM_TASK_ID",0);
			newTaskInfo.put("QUICK_SUBTASK_ID",subtaskId);
			newTaskInfo.put("QUICK_TASK_ID",taskInfo.get("taskId"));
		}
		
		return newTaskInfo;
	}

}
