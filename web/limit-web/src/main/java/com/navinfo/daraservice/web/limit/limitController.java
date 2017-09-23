package com.navinfo.daraservice.web.limit;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.engine.limit.glm.iface.IRow;
import com.navinfo.dataservice.engine.limit.glm.iface.LimitObjType;
import com.navinfo.dataservice.engine.limit.search.SearchProcess;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.Connection;
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

            List<? extends IRow> objList = p.searchMetaDataByCondition(
                    LimitObjType.valueOf(objType), condition);

            JSONArray array = new JSONArray();

            if (objList != null) {

                for (IRow obj : objList) {
                    JSONObject json = obj.Serialize(ObjLevel.FULL);
                    json.put("geoLiveType", objType);
                    array.add(json);
                }
                return new ModelAndView("jsonView", success(array));
            } else {
                return new ModelAndView("jsonView", success());
            }
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

            List<? extends IRow> objList = p.searchLimitDataByCondition(
                    LimitObjType.valueOf(objType), condition);

            JSONArray array = new JSONArray();

            if (objList != null) {
                for (IRow obj : objList) {
                    JSONObject json = obj.Serialize(ObjLevel.FULL);
                    json.put("geoLiveType", objType);
                    array.add(json);
                }
                return new ModelAndView("jsonView", success(array));
            } else {
                return new ModelAndView("jsonView", success());
            }
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
}