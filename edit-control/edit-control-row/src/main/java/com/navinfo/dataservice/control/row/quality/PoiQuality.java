package com.navinfo.dataservice.control.row.quality;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONArray;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.control.dealership.service.utils.DealerShipConstantField;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiRestaurant;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiAddress;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiChildren;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiContact;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiName;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiParent;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;
import com.navinfo.dataservice.dao.log.LogReader;
import com.navinfo.dataservice.engine.editplus.diff.StringUtil;
import com.navinfo.navicommons.database.QueryRunner;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

public class PoiQuality {
	private static final Logger logger = Logger.getLogger(PoiQuality.class);

	public void initQualityData() throws Exception {

		List<Map<String, Object>>  list = getGeometryAndSubtaskId();//根据质检圈的的qualityId,获取质检圈geometry
		logger.info("开始初始化poi_count_table表----");
		for (Map<String, Object> map : list) {
			Connection conn = null;
			try {
				
				String geometry = (String) map.get("geometry");
				String subtaskId =(String) map.get("subtaskId");
				int qualityId = (int)map.get("qualityId");
				int dbId = (int)map.get("dbId");
				conn = DBConnector.getInstance().getConnectionById(dbId);
				List<Integer> pidList = getPidList(geometry,conn);//查询质检圈内的所有poi
				IxPoiSelector poiSelector = new IxPoiSelector(conn);
				
				for (Integer pid : pidList) {

					IxPoi poi = (IxPoi) poiSelector.loadAllById(pid, false);
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("FID", poi.getPoiNum());
					jsonObject.put("\"LEVEL\"", "null");
					jsonObject.put("POI_NAME", "null");
					jsonObject.put("MESH_ID", "null");
					jsonObject.put("AREA", "null");
					jsonObject.put("CATEGORY", "null");
					
					LogReader logReader = new LogReader(conn);
					int state = logReader.getObjectState(poi.pid(), "IX_POI");
					if(state==2){
						jsonObject.put("EXTRA", "DB统计");
					}else{
						jsonObject.put("EXTRA", "0");
					}
					jsonObject.put("MISSING","0");
					
					/**
					 * 统计项相关信息赋值
					 */
					setNameStatisticsByPoi(jsonObject, poi,null,true);//赋值名称统计项
					setPositionStatisticsByPoi(jsonObject, poi,null,true);//赋值点位统计项
					setCategoryStatisticsByPoi(jsonObject, poi,null,true);//赋值分类统计项
					setAddressStatisticsByPoi(jsonObject, poi,null,true);//赋值地址统计项
					setPhoteStatisticsByPoi(jsonObject, poi,null,true);//赋值电话统计项
					setFatherSonStatisticsByPoi(jsonObject, poi,conn,null,true);//赋值父子关系统计项
					setDeepStatisticsByPoi(jsonObject, poi,conn,null,true);//赋值深度信息统计项
					setLebelStatisticsByPoi(jsonObject, poi,null,true);//赋值Lebel统计项
					setResturantStatisticsByPoi(jsonObject, poi,null,true);//赋值餐厅统计项
					setLinkStatisticsByPoi(jsonObject, poi,null,true);//赋值LINK_PID统计项
					setLevelStatisticsByPoi(jsonObject, poi,null,true);//赋值Level统计项
					
					
					jsonObject.put("COLLECTOR_USERID","null");
					jsonObject.put("COLLECTOR_TIME","null");
					jsonObject.put("INPUT_USERID","null");
					jsonObject.put("INPUT_TIME","null");
					jsonObject.put("QC_USERID","null");
					jsonObject.put("QC_TIME","null");
					jsonObject.put("QC_SUB_TASKID",subtaskId);
					jsonObject.put("VISION", SystemConfigFactory.getSystemConfig().getValue(PropConstant.seasonVersion));
					jsonObject.put("MEMO","null");
					jsonObject.put("MEMO_USERID","null");
					jsonObject.put("HAS_EXPORT", "0");
					
					initPoiColumnTable(jsonObject);//初始化poi_count_table
					
					updateSubtaskQualityDbstat(qualityId);//更新subtask_quality DB统计状态为1

					logger.info(pid+"-------------"+jsonObject);
				}
				
				
				logger.info("初始化poi_count_table表完成----");
			}catch (Exception e) {
				logger.error(e.getMessage(), e);
				throw e;
			} finally {
				DbUtils.commitAndCloseQuietly(conn);
			}
		}
		
	}
	
	/**
	 * 初始化完成后更新DB统计状态
	 * @param qualityId
	 * @throws Exception 
	 */
	private void updateSubtaskQualityDbstat(int qualityId) throws Exception {
		Connection conn = null;
		PreparedStatement pstmt = null;
        try {
        	
        	conn = DBConnector.getInstance().getManConnection();
        	
        	String sql="UPDATE SUBTASK_QUALITY SET POI_DB_STAT = 1 WHERE QUALITY_ID = "+qualityId;

            pstmt = conn.prepareStatement(sql);

            pstmt.executeUpdate();
        } catch (Exception e) {
        	logger.error(e.getMessage(), e);
			throw e;
		} finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
		
	}

