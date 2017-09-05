package com.navinfo.dataservice.engine.fcc.tips;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.RegionMesh;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.bizcommons.upload.RegionUploadResult;
import com.navinfo.dataservice.bizcommons.upload.stat.UploadCrossRegionInfoDao;
import com.navinfo.dataservice.bizcommons.upload.stat.UploadRegionInfoOperator;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.photo.Photo;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.MD5Utils;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.fcc.HBaseConnector;
import com.navinfo.dataservice.dao.fcc.SolrController;
import com.navinfo.dataservice.dao.fcc.TaskType;
import com.navinfo.dataservice.dao.fcc.model.TipsDao;
import com.navinfo.dataservice.dao.fcc.operator.TipsIndexOracleOperator;
import com.navinfo.dataservice.dao.fcc.tips.selector.HbaseTipsQuery;
import com.navinfo.dataservice.engine.audio.Audio;
import com.navinfo.dataservice.engine.fcc.tips.model.FieldRoadQCRecord;
import com.navinfo.dataservice.engine.fcc.tips.model.TipsTrack;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Geometry;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.dbutils.DbUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.log4j.Logger;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.Map.Entry;

/**
 * 保存上传的tips数据
 *
 */

class ErrorType {
    static int InvalidLifecycle = 1;

    static int Deleted = 2;

    static int InvalidDate = 3;

    static int Notexist = 4;

    static int InvalidData = 5;

    static int FreshnessVerificationData = 6; // 鲜度验证tips(不入库)
    
    static int deleteNoteExists = 8;


}

class TipsMeshGrid {
    private Set<String> meshes;
    private String gridId;

    public Set<String> getMeshes() {
        return meshes;
    }

    public void setMeshes(Set<String> meshes) {
        this.meshes = meshes;
    }

    public String getGridId() {
        return gridId;
    }

    public void setGridId(String gridId) {
        this.gridId = gridId;
    }
}

public class TipsUpload {

    static int IMPORT_STAGE = 1;

    static int IMPORT_TIP_STATUS = 2;

    static int TIMELINE_FIRST_DATE_TYPE = 1;

    private Map<String, JSONObject> insertTips = new HashMap<String, JSONObject>();

    private Map<String, JSONObject> updateTips = new HashMap<String, JSONObject>();

    private Map<String, JSONObject> oldTips = new HashMap<String, JSONObject>();

    private Map<String, JSONObject> roadNameTips = new HashMap<String, JSONObject>(); // 道路名tips，用户道路名入元数据库

    private Map<String, String> allNeedDiffRowkeysCodeMap = new HashMap<String, String>(); // 所有入库需要差分的tips的<rowkey,code
    // >

    private static final Logger logger = Logger.getLogger(TipsUpload.class);

    private String currentDate;
    
    private String t_dataDate; //入库时间
    

    private int total;

    private int failed; // 记录导入错误的条数

    private JSONArray reasons; //失败的
    
    private JSONArray conflict; //时间冲突的
    
    private JSONArray freshed; //鲜度验证的

    private SolrController solr;

    private int subTaskId = 0;

    private int s_qTaskId = 0; // 快线任务号
    private int s_qSubTaskId = 0; // 快线子任务号
    private int s_mTaskId = 0;// 中线任务号
    private int s_mSubTaskId = 0; // 中线子任务号
    private Subtask subtask = null;
    private int qcTotal = 0;
    private JSONArray qcReasons = new JSONArray();
    private String qcErrMsg = "";
    private String firstCollectTime = null;
    private boolean isInsertFirstTime = false;
    private List<RegionUploadResult> regionResults = new ArrayList<RegionUploadResult>();

    public String getQcErrMsg() {
        return qcErrMsg;
    }

    public void setQcErrMsg(String qcErrMsg) {
        this.qcErrMsg = qcErrMsg;
    }

    public int getQcTotal() {
        return qcTotal;
    }

    public void setQcTotal(int qcTotal) {
        this.qcTotal = qcTotal;
    }

    public JSONArray getQcReasons() {
        return qcReasons;
    }

    public void setQcReasons(JSONArray qcReasons) {
        this.qcReasons = qcReasons;
    }

    public List<RegionUploadResult> getRegionResults() {
        return regionResults;
    }

    public void setRegionResults(List<RegionUploadResult> regionResults) {
        this.regionResults = regionResults;
    }
    
    

    /**
	 * @return the t_dataDate
	 */
	public String getT_dataDate() {
		return t_dataDate;
	}

	/**
	 * @param t_dataDate the t_dataDate to set
	 */
	public void setT_dataDate(String t_dataDate) {
		this.t_dataDate = t_dataDate;
	}

	/**
     * @param subtaskid
     * @throws Exception
     */
    public TipsUpload(int subtaskid) throws Exception {

        this.subTaskId = subtaskid;

        solr = new SolrController();

        initTaskId();
    }

    /**
     * @Description:根据采集端上传的子任务号，获取对应的任务号
     * @author: y
     * @throws Exception
     * @time:2017-4-19 上午10:35:33
     */
    private void initTaskId() throws Exception {

        // 外业采集任务号，可能为空，或者没有，没有则全赋值为0
        if (subTaskId == 0)
            return;

        // 调用 manapi 获取 任务类型、及任务号
        ManApi manApi = (ManApi) ApplicationContextUtil.getBean("manApi");
        try {
            Map<String, Integer> taskMap = manApi.getTaskBySubtaskId(subTaskId);
            if (taskMap != null) {
                int taskId = taskMap.get("taskId");
                // 1，中线 4，快线
                int taskType = taskMap.get("programType");

                if (TaskType.M_TASK_TYPE == taskType) {// 中线

                    s_mTaskId = taskId;// 中线任务号
                    s_mSubTaskId = subTaskId; // 中线子任务号

                    // 20170519 赋中线清空快线
                    s_qTaskId = 0;
                    s_qSubTaskId = 0;

                    //只查中线子任务的第一采集时间
                    Map<String, Object> timelineMap = manApi.queryTimelineByCondition(subTaskId, "subtask", TIMELINE_FIRST_DATE_TYPE);
                    if(timelineMap == null || timelineMap.size() == 0) {
                        isInsertFirstTime = true;
                    }
                }

                if (TaskType.Q_TASK_TYPE == taskType) {// 快线
                    s_qTaskId = taskId; // 快线任务号
                    s_qSubTaskId = subTaskId; // 快线子任务号

                    // 20170519 赋快线清空中线
                    s_mTaskId = 0;
                    s_mSubTaskId = 0;
                }

                subtask = manApi.queryBySubtaskId(subTaskId);
            } else {
                throw new Exception("根据子任务号，没查到对应的任务号，sutaskid:" + subTaskId);
            }

        } catch (Exception e) {
            logger.error("根据子任务号，获取任务任务号及任务类型出错：" + e.getMessage(), e);
            throw e;
        }

    }

    public JSONArray getReasons() {
        return reasons;
    }

    public void setReasons(JSONArray reasons) {
        this.reasons = reasons;
    }
    
    

    /**
	 * @return the conflict
	 */
	public JSONArray getConflict() {
		return conflict;
	}

	/**
	 * @param conflict the conflict to set
	 */
	public void setConflict(JSONArray conflict) {
		this.conflict = conflict;
	}

	/**
	 * @return the freshed
	 */
	public JSONArray getFreshed() {
		return freshed;
	}

	/**
	 * @param freshed the freshed to set
	 */
	public void setFreshed(JSONArray freshed) {
		this.freshed = freshed;
	}

	public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getFailed() {
        return failed;
    }

    public void setFailed(int failed) {
        this.failed = failed;
    }

