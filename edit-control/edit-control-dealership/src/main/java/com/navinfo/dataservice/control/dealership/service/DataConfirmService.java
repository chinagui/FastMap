package com.navinfo.dataservice.control.dealership.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.edit.model.IxDealershipChain;
import com.navinfo.dataservice.api.edit.model.IxDealershipResult;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.control.dealership.service.model.InformationExportResult;
import com.navinfo.navicommons.database.QueryRunner;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class DataConfirmService {
	private Logger log = LoggerRepos.getLogger(DataPrepareService.class);

	private DataConfirmService() {
	}

	private static class SingletonHolder {
		private static final DataConfirmService INSTANCE = new DataConfirmService();
	}

	public static DataConfirmService getInstance() {
		return SingletonHolder.INSTANCE;
	}

	public String[] headers = { "UUID", "情报ID", "省份", "城市", "项目", "代理店分类", "代理店品牌", "厂商提供名称", "厂商提供简称", "厂商提供地址",
			"厂商提供电话（销售）", "厂商提供电话（服务）", "厂商提供电话（其他）", "厂商提供邮编", "厂商提供英文名称", "厂商提供英文地址", "旧一览表ID", "新旧一览表差分结果",
			"与POI的匹配方式", "POI1_NUM", "POI2_NUM", "POI3_NUM", "POI4_NUM", "POI5_NUM", "匹配度", "代理店显示坐标X", "代理店显示坐标Y",
			"待采纳POI外业采集号码", "四维确认备注", "期望时间", "情报类型", "情报等级", "大区ID" };

	/**
	 * 根据品牌代码得到输出到excel的数据
	 * 
	 * @param conn
	 * @param chainCode
	 * @return
	 * @throws Exception
	 */
	public List<InformationExportResult> getOutConfirmList(Connection conn, String chainCode) throws Exception {
		Map<Integer, IxDealershipResult> result = new HashMap<>();
		List<InformationExportResult> arrayResult = new ArrayList<>();
		QueryRunner run = new QueryRunner();

		String sqlstr = "";
		if (!chainCode.isEmpty()) {
			sqlstr = String.format(
					"SELECT * FROM IX_DEALERSHIP_RESULT WHERE WORKFLOW_STATUS = 4 AND CFM_STATUS = 1 AND CHAIN = '%s'",
					chainCode);
		} else {
			sqlstr = "SELECT * FROM IX_DEALERSHIP_RESULT WHERE WORKFLOW_STATUS = 4 AND CFM_STATUS = 1";
		}

		result = IxDealershipResultSelector.getIxDealershipResultsBySql(conn, sqlstr);
		Iterator<Entry<Integer, IxDealershipResult>> interator = result.entrySet().iterator();

		while (interator.hasNext()) {
			InformationExportResult expInfo = new InformationExportResult();
			Map.Entry<Integer, IxDealershipResult> entry = (Map.Entry<Integer, IxDealershipResult>) interator.next();
			IxDealershipResult dealership = (IxDealershipResult) entry.getValue();

			expInfo.setUuid(String.valueOf(dealership.getResultId()));
			expInfo.setInfoId("");
			expInfo.setProvince(dealership.getProvince());
			expInfo.setCity(dealership.getCity());
			expInfo.setProject(dealership.getProject());
			expInfo.setKindCode(dealership.getKindCode());
			expInfo.setChain(dealership.getChain());
			expInfo.setName(dealership.getName());
			expInfo.setNameShort(dealership.getNameShort());
			expInfo.setAddress(dealership.getAddress());
			expInfo.setTelSale(dealership.getTelSale());
			expInfo.setTelService(dealership.getTelService());
			expInfo.setTelOther(dealership.getTelOther());
			expInfo.setPostCode(dealership.getPostCode());
			expInfo.setNameEng(dealership.getNameEng());
			expInfo.setAddressEng(dealership.getAddressEng());
			expInfo.setSourceId(String.valueOf(dealership.getSourceId()));

			switch (dealership.getDealSrcDiff()) {
			case 1:
				expInfo.setDealSrcDiff("一致");
				break;
			case 2:
				expInfo.setDealSrcDiff("删除");
				break;
			case 3:
				expInfo.setDealSrcDiff("新增");
				break;
			case 4:
				expInfo.setDealSrcDiff("更新");
				break;
			default:
				expInfo.setDealSrcDiff("");
				break;
			}

			switch (dealership.getMatchMethod()) {
			case 0:
				expInfo.setMatchMethod("不应用");
				break;
			case 1:
				expInfo.setMatchMethod("POI匹配");
				break;
			case 2:
				expInfo.setMatchMethod("推荐匹配");
				break;
			default:
				expInfo.setMatchMethod("");
				break;
			}

			expInfo.setPoiNum1(dealership.getPoiNum1());
			expInfo.setPoiNum2(dealership.getPoiNum2());
			expInfo.setPoiNum3(dealership.getPoiNum3());
			expInfo.setPoiNum4(dealership.getPoiNum4());
			expInfo.setPoiNum5(dealership.getPoiNum5());
			expInfo.setSimilarity(dealership.getSimilarity());

			Coordinate geo = dealership.getGeometry() == null ? null : dealership.getGeometry().getCoordinate();
			expInfo.setXLocate(geo == null ? "" : String.valueOf(geo.x));
			expInfo.setYLocate(geo == null ? "" : String.valueOf(geo.y));

			expInfo.setCfmPoiNum(dealership.getCfmPoiNum());
			expInfo.setCfmMemo(dealership.getCfmMemo());
			expInfo.setExpectTime("");
			expInfo.setInfoType("");

			String sql = String.format("SELECT CHAIN_WEIGHT FROM IX_DEALERSHIP_CHAIN WHERE CHAIN_CODE = '%s'",
					dealership.getChain());
			int chainWeight = run.queryForInt(conn, sql);
			switch (chainWeight) {
			case 0:
				expInfo.setInfoLevel("1级情报");
				break;
			case 1:
				expInfo.setInfoLevel("2级情报");
				break;
			}

			expInfo.setRegionId(String.valueOf(dealership.getRegionId()));
			arrayResult.add(expInfo);
		}
		return arrayResult;
	}

}
