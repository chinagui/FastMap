package com.navinfo.dataservice.web.fcc.controller;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.bizcommons.sys.SysLogConstant;
import com.navinfo.dataservice.bizcommons.sys.SysLogOperator;
import com.navinfo.dataservice.bizcommons.sys.SysLogStats;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.photo.Photo;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.commons.util.*;
import com.navinfo.dataservice.dao.fcc.operator.TipsIndexOracleOperator;
import com.navinfo.dataservice.engine.audio.Audio;
import com.navinfo.dataservice.engine.audio.AudioImport;
import com.navinfo.dataservice.engine.dropbox.manger.UploadService;
import com.navinfo.dataservice.engine.fcc.patternImage.PatternImageExporter;
import com.navinfo.dataservice.engine.fcc.patternImage.PatternImageImporter;
import com.navinfo.dataservice.engine.fcc.tips.*;
import com.navinfo.dataservice.engine.photo.CollectorImport;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.navinfo.navicommons.geo.computation.GridUtils;
import com.navinfo.nirobot.business.TipsTaskCheckMR;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.httpclient.util.DateUtil;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.*;

@Controller
public class TipsController extends BaseController {

    private static final Logger logger = Logger.getLogger(TipsController.class);

    @RequestMapping(value = "/tip/checkUpdate")
    public ModelAndView checkUpdate(HttpServletRequest request
    ) throws ServletException, IOException {

        String parameter = request.getParameter("parameter");

        try {

            if (StringUtils.isEmpty(parameter)) {
                throw new IllegalArgumentException("parameter参数不能为空。");
            }

            JSONObject jsonReq = JSONObject.fromObject(parameter);

            //grid和date的对象数组
            JSONArray condition = jsonReq.getJSONArray("condition");

            if (condition==null||condition.isEmpty()) {
                throw new IllegalArgumentException("参数错误:condition不能为空");
            }

            TipsSelector selector = new TipsSelector();

            JSONArray  resutArr=new JSONArray();

            for (Object object : condition) {

                JSONObject obj=JSONObject.fromObject(object);

                String grid=obj.getString("grid");

                if (StringUtils.isEmpty(grid)) {
                    throw new IllegalArgumentException("参数错误：grid不能为空。");
                }

                String date=obj.getString("date");

                if("null".equalsIgnoreCase(date)){

                    date=null;
                }

                JSONObject result=new JSONObject();

                result.put("grid", grid);

                result.put("result", selector.checkUpdate(
                        grid,date));

                resutArr.add(result);
            }

            return new ModelAndView("jsonView", success(resutArr));

        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            return new ModelAndView("jsonView", fail(e.getMessage()));
        }
    }

    @RequestMapping(value = "/tip/edit")
    public ModelAndView edit(HttpServletRequest request )
            throws ServletException, IOException {

        String parameter = request.getParameter("parameter");

        try {
            if (StringUtils.isEmpty(parameter)) {
                throw new IllegalArgumentException("parameter参数不能为空。");
            }

            JSONObject jsonReq = JSONObject.fromObject(parameter);

            String rowkey = jsonReq.getString("rowkey");

            //int stage = jsonReq.getInt("stage");

            int handler = jsonReq.getInt("handler");

            String mdFlag= jsonReq.getString("mdFlag");

            int editStatus = jsonReq.getInt("editStatus");
            int editMeth = jsonReq.getInt("editMeth");

            if (StringUtils.isEmpty(rowkey)) {
                throw new IllegalArgumentException("参数错误:rowkey不能为空");
            }

            if (StringUtils.isEmpty(mdFlag)) {
                throw new IllegalArgumentException("参数错误:mdFlag不能为空");
            }

            //值域验证
            if(!"m".equals(mdFlag)&&!"d".equals(mdFlag)){
                throw new IllegalArgumentException("参数错误:mdflag值域错误。");
            }


            String pid = null;

            if (jsonReq.containsKey("pid")) {
                pid = jsonReq.getString("pid");
            }

            TipsOperator op = new TipsOperator();

            op.update(rowkey, handler, pid, mdFlag, editStatus, editMeth);

            return new ModelAndView("jsonView", success());

        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            return new ModelAndView("jsonView", fail(e.getMessage()));
        }
    }