    /**
     * 读取文件内容，保存数据 考虑到数据量不会特别大，所以和数据库一次交互即可
     *
     * @param fileName
     * @param audioMap
     * @param photoMap
     * @throws Exception
     */
    public void run(String fileName, Map<String, Photo> photoMap, Map<String, Audio> audioMap,
                    long userId) throws Exception {
        java.sql.Connection conn = null;
        Table htab = null;
        java.sql.Connection sysConn = null;
        try {
            conn = DBConnector.getInstance().getTipsIdxConnection();
            total = 0;

            failed = 0;

            reasons = new JSONArray();
            
            conflict = new JSONArray(); //时间冲突的
            
            freshed = new JSONArray(); //鲜度验证的

            currentDate = StringUtils.getCurrentTime();
            
            t_dataDate=currentDate; 

            Connection hbaseConn = HBaseConnector.getInstance().getConnection();

            htab = hbaseConn.getTable(TableName.valueOf(HBaseConstant.tipTab));

            //20170828 跨大区日志统计
            //Map key = mesh  value = set gridList,tips outnumber
            //上传数据的所有mesh
            Map<String, TipsMeshGrid> meshMap = new HashMap<>();
            List<Get> gets = loadFileContent(fileName, photoMap, audioMap, meshMap);

            loadOldTips(htab, gets);

            List<Put> puts = new ArrayList<Put>();
            List<TipsDao> solrIndexList = new ArrayList<TipsDao>();

            //根据tips图幅查找大区库信息
            Set<String> meshes = new HashSet<>();
            for (String rowkey : meshMap.keySet()) {
               TipsMeshGrid tipsMeshGrid = meshMap.get(rowkey);
               meshes.addAll(tipsMeshGrid.getMeshes());
            }
            ManApi manApi = (ManApi)ApplicationContextUtil.getBean("manApi");
            List<RegionMesh> regions = manApi.queryRegionWithMeshes(meshes);
            if(regions==null||regions.size()==0){
                logger.error("根据图幅未查询到所属大区库信息");
                throw new Exception("根据图幅未查询到所属大区库信息");
            }
            meshes.clear();
            // 新增(已存在)或者修改的时候判断是否是鲜度验证的tips
            Map<String, Integer> gridCountMap = new HashMap<>();

            doInsert(puts, solrIndexList, regions, meshMap, gridCountMap);

            doUpdate(puts, solrIndexList, regions, meshMap, gridCountMap);


            //保存oracle索引放在hbase put之前，oracle异常事务回滚，索引库异常hbase不执行入库
            //保证索引和hbase数据一致
            TipsIndexOracleOperator indexOracleOperator = new TipsIndexOracleOperator(conn);
            indexOracleOperator.update(solrIndexList);

            htab.put(puts);

            // 道路名入元数据库
            importRoadNameToMeta();

            //中线子任务第一采集时间
            if(StringUtils.isNotEmpty(firstCollectTime) && total-failed > 0) {
                manApi.saveTimeline(s_mSubTaskId, "subtask", TIMELINE_FIRST_DATE_TYPE, firstCollectTime);
            }

            //20170828 跨大区日志统计,记录无任务
            //被统计为无任务的数据上传
            sysConn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
            if(gridCountMap.size() > 0) {
                List<UploadCrossRegionInfoDao> infos = new ArrayList<UploadCrossRegionInfoDao>();
                Map<String, Integer> meshRegionMap = new HashMap<>();
                for(String gridId : gridCountMap.keySet()) {
                    String meshId = gridId.substring(0, gridId.length()-2);
                    if(!meshRegionMap.containsKey(meshId)) {
                        for(RegionMesh regionMesh : regions) {
                            if(regionMesh.meshContains(meshId)) {
                                meshRegionMap.put(meshId, regionMesh.getRegionId());
                                break;
                            }
                        }
                    }
                    int count = gridCountMap.get(gridId);
                    UploadCrossRegionInfoDao info = new UploadCrossRegionInfoDao();
                    info.setUserId(userId);
                    info.setFromSubtaskId(subTaskId);
                    info.setUploadType(2);
                    int regionId = meshRegionMap.get(meshId);
                    info.setOutRegionId(regionId);
                    info.setOutGridId(Integer.valueOf(gridId));
                    info.setOutGridNumber(count);
                    infos.add(info);
                }
                UploadRegionInfoOperator op = new UploadRegionInfoOperator(sysConn);
                op.save(infos);
                gridCountMap.clear();
            }
            Map<Integer, JSONObject> regionResultMap = new HashMap<>();
            //失败的记录数
            if(reasons.size() > 0) {
                for(int i = 0 ; i < reasons.size(); i++) {
                    JSONObject reasonObj = reasons.getJSONObject(i);
                    String rowkey = reasonObj.getString("rowkey");
                    int type = reasonObj.getInt("type");
                    TipsMeshGrid tipsMeshGrid = meshMap.get(rowkey);
                    if(tipsMeshGrid == null) {
                        continue;
                    }
                    String gridId = tipsMeshGrid.getGridId();
                    int regionId = 0;
                    for(RegionMesh regionMesh : regions) {
                        String meshId = gridId.substring(0, gridId.length()-2);
                        if(regionMesh.meshContains(meshId)) {
                            regionId = regionMesh.getRegionId();
                            break;
                        }
                    }
                    if(regionId > 0) {
                        if(regionResultMap.containsKey(regionId)) {
                            JSONObject jsonObject = regionResultMap.get(regionId);
                            if(type == 6) {
                                int sCount = jsonObject.getInt("sCount") + 1;
                                jsonObject.put("sCount", sCount);
                            }else {
                                int eCount = jsonObject.getInt("eCount") + 1;
                                jsonObject.put("eCount", eCount);
                            }
                        }else{
                            JSONObject jsonObject = new JSONObject();
                            if(type == 6) {//type=6不算失败
                                jsonObject.put("sCount", 1);
                                jsonObject.put("eCount", 0);
                            }else{
                                jsonObject.put("sCount", 0);
                                jsonObject.put("eCount", 1);
                            }
                            regionResultMap.put(regionId, jsonObject);
                        }
                    }
                    meshMap.remove(rowkey);
                }
            }
            //成功的记录数
            for(String rowkey : meshMap.keySet()) {
                TipsMeshGrid tipsMeshGrid = meshMap.get(rowkey);
                String gridId = tipsMeshGrid.getGridId();
                int regionId = 0;
                for(RegionMesh regionMesh : regions) {
                    String meshId = gridId.substring(0, gridId.length()-2);
                    if(regionMesh.meshContains(meshId)) {
                        regionId = regionMesh.getRegionId();
                        break;
                    }
                }
                if(regionId > 0) {
                    if(regionResultMap.containsKey(regionId)) {
                        JSONObject jsonObject = regionResultMap.get(regionId);
                        int sCount = jsonObject.getInt("sCount") + 1;
                        jsonObject.put("sCount", sCount);
                    }else{
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("sCount", 1);
                        jsonObject.put("eCount", 0);
                        regionResultMap.put(regionId, jsonObject);
                    }
                }
            }
            meshMap.clear();
            //返回统计结果
            for(int regionId : regionResultMap.keySet()) {
                RegionUploadResult regionResult = new RegionUploadResult(regionId);
                regionResult.setSubtaskId(subTaskId);
                JSONObject jsonObject = regionResultMap.get(regionId);
                regionResult.addResult(jsonObject.getInt("sCount"), jsonObject.getInt("eCount"));
                regionResults.add(regionResult);
            }
            regionResultMap.clear();

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            DbUtils.rollbackAndCloseQuietly(conn);
            throw new Exception("Tips上传报错", e);
        } finally {
            DbUtils.commitAndCloseQuietly(conn);
            DbUtils.commitAndCloseQuietly(sysConn);
            if(htab != null) {
                try{
                    htab.close();
                }catch (Exception e) {

                }
            }
        }

        // tips差分 （新增、修改的都差分） 放在写入hbase之后在更新
        //20170808  确认，web渲染不再使用差分结果。因此取消差分
        // TipsDiffer.tipsDiff(allNeedDiffRowkeysCodeMap);

    }

    /**
     * 道路名入元数据库
     */

    private void importRoadNameToMeta() {

        Set<Entry<String, JSONObject>> set = roadNameTips.entrySet();
        Iterator<Entry<String, JSONObject>> it = set.iterator();
        while (it.hasNext()) {
            Entry<String, JSONObject> en = it.next();

            String rowkey = en.getKey();
            // 坐标
            JSONObject nameTipJson = en.getValue();
            JSONObject gLocation = nameTipJson.getJSONObject("g_location");
            String sourceType = nameTipJson.getString("s_sourceType");

            // 名称,调用元数据库接口入库
            MetadataApi metaApi = (MetadataApi) ApplicationContextUtil.getBean("metaApi");
            JSONArray names = nameTipJson.getJSONArray("n_array");
            for (Object name : names) {
                // 修改 20170308，道路名去除空格，否则转英文报错
                if (name != null && StringUtils.isNotEmpty(name.toString().trim())) {
                    try {
                        metaApi.nameImport(name.toString().trim(), gLocation, rowkey, sourceType);
                    } catch (Exception e) {
                        // reasons.add(newReasonObject(rowkey,
                        // ErrorType.InvalidData));
                        logger.error(e.getMessage(), e.getCause());
                    }
                }
            }

        }
    }

