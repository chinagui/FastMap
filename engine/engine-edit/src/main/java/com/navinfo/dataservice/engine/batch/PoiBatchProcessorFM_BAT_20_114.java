package com.navinfo.dataservice.engine.batch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiGasstation;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.engine.batch.util.IBatch;
import com.navinfo.dataservice.engine.edit.service.EditApiImpl;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;
public class PoiBatchProcessorFM_BAT_20_114 implements IBatch {
	private static final Logger logger = Logger.getLogger(PoiBatchProcessorFM_BAT_20_114.class);
	@Override
	public JSONObject run(IxPoi poi,Connection conn,JSONObject json,EditApiImpl editApiImpl) throws Exception {
		JSONObject result = new JSONObject();
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;
		
		try {
			if (poi.getGasstations().size()==0) {
				return result;
			}
			
			if (poi.getuRecord() == 2) {
				return result;
			}
			
			String adminId = "";
			String regionId = String.valueOf(poi.getRegionId());
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT A.admin_id");
			sql.append(" FROM AD_ADMIN A, AD_FACE F");
			sql.append(" WHERE F.REGION_ID = A.REGION_ID");
			sql.append(" AND sdo_Geom.Relate(F.GEOMETRY,'Anyinteract',sdo_geometry(:1,8307),0.005) = 'TRUE'");
			sql.append(" and rownum=1");
			Geometry geometry = poi.getGeometry();
			String wkt = GeoTranslator.jts2Wkt(geometry,0.00001,5);
			logger.info("wkt："+wkt);
			pstmt = conn.prepareStatement(sql.toString());
			pstmt.setString(1, wkt);
			resultSet = pstmt.executeQuery();
			if (resultSet.next()) {
				adminId = resultSet.getString("admin_id");
			} else {
				logger.error("未知的行政区划,regionId:"+regionId);
				return result;
			}
			logger.info("adminId："+adminId);
			if (adminId.startsWith("44") || adminId.startsWith("110") || adminId.startsWith("310")
					|| adminId.startsWith("3201") || adminId.startsWith("3202") || adminId.startsWith("3204")
					|| adminId.startsWith("3205") || adminId.startsWith("3206") || adminId.startsWith("3210")
					|| adminId.startsWith("3211") || adminId.startsWith("3212") || adminId.startsWith("12")) {
				
				List<IRow> gasstationList = poi.getGasstations();
				JSONArray dataArray = new JSONArray();
				
				for (IRow gasstation:gasstationList) {
					IxPoiGasstation ixPoiGasstation = (IxPoiGasstation) gasstation;
					
					int gasURecord = ixPoiGasstation.getuRecord();
					
					if (gasURecord == 2) {
						continue;
					}
					
					String oilType = ixPoiGasstation.getOilType();
					logger.info("oilType："+oilType);
					if (StringUtils.isEmpty(oilType)){
						continue;
					}
					boolean changeFlag = false;
					if (oilType.indexOf("90")>-1) {
						if (oilType.indexOf("89")>-1) {
							if (oilType.indexOf("|90")>-1) {
								oilType = oilType.replace("|90", "");
							} else if (oilType.indexOf("90|")>-1) {
								oilType = oilType.replace("90|", "");
							} else {
								oilType = oilType.replace("90", "");
							}
						} else {
							oilType = oilType.replace("90", "89");
						}
						changeFlag = true;
					}
					if (oilType.indexOf("93")>-1) {
						if (oilType.indexOf("92")>-1) {
							if (oilType.indexOf("|93")>-1) {
								oilType = oilType.replace("|93", "");
							} else if (oilType.indexOf("93|")>-1){
								oilType = oilType.replace("93|", "");
							} else {
								oilType = oilType.replace("93", "");
							}
						} else {
							oilType = oilType.replace("93", "92");
						}
						changeFlag = true;
					}
					if (oilType.indexOf("97")>-1) {
						if (oilType.indexOf("95")>-1) {
							if (oilType.indexOf("|97")>-1) {
								oilType = oilType.replace("|97", "");
							} else if (oilType.indexOf("97|")>-1){
								oilType = oilType.replace("97|", "");
							} else {
								oilType = oilType.replace("97", "");
							}
						} else {
							oilType = oilType.replace("97", "95");
						}
						changeFlag = true;
					}
					if (changeFlag) {
						ixPoiGasstation.setOilType(oilType);
						JSONObject changeFields = ixPoiGasstation.Serialize(null);
						changeFields.remove("uDate");
						changeFields.put("objStatus", ObjStatus.UPDATE.toString());
						dataArray.add(changeFields);
					}
				}
				
				if (dataArray.size()>0) {
					result.put("gasstations", dataArray);
				}
				
			}
			
			return result;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
	}

}