    /**
     * @Description:批量编辑tips状态
     * @param request
     * @return
     * @throws ServletException
     * @throws IOException
     * @author: y
     * @time:2017-2-8 上午9:15:07
     */
    @RequestMapping(value = "/tip/batchEditStatus")
    public ModelAndView batchEditStatus(HttpServletRequest request )
            throws ServletException, IOException {

        String parameter = request.getParameter("parameter");

        try {
            if (StringUtils.isEmpty(parameter)) {
                throw new IllegalArgumentException("parameter参数不能为空。");
            }

            JSONObject jsonReq = JSONObject.fromObject(parameter);

            //{mdflag:'',handler:'',data:[{rowkey:'',status:''}]}

            JSONArray data = jsonReq.getJSONArray("data");

            int handler = jsonReq.getInt("handler");

            String mdFlag= jsonReq.getString("mdFlag");


            if (data==null||data.size()==0) {
                throw new IllegalArgumentException("参数错误:data不能为空");
            }

            if (StringUtils.isEmpty(mdFlag)) {
                throw new IllegalArgumentException("参数错误:mdFlag不能为空");
            }

            //值域验证
            if(!"m".equals(mdFlag)&&!"d".equals(mdFlag)){
                throw new IllegalArgumentException("参数错误:mdflag值域错误。");
            }

            TipsOperator op = new TipsOperator();

            op.batchUpdateStatus(data, handler, mdFlag);

            return new ModelAndView("jsonView", success());

        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            return new ModelAndView("jsonView", fail(e.getMessage()));
        }
    }

    @RequestMapping(value = "/tip/import")
    public ModelAndView importTips(HttpServletRequest request) throws ServletException, IOException {

        String parameter = request.getParameter("parameter");

        logger.info("开始上传tips,parameter:"+parameter);
        try {

            if (StringUtils.isEmpty(parameter)) {
                throw new IllegalArgumentException("parameter参数不能为空。");
            }

            String beginDate = DateUtils.getSysDateFormat();

            JSONObject json = JSONObject.fromObject(parameter);

            int jobId = json.getInt("jobId");

            int subtaskId = 0;

            //外业，有可能没有任务号
            if(json.containsKey("subtaskId")){
                subtaskId=json.getInt("subtaskId");
            }
            
            AccessToken tokenObj = (AccessToken) request.getAttribute("token");
			long userId = tokenObj.getUserId();
            
            UploadService upload = UploadService.getInstance();

            String filePath = upload.unzipByJobId(jobId);

            logger.info("jobId"+jobId+"\tfilePath:"+filePath);

            TipsUpload tipsUploader = new TipsUpload(subtaskId);

            Map<String, Photo> photoMap=new HashMap<String, Photo>();

            Map<String, Audio> audioMap=new HashMap<String, Audio>();
            
            tipsUploader.run(filePath + "/"+ "tips.txt",photoMap,audioMap,userId);

            //CollectorImport.importPhoto(map, filePath + "/photo");

            CollectorImport.importPhoto(photoMap, filePath );

            AudioImport.importAudio(audioMap,filePath);

            JSONArray patternImageResultImpResult=PatternImageImporter.importImage(filePath + "/"+ "JVImage.txt",filePath +"/JVImage"); //JVImage为模式图的文件夹

            JSONObject result = new JSONObject();

            result.put("total", tipsUploader.getTotal());

            result.put("failed", tipsUploader.getFailed());

            result.put("failedReasons", tipsUploader.getReasons());
            
            result.put("conflict", tipsUploader.getConflict());
            
            result.put("freshed", tipsUploader.getFreshed());
            
            result.put("t_dataDate", tipsUploader.getT_dataDate());

            result.put("regionResults", tipsUploader.getRegionResults());

            result.put("JVImageResult", patternImageResultImpResult);

            logger.info("开始上传tips完成，jobId:"+jobId+"\tresult:"+result);


            //记录上传日志。不抛出异常
            insertStatisticsInfoNoException(jobId, subtaskId, userId,
                    tipsUploader, beginDate);

            //20170712 Tips上传增加外业质检问题记录上传
            logger.info("start uplod qc problem,filePath:"+ filePath + "/"+ "rd_qcRecord.txt");
            tipsUploader.runQuality(filePath + "/"+ "rd_qcRecord.txt");
            result.put("qcTotal", tipsUploader.getQcTotal());
            result.put("qcReasons", tipsUploader.getQcReasons());
            result.put("qcErrMsg", tipsUploader.getQcErrMsg());

            return new ModelAndView("jsonView", success(result));

        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            return new ModelAndView("jsonView", fail(e.getMessage()));
        }

    }