    /**
     * 读取Tips文件，组装Get列表
     *
     * @param fileName
     * @return
     * @throws Exception
     */
    private List<Get> loadFileContent(String fileName, Map<String, Photo> photoInfo, Map<String, Audio> AudioInfo,
                                      Map<String, TipsMeshGrid> meshMap)
            throws Exception {
        Scanner scanner = new Scanner(new FileInputStream(fileName));

        List<Get> gets = new ArrayList<Get>();

        while (scanner.hasNextLine()) {

            total += 1;

            String rowkey = "";

            try {

                String line = scanner.nextLine();

                JSONObject json = TipsUtils.stringToSFJson(line);
                
                rowkey = json.getString("rowkey");

                int lifecycle = json.getInt("t_lifecycle");

                //20170828跨大区统计，统计上传Tips的图幅
                JSONObject gLocation = json.getJSONObject("g_location");
                try {
                    Set<String> tipsMeshes = CompGeometryUtil.calculateGeometeryMesh(GeoTranslator.geojson2Jts(gLocation));
                    TipsMeshGrid tipsMeshGrid = new TipsMeshGrid();
                    tipsMeshGrid.setMeshes(tipsMeshes);
                    tipsMeshGrid.setGridId(getTipsWktGird(json));
                    meshMap.put(rowkey, tipsMeshGrid);
                }catch (Exception e) {
                    logger.error(rowkey + "获取显示坐标图幅异常", e);
                }

                if (lifecycle == 0) {
                    failed += 1;

                    reasons.add(newReasonObject(rowkey, ErrorType.InvalidLifecycle));

                    continue;
                }

                JSONArray attachments = json.getJSONArray("attachments");

                JSONArray newFeedbacks = new JSONArray();

                for (int i = 0; i < attachments.size(); i++) {

                    // attachment结构：{"id":"","type":1,"content":""}
                    JSONObject attachment = attachments.getJSONObject(i);

                    int type = attachment.getInt("type");

                    String content = "";
                    // 照片
                    if (1 == type) {

                        content = attachment.getString("content"); // 是文件名

                        Photo photo = getPhoto(attachment, json);

                        photoInfo.put(content, photo); // 文件名为key

                        content = photo.getRowkey();
                    }
                    // 语音
                    if (2 == type) {

                        Audio audio = getAudio(attachment, json);

                        content = attachment.getString("id"); // id为key

                        AudioInfo.put(content, audio); // id为key

                    }
                    // 文字
                    if (3 == type) {
                        content = attachment.getString("content");
                    }
                    // 草图
                    if (6 == type) {
                        content = attachment.getString("content");
                    }
					/*
					 * ║║ user 整数 edit_Tips t_handler 原值导入 ║║ userRole 空 ║║ type
					 * edit_Tips attachments attachments.type ║║ content
					 * edit_Tips attachments attachments.content ║║ auditRemark
					 * 空 ║║ date 数据入库时服务器时间
					 */

                    JSONObject newFeedback = new JSONObject();

                    newFeedback.put("user", json.getInt("t_handler"));

                    newFeedback.put("userRole", "");

                    newFeedback.put("type", type);

                    newFeedback.put("content", content);

                    newFeedback.put("auditRemark", "");

                    newFeedback.put("date", json.getString("t_operateDate")); // 原值导入

                    newFeedbacks.add(newFeedback);
                }

                JSONObject feedbackObj = new JSONObject();
                feedbackObj.put("f_array", newFeedbacks);
                json.put("feedback", feedbackObj);

                String sourceType = json.getString("s_sourceType");
                JSONObject deep = json.getJSONObject("deep");
                if (sourceType.equals("2001")) {

                    double length = GeometryUtils.getLinkLength(GeoTranslator.geojson2Jts(gLocation));

                    deep.put("len", length);

                    json.put("deep", deep);
                }

                // 20170223添加：增加快线、中线任务号
                updateTaskIds(json);

                // 20170519 Tips上传服务赋值
                updateTipsStatus(json);

                // 道路名测线
                JSONObject rdNameObject = new JSONObject();
                rdNameObject.put("g_location", gLocation);
                rdNameObject.put("s_sourceType", sourceType);
                if (sourceType.equals("1901")) {
                    JSONArray names = deep.getJSONArray("n_array");
                    rdNameObject.put("n_array", names);
                    roadNameTips.put(rowkey, rdNameObject);
                } else if (sourceType.equals("1407")) {// 高速分歧
                    JSONArray infoArray = deep.getJSONArray("info");
                    JSONArray names = new JSONArray();
                    for (int i = 0; i < infoArray.size(); i++) {
                        JSONObject infoObj = infoArray.getJSONObject(i);
                        String exitStr = infoObj.getString("exit");
                        if (exitStr.length() > 8) {
                            String[] exitArray = exitStr.split("/");
                            for (String perExit : exitArray) {
                                names.add(perExit);
                            }
                        } else {
                            names.add(exitStr);
                        }
                    }
                    rdNameObject.put("n_array", names);
                    roadNameTips.put(rowkey, rdNameObject);
                    // 桥、隧道、跨线立交桥、步行街、环岛、风景路线、立交桥、航线、Highway道路名
                } else if (sourceType.equals("1510") || sourceType.equals("1511") || sourceType.equals("1509")
                        || sourceType.equals("1507") || sourceType.equals("1601") || sourceType.equals("1607")
                        || sourceType.equals("1705") || sourceType.equals("1209") || sourceType.equals("8006")) {
                    JSONArray names = new JSONArray();
                    String nameStr = deep.getString("name");
                    names.add(nameStr);
                    rdNameObject.put("n_array", names);
                    roadNameTips.put(rowkey, rdNameObject);
                }

                if (3 == lifecycle) {
                    insertTips.put(rowkey, json);
                } else {
                    updateTips.put(rowkey, json);
                }

                Get get = new Get(rowkey.getBytes());

                get.addColumn("data".getBytes(), "track".getBytes());

                get.addColumn("data".getBytes(), "feedback".getBytes());

                get.addColumn("data".getBytes(), "deep".getBytes());

                get.addColumn("data".getBytes(), "geometry".getBytes());

                gets.add(get);

            } catch (Exception e) {
                failed += 1;

                reasons.add(newReasonObject(rowkey, ErrorType.InvalidData));

                logger.error(e.getMessage(), e);

                e.printStackTrace();
            }

        }

        return gets;
    }


    /**
     * @Description:获取中线快线任务号
     * @param json
     * @author: y
     * @throws Exception
     * @time:2017-2-23 上午9:40:53
     */
    public void updateTaskIds(JSONObject json) throws Exception {

        json.put("s_qTaskId", s_qTaskId);

        json.put("s_qSubTaskId", s_qSubTaskId);

        json.put("s_mTaskId", s_mTaskId);

        json.put("s_mSubTaskId", s_mSubTaskId);

    }

    /**
     * Tips上传FCC服务赋值
     *
     * @param json
     */
    public void updateTipsStatus(JSONObject json) {
        json.put("t_tipStatus", TipsUpload.IMPORT_TIP_STATUS);
        json.put("t_date", currentDate);
    }

    /**
     * @Description:TOOD
     * @param attachment
     * @param json
     * @return
     * @author: y
     * @time:2016-12-3 下午3:22:17
     */
    private Audio getAudio(JSONObject attachment, JSONObject json) {

        Audio audio = new Audio();

        String id = attachment.getString("id");

        audio.setRowkey(id);

        audio.setA_uuid(id);

        audio.setA_fileName(attachment.getString("content"));

        audio.setA_uploadUser(json.getInt("t_handler"));

        audio.setA_uploadDate(json.getString("t_operateDate"));

        return audio;
    }

