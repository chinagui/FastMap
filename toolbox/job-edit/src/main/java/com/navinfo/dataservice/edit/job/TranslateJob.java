package com.navinfo.dataservice.edit.job;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.dbutils.DbUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Title: TranslateJob
 * @Package: com.navinfo.dataservice.edit.job
 * @Description: ${TODO}
 * @Author: Crayeres
 * @Date: 10/16/2017
 * @Version: V1.0
 */
public class TranslateJob extends AbstractJob {

    private final static Integer PART_SIZE = 1000;

    private final static String TABLE_NAME = "TRANSLATE";

    private static final String POOL_SIZE_KEY = "job.threadpool.size";

    public TranslateJob(JobInfo jobInfo) {
        super(jobInfo);
    }

    @Override
    public void execute() throws JobException {
        TranslateJobRequest translateJobRequest = (TranslateJobRequest) request;
        String filePath = translateJobRequest.getFilePath();

        log.info(String.format("translate file path:%s", filePath));

        try {
            //Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
            Class.forName("com.hxtt.sql.access.AccessDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet result = null;

        String dbURL = "jdbc:access:///" + filePath;
        try {
            Properties prop = new Properties();
            prop.setProperty("charSet", "gb2312");
            prop.setProperty("user", "");
            prop.setProperty("password", "");

            conn = DriverManager.getConnection(dbURL, prop);
            conn.setAutoCommit(false);

            pstmt = conn.prepareStatement("SELECT * FROM " + TranslateJob.TABLE_NAME);
            result = pstmt.executeQuery();

            List<TranslateData> datas = new ArrayList<>();

            while (result.next()) {
                TranslateData data = new TranslateData();
                data.id = result.getString("ID");
                data.type = result.getString("TYPE");
                data.kindCode = result.getString("KIND_CODE");
                data.chain = result.getString("CHAIN");
                data.name = result.getString("NAME");
                data.phonetic = result.getString("PHONETIC");
                datas.add(data);
            }

            log.info(String.format("translate data size : %s", datas.size()));

            List<List<TranslateData>> partitions = ListUtils.partition(datas, TranslateJob.PART_SIZE);

            ThreadPoolExecutor executor = new ThreadPoolExecutor(
                    Integer.parseInt(SystemConfigFactory.getSystemConfig().getValue(POOL_SIZE_KEY,"10")),
                    Integer.parseInt(SystemConfigFactory.getSystemConfig().getValue(POOL_SIZE_KEY,"10")),
                    3, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(), new ThreadPoolExecutor.DiscardOldestPolicy());

            for (List<TranslateData> partition : partitions) {
                Task task = new Task(partition);
                executor.execute(task);
            }

            executor.shutdown();

            while (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                log.info(String.format("executor.getPoolSize()：%d，executor.getQueue().size()：%d，executor.getCompletedTaskCo" +
                        "unt()：%d", executor.getPoolSize(), executor.getQueue().size(), executor.getCompletedTaskCount()));
            }

            pstmt = conn.prepareStatement("UPDATE " + TABLE_NAME + " SET NAME_ENG = ? WHERE ID = ?");
            for (int index = 0; index < datas.size(); index++) {
                TranslateData data = datas.get(index);

                //StringBuilder sql = new StringBuilder();
                //sql.append("UPDATE ").append(TABLE_NAME);
                //sql.append(" SET NAME_ENG = '").append(data.nameEng).append("' ");
                //sql.append(" WHERE ID = '").append(data.id).append("'");
                //
                //stmt.addBatch(sql.toString());
                //if (index % 500 == 0 && index != 0) {
                //    stmt.executeBatch();
                //}
                pstmt.setString(1, data.nameEng);
                pstmt.setString(2, data.id);
                pstmt.addBatch();

                if (index % 500 == 0 && index != 0) {
                    pstmt.executeBatch();
                }
            }
            pstmt.executeBatch();

            conn.commit();
        } catch (Exception e) {
            throw new JobException(e.getMessage(), e.fillInStackTrace());
        } finally {
            DbUtils.closeQuietly(result);
            DbUtils.closeQuietly(pstmt);
            DbUtils.closeQuietly(conn);
        }
    }

    class TranslateData {
        /**
         * 主键
         */
        private String id;

        /**
         * 要素类型
         */
        private String type;

        /**
         * 分类
         */
        private String kindCode;

        /**
         * 品牌
         */
        private String chain;

        /**
         * 名称
         */
        private String name;

        /**
         * 拼音
         */
        private String phonetic;

        /**
         * 英文名
         */
        private String nameEng;

    }

    class Task implements Runnable {

        private List<TranslateData> datas;

        public Task(List<TranslateData> datas) {
            this.datas = datas;
        }

        @Override
        public void run() {
            MetadataApi api = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");

            TranslateData data = null;
            for (int index = 0; index < datas.size(); index++) {
                try {
                    data = datas.get(index);
                    String nameEng = api.convertEng(data.name, data.kindCode, data.chain, data.phonetic);
                    data.nameEng = nameEng;
                } catch (Exception e) {
                    log.error(String.format("translate has error [id:%s, name:%s]", data.id, data.name));
                    log.error(e.getMessage(), e);
                }
            }
            //log.info(String.format("thread %s over. data size %s", Thread.currentThread().getName(), datas.size()));
        }
    }
}