	/**
	 * @Description:记录上传日志
	 * @param jobId
	 * @param subtaskId
	 * @param userId
	 * @param tipsUploader
	 * @throws Exception
	 * @author: y
	 * @time:2017-8-9 上午11:09:43
	 */
	private void insertStatisticsInfoNoException(int jobId, int subtaskId,
			long userId, TipsUpload tipsUploader, String beginDate)  {
		try{
			SysLogStats log = new SysLogStats();
			log.setLogType(SysLogConstant.TIPS_UPLOAD_TYPE);
			log.setLogDesc(SysLogConstant.TIPS_UPLOAD_DESC+",jobId :"+jobId+",subtaskId:"+subtaskId);
			log.setFailureTotal(tipsUploader.getFailed());
			log.setSuccessTotal(tipsUploader.getTotal()-tipsUploader.getFailed());  
			log.setTotal(tipsUploader.getTotal());
			log.setBeginTime(beginDate);
			log.setEndTime(DateUtils.getSysDateFormat());
			log.setErrorMsg(tipsUploader.getReasons().toString());
			log.setUserId(String.valueOf(userId));
			SysLogOperator.getInstance().insertSysLog(log);
		
		}catch (Exception e) {
			logger.error("记录日志出错："+e.getMessage(), e);
		}
	}

    @RequestMapping(value = "/tip/export")
    public ModelAndView exportTips(HttpServletRequest request )
            throws ServletException, IOException {

        String parameter = request.getParameter("parameter");

        logger.info("下载tips,parameter:"+parameter);

        AccessToken tokenObj = (AccessToken) request.getAttribute("token");
        long userId = tokenObj.getUserId();
        String beginDate = DateUtils.getSysDateFormat();
        String uuid = UuidUtils.genUuid();

        try {
            if (StringUtils.isEmpty(parameter)) {
                throw new IllegalArgumentException("parameter参数不能为空。");
            }

            JSONObject jsonReq = JSONObject.fromObject(parameter);

            String day = StringUtils.getCurrentDay();

            String downloadFilePath = SystemConfigFactory.getSystemConfig().getValue(
                    PropConstant.downloadFilePathTips);

            String parentPath = downloadFilePath +File.separator+ day + "/";

            String filePath = parentPath + uuid + "/";

            File file = new File(filePath);

            if (!file.exists()) {
                file.mkdirs();
            }
            //grid和date的对象数组
            JSONArray condition = jsonReq.getJSONArray("condition");

            if (condition == null || condition.isEmpty()) {
                throw new IllegalArgumentException("参数错误:condition不能为空");
            }

            TipsExporter op = new TipsExporter();

            Map<String, Set<String>> images = new HashMap<>();
            //1.下载tips、照片、语音(照片的语音根据附件的id下载)
            int expCount = op.export(condition, filePath, "tips.txt", images);

            //2.模式图下载： 1406,1401需要导出模式图,对应元数据库SC_MODEL_MATCH_G
            //1402对应元数据库 sc_vector_match
            if(images.size() > 0) {
                Set<String> modelPtn = new HashSet<>();
                Set<String> vectorPtn = new HashSet<>();
                for(String key : images.keySet()) {
                    if(key.equals("1401") || key.equals("1406")) {
                        modelPtn.addAll(images.get(key));
                    }else if(key.equals("1402")) {
                        vectorPtn.addAll(images.get(key));
                    }
                }
                if(modelPtn.size() > 0 || vectorPtn.size() > 0) {
                    PatternImageExporter exporter = new PatternImageExporter();
                    exporter.export2SqliteByNames(filePath, modelPtn, vectorPtn);
                }
            }

            String zipFileName = uuid + ".zip";

            String zipFullName = parentPath + zipFileName;
            //3.打zip包
            ZipUtils.zipFile(filePath, zipFullName);

            String serverUrl =  SystemConfigFactory.getSystemConfig().getValue(
                    PropConstant.serverUrl);

            String downloadUrlPath = SystemConfigFactory.getSystemConfig().getValue(
                    PropConstant.downloadUrlPathTips);
            //4.返回的url
            String url = serverUrl + downloadUrlPath +File.separator+ day + "/"
                    + zipFileName;

            logger.info("url:"+url);

            JSONObject result=null; //如果没有数据，则返回 {"errmsg":"success","data":null，errcode":0} ,不返回url
            if(expCount>0){
                result=new JSONObject();

                result.put("url", url);

                result.put("downloadDate",  DateUtils.dateToString(new Date(),
                        DateUtils.DATE_COMPACTED_FORMAT));

                logger.info("下载tips完成,resut :"+result);
            }else{
                logger.info("下载tips完成,没有可下载的数据");
            }

            //Tips下载记录日志 sys库中
            insertExportLog(beginDate, userId, expCount, uuid, "");
            return new ModelAndView("jsonView", success(result));

        } catch (Exception e) {

            logger.error("下载tips出错："+e.getMessage(), e);
            //Tips下载记录日志 sys库中
            String errMsg = "下载tips出错,parameter:" + parameter + "错误信息:" + e.getCause();
            insertExportLog(beginDate, userId, 0, uuid, errMsg);
            return new ModelAndView("jsonView", fail(e.getMessage()));
        }
    }

