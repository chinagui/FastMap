package com.navinfo.dataservice.dao.glm.search.batch;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.search.batch.ixpoi.IxGasstationHandler;
import com.navinfo.dataservice.dao.glm.search.batch.ixpoi.IxHotelHandler;
import com.navinfo.dataservice.dao.glm.search.batch.ixpoi.IxParkingHandler;
import com.navinfo.dataservice.dao.glm.search.batch.ixpoi.IxPoiAddressHandler;
import com.navinfo.dataservice.dao.glm.search.batch.ixpoi.IxPoiChildrenHandler;
import com.navinfo.dataservice.dao.glm.search.batch.ixpoi.IxPoiNameHandler;
import com.navinfo.dataservice.dao.glm.search.batch.ixpoi.IxPoiParentHandler;
import com.navinfo.dataservice.dao.glm.search.batch.ixpoi.IxPoiContactHandler;
import com.navinfo.dataservice.dao.glm.search.batch.ixpoi.IxRestaurantHandler;
import com.navinfo.dataservice.dao.log.LogReader;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.navinfo.navicommons.geo.computation.CompGridUtil;
import com.navinfo.navicommons.geo.computation.GridUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import oracle.sql.STRUCT;

public class PoiGridIncreSearch {
	private static final Logger logger = Logger.getLogger(PoiGridIncreSearch.class);
	/**
	 * 
	 * @param gridDateList
	 * @return data
	 * @throws Exception
	 */
	public Collection<IxPoi> getPoiByGrids(Map<String,String> gridDateMap) throws Exception{
		Map<Long,IxPoi> results = new HashMap<Long,IxPoi>();//key:pid,value:obj
		Connection manConn = null;
		try {
			manConn = DBConnector.getInstance().getManConnection();
			String sql = "SELECT g.grid_id,r.daily_db_id FROM grid g,region r WHERE g.region_id=r.region_id and grid_id in ("+StringUtils.join(gridDateMap.keySet(), ",")+")";
			Map<Integer,Collection<String>> dbGridMap = new QueryRunner().query(manConn, sql, new ResultSetHandler<Map<Integer,Collection<String>>>(){

				@Override
				public Map<Integer, Collection<String>> handle(ResultSet rs) throws SQLException {
					Map<Integer,Collection<String>> map = new HashMap<Integer,Collection<String>>();
					while (rs.next()) {
						int dbId = rs.getInt("daily_db_id");
						List<String> gridList = new ArrayList<String>();
						if (map.containsKey(dbId)) {
							gridList = (List<String>) map.get(dbId);
						}
						gridList.add(rs.getString("grid_id"));
						map.put(dbId, gridList);
					}
					return map;
				}
				
			});
			for(Integer dbId:dbGridMap.keySet()){
				logger.debug("starting load ixpoi from dbId:"+dbId);
				Map<String,String> subMap = new HashMap<String,String>();
				Collection<String> myGrids = dbGridMap.get(dbId);
				logger.debug("my grids is "+StringUtils.join(myGrids,","));
				for(String g:gridDateMap.keySet()){
					if(myGrids.contains(g)){
						subMap.put(g, gridDateMap.get(g));
					}
				}
				results.putAll(loadDateSingleDb(dbId,subMap));
			}
			return results.values();
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			throw e;
		}finally {
			DBUtils.closeConnection(manConn);
		}
	}
	private Map<Long,IxPoi> loadDateSingleDb(int dbId,Map<String,String> gridDateMap)throws Exception{
		Connection conn = null;
		try{
			conn =  DBConnector.getInstance().getConnectionById(dbId);
			Map<Long,IxPoi> pois = null;
			for(String grid:gridDateMap.keySet()){
				logger.debug("starting load grid:"+grid+"from dbId:"+dbId);
				if(pois==null){
					pois = loadDataSingleDbGrid(conn,grid,gridDateMap.get(grid));
				}else{
					pois.putAll(loadDataSingleDbGrid(conn,grid,gridDateMap.get(grid)));
				}
			}
			loadChildTables(conn,pois);
			return pois;
		}catch (Exception e) {
			logger.error(e.getMessage(),e);
			throw e;
		}finally {
			DbUtils.closeQuietly(conn);
		}
	}
	/**
	 * 
	 * @param gridDate
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	private Map<Long,IxPoi> loadDataSingleDbGrid(Connection conn,String grid,String date) throws Exception{
		
		Map<Long,IxPoi> pois = null;
		LogReader logReader = new LogReader(conn);
		if(StringUtils.isEmpty(date)){
			//load all poi，初始化u_record应为0
			pois = loadIxPoi(grid,conn);
			//load status
			Map<Integer,Collection<Long>> poiStatus = logReader.getUpdatedObj("IX_POI","IX_POI", grid, null);
			
			//load 变更poi的状态，设置u_record
			if(poiStatus!=null&&poiStatus.size()>0){
				for(Integer status:poiStatus.keySet()){
					for(Long pid:poiStatus.get(status)){
						if(pois.containsKey(pid)){
							pois.get(pid).setuRecord(status);
						}
					}
				}
			}
		}else{
			Map<Integer,Collection<Long>> poiStatus = logReader.getUpdatedObj("IX_POI","IX_POI", grid, date);
			//load 
			pois = new HashMap<Long,IxPoi>();
			for(Integer status:poiStatus.keySet()){
				Map<Long,IxPoi> result = loadIxPoi(status,poiStatus.get(status),conn);
				if(result!=null) pois.putAll(result);
			}
		}
		return pois;
	}
	/**
	 * 全量下载
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("static-access")
	private Map<Long,IxPoi> loadIxPoi(String grid,Connection conn)throws Exception{
		Map<Long,IxPoi> pois = new HashMap<Long,IxPoi>();
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT poi_num,pid,mesh_id,kind_code,link_pid,x_guide,y_guide,post_code,open_24h,chain,u_record,geometry,\"LEVEL\",sports_venue,indoor,vip_flag,truck_flag  ");
		sb.append(" FROM ix_poi");
		sb.append(" WHERE sdo_within_distance(geometry, sdo_geometry(    :1  , 8307), 'mask=anyinteract') = 'TRUE' ");
		// 不下载已删除的点20161013
		sb.append(" AND u_record!=2");
		logger.info("poi query sql:"+sb);
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		
		try {
			pstmt = conn.prepareStatement(sb.toString());
			
			GridUtils gu = new GridUtils();
			String wkt = gu.grid2Wkt(grid);
			logger.info("grid wkt:"+wkt);
			pstmt.setString(1, wkt);
			resultSet = pstmt.executeQuery();
			while(resultSet.next()){
				IxPoi ixPoi = new IxPoi();
				fillMainTable(ixPoi,resultSet);
				
				Coordinate coord = ixPoi.getGeometry().getCoordinate();
				String[] grids = CompGridUtil.point2Grids(coord.x,coord.y);
				boolean inside=false;
				for(String gridId : grids){
					if(gridId.equals(grid)){
						inside=true;
					}
				}
				if(!inside){
					continue;
				}
				Long pid = (long) ixPoi.getPid();
				pois.put(pid, ixPoi);
			}
			
			return pois;
		} catch(Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
	}
	
	/**
	 * 增量下载
	 * @param pids
	 * @return
	 * @throws Exception
	 */
	private Map<Long,IxPoi> loadIxPoi(int status,Collection<Long> pois,Connection conn)throws Exception{
		
		if (status == 2) {
			return new HashMap<Long,IxPoi>();
		}
		Clob pidClod = null;
		Map<Long,IxPoi> poisMap = new HashMap<Long,IxPoi>();
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT poi_num,pid,mesh_id,kind_code,link_pid,x_guide,y_guide,post_code,open_24h,chain,u_record,geometry,\"LEVEL\",sports_venue,indoor,vip_flag,truck_flag  ");
		sb.append(" FROM ix_poi");
		pidClod = ConnectionUtil.createClob(conn);
		pidClod.setString(1, StringUtils.join(pois, ","));
		sb.append(" WHERE a.pid in (select to_char(pid) from table(clob_to_table(?)))");
		logger.info("poi query sql:"+sb);
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setClob(1, pidClod);
			resultSet = pstmt.executeQuery();
			
