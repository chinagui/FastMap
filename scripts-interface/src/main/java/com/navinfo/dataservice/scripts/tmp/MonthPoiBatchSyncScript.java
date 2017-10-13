package com.navinfo.dataservice.scripts.tmp;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.dao.log.LogReader;
import com.navinfo.dataservice.dao.plus.log.LogDetail;
import com.navinfo.dataservice.dao.plus.log.ObjHisLogParser;
import com.navinfo.dataservice.dao.plus.log.PoiLogDetailStat;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.ObjBatchSelector;
import com.navinfo.dataservice.scripts.JobScriptsInterface;
import com.navinfo.dataservice.scripts.ToolScriptsInterface;
import net.sf.json.JSONObject;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.sql.*;
import java.util.*;

/**
 * @Title: MonthPoiBatchSyncScript
 * @Package: com.navinfo.dataservice.scripts.tmp
 * @Description: ${TODO}
 * @Author: Crayeres
 * @Date: 10/13/2017
 * @Version: V1.0
 */
public class MonthPoiBatchSyncScript {

    private static Logger log = Logger.getLogger(MonthPoiBatchSyncScript.class);

    public JSONObject run(JSONObject json) throws Exception{
        log.info(" start MonthPoiBatchSyncJob");
        //MonthPoiBatchSyncJobRequest myRequest = (MonthPoiBatchSyncJobRequest) request;
        //ManApi apiService = (ManApi) ApplicationContextUtil.getBean("manApi");
        //int taskId = myRequest.getTaskId();
        //long userId = myRequest.getUserId();
        Connection conn = null;
        try {
            log.info("获取任务对应的参数");
            //Subtask subtask = apiService.queryBySubtaskId(taskId);
            //log.info("params:taskId=" + taskId);
            //int dbId = subtask.getDbId();
            //log.info("dbId = " + dbId);
            conn = DBConnector.getInstance().getMkConnection();
            LogReader logReader = new LogReader(conn);
            Collection<String> grids = new HashSet<>();

            String meshStr = json.getString("meshes");
            if (StringUtils.isEmpty(meshStr)) {
                return new JSONObject();
            }

            List<Integer> meshes = new ArrayList<>();
            for (String mesh : meshStr.split(",")) {
                meshes.add(Integer.valueOf(mesh));
            }

            for (int grid : meshs2grids(meshes)) {
                grids.add(String.valueOf(grid));
            }

            log.info("grids=" + grids);
            log.info("获取任务范围内新增修改的poi信息");
            Map<Integer, Collection<Long>> map = logReader.getUpdatedObj(
                    "IX_POI", "IX_POI", grids, null);
            Collection<Long> pids = new ArrayList<Long>();
            Collection<Long> addPids = map.get(1);
            Collection<Long> updatePids = map.get(3);
            log.info("新增 poi 信息=" + addPids);
            log.info("修改poi 信息=" + updatePids);
            if (addPids != null && addPids.size() > 0) {
                pids.addAll(addPids);
            }
            if (updatePids != null && updatePids.size() > 0) {
                pids.addAll(updatePids);
            }
            if (pids.size() > 0) {
                // 获取poi对象信息
                Set<String> tabNames = new HashSet<String>();
                tabNames.add("IX_POI_NAME");
                Map<Long, BasicObj> objs = ObjBatchSelector.selectByPids(conn,
                        ObjectName.IX_POI, tabNames, false, pids, true, true);
                Map<Long, List<LogDetail>> logs = PoiLogDetailStat.loadByColEditStatus(conn, pids);
                log.info(" 加载poi信息以及对应子表信息");

                // 将poi对象与履历合并起来
                if (!objs.isEmpty()) {
                    ObjHisLogParser.parse(objs, logs);
                }

                Collection<Long> chiNamePids = new ArrayList<Long>();

                Collection<Long> chtNamePids = new ArrayList<Long>();

                Collection<Long> originEngNamePids = new ArrayList<Long>();

                Collection<Long> OfficeStandardEngNamePids = new ArrayList<Long>();

                Collection<Long> originPotNamePids = new ArrayList<Long>();

                Collection<Long> standardPotNamePids = new ArrayList<Long>();
                for (long pid : objs.keySet()) {
                    BasicObj obj = objs.get(pid);
                    IxPoiObj poiObj = (IxPoiObj) obj;
                    if (poiObj.getOfficeStandardCHIName() != null) {
                        IxPoiName poiName = poiObj.getOfficeStandardCHIName();
                        if (poiName.getHisOpType() == OperationType.UPDATE
                                || poiName.getHisOpType() == OperationType.INSERT) {
                            chiNamePids.add(pid);
                        }

                    } else {
                        if (obj.isDelOfficeStandardCHIName()) {
                            chiNamePids.add(pid);
                        }
                    }

                    if (poiObj.getOfficeStandardCHTName() != null) {
                        IxPoiName poiName = poiObj.getOfficeStandardCHTName();
                        if (poiName.getHisOpType() == OperationType.UPDATE
                                || poiName.getHisOpType() == OperationType.INSERT) {
                            chtNamePids.add(pid);
                        }

                    } else {
                        if (obj.isDelOfficeStandardCHTName()) {
                            chtNamePids.add(pid);
                        }
                    }
                    if (poiObj.getOfficeOriginEngName() != null) {
                        IxPoiName poiName = poiObj.getOfficeOriginEngName();
                        if (poiName.getHisOpType() == OperationType.UPDATE
                                || poiName.getHisOpType() == OperationType.INSERT) {
                            originEngNamePids.add(pid);
                        }

                    } else {
                        if (obj.isDelOfficeOriginEngName()) {
                            originEngNamePids.add(pid);
                        }
                    }
                    if (poiObj.getOfficeStandardEngName() != null) {
                        IxPoiName poiName = poiObj.getOfficeStandardEngName();
                        if (poiName.getHisOpType() == OperationType.UPDATE
                                || poiName.getHisOpType() == OperationType.INSERT) {
                            OfficeStandardEngNamePids.add(pid);
                        }

                    } else {
                        if (obj.isDelOfficeStandardEngName()) {
                            OfficeStandardEngNamePids.add(pid);
                        }

                    }
                    if (poiObj.getOfficeOriginPOTName() != null) {
                        IxPoiName poiName = poiObj.getOfficeOriginPOTName();
                        if (poiName.getHisOpType() == OperationType.UPDATE
                                || poiName.getHisOpType() == OperationType.INSERT) {
                            originPotNamePids.add(pid);
                        }

                    } else {
                        if (obj.isDelOfficeOriginPotName()) {
                            originPotNamePids.add(pid);
                        }
                    }
                    if (poiObj.getOfficeStandardPOTName() != null) {
                        IxPoiName poiName = poiObj.getOfficeStandardPOTName();
                        if (poiName.getHisOpType() == OperationType.UPDATE
                                || poiName.getHisOpType() == OperationType.INSERT) {
                            standardPotNamePids.add(pid);
                        }

                    }
                    if (obj.isDelOfficeStandardPotName()) {
                        standardPotNamePids.add(pid);
                    }

                }

                log.info("批 FieldState");
                this.updateBatchPoi(chiNamePids,
                        this.getUpadeFieldStateForSql("改标准化简体中文"), conn);
                this.updateBatchPoi(chtNamePids,
                        this.getUpadeFieldStateForSql("改标准化繁体中文"), conn);
                this.updateBatchPoi(originEngNamePids,
                        this.getUpadeFieldStateForSql("改官方名原始英文"), conn);
                this.updateBatchPoi(OfficeStandardEngNamePids,
                        this.getUpadeFieldStateForSql("改官方名标准化英文"), conn);
                this.updateBatchPoi(originPotNamePids,
                        this.getUpadeFieldStateForSql("改官方名原始葡萄文"), conn);
                this.updateBatchPoi(standardPotNamePids,
                        this.getUpadeFieldStateForSql("改官方名标准化葡萄文"), conn);
            }
            //log.info("关闭任务");
            //apiService.closeSubtask(taskId, userId);
        } catch (Exception e) {
            log.error("MonthPoiBatchSyncJob错误", e);
            DbUtils.rollbackAndCloseQuietly(conn);
            throw new Exception(e);
        } finally {
            DbUtils.commitAndCloseQuietly(conn);
        }

        return new JSONObject();
    }