	/**
	 * 查询已发布未统计的质检圈
	 * @return
	 * @throws Exception 
	 */
	private List<Map<String, Object>> getGeometryAndSubtaskId() throws Exception {
		Connection conn = null;
		
		try{
			conn = DBConnector.getInstance().getManConnection();
			
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			StringBuffer sb = new StringBuffer();
			sb.append("SELECT ST.SUBTASK_ID,R.DAILY_DB_ID,sq.geometry,sq.quality_id FROM SUBTASK ST,TASK T,REGION R,subtask_quality sq ");
			sb.append(" WHERE ST.TASK_ID = T.TASK_ID AND T.REGION_ID = R.REGION_ID AND sq.subtask_id = st.subtask_id ");
			sb.append(" AND st.quality_plan_status = 1 AND st.is_quality = 1 AND sq.poi_db_stat = 0");
			
			pstmt = conn.prepareStatement(sb.toString());
			rs = pstmt.executeQuery();
			
			List<Map<String, Object>> list = new ArrayList<>();
			while (rs.next()) {
				Map<String, Object> map = new HashMap<>();
				map.put("subtaskId", rs.getString("SUBTASK_ID"));
				map.put("dbId", rs.getInt("DAILY_DB_ID"));
				String geometry = "";
				STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
				try {
					geometry =  GeoTranslator.struct2Wkt(struct);
				} catch (Exception e) {
					e.printStackTrace();
				}
				map.put("geometry", geometry);
				map.put("qualityId", rs.getInt("QUALITY_ID"));
				list.add(map);
			}
			return list;
		}catch(Exception e){
			logger.error(e.getMessage(), e);
			throw e;
		}finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}


