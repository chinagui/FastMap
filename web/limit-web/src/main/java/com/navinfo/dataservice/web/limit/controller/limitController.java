package com.navinfo.dataservice.web.limit.controller;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.exception.DataNotChangeException;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.commons.util.ResponseUtils;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchSelector;
import com.navinfo.dataservice.engine.limit.glm.iface.IRow;
import com.navinfo.dataservice.engine.limit.glm.iface.LimitObjType;
import com.navinfo.dataservice.engine.limit.operation.Transaction;
import com.navinfo.dataservice.engine.limit.search.RenderParam;
import com.navinfo.dataservice.engine.limit.search.SearchProcess;
import com.navinfo.navicommons.database.QueryRunner;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by ly on 2017/9/14.
 */

@Controller
public class limitController extends BaseController {

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

            conn = DBConnector.getInstance().getLimitConnection();

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

    @RequestMapping(value = "/run")
    public ModelAndView run(HttpServletRequest request)
            throws ServletException, IOException {

        String parameter = request.getParameter("parameter");
        logger.info("parameter====" + parameter);
//        AccessToken tokenObj = (AccessToken) request.getAttribute("token");

        com.alibaba.fastjson.JSONObject fastJson = com.alibaba.fastjson.JSONObject
                .parseObject(parameter);

        JSONObject paraJson = JsonUtils.fastJson2netJson(fastJson);

        try {
            long beginRunTime = System.currentTimeMillis();
            logger.info("BEGIN EDIT RUN");
            Transaction t = new Transaction(parameter);
            // 加载用户ID
//            t.setUserId(tokenObj.getUserId());
            // 加载用户taskId
            if (paraJson.containsKey("subtaskId")) {
                t.setSubTaskId(paraJson.getInt("subtaskId"));
            }

            String msg = t.run();

            String log = t.getLogs();

            JSONObject json = new JSONObject();

            json.put("result", msg);

            json.put("log", log);

            json.put("check", t.getCheckLog());

            json.put("Id", t.getId());
            long endRunTime = System.currentTimeMillis();
            logger.info("END EDIT RUN");
            logger.info("edit run total use time   "
                    + String.valueOf(endRunTime - beginRunTime));
            if (parameter.contains("\"infect\":1")) {
                return new ModelAndView("jsonView", infect(json));
            } else {
                return new ModelAndView("jsonView", success(json));
            }
        } catch (DataNotChangeException e) {
            logger.error(e.getMessage(), e);

            return new ModelAndView("jsonView", success(e.getMessage()));
        } catch (Exception e) {

            logger.error(e.getMessage(), e);

            return new ModelAndView("jsonView", fail(e.getMessage()));
        }
    }

    @RequestMapping(value = "/getRegionIdByAdmin")
    public ModelAndView getRegionIdByAdiminArea(HttpServletRequest request) throws Exception {
        String parameter = request.getParameter("parameter");

        Connection conn = null;

        try {
            JSONObject jsonReq = JSONObject.fromObject(parameter);

            if (jsonReq == null || !jsonReq.containsKey("adminCode")) {
                throw new Exception("行政区划编码为空，无法获取相应大区库！");
            }

            String adminCode = jsonReq.getString("adminCode");

            conn = DBConnector.getInstance().getManConnection();

            SearchProcess p = new SearchProcess(conn);
            
            int dbId = p.searchDbId(adminCode);

            return new ModelAndView("jsonView", success(dbId));

        } catch (Exception e) {
            logger.error(e.getMessage(), e);

            return new ModelAndView("jsonView", fail(e.getMessage()));
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    @RequestMapping(value = "/getAdminPosition")
	public ModelAndView getAdminPosition(HttpServletRequest request) throws Exception {
		String parameter = request.getParameter("parameter");

		Connection conn = null;

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			if (jsonReq == null || !jsonReq.containsKey("adminCode")) {
				throw new Exception("行政区划编码为空，无法进行定位！");
			}

			String adminCode = jsonReq.getString("adminCode");

			conn = DBConnector.getInstance().getMkConnection();

			SearchProcess p = new SearchProcess(conn);
			
			JSONObject admin = p.searchAdminPosition(adminCode);

			return new ModelAndView("jsonView", success(admin));

		} catch (Exception e) {
			logger.error(e.getMessage(), e);

			return new ModelAndView("jsonView", fail(e.getMessage()));
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}
    
    @RequestMapping(value = "/searchRdLinkByName")
    public ModelAndView getRdLinkByName(HttpServletRequest request) throws Exception {
        String parameter = request.getParameter("parameter");

        Connection conn = null;

        try {
            JSONObject jsonReq = JSONObject.fromObject(parameter);

            if (jsonReq == null || !jsonReq.containsKey("dbId") || !jsonReq.containsKey("type") || !jsonReq.containsKey("condition")) {
                throw new Exception("输入信息不完善，无法查询道路link！");
            }

            int dbId = jsonReq.getInt("dbId");

            int type = jsonReq.getInt("type");

            JSONObject condition = jsonReq.getJSONObject("condition");

            conn = DBConnector.getInstance().getConnectionById(dbId);

            SearchProcess p = new SearchProcess(conn);

            JSONObject result = p.searchRdLinkDataByCondition(type, condition);

            return new ModelAndView("jsonView", success(result));

        } catch (Exception e) {
            logger.error(e.getMessage(), e);

            return new ModelAndView("jsonView", fail(e.getMessage()));
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    @RequestMapping(value = "/getByTileWithGap")
    public void getLimitByTile(HttpServletRequest request,
                             HttpServletResponse response) throws ServletException, IOException {

        String parameter = request.getParameter("parameter");
        JSONObject jsonReq = JSONObject.fromObject(parameter);

        response.getWriter().println(limitRender(jsonReq));
    }

    private String limitRender(JSONObject jsonReq) {

        try {

            JSONArray type = jsonReq.getJSONArray("types");

            RenderParam param=new RenderParam();

            param.setX(jsonReq.getInt("x"));

            param.setY(jsonReq.getInt("y"));

            param.setZ(jsonReq.getInt("z"));

            if (jsonReq.containsKey("gap")) {
                param.setGap(jsonReq.getInt("gap"));
            }

            List<LimitObjType> types = new ArrayList<>();

            for (int i = 0; i < type.size(); i++) {
                types.add(LimitObjType.valueOf(type.getString(i)));
            }

            JSONObject data = null;

            if (param.getZ() >13) {
                com.navinfo.dataservice.engine.limit.search.SearchProcess p = new com.navinfo.dataservice.engine.limit.search.SearchProcess();

                data = p.searchDataByTileWithGap(types,param);
            }
            return ResponseUtils.assembleRegularResult(data);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseUtils.assembleFailResult(e.getMessage());
        } finally {

        }
    }
    
	@RequestMapping(value = "/getByPids")
	public ModelAndView getByPids(HttpServletRequest request)
			throws ServletException, IOException {

		String parameter = request.getParameter("parameter");

		Connection conn = null;

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			String objType = jsonReq.getString("type");

			int dbId = jsonReq.getInt("dbId");

			conn = DBConnector.getInstance().getConnectionById(dbId);

			
				JSONArray pidArray = jsonReq.getJSONArray("pids");

				SearchProcess p = new SearchProcess(conn);

				List<RdLink> objList = p.searchDataByPids(pidArray);

				JSONArray array = new JSONArray();

				if (objList != null) {

					for (RdLink obj : objList) {
						JSONObject json = obj.Serialize(ObjLevel.FULL);
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