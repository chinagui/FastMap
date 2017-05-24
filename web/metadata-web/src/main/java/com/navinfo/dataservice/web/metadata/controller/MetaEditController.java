package com.navinfo.dataservice.web.metadata.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.web.servlet.ModelAndView;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.util.ResponseUtils;
import com.navinfo.dataservice.engine.meta.service.ScBcrossnodeMatchckService;
import com.navinfo.dataservice.engine.meta.service.ScBranchCommcService;
import com.navinfo.dataservice.engine.meta.service.ScBranchSpeccService;
import com.navinfo.dataservice.engine.meta.service.ScModelMatchGService;
import com.navinfo.dataservice.engine.meta.service.ScModelRepdelGService;
import com.navinfo.dataservice.engine.meta.service.ScRoadnameAbbService;
import com.navinfo.dataservice.engine.meta.service.ScRoadnameEngnmQjService;
import com.navinfo.dataservice.engine.meta.service.ScRoadnameFixedPhraseService;
import com.navinfo.dataservice.engine.meta.service.ScRoadnameHwCodeService;
import com.navinfo.dataservice.engine.meta.service.ScRoadnameHwInfoService;
import com.navinfo.dataservice.engine.meta.service.ScRoadnameInfixService;
import com.navinfo.dataservice.engine.meta.service.ScRoadnamePositionService;
import com.navinfo.dataservice.engine.meta.service.ScRoadnameSplitPrefixService;
import com.navinfo.dataservice.engine.meta.service.ScRoadnameSuffixService;
import com.navinfo.dataservice.engine.meta.service.ScRoadnameTypenameService;
import com.navinfo.dataservice.engine.meta.service.ScVectorMatchService;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.StringUtils;
import com.navinfo.navicommons.database.Page;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.sql.BLOB;

/** 
* @ClassName: ScRoadnameAbbController 
* @author code generator 
* @date 2016年4月6日 下午6:25:24 
* @Description: TODO
*/
@Controller
public class MetaEditController extends BaseController {
	private Logger log = LoggerRepos.getLogger(this.getClass());
	@Autowired 
	private ScRoadnameAbbService scRoadnameAbbService;
	@Autowired 
	private ScBranchCommcService scBranchCommcService;
	@Autowired 
	private ScBranchSpeccService scBranchSpeccService;
	@Autowired 
	private ScModelMatchGService scModelMatchGService;
	@Autowired 
	private ScModelRepdelGService scModelRepdelGService;
	@Autowired 
	private ScRoadnameEngnmQjService scRoadnameEngnmQjService;
	@Autowired 
	private ScRoadnameFixedPhraseService scRoadnameFixedPhraseService;
	@Autowired 
	private ScRoadnameHwCodeService scRoadnameHwCodeService;
	@Autowired 
	private ScRoadnameHwInfoService scRoadnameHwInfoService;
	@Autowired 
	private ScRoadnameInfixService scRoadnameInfixService;
	@Autowired 
	private ScRoadnamePositionService scRoadnamePositionService;
	@Autowired 
	private ScRoadnameSplitPrefixService scRoadnameSplitPrefixService;
	@Autowired 
	private ScRoadnameSuffixService scRoadnameSuffixService;
	@Autowired 
	private ScRoadnameTypenameService scRoadnameTypenameService;
	@Autowired 
	private ScVectorMatchService scVectorMatchService;
	@Autowired 
	private ScBcrossnodeMatchckService scBcrossnodeMatchckService;
	
