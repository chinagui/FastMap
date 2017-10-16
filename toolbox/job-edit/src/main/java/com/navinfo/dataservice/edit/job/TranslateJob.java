package com.navinfo.dataservice.edit.job;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
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

    private final static String TABLE_NAME = "";

    public TranslateJob(JobInfo jobInfo) {
        super(jobInfo);
    }

    @Override
    public void execute() throws JobException {
        TranslateJobRequest translateJobRequest = (TranslateJobRequest) request;
        String filePath = translateJobRequest.getFilePath();

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

            result = stmt.executeQuery("SELECT * FROM ALL_KIND_MAP");
            result.setFetchSize(2000);

            List<TranslateData> datas = new ArrayList<>();

            while (result.next()) {
                TranslateData data = new TranslateData();
                data.setId(result.getString("ID"));
                data.setType(result.getString("TYPE"));
                data.setKindCode(result.getString("KIND_CODE"));
                data.setChain(result.getString("CHAIN"));
                data.setName(result.getString("NAME"));
                data.setPhonetic(result.getString("PHONETIC"));
                datas.add(data);
            }

            List<List<TranslateData>> partition = ListUtils.partition(datas, TranslateJob.PART_SIZE);

            ThreadPoolExecutor executor = new ThreadPoolExecutor(20, 20, 3, TimeUnit.SECONDS,
                    new ArrayBlockingQueue<Runnable>(1000), new ThreadPoolExecutor.DiscardOldestPolicy());

            for (List<TranslateData> data : partition) {
                executor.execute(new MyThread(data, conn));
            }

            while (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                //logger.info(String.format("running mesh is [%s]", JSONObject.fromObject(runningMesh).toString()));
                //logger.info(String.format("executor.getPoolSize()：%d，executor.getQueue().size()：%d，executor.getCompletedTaskCo" +
                //        "unt()：%d", executor.getPoolSize(), executor.getQueue().size(), executor.getCompletedTaskCount()));
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
        private String id;

        private String type;

        private String kindCode;

        private String chain;

        private String name;

        private String phonetic;

        private String nameEng;

        /**
         * Getter method for property <tt>id</tt>.
         *
         * @return property value of id
         */
        public String getId() {
            return id;
        }

        /**
         * Setter method for property <tt>id</tt>.
         *
         * @param id value to be assigned to property id
         */
        public void setId(String id) {
            this.id = id;
        }

        /**
         * Getter method for property <tt>type</tt>.
         *
         * @return property value of type
         */
        public String getType() {
            return type;
        }

        /**
         * Setter method for property <tt>type</tt>.
         *
         * @param type value to be assigned to property type
         */
        public void setType(String type) {
            this.type = type;
        }

        /**
         * Getter method for property <tt>kindCode</tt>.
         *
         * @return property value of kindCode
         */
        public String getKindCode() {
            return kindCode;
        }

        /**
         * Setter method for property <tt>kindCode</tt>.
         *
         * @param kindCode value to be assigned to property kindCode
         */
        public void setKindCode(String kindCode) {
            this.kindCode = kindCode;
        }

        /**
         * Getter method for property <tt>chain</tt>.
         *
         * @return property value of chain
         */
        public String getChain() {
            return chain;
        }

        /**
         * Setter method for property <tt>chain</tt>.
         *
         * @param chain value to be assigned to property chain
         */
        public void setChain(String chain) {
            this.chain = chain;
        }

        /**
         * Getter method for property <tt>name</tt>.
         *
         * @return property value of name
         */
        public String getName() {
            return name;
        }

        /**
         * Setter method for property <tt>name</tt>.
         *
         * @param name value to be assigned to property name
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * Getter method for property <tt>phonetic</tt>.
         *
         * @return property value of phonetic
         */
        public String getPhonetic() {
            return phonetic;
        }

        /**
         * Setter method for property <tt>phonetic</tt>.
         *
         * @param phonetic value to be assigned to property phonetic
         */
        public void setPhonetic(String phonetic) {
            this.phonetic = phonetic;
        }

        /**
         * Getter method for property <tt>nameEng</tt>.
         *
         * @return property value of nameEng
         */
        public String getNameEng() {
            return nameEng;
        }

        /**
         * Setter method for property <tt>nameEng</tt>.
         *
         * @param nameEng value to be assigned to property nameEng
         */
        public void setNameEng(String nameEng) {
            this.nameEng = nameEng;
        }
    }

    class MyThread extends Thread {

        Connection conn;

        private List<TranslateData> datas;

        private AtomicInteger atomicInteger;

        public MyThread(List<TranslateData> datas, Connection conn) {
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

                String sql = "UPDATE " + TranslateJob.TABLE_NAME + " SET NAME_ENG = %s WHERE ID = %s";

                for (TranslateData data : datas) {
                    String engName = api.convertEng(data.getName(), data.getChain());
                    stmt.addBatch(String.format(sql, data.getId(), engName));
                    if (atomicInteger.addAndGet(1) % 500 == 0) {
                        stmt.executeBatch();
                    }
                }
                stmt.executeBatch();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                DbUtils.closeQuietly(stmt);
            }
        }
    }


}