    private String getUpadeFieldStateForSql(String strValue) {

        return "   UPDATE ix_poi p								               \n"
                + "    SET    p.FIELD_STATE =  DECODE (INSTR (p.FIELD_STATE, '"
                + strValue
                + "'),                                                          \n"
                + "                          NULL,  '"
                + strValue
                + "|',                                                          \n"
                + "                           0, p.FIELD_STATE ||  '"
                + strValue
                + "|',                         \n"
                + "                           p.FIELD_STATE)                                        \n"
                + "     WHERE p.pid in  (select to_number(column_value) from table(clob_to_table(?))) \n";
    }

    public void updateBatchPoi(Collection<Long> pidList, String sql,
                               Connection conn) throws Exception {

        if (pidList == null || pidList.size() == 0) {
            return;
        }
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        try {
            Clob pidsClob = ConnectionUtil.createClob(conn);
            pidsClob.setString(1, StringUtils.join(pidList, ","));
            pstmt = conn.prepareStatement(sql);
            pstmt.setClob(1, pidsClob);
            pstmt.executeUpdate();
        } catch (Exception e) {
            throw e;
        } finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(pstmt);
        }
    }

    class PidHandler implements ResultSetHandler<Collection<Long>> {
        @Override
        public Collection<Long> handle(ResultSet rs) throws SQLException {

            Collection<Long> resultPids = new ArrayList<Long>();
            while (rs.next()) {
                resultPids.add(rs.getLong("pid"));
            }
            return resultPids;
        }

    }

    private List<Integer> meshs2grids(List<Integer> meshs) throws Exception {
        List<Integer> grids =new ArrayList<Integer>();
        for(Object obj:meshs){
            int m=0;
            if(obj instanceof Integer){
                m=(int) obj;
            }else if(obj instanceof String){
                m =Integer.parseInt(String.valueOf(obj));
            }
            for(int i=0;i<4;i++){
                for(int j=0;j<4;j++){
                    grids.add(m*100 + i*10+ j);
                }
            }
        }
        return grids;
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

            MonthPoiBatchSyncScript script = new MonthPoiBatchSyncScript();
            response = script.run(request);

            ToolScriptsInterface.writeJson(response,dir+"response"+File.separator+irequest);
            log.debug(response);
            log.debug("Over.");
            System.exit(0);
        }catch(Exception e){
            System.out.println("Oops, something wrong...");
            e.printStackTrace();
        }
    }
}