    /**
     * Tips下载记录日志
     * @param beginDate
     * @param userId
     * @param expCount
     * @param uuid
     * @param errMsg
     */
    private void insertExportLog(String beginDate, long userId, int expCount, String uuid, String errMsg) {
        try{
            SysLogStats log = new SysLogStats();
            log.setLogType(SysLogConstant.TIPS_DOWNLOAD_TYPE);
            log.setLogDesc(SysLogConstant.TIPS_DOWNLOAD_DESC+",uuid :"+uuid);
            log.setFailureTotal(0);
            log.setSuccessTotal(expCount);
            log.setTotal(expCount);
            log.setBeginTime(beginDate);
            log.setEndTime(DateUtils.getSysDateFormat());
            log.setErrorMsg(errMsg);
            log.setUserId(String.valueOf(userId));
            SysLogOperator.getInstance().insertSysLog(log);
        }catch (Exception e) {
            logger.error("Tips下载记录日志出错："+e.getMessage(), e);
        }
    }

    @RequestMapping(value = "/tip/getByRowkey")
    public void getByRowkey(HttpServletRequest request,HttpServletResponse response
    ) throws ServletException, IOException {

        String parameter = request.getParameter("parameter");

        try {
            if (StringUtils.isEmpty(parameter)) {
                throw new IllegalArgumentException("parameter参数不能为空。");
            }

            JSONObject jsonReq = JSONObject.fromObject(parameter);

            String rowkey = jsonReq.getString("rowkey");

            if (StringUtils.isEmpty(rowkey)) {
                throw new IllegalArgumentException("参数错误：rowkey不能为空");
            }


            TipsSelector selector = new TipsSelector();

            JSONObject data = selector.searchDataByRowkey(rowkey);

            response.getWriter().println(
                    ResponseUtils.assembleRegularResult(data));
        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            response.getWriter().println(
                    ResponseUtils.assembleFailResult(e.getMessage()));
        }
    }


    @RequestMapping(value = "/tip/getByRowkeyNew")
    public void getByRowkeyNew(HttpServletRequest request,HttpServletResponse response
    ) throws ServletException, IOException {

        String parameter = request.getParameter("parameter");

        try {
            if (StringUtils.isEmpty(parameter)) {
                throw new IllegalArgumentException("parameter参数不能为空。");
            }

            JSONObject jsonReq = JSONObject.fromObject(parameter);

            String rowkey = jsonReq.getString("rowkey");

            if (StringUtils.isEmpty(rowkey)) {
                throw new IllegalArgumentException("参数错误：rowkey不能为空");
            }


            TipsSelector selector = new TipsSelector();

            JSONObject data = selector.searchDataByRowkeyNew(rowkey);

            response.getWriter().println(
                    ResponseUtils.assembleRegularResult(data));
        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            response.getWriter().println(
                    ResponseUtils.assembleFailResult(e.getMessage()));
        }
    }