    /**
     * 从Hbase读取要修改和删除的Tips
     *
     * @param htab
     * @param gets
     * @throws Exception
     */
    private void loadOldTips(Table htab, List<Get> gets) throws Exception {

        if (0 == gets.size()) {
            return;
        }

        Result[] results = htab.get(gets);

        for (Result result : results) {

            if (result.isEmpty()) {
                continue;
            }

            String rowkey = new String(result.getRow());

            try {
                JSONObject jo = new JSONObject();

                String track = new String(result.getValue("data".getBytes(), "track".getBytes()));
                jo.put("track", track);

                if (result.containsColumn("data".getBytes(), "feedback".getBytes())) {
                    String fastFeedback = new String(result.getValue("data".getBytes(), "feedback".getBytes()));
                    JSONObject feedback = TipsUtils.stringToSFJson(fastFeedback);

                    jo.put("feedback", feedback);
                } else {
                    jo.put("feedback", TipsUtils.OBJECT_NULL_DEFAULT_VALUE);
                }

                String geometry = new String(result.getValue("data".getBytes(), "geometry".getBytes()));
                jo.put("geometry", geometry);

                String deep = new String(result.getValue("data".getBytes(), "deep".getBytes()));
                jo.put("deep", deep);

                oldTips.put(rowkey, jo);
            } catch (Exception e) {
                logger.error(e.getMessage(), e.getCause());
                throw e;
            }
        }
    }

    /**
     * 处理新增的Tips
     *
     * @param puts
     */
    private void doInsert(List<Put> puts, List<TipsDao> solrIndexList, List<RegionMesh> regionMeshes,
                          Map<String, TipsMeshGrid> meshMap, Map<String, Integer> gridCountMap) throws Exception {
        Set<Entry<String, JSONObject>> set = insertTips.entrySet();

        Iterator<Entry<String, JSONObject>> it = set.iterator();

        while (it.hasNext()) {

            String rowkey = "";

            try {
                Entry<String, JSONObject> en = it.next();

                rowkey = en.getKey();

                JSONObject json = en.getValue();

                Put put = null;

                // old有则更新,判断是否已存在
                if (oldTips.containsKey(rowkey)) {

                    JSONObject oldTip = oldTips.get(rowkey);

                    // 差分判断是不是tips无变更(鲜度验证的tips)，是则不更新
                    if (isFreshnessVerification(oldTip, json)) {

                       // reasons.add(newReasonObject(rowkey, ErrorType.FreshnessVerificationData));
                    	freshed.add(newReasonObject(rowkey, ErrorType.FreshnessVerificationData));
                        continue;
                    }

                    // 对比采集时间，采集时间和数据库中 hbase old.trackinfo.date(最后一条)
                    int res = canUpdate(oldTip, json.getString("t_dataDate"));
                    if (res < 0) {
                        failed += 1;
                        // -1表示old已删除
                        if (res == -1) {
                            reasons.add(newReasonObject(rowkey, ErrorType.Deleted));
                        }
                        // else =-2表示当前采集时间较旧
                        else {
                          //  reasons.add(newReasonObject(rowkey, ErrorType.InvalidDate));
                        	conflict.add(newReasonObject(rowkey, ErrorType.InvalidDate));
                        }
                        continue;
                    }

                    put = updatePut(rowkey, json, oldTip);

                    // 修改的需要差分
                    allNeedDiffRowkeysCodeMap.put(rowkey, json.getString("s_sourceType"));

                    //判断数据是否在大区库范围内，如果不在，则强制刷成无任务
                    updateTipCrossRegion(meshMap, rowkey, regionMeshes, json, gridCountMap);

                } else {
                    put = insertPut(rowkey, json);
                    // 修改的需要差分
                    allNeedDiffRowkeysCodeMap.put(rowkey, json.getString("s_sourceType"));

                    //判断数据是否在大区库范围内，如果不在，则强制刷成无任务
                    updateTipCrossRegion(meshMap, rowkey, regionMeshes, json, gridCountMap);
                }

                puts.add(put);
                
                json.put("t_dataDate", t_dataDate); //重新赋值 ，必须放在 时间判断后，确定入库后修改这个时间。
                TipsDao tipsIndexModel = TipsUtils.generateSolrIndex(json, TipsUpload.IMPORT_STAGE);
                solrIndexList.add(tipsIndexModel);

                //中线子任务第一采集时间
                if(s_mSubTaskId > 0 && isInsertFirstTime) {
                    if(StringUtils.isNotEmpty(firstCollectTime)) {
                        long lastTime = DateUtils.stringToLong(firstCollectTime, "yyyyMMddHHmmss");
                        long thisTime = DateUtils.stringToLong(json.getString("t_operateDate"), "yyyyMMddHHmmss");
                        if(thisTime < lastTime) {//如果当前Tips时间小于之前记录的采集时间
                            firstCollectTime = json.getString("t_operateDate");
                        }
                    }else{
                        firstCollectTime = json.getString("t_operateDate");
                    }
                }
            } catch (Exception e) {
                failed += 1;

                reasons.add(newReasonObject(rowkey, ErrorType.InvalidData));

                e.printStackTrace();
            }
        }
    }

    /**
     * 判断Tips是否在任务大区范围内，如果不在数据刷成无任务
     * @param meshMap
     * @param rowkey
     * @param regionMeshes
     * @param json
     */
    private void updateTipCrossRegion(Map<String, TipsMeshGrid> meshMap, String rowkey, List<RegionMesh> regionMeshes,
                                      JSONObject json, Map<String, Integer> gridCountMap) throws Exception{
        //无任务上传，Tips数据本身就是无任务
        //按照任务号上传，则判断是否在任务大区库内，不在则数据强制成无任务
        boolean isNoTask = true;//默认无任务
        if(subtask != null && subtask.getSubtaskId() > 0) {
            Set<String> tipsMeshes = meshMap.get(rowkey).getMeshes();
            for(RegionMesh regionMesh : regionMeshes) {
                //日编大区库ID
                int dailyDbId = regionMesh.getDailyDbId();
                if(dailyDbId == subtask.getDbId()) {//查找和任务相同的大区库
                    for(String tipsMesh : tipsMeshes) {//遍历Tips数据的图幅
                        //判断Tips图幅是否在任务大区库图幅范围内，如果不在，则为无任务
                        if(regionMesh.meshContains(tipsMesh)){
                            isNoTask = false;//在任务大区库图幅范围内，则为有任务
                            break;
                        }
                    }

                }
            }
        }
        if(isNoTask) {//如果数据为无任务
            json.put("s_qTaskId", 0);
            json.put("s_qSubTaskId", 0);
            json.put("s_mTaskId", 0);
            json.put("s_mSubTaskId", 0);

            //根据统计坐标计算该Tips所在grid
            String gridId = getTipsWktGird(json);
            int count = 1;
            if(gridCountMap.containsKey(gridId)) {
                count = gridCountMap.get(gridId) + 1;
                gridCountMap.remove(gridId);
            }
            gridCountMap.put(gridId, count);
        }
    }

    /**
     * 根据tipsjson获取统计坐标
     * @return
     */
    private String getTipsWktGird(JSONObject json) throws Exception{
        String sourceType = json.getString("s_sourceType");
        JSONObject g_location = json.getJSONObject("g_location");
        JSONObject deep = json.getJSONObject("deep");
        String wkt = TipsImportUtils.generateSolrStatisticsWkt(sourceType, deep,g_location, null);
        Geometry wktGeo = GeoTranslator.wkt2Geometry(wkt);
        Set<String> grids = CompGeometryUtil.geo2GridsWithoutBreak(wktGeo);
        //任选一个grid作为该Tips的girdId
        String gridId = grids.iterator().next();
        return gridId;
    }