	/**
	 * 初始化POI_COUNT_TABLE表
	 * @param conn
	 * @param jsonObject
	 * @throws Exception 
	 */
	public void initPoiColumnTable(JSONObject jsonObject) throws Exception{
		Connection conn = null;
		PreparedStatement pstmt = null;
        try {
        	conn = DBConnector.getInstance().getCheckConnection();
        	Iterator iterator = jsonObject.keys();
        	StringBuffer sb = new StringBuffer();
        	StringBuffer sb1 = new StringBuffer();
        	StringBuffer sb2 = new StringBuffer();
        	String key = "";
        	String value = "";
        	sb.append("INSERT INTO POI_COUNT_TABLE(");
        	while(iterator.hasNext()){
        		 key = (String) iterator.next();
        	     value = jsonObject.getString(key);
        	     sb1.append(""+key+",");
        	     sb2.append("'"+value+"',");
        	}
        	sb1.deleteCharAt(sb1.length() - 1);
        	sb2.deleteCharAt(sb2.length() - 1);
        	sb.append(sb1).append(")VALUES(");
        	sb.append(sb2).append(")");
        	
        	logger.info("sql----------"+sb.toString());

            pstmt = conn.prepareStatement(sb.toString());

            pstmt.executeUpdate();
        } catch (Exception e) {
        	logger.error(e.getMessage(), e);
			throw e;
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	/**
	 * 赋值Level统计项
	 * @param jsonObject
	 * @param poi
	 */
	private void setLevelStatisticsByPoi(JSONObject jsonObject, IxPoi poi,String dataUnmodified,boolean beforeCheck) {
		String level = poi.getLevel();
		String levelInfo = "null";
		boolean levelFlag = false;
		if(StringUtils.isNotBlank(level)){
			levelInfo = level;
			levelFlag = true;
		}

		if(beforeCheck){
			jsonObject.put("LEVEL_DATA_UNMODIFIED", levelInfo);
			if(levelFlag){
				jsonObject.put("LEVEL_DB_COUNT", "1");
			}else{
				jsonObject.put("LEVEL_DB_COUNT", "0");
			}
			setNullDataByType("LEVEL", jsonObject);
			
		}else{
			jsonObject.put("LEVEL_DATA_MODIFIED", levelInfo);
			if(levelFlag){
				jsonObject.put("LEVEL_SITE_COUNT", "1");
			}else{
				jsonObject.put("LEVEL_SITE_COUNT", "0");
			}
			if(dataUnmodified.equals(levelInfo)){
				jsonObject.put("LEVEL_MODIFY", "0");
			}else {
				jsonObject.put("LEVEL_MODIFY", "1");
			}
		}
		
	}

	/**
	 * 赋值LINK_PID统计项
	 * @param jsonObject
	 * @param poi
	 */
	private void setLinkStatisticsByPoi(JSONObject jsonObject, IxPoi poi,String dataUnmodified,boolean beforeCheck) {
		Integer linkPid = poi.getLinkPid();
		String link = "null";
		boolean linkFlag = false;
		if(linkPid!=null){
			link = linkPid.toString();
			linkFlag = true;
		}
		if(beforeCheck){
			if(linkFlag){
				jsonObject.put("LINK_DB_COUNT", "1");
			}else{
				jsonObject.put("LINK_DB_COUNT", "0");
			}
			
			jsonObject.put("LINK_DATA_UNMODIFIED", link);
			setNullDataByType("LINK", jsonObject);
		}else{
			if(linkFlag){
				jsonObject.put("LINK_SITE_COUNT", "1");
			}else{
				jsonObject.put("LINK_SITE_COUNT", "0");
			}
			jsonObject.put("LINK_DATA_MODIFIED", link);
			if (dataUnmodified.equals(link)) {
				jsonObject.put("LINK_MODIFY", "0");
			}else{
				jsonObject.put("LINK_MODIFY", "1");
			}
		}
		
		
		
	}

	/**
	 * 赋值餐厅统计项
	 * @param jsonObject
	 * @param poi
	 */
	private void setResturantStatisticsByPoi(JSONObject jsonObject, IxPoi poi,String dataUnmodified,boolean beforeCheck) {
		boolean resturantFlag = false;
		JSONArray jsonArray = new JSONArray();
		String resturant = "null";
		if(poi.getRestaurants()!=null&&poi.getRestaurants().size()>0){
			List<IRow> resturants = poi.getRestaurants();
			for (IRow iRow : resturants) {
				IxPoiRestaurant addressTmp = (IxPoiRestaurant)iRow;
				JSONObject jo =  JSONObject.fromObject(addressTmp);   
				jsonArray.add(jo);
				resturantFlag = true;
			}
			resturant = jsonArray.toJSONString();
		}
		
		if(beforeCheck){
			jsonObject.put("RESTURANT_DATA_UNMODIFIED", resturant);
			if(resturantFlag){
				jsonObject.put("RESTURANT_DB_COUNT", "1");
			}else{
				jsonObject.put("RESTURANT_DB_COUNT", "0");
			}
			setNullDataByType("RESTURANT", jsonObject);
		}else {
			jsonObject.put("RESTURANT_DATA_MODIFIED", resturant);
			if(resturantFlag){
				jsonObject.put("RESTURANT_SITE_COUNT", "1");
			}else{
				jsonObject.put("RESTURANT_SITE_COUNT", "0");
			}
			if(dataUnmodified.equals(resturant)){
				jsonObject.put("RESTURANT_MODIFY", "0");
			}else{
				jsonObject.put("RESTURANT_MODIFY", "1");
			}
		}
		
		
		
	}


	/**
	 * 赋值Lebel统计项
	 * @param jsonObject
	 * @param poi
	 */
	private void setLebelStatisticsByPoi(JSONObject jsonObject, IxPoi poi,String dataUnmodified,boolean beforeCheck) {
		String label = poi.getLabel();
		String labelInfo = "null";
		boolean labelFlag = false;
		if(StringUtils.isNotBlank(label)){
			labelInfo = label;
			labelFlag = true;
		}
		if(beforeCheck){
			jsonObject.put("LABEL_DATA_UNMODIFIED", labelInfo);
			if(labelFlag){
				jsonObject.put("LABEL_DB_COUNT", "1");
			}else{
				jsonObject.put("LABEL_DB_COUNT", "0");
			}
			setNullDataByType("LABEL", jsonObject);
		}else{
			jsonObject.put("LABEL_DATA_MODIFIED", labelInfo);
			if(StringUtils.isBlank(label)){
				jsonObject.put("LABEL_SITE_COUNT", "0");
			}else{
				jsonObject.put("LABEL_SITE_COUNT", "1");
			}
			if(dataUnmodified.equals(labelInfo)){
				jsonObject.put("LABEL_MODIFY", "0");
			}else{
				jsonObject.put("LABEL_MODIFY", "1");
			}
		}
		
	}

	
	public List<String> assembleDeepSqlListByPid(int pid){
		List<String> sqlList = new ArrayList<>();
		String sql1  =  "select * from ix_poi_hotel where poi_pid = "+pid+" order by hotel_id";
		String sql2  =  "select * from ix_poi_building where poi_pid = "+pid+" order by poi_pid";
		String sql3  =  "select * from ix_poi_gasstation where poi_pid = "+pid+" order by gasstation_id";
		String sql4  =  "select * from ix_poi_chargingstation where poi_pid = "+pid+" order by charging_id";
		String sql5  =  "select * from ix_poi_chargingplot where poi_pid = "+pid+" order by poi_pid";
		String sql6  =  "select * from ix_poi_parking where poi_pid = "+pid+" order by parking_id";
		sqlList.add(sql1);
		sqlList.add(sql2);
		sqlList.add(sql3);
		sqlList.add(sql4);
		sqlList.add(sql5);
		sqlList.add(sql6);
		return sqlList;
	}
	
	

	/**
	 * 赋值深度信息统计项
	 * @param jsonObject
	 * @param poi
	 * @param conn
	 * @throws Exception
	 */
	private void setDeepStatisticsByPoi(JSONObject jsonObject, IxPoi poi,Connection conn,String dataUnmodified,boolean beforeCheck) throws Exception {
		int pid = poi.getPid();
		JSONArray data = new JSONArray();
		String deep = "null";
		boolean deepFlag = false;
		try{

			List<String> sqlList = assembleDeepSqlListByPid(pid);
			for (String sql:sqlList) {
				data = setDeepInfoBySql(sql,conn);
				if(!data.isEmpty()){
					deep = data.toJSONString();
					deepFlag = true;
					break;
				}
			}
			
			if (beforeCheck) {
				jsonObject.put("DEEP_DATA_UNMODIFIED", deep);
				if(deepFlag){
					jsonObject.put("DEEP_DB_COUNT", "1");
				}else{
					jsonObject.put("DEEP_DB_COUNT", "0");
				}
				setNullDataByType("DEEP", jsonObject);
			}else{
				jsonObject.put("DEEP_DATA_MODIFIED", deep);
				if(deepFlag){
					jsonObject.put("DEEP_SITE_COUNT", "1");
				}else{
					jsonObject.put("DEEP_SITE_COUNT", "0");
				}
				if(dataUnmodified.equals(deep)){
					jsonObject.put("DEEP_MODIFY","0");
				}else{
					jsonObject.put("DEEP_MODIFY","1");
				}
			}
			
			
		}catch(Exception e){
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	public StringBuffer contactFatherSonStatisticsByPoi(IxPoi poi,Connection conn) throws Exception{
		StringBuffer sb = new StringBuffer();
		if(poi.getParents()!=null&&poi.getParents().size()>0){
			List<IRow> parents = poi.getParents();
			sb.append((selectPoiNumByPid(conn,((IxPoiParent)parents.get(0)).getParentPoiPid()))).append("|");
		}
		if(poi.getChildren()!=null&&poi.getChildren().size()>0){
			List<IRow> childrens = poi.getChildren();
			for (IRow iRow : childrens) {
				sb.append((selectPoiNumByPid(conn,((IxPoiChildren)iRow).getChildPoiPid()))).append("|");
			}
		}
		return sb;
	}
	

	/**
	 * 赋值父子关系统计项
	 * @param jsonObject
	 * @param poi
	 * @param conn
	 * @throws Exception
	 */
	private void setFatherSonStatisticsByPoi(JSONObject jsonObject, IxPoi poi,Connection conn,String dataUnmodified,boolean beforeCheck) throws Exception {
		StringBuffer sb = contactFatherSonStatisticsByPoi(poi, conn);
		String fatherSon = "null";
		boolean fatherSonFlag = false;
		if(sb.length()>0){
			fatherSon = sb.toString().substring(0, sb.toString().length() - 1);
			fatherSonFlag = true;
		}
		if(beforeCheck){
			if(fatherSonFlag){
				jsonObject.put("FATHER_SON_DB_COUNT", "1");
			}else{
				jsonObject.put("FATHER_SON_DB_COUNT", "0");
			}
			jsonObject.put("FATHER_SON_DATA_UNMODIFIED",fatherSon);
			setNullDataByType("FATHER_SON", jsonObject);
		}else{
			if(fatherSonFlag){
				jsonObject.put("FATHER_SON_SITE_COUNT", "1");
			}else{
				jsonObject.put("FATHER_SON_SITE_COUNT", "0");
			}
			jsonObject.put("FATHER_SON_DATA_MODIFIED",fatherSon);
			if(dataUnmodified.equals(fatherSon)){
				jsonObject.put("FATHER_SON_MODIFY","0");
			}else{
				jsonObject.put("FATHER_SON_MODIFY","1");
			}
		}
		
	}


	/**
	 * 赋值电话统计项
	 * @param jsonObject
	 * @param poi
	 */
	private void setPhoteStatisticsByPoi(JSONObject jsonObject, IxPoi poi,String dataUnmodified,boolean beforeCheck) {
		String telephone = "null";
		boolean photeFlag = false;
		if(poi.getContacts()!=null&&poi.getContacts().size()>0){
			telephone  = contactTelephoneByPoi(poi);
			photeFlag = true;
		}
		
		if(beforeCheck){
			jsonObject.put("PHOTE_DATA_UNMODIFIED",telephone);
			if(photeFlag){
				jsonObject.put("PHOTE_DB_COUNT", "1");
			}else{
				jsonObject.put("PHOTE_DB_COUNT", "0");
			}
			setNullDataByType("PHOTE", jsonObject);
		}else{
			jsonObject.put("PHOTE_DATA_MODIFIED",telephone);
			if(photeFlag){
				jsonObject.put("PHOTE_SITE_COUNT", "1");
			}else{
				jsonObject.put("PHOTE_SITE_COUNT", "0");
			}
			if(dataUnmodified.equals(telephone)){
				jsonObject.put("PHOTE_MODIFY","0");
			}else{
				jsonObject.put("PHOTE_MODIFY","1");
			}
		}
		
	}

	/**
	 * 根据poi封装telephone
	 * @param poi
	 * @return
	 */
	public String contactTelephoneByPoi(IxPoi poi){
		String telephone = "";
		StringBuffer sb = new StringBuffer();
		List<IRow> contacts = poi.getContacts();
		for (IRow iRow : contacts) {
			sb.append(((IxPoiContact)iRow).getContact()).append(";");
		}
		if (sb.length() > 0){
			telephone = sb.toString().substring(0, sb.toString().length() - 1);
			telephone = StringUtil.sortPhone(telephone);
			telephone = telephone.replace(";", "|");
			telephone = telephone.substring(0, telephone.length() - 1);
		}
		return telephone;
	}
	
	/**
	 * 赋值地址统计项
	 * @param jsonObject
	 * @param poi
	 */
	private void setAddressStatisticsByPoi(JSONObject jsonObject, IxPoi poi,String dataUnmodified,boolean beforeCheck) {

		boolean addressFlag = false;
		String address = "null";
		if(poi.getAddresses()!=null&&poi.getAddresses().size()>0){
			List<IRow> addresses = poi.getAddresses();
			for (IRow iRow : addresses) {
				IxPoiAddress addressTmp = (IxPoiAddress)iRow;
				if(addressTmp.getLangCode().equals("CHI")){
					address = addressTmp.getFullname();
					addressFlag = true;
				}
			}
		}
		
		if(beforeCheck){
			jsonObject.put("ADDRESS_DATA_UNMODIFIED", address);
			if(addressFlag){
				jsonObject.put("ADDRESS_DB_COUNT", "1");
			}else{
				jsonObject.put("ADDRESS_DB_COUNT", "0");
			}
			setNullDataByType("ADDRESS", jsonObject);
		}else{
			jsonObject.put("ADDRESS_DATA_MODIFIED", address);
			if(addressFlag){
				jsonObject.put("ADDRESS_SITE_COUNT", "1");
			}else{
				jsonObject.put("ADDRESS_SITE_COUNT", "0");
			}
			if(address.equals(dataUnmodified)){
				jsonObject.put("ADDRESS_MODIFY", "0");
			}else{
				jsonObject.put("ADDRESS_MODIFY", "1");
			}
		}
		
		
	}


	/**
	 * 赋值分类统计项
	 * @param jsonObject
	 * @param poi
	 */
	private void setCategoryStatisticsByPoi(JSONObject jsonObject, IxPoi poi,String dataUnmodified,boolean beforeCheck) {

		String kindCode = poi.getKindCode();
		String kindCodeData = "null";
		boolean kindCodeFlag = false;
		if(StringUtils.isNotBlank(kindCode)){
			kindCodeData = kindCode;
			kindCodeFlag = true;
		}
		if(beforeCheck){
			jsonObject.put("CATEGORY_DATA_UNMODIFIED", kindCodeData);
			if(kindCodeFlag){
				jsonObject.put("CATEGORY_DB_COUNT", "1");
			}else{
				jsonObject.put("CATEGORY_DB_COUNT", "0");
			}
			setNullDataByType("CATEGORY", jsonObject);
			
		}else{
			jsonObject.put("CATEGORY_DATA_MODIFIED", kindCodeData);
			if(kindCodeFlag){
				jsonObject.put("CATEGORY_SITE_COUNT", "1");
			}else{
				jsonObject.put("CATEGORY_SITE_COUNT", "0");
			}
			if(dataUnmodified.equals(kindCodeData)){
				jsonObject.put("CATEGORY_MODIFY", "0");
			}else{
				jsonObject.put("CATEGORY_MODIFY", "1");
			}
		}
		
		
	}


	/**
	 * 赋值点位统计项
	 * @param jsonObject
	 * @param poi
	 */
	private void setPositionStatisticsByPoi(JSONObject jsonObject, IxPoi poi,String dataUnmodified,boolean beforeCheck) {
		Geometry geometry = poi.getGeometry();
		geometry = GeoTranslator.transform(geometry, 0.00001, 5);
		Coordinate coordinate= geometry.getCoordinate();
		double x = coordinate.x;
		double y = coordinate.y;
		
		JSONObject jo = new JSONObject();
		jo.put("X", x);
		jo.put("Y", y);
		if(beforeCheck){
			jsonObject.put("POSITION_DB_COUNT", "1");
			jsonObject.put("POSITION_DATA_UNMODIFIED", jo);
			setNullDataByType("POSITION", jsonObject);
		}else{
			jsonObject.put("POSITION_SITE_COUNT", "1");
			jsonObject.put("POSITION_DATA_MODIFIED", jo);
			if(dataUnmodified.equals(jo.toString())){
				jsonObject.put("POSITION_MODIFY", "0");
			}else{
				jsonObject.put("POSITION_MODIFY", "1");
			}
		}
		
		
	}



	/**
	 * 根据geometry扩圈
	 * @param geometry
	 * @param dbId
	 * @return
	 * @throws Exception
	 */
	private List<Integer> getPidList(String geometry, Connection conn) throws Exception {
		try{
			
			PreparedStatement pstmt = null;
			ResultSet resultSet = null;
			String sql = "SELECT pid FROM ix_poi WHERE sdo_within_distance(geometry,sdo_geometry(:1,8307),'mask=anyinteract') = 'TRUE'"
					+ " and u_record <> 2 ";
			
			pstmt = conn.prepareStatement(sql);
			Clob geoClob =ConnectionUtil.createClob(conn);
			geoClob.setString(1, geometry);
			pstmt.setClob(1, geoClob);
			
			resultSet = pstmt.executeQuery();
			
			List<Integer> pidList = new ArrayList<>();
			while (resultSet.next()) {
				pidList.add(resultSet.getInt(1));
			}
			return pidList;
		}catch(Exception e){
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	
	
	public Map<String, String> getGeometryByQualityId(int qualityId) throws Exception {
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			
			QueryRunner run = new QueryRunner();
			String sql = "SELECT GEOMETRY,SUBTASK_ID FROM SUBTASK_QUALITY WHERE QUALITY_ID  = "+qualityId;
			ResultSetHandler<Map<String, String>> rs = new ResultSetHandler<Map<String, String>>(){
				public Map<String, String> handle(ResultSet rs) throws SQLException {
				    Map<String, String> map = new HashMap<>();
					if(rs.next()){
						String geometry = "";
						STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
						try {
							geometry =  GeoTranslator.struct2Wkt(struct);
						} catch (Exception e) {
							e.printStackTrace();
						}
						map.put("subtaskId", rs.getString("SUBTASK_ID"));
						map.put("geometry", geometry);
						return map;
					}
					return null;
				}
			};
			return run.query(conn, sql, rs);
		}catch(Exception e){
			logger.error(e.getMessage(), e);
			throw e;
		}finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	
	/**
	 * 赋值名称统计项
	 * @param jsonObject
	 * @param poi
	 * @param dataUnmodified
	 * @param beforeCheck true 质检前，false 质检后
	 */
	private void setNameStatisticsByPoi(JSONObject jsonObject,IxPoi poi,String dataUnmodified,boolean beforeCheck){
		
		boolean nameFlag = false;
		String name = "null";
		if(poi.getNames()!=null&&poi.getNames().size()>0){
			List<IRow> names = poi.getNames();
			for (IRow iRow : names) {
				IxPoiName nameTmp = (IxPoiName)iRow;
				if(nameTmp.getLangCode().equals("CHI")&&nameTmp.getNameType()==2
						&&nameTmp.getNameClass()==1){
					name = nameTmp.getName();
					nameFlag = true;
				}
			}
		}
		
		if(beforeCheck){
			setNullDataByType("NAME", jsonObject);
			jsonObject.put("NAME_DATA_UNMODIFIED", name);
			if(nameFlag){
				jsonObject.put("NAME_DB_COUNT", "1");
			}else{
				jsonObject.put("NAME_DB_COUNT", "0");
			}
		}else{
			jsonObject.put("POI_NAME", name);
			jsonObject.put("NAME_DATA_MODIFIED", name);
			if(nameFlag){
				jsonObject.put("NAME_SITE_COUNT", "1");
			}else{
				jsonObject.put("NAME_SITE_COUNT", "0");
			}
			
			if(name.equals(dataUnmodified)){
				jsonObject.put("NAME_MODIFY", "0");
			}else{
				jsonObject.put("NAME_MODIFY", "1");
			}
		}
	}
	
	/**
	 * 根据pid找poiNum
	 * @param conn
	 * @param pid
	 * @return
	 * @throws Exception 
	 */
	public String selectPoiNumByPid(Connection conn,int pid) throws Exception{
		try{
			PreparedStatement pstmt = null;
			ResultSet resultSet = null;
			
			String sql = "SELECT poi_num FROM ix_poi WHERE pid = "+pid;
			pstmt = conn.prepareStatement(sql);
			
			resultSet = pstmt.executeQuery();
			
			if(resultSet.next()) {
				return resultSet.getString(1);
			}
			
			return null;
		}catch(Exception e){
			logger.error(e.getMessage(), e);
			throw e;
		}
	}
	
	
	@SuppressWarnings("unchecked")		
	public static JSONArray convertResultSetToJson(ResultSet resultSet) throws SQLException
	{
		if(resultSet == null)
		return null;
	
		JSONArray json = new JSONArray();
		ResultSetMetaData metadata = resultSet.getMetaData();
		int numColumns = metadata.getColumnCount();
		
		while(resultSet.next()) 			
		{
			JSONObject obj = new JSONObject();		
			for (int i = 1; i <= numColumns; ++i) 			
			{
				String column_name = metadata.getColumnName(i);
				if(column_name.equalsIgnoreCase("row_id")){
					obj.put(column_name, resultSet.getString("row_id"));	
				}else{
					obj.put(column_name, resultSet.getObject(column_name));
				}
			}
			json.add(obj);
		}
		return json;
	
	}
	
	/**
	 * 根据sql赋值深度信息
	 * @param sql
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	public JSONArray setDeepInfoBySql(String sql,Connection conn) throws Exception{
		PreparedStatement pstmt = conn.prepareStatement(sql);
		ResultSet resultSet =  pstmt.executeQuery();
		return convertResultSetToJson(resultSet);
	}
	
	public void setNullDataByType(String type,JSONObject jsonObject){
		jsonObject.put(""+type+"_SITE_COUNT", "null");
		jsonObject.put(""+type+"_MODIFY", "null");
		jsonObject.put(""+type+"_DATA_MODIFIED", "null");
	}
	
	/**
	 * 提交时更新countTable
	 * @param pidList
	 * @param conn
	 * @throws Exception
	 */
	public void releaseUpdateCountTable(JobInfo jobInfo,Connection conn,List<Integer> pidList) throws Exception {
		Connection checkConn = null;
		IxPoiSelector poiSelector = new IxPoiSelector(conn);
		try {
			logger.debug("start releaseUpdateCountTable");
			checkConn = DBConnector.getInstance().getCheckConnection();
			ManApi apiService = (ManApi) ApplicationContextUtil.getBean("manApi");
			long subtaskId = jobInfo.getTaskId();
			logger.debug("subtaskId----------"+subtaskId);
			Subtask subtask = apiService.queryBySubtaskId((int)subtaskId);
			if(subtask==null||subtask.getIsQuality()==0){return;}
			long userId = jobInfo.getUserId();
			logger.debug("pidList----------"+pidList);
			
			for (Integer pid : pidList) {

				IxPoi poi = (IxPoi) poiSelector.loadAllById(pid, false);
				Map<String, String> countTableInfoMap = getCountTableInfoByFid(poi.getPoiNum(),checkConn);
				String extra = countTableInfoMap.get("EXTRA");
				String nameDataUnmodified = countTableInfoMap.get("NAME_DATA_UNMODIFIED")==null?"null":countTableInfoMap.get("NAME_DATA_UNMODIFIED");
				String positionDataUnmodified = countTableInfoMap.get("POSITION_DATA_UNMODIFIED")==null?"null":countTableInfoMap.get("POSITION_DATA_UNMODIFIED");
				String categoryDataUnmodified = countTableInfoMap.get("CATEGORY_DATA_UNMODIFIED")==null?"null":countTableInfoMap.get("CATEGORY_DATA_UNMODIFIED");
				String addressDataUnmodified = countTableInfoMap.get("ADDRESS_DATA_UNMODIFIED")==null?"null":countTableInfoMap.get("ADDRESS_DATA_UNMODIFIED");
				String photeDataUnmodified = countTableInfoMap.get("PHOTE_DATA_UNMODIFIED")==null?"null":countTableInfoMap.get("PHOTE_DATA_UNMODIFIED");
				String fatherSonDataUnmodified = countTableInfoMap.get("FATHER_SON_DATA_UNMODIFIED")==null?"null":countTableInfoMap.get("FATHER_SON_DATA_UNMODIFIED");
				String deepDataUnmodified = countTableInfoMap.get("DEEP_DATA_UNMODIFIED")==null?"null":countTableInfoMap.get("DEEP_DATA_UNMODIFIED");
				String labelDataUnmodified = countTableInfoMap.get("LABEL_DATA_UNMODIFIED")==null?"null":countTableInfoMap.get("LABEL_DATA_UNMODIFIED");
				String resturantDataUnmodified = countTableInfoMap.get("RESTURANT_DATA_UNMODIFIED")==null?"null":countTableInfoMap.get("RESTURANT_DATA_UNMODIFIED");
				String linkDataUnmodified = countTableInfoMap.get("LINK_DATA_UNMODIFIED")==null?"null":countTableInfoMap.get("LINK_DATA_UNMODIFIED");
				String levelDataUnmodified = countTableInfoMap.get("LEVEL_DATA_UNMODIFIED")==null?"null":countTableInfoMap.get("LEVEL_DATA_UNMODIFIED");
				
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("\"LEVEL\"", StringUtils.isNotBlank(poi.getLevel())?poi.getLevel():"null");
				jsonObject.put("MESH_ID", poi.getMeshId()!=0?poi.getMeshId()+"":"null");
				jsonObject.put("CATEGORY", StringUtils.isNotBlank(poi.getKindCode())?poi.getKindCode():"null");
				
				boolean hasProblem = hasProblemByFid(poi.getPoiNum(),checkConn);
				
				if(hasProblem){
					jsonObject.put("FULL_MATCH", "0");
					jsonObject.put("PARTIAL_MATCH", "1");
				}else{
					jsonObject.put("FULL_MATCH", "1");
					jsonObject.put("PARTIAL_MATCH", "0");
				}
				
				LogReader logReader = new LogReader(conn);
				int state = logReader.getObjectState(poi.pid(), "IX_POI");
				if(state==2){
					if(extra==null||extra.equals("0")){
						jsonObject.put("EXTRA", "1");
					}else if(extra.equals("DB统计")){
						jsonObject.put("EXTRA", "0");
					}
				}else{
					jsonObject.put("EXTRA", "0");
				}
				
				JSONObject jo = QualityService.getInstance().queryInitValueForProblem(userId, pid, (int)subtaskId);
				long usId = jo.getLong("usId");
				String collectorTimeString = jo.getString("collectorTime");
				String collectorTime = "null";
				if(StringUtils.isNotBlank(collectorTimeString)){
					Date date = DateUtils.parse(collectorTimeString, "yyyy.MM.dd");
					collectorTime = DateUtils.format(date, "yyyyMMddHHmmss");
				}
				
				boolean existRecord = true;
				if(state==1){
					int count = existRecordInPoiCountTable(poi.getPoiNum(), checkConn);
					if(count==0&&usId==0){
						existRecord = false;
					}
				}
				
				if(!existRecord){
					jsonObject.put("MISSING","1");
				}
				
				setNameStatisticsByPoi(jsonObject, poi, nameDataUnmodified,false);//赋值名称统计项
				setPositionStatisticsByPoi(jsonObject, poi, positionDataUnmodified,false);//赋值点位统计项
				setCategoryStatisticsByPoi(jsonObject, poi, categoryDataUnmodified,false);//赋值分类统计项
				setAddressStatisticsByPoi(jsonObject, poi, addressDataUnmodified,false);//赋值地址统计项
				setPhoteStatisticsByPoi(jsonObject, poi, photeDataUnmodified,false);//赋值电话统计项
				setFatherSonStatisticsByPoi(jsonObject, poi,conn,fatherSonDataUnmodified,false);//赋值父子关系统计项
				setDeepStatisticsByPoi(jsonObject, poi,conn,deepDataUnmodified,false);//赋值深度信息统计项
				setLebelStatisticsByPoi(jsonObject, poi,labelDataUnmodified,false);//赋值Lebel统计项
				setResturantStatisticsByPoi(jsonObject, poi,resturantDataUnmodified,false);//赋值餐厅统计项
				setLinkStatisticsByPoi(jsonObject, poi,linkDataUnmodified,false);//赋值LINK_PID统计项
				setLevelStatisticsByPoi(jsonObject, poi,levelDataUnmodified,false);//赋值Level统计项
				
				
				jsonObject.put("COLLECTOR_USERID", usId==0?"AAA":usId+"");
				jsonObject.put("COLLECTOR_TIME", collectorTime);
				jsonObject.put("QC_USERID", userId+"");
				jsonObject.put("QC_TIME", DateUtils.format(new Date(), "yyyyMMddHHmmss"));
				
				Subtask convenSubtask = apiService.queryBySubTaskIdAndIsQuality((int)subtaskId, "0", 1);
				if(convenSubtask!=null){
					jsonObject.put("MEMO_USERID", usId==0?convenSubtask.getExeUserId()+"":usId+"");
				}
				
				if(existRecord){//更新
					updatePoiCountTable(jsonObject,poi.getPoiNum(),checkConn);
				}else{//插入
					qualityInsertPoiCountTable(jsonObject, poi.getPoiNum(),subtaskId);
				}
				
				logger.debug(pid+"---------"+jsonObject);
				
			}
			logger.debug("end releaseUpdateCountTable");
		}catch(Exception e){
			logger.error(e.getMessage(), e);
			throw e;
		} finally {
			DbUtils.commitAndCloseQuietly(checkConn);
		}
		
	}
	
	/**
	 * 质检新增poi_count_table记录
	 * @param jsonObject
	 * @param poiNum
	 * @param checkConn
	 * @throws Exception
	 */
	private void qualityInsertPoiCountTable(JSONObject jsonObject,String poiNum,long subtaskId) throws Exception {
		jsonObject.put("FID", poiNum);
		jsonObject.put("AREA", "null");
		jsonObject.put("NAME_DB_COUNT", "0");
		jsonObject.put("NAME_DATA_UNMODIFIED", "null");
		jsonObject.put("POSITION_DB_COUNT", "0");
		jsonObject.put("POSITION_DATA_UNMODIFIED", "null");
		jsonObject.put("CATEGORY_DB_COUNT", "0");
		jsonObject.put("CATEGORY_DATA_UNMODIFIED", "null");
		jsonObject.put("ADDRESS_DB_COUNT", "0");
		jsonObject.put("ADDRESS_DATA_UNMODIFIED", "null");
		jsonObject.put("PHOTE_DB_COUNT", "0");
		jsonObject.put("PHOTE_DATA_UNMODIFIED", "null");
		jsonObject.put("FATHER_SON_DB_COUNT", "0");
		jsonObject.put("FATHER_SON_DATA_UNMODIFIED", "null");
		jsonObject.put("DEEP_DB_COUNT", "0");
		jsonObject.put("DEEP_DATA_UNMODIFIED", "null");
		jsonObject.put("LABEL_DB_COUNT", "0");
		jsonObject.put("LABEL_DATA_UNMODIFIED", "null");
		jsonObject.put("RESTURANT_DB_COUNT", "0");
		jsonObject.put("RESTURANT_DATA_UNMODIFIED", "null");
		jsonObject.put("LINK_DB_COUNT", "0");
		jsonObject.put("LINK_DATA_UNMODIFIED", "null");
		jsonObject.put("LEVEL_DB_COUNT", "0");
		jsonObject.put("LEVEL_DATA_UNMODIFIED", "null");
		jsonObject.put("INPUT_USERID", "null");
		jsonObject.put("INPUT_TIME", "null");
		jsonObject.put("QC_SUB_TASKID",subtaskId);
		jsonObject.put("VISION", SystemConfigFactory.getSystemConfig().getValue(PropConstant.seasonVersion));
		jsonObject.put("MEMO","null");
		jsonObject.put("TYPE","0");
		jsonObject.put("HAS_EXPORT","0");
		initPoiColumnTable(jsonObject);
	}
	
	/**
	 * 更新PoiCountTable
	 * @param jobInfo
	 * @param checkConn
	 * @throws Exception 
	 */
	private void updatePoiCountTable(JSONObject jsonObject,String poiNum, Connection checkConn) throws Exception {
		PreparedStatement pstmt = null;
        try {
        	Iterator iterator = jsonObject.keys();
        	StringBuffer sb = new StringBuffer();
        	String key = "";
        	String value = "";
        	sb.append("UPDATE POI_COUNT_TABLE SET ");
        	while(iterator.hasNext()){
        		 key = (String) iterator.next();
        	     value = jsonObject.getString(key);
        	     sb.append(""+key+" = ");
        	     sb.append("'"+value+"',");
        	}
        	sb.deleteCharAt(sb.length() - 1);
        	sb.append(" WHERE FID = '"+poiNum+"'");
        	
        	logger.info("sql----------"+sb.toString());

            pstmt = checkConn.prepareStatement(sb.toString());

            pstmt.executeUpdate();
        } catch (Exception e) {
        	logger.error(e.getMessage(), e);
			throw e;
		} 
	}


	public int existRecordInPoiCountTable(String poiNum,Connection conn) throws Exception{
		try {

			conn = DBConnector.getInstance().getCheckConnection();
			
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			String sql = "SELECT count(1) from poi_count_table WHERE FID = '" +poiNum+"'";
			
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			
			if(rs.next()){
				return rs.getInt(1);
			}
			return 0;
		}catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
			
		
	}
	
	/**
	 * 根据subtaskId查找pidList
	 * @param subtaskId
	 * @param conn
	 * @return
	 * @throws Exception 
	 */
	public List<Integer> getPidListBySubTaskId(long subtaskId,Connection conn) throws Exception{
		try {
			
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT PID FROM POI_EDIT_STATUS E WHERE E.STATUS = 2 AND NOT EXISTS (");
			sb.append( " SELECT 1 FROM CK_RESULT_OBJECT R,NI_VAL_EXCEPTION N "
					+ "         WHERE R.TABLE_NAME = 'IX_POI' "
					+ "           AND R.PID = E.PID AND R.MD5_CODE = N.MD5_CODE "
					+ "			  AND N.RULEID IN ("+DealerShipConstantField.DEALERSHIP_CHECK_RULE+"))");
			sb.append( " AND (E.QUICK_SUBTASK_ID='"+subtaskId+"' or E.MEDIUM_SUBTASK_ID='"+subtaskId+"')");
			
			pstmt = conn.prepareStatement(sb.toString());
			rs = pstmt.executeQuery();
			
			List<Integer> pidList = new ArrayList<>();
			while (rs.next()){
				pidList.add(rs.getInt(1));
			}
			
			return pidList;
			
		}catch(Exception e){
			logger.error(e.getMessage(), e);
			throw e;
		}
	}
	
	
	/**
	 * 根据fid查询原先countTable记录
	 * @param poiNum
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	public Map<String, String> getCountTableInfoByFid(String poiNum,Connection conn) throws Exception{
		
		try {
			
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			String sql = "SELECT EXTRA,NAME_DATA_UNMODIFIED,POSITION_DATA_UNMODIFIED,CATEGORY_DATA_UNMODIFIED, "
					+ "ADDRESS_DATA_UNMODIFIED,PHOTE_DATA_UNMODIFIED,FATHER_SON_DATA_UNMODIFIED,DEEP_DATA_UNMODIFIED, "
					+ "LABEL_DATA_UNMODIFIED,RESTURANT_DATA_UNMODIFIED,LINK_DATA_UNMODIFIED,LEVEL_DATA_UNMODIFIED "
					+ " FROM POI_COUNT_TABLE WHERE FID = '" +poiNum+"'";
			
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			
			Map<String, String> map  = new HashMap<>();
			if(rs.next()){
				map.put("EXTRA", rs.getString("EXTRA"));
				map.put("NAME_DATA_UNMODIFIED", rs.getString("NAME_DATA_UNMODIFIED"));
				map.put("POSITION_DATA_UNMODIFIED", rs.getString("POSITION_DATA_UNMODIFIED"));
				map.put("CATEGORY_DATA_UNMODIFIED", rs.getString("CATEGORY_DATA_UNMODIFIED"));
				map.put("ADDRESS_DATA_UNMODIFIED", rs.getString("ADDRESS_DATA_UNMODIFIED"));
				map.put("PHOTE_DATA_UNMODIFIED", rs.getString("PHOTE_DATA_UNMODIFIED"));
				map.put("FATHER_SON_DATA_UNMODIFIED", rs.getString("FATHER_SON_DATA_UNMODIFIED"));
				map.put("DEEP_DATA_UNMODIFIED", rs.getString("DEEP_DATA_UNMODIFIED"));
				map.put("LABEL_DATA_UNMODIFIED", rs.getString("LABEL_DATA_UNMODIFIED"));
				map.put("RESTURANT_DATA_UNMODIFIED", rs.getString("RESTURANT_DATA_UNMODIFIED"));
				map.put("LINK_DATA_UNMODIFIED", rs.getString("LINK_DATA_UNMODIFIED"));
				map.put("LEVEL_DATA_UNMODIFIED", rs.getString("LEVEL_DATA_UNMODIFIED"));
				return map;
			}
			
			return map;
			
		}catch(Exception e){
			logger.error(e.getMessage(), e);
			throw e;
		}
	}
	
	
	/**
	 * 根据fid查询是否有问题记录表
	 * @param poiNum
	 * @return
	 * @throws Exception 
	 */
	public boolean hasProblemByFid(String poiNum,Connection conn) throws Exception{
		try {

			PreparedStatement pstmt = null;
			ResultSet resultSet = null;
			String sql = "SELECT count(1) FROM 	POI_PROBLEM_SUMMARY WHERE POI_NUM = '" +poiNum+"'";
			
			pstmt = conn.prepareStatement(sql);
			resultSet = pstmt.executeQuery();
			
			if(resultSet.next()){
				if(resultSet.getInt(1)>0){
					return true;
				}
			}
			return false;
		}catch(Exception e){
			logger.error(e.getMessage(), e);
			throw e;
		}
		
	}
	

}
