package com.navinfo.dataservice.control.row.pointaddress;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.edit.upload.EditJson;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.exception.DataNotChangeException;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.operation.OperationSegment;
import com.navinfo.dataservice.engine.editplus.operation.imp.DefaultObjImportorCommand;
import com.navinfo.dataservice.engine.editplus.operation.imp.IxPointAddressObjImportor;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 点门牌保存
 * @Title:PointAddressSave
 * @Package:com.navinfo.dataservice.control.row.pointaddress
 * @Description: 
 * @author:Jarvis 
 * @date: 2017年9月28日
 */
public class PointAddressSave {

	private static final Logger logger = LoggerRepos.getLogger(PointAddressSave.class);
	private static Integer QST = null;
	private static Integer QT = null;
	private static Integer MST = null;
	private static Integer MT = null;

	private PointAddressSave() {
	}

	private static class SingletonHolder {
		private static final PointAddressSave INSTANCE = new PointAddressSave();
	}

	public static PointAddressSave getInstance() {
		return SingletonHolder.INSTANCE;
	}

	/**
	 * 点门牌保存
	 * @param parameter
	 * @param userId
	 * @return
	 * @throws Exception 
	 */
	public JSONObject save(String parameter, long userId) throws Exception {
		Connection conn = null;
        JSONObject result = new JSONObject();
        
        try {

            JSONObject json = JSONObject.fromObject(parameter);

            int dbId = json.getInt("dbId");
            int subtaskId = json.getInt("subtaskId");
            
            conn = DBConnector.getInstance().getConnectionById(dbId);
            JSONObject poiData = json.getJSONObject("data");

            OperType operType = Enum.valueOf(OperType.class,
					json.getString("command"));

            ObjType type = Enum.valueOf(ObjType.class,
					json.getString("type"));

            if(!type.equals(ObjType.IXPOINTADDRESS)){
            	return result;
            }
           
            
			if(poiData.containsKey("longitude") && poiData.containsKey("latitude")){
				calcGeometryAndMesh(poiData, conn);
			}
            
            
            json.put("data", poiData);
            
            calcSubtaskId(subtaskId);
            
            if(operType == OperType.CREATE){
            	json.put("command", "INSERT");
            }
            
            IxPointAddressObjImportor importor = new IxPointAddressObjImportor(conn,null);
 			EditJson editJson = new EditJson();
 			editJson.addJsonPoi(json);
 			DefaultObjImportorCommand command = new DefaultObjImportorCommand(editJson);
 			importor.operate(command);
 			importor.setSubtaskId(subtaskId);
 			importor.persistChangeLog(OperationSegment.SG_COLUMN, userId);
 			OperationResult operationResult = importor.getResult();
        	long pid = operationResult.getAllObjs().get(0).getMainrow().getObjPid();
        	
 			afterOperate(operType, pid, conn, result);//新增或修改之后的操作
        	
            return result;
        } catch (DataNotChangeException e) {
            DbUtils.rollback(conn);
            logger.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            DbUtils.rollback(conn);
            logger.error(e.getMessage(), e);
            throw e;
        } finally {
            DbUtils.commitAndCloseQuietly(conn);
        }
	}
	
	/**
	 * 新增或修改之后的操作
	 * @throws Exception 
	 */
	public void afterOperate(OperType operType,long pid,Connection conn,JSONObject result) throws Exception{
		if(operType == OperType.CREATE){
        	insertDayEditStatus(pid, conn);
        	JSONArray logArray = new JSONArray();
        	JSONObject logObject = new JSONObject();
        	logObject.put("type", "IXPOINTADDRESS");
        	logObject.put("pid", pid);
        	logObject.put("childPid", "");
        	logObject.put("op", "新增");
        	logArray.add(logObject);
        	
        	result.put("log", logArray);
        	result.put("check", new JSONArray());
        	result.put("pid", pid);
        }else if(operType == OperType.UPDATE){
        	boolean isFreshVerified = isFreshVerified(pid, conn);
        	updateDayEditStatus(pid, isFreshVerified, conn);
			
        }
			
	}
	
