package com.navinfo.dataservice.dao.glm.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import com.navinfo.navicommons.geo.computation.GridUtils;
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
	@SuppressWarnings("unchecked")
	public List<IRow> getPoiByGrids(JSONArray gridDateList) throws Exception{
		Connection manConn = null;
		Connection conn = null;
		try {
			manConn = DBConnector.getInstance().getManConnection();
			String manQuery = "SELECT region_id FROM grid WHERE grid_id=:1";
			PreparedStatement pstmt = null;
			ResultSet resultSet = null;
			int oldRegionId = 0;
			int regionId = 0;
			List<IRow> retList = new ArrayList<IRow>();
			for (int i=0;i<gridDateList.size();i++ ){
				pstmt = manConn.prepareStatement(manQuery);
				JSONObject gridDate = gridDateList.getJSONObject(i);
				pstmt.setString(1, gridDate.getString("grid"));
				resultSet = pstmt.executeQuery();
				if (resultSet.next()){
					regionId = resultSet.getInt("region_id");
					if (regionId != oldRegionId){
						//关闭之前的连接，创建新连接
						if (conn != null) {
							try {
								conn.close();
							} catch (Exception e) {

							}
						}
						oldRegionId = regionId;
						conn = DBConnector.getInstance().getConnectionById(regionId);
					}
					List<IRow> data = getPoiData(gridDate,conn);
					retList.addAll(data);
				} else {
					continue;
				}
			}
			return retList;
		} catch (Exception e) {
			throw e;
		}finally {
			if (manConn != null) {
				try {
					manConn.close();
				} catch (Exception e) {

				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {

				}
			}
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
		IxPoi ixPoi = new IxPoi();
		List<IRow> retList = new ArrayList<IRow>();
		
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT poi_num,pid,mesh_id,kind_code,link_pid,x_guide,y_guide,post_code,open_24h,chain,u_record,geometry");
		sb.append(" FROM "+ixPoi.tableName());
		sb.append(" WHERE sdo_relate(geometry, sdo_geometry(    :1  , 8307), 'mask=anyinteract') = 'TRUE' ");
		if (!gridDate.getString("date").isEmpty()){
			sb.append(" AND u_date>'"+gridDate.getString("date")+"'");
		}
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		
		try {
			pstmt = conn.prepareStatement(sb.toString());
			
			GridUtils gu = new GridUtils();
			String wkt = gu.grid2Wkt(gridDate.getString("grid"));

			pstmt.setString(1, wkt);
			resultSet = pstmt.executeQuery();
			
			while(resultSet.next()){
				setAttr(ixPoi,resultSet);
				
				int id = ixPoi.getPid();
				
				// 设置子表IX_POI_NAME
				IxPoiNameSelector poiNameSelector = new IxPoiNameSelector(conn);

				ixPoi.setNames(poiNameSelector.loadByIdForAndroid(id));

				// 设置子表IX_POI_ADDRESS
				IxPoiAddressSelector ixPoiAddressSelector = new IxPoiAddressSelector(conn);

				ixPoi.setAddresses(ixPoiAddressSelector.loadByIdForAndroid(id));

				//TODO IX_POI_ATTR_AUX
				
				// 设置子表IX_POI_PARENT
				IxPoiParentSelector ixPoiParentSelector = new IxPoiParentSelector(conn);
				
				ixPoi.setParents(ixPoiParentSelector.loadByIdForAndroid(id));
				
				// 设置子表IX_POI_CHILDREN
				IxPoiChildrenSelector ixPoiChildrenSelector = new IxPoiChildrenSelector(conn);
				
				ixPoi.setChildren(ixPoiChildrenSelector.loadByIdForAndroid(id));
				
				// 设置子表IX_POI_CONTACT
				IxPoiContactSelector ixPoiContactSelector = new IxPoiContactSelector(conn);
				
				ixPoi.setContacts(ixPoiContactSelector.loadRowsByParentId(id, false));
				
				// 设置子表IX_POI_RESTAURANT
				IxPoiRestaurantSelector ixPoiRestaurantSelector = new IxPoiRestaurantSelector(conn);
				
				ixPoi.setRestaurants(ixPoiRestaurantSelector.loadRowsByParentId(id, false));
				
				// 设置子表IX_POI_PARKING
				IxPoiParkingSelector ixPoiParkingSelector = new IxPoiParkingSelector(conn);
				
				ixPoi.setParkings(ixPoiParkingSelector.loadRowsByParentId(id, false));
				
				// 设置子表IX_POI_HOTEL
				IxPoiHotelSelector ixPoiHotelSelector = new IxPoiHotelSelector(conn);
				
				ixPoi.setHotels(ixPoiHotelSelector.loadRowsByParentId(id, false));
				
				// 设置子表IX_POI_GASSTATION
				IxPoiGasstationSelector ixPoiGasstationSelector = new IxPoiGasstationSelector(conn);
				
				ixPoi.setGasstations(ixPoiGasstationSelector.loadRowsByParentId(id, false));
				
				//TODO indoor
				
				retList.add(ixPoi);
			}
		} catch (Exception e) {
			throw e;
		}
		return retList;
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

		Geometry geometry = GeoTranslator.struct2Jts(struct, 100000, 0);

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
		
	}
}