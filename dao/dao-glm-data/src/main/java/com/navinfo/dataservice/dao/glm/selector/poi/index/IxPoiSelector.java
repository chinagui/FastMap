package com.navinfo.dataservice.dao.glm.selector.poi.index;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import org.apache.commons.lang.StringUtils;



import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiAdvertisement;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiAttraction;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiBuilding;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiBusinessTime;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiCarrental;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiChargingPlot;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiChargingPlotPh;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiChargingStation;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiDetail;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiGasstation;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiHotel;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiIntroduction;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiParking;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiRestaurant;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiAddress;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiAudio;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiChildren;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiContact;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiEntryimage;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiFlag;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiIcon;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiName;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiOperateRef;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiParent;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiPhoto;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiVideo;
import com.navinfo.dataservice.dao.glm.selector.poi.deep.IxPoiAdvertisementSelector;
import com.navinfo.dataservice.dao.glm.selector.poi.deep.IxPoiAttractionSelector;
import com.navinfo.dataservice.dao.glm.selector.poi.deep.IxPoiBuildingSelector;
import com.navinfo.dataservice.dao.glm.selector.poi.deep.IxPoiBusinessTimeSelector;
import com.navinfo.dataservice.dao.glm.selector.poi.deep.IxPoiCarrentalSelector;
import com.navinfo.dataservice.dao.glm.selector.poi.deep.IxPoiChargingPlotPhSelector;
import com.navinfo.dataservice.dao.glm.selector.poi.deep.IxPoiChargingPlotSelector;
import com.navinfo.dataservice.dao.glm.selector.poi.deep.IxPoiChargingStationSelector;
import com.navinfo.dataservice.dao.glm.selector.poi.deep.IxPoiDetailSelector;
import com.navinfo.dataservice.dao.glm.selector.poi.deep.IxPoiGasstationSelector;
import com.navinfo.dataservice.dao.glm.selector.poi.deep.IxPoiHotelSelector;
import com.navinfo.dataservice.dao.glm.selector.poi.deep.IxPoiIntroductionSelector;
import com.navinfo.dataservice.dao.glm.selector.poi.deep.IxPoiParkingSelector;
import com.navinfo.dataservice.dao.glm.selector.poi.deep.IxPoiRestaurantSelector;
import com.vividsolutions.jts.geom.Geometry;

/**
 * POI基础信息表 selector
 * 
 * @author zhangxiaolong
 * 
 */
public class IxPoiSelector implements ISelector {

	private Connection conn;

