package com.navinfo.dataservice.scripts;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.RegionMesh;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.sql.DBUtils;

import net.sf.json.JSONObject;

/**
 * @des hadoop库初始化大区库
 * @author fhx
 *
 */
public class InitializeRegionDBPoiFlag {
	private static Logger log = LoggerRepos.getLogger(InitializeRegionDBPoiFlag.class);
	private static QueryRunner run = new QueryRunner();

	public static void main(String[] args) throws Exception {
		Map<Integer, List<PoiInfo>> poiDataByDailyDbId = createMapBetweenPoiAndDbId();
		
		for(Map.Entry<Integer, List<PoiInfo>> entry:poiDataByDailyDbId.entrySet()){
			createPoiFlagTable(entry.getKey());
		}
	}

	// 读取hadoop库，建立Poi与dbId的映射，按照dbId存储相应的poi信息
	private static Map<Integer, List<PoiInfo>> createMapBetweenPoiAndDbId() throws Exception {
		Map<Integer, List<PoiInfo>> poiDataByDailyDbId = new HashMap<>();
		HbasePoiInfo hbaseData = new HbasePoiInfo();
		hbaseData.getHBaseDataInfo();

		ManApi manApi = (ManApi) ApplicationContextUtil.getBean("manApi");
		List<RegionMesh> regions = manApi.queryRegionWithMeshes(hbaseData.getPoiCollectionByMesh().keySet());

		if (regions == null || regions.size() == 0) {
			log.error("根据图幅未查询到所属大区库信息");
			throw new Exception("根据图幅未查询到所属大区库信息");
		}

		// 以图幅为主键的数据，更新为以dbId为主键
		for (Map.Entry<String, List<PoiInfo>> entry : hbaseData.getPoiCollectionByMesh().entrySet()) {
			for (RegionMesh region : regions) {
				if (!region.getMeshes().contains(entry.getKey())) {
					continue;
				}

				if (poiDataByDailyDbId.containsKey(region.getDailyDbId())) {
					poiDataByDailyDbId.get(region.getDailyDbId()).addAll(entry.getValue());
				} else {
					poiDataByDailyDbId.put(region.getDailyDbId(), entry.getValue());
				}
			} // for
		} // for

		hbaseData.clearCollection();
		return poiDataByDailyDbId;
	}

	// 创建poi_flag表，pid赋值为ix_poi的pid

	private static void createPoiFlagTable(int dailyDbId) throws Exception {
		Connection dailyConn = null;
		try {
			dailyConn = DBConnector.getInstance().getConnectionById(dailyDbId);
			String existPoiFlag = String.format("SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME = 'POI_FLAG'");

			int PoiFlagCount = run.queryForInt(dailyConn, existPoiFlag);
			if (PoiFlagCount == 1) {
				log.info(String.format("该库%d已存在POI_FLAG",dailyDbId));
				return;
			}

			String createPoiFlag = String
					.format("CREATE TABLE POI_FLAG(PID NUMBER(10),VER_RECORD NUMBER(1),SRC_RECORD NUMBER(1),SRC_NAME_CH NUMBER(1),SRC_ADDRESS NUMBER(1),"
							+ "SRC_TELEPHONE NUMBER(1),SRC_COORDINATE NUMBER(1),SRC_NAME_ENG NUMBER(1),SRC_NAME_POR NUMBER(1),FIELD_VERIFIED NUMBER(1),"
							+ "RELIABILITY NUMBER(3),REFRESH_CYCLE NUMBER(3),REFRESH_DATE VARCHAR2(14))");
			run.execute(dailyConn, createPoiFlag);
			log.info(String.format("日库%d创建POI_FLAG:成功",dailyDbId));

		} catch (Exception e) {
			throw e;
		} finally {
			if (dailyConn != null) {
				DBUtils.closeConnection(dailyConn);
			}
		}
	}

}
