package com.navinfo.dataservice.web.metadata.controller;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;
import org.apache.uima.pear.util.FileUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import com.navinfo.dataservice.api.fcc.iface.FccApi;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfig;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.commons.util.ResponseUtils;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.ZipUtils;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdmin;
import com.navinfo.dataservice.dao.glm.search.AdAdminSearch;
import com.navinfo.dataservice.engine.meta.area.ScPointAdminArea;
import com.navinfo.dataservice.engine.meta.chain.ChainSelector;
import com.navinfo.dataservice.engine.meta.chain.FocusSelector;
import com.navinfo.dataservice.engine.meta.kindcode.KindCodeSelector;
import com.navinfo.dataservice.engine.meta.level.LevelSelector;
import com.navinfo.dataservice.engine.meta.mesh.MeshSelector;
import com.navinfo.dataservice.engine.meta.patternimage.PatternImageExporter;
import com.navinfo.dataservice.engine.meta.patternimage.PatternImageSelector;
import com.navinfo.dataservice.engine.meta.pinyin.PinyinConverter;
import com.navinfo.dataservice.engine.meta.rdname.RdNameImportor;
import com.navinfo.dataservice.engine.meta.rdname.RdNameOperation;
import com.navinfo.dataservice.engine.meta.rdname.RdNameSelector;
import com.navinfo.dataservice.engine.meta.rdname.ScRoadnameTypename;
import com.navinfo.dataservice.engine.meta.service.ScPointPoicodeNewService;
import com.navinfo.dataservice.engine.meta.svg.SvgImageSelector;
import com.navinfo.dataservice.engine.meta.tmc.model.TmcLine;
import com.navinfo.dataservice.engine.meta.tmc.model.TmcLineTree;
import com.navinfo.dataservice.engine.meta.tmc.model.TmcPoint;
import com.navinfo.dataservice.engine.meta.tmc.selector.TmcLineSelector;
import com.navinfo.dataservice.engine.meta.tmc.selector.TmcPointSelector;
import com.navinfo.dataservice.engine.meta.tmc.selector.TmcSelector;
import com.navinfo.dataservice.engine.meta.translates.EnglishConvert;
import com.navinfo.dataservice.engine.meta.truck.TruckSelector;
import com.navinfo.dataservice.engine.meta.workitem.Workitem;
import com.navinfo.navicommons.exception.ServiceException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
public class MetaController extends BaseController {
    private static final Logger logger = Logger.getLogger(MetaController.class);

    @RequestMapping(value = "/rdname/search")
    public ModelAndView searchRdName(HttpServletRequest request)
            throws ServletException, IOException {

        String parameter = request.getParameter("parameter");

        try {
            JSONObject jsonReq = JSONObject.fromObject(parameter);

            String name = jsonReq.getString("name");

            int pageSize = jsonReq.getInt("pageSize");

            int pageNum = jsonReq.getInt("pageNum");

            RdNameSelector selector = new RdNameSelector();

            JSONObject data = selector.searchByName(name, pageSize, pageNum);

            return new ModelAndView("jsonView", success(data));

        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            return new ModelAndView("jsonView", fail(e.getMessage()));
        }
    }

    @RequestMapping(value = "/pinyin/convert")
    public ModelAndView convertPinyin(HttpServletRequest request)
            throws ServletException, IOException {

        String parameter = request.getParameter("parameter");

        try {
            JSONObject jsonReq = JSONObject.fromObject(parameter);

            String word = jsonReq.getString("word");

            String oldWord = word;
            
            String adminId = null;
            if(jsonReq.containsKey("adminId")){
            	adminId = jsonReq.getString("adminId");
            }
            PinyinConverter py = new PinyinConverter();

            //特殊处理  以G、S、Y、X、C、Z开头的词(分歧编辑中)
            if(jsonReq.containsKey("flag") && jsonReq.getInt("flag") == 1 ){
            	word = py.wordConvert(word, adminId);
            }
            
//          String[] result = py.convert(word);
            String[] result = py.pyVoiceConvert(word, null, adminId, null);

            if (result != null) {
                JSONObject json = new JSONObject();

                if(jsonReq.containsKey("flag") && jsonReq.getInt("flag") == 1 ){
                	json.put("phonetic", py.pyConvert(oldWord, adminId, null));
                }else{
                	json.put("phonetic", result[0]);
                }

                json.put("voicefile", result[1]);

                return new ModelAndView("jsonView", success(json));
            } else {
                throw new Exception("转拼音失败");
            }

        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            return new ModelAndView("jsonView", fail(e.getMessage()));
        }
    }
    