    @RequestMapping(value = "/tip/getByRowkeys")
    public void getByRowkeys(HttpServletRequest request,HttpServletResponse response)
            throws ServletException, IOException {

        String parameter = request.getParameter("parameter");

        try {
            if (StringUtils.isEmpty(parameter)) {
                throw new IllegalArgumentException("parameter参数不能为空。");
            }

            JSONObject jsonReq = JSONObject.fromObject(parameter);

            JSONArray rowkeyArr = jsonReq.getJSONArray("rowkey");

            if (rowkeyArr == null || rowkeyArr.isEmpty() || rowkeyArr.size() == 0) {
                throw new IllegalArgumentException("参数错误：rowkeys不能为空");
            }


            TipsSelector selector = new TipsSelector();

            JSONArray data = selector.searchDataByRowkeyNew(rowkeyArr);

            response.getWriter().println(
                    ResponseUtils.assembleRegularResult(data));
        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            response.getWriter().println(
                    ResponseUtils.assembleFailResult(e.getMessage()));
        }
    }


    @RequestMapping(value = "/tip/getBySpatial")
    public ModelAndView getBySpatial(HttpServletRequest request
    ) throws ServletException, IOException {

        String parameter = request.getParameter("parameter");

        try {

            if (StringUtils.isEmpty(parameter)) {
                throw new IllegalArgumentException("parameter参数不能为空。");
            }

            JSONObject jsonReq = JSONObject.fromObject(parameter);

            String wkt = jsonReq.getString("wkt");

            if (StringUtils.isEmpty(wkt)) {
                throw new IllegalArgumentException("参数错误：wkt不能为空");
            }

            TipsSelector selector = new TipsSelector();

            JSONArray array = selector.searchDataBySpatial(wkt);

            return new ModelAndView("jsonView", success(array));

        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            return new ModelAndView("jsonView", fail(e.getMessage()));
        }
    }

    @RequestMapping(value = "/tip/getSnapshot")
    public void getSnapshot(HttpServletRequest request, HttpServletResponse response
    ) throws ServletException, IOException {

        String parameter = request.getParameter("parameter");

        try {

            if (StringUtils.isEmpty(parameter)) {
                throw new IllegalArgumentException("parameter参数不能为空。");
            }

            JSONObject jsonReq = JSONObject.fromObject(parameter);

            String type = jsonReq.getString("type");
            if (StringUtils.isEmpty(type)) {
                throw new IllegalArgumentException("参数错误:type不能为空。");
            }

            int dbId = jsonReq.getInt("dbId");
            if (dbId == 0) {
                throw new IllegalArgumentException("参数错误:dbId不能为空。");
            }

            int subtaskId = jsonReq.getInt("subtaskId");
            if (subtaskId == 0) {
                throw new IllegalArgumentException("参数错误:subtaskId不能为空。");
            }

            TipsSelector selector = new TipsSelector();
            JSONArray array = selector.getSnapshot(parameter);

            response.getWriter().println(
                    ResponseUtils.assembleRegularResult(array));

        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            response.getWriter().println(
                    ResponseUtils.assembleFailResult(e.getMessage()));
        }
    }

    @RequestMapping(value = "/tip/getStats")
    public ModelAndView getStats(HttpServletRequest request
    ) throws ServletException, IOException {

        String parameter = request.getParameter("parameter");

        try {
            JSONObject jsonReq = JSONObject.fromObject(parameter);

//            JSONArray grids = jsonReq.getJSONArray("grids");

            int subtaskId = jsonReq.getInt("subtaskId");

//            if (grids==null||grids.size()==0) {
//                throw new IllegalArgumentException("参数错误:grids不能为空。");
//            }

            if(!jsonReq.containsKey("workStatus")) {
                throw new IllegalArgumentException("参数错误:workStatus不能为空。");
            }

            if(subtaskId == 0) {
                throw new IllegalArgumentException("参数错误:subtaskId不能为空。");
            }

            TipsSelector selector = new TipsSelector();

            JSONObject data = selector.getStats(parameter);

            return new ModelAndView("jsonView", success(data));

        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            return new ModelAndView("jsonView", fail(e.getMessage()));
        }
    }



