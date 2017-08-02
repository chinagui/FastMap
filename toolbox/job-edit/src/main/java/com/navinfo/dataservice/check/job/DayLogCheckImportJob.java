package com.navinfo.dataservice.check.job;

import com.navinfo.dataservice.api.datahub.iface.DatahubApi;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.edit.iface.DatalockApi;
import com.navinfo.dataservice.api.edit.model.FmEditLock;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.impcore.flushbylog.FlushResult;
import com.navinfo.dataservice.impcore.flusher.DefaultLogFlusher;
import com.navinfo.dataservice.impcore.flusher.LogFlusher;
import com.navinfo.dataservice.impcore.mover.CopBatchLogMover;
import com.navinfo.dataservice.impcore.mover.DefaultLogMover;
import com.navinfo.dataservice.impcore.mover.LogMoveResult;
import com.navinfo.dataservice.impcore.mover.LogMover;
import com.navinfo.dataservice.impcore.selector.LogSelector;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.exception.LockException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.sql.DBUtils;
import net.sf.json.JSONObject;
import oracle.sql.STRUCT;
import org.apache.commons.dbutils.DbUtils;

import javax.xml.crypto.Data;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ly on 2017/8/1.
 */
public class DayLogCheckImportJob extends AbstractJob {

    protected FmEditLock editLock = null;

    public DayLogCheckImportJob(JobInfo jobInfo) {
        super(jobInfo);
    }

    @Override
    public void execute() throws JobException {

        LogSelector logSelector = null;

        boolean commitStatus = false;

        try {
            DayLogCheckImportJobRequest req = (DayLogCheckImportJobRequest) request;

            DatahubApi datahub = (DatahubApi) ApplicationContextUtil.getBean("datahubApi");

            DbInfo logDbInfo = datahub.getDbById(req.getLogDbId());

            DbInfo tarDbInfo = datahub.getDbById(req.getTargetDbId());

            OracleSchema tarSchema = new OracleSchema(
                    DbConnectConfig.createConnectConfig(tarDbInfo.getConnectParam()));

            OracleSchema logSchema = new OracleSchema(
                    DbConnectConfig.createConnectConfig(logDbInfo.getConnectParam()));

            Date startData = getLogMaxDate(tarSchema);

            if (startData==null)
            {
                startData=  new Date();
            }

            //1. 履历选择
            logSelector = new DayLogCheckNonLockSelector(logSchema, startData);

            String tempTable = logSelector.select();
            response("履历选择完成", null);

            //2. 履历刷库
            LogFlusher logFlusher = new DefaultLogFlusher(logSchema, tarSchema, false, tempTable);
            FlushResult result = logFlusher.flush();
            response("履历刷库完成", null);
            //3. 履历搬迁
            if (req.getLogDbId() == req.getTargetDbId()) {
                response("履历搬迁完成,相同库无须搬履历", null);
            } else {
                LogMover logMover = null;
                if (req.getLogMoveType().equals("copBatch")) {
                    logMover = new CopBatchLogMover(logSchema, tarSchema, tempTable, null);
                } else {
                    logMover = new DefaultLogMover(logSchema, tarSchema, tempTable, null);
                }
                LogMoveResult moveResult = logMover.move();
                response("履历搬迁完成->Action:" + moveResult.getLogActionMoveCount() + ",Operation:" + moveResult.getLogOperationMoveCount() + ",Detail:"
                        + moveResult.getLogDetailMoveCount() + ",Grid:" + moveResult.getLogDetailGridMoveCount(), null);
            }
            commitStatus = true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new JobException("job执行过程出错，" + e.getMessage(), e);
        } finally {
            if (logSelector != null) {
                try {
                    logSelector.unselect(commitStatus);
                } catch (Exception e) {
                    log.warn("履历重置状态时发生错误，请手工对应。" + e.getMessage(), e);
                }
            }
        }
    }

//    @Override
//    public void lockResources() throws LockException {
//        DayLogCheckImportJobRequest req = (DayLogCheckImportJobRequest) request;
//        // 根据批处理的目标库找到对应的大区
//        try {
//            DatalockApi datalock = (DatalockApi) ApplicationContextUtil.getBean("datalockApi");
////			editLock = datalock.lockGrid(req.getTargetDbId(), FmEditLock.LOCK_OBJ_ALL, req.getGrids(), FmEditLock.TYPE_COMMIT, jobInfo.getId());
//        } catch (Exception e) {
//            log.error(e.getMessage(), e);
//            throw new LockException("加锁发生错误," + e.getMessage(), e);
//        }
//    }
//
//    @Override
//    public void unlockResources() throws LockException {
//        if (editLock == null)
//            return;
//        try {
//            DatalockApi datalock = (DatalockApi) ApplicationContextUtil.getBean("datalockApi");
//            datalock.unlockGrid(editLock.getLockSeq(), editLock.getDbType());
//        } catch (Exception e) {
//            log.error(e.getMessage(), e);
//            throw new LockException("解锁时发生错误," + e.getMessage(), e);
//        }
//    }

    private Date getLogMaxDate(OracleSchema tarSchema) throws Exception {

        Connection conn = null;

        PreparedStatement pstmt = null;

        ResultSet resultSet = null;

        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");

        Date startData =df.parse("19000101000000");
        try {

            conn=  tarSchema.getPoolDataSource().getConnection();
            String sql = "SELECT (MAX(T.OP_DT)) OP_DT FROM LOG_OPERATION T ";

            pstmt = conn.prepareStatement(sql);

            resultSet = pstmt.executeQuery();

            if (resultSet.next()) {

                startData = resultSet.getTimestamp("OP_DT");
            }
            return startData;
        } catch (Exception e) {

            throw new Exception(e);
        } finally {
            DBUtils.closeStatement(pstmt);
            DBUtils.closeResultSet(resultSet);
            DbUtils.closeQuietly(conn);
        }
    }

}