	/**
	 * 查询对象是否鲜度认证
	 * 
	 * @param objPid
	 * @return
	 * @throws Exception
	 */
	public boolean isFreshVerified(long pid,Connection conn) throws Exception {
		String fd_lst=null;

		String sql = "SELECT de.fd_lst FROM LOG_DETAIL de WHERE de.OB_PID= :1 AND de.TB_NM='IX_POINTADDRESS'  AND de.FD_LST IS NOT NULL ";
		
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, pid);
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				  fd_lst=resultSet.getString("fd_lst");
				  String[] arrFd = fd_lst.replace("[", "").replace("]", "").split(",");
				  for(int j= 0 ; j<arrFd.length;j++){
		            	if (!"\"MEMO\"".equals(arrFd[j])){
		            		return false;
		            	}
				  }  
			}
			return true;
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
	}
	
	
	/**
	 * 新增DayEditStatus
	 * @throws Exception 
	 */
	public void insertDayEditStatus(long pid,Connection conn) throws Exception{
		PreparedStatement pstmt = null;
		String sql = "INSERT INTO POINTADDRESS_EDIT_STATUS(PID,STATUS,QUICK_SUBTASK_ID,QUICK_TASK_ID,MEDIUM_SUBTASK_ID,MEDIUM_TASK_ID) VALUES(";
		sql += ""+pid+",2,"+QST+","+QT+","+MST+","+MT+")";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.executeUpdate();
		} catch (Exception e) {
			DbUtils.rollback(conn);
            logger.error(e.getMessage(), e);
			throw e;
		} finally {
			DbUtils.closeQuietly(pstmt);
		}
	}
	
	/**
	 * 修改DayEditStatus
	 * @throws Exception 
	 */
	public void updateDayEditStatus(long pid,boolean isFreshVerified,Connection conn) throws Exception{
		PreparedStatement pstmt = null;
    	StringBuilder sb = new StringBuilder();
    	if(isFreshVerified){// 鲜度验证保存时调用
    		String condition = " QUICK_SUBTASK_ID = 0 AND QUICK_TASK_ID = 0 AND MEDIUM_SUBTASK_ID = 0 AND MEDIUM_TASK_ID = 0";
			sb.append(" UPDATE pointaddress_edit_status SET STATUS = 2 ,FRESH_VERIFIED = 1,QUICK_SUBTASK_ID = CASE WHEN "+ condition +" THEN "+QST+" ELSE QUICK_SUBTASK_ID END,");
			sb.append(" QUICK_TASK_ID = CASE WHEN"+ condition +" THEN "+QT+" ELSE QUICK_TASK_ID END,");
			sb.append(" MEDIUM_SUBTASK_ID = CASE WHEN"+ condition +" THEN "+MST+" ELSE MEDIUM_SUBTASK_ID END,");
			sb.append(" MEDIUM_TASK_ID = CASE WHEN"+ condition +" THEN "+MT+" ELSE MEDIUM_TASK_ID END WHERE pid = "+pid+"");
    	}else{
    		sb.append(" MERGE INTO POINTADDRESS_EDIT_STATUS T1 ");
    		sb.append(" USING (SELECT (CASE WHEN "+MST+" = 0 THEN T.MEDIUM_SUBTASK_ID WHEN T.STATUS IN (1, 2) AND ");
    		sb.append(" T.MEDIUM_SUBTASK_ID NOT IN (0, "+MST+") THEN T.MEDIUM_SUBTASK_ID WHEN T.STATUS = 1 AND T.MEDIUM_SUBTASK_ID = 0 THEN ");
    		sb.append(" T.MEDIUM_SUBTASK_ID ELSE "+MST+" END) MST,");
    		sb.append(" (CASE WHEN "+MT+" = 0 THEN T.MEDIUM_TASK_ID WHEN T.STATUS IN (1, 2) AND T.MEDIUM_TASK_ID NOT IN (0, "+MT+") THEN ");
    		sb.append(" T.MEDIUM_TASK_ID WHEN T.STATUS = 1 AND T.MEDIUM_TASK_ID = 0 THEN T.MEDIUM_TASK_ID ELSE "+MT+"  END) MT,");
    		sb.append(" (CASE WHEN "+QST+"  = 0 THEN T.QUICK_SUBTASK_ID WHEN T.STATUS IN (1, 2) AND T.QUICK_SUBTASK_ID NOT IN (0,"+QST+") THEN ");
    		sb.append(" T.QUICK_SUBTASK_ID WHEN T.STATUS = 1 AND T.QUICK_SUBTASK_ID = 0 THEN T.QUICK_SUBTASK_ID ELSE "+QST+" END) QST,");
    		sb.append(" (CASE WHEN "+QT+" = 0 THEN T.QUICK_TASK_ID WHEN T.STATUS IN (1, 2) AND T.QUICK_TASK_ID NOT IN (0, "+QT+") THEN ");
    		sb.append(" T.QUICK_TASK_ID WHEN T.STATUS = 1 AND T.QUICK_TASK_ID = 0 THEN T.QUICK_TASK_ID ELSE "+QT+" END) QT, ");
    		sb.append(" IX.PID AS D FROM IX_POINTADDRESS IX, POINTADDRESS_EDIT_STATUS T WHERE IX.PID = T.PID(+) AND IX.PID = "+pid+") T2 ");
    		sb.append(" ON (T1.PID = T2.D) WHEN MATCHED THEN UPDATE SET T1.STATUS = 2,T1.FRESH_VERIFIED = 0,");
    		sb.append(" T1.QUICK_SUBTASK_ID  = T2.QST,T1.QUICK_TASK_ID = T2.QT,T1.MEDIUM_SUBTASK_ID = T2.MST,T1.MEDIUM_TASK_ID = T2.MT ");
    	}
		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.executeUpdate();
		} catch (Exception e) {
			DbUtils.rollback(conn);
            logger.error(e.getMessage(), e);
			throw e;
		} finally {
			DbUtils.closeQuietly(pstmt);
		}
	}
	

	/**
	 * 计算精细化子任务号
	 * @param subtaskId
	 * @throws Exception
	 */
	public void calcSubtaskId(Integer subtaskId) throws Exception{
		 
        ManApi apiService = (ManApi) ApplicationContextUtil
				.getBean("manApi");
		Map<String, Integer> taskInfo = apiService
				.getTaskBySubtaskId(subtaskId);
		
		if (taskInfo.get("programType") == 1) {
			MST =  subtaskId;
			MT  =  taskInfo.get("taskId");
			QST = 0;
			QT  = 0;
		} else {
			MST = 0;
			MT  = 0;
			QST = subtaskId;
			QT  = taskInfo.get("taskId");
		}
		
	}
	
	/**
	 * 计算图幅号以及坐标
	 * @param poiData
	 * @param conn
	 * @throws Exception
	 */
	public void calcGeometryAndMesh(JSONObject poiData,Connection conn) throws Exception{
		JSONObject geoPoint = new JSONObject();

        geoPoint.put("type", "Point");

        geoPoint.put("coordinates", new double[]{poiData.getDouble("longitude"), poiData.getDouble("latitude")});

        poiData.remove("longitude");
        poiData.remove("latitude");
        
        // 根据经纬度计算图幅ID
        String meshIds[] = CompGeometryUtil.geo2MeshesWithoutBreak(GeoTranslator.geojson2Jts(geoPoint, 1, 5));

        if (meshIds.length > 1) {
            throw new Exception("不能在图幅线上创建行政区划代表点");
        }
        if (meshIds.length == 1) {
        	poiData.put("meshId", Integer.parseInt(meshIds[0]));
        }
        
        poiData.put("geometry", geoPoint);
        
        Geometry go  = GeoTranslator.geojson2Jts(geoPoint, 100000, 0);
        
        RdLinkSelector rdLinkSelector = new RdLinkSelector(conn);
        
        RdLink link = (RdLink) rdLinkSelector.loadByIdOnlyRdLink(poiData.getInt("guideLinkPid"), false);
        
        // 计算行政区划代表点与关联线的左右关系
        Coordinate c = GeometryUtils.GetNearestPointOnLine(GeoTranslator.transform(go, 0.00001, 5).getCoordinate(), 
        		GeoTranslator.transform(link.getGeometry(), 0.00001, 5));
        
        JSONObject geojson = new JSONObject();
        geojson.put("type", "Point");
        geojson.put("coordinates", new double[]{c.x, c.y});
        Geometry nearestPointGeo = GeoTranslator.geojson2Jts(geojson, 1, 0);
        int side = GeometryUtils.calulatPointSideOflink(go, link.getGeometry(), nearestPointGeo);

        poiData.put("guideLinkSide", side);
	}
	
	
	
	
	
}
