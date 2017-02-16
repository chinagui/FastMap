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

import net.sf.json.JSONArray;

import com.navinfo.dataservice.api.edit.iface.DatalockApi;
import com.navinfo.dataservice.api.edit.model.FmEditLock;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Region;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.log.LogDetail;
import com.navinfo.dataservice.dao.plus.log.ObjHisLogParser;
import com.navinfo.dataservice.dao.plus.log.PoiLogDetailStat;
import com.navinfo.dataservice.dao.plus.log.SamepoiLogDetailStat;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.selector.ObjBatchSelector;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.check.Check;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.check.CheckCommand;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.exception.LockException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.geo.computation.GridUtils;

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
			//构造检查参数，执行检查
			OperationResult operationResult=new OperationResult();
			Map<String,Map<Long,BasicObj>> objsMap=new HashMap<String, Map<Long,BasicObj>>();
			objsMap.put(ObjectName.IX_POI, objs);
			objsMap.put(ObjectName.IX_SAMEPOI, sameobjs);
			operationResult.putAll(objsMap);
			
			CheckCommand checkCommand=new CheckCommand();
			checkCommand.setOperationName("POI_ROW_COMMIT");
			
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
			//修改数据提交状态:将没有检查错误的已作业poi进行提交
			log.info("start change poi_edit_status=3 commit");
			commitPoi(myRequest);
			log.info("end change poi_edit_status=3 commit");
			super.response("POI行编提交成功！",null);
		}catch(Exception e){
			log.error("PoiRowValidationJob错误", e);
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new JobException(e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	
	@Override
	public void lockResources() throws LockException {
		EditPoiBaseReleaseJobRequest req = (EditPoiBaseReleaseJobRequest) request;
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
		}
	}

	@Override
	public void unlockResources() throws LockException {
		if (lockSeq < 0)
			return;
		try {
			DatalockApi datalock = (DatalockApi) ApplicationContextUtil.getBean("datalockApi");
			datalock.unlockGrid(lockSeq, dbType);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new LockException("加锁发生错误," + e.getMessage(), e);
		}
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
			Subtask subtask = apiService.queryBySubtaskId((int)jobInfo.getTaskId());
			String sql="SELECT ip.pid"
					+ "  FROM ix_poi ip, poi_edit_status ps"
					+ " WHERE ip.pid = ps.pid"
					+ "   AND ps.work_type = 1 AND ps.status in (1,2)"
					+ "   and ip.u_record!=2"
					+ "   AND sdo_within_distance(ip.geometry,"
					+ "                           sdo_geometry('"+subtask.getGeometry()+"', 8307),"
					+ "                           'mask=anyinteract') = 'TRUE'";
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
	public void commitPoi(EditPoiBaseReleaseJobRequest releaseJobRequest) throws Exception{
		Connection conn = null;
		try{
			String wkt = GridUtils.grids2Wkt((JSONArray) releaseJobRequest.getGridIds());
			String sql="UPDATE POI_EDIT_STATUS E"
					+ "   SET E.STATUS = 3, FRESH_VERIFIED = 0,E.SUBMIT_DATE=SYSDATE,E.COMMIT_HIS_STATUS = 1 "
					+ " WHERE E.STATUS = 2"
					+ "   AND NOT EXISTS (SELECT 1"
					+ "          FROM CK_RESULT_OBJECT R"
					+ "         WHERE R.TABLE_NAME = 'IX_POI'"
					+ "           AND R.PID = E.PID)"
					+ "   AND EXISTS (SELECT 1"
					+ "          FROM IX_POI P"
					+ "         WHERE SDO_WITHIN_DISTANCE(P.GEOMETRY,"
					+ "                                   SDO_GEOMETRY('"+wkt+"', 8307),"
					+ "                                   'MASK=ANYINTERACT') = 'TRUE'"
					+ "           AND P.PID = E.PID)";
			
			conn = DBConnector.getInstance().getConnectionById(releaseJobRequest.getTargetDbId());
	    	QueryRunner run = new QueryRunner();		
	    	run.execute(conn, sql);
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw e;
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

}
