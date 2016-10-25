package com.navinfo.dataservice.dao.glm.search;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.selector.poi.deep.IxPoiGasstationSelector;
import com.navinfo.dataservice.dao.glm.selector.poi.deep.IxPoiHotelSelector;
import com.navinfo.dataservice.dao.glm.selector.poi.deep.IxPoiParkingSelector;
import com.navinfo.dataservice.dao.glm.selector.poi.deep.IxPoiRestaurantSelector;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiAddressSelector;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiChildrenSelector;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiContactSelector;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiNameSelector;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiParentSelector;
import com.navinfo.dataservice.dao.log.LogReader;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.sql.DBUtils;
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
		Map<Integer,IxPoi> results = new HashMap<Integer,IxPoi>();//key:pid,value:obj
		Connection manConn = null;
		try {
			manConn = DBConnector.getInstance().getManConnection();
			String sql = "SELECT g.grid_id,r.daily_db_id FROM grid g,region r WHERE g.region_id=r.region_id and grid_id in ("+StringUtils.join(gridDateMap.keySet(), ",")+")";
			Map<Integer,Collection<String>> dbGridMap = new QueryRunner().query(manConn, sql, new ResultSetHandler<Map<Integer,Collection<String>>>(){

				@Override
				public Map<Integer, Collection<String>> handle(ResultSet rs) throws SQLException {
					Map<Integer,Collection<String>> map = new HashMap<Integer,Collection<String>>();
					//...
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
	private Map<Integer,IxPoi> loadDateSingleDb(int dbId,Map<String,String> gridDateMap)throws Exception{
		Connection conn = null;
		try{
			conn =  DBConnector.getInstance().getConnectionById(dbId);
			Map<Integer,IxPoi> pois = null;
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
	private Map<Integer,IxPoi> loadDataSingleDbGrid(Connection conn,String grid,String date) throws Exception{
		
		Map<Integer,IxPoi> pois = null;
		if(StringUtils.isEmpty(date)){
			//load all poi，初始化u_record应为0
			pois = loadIxPoi(grid);
			//load status
			Map<Integer,Collection<Integer>> poiStatus = LogReader.getUpdatedObj("IX_POI", grid, null);
			
			//load 变更poi的状态，设置u_record
			if(poiStatus!=null&&poiStatus.size()>0){
				for(Integer status:poiStatus.keySet()){
					for(Integer pid:poiStatus.get(status)){
						if(pois.containsKey(pid)){
							pois.get(pid).setuRecord(status);
						}
					}
				}
			}
		}else{
			Map<Integer,Collection<Integer>> poiStatus = LogReader.getUpdatedObj("IX_POI", grid, date);
			//load 
			pois = new HashMap<Integer,IxPoi>();
			for(Integer status:poiStatus.keySet()){
				Map<Integer,IxPoi> result = loadIxPoi(status,poiStatus.get(status));
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
	private Map<Integer,IxPoi> loadIxPoi(String grid)throws Exception{
		return null;
	}
	
	/**
	 * 增量下载
	 * @param pids
	 * @return
	 * @throws Exception
	 */
	private Map<Integer,IxPoi> loadIxPoi(int status,Collection<Integer> pois)throws Exception{
		return null;
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
	
	private void loadChildTables(Connection conn,IxPoi ixPoi)throws Exception{
		
		int id = ixPoi.getPid();
		
		logger.info("设置子表IX_POI_NAME");
		IxPoiNameSelector poiNameSelector = new IxPoiNameSelector(conn);

		ixPoi.setNames(poiNameSelector.loadByIdForAndroid(id));

		logger.info("设置子表IX_POI_ADDRESS");
		IxPoiAddressSelector ixPoiAddressSelector = new IxPoiAddressSelector(conn);

		ixPoi.setAddresses(ixPoiAddressSelector.loadByIdForAndroid(id));

		logger.info("设置子表IX_POI_PARENT");
		IxPoiParentSelector ixPoiParentSelector = new IxPoiParentSelector(conn);
		
		ixPoi.setParents(ixPoiParentSelector.loadByIdForAndroid(id));
		
		logger.info("设置子表IX_POI_CHILDREN");
		IxPoiChildrenSelector ixPoiChildrenSelector = new IxPoiChildrenSelector(conn);
		
		ixPoi.setChildren(ixPoiChildrenSelector.loadByIdForAndroid(id));
		
		logger.info("设置子表IX_POI_CONTACT");
		IxPoiContactSelector ixPoiContactSelector = new IxPoiContactSelector(conn);
		
		ixPoi.setContacts(ixPoiContactSelector.loadByIdForAndroid(id));
		
		logger.info("设置子表IX_POI_RESTAURANT");
		IxPoiRestaurantSelector ixPoiRestaurantSelector = new IxPoiRestaurantSelector(conn);
		
		ixPoi.setRestaurants(ixPoiRestaurantSelector.loadByIdForAndroid(id));
		
		logger.info("设置子表IX_POI_PARKING");
		IxPoiParkingSelector ixPoiParkingSelector = new IxPoiParkingSelector(conn);
		
		ixPoi.setParkings(ixPoiParkingSelector.loadByIdForAndroid(id));
		
		logger.info("设置子表IX_POI_HOTEL");
		IxPoiHotelSelector ixPoiHotelSelector = new IxPoiHotelSelector(conn);
		
		ixPoi.setHotels(ixPoiHotelSelector.loadByIdForAndroid(id));
		
		logger.info("设置子表IX_POI_GASSTATION");
		IxPoiGasstationSelector ixPoiGasstationSelector = new IxPoiGasstationSelector(conn);
		
		ixPoi.setGasstations(ixPoiGasstationSelector.loadByIdForAndroid(id));
	}
}