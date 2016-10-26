package com.navinfo.dataservice.dao.glm.search;

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
				if(status!=null) pois.putAll(result);
			}
		}
		return pois;
	}
	/**
	 * 全量下载
	 * @return
	 * @throws Exception
	 */
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
			loadChildTables(conn,pois);
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
			
			loadChildTables(conn,poisMap);
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
		

		
		logger.info("设置子表IX_POI_NAME");
		
		String sql="select * from ix_poi_name where poi_pid in (select to_number(column_value) from table(clob_to_table(?)))";
		
		Map<Long,List<IRow>> names = run.query(conn, sql, new IxPoiNameHandler(),StringUtils.join(pids, ","));

		for(Long pid:names.keySet()){
			pois.get(pid).setNames(names.get(pid));
		}
		//...
	}
	class IxPoiNameHandler implements ResultSetHandler<Map<Long,List<IRow>>>{

		@Override
		public Map<Long, List<IRow>> handle(ResultSet rs) throws SQLException {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
}