package com.navinfo.dataservice.control.app.download;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import com.navinfo.dataservice.bizcommons.sys.SysLogConstant;
import com.navinfo.dataservice.bizcommons.sys.SysLogOperator;
import com.navinfo.dataservice.bizcommons.sys.SysLogStats;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.commons.util.ZipUtils;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.ixpointaddress.IxPointaddress;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiChargingPlot;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiChargingStation;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiGasstation;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiHotel;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiParking;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiRestaurant;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiAddress;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiChildrenForAndroid;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiContact;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiName;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiParentForAndroid;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiFlagMethod;
import com.navinfo.dataservice.dao.glm.search.batch.PaGridIncreSearch;
import com.navinfo.dataservice.dao.glm.search.batch.PoiGridIncreSearch;
import com.navinfo.dataservice.engine.editplus.operation.imp.ErrorLog;
import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

public class PaDownloadOperation {
	private static final Logger logger = Logger.getLogger(PaDownloadOperation.class);
	//*******2017.08.16 zl *****
	//
	private List<ErrorLog> errorLogs = null;
	
	/**
	 * 
	 * @param grids
	 * @return url
	 * @throws Exception
	 */
	public String generateZip(Map<String,String> gridDateMap,int subtaskId,long userId) throws Exception{
		
		try{
			errorLogs = new ArrayList<ErrorLog>();
			Date startTime = new Date();
			String day = StringUtils.getCurrentDay();

			String uuid = UuidUtils.genUuid();
			
			String downloadFilePath = SystemConfigFactory.getSystemConfig().getValue(
					PropConstant.downloadFilePathPa);

			String parentPath = downloadFilePath +File.separator+ day + File.separator+userId+File.separator;

			String filePath = parentPath + uuid + File.separator;

			File file = new File(filePath);
			
			if (!file.exists()) {
				file.mkdirs();
			}
			//初始化存储图片属性的map
			Map<String, Object> logMap=new HashMap<String, Object>();
			logMap.put("beginTime", DateUtils.getSysDateFormat());
			
			logger.info("export ix_pointaddress to pa.txt--->start");
			export2Txt(gridDateMap, filePath, "pa.txt",logMap);
			logger.info("export ix_pointaddress to pa.txt--->end");
			
			String zipFileName = uuid + ".zip";

			String zipFullName = parentPath + zipFileName;

			ZipUtils.zipFile(filePath, zipFullName);
			
			String serverUrl =  SystemConfigFactory.getSystemConfig().getValue(
					PropConstant.serverUrl);
			
			String downloadUrlPath = SystemConfigFactory.getSystemConfig().getValue(
					PropConstant.downloadUrlPathPoi);

			String url = serverUrl + downloadUrlPath +File.separator+ day + File.separator+userId+File.separator
					+ zipFileName;
			
			Date endTime = new Date();
			logger.info("total time:"+ (endTime.getTime() - startTime.getTime())+"ms");
			
			logMap.put("endTime", DateUtils.getSysDateFormat());
			logMap.put("userId", userId);
			logMap.put("uuid", uuid);
			
			insertStatisticsInfoNoException(subtaskId, userId, logMap);
			return url;
		} catch (Exception e){
			throw e;
		}
	}
	

