package com.navinfo.dataservice.engine.man.job.Day2Month;

import com.navinfo.dataservice.api.fcc.iface.FccApi;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.man.job.JobPhase;
import com.navinfo.dataservice.engine.man.job.bean.InvokeType;
import com.navinfo.dataservice.engine.man.job.bean.ItemType;
import com.navinfo.dataservice.engine.man.job.bean.JobProgressStatus;
import com.navinfo.dataservice.engine.man.job.operator.JobProgressOperator;
import com.navinfo.navicommons.database.QueryRunner;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.util.List;
import java.util.Set;

/**
 * Created by wangshishuai3966 on 2017/7/14.
 */
public class CloseMeshPhase extends JobPhase {
    private static Logger log = LoggerRepos.getLogger(CloseMeshPhase.class);

    @Override
    public void initInvokeType() {
        this.invokeType = InvokeType.SYNC;
    }

    @Override
    public JobProgressStatus run() throws Exception {
        Connection conn = null;
        Connection meta = null;
        JobProgressOperator jobProgressOperator = null;
        try {
            conn = DBConnector.getInstance().getManConnection();
            //更新状态为进行中
            jobProgressOperator = new JobProgressOperator(conn);
            jobProgressOperator.updateStatus(jobProgress, JobProgressStatus.RUNNING);
            conn.commit();

            //业务逻辑
            if(jobRelation.getItemType()== ItemType.PROJECT) {
                //按项目落需要关闸
                JSONObject outPrarm = JSONObject.fromObject(lastJobProgress.getOutParameter());
                List<Integer> meshs = (List<Integer>) JSONArray.toCollection(outPrarm.getJSONArray("allQuickMeshes"));
                log.info("获取日落月全图幅"+meshs.toString());
                FccApi fccApi = (FccApi) ApplicationContextUtil.getBean("fccApi");

                Set<Integer> collectTaskSet = Day2MonthUtils.getTaskIdSet(conn, jobRelation.getItemId());
                Set<Integer> tipsMeshset = fccApi.getTipsMeshIdSet(collectTaskSet);
                log.info("获取tips全图幅"+tipsMeshset.toString());

                tipsMeshset.addAll(meshs);
                String updateSql = "UPDATE SC_PARTITION_MESHLIST SET OPEN_FLAG = 0 WHERE MESH IN "
                        + tipsMeshset.toString().replace("[", "(").replace("]", ")");
                meta = DBConnector.getInstance().getMetaConnection();
                QueryRunner run = new QueryRunner();
                run.update(meta, updateSql);
            }
            //更新状态为成功
            jobProgressOperator.updateStatus(jobProgress, JobProgressStatus.SUCCESS);
            return jobProgress.getStatus();
        } catch (Exception ex) {
            //有异常，更新状态为执行失败
            log.error(ExceptionUtils.getStackTrace(ex));
            DbUtils.rollback(conn);
            DbUtils.rollback(meta);
            if (jobProgressOperator != null && jobProgress != null) {
                jobProgress.setOutParameter(ex.getMessage());
                jobProgressOperator.updateStatus(jobProgress, JobProgressStatus.FAILURE);
            }
            throw ex;
        } finally {
            DbUtils.commitAndCloseQuietly(conn);
            DbUtils.commitAndCloseQuietly(meta);
        }
    }
}