    @RequestMapping(value = "/tip/checkInfoTask")
    public void checkInfoTask(HttpServletRequest request,
                              HttpServletResponse response) throws ServletException, IOException {
        String parameter = request.getParameter("parameter");

        try {
            JSONObject jsonReq = JSONObject.fromObject(parameter);
            int subtaskId = jsonReq.getInt("subTaskId");
            int dbId = jsonReq.getInt("dbId");//大区库ID

            if(subtaskId == 0) {
                throw new IllegalArgumentException("参数错误:subtaskId不能为空。");
            }

            if (dbId == 0) {
                throw new IllegalArgumentException("参数错误:dbId不能为空。");
            }

            if(!jsonReq.containsKey("programType")) {
                throw new IllegalArgumentException("参数错误:programType不能为空。");
            }
            int programType = jsonReq.getInt("programType");
            if(programType != 4 && programType != 1) {
                throw new IllegalArgumentException("参数错误:programType值域只能为1或4。");
            }

            TipsSelector selector = new TipsSelector();
            List<String> rowkeyList = selector.getCheckRowkeyList(parameter);

            ManApi manApi = (ManApi) ApplicationContextUtil.getBean("manApi");
            List<Integer> gridList = manApi.getGridIdsBySubtaskId(subtaskId);
            Set<String> meshes = TipsSelectorUtils.getMeshesByGrids(gridList);
            TipsTaskCheckMR checkMR = new TipsTaskCheckMR();
            int total = checkMR.process(rowkeyList, dbId, meshes, subtaskId);//TODO:需要和李娅、俊芳确认是否使用了solr
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("total", total);

            response.getWriter().println(
                    ResponseUtils.assembleRegularResult(jsonObject));

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            response.getWriter().println(
                    ResponseUtils.assembleFailResult(e.getMessage()));
        }
    }

    @RequestMapping(value = "/tip/updateInfoCheck")
    public void updateInfoCheck(HttpServletRequest request,
                                HttpServletResponse response) throws ServletException, IOException {
        String parameter = request.getParameter("parameter");

        try {
            JSONObject jsonReq = JSONObject.fromObject(parameter);
            int resultId = jsonReq.getInt("resultId");
            int status = jsonReq.getInt("status");
            int ckConfirm = jsonReq.getInt("ckConfirm");

            if(!jsonReq.containsKey("resultId")) {
                throw new IllegalArgumentException("参数错误:resultId不能为空。");
            }

            TipsInfoCheckOperator operator = new TipsInfoCheckOperator();
            operator.updateInfoCheckResult(resultId, status, ckConfirm);

            response.getWriter().println(
                    ResponseUtils.assembleRegularResult(null));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            response.getWriter().println(
                    ResponseUtils.assembleFailResult(e.getMessage()));
        }
    }

    @RequestMapping(value = "/tip/listInfoCheckResult")
    public void listInfoCheckResult(HttpServletRequest request,
                                    HttpServletResponse response) throws ServletException, IOException {
        String parameter = request.getParameter("parameter");

        try {
            JSONObject jsonReq = JSONObject.fromObject(parameter);
            int subTaskId = jsonReq.getInt("subTaskId");
            int curPage = jsonReq.getInt("curPage");
            int pageSize = jsonReq.getInt("pageSize");

            if(!jsonReq.containsKey("subTaskId")) {
                throw new IllegalArgumentException("参数错误:subTaskId不能为空。");
            }

            TipsInfoCheckOperator operator = new TipsInfoCheckOperator();
            JSONObject jsonObject = operator.listInfoCheckResult(subTaskId, curPage, pageSize);

            response.getWriter().println(
                    ResponseUtils.assembleRegularResult(jsonObject));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            response.getWriter().println(
                    ResponseUtils.assembleFailResult(e.getMessage()));
        }
    }

