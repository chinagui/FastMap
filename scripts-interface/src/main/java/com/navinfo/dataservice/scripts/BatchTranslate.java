package com.navinfo.dataservice.scripts;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.plus.log.LogDetail;
import com.navinfo.dataservice.dao.plus.log.ObjHisLogParser;
import com.navinfo.dataservice.dao.plus.log.PoiLogDetailStat;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.operation.OperationSegment;
import com.navinfo.dataservice.dao.plus.selector.ObjBatchSelector;
import com.navinfo.dataservice.day2mon.PostBatch;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.Batch;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.BatchCommand;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Title: BatchTranslate
 * @Package: com.navinfo.dataservice.scripts
 * @Description: ${TODO}
 * @Author: Crayeres
 * @Date: 9/5/2017
 * @Version: V1.0
 */
public class BatchTranslate {

    private static final Logger logger = LoggerRepos.getLogger(BatchTranslate.class);

    private BatchTranslate(){
    }

    private final static Integer INIT_THREAD_SIZE = 20;

    private final static Integer MAX_THREAD_SIZE = 20;

    private final static Integer PART_SIZE = 3000;

    private static ThreadLocal<BatchTranslate> threadLocal = new ThreadLocal<>();

    private volatile Map<String, Integer> successMesh;

    private volatile List<String> failureMesh;

    private volatile Map<String, Integer> runningMesh;

    public static BatchTranslate getInstance() {
        BatchTranslate translate = threadLocal.get();
        if (null == translate) {
            translate = new BatchTranslate();
            threadLocal.set(translate);
        }
        return translate;
    }

    private Integer dbId;

    private void init(JSONObject request) {
        successMesh = new HashMap<>();
        failureMesh = new ArrayList<>();
        runningMesh = new HashMap<>();
        dbId = request.optInt("dbId", Integer.MIN_VALUE);
    }

    public JSONObject execute(JSONObject request) throws Exception {
        logger.info("batch translate start...");
        Long time = System.currentTimeMillis();

        JSONObject response = new JSONObject();

        init(request);

        Map<Integer, List<BasicObj>> map = loadData(request);
        if (map.isEmpty()) {
            return response;
        }
        logger.info(String.format("translate meshes with [%s]", Arrays.toString(map.keySet().toArray(new Integer[]{}))));

        //int queueSize = 0;
        //for (List<BasicObj> list : map.values()) {
        //    queueSize += list.size() % PART_SIZE == 0 ? list.size() /PART_SIZE : list.size() / PART_SIZE + 1;
        //}
        //queueSize = queueSize > MAX_THREAD_SIZE ? queueSize - MAX_THREAD_SIZE : queueSize;

        ThreadPoolExecutor executor = new ThreadPoolExecutor(INIT_THREAD_SIZE, MAX_THREAD_SIZE, 3, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(20000), new ThreadPoolExecutor.DiscardOldestPolicy());

        for (final Map.Entry<Integer, List<BasicObj>> entry: map.entrySet()) {
            //List<List<BasicObj>> parts = ListUtils.partition(entry.getValue(), PART_SIZE);
            //
            //for (List<BasicObj> part : parts) {
            //    Task task = new Task(entry.getKey(), part);
            //    executor.execute(task);
            //}
            Task task = new Task(entry.getKey(), entry.getValue());
            executor.execute(task);
        }

        executor.shutdown();

        while (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
            logger.info(String.format("running mesh is [%s]", JSONObject.fromObject(runningMesh).toString()));
            logger.info(String.format("executor.getPoolSize()：%d，executor.getQueue().size()：%d，executor.getCompletedTaskCo" +
                            "unt()：%d", executor.getPoolSize(), executor.getQueue().size(), executor.getCompletedTaskCount()));
        }

        response.put("successMesh", JSONArray.fromObject(successMesh).toString());
        response.put("failureMesh", JSONArray.fromObject(failureMesh).toString());
        response.put("speedTime", (System.currentTimeMillis() - time) >> 10);

        logger.info("batch translate end...");

        return response;
    }

    private Map<Integer, List<BasicObj>> loadData(JSONObject request) throws Exception{
        Map<Integer, List<BasicObj>> map = new HashMap<>();
        Connection conn = null;

        try {
            if (dbId != Integer.MIN_VALUE) {
                conn = DBConnector.getInstance().getConnectionById(dbId);
            } else {
                conn = DBConnector.getInstance().getMkConnection();
            }

            //StringBuilder sb = new StringBuilder();
            //sb.append("SELECT *");
            //sb.append("  FROM IX_POI IP");
            //sb.append(" WHERE IP.PID IN");
            //sb.append("       (SELECT PID FROM day_mon_poi_915)");
            //// op_id log_detail
            ////sb.append("       (SELECT ob_pid FROM log_detail)");
            //
            //String meshes = request.optString("meshes", "");
            //
            //if (StringUtils.isNotEmpty(meshes)) {
            //    sb.append("   AND IP.MESH_ID IN (");
            //    sb.append(org.apache.commons.lang.StringUtils.join(meshes.split(","), ","));
            //    sb.append(")");
            //}
            //List<Long> pids = new QueryRunner().query(conn, sb.toString(), new TranslateHandler());
            //
            //logger.info(String.format("load data number is %d", pids.size()));
            //
            //Set<String> tabNames = new HashSet<>();
            //tabNames.add("IX_POI_NAME");
            //tabNames.add("IX_POI_NAME_FLAG");
            //Map<Long,BasicObj> objs =  ObjBatchSelector.selectByPids(conn, "IX_POI", tabNames,false, pids, true, true);
            //Map<Long, List<LogDetail>> logs = PoiLogDetailStat.loadAllLog(conn, pids);
            //ObjHisLogParser.parse(objs, logs);

            OperationResult operationResult = parseLog(conn, request.getString("tableName"));

            IxPoi ixPoi;
            Integer meshId;
            for (BasicObj basicObj : operationResult.getAllObjs()) {
                ixPoi = (IxPoi) basicObj.getMainrow();
                meshId = ixPoi.getMeshId();
                if (map.containsKey(meshId)) {
                    map.get(meshId).add(basicObj);
                } else {
                    List<BasicObj> list = new ArrayList<>();
                    list.add(basicObj);
                    map.put(meshId, list);
                }
            }

        } catch (Exception e) {
            logger.error("load data error...", e.fillInStackTrace());
            throw e;
        } finally {
            DbUtils.closeQuietly(conn);
        }

        return map;
    }

