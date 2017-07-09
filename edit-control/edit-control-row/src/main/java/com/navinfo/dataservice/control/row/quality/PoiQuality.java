package com.navinfo.dataservice.control.row.quality;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONArray;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
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

	public void initQualityData(int qualityId, int dbId) throws Exception {

		Connection conn = null;
		try {
			
			Map<String, String>  map = getGeometryByQualityId(qualityId);//根据质检圈的的qualityId,获取质检圈geometry
			String geometry = map.get("geometry");
			String subtaskId = map.get("subtaskId");
			conn = DBConnector.getInstance().getConnectionById(dbId);
//			List<Integer> pidList = getPidList(geometry,conn);//查询质检圈内的所有poi
			List<Integer> pidList = new ArrayList<>();
			/*pidList.add(11942);
			pidList.add(5838);
			pidList.add(83276925);
			pidList.add(152133);
			pidList.add(56554713);
			pidList.add(4658944);*/
			pidList.add(4658944);
			
			for (Integer pid : pidList) {

				IxPoiSelector poiSelector = new IxPoiSelector(conn);
				IxPoi poi = (IxPoi) poiSelector.loadById(pid, false);
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
				setInitNameStatisticsByPoi(jsonObject, poi);//赋值名称统计项
				setInitPositionStatisticsByPoi(jsonObject, poi);//赋值点位统计项
				setInitCategoryStatisticsByPoi(jsonObject, poi);//赋值分类统计项
				setInitAddressStatisticsByPoi(jsonObject, poi);//赋值地址统计项
				setInitPhoteStatisticsByPoi(jsonObject, poi);//赋值电话统计项
				setInitFatherSONStatisticsByPoi(jsonObject, poi,conn);//赋值父子关系统计项
				setInitDeepStatisticsByPoi(jsonObject, poi,conn);//赋值深度信息统计项
				setInitLebelStatisticsByPoi(jsonObject, poi);//赋值Lebel统计项
				setInitResturantStatisticsByPoi(jsonObject, poi);//赋值餐厅统计项
				setInitLinkStatisticsByPoi(jsonObject, poi);//赋值LINK_PID统计项
				setInitLevelStatisticsByPoi(jsonObject, poi);//赋值Level统计项
				
				
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
				
				initPoiColumnTable(jsonObject);

				System.out.println(pid+"-------------"+jsonObject);
			}
			
			
		}catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		} finally {
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
        	
        	System.out.println("sql----------"+sb.toString());

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
	private void setInitLevelStatisticsByPoi(JSONObject jsonObject, IxPoi poi) {
		String level = poi.getLevel();
		if(StringUtils.isBlank(level)){
			jsonObject.put("LEVEL_DB_COUNT", "0");
			jsonObject.put("LEVEL_DATA_UNMODIFIED", "null");
		}else{
			jsonObject.put("LEVEL_DB_COUNT", "1");
			jsonObject.put("LEVEL_DATA_UNMODIFIED", level);
		}
		
		setNullDataByType("LEVEL", jsonObject);
	}

	/**
	 * 赋值LINK_PID统计项
	 * @param jsonObject
	 * @param poi
	 */
	private void setInitLinkStatisticsByPoi(JSONObject jsonObject, IxPoi poi) {
		Integer linkPid = poi.getLinkPid();
		if(linkPid==null){
			jsonObject.put("LINK_DB_COUNT", "0");
			jsonObject.put("LINK_DATA_UNMODIFIED", "null");
		}else{
			jsonObject.put("LINK_DB_COUNT", "1");
			jsonObject.put("LINK_DATA_UNMODIFIED", linkPid.toString());
		}
		
		setNullDataByType("LINK", jsonObject);
		
	}

	/**
	 * 赋值餐厅统计项
	 * @param jsonObject
	 * @param poi
	 */
	private void setInitResturantStatisticsByPoi(JSONObject jsonObject, IxPoi poi) {
		boolean resturantFlag = false;
		JSONArray jsonArray = new JSONArray();
		if(poi.getRestaurants()!=null&&poi.getRestaurants().size()>0){
			List<IRow> resturants = poi.getRestaurants();
			for (IRow iRow : resturants) {
				IxPoiRestaurant addressTmp = (IxPoiRestaurant)iRow;
				JSONObject jo =  JSONObject.fromObject(addressTmp);   
				jsonArray.add(jo);
				resturantFlag = true;
			}
		}
		if(resturantFlag){
			jsonObject.put("RESTURANT_DB_COUNT", "1");
			jsonObject.put("RESTURANT_DATA_UNMODIFIED",jsonArray.toJSONString());
		}else{
			jsonObject.put("RESTURANT_DB_COUNT", "0");
			jsonObject.put("RESTURANT_DATA_UNMODIFIED", "null");
		}
		
		setNullDataByType("RESTURANT", jsonObject);
		
	}


	/**
	 * 赋值Lebel统计项
	 * @param jsonObject
	 * @param poi
	 */
	private void setInitLebelStatisticsByPoi(JSONObject jsonObject, IxPoi poi) {
		String label = poi.getLabel();
		if(StringUtils.isBlank(label)){
			jsonObject.put("LABEL_DB_COUNT", "0");
			jsonObject.put("LABEL_DATA_UNMODIFIED", "null");
		}else{
			jsonObject.put("LABEL_DB_COUNT", "1");
			jsonObject.put("LABEL_DATA_UNMODIFIED", label);
		}
		
		setNullDataByType("LABEL", jsonObject);
	}


	/**
	 * 赋值深度信息统计项
	 * @param jsonObject
	 * @param poi
	 * @param conn
	 * @throws Exception
	 */
	private void setInitDeepStatisticsByPoi(JSONObject jsonObject, IxPoi poi,Connection conn) throws Exception {
		int pid = poi.getPid();
		JSONArray data = new JSONArray();
		try{
			String sql1  =  "select * from ix_poi_hotel where poi_pid = "+pid+" order by hotel_id";
			String sql2  =  "select * from ix_poi_building where poi_pid = "+pid+" order by poi_pid";
			String sql3  =  "select * from ix_poi_gasstation where poi_pid = "+pid+" order by gasstation_id";
			String sql4  =  "select * from ix_poi_chargingstation where poi_pid = "+pid+" order by charging_id";
			String sql5  =  "select * from ix_poi_chargingplot where poi_pid = "+pid+" order by poi_pid";
			String sql6  =  "select * from ix_poi_parking where poi_pid = "+pid+" order by parking_id";
			List<String> sqlList = new ArrayList<>();
			sqlList.add(sql1);
			sqlList.add(sql2);
			sqlList.add(sql3);
			sqlList.add(sql4);
			sqlList.add(sql5);
			sqlList.add(sql6);
			
			for (String sql:sqlList) {
				data = setDeepInfoBySql(sql,conn);
				if(!data.isEmpty()){
					jsonObject.put("DEEP_DB_COUNT", "1");
					jsonObject.put("DEEP_DATA_UNMODIFIED", data.toJSONString());
					break;
				}
			}
			
			if(data.isEmpty()){
				jsonObject.put("DEEP_DB_COUNT", "0");
				jsonObject.put("DEEP_DATA_UNMODIFIED", "null");
			}
			
			setNullDataByType("DEEP", jsonObject);
		}catch(Exception e){
			logger.error(e.getMessage(), e);
			throw e;
		}
	}


	/**
	 * 赋值父子关系统计项
	 * @param jsonObject
	 * @param poi
	 * @param conn
	 * @throws Exception
	 */
	private void setInitFatherSONStatisticsByPoi(JSONObject jsonObject, IxPoi poi,Connection conn) throws Exception {
		StringBuffer sb = new StringBuffer();
		String fatherSon = "";
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
		if(sb.length()>0){
			fatherSon = sb.toString().substring(0, sb.toString().length() - 1);
			jsonObject.put("FATHER_SON_DB_COUNT", "1");
			jsonObject.put("FATHER_SON_DATA_UNMODIFIED",fatherSon);
		}else{
			jsonObject.put("FATHER_SON_DB_COUNT", "0");
			jsonObject.put("FATHER_SON_DATA_UNMODIFIED","null");
		}
		
		setNullDataByType("FATHER_SON", jsonObject);
	}


	/**
	 * 赋值电话统计项
	 * @param jsonObject
	 * @param poi
	 */
	private void setInitPhoteStatisticsByPoi(JSONObject jsonObject, IxPoi poi) {
		StringBuffer sb = new StringBuffer();
		String telephone = "null";
		if(poi.getContacts()!=null&&poi.getContacts().size()>0){
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
			
			jsonObject.put("PHOTE_DB_COUNT", "1");
			jsonObject.put("PHOTE_DATA_UNMODIFIED",telephone );
			
		}else{
			jsonObject.put("PHOTE_DB_COUNT", "0");
			jsonObject.put("PHOTE_DATA_UNMODIFIED", "null");
		}
		
		setNullDataByType("PHOTE", jsonObject);
		
	}


	
	/**
	 * 赋值地址统计项
	 * @param jsonObject
	 * @param poi
	 */
	private void setInitAddressStatisticsByPoi(JSONObject jsonObject, IxPoi poi) {

		boolean addressFlag = false;
		if(poi.getAddresses()!=null&&poi.getAddresses().size()>0){
			List<IRow> addresses = poi.getAddresses();
			for (IRow iRow : addresses) {
				IxPoiAddress addressTmp = (IxPoiAddress)iRow;
				if(addressTmp.getLangCode().equals("CHI")){
					jsonObject.put("ADDRESS_DATA_UNMODIFIED", addressTmp.getFullname());
					addressFlag = true;
				}
			}
		}
		if(addressFlag){
			jsonObject.put("ADDRESS_DB_COUNT", "1");
		}else{
			jsonObject.put("ADDRESS_DB_COUNT", "0");
			jsonObject.put("ADDRESS_DATA_UNMODIFIED", "null");
		}
		
		setNullDataByType("ADDRESS", jsonObject);
		
	}


	/**
	 * 赋值分类统计项
	 * @param jsonObject
	 * @param poi
	 */
	private void setInitCategoryStatisticsByPoi(JSONObject jsonObject, IxPoi poi) {

		String kindCode = poi.getKindCode();
		if(StringUtils.isBlank(kindCode)){
			jsonObject.put("CATEGORY_DB_COUNT", "0");
			jsonObject.put("CATEGORY_DATA_UNMODIFIED", "null");
		}else{
			jsonObject.put("CATEGORY_DB_COUNT", "1");
			jsonObject.put("CATEGORY_DATA_UNMODIFIED", kindCode);
		}
		
		setNullDataByType("CATEGORY", jsonObject);
		
	}


	/**
	 * 赋值点位统计项
	 * @param jsonObject
	 * @param poi
	 */
	private void setInitPositionStatisticsByPoi(JSONObject jsonObject, IxPoi poi) {
		Geometry geometry = poi.getGeometry();
		geometry = GeoTranslator.transform(geometry, 0.00001, 5);
		Coordinate coordinate= geometry.getCoordinate();
		double x = coordinate.x;
		double y = coordinate.y;
		
		jsonObject.put("POSITION_DB_COUNT", "1");
		JSONObject jo = new JSONObject();
		jo.put("X", x);
		jo.put("Y", y);
		jsonObject.put("POSITION_DATA_UNMODIFIED", jo);
		setNullDataByType("POSITION", jsonObject);
		
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
	 */
	private void setInitNameStatisticsByPoi(JSONObject jsonObject,IxPoi poi){
		setNullDataByType("NAME", jsonObject);
		
		boolean nameFlag = false;
		if(poi.getNames()!=null&&poi.getNames().size()>0){
			List<IRow> names = poi.getNames();
			for (IRow iRow : names) {
				IxPoiName nameTmp = (IxPoiName)iRow;
				if(nameTmp.getLangCode().equals("CHI")&&nameTmp.getNameType()==2
						&&nameTmp.getNameClass()==1){
					jsonObject.put("NAME_DATA_UNMODIFIED", nameTmp.getName());
					nameFlag = true;
				}
			}
		}
		if(nameFlag){
			jsonObject.put("NAME_DB_COUNT", "1");
		}else{
			jsonObject.put("NAME_DB_COUNT", "0");
			jsonObject.put("NAME_DATA_UNMODIFIED", "null");
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
	
}
