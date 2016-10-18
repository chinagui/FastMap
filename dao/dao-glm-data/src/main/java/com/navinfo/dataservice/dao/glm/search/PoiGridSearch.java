package com.navinfo.dataservice.dao.glm.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

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
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.navinfo.navicommons.geo.computation.CompGridUtil;
import com.navinfo.navicommons.geo.computation.GridUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

public class PoiGridSearch {
	
	/**
	 * 
	 * @param gridDateList
	 * @return data
	 * @throws Exception
	 */
	public List<IRow> getPoiByGrids(JSONArray gridDateList) throws Exception{
		Connection manConn = null;
		Connection conn = null;
		try {
			manConn = DBConnector.getInstance().getManConnection();
			String manQuery = "SELECT r.daily_db_id FROM grid g,region r WHERE g.region_id=r.region_id and grid_id=:1";
			int oldRegionId = 0;
			int regionId = 0;
			List<IRow> retList = new ArrayList<IRow>();
			for (int i=0;i<gridDateList.size();i++ ){
				JSONObject gridDate = gridDateList.getJSONObject(i);
				QueryRunner qRunner = new QueryRunner();
				regionId = qRunner.queryForInt(manConn, manQuery, gridDate.getString("grid"));
				if (regionId != oldRegionId){
					//关闭之前的连接，创建新连接
					DBUtils.closeConnection(conn);
					oldRegionId = regionId;
					conn = DBConnector.getInstance().getConnectionById(regionId);
				}
				List<IRow> data = getPoiData(gridDate,conn);
				retList.addAll(data);
			}
			return retList;
		} catch (Exception e) {
			throw e;
		}finally {
			DBUtils.closeConnection(conn);
			DBUtils.closeConnection(manConn);
		}
	}
	
	/**
	 * 
	 * @param gridDate
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("static-access")
	private List<IRow> getPoiData(JSONObject gridDate,Connection conn) throws Exception{
		
		List<IRow> retList = new ArrayList<IRow>();
		
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT poi_num,pid,mesh_id,kind_code,link_pid,x_guide,y_guide,post_code,open_24h,chain,u_record,geometry,\"LEVEL\",sports_venue,indoor,vip_flag,truck_flag  ");
		sb.append(" FROM ix_poi");
		sb.append(" WHERE sdo_within_distance(geometry, sdo_geometry(    :1  , 8307), 'mask=anyinteract') = 'TRUE' ");
		// 不下载已删除的点20161013
		sb.append(" AND u_record!=2");
		if (!gridDate.getString("date").isEmpty()){
			sb.append(" AND u_date>'"+gridDate.getString("date")+"'");
		}
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		
		try {
			pstmt = conn.prepareStatement(sb.toString());
			
			GridUtils gu = new GridUtils();
			String grid = gridDate.getString("grid");
			String wkt = gu.grid2Wkt(grid);

			pstmt.setString(1, wkt);
			resultSet = pstmt.executeQuery();
			
			while(resultSet.next()){
				IxPoi ixPoi = new IxPoi();
				setAttr(ixPoi,resultSet);
				
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
				
				int id = ixPoi.getPid();
				
				// 设置子表IX_POI_NAME
				IxPoiNameSelector poiNameSelector = new IxPoiNameSelector(conn);

				ixPoi.setNames(poiNameSelector.loadByIdForAndroid(id));

				// 设置子表IX_POI_ADDRESS
				IxPoiAddressSelector ixPoiAddressSelector = new IxPoiAddressSelector(conn);

				ixPoi.setAddresses(ixPoiAddressSelector.loadByIdForAndroid(id));

				// 设置子表IX_POI_PARENT
				IxPoiParentSelector ixPoiParentSelector = new IxPoiParentSelector(conn);
				
				ixPoi.setParents(ixPoiParentSelector.loadByIdForAndroid(id));
				
				// 设置子表IX_POI_CHILDREN
				IxPoiChildrenSelector ixPoiChildrenSelector = new IxPoiChildrenSelector(conn);
				
				ixPoi.setChildren(ixPoiChildrenSelector.loadByIdForAndroid(id));
				
				// 设置子表IX_POI_CONTACT
				IxPoiContactSelector ixPoiContactSelector = new IxPoiContactSelector(conn);
				
				ixPoi.setContacts(ixPoiContactSelector.loadByIdForAndroid(id));
				
				// 设置子表IX_POI_RESTAURANT
				IxPoiRestaurantSelector ixPoiRestaurantSelector = new IxPoiRestaurantSelector(conn);
				
				ixPoi.setRestaurants(ixPoiRestaurantSelector.loadByIdForAndroid(id));
				
				// 设置子表IX_POI_PARKING
				IxPoiParkingSelector ixPoiParkingSelector = new IxPoiParkingSelector(conn);
				
				ixPoi.setParkings(ixPoiParkingSelector.loadByIdForAndroid(id));
				
				// 设置子表IX_POI_HOTEL
				IxPoiHotelSelector ixPoiHotelSelector = new IxPoiHotelSelector(conn);
				
				ixPoi.setHotels(ixPoiHotelSelector.loadByIdForAndroid(id));
				
				// 设置子表IX_POI_GASSTATION
				IxPoiGasstationSelector ixPoiGasstationSelector = new IxPoiGasstationSelector(conn);
				
				ixPoi.setGasstations(ixPoiGasstationSelector.loadByIdForAndroid(id));
				
				retList.add(ixPoi);
			}
			return retList;
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
	}
	
	/**
	 * 
	 * @param ixPoi
	 * @param resultSet
	 * @throws Exception
	 */
	private void setAttr(IxPoi ixPoi,ResultSet resultSet) throws Exception{
		
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
}