    /**
     * @Description:判断是否是鲜度验证的tips
     * @param oldTip
     * @param json
     * @return
     * @author: y
     * @time:2017-1-18 下午1:39:55
     */
    private boolean isFreshnessVerification(JSONObject oldTip, JSONObject json) {

        // （鲜度验证的数据不入库：geometry、deep、feedback、track.t_command等字段内容不变）。
        // 已确认不需要每个属性字段差分。几个属性合起来比较就可以了
        // old
        // solr里面就是字符串
        String geometryOld = oldTip.getString("geometry");
        JSONObject geoObj = JSONObject.fromObject(geometryOld);
        String g_locationOld = geoObj.getString("g_location");
        String g_guideOld = geoObj.getString("g_guide");
        String deepOld = oldTip.getString("deep");
        String feedbackOld = oldTip.getString("feedback");

        JSONObject trackOld = oldTip.getJSONObject("track");
        int tCommandOld = trackOld.getInt("t_command");

        Geometry g_locationOldGeo = GeoTranslator.geojson2Jts(JSONObject.fromObject(g_locationOld));
        String g_locationOldWkt = GeoTranslator.jts2Wkt(g_locationOldGeo);
        Geometry g_guideOldGeo = GeoTranslator.geojson2Jts(JSONObject.fromObject(g_guideOld));
        String g_guideOldWkt = GeoTranslator.jts2Wkt(g_guideOldGeo);
        String mdb5Old = MD5Utils.md5(g_locationOldWkt + g_guideOldWkt + deepOld + feedbackOld + tCommandOld);

        // new
        String g_locationNew = json.getString("g_location");
        String g_guideNew = json.getString("g_guide");
        String deepNew = json.getString("deep");
        String feedbackNew = json.getString("feedback");

        // JSONObject trackNew=json.getJSONObject("track");
        int tCommandNew = json.getInt("t_command");
        Geometry g_locationNewGeo = GeoTranslator.geojson2Jts(JSONObject.fromObject(g_locationNew));
        String g_locationNewWkt = GeoTranslator.jts2Wkt(g_locationNewGeo);
        Geometry g_guideNewGeo = GeoTranslator.geojson2Jts(JSONObject.fromObject(g_guideNew));
        String g_guideNewWkt = GeoTranslator.jts2Wkt(g_guideNewGeo);
        String mdb5New = MD5Utils.md5(g_locationNewWkt + g_guideNewWkt + deepNew + feedbackNew + tCommandNew);

        if (mdb5Old.equals(mdb5New)) {
            return true;
        }

        return false;
    }

    private Put insertPut(String rowkey, JSONObject json) {

        Put put = new Put(rowkey.getBytes());

        // track
        int stage = TipsUpload.IMPORT_STAGE;
        // 20170519 状态流转变更
        int t_lifecycle = 3;
        // track
        TipsTrack track = new TipsTrack();
        track.setT_lifecycle(t_lifecycle);
        track.setT_date(currentDate);
        track.setT_dataDate(t_dataDate); //915新增字段，需要重新赋值为系统当前时间 
        track.setT_command(json.getInt("t_command"));
        track.setT_tipStatus(json.getInt("t_tipStatus"));
        track.addTrackInfo(stage, json.getString("t_operateDate"), json.getInt("t_handler"));

        JSONObject trackJson = JSONObject.fromObject(track);
        put.addColumn("data".getBytes(), "track".getBytes(), trackJson.toString().getBytes());

        JSONObject jsonSourceTemplate = TipsUploadUtils.getSourceConstruct();
        JSONObject jsonSource = new JSONObject();

        Iterator<String> itKey = jsonSourceTemplate.keys();

        while (itKey.hasNext()) {

            String key = itKey.next();
            jsonSource.put(key, json.get(key));
        }

        put.addColumn("data".getBytes(), "source".getBytes(), jsonSource.toString().getBytes());

        JSONObject jsonGeomTemplate = TipsUploadUtils.getGeometryConstruct();

        itKey = jsonGeomTemplate.keys();

        JSONObject jsonGeom = new JSONObject();

        while (itKey.hasNext()) {

            String key = itKey.next();
            jsonGeom.put(key, json.get(key));
        }

        put.addColumn("data".getBytes(), "geometry".getBytes(),
                TipsUtils.netJson2fastJson(jsonGeom).toString().getBytes());

        put.addColumn("data".getBytes(), "deep".getBytes(), json.getString("deep").getBytes());

        JSONObject feedback = json.getJSONObject("feedback");

        put.addColumn("data".getBytes(), "feedback".getBytes(),
                TipsUtils.netJson2fastJson(feedback).toString().getBytes());

        return put;
    }

    /**
     * 处理修改和删除的Tips
     *
     * @param puts
     */
    private void doUpdate(List<Put> puts, List<TipsDao> solrIndexList, List<RegionMesh> regionMeshes,
                          Map<String, TipsMeshGrid> meshMap, Map<String, Integer> gridCountMap) throws Exception {
        Set<Entry<String, JSONObject>> set = updateTips.entrySet();
        Iterator<Entry<String, JSONObject>> it = set.iterator();

        while (it.hasNext()) {
            String rowkey = "";

            try {
                Entry<String, JSONObject> en = it.next();

                rowkey = en.getKey();
                
                int lifeCycle=en.getValue().getInt("t_lifecycle");
                
                //修改不存在
                if (!oldTips.containsKey(rowkey)&&lifeCycle==2) {
                    failed += 1;

                    reasons.add(newReasonObject(rowkey, ErrorType.Notexist));

                    continue;
                }
                //删除不存在
                else if(!oldTips.containsKey(rowkey)&&lifeCycle==1){
                	failed += 1;

                    reasons.add(newReasonObject(rowkey, ErrorType.deleteNoteExists));

                    continue;
                }

                JSONObject oldTip = oldTips.get(rowkey);

                JSONObject json = en.getValue();

                int lifecycle = json.getInt("t_lifecycle");
                // 是否是鲜度验证的tips lifecycle==1删除的，不进行鲜度验证
                if (lifecycle != 1 && isFreshnessVerification(oldTip, json)) {

                  //  reasons.add(newReasonObject(rowkey, ErrorType.FreshnessVerificationData));
                    
                    freshed.add(newReasonObject(rowkey, ErrorType.FreshnessVerificationData));

                    continue;
                }

                // 时间判断
                int res = canUpdate(oldTip, json.getString("t_dataDate"));
                if (res < 0) {
                    failed += 1;

                    if (res == -1) {
                        reasons.add(newReasonObject(rowkey, ErrorType.Deleted));
                    } else {
                        //reasons.add(newReasonObject(rowkey, ErrorType.InvalidDate));
                    	conflict.add(newReasonObject(rowkey, ErrorType.InvalidDate));
                    }

                    continue;
                }

                //判断数据是否在大区库范围内，如果不在，则强制刷成无任务
                updateTipCrossRegion(meshMap, rowkey, regionMeshes, json, gridCountMap);

                Put put = updatePut(rowkey, json, oldTip);
                puts.add(put);
                
                json.put("t_dataDate", t_dataDate); //重新赋值 ，必须放在 时间判断后，确定入库后修改这个时间。
                TipsDao tipsIndexModel = TipsUtils.generateSolrIndex(json, TipsUpload.IMPORT_STAGE);
                solrIndexList.add(tipsIndexModel);

                // 修改的需要差分
                allNeedDiffRowkeysCodeMap.put(rowkey, json.getString("s_sourceType"));

                //中线子任务第一采集时间
                if(s_mSubTaskId > 0 && isInsertFirstTime) {
                    if(StringUtils.isNotEmpty(firstCollectTime)) {
                        long lastTime = DateUtils.stringToLong(firstCollectTime, "yyyyMMddHHmmss");
                        long thisTime = DateUtils.stringToLong(json.getString("t_operateDate"), "yyyyMMddHHmmss");
                        if(thisTime < lastTime) {//如果当前Tips时间小于之前记录的采集时间
                            firstCollectTime = json.getString("t_operateDate");
                        }
                    }else{
                        firstCollectTime = json.getString("t_operateDate");
                    }
                }

            } catch (Exception e) {
                failed += 1;

                reasons.add(newReasonObject(rowkey, ErrorType.InvalidData));

                e.printStackTrace();
            }
        }
    }

