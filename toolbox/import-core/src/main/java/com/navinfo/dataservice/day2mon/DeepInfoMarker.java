package com.navinfo.dataservice.day2mon;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.ExcelReader;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.log.LogOpTypeStat;
import com.navinfo.dataservice.dao.log.LogReader;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiCarrental;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiHotel;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiParking;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiPhoto;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.navicommons.database.sql.DBUtils;

public class DeepInfoMarker {
	Logger log = LoggerRepos.getLogger(this.getClass());
	private OperationResult opResult;
	private Connection conn = null;
	private String opTempTable ="";
	private List<String> parkingKindCode = Arrays.asList("230210", "230213", "230214");
	private List<String> carrentalKindCode = Arrays.asList("200201");
	private List<String> carrentalChain = Arrays.asList("8007", "8006", "8004", "8003", "3000", "3854", "3902", "8005",
			"2FAA", "36AD", "36AE", "36AF", "3958", "3959");
	private List<String> detailKindCode = Arrays.asList("180308", "180309", "180304", "180400", "160206", "160205",
			"170101", "170102", "150101", "110103", "110102", "130501", "130105", "110200", "120101", "120102");

	private List<String> viewKindCode = Arrays.asList("180308", "180309", "180304", "180400", "160206", "160205");
	private List<String> hospitalKindCode = Arrays.asList("170101", "170102");
	private List<String> foodKindCode = Arrays.asList("110103", "110102");
	private List<String> bankKindCode = Arrays.asList("150101");
	private List<String> hostelKindCode = Arrays.asList("120101", "120102");
	private List<String> otherKindCode = Arrays.asList("130501", "130105", "110200");
	private List<Integer> hostelRatings = Arrays.asList(3, 13, 4, 14, 5, 15);

	public DeepInfoMarker(OperationResult opResult, Connection conn,String opTempTable) {
		super();
		this.opResult = opResult;
		this.conn = conn;
		this.opTempTable = opTempTable;
	}

	public DeepInfoMarker(OperationResult opResult, Connection conn) {
		super();
		this.opResult = opResult;
		this.conn = conn;
	}

	public void execute() throws Exception {
		// TODO:根据OperationResult进行深度信息打标记；

		Statement stmt = null;

		try {
			stmt = conn.createStatement();

			Map<Long, BasicObj> objMap = opResult.getObjsMapByType(ObjectName.IX_POI);
			
//			LogReader logRead = new LogReader(conn);
//			Map<Long,Integer> poiStates = logRead.getObjectState(objMap.keySet(), "IX_POI");
			Map<Long,Integer> poiStates = new HashMap<Long,Integer>(); 
			LogOpTypeStat stat = new LogOpTypeStat(conn);
			Map<Integer,Collection<Long>> updatedObjs=new HashMap<Integer,Collection<Long>>();
			if(StringUtils.isNotEmpty(opTempTable)){
				updatedObjs = stat.getOpTypeByTempOpTable(ObjectName.IX_POI, ObjectName.IX_POI, opTempTable);
			}else{
				updatedObjs = stat.getOpTypeByPids(ObjectName.IX_POI, ObjectName.IX_POI, objMap.keySet(), null, null);
			}
			
			for(Map.Entry<Integer, Collection<Long>> entry:updatedObjs.entrySet()){
				for(Long pid:entry.getValue()){
					poiStates.put(pid, entry.getKey());
				}
			}

			for (Map.Entry<Long, BasicObj> entry : objMap.entrySet()) {
				IxPoiObj poiObj = (IxPoiObj) entry.getValue();
				IxPoi poi = (IxPoi) poiObj.getMainrow();
				// TODO 判断是否为删除
				String kindCode = poi.getKindCode();
				String chain = poi.getChain();
				long pid = entry.getKey();
				// 停车场
				if (parkingKindCode.contains(kindCode)) {
					// 是否符合停车场抓取原则
					if (isParkingPoi(poiObj, kindCode)) {
//						String sql = "insert into POI_COLUMN_STATUS s (s.PID,s.WORK_ITEM_ID,s.HANDLER,s.TASK_ID) values("
//								+ pid + ",'FM-PARKING',0,0)";
						String sql = updateColumnStatus(pid,"FM-PARKING",0);
						stmt.addBatch(sql);
					}
					continue;
				}

				// 汽车租赁
				if (carrentalKindCode.contains(kindCode)) {
					// 是否符合汽车租赁抓取原则
					if (isCarrentalPoi(poiObj, chain)) {
						//String sql = "insert into POI_COLUMN_STATUS s (s.PID,s.WORK_ITEM_ID,s.HANDLER,s.TASK_ID) values("
						//		+ pid + ",'FM-CARRENTAL',0,0)";
						String sql = updateColumnStatus(pid,"FM-CARRENTAL",0);
						stmt.addBatch(sql);
					}
					continue;
				}

				// 通用
				if (detailKindCode.contains(kindCode)) {
					// 是否符合通用抓取原则
					if (isDetailPoi(poiObj,poiStates)) {
//						String sql = "insert into POI_COLUMN_STATUS s (s.PID,s.WORK_ITEM_ID,s.HANDLER,s.TASK_ID) values("
//								+ pid + ",'FM-DETAIL',0,0)";
						String sql = updateColumnStatus(pid,"FM-DETAIL",0);
						stmt.addBatch(sql);
					}
					continue;
				}
			}

			stmt.executeBatch();
		} catch (Exception e) {
			DbUtils.rollback(conn);
			throw e;
		} finally {
			DbUtils.closeQuietly(stmt);
		}
	}

