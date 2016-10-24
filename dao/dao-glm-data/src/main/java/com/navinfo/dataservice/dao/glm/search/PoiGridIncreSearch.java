package com.navinfo.dataservice.dao.glm.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
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
import com.navinfo.navicommons.geo.computation.CompGridUtil;
import com.navinfo.navicommons.geo.computation.GridUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

public class PoiGridIncreSearch {
	private static final Logger logger = Logger.getLogger(PoiGridIncreSearch.class);
	/**
	 * 
	 * @param gridDateList
	 * @return data
	 * @throws Exception
	 */
	public List<IxPoi> getPoiByGrids(Map<String,String> gridDateMap) throws Exception{
		List<IxPoi> results = new ArrayList<IxPoi>();
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
				results.addAll(loadDateSingleDb(dbId,subMap));
			}
			return results;
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			throw e;
		}finally {
			DBUtils.closeConnection(manConn);
		}
	}
	private List<IxPoi> loadDateSingleDb(int dbId,Map<String,String> gridDateMap)throws Exception{
		Connection conn = null;
		try{
			conn =  DBConnector.getInstance().getConnectionById(dbId);
			List<IxPoi> retList = null;
			for(String grid:gridDateMap.keySet()){
				if(retList==null){
					retList = loadDataSingleDbGrid(conn,grid,gridDateMap.get(grid));
				}else{
					retList.addAll(loadDataSingleDbGrid(conn,grid,gridDateMap.get(grid)));
				}
			}
			return retList;
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
	private List<IxPoi> loadDataSingleDbGrid(Connection conn,String grid,String date) throws Exception{
		
		List<IxPoi> retList = new ArrayList<IxPoi>();
		
		if(StringUtils.isEmpty(date)){
			Map<Integer,Integer> poiStatus = LogReader.getUpdatedObj("IX_POI", grid, null);
			//load all poi，初始化u_record应为0
			List<IxPoi> pois = loadIxPoi(grid);
			//load 变更poi的状态，设置u_record
			Set<Integer> updatedPois=poiStatus.keySet();
			if(updatedPois!=null&&updatedPois.size()>0){
				for(IxPoi p:pois){
					if(updatedPois.contains(p.getPid())){
						p.setuRecord(poiStatus.get(p.getPid()));
					}
				}
			}
		}else{
			Map<Integer,Integer> poiStatus = LogReader.getUpdatedObj("IX_POI", grid, date);
			//load 
			List<IxPoi> pois = loadIxPoi(poiStatus);
		}
		return retList;
	}
	/**
	 * 全量下载
	 * @return
	 * @throws Exception
	 */
	private List<IxPoi> loadIxPoi(String grid)throws Exception{
		return null;
	}
	
	/**
	 * 增量下载
	 * @param pids
	 * @return
	 * @throws Exception
	 */
	private List<IxPoi> loadIxPoi(Map<Integer,Integer> pois)throws Exception{
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