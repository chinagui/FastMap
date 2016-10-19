package com.navinfo.dataservice.edit.job;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.api.edit.iface.DatalockApi;
import com.navinfo.dataservice.api.edit.model.FmEditLock;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Region;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.exception.LockException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;
import com.navinfo.dataservice.jobframework.runjob.JobCreateStrategy;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.geo.computation.GridUtils;

/**
 * 
 * @author zhangxiaoyi
 * 本提交模块是整体grids一起提交，暂不支持部分提交
 *
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
		EditPoiBaseReleaseJobRequest releaseJobRequest=(EditPoiBaseReleaseJobRequest) request;
		try{
			log.info("check exception1");
			//判断是否有检查错误，有检查错误则直接返回不进行后续步骤
			List<Integer> errorGrid=hasException(releaseJobRequest);
			List<Integer> allGrid=releaseJobRequest.getGridIds();
			//将有错误的grid排除
			allGrid.removeAll(errorGrid);
			if(errorGrid.size()!=0){
				log.error("has exception,connot commit!");
				Map<String, List<Integer>> returnGrid=new HashMap<String, List<Integer>>();
				returnGrid.put("有检查错误的grid", errorGrid);
				super.response("grids中有检查错误，不能提交！",returnGrid);}
			if(allGrid.size()==0){
				throw new Exception("所有grid均存在检查错误，终止提交操作！");
			}
			//1对grids提取数据执行gdb检查
			log.info("start gdb check");
			JobInfo valJobInfo = new JobInfo(jobInfo.getId(), jobInfo.getGuid());
			releaseJobRequest.getSubJobRequest("validation").setAttrValue("grids", allGrid);
			AbstractJob valJob = JobCreateStrategy.createAsSubJob(valJobInfo,
					releaseJobRequest.getSubJobRequest("validation"), this);
			valJob.run();
			if (valJob.getJobInfo().getStatus() != 3) {
				String msg = (valJob.getException()==null)?"未知错误。":"错误："+valJob.getException().getMessage();
				throw new Exception("执行检查job内部发生"+msg);
			}
			int valDbId=valJob.getJobInfo().getResponse().getInt("valDbId");
			jobInfo.addResponse("val&BatchDbId", valDbId);
			log.info("end gdb check");
			//判断是否有检查错误，有检查错误则直接返回不进行后续步骤
			log.info("check exception2");
			errorGrid=hasException(releaseJobRequest);
			//将有错误的grid排除
			allGrid.removeAll(errorGrid);
			if(errorGrid.size()!=0){
				log.error("has exception,connot commit!");
				Map<String, List<Integer>> returnGrid=new HashMap<String, List<Integer>>();
				returnGrid.put("有检查错误的grid", errorGrid);
				super.response("grids中有检查错误，不能提交！",returnGrid);}
			if(allGrid.size()==0){
				throw new Exception("所有grid均存在检查错误，终止提交操作！");
			}
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
			//修改数据提交状态
			log.info("start change poi_edit_status=3 commit");
			commitPoi(allGrid,releaseJobRequest);
			log.info("end change poi_edit_status=3 commit");
			super.response("POI行编提交成功！",null);
		}catch (Exception e){
			log.error(e.getMessage(), e);
			throw new JobException(e.getMessage(),e);
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
	 * grids范围内poi编辑状态修改:2已作业-->3已提交
	 * @param releaseJobRequest
	 * @throws Exception
	 */
	public void commitPoi(List<Integer> grids,EditPoiBaseReleaseJobRequest releaseJobRequest) throws Exception{
		Connection conn = null;
		try{
			String wkt = GridUtils.grids2Wkt((JSONArray) grids);
			String sql="UPDATE POI_EDIT_STATUS E"
					+ " SET E.STATUS = 3,FRESH_VERIFIED=0 "
					+ " WHERE E.STATUS = 2"
					+ "   AND EXISTS (SELECT 1"
					+ "          FROM IX_POI P"
					+ "         WHERE SDO_WITHIN_DISTANCE(P.GEOMETRY,SDO_GEOMETRY('"+wkt+"',8307),'MASK=ANYINTERACT') = 'TRUE'"
					+ "           AND P.ROW_ID = E.ROW_ID)";
			
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
	
	/**
	 * 是否有检查错误 false:有检查错误 true：无检查错误
	 * @return
	 */
	public List<Integer> hasException(EditPoiBaseReleaseJobRequest releaseJobRequest) throws Exception{
		Connection conn = null;
		try{
			String sql="SELECT DISTINCT G.GRID_ID FROM NI_VAL_EXCEPTION_GRID G "
					+ "WHERE G.GRID_ID IN ("+org.apache.commons.lang.StringUtils.join(releaseJobRequest.getGridIds(),",")+")"
							+ " AND EXISTS ("
							+ " SELECT 1 FROM CK_RESULT_OBJECT O "
							+ " WHERE (O.table_name like 'IX_POI\\_%' ESCAPE '\\' OR O.table_name ='IX_POI')"
							+ "   AND O.MD5_CODE=G.MD5_CODE)";
			conn = DBConnector.getInstance().getConnectionById(releaseJobRequest.getTargetDbId());
			ResultSetHandler<List<Integer>> rsHandler = new ResultSetHandler<List<Integer>>(){
				public List<Integer> handle(ResultSet rs) throws SQLException {
					List<Integer> errorGrid=new ArrayList<Integer>();
					while(rs.next()){
						errorGrid.add(rs.getInt(1));
					}
					return errorGrid;
				}	    		
			};		
			QueryRunner run = new QueryRunner();		
			List<Integer> errorGrid=run.query(conn, sql,rsHandler);
			return errorGrid;
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw e;
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

}
