package com.navinfo.dataservice.control.dealership.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URLEncoder;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

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

	public String[] feedbackHeaders = { "UUID", "情报ID", "省份", "城市", "代理店分类", "代理店品牌", "厂商提供名称", "厂商提供地址", "厂商提供电话",
			"四维确认备注", "代理店显示坐标X", "代理店显示坐标Y", "情报对应的要素外业采集ID", "情报是否被采纳", "情报未采纳原因", "情报未采纳或部分采纳备注", "任务号", "情报类型",
			"情报等级", "反馈时间" };

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
		String xlslocalFile = returnParam.getString("filePath");
		ExcelReader reader = new ExcelReader(xlslocalFile);
		List<Integer> infoTypeList = Arrays.asList(1,2,3);

		log.info("情报文件已上传至：" + xlslocalFile);
		log.info("开始读取情报文件数据");

		try {
			//List<Map<String, Object>> importResult = readCsvFile(localFile, headers, importInfoHeader());
			List<Map<String, Object>> importResult = reader.readExcelContent(importInfoHeader());
			List<String> uniqueKeys = new ArrayList<>();
			for (Map<String, Object> result : importResult) {

				// 若文件中“情报类型”为空 的值域必须在｛1，2，3｝范围内［41］，否则整个文件不可以上传；
				String infoType = result.get("infoType").toString();
				if (StringUtils.isEmpty(infoType)) {
					log.error("“情报类型”为空，文件不可以上传");
					throw new Exception("“情报类型”为空，文件不可以上传");
				} else {
					if(StringUtils.isNumeric(infoType) == false || !infoTypeList.contains(Integer.valueOf(infoType))){
						log.error("“情报类型”值不在{1,2,3}范围内，文件不可以上传");
						throw new Exception("“情报类型”值不在{1,2,3}范围内，文件不可以上传");
					}
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
			
			//根据excel生成csv文件
			String localFile = xls2csv(importResult,xlslocalFile);
			data = updateResultTable(localFile, userId);
			log.info("情报下发：成功");
		} catch (Exception e) {
			log.error("情报下发，失败：" + e.getMessage());
			throw e;
		}
		return data;
	}
	
	public String xls2csv(List<Map<String, Object>> cellValue, String localPath) throws Exception {
		StringBuilder buffer = new StringBuilder();
		buffer.append(StringUtils.join(headers, ",") + "\n");

		for (Map<String, Object> cell : cellValue) {
			buffer.append(cell.get("resultId") + ",");
			buffer.append(cell.get("infoId") + ",");
			buffer.append(cell.get("province") + ",");
			buffer.append(cell.get("city") + ",");
			buffer.append(cell.get("project") + ",");
			buffer.append(cell.get("kindCode") + ",");
			buffer.append(cell.get("chain") + ",");
			buffer.append(cell.get("name") + ",");
			buffer.append(cell.get("nameShort") + ",");
			buffer.append(cell.get("address") + ",");
			buffer.append(cell.get("telSale") + ",");
			buffer.append(cell.get("telService") + ",");
			buffer.append(cell.get("telOther") + ",");
			buffer.append(cell.get("postcode") + ",");
			buffer.append(cell.get("nameEng") + ",");
			buffer.append(cell.get("addressEng") + ",");
			buffer.append(cell.get("sourceId") + ",");
			buffer.append(cell.get("dealSrcDiff") + ",");
			buffer.append(cell.get("matchMethod") + ",");
			buffer.append(cell.get("poiNum1") + ",");
			buffer.append(cell.get("poiNum2") + ",");
			buffer.append(cell.get("poiNum3") + ",");
			buffer.append(cell.get("poiNum4") + ",");
			buffer.append(cell.get("poiNum5") + ",");
			buffer.append(cell.get("similarity") + ",");
			buffer.append(cell.get("xLocate") + ",");
			buffer.append(cell.get("yLocate") + ",");
			buffer.append(cell.get("cfmPoiNum") + ",");
			buffer.append(cell.get("expectTime") + ",");
			buffer.append(cell.get("infoType") + ",");
			buffer.append(cell.get("infoLevel") + ",");
			buffer.append(cell.get("regionId") + "\n");
		}

		String savePath = String.format("release%s.csv", DateUtils.dateToString(new Date(), "yyyyMMddHHmmss"));
		File saveCSV = new File(localPath.substring(0, localPath.lastIndexOf("/")), savePath);
		try {
			if (!saveCSV.exists())
				saveCSV.createNewFile();

			DataOutputStream in = new DataOutputStream(new FileOutputStream(saveCSV));
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(in, "GBK"));
			writer.write(buffer.toString());
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return localPath.substring(0, localPath.lastIndexOf("/") + 1) + savePath;
	}
	
	
	/**
	 * 情报下发：上传成功后，连接情报库，按要求更新RESULT数据库
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
            
			//调用情报下发接口：http://fs-road.navinfo.com/dev/trunk/service/mapspotter/data/info/agent/import/?
			String infoPassUrl = SystemConfigFactory.getSystemConfig().getValue(PropConstant.mapspotterInfoPass);
			urlStr.append(infoPassUrl+"access_token=");
			urlStr.append(accessToken);
			urlStr.append("&parameter=");
			urlStr.append(URLEncoder.encode(jsonObj.toString(), "utf-8"));

			String return_value = Parser_Tool.do_get(urlStr.toString());
			JSONObject resultObj = JSONObject.fromObject(return_value);
			
			if(!resultObj.getString("errmsg").equals("success")){
				throw new Exception("情报接口未返回文件！");
			}

			log.info("输入的URL：" + urlStr);
			log.info("接口返回结果：" + resultObj);

			String successList = resultObj.getString("successList");
			String[] successLists = successList.replace("[", "").replace("]", "").split(",");

			for (String success : successLists) {
				if(success.isEmpty()){
					continue;
				}
				
				String sql = String.format(
						"UPDATE IX_DEALERSHIP_RESULT SET CFM_STATUS = 2,TO_INFO_DATE = %s WHERE RESULT_ID = '%s'",
						DateUtils.dateToString(new Date(), "yyyyMMddHHmmss"), success.replace("\"", ""));
				run.execute(conn, sql);
			}
			int generateFail = resultObj.getString("generateFailedList").equals("[]") ? 0
					: (resultObj.getString("generateFailedList").replace("[", "").replace("]", "")).split(",").length;
			int insertFail = resultObj.getString("insertFailedList").equals("[]") ? 0
					: (resultObj.getString("insertFailedList").replace("[", "").replace("]", "")).split(",").length;
			int sendFail = resultObj.getString("sendFailedList").equals("[]") ? 0
					: (resultObj.getString("sendFailedList").replace("[", "").replace("]", "")).split(",").length;
			int updateFail = resultObj.getString("updateFailedList").equals("[]") ? 0
					: (resultObj.getString("updateFailedList").replace("[", "").replace("]", "")).split(",").length;

			data.put("successCount", successLists.length);
			data.put("failCount", generateFail + insertFail + sendFail + updateFail);

		} catch (Exception e) {
			DbUtils.rollback(conn);
			log.error(e.getMessage());
			throw e;
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
		return data;
	}

	/**
	 * 情报下发：excel头文件对应的标识组合
	 * 
	 * @return
	 * @throws Exception
	 */
	public Map<String, String> importInfoHeader() {
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

	/**
	 * 反馈数据导出服务
	 * @param userId
	 * @param timeObj
	 * @throws Exception
	 */
	public String expInfoFeedbackService(long userId, JSONObject timeObj, HttpServletRequest request) throws Exception {
		String fileName = getFeedbackFileName(timeObj, userId);
		log.info("调用情报接口，反馈文件名称：" + fileName);
		
		if(fileName.contains("未查到符合条件的情报信息")){
			return fileName;
		}

		String filePath = SystemConfigFactory.getSystemConfig().getValue(PropConstant.uploadPath)
				+ "/dealership/information/" + fileName;

		log.info("反馈情报路径：" + filePath);
		Connection conn = null;

		try {
			conn = DBConnector.getInstance().getDealershipConnection();
			List<Map<String, Object>> feedbackResult = readCsvFile(filePath, feedbackHeaders, infoFeedbackHeader());

			for (Map<String, Object> result : feedbackResult) {

				// 文件中字段“情报是否被采纳”+“：”+“情报未采纳原因”+“，”+“情报未采纳或部分采纳备注”+“。”+“关联POI为”+“情报对应的要素外业采集ID”
				String fbContent = result.get("isAdopted") + "：" + result.get("notAdoptedReason") + "，"
						+ result.get("memo") + "。关联POI为" + result.get("cfmPoiNum");
				String sql = String.format(
						"UPDATE IX_DEALERSHIP_RESULT SET WORKFLOW_STATUS = 3, CFM_STATUS = 3, FB_DATE = '%s', FB_CONTENT = '%s', FB_SOURCE = 1 WHERE RESULT_ID = %d",
						result.get("feedbackTime") == null ? "" : result.get("feedbackTime"), fbContent,
						Integer.valueOf(result.get("resultId").toString()));
				
				run.execute(conn, sql);
			}
		} catch (Exception e) {
			DbUtils.rollback(conn);
			log.error(e.getMessage());
			throw e;
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
		return filePath;
	}

	/**
	 * 情报反馈：头文件对应map
	 * 
	 * @return
	 */
	public Map<String, String> infoFeedbackHeader() {
		Map<String, String> excelHeader = new HashMap<String, String>();

		excelHeader.put("UUID", "resultId");
		excelHeader.put("情报ID", "infoId");
		excelHeader.put("省份", "province");
		excelHeader.put("城市", "city");
		excelHeader.put("项目", "project");
		excelHeader.put("代理店分类", "kindCode");
		excelHeader.put("代理店品牌", "chain");
		excelHeader.put("厂商提供名称", "name");
		excelHeader.put("厂商提供地址", "address");
		excelHeader.put("厂商提供电话", "telSale");
		excelHeader.put("四维确认备注", "cfmMemo");
		excelHeader.put("代理店显示坐标X", "xLocate");
		excelHeader.put("代理店显示坐标Y", "yLocate");
		excelHeader.put("情报对应的要素外业采集ID", "cfmPoiNum");
		excelHeader.put("情报是否被采纳", "isAdopted");
		excelHeader.put("情报未采纳原因", "notAdoptedReason");
		excelHeader.put("情报未采纳或部分采纳备注", "memo");
		excelHeader.put("情报类型", "infoType");
		excelHeader.put("情报等级", "infoLevel");
		excelHeader.put("反馈时间", "feedbackTime");

		return excelHeader;
	}

	/**
	 * 调用情报接口，得到反馈文件名称
	 * 
	 * @param timeObj
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	public String getFeedbackFileName(JSONObject timeObj, long userId) throws Exception {
		String accessToken = AccessTokenFactory.generate(userId).getTokenString();

		StringBuilder urlStr = new StringBuilder();
		String feedBackUrl = SystemConfigFactory.getSystemConfig().getValue(PropConstant.mapspotterInfoFeedBack);
		urlStr.append(feedBackUrl+"access_token=");
		urlStr.append(accessToken);
		urlStr.append("&parameter=");
		urlStr.append(URLEncoder.encode(timeObj.toString(), "utf-8"));
		log.info("情报反馈URL:" + urlStr);

		String return_value = Parser_Tool.do_get(urlStr.toString());
		JSONObject resultObj = JSONObject.fromObject(return_value);
		
		log.info("情报反馈返回值："+return_value);
		log.info("情报反馈："+resultObj);
		
		int exportCount = resultObj.getInt("exportCount");
		if(exportCount == 0){
			return "未查到符合条件的情报信息!";
		}
		
		String fileName = resultObj.getString("filename");
		return fileName;
	}

	public List<Map<String, Object>> readCsvFile(String uploadFile, String[] headers, Map<String, String> excelHeader)
			throws Exception {
		DataInputStream in = new DataInputStream(new FileInputStream(new File(uploadFile)));
		BufferedReader br = new BufferedReader(new InputStreamReader(in, "GBK"));

		List<Map<String, Object>> sourceResult = new ArrayList<>();

		String line = "";
		int n = 0;

		try {
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
		} catch (Exception e) {
			throw e;
		} finally {
			br.close();
			in.close();
		}
		return sourceResult;
	}
}