	public IxPoiSelector(Connection conn) {
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {
		IxPoi ixPoi = new IxPoi();

		StringBuilder sb = new StringBuilder("select * from "
				+ ixPoi.tableName() + " WHERE pid = :1 and  u_record !=2");

		if (isLock) {
			sb.append(" for update nowait");
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setInt(1, id);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {

				// 设置主表信息
				setAttr(ixPoi, resultSet);

				// 设置子表IX_POI_NAME
				IxPoiNameSelector poiNameSelector = new IxPoiNameSelector(conn);

				ixPoi.setNames(poiNameSelector.loadRowsByParentId(id, isLock));

				for (IRow row : ixPoi.getNames()) {

					IxPoiName obj = (IxPoiName) row;

					ixPoi.nameMap.put(obj.getRowId(), obj);
				}
				
				//设置POI_EDIT_STATUS
				IxPoiEditStatusSelector ixPoiEditStatusSelector = new IxPoiEditStatusSelector(conn);
				
				int status = ixPoiEditStatusSelector.loadStatusByRowId(ixPoi.getRowId(), isLock);
				
				ixPoi.setStatus(status);
				
				// 设置子表IX_POI_ADDRESS
				IxPoiAddressSelector ixPoiAddressSelector = new IxPoiAddressSelector(
						conn);

				ixPoi.setAddresses(ixPoiAddressSelector.loadRowsByParentId(id,
						isLock));

				for (IRow row : ixPoi.getAddresses()) {

					IxPoiAddress obj = (IxPoiAddress) row;

					ixPoi.addressMap.put(obj.getRowId(), obj);
				}

				// 设置子表IX_POI_CONTACT
				IxPoiContactSelector ixPoiContactSelector = new IxPoiContactSelector(
						conn);

				ixPoi.setContacts(ixPoiContactSelector.loadRowsByParentId(id,
						isLock));

				for (IRow row : ixPoi.getContacts()) {
					IxPoiContact obj = (IxPoiContact) row;

					ixPoi.contactMap.put(obj.getRowId(), obj);
				}

				// 设置子表IX_POI_FLAG
				IxPoiFlagSelector ixPoiFlagSelector = new IxPoiFlagSelector(
						conn);

				ixPoi.setFlags(ixPoiFlagSelector.loadRowsByParentId(id, isLock));

				for (IRow row : ixPoi.getFlags()) {
					IxPoiFlag obj = (IxPoiFlag) row;

					ixPoi.flagMap.put(obj.getRowId(), obj);
				}

				// 设置子表IX_POI_ENTRYIMAGE
				IxPoiEntryImageSelector ixPoiEntryImageSelector = new IxPoiEntryImageSelector(
						conn);

				ixPoi.setEntryImages(ixPoiEntryImageSelector
						.loadRowsByParentId(id, isLock));

				for (IRow row : ixPoi.getEntryImages()) {
					IxPoiEntryimage obj = (IxPoiEntryimage) row;

					ixPoi.entryImageMap.put(obj.getRowId(), obj);
				}

				// 设置子表IX_POI_ICON
				IxPoiIconSelector ixPoiIconSelector = new IxPoiIconSelector(
						conn);

				ixPoi.setIcons(ixPoiIconSelector.loadRowsByParentId(id, isLock));

				for (IRow row : ixPoi.getIcons()) {
					IxPoiIcon obj = (IxPoiIcon) row;

					ixPoi.iconMap.put(obj.getRowId(), obj);
				}

				// 设置子表IX_POI_PHOTO*
				IxPoiPhotoSelector ixPoiPhotoSelector = new IxPoiPhotoSelector(
						conn);

				ixPoi.setPhotos(ixPoiPhotoSelector.loadRowsByParentId(id,
						isLock));

				for (IRow row : ixPoi.getPhotos()) {
					IxPoiPhoto obj = (IxPoiPhoto) row;

					ixPoi.photoMap.put(obj.getRowId(), obj);
				}

				// 设置子表IX_POI_AUDIO*
				IxPoiAudioSelector ixPoiAudioSelector = new IxPoiAudioSelector(
						conn);

				ixPoi.setAudioes(ixPoiAudioSelector.loadRowsByParentId(id,
						isLock));

				for (IRow row : ixPoi.getAudioes()) {
					IxPoiAudio obj = (IxPoiAudio) row;

					ixPoi.audioMap.put(obj.getRowId(), obj);
				}

				// 设置子表IX_POI_VIDEO*
				IxPoiVideoSelector ixPoiVideoSelector = new IxPoiVideoSelector(
						conn);

				ixPoi.setVideoes(ixPoiVideoSelector.loadRowsByParentId(id,
						isLock));

				for (IRow row : ixPoi.getVideoes()) {
					IxPoiVideo obj = (IxPoiVideo) row;

					ixPoi.videoMap.put(obj.getRowId(), obj);
				}

				// 设置子表IX_POI_PARENT
				IxPoiParentSelector ixPoiParentSelector = new IxPoiParentSelector(
						conn);

				ixPoi.setParents(ixPoiParentSelector.loadParentRowsByPoiId(id,
						isLock));
				
				int groupId = 0;
				
				for (IRow row : ixPoi.getParents()) {
					IxPoiParent obj = (IxPoiParent) row;
					
					groupId = obj.getPid();
					
					ixPoi.parentMap.put(obj.getRowId(), obj);
				}
				
				// 设置子表IX_POI_CHILDREN
				IxPoiChildrenSelector ixPoiChildrenSelector = new IxPoiChildrenSelector(
						conn);
				
				ixPoi.setChildren(ixPoiChildrenSelector.loadRowsByParentId(groupId,
						isLock));

				for (IRow row : ixPoi.getChildren()) {
					IxPoiChildren obj = (IxPoiChildren) row;

					ixPoi.childrenMap.put(obj.getRowId(), obj);
				}

				// 设置子表IX_POI_PARKING
				IxPoiParkingSelector ixPoiParkingSelector = new IxPoiParkingSelector(
						conn);

				ixPoi.setParkings(ixPoiParkingSelector.loadRowsByParentId(id,
						isLock));

				for (IRow row : ixPoi.getParkings()) {
					IxPoiParking obj = (IxPoiParking) row;

					ixPoi.parkingMap.put(obj.getRowId(), obj);
				}

				// 设置子表IX_POI_DETAIL
				IxPoiDetailSelector ixPoiDetailSelector = new IxPoiDetailSelector(
						conn);

				ixPoi.setDetails(ixPoiDetailSelector.loadRowsByParentId(id,
						isLock));

				for (IRow row : ixPoi.getDetails()) {
					IxPoiDetail obj = (IxPoiDetail) row;

					ixPoi.detailMap.put(obj.getRowId(), obj);
				}

				// 设置子表IX_POI_BUSINESSTIME
				IxPoiBusinessTimeSelector ixPoiBusinessTimeSelector = new IxPoiBusinessTimeSelector(
						conn);

				ixPoi.setBusinesstimes(ixPoiBusinessTimeSelector
						.loadRowsByParentId(id, isLock));

				for (IRow row : ixPoi.getBusinesstimes()) {
					IxPoiBusinessTime obj = (IxPoiBusinessTime) row;

					ixPoi.businesstimeMap.put(obj.getRowId(), obj);
				}

				// 设置子表IX_POI_CHARGINGSTATION
				IxPoiChargingStationSelector ixPoiChargingStationSelector = new IxPoiChargingStationSelector(
						conn);

				ixPoi.setChargingstations(ixPoiChargingStationSelector
						.loadRowsByParentId(id, isLock));

				for (IRow row : ixPoi.getChargingstations()) {
					IxPoiChargingStation obj = (IxPoiChargingStation) row;

					ixPoi.chargingstationMap.put(obj.getRowId(), obj);
				}
				// 设置子表IX_POI_BUSINESSTIME
				IxPoiOperateRefSelector IxPoiOperateRefSelector = new IxPoiOperateRefSelector(
						conn);

				ixPoi.setBusinesstimes(IxPoiOperateRefSelector
						.loadRowsByParentId(id, isLock));

				for (IRow row : ixPoi.getOperateRefs()) {
					IxPoiOperateRef obj = (IxPoiOperateRef) row;

					ixPoi.operateRefMap.put(obj.getRowId(), obj);
				}

				// 设置子表IX_POI_CHARGINGPLOT
				IxPoiChargingPlotSelector ixPoiChargingPlotSelector = new IxPoiChargingPlotSelector(
						conn);

				ixPoi.setChargingplots(ixPoiChargingPlotSelector
						.loadRowsByParentId(id, isLock));

				for (IRow row : ixPoi.getChargingplots()) {
					IxPoiChargingPlot obj = (IxPoiChargingPlot) row;

					ixPoi.chargingplotMap.put(obj.getRowId(), obj);
				}

				// 设置子表IX_POI_CHARGINGPLOT_PH
				IxPoiChargingPlotPhSelector ixPoiChargingPlotPhSelector = new IxPoiChargingPlotPhSelector(
						conn);

				ixPoi.setChargingplotPhs(ixPoiChargingPlotPhSelector
						.loadRowsByParentId(id, isLock));

				for (IRow row : ixPoi.getChargingplotPhs()) {
					IxPoiChargingPlotPh obj = (IxPoiChargingPlotPh) row;

					ixPoi.chargingplotPhMap.put(obj.getRowId(), obj);
				}

				// 设置子表IX_POI_BUILDING
				IxPoiBuildingSelector ixPoiBuildingSelector = new IxPoiBuildingSelector(
						conn);

				ixPoi.setBuildings(ixPoiBuildingSelector.loadRowsByParentId(id,
						isLock));

				for (IRow row : ixPoi.getBuildings()) {
					IxPoiBuilding obj = (IxPoiBuilding) row;

					ixPoi.buildingMap.put(obj.getRowId(), obj);
				}

				// 设置子表IX_POI_ADVERTISEMENT
				IxPoiAdvertisementSelector ixPoiAdvertisementSelector = new IxPoiAdvertisementSelector(
						conn);

				ixPoi.setAdvertisements(ixPoiAdvertisementSelector
						.loadRowsByParentId(id, isLock));

				for (IRow row : ixPoi.getAdvertisements()) {
					IxPoiAdvertisement obj = (IxPoiAdvertisement) row;

					ixPoi.advertisementMap.put(obj.getRowId(), obj);
				}

				// 设置子表IX_POI_GASSTATION
				IxPoiGasstationSelector ixPoiGasstationSelector = new IxPoiGasstationSelector(
						conn);

				ixPoi.setGasstations(ixPoiGasstationSelector
						.loadRowsByParentId(id, isLock));

				for (IRow row : ixPoi.getGasstations()) {
					IxPoiGasstation obj = (IxPoiGasstation) row;

					ixPoi.gasstationMap.put(obj.getRowId(), obj);
				}

				// 设置子表IX_POI_INTRODUCTION
				IxPoiIntroductionSelector ixPoiIntroductionSelector = new IxPoiIntroductionSelector(
						conn);

				ixPoi.setIntroductions(ixPoiIntroductionSelector
						.loadRowsByParentId(id, isLock));

				for (IRow row : ixPoi.getIntroductions()) {
					IxPoiIntroduction obj = (IxPoiIntroduction) row;

					ixPoi.introductionMap.put(obj.getRowId(), obj);
				}

				// 设置子表IX_POI_ATTRACTION
				IxPoiAttractionSelector ixPoiAttractionSelector = new IxPoiAttractionSelector(
						conn);

				ixPoi.setAttractions(ixPoiAttractionSelector
						.loadRowsByParentId(id, isLock));

				for (IRow row : ixPoi.getAttractions()) {
					IxPoiAttraction obj = (IxPoiAttraction) row;

					ixPoi.attractionMap.put(obj.getRowId(), obj);
				}

				// 设置子表IX_POI_HOTEL
				IxPoiHotelSelector ixPoiHotelSelector = new IxPoiHotelSelector(
						conn);

				ixPoi.setHotels(ixPoiHotelSelector.loadRowsByParentId(id,
						isLock));

				for (IRow row : ixPoi.getHotels()) {
					IxPoiHotel obj = (IxPoiHotel) row;

					ixPoi.hotelMap.put(obj.getRowId(), obj);
				}

				// 设置子表IX_POI_RESTAURANT
				IxPoiRestaurantSelector ixPoiRestaurantSelector = new IxPoiRestaurantSelector(
						conn);

				ixPoi.setRestaurants(ixPoiRestaurantSelector
						.loadRowsByParentId(id, isLock));

				for (IRow row : ixPoi.getRestaurants()) {
					IxPoiRestaurant obj = (IxPoiRestaurant) row;

					ixPoi.restaurantMap.put(obj.getRowId(), obj);
				}

				// 设置子表IX_POI_CARRENTAL
				IxPoiCarrentalSelector ixPoiCarrentalSelector = new IxPoiCarrentalSelector(
						conn);

				ixPoi.setCarrentals(ixPoiCarrentalSelector.loadRowsByParentId(
						id, isLock));

				for (IRow row : ixPoi.getCarrentals()) {
					IxPoiCarrental obj = (IxPoiCarrental) row;

					ixPoi.carrentalMap.put(obj.getRowId(), obj);
				}

				return ixPoi;
			} else {

				throw new Exception("对应IX_POI对象不存在!");
			}
		} catch (Exception e) {

			throw e;

		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
			} catch (Exception e) {

			}

			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (Exception e) {

			}

		}
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {
		return null;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock)
			throws Exception {
		return null;
	}

	public JSONObject loadPids(boolean isLock,int pid ,String pidName,int pageSize, int pageNum) throws Exception {

		JSONObject result = new JSONObject();

		JSONArray array = new JSONArray();

		int total = 0;
		int startRow = pageNum * pageSize + 1;

		int endRow = (pageNum + 1) * pageSize;
		StringBuilder buffer = new StringBuilder();
        buffer.append(" SELECT * ");
        buffer.append(" FROM (SELECT c.*, ROWNUM rn ");
        buffer.append(" FROM (SELECT COUNT (1) OVER (PARTITION BY 1) total,");
        //TODO 0 as freshness_vefication
        buffer.append(" ip.pid,ip.kind_code, 0 as freshness_vefication,ipn.name,ip.geometry,ip.collect_time,ip.u_record ");
        buffer.append(" FROM ix_poi ip, ix_poi_name ipn ");
        buffer.append(" WHERE     ip.pid = ipn.poi_pid ");
        buffer.append(" AND lang_code = 'CHI'");
        buffer.append(" AND ipn.name_type = 2 ");
        buffer.append(" AND name_class = 1"); 
        if( pid != 0){
        	buffer.append("AND ip.pid = "+pid+"");
        }else{
        	if(StringUtils.isNotBlank(pidName)){
        		buffer.append("AND ipn.name like %'"+pidName+"%'");
        	}
        }
        
        buffer.append(" ) c");
        buffer.append(" WHERE ROWNUM <= :1) ");
        buffer.append("  WHERE rn >= :2 ");
		if (isLock) {
			buffer.append(" for update nowait");
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;
		
		try {
			pstmt = conn.prepareStatement(buffer.toString());
			pstmt.setInt(1, endRow);

			pstmt.setInt(2, startRow);
			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				if (total == 0) {
					total = resultSet.getInt("total");
				}
				STRUCT struct = (STRUCT) resultSet.getObject("geometry");
				Geometry geometry = GeoTranslator.struct2Jts(struct, 1, 0);
				JSONObject json = new JSONObject();
				json.put("pid", resultSet.getInt("pid"));
				json.put("kindCode", resultSet.getString("kind_code"));
				json.put("freshnessVefication", resultSet.getInt("freshness_vefication"));
				json.put("name", resultSet.getString("name"));
				json.put("geometry", GeoTranslator.jts2Geojson(geometry));
				json.put("uRecord", resultSet.getInt("u_record"));
				json.put("collectTime", resultSet.getString("collect_time"));
				array.add(json);
			}
			result.put("total", total);

			result.put("rows", array);

			return result;
		} catch (Exception e) {

			throw e;

		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
			} catch (Exception e) {

			}

			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (Exception e) {

			}

		}
	}