    private OperationResult parseLog(Connection monthConn, String tempOpTable) throws Exception {
        Map<Long, List<LogDetail>> logStatInfo = PoiLogDetailStat.loadByOperation(monthConn, tempOpTable);
        Set<String> tabNames = new HashSet<>();
        tabNames.add("IX_POI_NAME");
        tabNames.add("IX_POI_NAME_FLAG");
        OperationResult result = new OperationResult();
        Map<Long, BasicObj> objs = ObjBatchSelector.selectByPids(monthConn,
                "IX_POI", tabNames, false, logStatInfo.keySet(), true, true);
        ObjHisLogParser.parse(objs, logStatInfo);
        result.putAll(objs.values());
        return result;
    }


    private void batchTranslate(Integer dbId, List<BasicObj> list) throws Exception{
        Connection conn = null;
        try {
            if (dbId != Integer.MIN_VALUE) {
                conn = DBConnector.getInstance().getConnectionById(dbId);
            } else {
                conn = DBConnector.getInstance().getMkConnection();
            }
            conn.setAutoCommit(false);

            OperationResult operationResult=new OperationResult();
            operationResult.putAll(list);

            // 执行批处理FM-BAT-20-115
            BatchCommand batchCommand=new BatchCommand();
            batchCommand.setRuleId("FM-BAT-20-115");
            Batch batch=new Batch(conn,operationResult);
            batch.operate(batchCommand);
            persistBatch(batch);
            
    		// 处理sourceFlag
            new PostBatch(operationResult, conn).detealSourceFlag();
    		// 200170特殊处理
            new PostBatch(operationResult, conn).deteal200170();
            // 改171状态
            new PostBatch(operationResult, conn).updateHandler();
            
            conn.commit();
        } catch (Exception e) {
            DbUtils.rollback(conn);
            logger.error("run fm-bat-20-115 is error...", e.fillInStackTrace());
            throw e;
        } finally {
            DbUtils.closeQuietly(conn);
        }
    }

    private void persistBatch(Batch batch) throws Exception {
        batch.persistChangeLog(OperationSegment.SG_COLUMN, 0);//FIXME:修改默认的用户
    }


    private class TranslateHandler implements ResultSetHandler<List<Long>> {
        @Override
        public List<Long> handle(ResultSet rs) throws SQLException {
            List<Long> pids = new ArrayList<>();
            rs.setFetchSize(2000);
            while (rs.next()) {
                pids.add(rs.getLong("PID"));
            }
            return pids;
        }
    }


    class Task implements Runnable {

        private Integer meshId;

        private List<BasicObj> list;

        protected Task(Integer meshId, List<BasicObj> list) {
            this.meshId = meshId;
            this.list = list;
        }

        @Override
        public void run() {
            BatchTranslate translate = BatchTranslate.getInstance();
            if (failureMesh.contains(meshId.toString())) {
                return;
            }

            String key = String.format("%d(%d)", meshId, Thread.currentThread().getId());
            try {
                runningMesh.put(key, list.size());
                translate.batchTranslate(dbId, list);
                runningMesh.remove(key);

                if (successMesh.containsKey(meshId.toString())) {
                    successMesh.put(meshId.toString(), successMesh.get(meshId.toString()) + list.size());
                } else {
                    successMesh.put(meshId.toString(), list.size());
                }
                //successMesh.put(key, list.size());
                logger.info(String.format("mesh %d translate success..", meshId));
            } catch (Exception e) {
                logger.error(String.format("mesh %d translate error..", meshId));
                failureMesh.add(meshId.toString());
                e.fillInStackTrace();
            }

        }

    }

    public static void main(String[] args) {
        try{
            Map<String,String> map = new HashMap<String,String>();
            if(args.length%2!=0){
                System.out.println("ERROR:need args:-irequest xxx");
                return;
            }
            for(int i=0; i<args.length;i+=2){
                map.put(args[i], args[i+1]);
            }
            String irequest = map.get("-irequest");
            if(StringUtils.isEmpty(irequest)){
                System.out.println("ERROR:need args:-irequest xxx");
                return;
            }
            JSONObject request=null;
            JSONObject response = null;
            String dir = SystemConfigFactory.getSystemConfig().getValue("scripts.dir");
            //String dir = "D:/";
            //初始化context
            JobScriptsInterface.initContext();
            //
            request = ToolScriptsInterface.readJson(dir+"request"+ File.separator+irequest);

            BatchTranslate translate = new BatchTranslate();
            response = translate.execute(request);

            ToolScriptsInterface.writeJson(response,dir+"response"+File.separator+irequest);
            logger.debug(response);
            logger.debug("Over.");
            System.exit(0);
        }catch(Exception e){
            System.out.println("Oops, something wrong...");
            e.printStackTrace();
        }
    }
}