    private Put updatePut(String rowkey, JSONObject json, JSONObject oldTip) throws IOException {

        Put put = new Put(rowkey.getBytes());

        int t_lifecycle = json.getInt("t_lifecycle");

        JSONObject oldTrack = oldTip.getJSONObject("track");

        // track
        int stage = TipsUpload.IMPORT_STAGE;
        TipsTrack track = (TipsTrack) JSONObject.toBean(oldTrack, TipsTrack.class);
        track.setT_lifecycle(t_lifecycle);
        track.setT_date(currentDate);
        track.setT_dataDate(t_dataDate); //915新增字段，需要重新赋值为系统当前时间 
        track.setT_tipStatus(json.getInt("t_tipStatus"));
        track.addTrackInfo(stage, json.getString("t_operateDate"), json.getInt("t_handler"));

        JSONObject trackJson = JSONObject.fromObject(track);
        put.addColumn("data".getBytes(), "track".getBytes(), trackJson.toString().getBytes());

        JSONObject jsonSourceTemplate = TipsUploadUtils.getSourceConstruct();

        Iterator<String> itKey = jsonSourceTemplate.keys();

        JSONObject jsonSource = new JSONObject();

        while (itKey.hasNext()) {

            String key = itKey.next();
            jsonSource.put(key, json.get(key));
        }

        put.addColumn("data".getBytes(), "source".getBytes(), jsonSource.toString().getBytes());

        JSONObject jsonGeomTemplate = TipsUploadUtils.getGeometryConstruct();

        itKey = jsonGeomTemplate.keys();

        JSONObject jsonGeom = new JSONObject();

        while (itKey.hasNext()) {

            String key = itKey.next();
            jsonGeom.put(key, json.get(key));
        }

        put.addColumn("data".getBytes(), "geometry".getBytes(),
                TipsUtils.netJson2fastJson(jsonGeom).toString().getBytes());

        put.addColumn("data".getBytes(), "deep".getBytes(), json.getString("deep").getBytes());

        JSONObject feedback = json.getJSONObject("feedback");

        put.addColumn("data".getBytes(), "feedback".getBytes(),
                TipsUtils.netJson2fastJson(feedback).toString().getBytes());

        return put;
    }

    private Photo getPhoto(JSONObject attachment, JSONObject tip) {

        Photo photo = new Photo();

        //20170809 判断是否存在extContent
        if(attachment.containsKey("extContent")) {
            JSONObject extContent = attachment.getJSONObject("extContent");
            double lng = extContent.getDouble("longitude");
            double lat = extContent.getDouble("latitude");
            photo.setA_longitude(lng);
            photo.setA_latitude(lat);
            photo.setA_direction(extContent.getDouble("direction"));
            photo.setA_shootDate(extContent.getString("shootDate"));
            photo.setA_deviceNum(extContent.getString("deviceNum"));
        }


        // String uuid = fileName.replace(".jpg", "");
        //
        // String type = String.format("%02d",
        // Integer.valueOf(tip.getString("s_sourceType")));
        //
        // String key = GeoHash.geoHashStringWithCharacterPrecision(lat, lng,
        // 12)
        // + type + uuid;

        String id = attachment.getString("id");

        photo.setRowkey(id);

        // photo.setA_uuid(id.substring(14));

        photo.setA_uuid(id);

        photo.setA_uploadUser(tip.getInt("t_handler"));

        photo.setA_uploadDate(tip.getString("t_operateDate"));

        //Tips上传a_sourceId = 2
        photo.setA_sourceId(2);

        photo.setA_fileName(attachment.getString("content"));

        photo.setA_content(1);

        photo.setA_refUuid("");

        photo.setA_version(SystemConfigFactory.getSystemConfig().getValue(PropConstant.seasonVersion));//当前版本
        return photo;
    }

    private int canUpdate(JSONObject oldTips, String collectior_tDataDate) {
        JSONObject oldTrack = oldTips.getJSONObject("track");

        int lifecycle = oldTrack.getInt("t_lifecycle");
        if(!oldTrack.containsKey("t_trackInfo")) {
            return 0;
        }
        JSONArray tracks = oldTrack.getJSONArray("t_trackInfo");
        if(tracks == null || tracks.size() == 0) {
            return 0;
        }
        String lastDate = null;

        // 入库仅与上次stage=1的数组data进行比较. 最后一条stage=1的数据(取消)
     /*   for (int i = tracks.size(); i > 0; i--) {

            JSONObject info = tracks.getJSONObject(i - 1);

            if (info.getInt("stage") == 1) {

                lastDate = info.getString("date");

                break;
            }
        }*/
        
        //入库和t_dataDate时间做对比
        lastDate=oldTrack.getString("t_dataDate");

        JSONObject lastTrack = tracks.getJSONObject(tracks.size() - 1);

        int lastStage = lastTrack.getInt("stage");

        // lifecycle:0（无） 1（删除）2（修改）3（新增） ;
        // 0 初始化；1 外业采集；2 内业日编；3 内业月编；4 GDB增量；5 内业预处理；6 多源融合；
        // 1)是增量更新删除：不对比时间。
        // 库里最后最状态是不是增量更新删除：lifecycle=1（删除），t_stage=4（增量更新）,是，则不更新 : -1表示old已删除
        if (lifecycle == 1 && lastStage == 4) {
            return -1;
        }

        // 增量的：新增修改的和stage=0的一样处理，不判断时间
        if (lifecycle != 1 && lastStage == 4) {
            return 0;
        }

        if (lastStage == 0) {
            return 0;
        }

        // 2) 需要用stage=1的最后一条数据和采集端对比（stage=0是初始化数据，不进行时间对比）
        // 如果不存在stage=1时，则按以下情况比较（如果存在（stage=5或者stage=6）且不存在stage=1时，直接覆盖）
        if (lastDate == null && hasPreStage(tracks)) {
            return 0;
        } else {
        	//20170830修改   时间判断修改。外业的时间 >=库里面的时间  都可以上传。
            if (collectior_tDataDate.compareTo(lastDate) <0) {
                return -2;
            }
        }
        // 其他情况都返回0，包括 stage=0 、stage=4(除了增量更新删除的)
        return 0;
    }

    /**
     * @Description:是否存在stage=5的
     * @param tracks
     * @return
     * @author: y
     * @time:2017-4-19 下午12:56:35
     */
    private boolean hasPreStage(JSONArray tracks) {

        for (int i = 0; i < tracks.size(); i++) {

            JSONObject info = tracks.getJSONObject(i);

            if (info.getInt("stage") == 5 || info.getInt("stage") == 6) {

                return true;
            }
        }
        return false;
    }

    private JSONObject newReasonObject(String rowkey, int type) {

        JSONObject json = new JSONObject();

        json.put("rowkey", rowkey);

        json.put("type", type);

        return json;
    }