	/**
	 * @Title: create
	 * @Description: 图形文件六张变的新增修改
	 * @param request
	 * @return  ModelAndView
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年3月27日 下午2:25:02 
	 */
	@RequestMapping(value = "/metadataEdit/patternImage/save")
	public ModelAndView imageCreate(HttpServletRequest request){
		try{	
			//处理长传参数
			DiskFileItemFactory factory = new DiskFileItemFactory();
			
			ServletFileUpload upload = new ServletFileUpload(factory);
			
			List<FileItem> items = upload.parseRequest(request);
			Iterator<FileItem> it = items.iterator();
			String tableName  = null;
			JSONObject dataJson =null;
			
			FileItem uploadItem = null;
				
			while(it.hasNext()){
				FileItem item = it.next();
				
				if (item.isFormField()){
					if ("parameter".equals(item.getFieldName())) {
						String param = item.getString("UTF-8");
						JSONObject jsonParam = JSONObject.fromObject(param);
						if(jsonParam.containsKey("tableName")){
							tableName = jsonParam.getString("tableName");
						}
						if(jsonParam.containsKey("data")){
							dataJson = jsonParam.getJSONObject("data");
						}
					}
				}else{
						if (item.getName()!= null && !item.getName().equals("")){
							uploadItem = item;
						}else{
							throw new Exception("上传的文件格式有问题！");
						}
				}
			}
			/*if (StringUtils.isEmpty(parameter)){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}	*/	
			/*JSONObject parameterJson = JSONObject.fromObject(URLDecode(parameter));			
			if(parameterJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			 tableName  = parameterJson.getString("tableName");*/
			if(tableName==null || StringUtils.isEmpty(tableName)){
				throw new IllegalArgumentException("tableName参数不能为空。");
			}
			 
			if(dataJson==null || dataJson.isEmpty()){
				throw new IllegalArgumentException("data参数不能为空。");
			}
			InputStream fileStream =null;
			if(uploadItem != null){
				fileStream = uploadItem.getInputStream();
			}
			
			if(tableName.equals("scMdelMatchG")){
				scModelMatchGService.save(dataJson,fileStream);
			}else if(tableName.equals("scModelRepdelG")){
				scModelRepdelGService.saveUpdate(dataJson);
			}else if(tableName.equals("scVectorMatch")){
				scVectorMatchService.saveUpdate(dataJson);
			}else if(tableName.equals("scBranchCommc")){
				scBranchCommcService.saveUpdate(dataJson);
			}else if(tableName.equals("scBranchSpecc")){
				scBranchSpeccService.saveUpdate(dataJson);
			}else if(tableName.equals("scBcrossnodeMatchck")){
				scBcrossnodeMatchckService.saveUpdate(dataJson);
			}else{
				throw new IllegalArgumentException("不识别的表: "+tableName);
			}
			return new ModelAndView("jsonView", success("创建成功"));
		}catch(Exception e){
			log.error("创建失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	
	@RequestMapping(value = "/metadataEdit/patternImage/update")
	public ModelAndView imageUpdate(HttpServletRequest request){
		try{	
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}		
			JSONObject parameterJson = JSONObject.fromObject(URLDecode(parameter));			
			if(parameterJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			String tableName  = parameterJson.getString("tableName");
			if(tableName==null || StringUtils.isEmpty(tableName)){
				throw new IllegalArgumentException("tableName参数不能为空。");
			}
			JSONObject dataJson = parameterJson.getJSONObject("data");
			if(dataJson==null || dataJson.isEmpty()){
				throw new IllegalArgumentException("data参数不能为空。");
			}
			if(tableName.equals("scMdelMatchG")){
				scModelMatchGService.update(dataJson);
			}else if(tableName.equals("scModelRepdelG")){
				scModelRepdelGService.saveUpdate(dataJson);
			}else if(tableName.equals("scVectorMatch")){
				scVectorMatchService.saveUpdate(dataJson);
			}else if(tableName.equals("scBranchCommc")){
				scBranchCommcService.saveUpdate(dataJson);
			}else if(tableName.equals("scBranchSpecc")){
				scBranchSpeccService.saveUpdate(dataJson);
			}else if(tableName.equals("scBcrossnodeMatchck")){
				scBcrossnodeMatchckService.saveUpdate(dataJson);
			}else{
				throw new IllegalArgumentException("不识别的表: "+tableName);
			}
			return new ModelAndView("jsonView", success("创建成功"));
		}catch(Exception e){
			log.error("创建失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	/**
	 * @Title: delete
	 * @Description: 图形文件的删除
	 * @param request
	 * @return  ModelAndView
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年3月27日 下午4:48:59 
	 */
	@RequestMapping(value = "/metadataEdit/patternImage/delete")
	public ModelAndView delete(HttpServletRequest request){
		try{			
			JSONObject parameterJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));			
			if(parameterJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			String tableName  = parameterJson.getString("tableName");
			if(tableName==null || StringUtils.isEmpty(tableName)){
				throw new IllegalArgumentException("tableName参数不能为空。");
			}
			JSONArray idsJson = parameterJson.getJSONArray("ids");
			if(idsJson==null || idsJson.size() <= 0 ){
				throw new IllegalArgumentException("ids参数不能为空。");
			}
			if(tableName.equals("scMdelMatchG")){
				scModelMatchGService.deleteByIds(idsJson);
			}else if(tableName.equals("scModelRepdelG")){
				scModelRepdelGService.deleteByIds(idsJson);
			}else if(tableName.equals("scVectorMatch")){
				scVectorMatchService.deleteByIds(idsJson);
			}else if(tableName.equals("scBranchCommc")){
				scBranchCommcService.deleteByIds(idsJson);
			}else if(tableName.equals("scBranchSpecc")){
				scBranchSpeccService.deleteByIds(idsJson);
			}else if(tableName.equals("scBcrossnodeMatchck")){
				scBcrossnodeMatchckService.deleteByIds(idsJson);
			}else{
				throw new IllegalArgumentException("不识别的表: "+tableName);
			}
			return new ModelAndView("jsonView", success("删除成功"));
		}catch(Exception e){
			log.error("删除失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	/**
	 * @Title: list
	 * @Description: 图形文件条件查询
	 * @param request
	 * @return  ModelAndView
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年3月28日 下午1:40:27 
	 */
	@RequestMapping(value = "/metadataEdit/patternImage/search")
	public ModelAndView list(HttpServletRequest request){
		try{			
			JSONObject parameterJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));			
			if(parameterJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			int curPageNum= 1;//默认为第一页
			int pageSize = 20;
			String sortby = "";//排序
			String tableName  = parameterJson.getString("tableName");
			if(tableName==null || StringUtils.isEmpty(tableName)){
				throw new IllegalArgumentException("tableName参数不能为空。");
			}
			
			if(parameterJson.containsKey("pageNum") &&  parameterJson.getInt("pageNum") > 0){
				curPageNum = parameterJson.getInt("pageNum");
			}
			if(parameterJson.containsKey("pageSize") &&  parameterJson.getInt("pageSize") > 0){
				pageSize = parameterJson.getInt("pageSize");
			}

			if(parameterJson.containsKey("sortby") &&  parameterJson.getString("sortby") != null){
				sortby = parameterJson.getString("sortby");
			}
			JSONObject dataJson = parameterJson.getJSONObject("data");
			if(dataJson==null || dataJson.isEmpty()){
				throw new IllegalArgumentException("data参数不能为空。");
			}
			if(tableName==null || StringUtils.isEmpty(tableName)){
				throw new IllegalArgumentException("tableName参数不能为空。");
			}
			Page data = new Page();
			if(tableName.equals("scMdelMatchG")){
				data = scModelMatchGService.list(dataJson,curPageNum,pageSize,sortby);
			}else if(tableName.equals("scModelRepdelG")){
				data = scModelRepdelGService.list(dataJson,curPageNum,pageSize,sortby);
			}else if(tableName.equals("scVectorMatch")){
				data = scVectorMatchService.list(dataJson,curPageNum,pageSize,sortby);
			}else if(tableName.equals("scBranchCommc")){
				data = scBranchCommcService.list(dataJson,curPageNum,pageSize,sortby);
			}else if(tableName.equals("scBranchSpecc")){
				data = scBranchSpeccService.list(dataJson,curPageNum,pageSize,sortby);
			}else if(tableName.equals("scBcrossnodeMatchck")){
				data = scBcrossnodeMatchckService.list(dataJson,curPageNum,pageSize,sortby);
			}else{
				throw new IllegalArgumentException("不识别的表: "+tableName);
			}
			return new ModelAndView("jsonView", success(data));
		}catch(Exception e){
			log.error("获取列表失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	@RequestMapping(value = "/metadataEdit/patternImage/getImage")
	public void getImageById(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("image/jpeg;charset=GBK");

		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Methods",
				"POST, GET, OPTIONS, DELETE,PUT"); 

		String parameter = request.getParameter("parameter");

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String tableName  = jsonReq.getString("tableName");
			if(tableName==null || StringUtils.isEmpty(tableName)){
				throw new IllegalArgumentException("tableName参数不能为空。");
			}
			String id  = jsonReq.getString("id");
			if(id==null || StringUtils.isEmpty(tableName)){
				throw new IllegalArgumentException("id参数不能为空。");
			}
			BLOB imageBlob = null;
			if(tableName.equals("scMdelMatchG")){
				imageBlob =scModelMatchGService.getFileContentById(id);
			}

			//处理返回的imageBlob 数据
			InputStream in = imageBlob.getBinaryStream(); // 建立输出流
            int len = (int) imageBlob.length();
            byte[] data = new byte[len]; // 建立缓冲区
            in.read(data);
            in.close();
			response.getOutputStream().write(data);

			
		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			response.getWriter().println(
					ResponseUtils.assembleFailResult(e.getMessage()));
		} finally {
			
		}

	}
	//*************************rd_name****************************
	/**
	 * @Title: rdCreate
	 * @Description: 道路名作业9张表的新增修改
	 * @param request
	 * @return  ModelAndView
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年3月27日 下午2:25:02 
	 */
	@RequestMapping(value = "/metadataEdit/rdName/saveUpdate")
	public ModelAndView rdCreate(HttpServletRequest request){
		try{	
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}		
			JSONObject parameterJson = JSONObject.fromObject(URLDecode(parameter));			
			if(parameterJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			String tableName  = parameterJson.getString("tableName");
			if(tableName==null || StringUtils.isEmpty(tableName)){
				throw new IllegalArgumentException("tableName参数不能为空。");
			}
			JSONObject dataJson = parameterJson.getJSONObject("data");
			if(dataJson==null || dataJson.isEmpty()){
				throw new IllegalArgumentException("data参数不能为空。");
			}
			if(tableName.equals("scRoadnameHwInfo")){
				scRoadnameHwInfoService.update(dataJson);
			}else if(tableName.equals("scRoadnameInfix")){
				scRoadnameInfixService.saveUpdate(dataJson);
			}else if(tableName.equals("scRoadnameFixedPhrase")){
				scRoadnameFixedPhraseService.saveUpdate(dataJson);
			}else if(tableName.equals("scRoadnameTypename")){
				scRoadnameTypenameService.saveUpdate(dataJson);
			}else if(tableName.equals("scRoadnamePosition")){
				scRoadnamePositionService.saveUpdate(dataJson);
			}else if(tableName.equals("scRoadnameSuffix")){
				scRoadnameSuffixService.saveUpdate(dataJson);
			}else if(tableName.equals("scRoadnameSplitPrefix")){
				scRoadnameSplitPrefixService.saveUpdate(dataJson);
			}else if(tableName.equals("scRoadnameEngnmQj")){
				scRoadnameEngnmQjService.saveUpdate(dataJson);
			}else if(tableName.equals("scRoadnameHwCode")){
				scRoadnameHwCodeService.saveUpdate(dataJson);
			}else if(tableName.equals("scRoadnameAbb")){
				scRoadnameAbbService.saveUpdate(dataJson);
			}else{
				throw new IllegalArgumentException("不识别的表: "+tableName);
			}
			return new ModelAndView("jsonView", success("创建成功"));
		}catch(Exception e){
			log.error("创建失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	
	
	/**
	 * @Title: delete
	 * @Description: 道路名作业9张表的删除
	 * @param request
	 * @return  ModelAndView
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年3月27日 下午4:48:59 
	 */
	@RequestMapping(value = "/metadataEdit/rdName/delete")
	public ModelAndView rdDelete(HttpServletRequest request){
		try{			
			JSONObject parameterJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));			
			if(parameterJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			String tableName  = parameterJson.getString("tableName");
			if(tableName==null || StringUtils.isEmpty(tableName)){
				throw new IllegalArgumentException("tableName参数不能为空。");
			}
			JSONArray idsJson = parameterJson.getJSONArray("ids");
			if(idsJson==null || idsJson.size() <= 0 ){
				throw new IllegalArgumentException("ids参数不能为空。");
			}
			if(tableName.equals("scRoadnameHwInfo")){
				scRoadnameHwInfoService.deleteByIds(idsJson);
			}else if(tableName.equals("scRoadnameInfix")){
				scRoadnameInfixService.deleteByIds(idsJson);
			}else if(tableName.equals("scRoadnameFixedPhrase")){
				scRoadnameFixedPhraseService.deleteByIds(idsJson);
			}else if(tableName.equals("scRoadnameTypename")){
				scRoadnameTypenameService.deleteByIds(idsJson);
			}else if(tableName.equals("scRoadnamePosition")){
				scRoadnamePositionService.deleteByIds(idsJson);
			}else if(tableName.equals("scRoadnameSuffix")){
				scRoadnameSuffixService.deleteByIds(idsJson);
			}else if(tableName.equals("scRoadnameSplitPrefix")){
				scRoadnameSplitPrefixService.deleteByIds(idsJson);
			}else if(tableName.equals("scRoadnameEngnmQj")){
				scRoadnameEngnmQjService.deleteByIds(idsJson);
			}else if(tableName.equals("scRoadnameHwCode")){
				scRoadnameHwCodeService.deleteByIds(idsJson);
			}else if(tableName.equals("scRoadnameAbb")){
				scRoadnameAbbService.deleteByIds(idsJson);
			}else{
				throw new IllegalArgumentException("不识别的表: "+tableName);
			}
			return new ModelAndView("jsonView", success("删除成功"));
		}catch(Exception e){
			log.error("删除失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	/**
	 * @Title: rdList
	 * @Description: 道路名作业9张表查询
	 * @param request
	 * @return  ModelAndView
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年3月28日 上午11:08:22 
	 */
	@RequestMapping(value = "/metadataEdit/rdName/search")
	public ModelAndView rdList(HttpServletRequest request){
		try{			
			JSONObject parameterJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));			
			if(parameterJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			int curPageNum= 1;//默认为第一页
			int pageSize = 20;
			String sortby = "";//排序
			String tableName  = parameterJson.getString("tableName");
			if(tableName==null || StringUtils.isEmpty(tableName)){
				throw new IllegalArgumentException("tableName参数不能为空。");
			}
			
			if(parameterJson.containsKey("pageNum") &&  parameterJson.getInt("pageNum") > 0){
				curPageNum = parameterJson.getInt("pageNum");
			}
			if(parameterJson.containsKey("pageSize") &&  parameterJson.getInt("pageSize") > 0){
				pageSize = parameterJson.getInt("pageSize");
			}

			if(parameterJson.containsKey("sortby") &&  parameterJson.getString("sortby") != null){
				sortby = parameterJson.getString("sortby");
			}
			JSONObject dataJson = parameterJson.getJSONObject("data");
			if(dataJson==null || dataJson.isEmpty()){
				throw new IllegalArgumentException("data参数不能为空。");
			}
			if(tableName==null || StringUtils.isEmpty(tableName)){
				throw new IllegalArgumentException("tableName参数不能为空。");
			}
			Page data = new Page();
			if(tableName.equals("scRoadnameHwInfo")){
				data = scRoadnameHwInfoService.list(dataJson,curPageNum,pageSize,sortby);
			}else if(tableName.equals("scRoadnameInfix")){
				data = scRoadnameInfixService.list(dataJson,curPageNum,pageSize,sortby);
			}else if(tableName.equals("scRoadnameFixedPhrase")){
				data = scRoadnameFixedPhraseService.list(dataJson,curPageNum,pageSize,sortby);
			}else if(tableName.equals("scRoadnameTypename")){
				data = scRoadnameTypenameService.list(dataJson,curPageNum,pageSize,sortby);
			}else if(tableName.equals("scRoadnamePosition")){
				data = scRoadnamePositionService.list(dataJson,curPageNum,pageSize,sortby);
			}else if(tableName.equals("scRoadnameSuffix")){
				data = scRoadnameSuffixService.list(dataJson,curPageNum,pageSize,sortby);
			}else if(tableName.equals("scRoadnameSplitPrefix")){
				data = scRoadnameSplitPrefixService.list(dataJson,curPageNum,pageSize,sortby);
			}else if(tableName.equals("scRoadnameEngnmQj")){
				data = scRoadnameEngnmQjService.list(dataJson,curPageNum,pageSize,sortby);
			}else if(tableName.equals("scRoadnameHwCode")){
				data = scRoadnameHwCodeService.list(dataJson,curPageNum,pageSize,sortby);
			}else if(tableName.equals("scRoadnameAbb")){
				data = scRoadnameAbbService.list(dataJson,curPageNum,pageSize,sortby);
			}else{
				throw new IllegalArgumentException("不识别的表: "+tableName);
			}
			return new ModelAndView("jsonView", success(data));
		}catch(Exception e){
			log.error("获取列表失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
}