    /**
     * @Title: pyPolyphoneConvert
     * @Description: 含多音字的转拼音接口
     * @param request
     * @return
     * @throws ServletException
     * @throws IOException  ModelAndView
     * @throws 
     * @author zl zhangli5174@navinfo.com
     * @date 2017年5月24日 下午3:17:26 
     */
    @RequestMapping(value = "/pinyin/pyPolyphoneConvert")
    public ModelAndView pyPolyphoneConvert(HttpServletRequest request)
            throws ServletException, IOException {

        String parameter = request.getParameter("parameter");

        try {
            JSONObject jsonReq = JSONObject.fromObject(parameter);

            String word = jsonReq.getString("word");
            
            String newWord = word;

            String adminId = null;
            if(jsonReq.containsKey("adminId")){
            	adminId = jsonReq.getString("adminId");
            }
            PinyinConverter py = new PinyinConverter();

//          String[] result = py.pyVoiceConvert(word, null, adminId, null);
            
            String result = py.pyPolyphoneConvert(word, adminId);
            String voiceStr = py.voiceConvert(word, null, adminId, null);

            //特殊处理  以G、S、Y、X、C、Z开头的词(分歧编辑中)
            int flag = jsonReq.containsKey("flag")?jsonReq.getInt("flag"):0;
            int code_type = jsonReq.containsKey("code_type")?jsonReq.getInt("code_type"):0;
            if(flag == 1 ){
            	logger.info("word : "+word+";voiceStr : "+voiceStr+";flag: "+flag+" ; code_type: "+code_type);
            	if( code_type == 5 || code_type == 6 || 
                		code_type == 7 || code_type == 8 || code_type == 9){
            		newWord = py.wordConvert(word, adminId);
                	logger.info("newWord : "+newWord);
                	voiceStr = py.voiceConvert(newWord, null, adminId, null);
                	logger.info("newWord voiceStr : "+voiceStr);
            	}
            		logger.info("voiceStr: "+voiceStr);
            		voiceStr = voiceStr.replace("gaosugonglu", "");
                	logger.info("new voiceStr : "+voiceStr);
                	voiceStr = voiceStr.endsWith("'")?voiceStr.substring(0, voiceStr.length() - 1):voiceStr;
                	logger.info("1 voiceStr :"+voiceStr);
            }
            
            if (result != null) {
                JSONObject json = new JSONObject();

                json.put("phonetic", result);
                
                json.put("voicefile", voiceStr);

                return new ModelAndView("jsonView", success(json));
            } else {
                throw new Exception("转拼音失败");
            }
        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            return new ModelAndView("jsonView", fail(e.getMessage()));
        }
    }
    /**
     * 转语音接口
     * @param request
     * @return
     * @throws ServletException
     * @throws IOException
     */
    @RequestMapping(value = "/pinyin/voiceConvert")
    public ModelAndView convertVoice(HttpServletRequest request)
            throws ServletException, IOException {

        String parameter = request.getParameter("parameter");

        try {
            JSONObject jsonReq = JSONObject.fromObject(parameter);

            String word = jsonReq.getString("word");

            String adminId = null;
            if(jsonReq.containsKey("adminId")){
            	adminId = jsonReq.getString("adminId");
            }
            String phonetic = null;
            if(jsonReq.containsKey("phonetic")){
            	phonetic = jsonReq.getString("phonetic");
            }
            PinyinConverter py = new PinyinConverter();
            
            String result = py.voiceConvert(word, phonetic, adminId, null);

            if (result != null) {
                JSONObject json = new JSONObject();

                json.put("voicefile", result);

                return new ModelAndView("jsonView", success(json));
            } else {
                throw new Exception("转语音失败");
            }

        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            return new ModelAndView("jsonView", fail(e.getMessage()));
        }
    }
    /**
     * @Title: autoConvertPinyin
     * @Description: 自动生成语音及拼音
     * 备注: 和道路名保存生成语音及拼音规则是一致的
     * @param request
     * @return
     * @throws ServletException
     * @throws IOException  ModelAndView
     * @throws 
     * @author zl zhangli5174@navinfo.com
     * @date 2017年4月21日 下午3:12:36 
     */
    @RequestMapping(value = "/pinyin/autoConvert")
    public ModelAndView autoConvertPinyin(HttpServletRequest request)
            throws ServletException, IOException {

        String parameter = request.getParameter("parameter");

        try {
            JSONObject jsonReq = JSONObject.fromObject(parameter);

            String word = jsonReq.getString("word");
            
            String adminId = null;
            if(jsonReq.containsKey("adminId")){
            	adminId = jsonReq.getString("adminId");
            }

            PinyinConverter py = new PinyinConverter();

            String[] result = py.pyVoiceConvert(word, null, adminId, null);

            if (result != null) {
                JSONObject json = new JSONObject();

                json.put("voicefile", result[1]);

                json.put("phonetic", result[0]);

                return new ModelAndView("jsonView", success(json));
            } else {
                throw new Exception("转拼音失败");
            }

        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            return new ModelAndView("jsonView", fail(e.getMessage()));
        }
    }