	/**
	 * 判断是否符合停车场抓取原则
	 * 
	 * @param poiObj
	 * @param kindCode
	 * @return
	 */
	private boolean isParkingPoi(IxPoiObj poiObj, String kindCode) {

		// KIND_CODE为230210,且IX_POI_PARKING.PARKING_TYPE=2的不抓取
		List<IxPoiParking> poiParkings = poiObj.getIxPoiParkings();
		if ("230210".equals(kindCode)) {
			if(CollectionUtils.isNotEmpty(poiParkings)){
				for (IxPoiParking parking : poiParkings) {
					String parkingType = parking.getParkingType();
					if ("2".equals(parkingType)) {
						return false;
					}
				}
			}
		}
		List<IxPoiPhoto> poiPhotos = poiObj.getIxPoiPhotos();
		if (CollectionUtils.isNotEmpty(poiPhotos)) {
			return true;
		}
		// 无照片，但是有修改parking履历的抓取
		if(CollectionUtils.isNotEmpty(poiParkings)){
			for (IxPoiParking parking : poiParkings) {
				if (parking.getHisOpType().equals(OperationType.INSERT)||(parking.getHisOpType().equals(OperationType.UPDATE))){
//						&& parking.hisOldValueContains(IxPoiParking.PARKING_ID))) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 判断是否符合汽车租赁抓取原则
	 * 
	 * @param poiObj
	 * @return
	 */
	private boolean isCarrentalPoi(IxPoiObj poiObj, String chain) {
		if (carrentalChain.contains(chain)) {
			List<IxPoiCarrental> poiCarrentals = poiObj.getIxPoiCarrentals();
			if (CollectionUtils.isEmpty(poiCarrentals)) {
				// CARRENTAL中无记录
				return true;
			} else {
				// 有记录但网址（IX_POI_CARRENTAL.WEB_SITE）、400电话字段（IX_POI_CARRENTAL.PHONE_400)为任一字段为空
				for (IxPoiCarrental carrental : poiCarrentals) {
					String webSite = carrental.getWebSite();
					if ("3000".equals(chain)) {
						// 其中3000只要求网址为空
						if (StringUtils.isEmpty(webSite)) {
							return true;
						}
						continue;
					}
					String phone400 = carrental.getPhone400();
					// 网址和400电话任一为空，则抓取
					if (StringUtils.isEmpty(webSite) || StringUtils.isEmpty(phone400)) {
						return true;
					}
				}
				return false;
			}
		}
		return false;

	}

	/**
	 * 判断是否符合通用抓取原则
	 * 
	 * @param poiObj
	 * @return
	 * @throws Exception
	 */
	private boolean isDetailPoi(IxPoiObj poiObj,Map<Long,Integer> poiStates) throws Exception {
		IxPoi poi = (IxPoi) poiObj.getMainrow();

		try {
			long regionId = poi.getRegionId();
			String adminCode = getAdminCode(regionId);

			// 加载元数据SC_POINT_DEEP_PLANARE
			MetadataApi metaApi = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
			List<String> adminCodeList = metaApi.getDeepAdminCodeList();

			// 1.直辖市（北京、天津、上海、重庆），admin_code的前两位匹配
			// 2.其他城市，admin_code的前四位匹配
			if(StringUtils.isNotEmpty(adminCode)){
				if ((!adminCodeList.contains(adminCode.substring(0, 2)))
						&& (!adminCodeList.contains(adminCode.substring(0, 4)))) {
					return false;
				}
			}
			

			String kindCode = poi.getKindCode();
			String chain = poi.getChain();

			if (viewKindCode.contains(kindCode)) {
				long pid = poi.getPid();
				if (isChildPoi(pid)) {
					return false;
				}
				return true;
			}

//			LogReader logRead = new LogReader(conn);
//			int poiState = logRead.getObjectState((int) poi.getPid(), "IX_POI");
			
			int poiState = 3;
			if(poiStates.containsKey(poi.getPid())){
				poiState = poiStates.get(poi.getPid());
			}

			// 非新增（IX_POI.STATE=3）且种别为医院（170101、170102）的不提取
			if (hospitalKindCode.contains(kindCode)) {
				if (poiState == 3) {
					return false;
				}
				return true;
			}

			if (bankKindCode.contains(kindCode)) {
				if (StringUtils.isEmpty(chain)) {
					return false;
				}
				List<IxPoiName> poiNames = poiObj.getIxPoiNames();
				for (IxPoiName poiName : poiNames) {
					int nameClass = poiName.getNameClass();
					int nameType = poiName.getNameType();
					String langCode = poiName.getLangCode();
					if (nameClass == 1 && nameType == 2 && ("CHI".equals(langCode) || "CHT".equals(langCode))) {
						String name = poiName.getName();
						if (StringUtils.isNotEmpty(name)) {
							String newName = ExcelReader.f2h(name);
							if (newName.matches(".*ATM.*") || newName.matches(".*自助银行.*")) {
								return false;
							}
						}
					}
				}
				return true;
			}

			// 非新增（IX_POI.STATE=3）且种别为餐饮（110103、110102）的不提取
			if (foodKindCode.contains(kindCode)) {
				if (poiState == 3) {
					return false;
				}
				// 只制作chain值不为空的
				if (StringUtils.isEmpty(chain)) {
					return false;
				}
				return true;
			}

			if (otherKindCode.contains(kindCode)) {
				// 只制作chain值不为空的
				if (StringUtils.isEmpty(chain)) {
					return false;
				}
				return true;
			}

			if (hostelKindCode.contains(kindCode)) {
				if ("120101".equals(kindCode)) {
					List<IxPoiHotel> poiHotels = poiObj.getIxPoiHotels();
					if(CollectionUtils.isEmpty(poiHotels)) return false;
					for (IxPoiHotel hotel : poiHotels) {
						int rating = hotel.getRating();
						if (hostelRatings.contains(rating)) {
							return true;
						}
					}
					return false;
				}
				if ("120102".equals(kindCode)) {
					// 只制作chain值不为空的
					if (StringUtils.isEmpty(chain)) {
						return false;
					}
					return true;
				}
			}

			return false;

		} catch (Exception e) {
			throw e;
		}

	}

	/**
	 * 判断poi是否为子poi
	 * 
	 * @param pid
	 * @return
	 * @throws Exception
	 */
	private boolean isChildPoi(long pid) throws Exception {

		String sql = "select count(1) as num from IX_POI_CHILDREN where CHILD_POI_PID=" + pid;

		PreparedStatement pstmt = null;
		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);
			resultSet = pstmt.executeQuery();

			int count = 0;
			while (resultSet.next()) {
				count = resultSet.getInt("num");
			}

			if (count != 0) {
				return true;
			}
			return false;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
	}

	/**
	 * 根据regionId获取adminCode
	 * 
	 * @param regionId
	 * @return
	 * @throws Exception
	 */
	private String getAdminCode(long regionId) throws Exception {

		String sql = "select admin_id from ad_admin where region_id=" + regionId;

		PreparedStatement pstmt = null;
		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);
			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				int adminId = resultSet.getInt("admin_id");
				return  String.valueOf(adminId);
			}

			return null;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
	}
	// 更新POI_COLUMN_STATUS 表
	private String updateColumnStatus(Long pid,String workItem,int handler) throws Exception {

		StringBuilder sb = new StringBuilder(" MERGE INTO poi_column_status T1 ");
		sb.append(" USING (SELECT "+pid+" as b,'" + workItem + "' as c," + handler
				+ " as d  FROM dual) T2 ");
		sb.append(" ON ( T1.pid=T2.b and T1.work_item_id=T2.c) ");
		sb.append(" WHEN MATCHED THEN ");
		sb.append(" UPDATE SET T1.first_work_status = 1,T1.second_work_status = 1,T1.handler = T2.d,T1.QC_FLAG=0,T1.common_handler=0 ");
		sb.append(" WHEN NOT MATCHED THEN ");
		sb.append(" INSERT (T1.pid,T1.work_item_id,T1.first_work_status,T1.second_work_status,T1.handler) VALUES(T2.b,T2.c,1,1,T2.d)");
		return sb.toString();

	}
}