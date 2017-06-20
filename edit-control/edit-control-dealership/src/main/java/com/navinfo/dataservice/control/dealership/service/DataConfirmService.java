package com.navinfo.dataservice.control.dealership.service;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.edit.model.IxDealershipChain;
import com.navinfo.dataservice.api.edit.model.IxDealershipResult;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.excel.ExcelReader;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.token.AccessTokenFactory;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.control.dealership.service.model.InformationExportResult;
import com.navinfo.dataservice.control.dealership.service.utils.InputStreamUtils;
import com.navinfo.dataservice.engine.editplus.diff.Parser_Tool;
import com.navinfo.navicommons.database.QueryRunner;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONObject;

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

	private QueryRunner run = new QueryRunner();

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
				expInfo.setInfoLevel("1");
				break;
			case 1:
				expInfo.setInfoLevel("2");
				break;
			}

			expInfo.setRegionId(String.valueOf(dealership.getRegionId()));
			arrayResult.add(expInfo);
		}
		return arrayResult;
	}

	/**
	 * 情报下发，（文件是否满足上传规则，更新result库)
	 * 
	 * @param request
	 * @param userId
	 * @throws Exception
	 */
	public JSONObject releaseInfoService(HttpServletRequest request, Long userId) throws Exception {
		JSONObject data = new JSONObject();
		String filePath = SystemConfigFactory.getSystemConfig().getValue(PropConstant.uploadPath)
				+ "/dealership/infoImport";
		JSONObject returnParam = InputStreamUtils.request2File(request, filePath, "info");
		String localFile = returnParam.getString("filePath");
		
		log.info("情报文件已上传至：" + localFile);
		log.info("开始读取情报文件数据");

		try {
			List<Map<String, Object>> importResult = readCsvFile(localFile);
			List<String> uniqueKeys = new ArrayList<>();
			for (Map<String, Object> result : importResult) {

				// 若文件中“情报类型”为空，则整个文件不可以上传；
				if (result.get("infoType") == null || result.get("infoType").toString().isEmpty()) {
					log.error("“情报类型”为空，文件不可以上传");
					throw new Exception("“情报类型”为空，文件不可以上传");
				}

				// 若文件中“UUID”和“情报ID”联合匹配必须唯一，否则整个文件不可导入
				String uniqueKey = result.get("resultId") + "," + result.get("infoId");
				if (uniqueKeys.contains(uniqueKey)) {
					log.error("文件中“UUID”和“情报ID”联合匹配不唯一，文件不可导入");
					throw new Exception("文件中“UUID”和“情报ID”联合匹配不唯一，文件不可导入");
				} else {
					uniqueKeys.add(uniqueKey);
				}
			}

			data = updateResultTable(localFile, userId);
			log.info("情报下发：成功");
		} catch (Exception e) {
			log.error("情报下发，失败：" + e.getMessage());
			throw e;
		}
		return data;
	}

	/**
	 * 上传成功后，连接情报库，按要求更新RESULT数据库
	 * 
	 * @param filePath
	 * @param userId
	 * @throws Exception
	 */
	public JSONObject updateResultTable(String filePath, Long userId) throws Exception {
		Connection conn = null;
		JSONObject data = new JSONObject();
		try {
			conn = DBConnector.getInstance().getDealershipConnection();
			String accessToken = AccessTokenFactory.generate(userId).getTokenString();
			String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("csvname", fileName);

			StringBuilder urlStr = new StringBuilder();
			urlStr.append(
					"http://fs-road.navinfo.com/dev/trunk/service/mapspotter/data/info/agent/import/?access_token=");
			urlStr.append(accessToken);
			urlStr.append("&parameter=");
			urlStr.append(URLEncoder.encode(jsonObj.toString(), "utf-8"));

			String return_value = Parser_Tool.do_get(urlStr.toString());
			JSONObject resultObj = JSONObject.fromObject(return_value);

			log.info("输入的URL：" + urlStr);
			log.info("接口返回结果：" + resultObj);

			String successList = resultObj.getString("successList");
			String[] successLists = successList.replace("[", "").replace("]", "").split(",");

			for (String success : successLists) {
				String sql = String.format(
						"UPDATE IX_DEALERSHIP_RESULT SET CFM_STATUS = 2,TO_INFO_DATE = %s WHERE RESULT_ID = '%s'",
						DateUtils.dateToString(new Date(), "yyyyMMddHHmmss"), success.replace("\"", ""));
				run.execute(conn, sql);
				conn.commit();
			}

			String[] generateFail = resultObj.getString("generateFailedList").replace("[", "").replace("]", "")
					.split(",");
			String[] insertFail = resultObj.getString("insertFailedList").replace("[", "").replace("]", "").split(",");
			String[] sendFail = resultObj.getString("sendFailedList").replace("[", "").replace("]", "").split(",");

			data.put("successCount", successLists.length);
			data.put("failCount", generateFail.length + insertFail.length + sendFail.length);

		} catch (Exception e) {
			conn.rollback();
			log.error(e.getMessage());
			throw e;
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
		return data;
	}

	public Map<String, String> importInfoExcel() throws Exception {
		Map<String, String> excelHeader = new HashMap<String, String>();

		excelHeader.put("UUID", "resultId");
		excelHeader.put("情报ID", "infoId");
		excelHeader.put("省份", "province");
		excelHeader.put("城市", "city");
		excelHeader.put("项目", "project");
		excelHeader.put("代理店分类", "kindCode");
		excelHeader.put("代理店品牌", "chain");
		excelHeader.put("厂商提供名称", "name");
		excelHeader.put("厂商提供简称", "nameShort");
		excelHeader.put("厂商提供地址", "address");
		excelHeader.put("厂商提供电话（销售）", "telSale");
		excelHeader.put("厂商提供电话（服务）", "telService");
		excelHeader.put("厂商提供电话（其他）", "telOther");
		excelHeader.put("厂商提供邮编", "postcode");
		excelHeader.put("厂商提供英文名称", "nameEng");
		excelHeader.put("厂商提供英文地址", "addressEng");
		excelHeader.put("旧一览表ID", "sourceId");
		excelHeader.put("新旧一览表差分结果", "dealSrcDiff");
		excelHeader.put("与POI的匹配方式", "matchMethod");
		excelHeader.put("POI1_NUM", "poiNum1");
		excelHeader.put("POI2_NUM", "poiNum2");
		excelHeader.put("POI3_NUM", "poiNum3");
		excelHeader.put("POI4_NUM", "poiNum4");
		excelHeader.put("POI5_NUM", "poiNum5");
		excelHeader.put("匹配度", "similarity");
		excelHeader.put("代理店显示坐标X", "xLocate");
		excelHeader.put("代理店显示坐标Y", "yLocate");
		excelHeader.put("待采纳POI外业采集号码", "cfmPoiNum");
		excelHeader.put("四维确认备注", "cfmMemo");
		excelHeader.put("期望时间", "expectTime");
		excelHeader.put("情报类型", "infoType");
		excelHeader.put("情报等级", "infoLevel");
		excelHeader.put("大区ID", "regionId");

		return excelHeader;
	}
	
	public List<Map<String, Object>> readCsvFile(String uploadFile) throws Exception {
		DataInputStream in = new DataInputStream(new FileInputStream(new File(uploadFile))); 
		BufferedReader br= new BufferedReader(new InputStreamReader(in,"GBK"));
		
		List<Map<String, Object>> sourceResult = new ArrayList<>();
		Map<String, String> excelHeader = importInfoExcel();

		String line = "";
		int n = 0;

		while ((line = br.readLine()) != null) {
			n++;
			if (n == 1)
				continue;
			Map<String, Object> cell = new HashMap<>();

			String[] cellsValue = line.split(",");
			for (int i = 0; i < cellsValue.length; i++) {
				if (excelHeader.containsKey(headers[i])) {
					cell.put(excelHeader.get(headers[i]), cellsValue[i]);
				}
			}
			sourceResult.add(cell);
		}
		return sourceResult;
	}
}
