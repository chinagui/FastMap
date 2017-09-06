package com.navinfo.dataservice.scripts;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
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
import com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.Batch;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.BatchCommand;
import net.sf.json.JSONObject;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
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

    private static ThreadLocal<BatchTranslate> threadLocal = new ThreadLocal<>();

    private volatile List<Integer> successMesh;

    private volatile List<Integer> failureMesh;

    public static BatchTranslate getInstance() {
        BatchTranslate translate = threadLocal.get();
        if (null == translate) {
            translate = new BatchTranslate();
            threadLocal.set(translate);
        }
        return translate;
    }

    private void init() {
        successMesh = new ArrayList<>();
        failureMesh = new ArrayList<>();
    }

    public JSONObject execute(JSONObject request) throws InterruptedException {
        Long time = System.currentTimeMillis();

        JSONObject response = new JSONObject();

        init();

        Map<Integer, List<BasicObj>> map = loadData(request);
        if (map.isEmpty()) {
            return response;
        }

        ThreadPoolExecutor executor = new ThreadPoolExecutor(10, 20, 3,
                TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(2000), new ThreadPoolExecutor.DiscardOldestPolicy());

        for (final Map.Entry<Integer, List<BasicObj>> entry: map.entrySet()) {
            Task task = new Task(entry.getKey(), entry.getValue());
            executor.execute(task);

            System.out.println("线程池中线程数目："+executor.getPoolSize()+"，队列中等待执行的任务数目："+
                    executor.getQueue().size()+"，已执行完别的任务数目："+executor.getCompletedTaskCount());
        }
        executor.shutdown();

        while (!executor.awaitTermination(10, TimeUnit.SECONDS));

        response.put("successMesh", Arrays.toString(successMesh.toArray()));
        response.put("failureMesh", Arrays.toString(failureMesh.toArray()));
        response.put("time", (System.currentTimeMillis() - time) >> 10);

        return response;
    }

    private Map<Integer, List<BasicObj>> loadData(JSONObject request) {
        Map<Integer, List<BasicObj>> map = new HashMap<>();
        Connection conn = null;

        try {
            conn = DBConnector.getInstance().getMkConnection();
            //conn = DBConnector.getInstance().getConnectionById(13);

            StringBuilder sb = new StringBuilder();
            sb.append("SELECT *");
            sb.append("  FROM IX_POI IP");
            sb.append(" WHERE IP.ROW_ID IN");
            sb.append("       (SELECT LG.TB_ROW_ID FROM LOG_DETAIL LG WHERE LG.OP_TP <> 2)");

            String meshes = request.optString("meshes", "");

            if (StringUtils.isNotEmpty(meshes)) {
                sb.append("   AND IP.MESH_ID IN (");
                sb.append(org.apache.commons.lang.StringUtils.join(meshes.split(","), ","));
                sb.append(")");
            }
            List<Long> pids = new QueryRunner().query(conn, sb.toString(), new TranslateHandler());

            Set<String> tabNames = new HashSet<>();
            tabNames.add("IX_POI_NAME");
            Map<Long,BasicObj> objs =  ObjBatchSelector.selectByPids(conn, "IX_POI", tabNames,false, pids, true, true);
            Map<Long, List<LogDetail>> logs = PoiLogDetailStat.loadByRowEditStatus(conn, pids);
            ObjHisLogParser.parse(objs, logs);

            IxPoi ixPoi;
            Integer meshId;
            for (BasicObj basicObj : objs.values()) {
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
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(conn);
        }

        return map;
    }


    private void batchTranslate(List<BasicObj> list) throws Exception{
        Connection conn = null;
        try {
            conn = DBConnector.getInstance().getMkConnection();
            //conn = DBConnector.getInstance().getConnectionById(13);

            OperationResult operationResult=new OperationResult();
            operationResult.putAll(list);

            // 执行批处理FM-BAT-20-115
            BatchCommand batchCommand=new BatchCommand();
            batchCommand.setRuleId("FM-BAT-20-115");
            Batch batch=new Batch(conn,operationResult);
            batch.operate(batchCommand);
            //persistBatch(batch);
        } catch (Exception e) {
            logger.error("执行FM-BAT-20-115批处理出错...", e.fillInStackTrace());
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
            try {
                translate.batchTranslate(list);
            } catch (Exception e) {
                failureMesh.add(meshId);
                e.fillInStackTrace();
            }
            successMesh.add(meshId);

            System.out.println("已批图幅号" + meshId);
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
            //String dir = SystemConfigFactory.getSystemConfig().getValue("scripts.dir");
            String dir = "D:/";
            //初始化context
            JobScriptsInterface.initContext();
            //
            request = ToolScriptsInterface.readJson(dir+"request"+ File.separator+irequest);

            BatchTranslate translate = new BatchTranslate();
            response = translate.execute(request);

            //ToolScriptsInterface.writeJson(response,dir+"response"+File.separator+irequest);
            System.out.println(response);
            System.out.println("Over.");
            System.exit(0);
        }catch(Exception e){
            System.out.println("Oops, something wrong...");
            e.printStackTrace();
        }
    }
}