	/**
	 * 设置属性
	 * 
	 * @param ixPoi
	 * @param resultSet
	 * @throws Exception
	 */
	private void setAttr(IxPoi ixPoi, ResultSet resultSet) throws Exception {
		ixPoi.setPid(resultSet.getInt("pid"));

		ixPoi.setKindCode(resultSet.getString("kind_code"));

		STRUCT struct = (STRUCT) resultSet.getObject("geometry");

		Geometry geometry = GeoTranslator.struct2Jts(struct, 100000, 0);

		ixPoi.setGeometry(geometry);

		ixPoi.setxGuide(resultSet.getDouble("x_guide"));

		ixPoi.setyGuide(resultSet.getDouble("y_guide"));

		ixPoi.setLinkPid(resultSet.getInt("link_pid"));

		ixPoi.setSide(resultSet.getInt("side"));

		ixPoi.setNameGroupid(resultSet.getInt("name_groupid"));

		ixPoi.setRoadFlag(resultSet.getInt("road_flag"));

		ixPoi.setPmeshId(resultSet.getInt("pmesh_id"));

		ixPoi.setAdminReal(resultSet.getInt("admin_real"));

		ixPoi.setImportance(resultSet.getInt("importance"));

		ixPoi.setChain(resultSet.getString("chain"));

		ixPoi.setAirportCode(resultSet.getString("airport_code"));

		ixPoi.setAccessFlag(resultSet.getInt("access_flag"));

		ixPoi.setOpen24h(resultSet.getInt("open_24h"));

		ixPoi.setMeshId5k(resultSet.getString("mesh_id_5k"));

		ixPoi.setMeshId(resultSet.getInt("mesh_id"));

		ixPoi.setRegionId(resultSet.getInt("region_id"));

		ixPoi.setPostCode(resultSet.getString("post_code"));

		ixPoi.setEditFlag(resultSet.getInt("edit_flag"));

		ixPoi.setDifGroupid(resultSet.getString("dif_groupid"));

		ixPoi.setReserved(resultSet.getString("reserved"));

		ixPoi.setState(resultSet.getInt("state"));

		ixPoi.setFieldState(resultSet.getString("field_state"));

		ixPoi.setLabel(resultSet.getString("label"));

		ixPoi.setType(resultSet.getInt("type"));

		ixPoi.setAddressFlag(resultSet.getInt("address_flag"));

		ixPoi.setExPriority(resultSet.getString("ex_priority"));

		ixPoi.setEditFlag(resultSet.getInt("edition_flag"));

		ixPoi.setPoiMemo(resultSet.getString("poi_memo"));

		ixPoi.setOldBlockcode(resultSet.getString("old_blockcode"));

		ixPoi.setOldName(resultSet.getString("old_name"));

		ixPoi.setOldAddress(resultSet.getString("old_address"));

		ixPoi.setOldKind(resultSet.getString("old_kind"));
		
		ixPoi.setPoiNum(resultSet.getString("poi_num"));

		ixPoi.setLog(resultSet.getString("log"));

		ixPoi.setTaskId(resultSet.getInt("task_id"));

		ixPoi.setDataVersion(resultSet.getString("data_version"));

		ixPoi.setFieldTaskId(resultSet.getInt("field_task_id"));

		ixPoi.setVerifiedFlag(resultSet.getInt("verified_flag"));

		ixPoi.setCollectTime(resultSet.getString("collect_time"));

		ixPoi.setGeoAdjustFlag(resultSet.getInt("geo_adjust_flag"));

		ixPoi.setFullAttrFlag(resultSet.getInt("full_attr_flag"));

		ixPoi.setOldXGuide(resultSet.getDouble("old_x_guide"));

		ixPoi.setOldYGuide(resultSet.getDouble("old_y_guide"));

		ixPoi.setRowId(resultSet.getString("row_id"));
		
		ixPoi.setuDate(resultSet.getString("u_date"));

	}
}