	/**
	 * @Title: insertStatisticsInfoNoException
	 * @Description: 记录pa下载日志
	 * @param subtaskId
	 * @param userId
	 * @param logMap  void
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年8月15日 下午3:05:07 
	 */
	private void insertStatisticsInfoNoException( int subtaskId,
			long userId, Map<String, Object> logMap)  {
		try{
			
			SysLogStats log = new SysLogStats();
			log.setLogType(SysLogConstant.PA_DOWNLOAD_TYPE);
			log.setLogDesc(SysLogConstant.PA_DOWNLOAD_DESC+",jobId :0,subtaskId:"+subtaskId+",downloadTime:"+logMap.get("endTime")+",uuid:"+logMap.get("uuid"));
			log.setFailureTotal(((int) logMap.get("total"))-((int) logMap.get("success")));
			log.setSuccessTotal((int) logMap.get("success"));  
			log.setTotal((int) logMap.get("total"));
			log.setBeginTime((String) logMap.get("beginTime"));
			log.setEndTime(DateUtils.getSysDateFormat());
			if(errorLogs != null && errorLogs.size() > 0 ){
				JSONArray jsonArrFail = JSONArray.fromObject(errorLogs);
				log.setErrorMsg(jsonArrFail.toString());
			}else{
				log.setErrorMsg("");
			}
			log.setUserId(String.valueOf(userId));
			
			SysLogOperator.getInstance().insertSysLog(log);
		
		}catch (Exception e) {
			logger.error("记录poi下载日志出错："+e.getMessage(), e);
		}
	}
	
	/**
	 * 
	 * @param grids
	 * @param folderName
	 * @param fileName
	 * @param errorLogs 
	 * @throws Exception
	 */
	public void export2Txt(Map<String,String> gridDateMap,  String folderName,
			String fileName,Map<String, Object> logMap) throws Exception {
		if (!folderName.endsWith("/")) {
			folderName += "/";
		}

		fileName = folderName + fileName;

		PrintWriter pw = new PrintWriter(fileName);
		try {
			logger.info("starting load data...");
			Collection<IxPointaddress> data = new PaGridIncreSearch().getPaByGrids(gridDateMap);
			logger.info("data total:"+data.size());
			logger.info("starting convert data...");
			JSONArray ja = changeData(data);
			logger.info("begin write json to file");
			String encoding = System.getProperty("file.encoding");
			logger.info("系统编码encoding:"+encoding);
			for (int j = 0; j < ja.size(); j++) {
				pw.println(ja.getJSONObject(j).toString());
			}
			logger.info("file write ok");
			logMap.put("total", data.size());
			logMap.put("success", ja.size());
		} catch (Exception e) {
			errorLogs.add(new ErrorLog("", "poi 下载报错:"+e.getMessage()));
			logger.error("poi 下载报错:"+e.getMessage());
//			throw e;
		} finally {
			pw.close();

		}
	}
	
	
	
	/**
	 * @param data
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("static-access")
	public JSONArray changeData(Collection<IxPointaddress> data) throws Exception{
		JSONArray retList = new JSONArray();
		JSONObject jsonObj = new JSONObject();
		
		for (IxPointaddress pa:data){
			
			jsonObj.put("fid", pa.getIdcode());
			
			jsonObj.put("dprName", pa.getDprName());
			
			jsonObj.put("dpName", pa.getDpName());
			
			jsonObj.put("pid", pa.getPid());
			
			
			if  (pa.getGuideLinkPid()==0 && pa.getxGuide()==0 && pa.getyGuide()==0) {
				jsonObj.put("guide", JSONNull.getInstance());
			} else {
				JSONObject guide = new JSONObject();
				guide.put("linkPid", pa.getGuideLinkPid());
				guide.put("longitude", pa.getxGuide());
				guide.put("latitude", pa.getyGuide());
				jsonObj.put("guide", guide);
			}
			
			jsonObj.put("attachments", new ArrayList<Object>());
			
			
			switch (pa.getuRecord()) {
			case 0:
				jsonObj.put("t_lifecycle", 0);
				break;
			case 1:
				jsonObj.put("t_lifecycle", 3);
				break;
			case 2:
				jsonObj.put("t_lifecycle", 1);
				break;
			case 3:
				jsonObj.put("t_lifecycle", 2);
				break;
			}
			
			GeoTranslator trans = new GeoTranslator();
			String geometry = trans.jts2Wkt(pa.getGeometry(),1,5);
			jsonObj.put("geometry", geometry);
			
			jsonObj.put("t_operateDate", "");
			
			retList.add(jsonObj);
		}
		return retList;
	}

}
