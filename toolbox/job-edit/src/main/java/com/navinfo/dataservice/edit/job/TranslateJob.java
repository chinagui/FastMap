package com.navinfo.dataservice.edit.job;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Title: TranslateJob
 * @Package: com.navinfo.dataservice.edit.job
 * @Description: ${TODO}
 * @Author: Crayeres
 * @Date: 10/16/2017
 * @Version: V1.0
 */
public class TranslateJob extends AbstractJob {

    private final static Integer PART_SIZE = 3000;

    private final static String TABLE_NAME = "TRANSLATE";

    private final static Logger logger = LoggerRepos.getLogger(TranslateJob.class);

    public TranslateJob(JobInfo jobInfo) {
        super(jobInfo);
    }

    @Override
    public void execute() throws JobException {
        TranslateJobRequest translateJobRequest = (TranslateJobRequest) request;
        String filePath = translateJobRequest.getFilePath();

        logger.info(String.format("translate file path:%s", filePath));

        try {
            //Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
            Class.forName("com.hxtt.sql.access.AccessDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Connection conn = null;
        Statement stmt = null;
        ResultSet result = null;

        String dbURL = "jdbc:access:///" + filePath;
        try {
            Properties prop = new Properties();
            prop.setProperty("charSet", "gb2312");
            prop.setProperty("user", "");
            prop.setProperty("password", "");

            conn = DriverManager.getConnection(dbURL, prop);
            conn.setAutoCommit(false);

            stmt = conn.createStatement();

            result = stmt.executeQuery("SELECT * FROM " + TranslateJob.TABLE_NAME);
            result.setFetchSize(2000);

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

            List<List<TranslateData>> partition = ListUtils.partition(datas, TranslateJob.PART_SIZE);

            ThreadPoolExecutor executor = new ThreadPoolExecutor(20, 20, 3, TimeUnit.SECONDS,
                    new ArrayBlockingQueue<Runnable>(1000), new ThreadPoolExecutor.DiscardOldestPolicy());

            for (List<TranslateData> data : partition) {
                Task task = new Task(data, conn);
                executor.execute(task);
            }

            executor.shutdown();

            while (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                logger.info(String.format("executor.getPoolSize()：%d，executor.getQueue().size()：%d，executor.getCompletedTaskCo" +
                        "unt()：%d", executor.getPoolSize(), executor.getQueue().size(), executor.getCompletedTaskCount()));
            }

            conn.commit();
        } catch (Exception e) {
            throw new JobException(e.getMessage(), e.fillInStackTrace());
        } finally {
            DbUtils.closeQuietly(result);
            DbUtils.closeQuietly(stmt);
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

        Connection conn;

        private List<TranslateData> datas;

        private AtomicInteger atomicInteger;

        public Task(List<TranslateData> datas, Connection conn) {
            this.datas = datas;
            this.conn = conn;
            this.atomicInteger = new AtomicInteger(1);
        }

        @Override
        public void run() {
            MetadataApi api = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
            Statement stmt = null;
            try {
                stmt = conn.createStatement();

                String sql = "UPDATE " + TranslateJob.TABLE_NAME + " SET NAME_ENG = '%s' WHERE ID = '%s'";

                for (TranslateData data : datas) {
                    String engName = api.convertEng(data.name, data.kindCode, data.chain, data.phonetic);
                    stmt.addBatch(String.format(sql, engName, data.id));
                    //stmt.addBatch(String.format(sql, engName, data.id));
                    if (atomicInteger.addAndGet(1) % 500 == 0) {
                        stmt.executeBatch();
                    }
                }
                stmt.executeBatch();
                logger.info(String.format("thread %s over.", Thread.currentThread().getName()));
            } catch (SQLException e) {
                logger.error("translate has error", e.fillInStackTrace());
            } finally {
                DbUtils.closeQuietly(stmt);
            }
        }
    }


}
