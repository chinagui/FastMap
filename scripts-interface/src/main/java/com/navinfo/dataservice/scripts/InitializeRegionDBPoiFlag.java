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

	public static void main(String[] args) throws Exception{
		Map<Integer, List<PoiInfo>> poiDataByDailyDbId = createMapBetweenPoiAndDbId();

		for (Map.Entry<Integer, List<PoiInfo>> entry : poiDataByDailyDbId.entrySet()) {
			createPoiFlagTable(entry.getKey(),entry.getValue());
		}
	}

	/**
	 * 读取hadoop库数据，建立Poi与dbId的映射关系，按照dbId存储相应的poi信息
	 * @return
	 * @throws Exception
	 */
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

	/**
	 * 大区日库创建POI_FLAG表，并插入数据。
	 * @param dailyDbId
	 * @param values
	 * @throws Exception
	 */
	private static void createPoiFlagTable(int dailyDbId, List<PoiInfo> values) throws Exception {
		Connection dailyConn = null;
		try {
			dailyConn = DBConnector.getInstance().getConnectionById(dailyDbId);
			String existPoiFlag = String.format("SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME = 'POI_FLAG'");

			int PoiFlagCount = run.queryForInt(dailyConn, existPoiFlag);
			if (PoiFlagCount == 1) {
				log.info(String.format("该库%d已存在POI_FLAG", dailyDbId));
			} else {
				String createPoiFlag = String
						.format("CREATE TABLE POI_FLAG(PID NUMBER(10),VER_RECORD NUMBER(1),SRC_RECORD NUMBER(1),SRC_NAME_CH NUMBER(1),SRC_ADDRESS NUMBER(1),"
								+ "SRC_TELEPHONE NUMBER(1),SRC_COORDINATE NUMBER(1),SRC_NAME_ENG NUMBER(1),SRC_NAME_POR NUMBER(1),FIELD_VERIFIED NUMBER(1),"
								+ "RELIABILITY NUMBER(3),REFRESH_CYCLE NUMBER(3),REFRESH_DATE VARCHAR2(14))");
				run.execute(dailyConn, createPoiFlag);
				log.info(String.format("日库%d创建POI_FLAG:成功", dailyDbId));
			}

			log.info("开始插入POI_FLAG数据：");
			for (PoiInfo poiInfo : values) {
				String isExistPoi = String.format("SELECT COUNT(*) FROM IX_POI WHERE PID = %d", poiInfo.getPid());
				int count = run.queryForInt(dailyConn, isExistPoi);

				if (count == 0) {
					log.info("IX_POI表中不存在pid = " + poiInfo.getPid() + "索引，不插入POI_FLAG表中");
					continue;
				}

				String insertItemToPoiFlag = String.format(
						"INSERT INTO POI_FLAG VALUES(%d, %d, %d, 0, 0, 0, 0, 0, 0, %d, 0, 0, null) WHERE NOT EXISTS (SELECT * FROM POI_FLAG WHERE PID = %d)",
						poiInfo.getPid(), poiInfo.getVerifyRecord(), poiInfo.getSourceRecord(),
						poiInfo.getFieldVerification(), poiInfo.getPid());
				run.execute(dailyConn, insertItemToPoiFlag);
			}
		} catch (Exception e) {
			DBUtils.rollBack(dailyConn);
			throw e;
		} finally {
			if (dailyConn != null) {
				DBUtils.closeConnection(dailyConn);
			}
		}
	}

}