    public void runQuality(String fileName) throws Exception {
        java.sql.Connection checkConn = null;
        PreparedStatement deletePstmt = null;
        PreparedStatement insertPstmt = null;
        Connection hbaseConn = null;
        Table htab = null;
        java.sql.Connection oracleConn = null;
        java.sql.Connection regionDBConn = null;
        try {
            String firstCollectTime = "";
            if (subtask != null && subtask.getIsQuality() == 1) {// 是质检子任务
                List<FieldRoadQCRecord> records = loadQualityContent(fileName);

                oracleConn = DBConnector.getInstance().getTipsIdxConnection();
                int dbId = subtask.getDbId();
                regionDBConn = DBConnector.getInstance().getConnectionById(dbId);

                logger.info("start uplod qc problem,subtaskid:" + subtask.getSubtaskId());
                ManApi manApi = (ManApi) ApplicationContextUtil.getBean("manApi");
                Map<String, Object> subTaskMap = manApi.getSubtaskInfoByQuality(subTaskId);
                String groupName = "";
                String province = "";
                String city = "";
                int userId = 0;
                String version = "";
                String startDate = "";
                try {
                    groupName = (String) subTaskMap.get("groupName");
                    province = (String) subTaskMap.get("province");
                    city = (String) subTaskMap.get("city");
                    userId = Integer.valueOf((String) subTaskMap.get("exeUserId"));
                    version = (String) subTaskMap.get("version");
                    startDate = (String) subTaskMap.get("planStartDate");

                    //查询质检子任务对应的常规子任务的第一采集时间
                    int collectSubTaskId = (Integer) subTaskMap.get("collectSubTaskId");
                    Map<String, Object> timelineMap = manApi.queryTimelineByCondition(collectSubTaskId, "subtask", TIMELINE_FIRST_DATE_TYPE);
                    if(timelineMap != null && timelineMap.size() > 0) {
                        firstCollectTime = (String)timelineMap.get("operateDate");
                    }
                }catch (Exception e) {
                    qcErrMsg = "质检子任务" + subTaskId + "信息不完整";
                    e.printStackTrace();
                    return;
                }
                String deleteSql = "delete from FIELD_RD_QCRECORD " + "where PROBLEM_NUM = ?";

                String insertSql = "INSERT INTO FIELD_RD_QCRECORD(UUID, AREA, FIELD_GROUP, LINK_PID, PROVINCE, "
                        + "CITY, ROWKEY, QC_SUBTASK, QC_SUBTASK_NAME, ROUTE_NUM, ESTAB_LEVEL, PROBLEM_NUM, "
                        + "PHOTO_NUM, MESH_ID, GROUP_NAME, POI_FID, KIND_CODE, CLASS_TOP, CLASS_MEDIUM, "
                        + "CLASS_BOTTOM, PROBLEM_TYPE, PROBLEM_PHENOMENON, PROBLEM_DESCRIPTION, INITIAL_CAUSE, "
                        + "ROOT_CAUSE, CHECK_USERID, CHECK_TIME, COLLECTOR_USERID, COLLECTOR_TIME, "
                        + "CHECK_DEPARTMENT, CHECK_MODE, MODIFY_DATE, MODIFY_USERID, CONFIRM_USERID, "
                        + "VERSION, PROBLEM_LEVEL, PHOTO_EXIST, KIND, FC, MEMO_USERID, CLASS_WEIGHT, "
                        + "PROBLEM_WEIGHT, TOTAL_WEIGHT, WORD_YEAR)"
                        + "values (SEQ_FIELD_RD_QCRECORD.nextval, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
                        + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                checkConn = DBConnector.getInstance().getCheckConnection();
                deletePstmt = checkConn.prepareStatement(deleteSql);
                insertPstmt = checkConn.prepareStatement(insertSql);

                hbaseConn = HBaseConnector.getInstance().getConnection();

                htab = hbaseConn.getTable(TableName.valueOf(HBaseConstant.tipTab));
                String problem_num = "";
                int sq = 0;
                for (FieldRoadQCRecord record : records) {
                    try {
                        sq++;
                        problem_num = record.getId();

                        deletePstmt.setString(1, problem_num);
                        deletePstmt.addBatch();

                        // 按照Tips统计坐标所在图幅统计
                        TipsIndexOracleOperator operator = new TipsIndexOracleOperator(oracleConn);
                        TipsDao solrObj = operator.getById(record.getRowkey());
                        String linkPidString = null;
                        if(solrObj.getS_sourceType().equals("2001")) {//测线的linkid返回其本身
                            linkPidString = solrObj.getId();
                        }else{
                            linkPidString = getQualityLinkPid(solrObj, regionDBConn);
                        }
                        record.setLink_pid(linkPidString);
                        insertPstmt.setString(1, "");
                        insertPstmt.setString(2, groupName);
                        insertPstmt.setString(3, record.getLink_pid());
                        insertPstmt.setString(4, province);
                        insertPstmt.setString(5, city);
                        insertPstmt.setString(6, record.getRowkey());
                        insertPstmt.setInt(7, subTaskId);
                        insertPstmt.setString(8, subtask.getName());
                        insertPstmt.setInt(9, 0);
                        insertPstmt.setString(10, "");
                        insertPstmt.setString(11, problem_num);
                        insertPstmt.setString(12, "");
                        Geometry geo = solrObj.getWkt();
                        String mesh = TipsGridCalculate.calculate(geo).iterator().next().substring(0, 6);
                        insertPstmt.setInt(13, Integer.valueOf(mesh));
                        insertPstmt.setString(14, "");
                        insertPstmt.setString(15, "");
                        insertPstmt.setString(16, "");
                        insertPstmt.setString(17, record.getClass_top());
                        insertPstmt.setString(18, record.getClass_bottom());
                        insertPstmt.setString(19, record.getClass_bottom());
                        insertPstmt.setString(20, record.getType());
                        insertPstmt.setString(21, record.getPhenomenon());
                        insertPstmt.setString(22, record.getDescription());
                        insertPstmt.setString(23, record.getInitial_cause());
                        insertPstmt.setString(24, record.getRoot_cause());
                        insertPstmt.setString(25, record.getCheck_userid());
                        insertPstmt.setString(26, record.getCheck_time());
                        // 当关联的link上tips外业有采集时，该link关联的所有tips都记录常规采集任务对应的userid,
                        // 当关联link上挂接的tips全部未采集时，该字段记录为AAA.（是否采集过通过stage=1,handler=常规采集子任务userid判断）
                        String collecorUserId = this.getCollectUserId(operator, record.getLink_pid(), userId, htab);
                        insertPstmt.setString(27, collecorUserId);
                        // 读取常规采集子任务的date
                        insertPstmt.setString(28, firstCollectTime);
                        insertPstmt.setString(29, "外业采集部");
                        insertPstmt.setInt(30, subtask.getQualityMethod());
                        insertPstmt.setString(31, record.getCheck_time());
                        insertPstmt.setString(32, record.getCheck_userid());
                        insertPstmt.setString(33, record.getConfirm_userid());
                        // 读取当前版本号
                        insertPstmt.setString(34, version);
                        insertPstmt.setString(35, "C");
                        insertPstmt.setInt(36, 0);

                        // 查询关联link或者测线在fcc中是否有种别Tips
//						String where = "((t.s_sourceType='1201' AND t.t_lifecycle<>1) OR t.s_sourceType='2001')";
//                        where += " AND (t.id = '" + record.getLink_pid() + "' OR EXISTS(SELECT 1 FROM TIPS_LINKS L WHERE t.id=l.id and L.LINK_ID = '" + record.getLink_pid() + "'))";
//                        String query = "select * from tips_index t where " + where;
                        String query = "WITH TMP AS\n" +
                                " (SELECT ID\n" +
                                "    FROM TIPS_INDEX T\n" +
                                "   WHERE T.ID = '" + record.getLink_pid() + "'\n" +
                                "     AND T.S_SOURCETYPE = '2001'\n" +
                                "  UNION\n" +
                                "  SELECT ID\n" +
                                "    FROM TIPS_INDEX T\n" +
                                "   WHERE T.S_SOURCETYPE = '1201'\n" +
                                "     AND T.T_LIFECYCLE <> 1\n" +
                                "     AND EXISTS (SELECT 1\n" +
                                "            FROM TIPS_LINKS L\n" +
                                "           WHERE T.ID = L.ID\n" +
                                "             AND L.LINK_ID = '" + record.getLink_pid() + "'))\n" +
                                "SELECT *\n" +
                                "  FROM TIPS_INDEX T\n" +
                                " WHERE EXISTS (SELECT 1 FROM TMP TT WHERE TT.ID = T.ID)";
                        List<TipsDao> snapotList = operator.query(query);
                        int kind = 0;// 种别直接在FCC库中获取
                        int fc = 0;// FC直接在GDB中获取
                        int linkPid = 0;
                        try {
                            linkPid = Integer.valueOf(record.getLink_pid());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (linkPid != 0) {
                            JSONObject linkObj = TipsImportUtils.queryLinkKindFC(regionDBConn, String.valueOf(linkPid));
                            if (linkObj != null) {
                                kind = linkObj.getInt("kind");
                                fc = linkObj.getInt("fc");
                            }
                        }

                        if (snapotList != null && snapotList.size() > 0) {// FCC存在
                            TipsDao kindObj = snapotList.get(0);
                            JSONObject deepObj = JSONObject.fromObject(kindObj.getDeep());
                            kind = deepObj.getInt("kind");
                        }
                        insertPstmt.setInt(37, kind);
                        insertPstmt.setInt(38, fc);
                        String memoUserId = "";
                        if (collecorUserId.equals("AAA")) {
                            memoUserId = String.valueOf(userId);
                        }
                        insertPstmt.setString(39, memoUserId);
                        insertPstmt.setString(40, "");
                        insertPstmt.setString(41, "");
                        insertPstmt.setString(42, "");
                        insertPstmt.setString(43, "");

                        insertPstmt.addBatch();

                        if (sq % 500 == 0) {
                            deletePstmt.executeBatch();
                            insertPstmt.executeBatch();
                            checkConn.commit();
                        }
                    } catch (Exception e) {
                        JSONObject reasonObj = newReasonObject(problem_num, 1);
                        qcReasons.add(reasonObj);
                        e.printStackTrace();
                    }
                }
                try {
                    deletePstmt.executeBatch();
                    insertPstmt.executeBatch();
                    checkConn.commit();
                } catch (Exception e) {
                    JSONObject reasonObj = newReasonObject(problem_num, 1);
                    qcReasons.add(reasonObj);
                    e.printStackTrace();
                }
                // Clob clob = ConnectionUtil.createClob(checkConn);
                // clob.setString(1, builder.toString());
                // deletePstmt.setClob(1, clob);
                // deletePstmt.execute();
                // checkConn.commit();
                // DBUtils.closeStatement(deletePstmt);
            }
        } catch (Exception e) {
            DbUtils.rollbackAndCloseQuietly(checkConn);
            DbUtils.rollbackAndCloseQuietly(oracleConn);
            DbUtils.rollbackAndCloseQuietly(regionDBConn);
            logger.error("质检问题上传失败，原因为：" + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtils.closeStatement(deletePstmt);
            DBUtils.closeStatement(insertPstmt);
            DbUtils.commitAndCloseQuietly(checkConn);
            DbUtils.commitAndCloseQuietly(oracleConn);
            DbUtils.commitAndCloseQuietly(regionDBConn);
        }

    }

    private String getQualityLinkPid(TipsDao tipsDao, java.sql.Connection regionDBConn) throws Exception {
        String linkPid = "";
        try{
            String deep = tipsDao.getDeep();
            String sourceType = tipsDao.getS_sourceType();
            if(StringUtils.isNotEmpty(deep)) {
                JSONObject deepJson = JSONObject.fromObject(deep);
                if (TipsStatConstant.fieldQCExpIdType.contains(sourceType)) {//exp.id
                    JSONObject exp = deepJson.getJSONObject("exp");
                    linkPid = exp.getString("id");
                } else if (TipsStatConstant.fieldQCFIdType.contains(sourceType)) {//f.id
                    JSONObject exp = deepJson.getJSONObject("f");
                    linkPid = exp.getString("id");
                } else if (TipsStatConstant.fieldQCFIdNodeType.contains(sourceType)) {
                    //f.type<>3时，取f.id;f.type=3时，取该node关联的任意一条link
                    JSONObject exp = deepJson.getJSONObject("f");
                    int type = exp.getInt("type");
                    String id = exp.getString("id");
                    if (type != 3) {
                        linkPid = exp.getString("id");
                    } else {
                        //TODO 该node关联的任意一条link
                        String nodeLink = TipsImportUtils.queryNodeRelateLink(regionDBConn, id);
                        if (StringUtils.isNotEmpty(nodeLink)) {
                            linkPid = nodeLink;
                        }
                    }
                } else if (TipsStatConstant.fieldQCInIdType.contains(sourceType)) {//in.id
                    JSONObject exp = deepJson.getJSONObject("in");
                    linkPid = exp.getString("id");
                } else if (TipsStatConstant.fieldQCOutIdType.contains(sourceType)) {//out.id
                    JSONObject exp = deepJson.getJSONObject("out");
                    linkPid = exp.getString("id");
                } else if (TipsStatConstant.fieldQCFArrayIdType.contains(sourceType)) {//farray.id
                    JSONArray fArray = deepJson.getJSONArray("f_array");
                    if (fArray != null && fArray.size() > 0) {
                        JSONObject fObj = fArray.getJSONObject(0);
                        linkPid = fObj.getString("id");
                    }
                } else if (TipsStatConstant.fieldQCPArrayIdType.contains(sourceType)) {//parray.id
                    JSONArray pArray = deepJson.getJSONArray("p_array");
                    if (pArray != null && pArray.size() > 0) {
                        JSONObject pObj = pArray.getJSONObject(0);
                        JSONObject fObj = pObj.getJSONObject("f");
                        linkPid = fObj.getString("id");
                    }
                }
            }
        }catch (Exception e) {
            throw new Exception("getQualityLinkPid获取linkpid失败！");
        }
        return linkPid;
    }

    private String getCollectUserId(TipsIndexOracleOperator operator, String linkPid, int userId, Table htab) throws Exception {
        String collecorUserId = "AAA";

        String where = "t.stage=1 AND EXISTS(SELECT 1 FROM TIPS_LINKS L WHERE t.id=l.id AND L.LINK_ID = '" + linkPid + "') AND ROWNUM = 1";
        String query = "select * from tips_index t where " + where;
        List<TipsDao> relateTips = operator.query(query);
        for (TipsDao snapshot : relateTips) {
//            collecorUserId = String.valueOf(snapshot.getHandler());

            JSONObject oldTip = HbaseTipsQuery.getHbaseTipsByRowkey(htab, snapshot.getId(), new String[]{"track"});
            JSONObject track = oldTip.getJSONObject("track");
            JSONArray trackInfoArr = track.getJSONArray("t_trackInfo");
            for (int i = trackInfoArr.size() - 1; i > -1; i--) {
                JSONObject trackInfoObj = trackInfoArr.getJSONObject(i);
                int stage = trackInfoObj.getInt("stage");
                if (stage == 1) {
                    int handler = trackInfoObj.getInt("handler");
                    if (handler == userId) {
                        collecorUserId = String.valueOf(userId);
                        break;
                    }
                }
            }
        }
        return collecorUserId;
    }

    /**
     * 读取Tips文件，组装Get列表
     *
     * @param fileName
     * @return
     * @throws Exception
     */
    private List<FieldRoadQCRecord> loadQualityContent(String fileName) throws Exception {
        Scanner scanner = new Scanner(new FileInputStream(fileName));
        List<FieldRoadQCRecord> records = new ArrayList<>();
        qcTotal = 0;
        while (scanner.hasNextLine()) {
            qcTotal++;
            String problem_num = "";
            try {
                String line = scanner.nextLine();
                com.alibaba.fastjson.JSONObject lineObj = com.alibaba.fastjson.JSONObject.parseObject(line);
                lineObj.put("class_bottom", lineObj.getString("class"));
                problem_num = lineObj.getString("id");
                FieldRoadQCRecord record = com.alibaba.fastjson.JSONObject.parseObject(lineObj.toJSONString(), FieldRoadQCRecord.class);
                problem_num = record.getId();
                records.add(record);
            } catch (Exception e) {
                JSONObject reasonObj = newReasonObject(problem_num, ErrorType.InvalidData);
                qcReasons.add(reasonObj);
                logger.error("质检问题上传解析JSON失败" + problem_num + "，原因为：" + e.getMessage());
            }

        }

        return records;
    }

    public static void main(String[] args) throws Exception {

		/*
		 * Map<String, Photo> photoMap = new HashMap<String, Photo>();
		 * 
		 * Map<String, Audio> audioMap = new HashMap<String, Audio>();
		 * 
		 * TipsUpload a = new TipsUpload();
		 * 
		 * a.run("D:/4.txt", photoMap, audioMap);
		 * 
		 * System.out.println("成功");
		 */
        // TipsUpload l = new TipsUpload(0);
        // l.run("F:\\FCC\\11151449646061.txt", null, null);

        String str = "{\"g_location\":{\"type\":\"Point\",\"coordinates\":[116.000,40.01351567]}}";
        // System.out.println(str.toString());
        JSONObject strJson = TipsUtils.stringToSFJson(str);
        //
        // com.alibaba.fastjson.JSONObject fj =
        // TipsUtils.netJson2fastJson(strJson);
        // System.out.println(fj.toString());
        // System.out.println(strJson.toString());
        JSONObject locJson1 = strJson.getJSONObject("g_location");
        JSONArray jsonArray1 = locJson1.getJSONArray("coordinates");
        System.out.println(jsonArray1.get(0));
        System.out.println(jsonArray1.get(1));
        System.out.println(GeoTranslator.transform(GeoTranslator.geojson2Jts(locJson1), 0.00001, 5));
    }
}