			while(resultSet.next()){
				IxPoi ixPoi = new IxPoi();
				fillMainTable(ixPoi,resultSet);
				
				Long pid = (long) ixPoi.getPid();
				poisMap.put(pid, ixPoi);
			}
			
			
			return poisMap;
		}catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
	}
	
	/**
	 * 增量下载时
	 * @param ixPoi
	 * @param resultSet
	 * @throws Exception
	 */
	private void fillMainTable(IxPoi ixPoi,ResultSet resultSet) throws Exception{
		
		ixPoi.setPid(resultSet.getInt("pid"));

		ixPoi.setKindCode(resultSet.getString("kind_code"));

		STRUCT struct = (STRUCT) resultSet.getObject("geometry");

		Geometry geometry = GeoTranslator.struct2Jts(struct);

		ixPoi.setGeometry(geometry);

		ixPoi.setxGuide(resultSet.getDouble("x_guide"));

		ixPoi.setyGuide(resultSet.getDouble("y_guide"));

		ixPoi.setLinkPid(resultSet.getInt("link_pid"));
		
		ixPoi.setPoiNum(resultSet.getString("poi_num"));
		
		ixPoi.setMeshId(resultSet.getInt("mesh_id"));
		
		ixPoi.setPostCode(resultSet.getString("post_code"));
		
		ixPoi.setOpen24h(resultSet.getInt("open_24h"));
		
		ixPoi.setChain(resultSet.getString("chain"));
		
		ixPoi.setuRecord(resultSet.getInt("u_record"));
		
		ixPoi.setLevel(resultSet.getString("level"));
		
		ixPoi.setSportsVenue(resultSet.getString("sports_venue"));
		
		ixPoi.setIndoor(resultSet.getInt("indoor"));
		
		ixPoi.setVipFlag(resultSet.getString("vip_flag"));
		
		ixPoi.setTruckFlag(resultSet.getInt("truck_flag"));
		
	}
	
	private void loadChildTables(Connection conn,Map<Long,IxPoi> pois)throws Exception{
		
		Collection<Long> pids = pois.keySet();
		
		QueryRunner run = new QueryRunner();
		
		Clob pidsClob = ConnectionUtil.createClob(conn);
		pidsClob.setString(1, StringUtils.join(pids, ","));
		
		logger.info("设置子表IX_POI_NAME");
		
		String sql="select * from ix_poi_name where u_record !=2 and name_class=1 and name_type=2 and lang_code='CHI' and poi_pid in (select to_number(column_value) from table(clob_to_table(?)))";
		
		Map<Long,List<IRow>> names = run.query(conn, sql, new IxPoiNameHandler(),pidsClob);

		for(Long pid:names.keySet()){
			pois.get(pid).setNames(names.get(pid));
		}
		
		logger.info("设置子表IX_POI_ADDRESS");
		
		sql="select * from ix_poi_address where u_record !=2 and name_groupid=1 and lang_code='CHI' and poi_pid in (select to_number(column_value) from table(clob_to_table(?)))";
		
		Map<Long,List<IRow>> addresses = run.query(conn, sql, new IxPoiAddressHandler(),pidsClob);

		for(Long pid:addresses.keySet()){
			pois.get(pid).setAddresses(addresses.get(pid));
		}
		
		logger.info("设置子表IX_POI_PARENT");
		
		StringBuilder sbParent = new StringBuilder();
		sbParent.append("WITH A AS(");
		sbParent.append(" SELECT CH.GROUP_ID,CH.CHILD_POI_PID,CH.RELATION_TYPE,P.PARENT_POI_PID FROM IX_POI_CHILDREN CH,IX_POI_PARENT P WHERE CH.GROUP_ID=P.GROUP_ID AND CH.CHILD_POI_PID IN (select to_number(column_value) PID from table(clob_to_table(?)))");
		sbParent.append(" ),");
		sbParent.append(" B AS(");
		sbParent.append(" SELECT CHILD_POI_PID,MIN(PARENT_POI_PID) P_PID FROM A WHERE RELATION_TYPE=2 GROUP BY CHILD_POI_PID");
		sbParent.append(" ),");
		sbParent.append(" C AS(");
		sbParent.append(" SELECT CHILD_POI_PID,MIN(PARENT_POI_PID) P_PID FROM A WHERE NOT EXISTS(SELECT 1 FROM B WHERE A.CHILD_POI_PID=B.CHILD_POI_PID) GROUP BY CHILD_POI_PID");
		sbParent.append(" )");
		sbParent.append(" SELECT B.CHILD_POI_PID,P.POI_NUM FROM B,IX_POI P WHERE B.P_PID=P.PID");
		sbParent.append(" UNION ALL");
		sbParent.append(" SELECT C.CHILD_POI_PID,P.POI_NUM FROM C,IX_POI P WHERE C.P_PID=P.PID");
		
		Map<Long,List<IRow>> parent = run.query(conn, sbParent.toString(), new IxPoiParentHandler(),pidsClob);

		for(Long pid:parent.keySet()){
			pois.get(pid).setParents(parent.get(pid));
		}
		
		logger.info("设置子表IX_POI_CHILDREN");
		
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT p.parent_poi_pid,c.child_poi_pid,c.relation_type,c.row_id,");
		sb.append("(select poi_num from ix_poi where pid=c.child_poi_pid) poi_num");
		sb.append(" FROM ix_poi_parent p");
		sb.append(" ,ix_poi_children c");
		sb.append(" WHERE p.group_id=c.group_id");
		sb.append(" AND p.parent_poi_pid in (select to_number(column_value) from table(clob_to_table(?)))");
		sb.append(" AND c.u_record !=2");
		
		Map<Long,List<IRow>> children = run.query(conn, sb.toString(), new IxPoiChildrenHandler(),pidsClob);

		for(Long pid:children.keySet()){
			pois.get(pid).setChildren(children.get(pid));
		}
		
		logger.info("设置子表IX_POI_CONTACT");
		
		sql="select * from ix_poi_contact where u_record!=2 and poi_pid in (select to_number(column_value) from table(clob_to_table(?)))";
		
		Map<Long,List<IRow>> contact = run.query(conn, sql, new IxPoiContactHandler(),pidsClob);

		for(Long pid:contact.keySet()){
			pois.get(pid).setContacts(contact.get(pid));
		}
		
		logger.info("设置子表IX_POI_RESTAURANT");
		
		sql="select * from ix_poi_restaurant WHERE u_record !=2 and poi_pid in (select to_number(column_value) from table(clob_to_table(?)))";
		
		Map<Long,List<IRow>> restaurant = run.query(conn, sql, new IxRestaurantHandler(),pidsClob);

		for(Long pid:restaurant.keySet()){
			pois.get(pid).setRestaurants(restaurant.get(pid));
		}
		
		logger.info("设置子表IX_POI_PARKING");
		
		sql="select * from ix_poi_parking WHERE u_record !=2 and poi_pid in (select to_number(column_value) from table(clob_to_table(?)))";
		
		Map<Long,List<IRow>> parking = run.query(conn, sql, new IxParkingHandler(),pidsClob);

		for(Long pid:parking.keySet()){
			pois.get(pid).setParkings(parking.get(pid));
		}
		
		logger.info("设置子表IX_POI_HOTEL");
		
		sql="select * from ix_poi_hotel WHERE u_record !=2 and poi_pid in (select to_number(column_value) from table(clob_to_table(?)))";
		
		Map<Long,List<IRow>> hotel = run.query(conn, sql, new IxHotelHandler(),pidsClob);

		for(Long pid:hotel.keySet()){
			pois.get(pid).setHotels(hotel.get(pid));
		}
		
		logger.info("设置子表IX_POI_GASSTATION");
		
		sql="select * from ix_poi_gasstation WHERE u_record !=2 and poi_pid in (select to_number(column_value) from table(clob_to_table(?)))";
		
		Map<Long,List<IRow>> gasstation = run.query(conn, sql, new IxGasstationHandler(),pidsClob);

		for(Long pid:gasstation.keySet()){
			pois.get(pid).setGasstations(gasstation.get(pid));
		}
		
	}
	
}