    /**
     * 矢量化任务关闭前检查是否有未提交的Tips，有则不能关闭
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @RequestMapping(value = "/tip/closeInfoTask")
    public void closeInfoTask(HttpServletRequest request,
                              HttpServletResponse response) throws ServletException, IOException {
        String parameter = request.getParameter("parameter");

        try {
            JSONObject jsonReq = JSONObject.fromObject(parameter);
            int subtaskId = jsonReq.getInt("subTaskId");

            if(subtaskId == 0) {
                throw new IllegalArgumentException("参数错误:subtaskId不能为空。");
            }

            if(!jsonReq.containsKey("programType")) {
                throw new IllegalArgumentException("参数错误:programType不能为空。");
            }
            int programType = jsonReq.getInt("programType");
            if(programType != 4 && programType != 1) {
                throw new IllegalArgumentException("参数错误:programType值域只能为1或4。");
            }

            TipsSelector selector = new TipsSelector();
            List<String> rowkeyList = selector.getUnCommitRowkeyList(parameter);

            JSONObject unCommitObj = new JSONObject();
            unCommitObj.put("unCommit", rowkeyList.size());
            response.getWriter().println(
                    ResponseUtils.assembleRegularResult(unCommitObj));

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            response.getWriter().println(
                    ResponseUtils.assembleFailResult(e.getMessage()));
        }
    }

    /**
     * 情报矢量化统计 Tips总数、测线总数、测线总里程
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @RequestMapping(value = "/tip/statInfoTask")
    public void statInfoTask(HttpServletRequest request,
                             HttpServletResponse response) throws ServletException, IOException {
        String parameter = request.getParameter("parameter");

        try {
            JSONObject jsonReq = JSONObject.fromObject(parameter);
            int subtaskId = jsonReq.getInt("subTaskId");

            if(subtaskId == 0) {
                throw new IllegalArgumentException("参数错误:subtaskId不能为空。");
            }

            if(!jsonReq.containsKey("programType")) {
                throw new IllegalArgumentException("参数错误:programType不能为空。");
            }
            int programType = jsonReq.getInt("programType");
            if(programType != 4 && programType != 1) {
                throw new IllegalArgumentException("参数错误:programType值域只能为1或4。");
            }

            TipsSelector selector = new TipsSelector();
            JSONObject statObj = selector.statInfoTask(parameter);

            response.getWriter().println(
                    ResponseUtils.assembleRegularResult(statObj));

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            response.getWriter().println(
                    ResponseUtils.assembleFailResult(e.getMessage()));
        }
    }

    @RequestMapping(value = "/tip/listInfoTipsByPage")
    public void listInfoTipsByPage(HttpServletRequest request,
                                   HttpServletResponse response) throws ServletException, IOException {
        String parameter = request.getParameter("parameter");

        try {
            JSONObject jsonReq = JSONObject.fromObject(parameter);
            int subTaskId = jsonReq.getInt("subTaskId");

            if(!jsonReq.containsKey("subTaskId")) {
                throw new IllegalArgumentException("参数错误:subTaskId不能为空。");
            }
            if(subTaskId == 0) {
                throw new IllegalArgumentException("参数错误:subTaskId不能为0。");
            }

            if(!jsonReq.containsKey("programType")) {
                throw new IllegalArgumentException("参数错误:programType不能为空。");
            }
            int programType = jsonReq.getInt("programType");
            if(programType != 4 && programType != 1) {
                throw new IllegalArgumentException("参数错误:programType值域只能为1或4。");
            }

            TipsSelector selector = new TipsSelector();
            JSONObject jsonObject = selector.listInfoTipsByPage(parameter);

            response.getWriter().println(
                    ResponseUtils.assembleRegularResult(jsonObject));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            response.getWriter().println(
                    ResponseUtils.assembleFailResult(e.getMessage()));
        }
    }

    @RequestMapping(value = "/tip/noTaskToMidTask")
    public void noTaskToMidTask(HttpServletRequest request,
                                HttpServletResponse response) throws ServletException, IOException {
        String parameter = request.getParameter("parameter");
        logger.info("noTaskToMidTask:" + parameter);

        Connection oracleConn = null;
        try {
            oracleConn = DBConnector.getInstance().getTipsIdxConnection();

            JSONObject jsonReq = JSONObject.fromObject(parameter);

            if(!jsonReq.containsKey("taskId")) {
                throw new IllegalArgumentException("参数错误:midTaskId不能为空。");
            }

            int midTaskId = jsonReq.getInt("taskId");
            if(midTaskId == 0) {
                throw new IllegalArgumentException("参数错误:midTaskId不能为空。");
            }

            ManApi manApi = (ManApi) ApplicationContextUtil.getBean("manApi");
            JSONArray gridList = manApi.getGridIdsByTaskId(midTaskId);
            String wkt = GridUtils.grids2Wkt(gridList);
            TipsOperator tipsOperator = new TipsOperator();
            long totalNum = tipsOperator.batchNoTaskDataByMidTask(wkt, midTaskId);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("total", totalNum);

            response.getWriter().println(ResponseUtils.assembleRegularResult(jsonObject));
            logger.info("noTaskToMidTask:" + totalNum);
        } catch (Exception e) {
            DBUtils.rollBack(oracleConn);
            logger.error(e.getMessage(), e);
            response.getWriter().println(
                    ResponseUtils.assembleFailResult(e.getMessage()));
        } finally {
            DBUtils.closeConnection(oracleConn);
        }
    }

    @RequestMapping(value="/tip/exportGps")
    public ModelAndView getGpsAndDeleteLinkTips(HttpServletRequest request) throws ServletException, IOException {
    	  String parameter = request.getParameter("parameter");

          try {

              if (StringUtils.isEmpty(parameter)) {
                  throw new IllegalArgumentException("parameter参数不能为空。");
              }

              JSONObject jsonReq = JSONObject.fromObject(parameter);
              
              int subTaskId = jsonReq.getInt("subtaskId");
              
              String beginTime = jsonReq.getString("beginTime");
              
              String endTime = jsonReq.getString("endTime");
              
              if(beginTime == null || beginTime.isEmpty() || endTime == null || endTime.isEmpty()){
            	  throw new IllegalArgumentException("参数错误:起止时间不能为空。");
              }
              
              int pageSize = jsonReq.getInt("pageSize");
              
              int curPage = jsonReq.getInt("pageNum");
              
              if(pageSize == 0 || curPage == 0){
            	  throw new IllegalArgumentException("参数错误:分页数据不能为0");
              }

              TipsSelector selector = new TipsSelector();

			JSONObject array = selector.searchGpsAndDeleteLinkTips(subTaskId, beginTime, endTime, pageSize, curPage,
					jsonReq);
              
              return new ModelAndView("jsonView", success(array));

          } catch (Exception e) {

              logger.error(e.getMessage(), e);

              return new ModelAndView("jsonView", fail(e.getMessage()));
          }
    }
    
    @RequestMapping(value = "/tip/poiRelateTips")
    public ModelAndView getPoiRelateTips(HttpServletRequest request) throws ServletException, IOException{
    	 String parameter = request.getParameter("parameter");

         try {

             if (StringUtils.isEmpty(parameter)) {
                 throw new IllegalArgumentException("parameter参数不能为空。");
             }

             JSONObject jsonReq = JSONObject.fromObject(parameter);
             
             int subTaskId = jsonReq.getInt("subtaskId");
             
             String id = jsonReq.getString("id");
             
             int buffer = jsonReq.getInt("buffer");
             
             int dbId = jsonReq.getInt("dbId");
             
             int programType = jsonReq.getInt("programType");
             
			if (id.isEmpty() || buffer == 0 || subTaskId == 0 || programType == 0) {
				throw new IllegalArgumentException("参数错误");
			}

             TipsSelector selector = new TipsSelector();

			JSONArray array = selector.searchPoiRelateTips(id, subTaskId, buffer, dbId, programType);
             
             return new ModelAndView("jsonView", success(array));

         } catch (Exception e) {

             logger.error(e.getMessage(), e);

             return new ModelAndView("jsonView", fail(e.getMessage()));
         }
    }
}