    @RequestMapping(value = "/eng/convert")
    public ModelAndView convertEng(HttpServletRequest request) throws ServletException, IOException {
        String parameter = request.getParameter("parameter");
        try {
            JSONObject jsonReq = JSONObject.fromObject(parameter);
            String languageType = jsonReq.getString("languageType");
            String word = jsonReq.getString("word");
            String pinyin = jsonReq.optString("pinyin", "");
            EnglishConvert englishConvert = new EnglishConvert();
            String result = englishConvert.convert(word, pinyin);
            if (result != null) {
                JSONObject json = new JSONObject();
                json.put("eng", result);
                return new ModelAndView("jsonView", success(json));
            } else {
                throw new Exception("转英文名称失败...");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new ModelAndView("jsonView", fail(e.getMessage()));
        }
    }

    @RequestMapping(value = "/province/getByLocation")
    public ModelAndView getProvinceByLocation(HttpServletRequest request)
            throws ServletException, IOException {

        String parameter = request.getParameter("parameter");

        try {
            JSONObject jsonReq = JSONObject.fromObject(parameter);

            double lon = jsonReq.getDouble("lon");

            double lat = jsonReq.getDouble("lat");

            MeshSelector selector = new MeshSelector();

            JSONObject data = selector.getProvinceByLocation(lon, lat);

            if (data != null) {
                return new ModelAndView("jsonView", success(data));
            } else {
                throw new Exception("不在中国省市范围内");
            }

        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            return new ModelAndView("jsonView", fail(e.getMessage()));
        }
    }

    @RequestMapping(value = "/patternImage/checkUpdate")
    public ModelAndView checkPatternImage(HttpServletRequest request)
            throws ServletException, IOException {

        String parameter = request.getParameter("parameter");

        try {
            JSONObject jsonReq = JSONObject.fromObject(parameter);

            String date = jsonReq.getString("date");

            PatternImageSelector selector = new PatternImageSelector();

            boolean flag = selector.checkUpdate(date);

            return new ModelAndView("jsonView", success(flag));

        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            return new ModelAndView("jsonView", fail(e.getMessage()));
        }
    }

    @RequestMapping(value = "/patternImage/download")
    public ModelAndView exportPatternImage(HttpServletRequest request)
            throws ServletException, IOException {

        String parameter = request.getParameter("parameter");

        try {
            JSONObject jsonReq = JSONObject.fromObject(parameter);

            PatternImageExporter exporter = new PatternImageExporter();

            SystemConfig config = SystemConfigFactory.getSystemConfig();

            String url = config.getValue(PropConstant.serverUrl);

            url += config.getValue(PropConstant.downloadUrlPathPatternimg);

            String path = config
                    .getValue(PropConstant.downloadFilePathPatternimg);

            String dir = null;

            String currentDate = StringUtils.getCurrentTime();

            String zipFileName = currentDate + ".zip";

            if (jsonReq.containsKey("names")) {
                JSONArray names = jsonReq.getJSONArray("names");

                path += "/byname";

                dir = path + "/" + currentDate;

                Set<String> set = new HashSet<String>();

                for (int i = 0; i < names.size(); i++) {
                    set.add(names.getString(i));
                }

                exporter.export2SqliteByNames(dir, set);

                url += "/byname/" + zipFileName;
            } else if (jsonReq.containsKey("date")) {
                String date = jsonReq.getString("date");
                if(date != null  && StringUtils.isNotEmpty(date)){
                	path += "/bydate";

                    dir = path + "/" + currentDate;

                    exporter.export2SqliteByDate(dir, date);

                    url += "/bydate/" + zipFileName;
                }else{
                	//在 date没有值的情况下,返回全量模式图zip包路径
//                	path += "/init";
            		File fileInit = new File(path);
            		
            		File[] fileInits = fileInit.listFiles();
            		
            		long version = 0;
            		
            		for(File f:fileInits){
            			
            			if(!f.isFile()){
            				continue;
            			}
            			
            			String name = f.getName();
            			
            			int index= name.indexOf(".");
            			
            			if(index==-1){
            				continue;
            			}
            			long tmpVersion = Long.parseLong(name.substring(0, index));
            			
            			if (tmpVersion > version){
            				version = tmpVersion;
            			}
            		}
            		zipFileName = version+".zip";
            		File lastestFile = new File(path+"/"+version+".zip");
            		
                	url += "/" + zipFileName;
    	        	 long filesize = lastestFile.length();
    	
    	             String versionStr = zipFileName.replace(".zip", "");
    	
    	             JSONObject json = new JSONObject();
    	
    	             ManApi man = (ManApi) ApplicationContextUtil.getBean("manApi");
    	
    	             String specVersion = man.querySpecVersionByType(3);
    	
    	             json.put("version", versionStr);
    	
    	             json.put("url", url);
    	
    	             json.put("filesize", filesize);
    	
    	             json.put("specVersion", specVersion);

                     return new ModelAndView("jsonView", success(json));
                }
            } else {
               throw new Exception("错误的参数");
            }

            ZipUtils.zipFile(dir, path + "/" + currentDate + ".zip");

            FileUtil.deleteDirectory(new File(dir));

            String fullPath = path + "/" + zipFileName;

            File f = new File(fullPath);

            long filesize = f.length();

            String version = zipFileName.replace(".zip", "");

            JSONObject json = new JSONObject();

            ManApi man = (ManApi) ApplicationContextUtil.getBean("manApi");

            String specVersion = man.querySpecVersionByType(3);

            json.put("version", version);

            json.put("url", url);

            json.put("filesize", filesize);

            json.put("specVersion", specVersion);

            return new ModelAndView("jsonView", success(json));

        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            return new ModelAndView("jsonView", fail(e.getMessage()));
        }
    }

    @RequestMapping(value = "/patternImage/getById")
    public void getPatternImageById(HttpServletRequest request,
                                    HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("image/jpeg;charset=GBK");

        String parameter = request.getParameter("parameter");

        try {
            JSONObject jsonReq = JSONObject.fromObject(parameter);

            String id = jsonReq.getString("id");

            PatternImageSelector selector = new PatternImageSelector();

            byte[] data = selector.getById(id);

            if (data.length == 0) {
                throw new Exception("id值不存在");
            }

            response.getOutputStream().write(data);

        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            response.getWriter().println(
                    ResponseUtils.assembleFailResult(e.getMessage()));
        }
    }
    
    @RequestMapping(value = "/svgImage/getById")
    public void getSvgImageById(HttpServletRequest request,
                                    HttpServletResponse response) throws ServletException, IOException {

        String parameter = request.getParameter("parameter");

        try {
            JSONObject jsonReq = JSONObject.fromObject(parameter);

            String id = jsonReq.getString("id");

            SvgImageSelector selector = new SvgImageSelector();

            String data = selector.getById(id);

            if (data.length() == 0) {
                throw new Exception("id值不存在");
            }
            else
            {
            	response.getOutputStream().write(data.getBytes());
            }
        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            response.getWriter().println(
                    ResponseUtils.assembleFailResult(e.getMessage()));
        }
    }

    @RequestMapping(value = "/patternImage/search")
    public ModelAndView searchPatternImage(HttpServletRequest request)
            throws ServletException, IOException {

        String parameter = request.getParameter("parameter");

        try {
            JSONObject jsonReq = JSONObject.fromObject(parameter);

            String name = jsonReq.getString("name");

            int pageSize = jsonReq.getInt("pageSize");

            int pageNum = jsonReq.getInt("pageNum");

            PatternImageSelector selector = new PatternImageSelector();

            JSONObject data = selector.searchByName(name, pageSize, pageNum);

            return new ModelAndView("jsonView", success(data));

        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            return new ModelAndView("jsonView", fail(e.getMessage()));
        }
    }
    
    @RequestMapping(value = "/svgImage/search")
    public ModelAndView searchSvgImage(HttpServletRequest request)
            throws ServletException, IOException {

        String parameter = request.getParameter("parameter");

        try {
            JSONObject jsonReq = JSONObject.fromObject(parameter);

            String name = jsonReq.getString("name");

            int pageSize = jsonReq.getInt("pageSize");

            int pageNum = jsonReq.getInt("pageNum");

            SvgImageSelector selector = new SvgImageSelector();

            JSONObject data = selector.searchByName(name, pageSize, pageNum);
            
            return new ModelAndView("jsonView", success(data));

        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            return new ModelAndView("jsonView", fail(e.getMessage()));
        }
    }

    @RequestMapping(value = "/queryTelByProvince")
    public ModelAndView searchTelByProvince(HttpServletRequest request)
            throws ServletException, IOException {

        String parameter = request.getParameter("parameter");

        try {
            JSONObject jsonReq = JSONObject.fromObject(parameter);

            String name = jsonReq.getString("province");

            ScPointAdminArea selector = new ScPointAdminArea();

            JSONArray data = selector.searchByProvince(name);

            return new ModelAndView("jsonView", success(data));

        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            return new ModelAndView("jsonView", fail(e.getMessage()));
        }
    }

    @RequestMapping(value = "/queryChain")
    public ModelAndView queryChain(HttpServletRequest request)
            throws ServletException, IOException {

        String parameter = request.getParameter("parameter");

        try {
            JSONObject jsonReq = JSONObject.fromObject(parameter);

            String kindCode = null;

            if (jsonReq.has("kindCode")) {
                kindCode = jsonReq.getString("kindCode");
            }

            ChainSelector selector = new ChainSelector();

            JSONObject data = selector.getChainByKindCode(kindCode);

            if (kindCode != null && data.has(kindCode)) {
                JSONArray array = data.getJSONArray(kindCode);

                return new ModelAndView("jsonView", success(array));
            }

            return new ModelAndView("jsonView", success(data));

        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            return new ModelAndView("jsonView", fail(e.getMessage()));
        }
    }

    @RequestMapping(value = "/chainLevel")
    public ModelAndView queryChainLevel(HttpServletRequest request)
            throws ServletException, IOException {

        String parameter = request.getParameter("parameter");

        try {
            JSONObject jsonReq = JSONObject.fromObject(parameter);

            String kindCode = jsonReq.getString("kindCode");

            String chainCode = jsonReq.getString("chainCode");

            ChainSelector selector = new ChainSelector();

            String data = selector.getLevelByChain(chainCode, kindCode);

            return new ModelAndView("jsonView", success(data));

        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            return new ModelAndView("jsonView", fail(e.getMessage()));
        }
    }

    @RequestMapping(value = "/queryFocus")
    public ModelAndView queryFocus(HttpServletRequest request)
            throws ServletException, IOException {

        try {

            FocusSelector selector = new FocusSelector();

            JSONArray data = selector.getPoiNum();

            return new ModelAndView("jsonView", success(data));

        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            return new ModelAndView("jsonView", fail(e.getMessage()));
        }
    }

    @RequestMapping(value = "/queryTelLength")
    public ModelAndView searchTelLength(HttpServletRequest request)
            throws ServletException, IOException {

        String parameter = request.getParameter("parameter");

        try {
            JSONObject jsonReq = JSONObject.fromObject(parameter);

            String code = jsonReq.getString("code");

            ScPointAdminArea selector = new ScPointAdminArea();

            String data = selector.searchTelLength(code);

            return new ModelAndView("jsonView", success(data));

        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            return new ModelAndView("jsonView", fail(e.getMessage()));
        }
    }

    @RequestMapping(value = "/queryFoodType")
    public ModelAndView searchFoodType(HttpServletRequest request)
            throws ServletException, IOException {

        String parameter = request.getParameter("parameter");

        try {
            JSONObject jsonReq = JSONObject.fromObject(parameter);

            String kindId = jsonReq.getString("kindId");

            ScPointAdminArea selector = new ScPointAdminArea();

            JSONArray data = selector.searchFoodType(kindId);

            return new ModelAndView("jsonView", success(data));

        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            return new ModelAndView("jsonView", fail(e.getMessage()));
        }
    }

    @RequestMapping(value = "/kindLevel")
    public ModelAndView searchKindLevel(HttpServletRequest request)
            throws ServletException, IOException {

        String parameter = request.getParameter("parameter");

        try {
            JSONObject jsonReq = JSONObject.fromObject(parameter);

            String kindId = jsonReq.getString("kindCode");

            KindCodeSelector selector = new KindCodeSelector();

            JSONObject data = selector.searchkindLevel(kindId);

            return new ModelAndView("jsonView", success(data));

        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            return new ModelAndView("jsonView", fail(e.getMessage()));
        }
    }

    @RequestMapping(value = "/queryTopKind")
    public ModelAndView QueryTopKind(HttpServletRequest request)
            throws ServletException, IOException {

        try {
            KindCodeSelector selector = new KindCodeSelector();

            JSONArray data = selector.queryTopKindInfo();

            return new ModelAndView("jsonView", success(data));

        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            return new ModelAndView("jsonView", fail(e.getMessage()));
        }
    }

    @RequestMapping(value = "/queryMediumKind")
    public ModelAndView queryMediumKind(HttpServletRequest request)
            throws ServletException, IOException {

        String parameter = request.getParameter("parameter");

        try {
            JSONObject jsonReq = JSONObject.fromObject(parameter);

            String topId = jsonReq.getString("topId");

            KindCodeSelector selector = new KindCodeSelector();

            JSONArray data = selector.queryMediumKindInfo(topId);

            return new ModelAndView("jsonView", success(data));

        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            return new ModelAndView("jsonView", fail(e.getMessage()));
        }
    }

    @RequestMapping(value = "/queryKind")
    public ModelAndView queryKind(HttpServletRequest request)
            throws ServletException, IOException {

        String parameter = request.getParameter("parameter");

        try {
            JSONObject jsonReq = JSONObject.fromObject(parameter);

            int region = jsonReq.getInt("region");

            KindCodeSelector selector = new KindCodeSelector();

            JSONArray data = selector.queryKindInfo(region);

            return new ModelAndView("jsonView", success(data));

        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            return new ModelAndView("jsonView", fail(e.getMessage()));
        }
    }


    /**
     * @param request
     * @return
     * @throws ServletException
     * @throws IOException      ModelAndView
     * @throws
     * @Title: searchRdNameForWeb
     * @Description: 根据任务的grid，查找对应任务grid范围内的rd_name
     * @author zl zhangli5174@navinfo.com
     * @date 2016年11月14日 下午7:58:19
     */
    @RequestMapping(value = "/rdname/websearch")
    public ModelAndView searchRdNameForWeb(HttpServletRequest request)
            throws ServletException, IOException {
        String parameter = request.getParameter("parameter");
        try {
            JSONObject jsonReq = JSONObject.fromObject(parameter);

            RdNameSelector selector = new RdNameSelector();

            int flag = jsonReq.getInt("flag");//1是任务查，0是全库查
            JSONObject data = new JSONObject();
            if(flag > 0){
            	int subtaskId = jsonReq.getInt("subtaskId");
                logger.info("subtaskId: "+subtaskId);
                ManApi apiService = (ManApi) ApplicationContextUtil.getBean("manApi");
                Subtask subtask = apiService.queryBySubtaskId(subtaskId);

                if (subtask == null) {
                    throw new Exception("subtaskid未找到数据");
                }

//    			int dbId = subtask.getDbId();

                FccApi apiFcc = (FccApi) ApplicationContextUtil.getBean("fccApi");
                JSONArray tips = apiFcc.searchDataBySpatial(subtask.getGeometry(), subtaskId, 1901, new JSONArray());

                logger.info("tips: "+tips);
                 data = selector.searchForWeb(jsonReq, tips);
            }else{
            	logger.info("全库范围内查询! ");
            	data = selector.searchForWeb(jsonReq);
            }

            return new ModelAndView("jsonView", success(data));

        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            return new ModelAndView("jsonView", fail(e.getMessage()));
        }
    }

    /**
     * @param request
     * @return
     * @throws ServletException
     * @throws IOException      ModelAndView
     * @throws
     * @Title: searchRdNameForWeb
     * @Description: 根据name_id，查询rd_name
     * @author zl zhangli5174@navinfo.com
     * @date 2016年11月14日 下午7:58:19
     */
    @RequestMapping(value = "/rdname/getByNameId")
    public ModelAndView searchByNameId(HttpServletRequest request)
            throws ServletException, IOException {
        String parameter = request.getParameter("parameter");
        try {
            JSONObject jsonReq = JSONObject.fromObject(parameter);

            RdNameSelector selector = new RdNameSelector();

            String nameId = jsonReq.getString("nameId");


            JSONObject data = selector.searchForWebByNameId(nameId);
            if (data != null) {
                return new ModelAndView("jsonView", success(data));
            } else {
                return new ModelAndView("jsonView", fail("无数据"));
            }

        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            return new ModelAndView("jsonView", fail(e.getMessage()));
        }
    }


    /**
     * @param request
     * @return
     * @throws ServletException
     * @throws IOException      ModelAndView
     * @throws
     * @Title: webSave
     * @Description: 增加参数 subtaskId
     * @author zl zhangli5174@navinfo.com
     * @date 2016年11月14日 下午6:15:21
     */
    @RequestMapping(value = "/rdname/websave")
    public ModelAndView webSave(HttpServletRequest request)
            throws ServletException, IOException {
        String parameter = request.getParameter("parameter");
        long userId = 0;
        try {
        	AccessToken tokenObj = (AccessToken) request.getAttribute("token");
			userId = tokenObj.getUserId();
			
            JSONObject jsonReq = JSONObject.fromObject(parameter);

            JSONObject data = jsonReq.getJSONObject("data");

            int subtaskId = 0 ;
            if(jsonReq.containsKey("subtaskId")){
            	subtaskId = jsonReq.getInt("subtaskId");
            }

//			int dbId = jsonReq.getInt("dbId");

            RdNameImportor importor = new RdNameImportor();

            JSONObject result = importor.importRdNameFromWeb(data, subtaskId,userId);

            return new ModelAndView("jsonView", success(result));
        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            return new ModelAndView("jsonView", fail(e.getMessage()));
        }
    }

    /**
     * @param request
     * @return
     * @throws ServletException
     * @throws IOException      ModelAndView
     * @throws
     * @Title: webResultUpdate
     * @Description: 修改某条道路名检查结果的状态
     * @author zl zhangli5174@navinfo.com
     * @date 2016年11月17日 下午8:32:40
     */
    @RequestMapping(value = "/rdname/webResultUpdate")
    public void webResultUpdate(HttpServletRequest request)
            throws ServletException, IOException {
        String parameter = request.getParameter("parameter");
        try {
            JSONObject jsonReq = JSONObject.fromObject(parameter);
            RdNameSelector selector = new RdNameSelector();
            int valExceptionId = jsonReq.getInt("valExceptionId");
            int qaStatus = jsonReq.getInt("qaStatus"); //1:以质检  2:未质检

            selector.udateResultStatusById(valExceptionId, qaStatus);

        } catch (Exception e) {

            logger.error(e.getMessage(), e);

        }
    }


    /**
     * @param request
     * @return
     * @throws ServletException
     * @throws IOException      ModelAndView
     * @throws
     * @Title: webTeilen
     * @Description: 道路名拆分(修)(第七迭代)  增加铁路类的英文拆分
     * @author zl zhangli5174@navinfo.com
     * @date 2016年11月18日 下午7:58:29
     */
    @RequestMapping(value = "/rdname/webteilen")
    public ModelAndView webTeilen(HttpServletRequest request)
            throws ServletException, IOException {
        String parameter = request.getParameter("parameter");

        Connection conn = null;

        try {
            JSONObject jsonReq = JSONObject.fromObject(parameter);

            int flag = jsonReq.getInt("flag");

            conn = DBConnector.getInstance().getMetaConnection();

            RdNameOperation operation = new RdNameOperation(conn);

            if (flag > 0) {//拆分指定数据
                JSONArray dataList = jsonReq.getJSONArray("data");

                operation.teilenRdName(dataList);
            } else {//拆分整个子任务数据
                int subtaskId = jsonReq.getInt("subtaskId");

                ManApi apiService = (ManApi) ApplicationContextUtil.getBean("manApi");

                Subtask subtask = apiService.queryBySubtaskId(subtaskId);

                FccApi apiFcc = (FccApi) ApplicationContextUtil.getBean("fccApi");

                JSONArray tips = apiFcc.searchDataBySpatial(subtask.getGeometry(),subtaskId, 1901, new JSONArray());

                operation.teilenRdNameByTask(tips);
            }

            return new ModelAndView("jsonView", success());
        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            return new ModelAndView("jsonView", fail(e.getMessage()));
        } finally {
            DbUtils.commitAndCloseQuietly(conn);
        }
    }

    /**
     * 获取nametype
     *
     * @param request
     * @return
     * @throws ServletException
     * @throws IOException
     */
    @RequestMapping(value = "/rdname/nametype")
    public ModelAndView webNameType(HttpServletRequest request)
            throws ServletException, IOException {
        String parameter = request.getParameter("parameter");
        try {
            JSONObject jsonReq = JSONObject.fromObject(parameter);

            int pageSize = jsonReq.getInt("pageSize");
            int pageNum = jsonReq.getInt("pageNum");

            String name = "";
            if (jsonReq.containsKey("name")) {
                name = jsonReq.getString("name");
            }

            String sortby = "";
            if (jsonReq.containsKey("sortby")) {
                sortby = jsonReq.getString("sortby");
            }

            ScRoadnameTypename typename = new ScRoadnameTypename();

            JSONObject data = typename.getNameType(pageNum, pageSize, name, sortby);

            return new ModelAndView("jsonView", success(data));
        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            return new ModelAndView("jsonView", fail(e.getMessage()));
        }
    }

    /**
     * 获取行政区划
     *
     * @param request
     * @return
     * @throws ServletException
     * @throws IOException
     */
    @RequestMapping(value = "/rdname/adminarea")
    public ModelAndView webAdminarea(HttpServletRequest request)
            throws ServletException, IOException {
        String parameter = request.getParameter("parameter");
        try {
            JSONObject jsonReq = JSONObject.fromObject(parameter);

            int pageSize = jsonReq.getInt("pageSize");
            int pageNum = jsonReq.getInt("pageNum");

            String name = "";
            if (jsonReq.containsKey("name")) {
                name = jsonReq.getString("name");
            }

            String sortby = "";
            if (jsonReq.containsKey("sortby")) {
                sortby = jsonReq.getString("sortby");
            }

            ScPointAdminArea adminarea = new ScPointAdminArea();

            JSONObject data = adminarea.getAdminArea(pageSize, pageNum, name, sortby);

            return new ModelAndView("jsonView", success(data));
        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            return new ModelAndView("jsonView", fail(e.getMessage()));
        }
    }

    /**
     * 查询该组下是否存在英文/葡文名
     *
     * @param request
     * @return
     * @throws ServletException
     * @throws IOException
     */
    @RequestMapping(value = "/rdname/group")
    public ModelAndView webEngGroup(HttpServletRequest request)
            throws ServletException, IOException {
        String parameter = request.getParameter("parameter");

        Connection conn = null;
        try {
            JSONObject jsonReq = JSONObject.fromObject(parameter);

            int nameGroupid = jsonReq.getInt("nameGroupid");

//			int dbId = jsonReq.getInt("dbId");

            conn = DBConnector.getInstance().getMetaConnection();

            RdNameOperation operation = new RdNameOperation(conn);

            boolean result = operation.checkEngName(nameGroupid);

            return new ModelAndView("jsonView", success(result));
        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            return new ModelAndView("jsonView", fail(e.getMessage()));
        } finally {
            DbUtils.closeQuietly(conn);
        }
    }

    @RequestMapping(value = "/deep/workitem")
    public ModelAndView getWorkItemMap(HttpServletRequest request)
            throws ServletException, IOException {
        String parameter = request.getParameter("parameter");

        try {
            JSONObject jsonReq = JSONObject.fromObject(parameter);

            int type = 0;
            if (jsonReq.containsKey("type")) {
                type = jsonReq.getInt("type");
            }

            Workitem workitem = new Workitem();

            JSONArray result = workitem.getDataMap(type);

            return new ModelAndView("jsonView", success(result));
        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            return new ModelAndView("jsonView", fail(e.getMessage()));
        }
    }

    @RequestMapping(value = "/queryTruck")
    public ModelAndView queryTruck(HttpServletRequest request)
            throws ServletException, IOException {

        String parameter = request.getParameter("parameter");
        Connection conn = null;
        try {
            JSONObject jsonReq = JSONObject.fromObject(parameter);

            String kind = jsonReq.getString("kindCode");
            String chain = jsonReq.getString("chain");
            String fuelType = jsonReq.getString("fuelType");

            conn = DBConnector.getInstance().getMetaConnection();
            TruckSelector selector = new TruckSelector(conn);

            int truck = selector.getTruck(kind, chain, fuelType);

            return new ModelAndView("jsonView", success(truck));

        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            return new ModelAndView("jsonView", fail(e.getMessage()));
        } finally {
            DbUtils.closeQuietly(conn);
        }
    }
    
    /**
     * @param request
     * @return
     * @throws ServletException
     * @throws IOException
     * 查询level接口
     */
    @RequestMapping(value = "/queryLevel")
    public ModelAndView queryLevel(HttpServletRequest request)
            throws ServletException, IOException {

        String parameter = request.getParameter("parameter");
        Connection conn = null;
        try {
            JSONObject jsonReq = JSONObject.fromObject(parameter);

            conn = DBConnector.getInstance().getMetaConnection();
            LevelSelector selector = new LevelSelector(conn);

			JSONObject res = selector.getLevel(jsonReq);

            return new ModelAndView("jsonView", success(res));

        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            return new ModelAndView("jsonView", fail(e.getMessage()));
        } finally {
            DbUtils.closeQuietly(conn);
        }
    }

    @RequestMapping(value = "/queryTmcTreeByIds")
    public ModelAndView queryTmcByIds(HttpServletRequest request)
            throws ServletException, IOException {

        String parameter = request.getParameter("parameter");
        Connection conn = null;
        try {
            JSONObject jsonReq = JSONObject.fromObject(parameter);

            JSONArray tmcIds = jsonReq.getJSONArray("tmcIds");

            conn = DBConnector.getInstance().getMetaConnection();

            TmcSelector selector = new TmcSelector(conn);

            TmcLineTree tree = selector.queryTmcTree(tmcIds);

            return new ModelAndView("jsonView", success(tree.Serialize(ObjLevel.BRIEF)));

        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            return new ModelAndView("jsonView", fail(e.getMessage()));
        } finally {
            DbUtils.closeQuietly(conn);
        }
    }

    @RequestMapping(value = "/queryTmcById")
    public ModelAndView queryTmcPointById(HttpServletRequest request)
            throws ServletException, IOException {

        String parameter = request.getParameter("parameter");
        Connection conn = null;
        try {
            JSONObject jsonReq = JSONObject.fromObject(parameter);

            conn = DBConnector.getInstance().getMetaConnection();

            int tmcId = jsonReq.getInt("tmcId");

            String objType = jsonReq.getString("type");

            switch (ObjType.valueOf(objType)) {
                case TMCLINE:
                    TmcLineSelector lineSelector = new TmcLineSelector(conn);

                    TmcLine tmcLine = lineSelector.loadByTmcLineId(tmcId);

                    return new ModelAndView("jsonView", success(tmcLine.Serialize(ObjLevel.BRIEF)));
                case TMCPOINT:
                    TmcPointSelector pointSelector = new TmcPointSelector(conn);

                    TmcPoint tmcPoint = pointSelector.loadByTmcPointId(tmcId);

                    return new ModelAndView("jsonView", success(tmcPoint.Serialize(ObjLevel.BRIEF)));
                default:
                    return new ModelAndView("jsonView", fail("TMC查询类型参数错误"));
            }
        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            return new ModelAndView("jsonView", fail(e.getMessage()));
        } finally {
            DbUtils.closeQuietly(conn);
        }
    }

    @RequestMapping(value = "/queryChargingChain")
    public ModelAndView queryChargingChain(HttpServletRequest request)
            throws ServletException, IOException {

        try {

            ChainSelector selector = new ChainSelector();

            JSONArray data = selector.getChargingChain();

            return new ModelAndView("jsonView", success(data));

        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            return new ModelAndView("jsonView", fail(e.getMessage()));
        }
    }
    @RequestMapping(value = "/queryAreaCodeByRegionId")
    public ModelAndView queryAreaCodeByRegionId(HttpServletRequest request)
            throws ServletException, IOException {
    	String parameter = request.getParameter("parameter");
    	Connection conn = null;

        try {
        	
        	JSONObject jsonReq = JSONObject.fromObject(parameter);

        	int regionId = jsonReq.getInt("regionId");
        	int taskId = jsonReq.getInt("taskId");
        	
        	ManApi apiService = (ManApi) ApplicationContextUtil.getBean("manApi");
        	Subtask subtask = apiService.queryBySubtaskId(taskId);
			int dbId = subtask.getDbId();
			conn = DBConnector.getInstance().getConnectionById(dbId);
        	
        	AdAdminSearch adAdminSearch = new AdAdminSearch(conn);
			AdAdmin adAdmin = (AdAdmin) adAdminSearch.searchDataByPid(regionId);
			int adminId=adAdmin.getAdminId();
        	
        	ScPointAdminArea selector = new ScPointAdminArea();

        	JSONObject data = selector.searchByAdminCode(String.valueOf(adminId));

            return new ModelAndView("jsonView", success(data));

        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            return new ModelAndView("jsonView", fail(e.getMessage()));
	    } finally {
	        DbUtils.closeQuietly(conn);
	    }
    }
    
    @RequestMapping(value = "/rdname/searchFix")
    public ModelAndView searchRdNameFix(HttpServletRequest request)
            throws ServletException, IOException {
        String parameter = request.getParameter("parameter");
        try {
            JSONObject jsonReq = JSONObject.fromObject(parameter);

            RdNameSelector selector = new RdNameSelector();

            String langCode = jsonReq.getString("langCode");
            if(langCode==null || StringUtils.isEmpty(langCode)){
				throw new IllegalArgumentException("langCode参数不能为空。");
			}
            logger.info("langCode: "+langCode);
           
            JSONObject data = selector.searchRdNameFix(langCode);

            return new ModelAndView("jsonView", success(data));

        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            return new ModelAndView("jsonView", fail(e.getMessage()));
        }
    }
    
    
    /**
     * @Title: metadataTeilen
     * @Description: 道路名拆分,元数据编辑平台
     * @param request
     * @return
     * @throws ServletException
     * @throws IOException  ModelAndView
     * @throws 
     * @author zl zhangli5174@navinfo.com
     * @date 2017年4月11日 下午7:02:16 
     */
    @RequestMapping(value = "/rdname/metadataTeilen")
    public ModelAndView metadataTeilen(HttpServletRequest request)
            throws ServletException, IOException {
        String parameter = request.getParameter("parameter");

        Connection conn = null;

        try {
            JSONObject jsonReq = JSONObject.fromObject(parameter);

            int flag = jsonReq.getInt("flag");

            conn = DBConnector.getInstance().getMetaConnection();

            RdNameOperation operation = new RdNameOperation(conn);

            if (flag > 0) {//拆分指定数据
                JSONArray dataList = jsonReq.getJSONArray("data");

                operation.teilenRdName(dataList);
            } else {//拆分特定范围内数据
            	JSONObject params = jsonReq.getJSONObject("params");

                operation.teilenRdNameByParams(params);
            }

            return new ModelAndView("jsonView", success());
        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            return new ModelAndView("jsonView", fail(e.getMessage()));
        } finally {
            DbUtils.commitAndCloseQuietly(conn);
        }
    }

    /**
     * @Title: getCiParaKindTop
     * @Description: 查询POI分类元数据接口
     * @param request
     * @return
     * @throws ServletException
     * @throws IOException  ModelAndView
     * @throws 
     * @author zl zhangli5174@navinfo.com
     * @date 2017年5月18日 下午5:18:31 
     */
    @RequestMapping(value = "/getCiParaKindTop")
    public ModelAndView getCiParaKindTop(HttpServletRequest request)
            throws ServletException, IOException {
        String parameter = request.getParameter("parameter");

        Connection conn = null;

        try {
            JSONObject jsonReq = JSONObject.fromObject(parameter);

            conn = DBConnector.getInstance().getMetaConnection();

            ScPointPoicodeNewService scPointPoicodeNewService = new ScPointPoicodeNewService();
            JSONArray data = scPointPoicodeNewService.list(jsonReq);

            return new ModelAndView("jsonView", success(data));
        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            return new ModelAndView("jsonView", fail(e.getMessage()));
        } finally {
            DbUtils.commitAndCloseQuietly(conn);
        }
    }
    
    /**
     * @Title: ScPointPoicodeNewService
     * @Description: 查询POI大分类/中分类元数据接口，应用场景：外业规划平台--数据规划--条件规划--poi分类
     * @param request
     * @return
     * @throws ServletException
     * @throws IOException  ModelAndView
     * @throws ServiceException 
     * @throws 
     * @author zl zhangli5174@navinfo.com
     * @date 2017年5月18日 下午5:18:31 
     */
    @RequestMapping(value = "/scPointPoicodeList")
    public ModelAndView scPointPoicodeList(HttpServletRequest request)
            throws ServletException, IOException, ServiceException {
            ScPointPoicodeNewService scPointPoicodeNewService = new ScPointPoicodeNewService();
            List<Map<String, Object>> data = scPointPoicodeNewService.list();

            return new ModelAndView("jsonView", success(data));
    }
}
