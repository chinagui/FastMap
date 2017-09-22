package com.navinfo.daraservice.web.limit;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.engine.limit.glm.iface.IRow;
import com.navinfo.dataservice.engine.limit.glm.iface.LimitObjType;
import com.navinfo.dataservice.engine.limit.search.SearchProcess;
import com.navinfo.dataservice.engine.limit.search.gdb.RdLinkSearch;
import com.navinfo.navicommons.database.QueryRunner;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by ly on 2017/9/14.
 */

@Controller
public class limitController  extends BaseController {

    @RequestMapping(value = "/test")
    public ModelAndView test(HttpServletRequest request) throws ServletException, IOException {
        return new ModelAndView("jsonView", success());
    }

    @RequestMapping(value = "/getMetaDataByCondition")
    public ModelAndView getMetaDataByCondition(HttpServletRequest request)
            throws ServletException, IOException {

        String parameter = request.getParameter("parameter");

        Connection conn = null;

        try {
            JSONObject jsonReq = JSONObject.fromObject(parameter);

            String objType = jsonReq.getString("type");

            conn = DBConnector.getInstance().getMetaConnection();

            JSONObject condition = jsonReq.getJSONObject("condition");

            SearchProcess p = new SearchProcess(conn);

            List<IRow> objList = new ArrayList<>();
            int count = p.searchMetaDataByCondition(
                    LimitObjType.valueOf(objType), condition, objList);

            JSONObject result = new JSONObject();
            JSONArray array = new JSONArray();

			for (IRow obj : objList) {
				JSONObject json = obj.Serialize(ObjLevel.FULL);
				json.put("geoLiveType", objType);
				array.add(json);
			}
			
			result.put("total", count);
			result.put("data", array);
			return new ModelAndView("jsonView", success(result));
        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            return new ModelAndView("jsonView", fail(e.getMessage()));
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @RequestMapping(value = "/getLimitDataByCondition")
    public ModelAndView getLimitDataByCondition(HttpServletRequest request)
            throws ServletException, IOException {

        String parameter = request.getParameter("parameter");

        Connection conn = null;

        try {
            JSONObject jsonReq = JSONObject.fromObject(parameter);

            String objType = jsonReq.getString("type");

            conn = DBConnector.getInstance().getLimitConnection();

            JSONObject condition = jsonReq.getJSONObject("condition");

            SearchProcess p = new SearchProcess(conn);
            
            List<IRow> objList = new ArrayList<>();

            int count = p.searchLimitDataByCondition(
                    LimitObjType.valueOf(objType), condition, objList);

            JSONObject result = new JSONObject();
            JSONArray array = new JSONArray();

            for (IRow obj : objList) {
			    JSONObject json = obj.Serialize(ObjLevel.FULL);
			    json.put("geoLiveType", objType);
			    array.add(json);
			}
			
			result.put("total", count);
			result.put("data", array);
			
			return new ModelAndView("jsonView", success(result));
        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            return new ModelAndView("jsonView", fail(e.getMessage()));
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    @RequestMapping(value = "/getRegionIdByAdmin")
    public ModelAndView getRegionIdByAdiminArea(HttpServletRequest request) throws Exception{
    	String parameter = request.getParameter("parameter");
    	
    	Connection conn = null;
    	
    	try{
    		JSONObject jsonReq = JSONObject.fromObject(parameter);

    		if(jsonReq== null ||!jsonReq.containsKey("adminCode")){
    			throw new Exception ("行政区划编码为空，无法获取相应大区库！");
    		}
    		
            String adminCode = jsonReq.getString("adminCode");

    		conn = DBConnector.getInstance().getManConnection();
    		
    		QueryRunner run = new QueryRunner();
    		
    		String sql = "SELECT A.DAILY_DB_ID FROM REGION A,CP_REGION_PROVINCE B WHERE A.REGION_ID = B.REGION_ID AND B.ADMINCODE = '" + adminCode + "'";
    		
    		int dbId = run.queryForInt(conn, sql);
    		
    		return new ModelAndView("jsonView",success(dbId));
    		
    	}catch(Exception e){
    		logger.error(e.getMessage(),e);
    		
    		return new ModelAndView("jsonView",fail(e.getMessage()));
    	}finally{
    		if(conn != null){
    			conn.close();
    		}
    	}
    }
    
    @RequestMapping(value = "/searchRdLinkByName")
    public ModelAndView getRdLinkByName(HttpServletRequest request) throws Exception{
    	String parameter = request.getParameter("parameter");
    	
    	Connection conn = null;
    	
    	try{
    		JSONObject jsonReq = JSONObject.fromObject(parameter);

    		if(jsonReq == null ||!jsonReq.containsKey("dbId") || !jsonReq.containsKey("type")||!jsonReq.containsKey("condition")){
    			throw new Exception ("输入不完善，无法查询道路link！");
    		}
    		
    		int dbId = jsonReq.getInt("dbId");
    	
    		JSONObject condition = jsonReq.getJSONObject("condition");
    		
    		conn = DBConnector.getInstance().getConnectionById(dbId);
    		
    		RdLinkSearch p = new RdLinkSearch(conn);
    		
    		JSONObject result = p.searchDataByCondition(condition);
    		
    		return new ModelAndView("jsonView",success(result));
    		
    	}catch(Exception e){
    		logger.error(e.getMessage(),e);
    		
    		return new ModelAndView("jsonView",fail(e.getMessage()));
    	}finally{
    		if(conn != null){
    			conn.close();
    		}
    	}
    }
    
    public ModelAndView getRdLinkByPid(HttpServletRequest request) throws Exception{
    	return new ModelAndView("jsonView",success());
    }
}