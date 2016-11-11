package com.navinfo.dataservice.web.man.controller;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.api.job.iface.JobApi;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.engine.man.produce.ProduceService;
import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.database.QueryRunner;

/** 
* @ClassName: BlockController 
* @author code generator 
* @date 2016年4月6日 下午6:25:24 
* @Description: TODO
*/
@Controller
public class ProduceController extends BaseController {
	private Logger log = LoggerRepos.getLogger(this.getClass());


	/**
	 * @Title: list
	 * @Description: (修改)日出品管理--列表(第七迭代)
	 * @param request
	 * @return  ModelAndView
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年11月3日 下午2:25:47 
	 */
	@RequestMapping(value = "/produce/list")
	public ModelAndView list(HttpServletRequest request){
		try{
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));

			int curPageNum = 1;// 默认为第一页
			String curPage = dataJson.getString("pageNum");
			if (StringUtils.isNotEmpty(curPage)) {
				curPageNum = Integer.parseInt(curPage);
			}
			int curPageSize = 20;// 默认为20条记录/页
			String curSize = dataJson.getString("pageSize");
			if (StringUtils.isNotEmpty(curSize)) {
				curPageSize = Integer.parseInt(curSize);
			}
			JSONObject condition = new JSONObject();	
			if(dataJson.containsKey("condition")){
				condition=dataJson.getJSONObject("condition");
			}
			Page data=ProduceService.getInstance().list(condition, curPageNum, curPageSize);
			Map<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put("result", data.getResult());
			resultMap.put("totalCount", data.getTotalCount());
			return new ModelAndView("jsonView", success(resultMap));
		}catch(Exception e){
			log.error("日出品失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	/**
	 * 日出品管理--列表
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/produce/query")
	public ModelAndView query(HttpServletRequest request){
		try{
			JSONObject dataJson = JSONObject.fromObject(URLDecode(request.getParameter("parameter")));

			int produceId = dataJson.getInt("produceId");
			Map<String, Object> data=ProduceService.getInstance().query(produceId);
			return new ModelAndView("jsonView", success(data));
		}catch(Exception e){
			log.error("日出品失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	/**
	 * 日出品管理--生成POI&Road日出品包,生成POI日出品包
	 * 判断类型，如果是POI，将grid范围内的POI数据刷到出品库；否则将grid范围内的全部数据刷到出品库。再调用出品转换脚本生成出品包。
	 * @param request
	 * @return
	 */
	/**
	 * @Title: generateDaily
	 * @Description: (修改)(第七迭代)
	 * @param request
	 * @return  ModelAndView
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年11月3日 下午2:01:16 
	 */
	@RequestMapping(value = "/produce/generateDaily")
	public ModelAndView generateDaily(HttpServletRequest request){
		try{	
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}		
			JSONObject dataJson = JSONObject.fromObject(URLDecode(parameter));			
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			AccessToken tokenObj=(AccessToken) request.getAttribute("token");
			long userId=tokenObj.getUserId();
			ProduceService.getInstance().generateDaily(userId,dataJson);
			//long userId=2;
			
			return new ModelAndView("jsonView", success("日出品创建成功"));
		}catch(Exception e){
			log.error("日出品失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	/**
	 * 月出品管理
	 * 选中需要出品的city，点击“生成月出品包”按钮，后台提取需要出品的数据履历并灌月出品库，进行月出品的转换：
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/produce/generateMonthly")
	public ModelAndView generateMonthly(HttpServletRequest request){
		try{	
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}		
			JSONObject dataJson = JSONObject.fromObject(URLDecode(parameter));			
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			AccessToken tokenObj=(AccessToken) request.getAttribute("token");
			long userId=tokenObj.getUserId();
			//long userId=2;
			JobApi jobApi=(JobApi) ApplicationContextUtil.getBean("jobApi");
			/*
			 * {"gridIds":[213424,343434,23423432],"stopTime":"yyyymmddhh24miss","dataType":"POI"//POI,ALL}
			 * jobType:releaseFmIdbDailyJob/releaseFmIdbMonthlyJob
			 */
			Connection conn = null;
			List<Integer> gridIds=new ArrayList<Integer>();
			try{
				QueryRunner run = new QueryRunner();
				conn = DBConnector.getInstance().getManConnection();
				String selectSql = "select g.grid_id from grid g where g.city_id in ("+StringUtils.join(dataJson.getJSONArray("cityIds"), ",")+")";		
				ResultSetHandler<List<Integer>> rsHandler = new ResultSetHandler<List<Integer>>(){
					public List<Integer> handle(ResultSet rs) throws SQLException {
						List<Integer> list = new ArrayList<Integer>();
						while(rs.next()){
								list.add(rs.getInt("GRID_ID"));
								}
						return list;
					}	    		
		    	};
		    	gridIds= run.query(conn, selectSql, rsHandler);
			}finally{
				DbUtils.closeQuietly(conn);
			}
			JSONObject jobDataJson=new JSONObject();
			jobDataJson.put("gridList", gridIds);
			jobDataJson.put("stopTime", "20160616000000");
			long jobId=jobApi.createJob("releaseFmIdbMonthlyJob", jobDataJson,userId, "月出品");
			return new ModelAndView("jsonView", success(jobId));
		}catch(Exception e){
			log.error("月出品失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
	/**
	 * 融合管理--融合月编辑库
	 * 选中需要融合的grid，点击“融合月编辑库”按钮，后台进行融合
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/produce/merge")
	public ModelAndView merge(HttpServletRequest request){
		try{	
			String parameter = request.getParameter("parameter");
			if (StringUtils.isEmpty(parameter)){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}		
			JSONObject dataJson = JSONObject.fromObject(URLDecode(parameter));			
			if(dataJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			AccessToken tokenObj=(AccessToken) request.getAttribute("token");
			long userId=tokenObj.getUserId();
			//long userId=2;
			JobApi jobApi=(JobApi) ApplicationContextUtil.getBean("jobApi");
			/*
			 * {"gridIds":[213424,343434,23423432],"stopTime":"yyyymmddhh24miss"}
			 * jobType:releaseFmidbDaily/releaseFmidbMonthly
			 */
			JSONObject jobDataJson=new JSONObject();
			jobDataJson.put("gridList", dataJson.get("gridIds"));
			jobDataJson.put("stopTime", "20160616000000");
			long jobId=jobApi.createJob("day2MonthRoadJob", jobDataJson, userId, "ROAD月融合");	
			return new ModelAndView("jsonView", success(jobId));
			
		}catch(Exception e){
			log.error("道路融合失败，原因："+e.getMessage(), e);
			return new ModelAndView("jsonView",exception(e));
		}
	}
	
}
