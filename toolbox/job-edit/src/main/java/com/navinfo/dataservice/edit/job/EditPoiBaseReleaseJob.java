package com.navinfo.dataservice.edit.job;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

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
			//判断是否有检查错误，有检查错误则直接返回不进行后续步骤
			if(!hasException(releaseJobRequest)){
				super.response("grids中有检查错误，不能提交！",null);
				throw new Exception("grids中有检查错误，不能提交！");}
			//1对grids提取数据执行gdb检查
			JSONObject validationResponseJson=exeGdbValidationJob(releaseJobRequest);
			int valDbId=validationResponseJson.getInt("valDbId");
			jobInfo.getResponse().put("val&BatchDbId", valDbId);
			//判断是否有检查错误，有检查错误则直接返回不进行后续步骤
			if(!hasException(releaseJobRequest)){
				super.response("grids中有检查错误，不能提交！",null);
				throw new Exception("grids中有检查错误，不能提交！");}			
			//对grids执行批处理
			JSONObject batchResponseJson=exeGdbBatchJob(valDbId, releaseJobRequest);
			//修改数据提交状态
			commitPoi(releaseJobRequest);
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
			String dbType = null;
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
	public void commitPoi(EditPoiBaseReleaseJobRequest releaseJobRequest) throws Exception{
		Connection conn = null;
		try{
			String wkt = GridUtils.grids2Wkt((JSONArray) releaseJobRequest.getGridIds());
			String sql="UPDATE POI_EDIT_STATUS E"
					+ " SET E.STATUS = 3"
					+ " WHERE E.STATUS = 2"
					+ "   AND EXISTS (SELECT 1"
					+ "          FROM IX_POI P"
					+ "         WHERE SDO_RELATE(P.GEOMETRY,SDO_GEOMETRY('"+wkt+"',8307),'MASK=ANYINTERACT') = 'TRUE'"
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
	public boolean hasException(EditPoiBaseReleaseJobRequest releaseJobRequest) throws Exception{
		Connection conn = null;
		try{
			String sql="SELECT 1 FROM NI_VAL_EXCEPTION_GRID G "
					+ "WHERE G.GRID_ID IN ("+org.apache.commons.lang.StringUtils.join(releaseJobRequest.getGridIds(),",")+")";
			conn = DBConnector.getInstance().getConnectionById(releaseJobRequest.getTargetDbId());
			ResultSetHandler<Integer> rsHandler = new ResultSetHandler<Integer>(){
				public Integer handle(ResultSet rs) throws SQLException {
					while(rs.next()){
						return rs.getInt(1);
					}
					return 0;
				}	    		
			};		
			QueryRunner run = new QueryRunner();		
			int exceptionCount=run.query(conn, sql,rsHandler);
			if(exceptionCount==0){return true;}
			else{return false;}
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw e;
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	public JSONObject exeGdbValidationJob(EditPoiBaseReleaseJobRequest releaseJobRequest) throws Exception{
		//1.1检查参数构建
		JSONObject validationRequestJSON=new JSONObject();
		validationRequestJSON.put("grids", releaseJobRequest.getGridIds());
		validationRequestJSON.put("rules", releaseJobRequest.getCheckRuleList());
		validationRequestJSON.put("targetDbId", releaseJobRequest.getTargetDbId());
		validationRequestJSON.put("type", "gdbValidation");
		validationRequestJSON.put("createValDb", releaseJobRequest.createDbJSON("validation temp db"));
		validationRequestJSON.put("expValDb", releaseJobRequest.expDbJSON());
		
		AbstractJobRequest gdbValidationRequest=JobCreateStrategy.createJobRequest("gdbValidation", validationRequestJSON);
		//创建检查任务并执行
		JobInfo gdbValidationJobInfo=new JobInfo(jobInfo.getId(),jobInfo.getGuid());
		AbstractJob gdbValidationJob=JobCreateStrategy.createAsSubJob(gdbValidationJobInfo, gdbValidationRequest, this);
		gdbValidationJob.run();
		JSONObject validationResponseJson=gdbValidationJob.getJobInfo().getResponse();
		if(validationResponseJson.getInt("exeStatus")!=3){
			throw new Exception("检查job执行失败。");
		}
		return validationResponseJson;
	}
	
	public JSONObject exeGdbBatchJob(int batchDbId,EditPoiBaseReleaseJobRequest releaseJobRequest) throws Exception{
		//1.1批处理参数构建
		JSONObject batchRequestJSON=new JSONObject();
		batchRequestJSON.put("grids", releaseJobRequest.getGridIds());
		batchRequestJSON.put("rules", releaseJobRequest.getBatchRuleList());
		batchRequestJSON.put("targetDbId", releaseJobRequest.getTargetDbId());
		batchRequestJSON.put("batchDbId", batchDbId);
		batchRequestJSON.put("type", "gdbBatch");
		batchRequestJSON.put("createBatchDb", releaseJobRequest.createDbJSON("batch temp db"));
		batchRequestJSON.put("expBatchDb", releaseJobRequest.expDbJSON());
		batchRequestJSON.put("createBakDb", releaseJobRequest.createDbJSON("batch bak db"));
		
		String copyBakDbString="{\"type\":\"gdbFullCopy\",\"request\":{\"featureType\":\"all\"}}";
		JSONObject copyBakDbRequestJSON=JSONObject.fromObject(copyBakDbString);
		batchRequestJSON.put("copyBakDb", copyBakDbRequestJSON);
		
		String diffString="{\"type\":\"diff\",\"request\":{}}";
		JSONObject diffRequestJSON=JSONObject.fromObject(diffString);
		batchRequestJSON.put("diff", diffRequestJSON);
		
		String commitString="{\"type\":\"batchLogFlush\",\"request\":{}}";
		JSONObject commitRequestJSON=JSONObject.fromObject(commitString);
		batchRequestJSON.put("commit", commitRequestJSON);
		
		AbstractJobRequest gdbBatchRequest=JobCreateStrategy.createJobRequest("gdbBatch", batchRequestJSON);
		
		JobInfo gdbBatchJobInfo=new JobInfo(jobInfo.getId(),jobInfo.getGuid());
		AbstractJob gdbBatchJob=JobCreateStrategy.createAsSubJob(gdbBatchJobInfo,gdbBatchRequest, this);
		gdbBatchJob.run();
		JSONObject BatchResponseJson=gdbBatchJob.getJobInfo().getResponse();
		if(BatchResponseJson.getInt("exeStatus")!=3){
			throw new Exception("批处理job执行失败。");
		}
		return BatchResponseJson;
	}

}
