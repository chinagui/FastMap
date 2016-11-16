package com.navinfo.dataservice.dao.glm.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiAddress;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiName;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiPhoto;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.poi.deep.IxPoiColumnStatusSelector;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiAddressSelector;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiNameSelector;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

public class IxPoiSearch implements ISearch {

	private Connection conn;
	
	private JSONObject CHAINMAP;
	
	private JSONObject KINDCODEMAP;
	
	private JSONObject ADMINMAP;
	
	private JSONObject CHARACTERMAP;
	
	private JSONObject NAVICOVPYMAP;
	
	private JSONObject ENGSHORTMAP;

	public IxPoiSearch(Connection conn) {
		super();
		this.conn = conn;
	}

	@Override
	public IObj searchDataByPid(int pid) throws Exception {

		IxPoiSelector ixPoiSelector = new IxPoiSelector(conn);

		IObj ixPoi = (IObj) ixPoiSelector.loadByIdAndChildren(pid, false);

		return ixPoi;
	}
	
	@Override
	public List<IObj> searchDataByPids(List<Integer> pidList) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<SearchSnapshot> searchDataBySpatial(String wkt)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SearchSnapshot> searchDataByCondition(String condition)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SearchSnapshot> searchDataByTileWithGap(int x, int y, int z,
			int gap) throws Exception {
		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();
		
		StringBuilder sb=new StringBuilder();
		
		sb.append("WITH TMP1 AS (SELECT PID, KIND_CODE, INDOOR, X_GUIDE, Y_GUIDE, GEOMETRY, ROW_ID FROM IX_POI WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'MASK=ANYINTERACT') = 'TRUE' AND U_RECORD != 2), TMP2 AS (SELECT PN.NAME, PN.POI_PID FROM TMP1 A LEFT JOIN IX_POI_NAME PN ON PN.POI_PID = A.PID WHERE PN.POI_PID = A.PID AND PN.LANG_CODE = 'CHI' AND PN.NAME_CLASS = 1 AND PN.NAME_TYPE = 2 AND PN.U_RECORD != 2) SELECT TMP.*, T . NAME FROM (SELECT A.*, B.STATUS FROM TMP1 A LEFT JOIN POI_EDIT_STATUS B ON A.ROW_ID = B.ROW_ID) TMP LEFT JOIN TMP2 T ON T.POI_PID = TMP.PID ");
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			
			pstmt = conn.prepareStatement(sb.toString());

			String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);

			pstmt.setString(1, wkt);
			
			resultSet = pstmt.executeQuery();

			double px = MercatorProjection.tileXToPixelX(x);

			double py = MercatorProjection.tileYToPixelY(y);

			while (resultSet.next()) {
				SearchSnapshot snapshot = new SearchSnapshot();

				int status = resultSet.getInt("status");

				JSONObject m = new JSONObject();

				m.put("b", status);
				m.put("d", resultSet.getString("kind_code"));

				m.put("e", resultSet.getString("name"));
				
				m.put("g", resultSet.getInt("indoor") == 0 ? 0 : 1);

				Double xGuide = resultSet.getDouble("x_guide");

				Double yGuide = resultSet.getDouble("y_guide");

				Geometry guidePoint = GeoTranslator.point2Jts(xGuide, yGuide);

				JSONObject guidejson = GeoTranslator.jts2Geojson(guidePoint);

				Geojson.point2Pixel(guidejson, z, px, py);

				m.put("c", guidejson.getJSONArray("coordinates"));

				snapshot.setM(m);

				snapshot.setT(21);

				snapshot.setI(resultSet.getString("pid"));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				JSONObject geojson = Geojson.spatial2Geojson(struct);

				Geojson.point2Pixel(geojson, z, px, py);

				snapshot.setG(geojson.getJSONArray("coordinates"));

				list.add(snapshot);
			}
		} catch (Exception e) {

			throw new Exception(e);
		} finally {
			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (Exception e) {

				}
			}

			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (Exception e) {

				}
			}

		}

		return list;
	}

	private String GetParentOrChild(int parentCount, int childCount) {
		String haveParentOrChild = "0";

		if (parentCount > 0 && childCount > 0) {
			haveParentOrChild = "3";
		} else if (parentCount > 0) {
			haveParentOrChild = "1";
		} else if (childCount > 0) {
			haveParentOrChild = "2";
		}

		return haveParentOrChild;
	}

	public static void main(String[] args) throws Exception {

//		Connection conn = DBConnector.getInstance().getConnectionById(11);
//		new IxPoiSearch(conn).searchDataByTileWithGap(215890, 99229, 18, 80);
		System.out.println(MercatorProjection.getWktWithGap(107940, 49615, 17, 80));
	}
	
	/**
	 * 精编作业数据查询
	 * @param firstWordItem
	 * @param secondWorkItem
	 * @param rowIds
	 * @param type
	 * @param langCode
	 * @return
	 * @throws Exception
	 */
	public JSONArray searchColumnPoiByRowId(String firstWordItem,String secondWorkItem,List<String> rowIds,String type,String langCode) throws Exception {
		
		JSONArray dataList = new JSONArray();
		
		try {
			
			MetadataApi apiService=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
			
			
			JSONObject metaData = apiService.getMetadataMap();
			
			this.CHAINMAP = metaData.getJSONObject("chain");
			
			this.KINDCODEMAP = metaData.getJSONObject("kindCode");
			
			this.ADMINMAP =  metaData.getJSONObject("admin");
			
			this.CHARACTERMAP = metaData.getJSONObject("character");
			
			this.NAVICOVPYMAP = metaData.getJSONObject("navicovpy");
			
			this.ENGSHORTMAP = metaData.getJSONObject("engshort");
			
			switch (firstWordItem) {
				case "poi_name":
					dataList = getPoiNameData(secondWorkItem,rowIds,type,langCode);
					break;
				case "poi_address":
					dataList = getPoiAddressData(secondWorkItem,rowIds,type,langCode);
					break;
				case "poi_englishname":
					dataList = getPoiEngnameData(secondWorkItem,rowIds,type,langCode);
					break;
				case "poi_englishaddress":
					dataList = getPoiEngaddrData(secondWorkItem,rowIds,type,langCode);
					break;
			}
			return dataList;
		} catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * poi_name作业项查询
	 * @param secondWorkItem
	 * @param rowIds
	 * @param type
	 * @param langCode
	 * @return
	 * @throws Exception
	 */
	private JSONArray getPoiNameData(String secondWorkItem,List<String> rowIds,String type,String langCode) throws Exception {
		
		JSONArray dataList = new JSONArray();
		
		try {
			boolean isLock = false;
			if (type.equals("integrate")) {
				// TODO 返回检查错误的数据
			}
			
			IxPoiSelector poiSelector = new IxPoiSelector(conn);
			IxPoiNameSelector nameSelector = new IxPoiNameSelector(conn);
			IxPoiColumnStatusSelector ixPoiDeepStatusSelector = new IxPoiColumnStatusSelector(conn);
			
			for (String rowId:rowIds) {
				IxPoi poi = (IxPoi) poiSelector.loadByRowId(rowId, isLock);
				List<IRow> nameList = nameSelector.loadRowsByParentId(poi.getPid(), isLock);
				poi.setNames(nameList);
				poi.setPhotos(new AbstractSelector(IxPoiPhoto.class,conn).loadRowsByParentId(poi.getPid(), isLock));
				JSONObject status = ixPoiDeepStatusSelector.getStatus(rowId,secondWorkItem);
				
				JSONObject poiObj = poi.Serialize(null);
				poiObj.put("photoCount", poi.getPhotos().size());
				if (CHAINMAP.containsKey(poi.getChain())) {
					poiObj.put("chainName", CHAINMAP.get(poi.getChain()));
				} else {
					poiObj.put("chainName", poi.getChain());
				}
				if (KINDCODEMAP.containsKey(poi.getKindCode())) {
					poiObj.put("kindCodeName", KINDCODEMAP.get(poi.getKindCode()));
				} else {
					poiObj.put("kindCodeName", poi.getKindCode());
				}
				if (ADMINMAP.containsKey(Integer.toString(poi.getAdminReal()))) {
					poiObj.put("detailArea", ADMINMAP.get(Integer.toString(poi.getAdminReal())));
				} else {
					poiObj.put("detailArea", Integer.toString(poi.getAdminReal()));
				}
				
				poiObj.put("classifyRules", status.getString("workItemId"));
				poiObj.put("auditStatus", status.getInt("firstWorkStatus"));
				
				// 港澳作业,参考信息
				if (langCode.equals("CHT")&&(secondWorkItem.equals("nameUnify") || secondWorkItem.equals("shortName"))) {
					List<String> msgList = getNamerefMsg(secondWorkItem,nameList);
					poiObj.put("namerefMsg", msgList);
				}
				
				// 名称统一，查询父名称
				if (secondWorkItem.equals("nameUnify")) {
					poiObj.put("parentName", nameSelector.loadByIdForColumn(poi.getPid(), langCode));
				}
				
				// 名称拼音作业，获取拼音组
				if (secondWorkItem.equals("namePinyin")) {
					List<List<String>> pyList = new ArrayList<List<String>>();
					JSONArray nameArray = new JSONArray();
					for (IRow temp:nameList) {
						IxPoiName name = (IxPoiName) temp;
						JSONObject nameObj = name.Serialize(null);
						if (name.getLangCode().equals(langCode) && name.getNameType() == 1) {
							if (name.getNameClass()==1||name.getNameClass()==3||name.getNameClass()==5||name.getNameClass()==8) {
								pyList = pyConvertor(name.getName());
								nameObj.put("multiPinyin", pyList);
							}
						}
						nameArray.add(nameObj);
					}
					poiObj.put("names", nameArray);
				}
				
				dataList.add(poiObj);
			}
			
			return dataList;
		} catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * poi_address作业项查询
	 * @param secondWorkItem
	 * @param rowIds
	 * @param type
	 * @param langCode
	 * @return
	 * @throws Exception
	 */
	private JSONArray getPoiAddressData(String secondWorkItem,List<String> rowIds,String type,String langCode) throws Exception {
		JSONArray dataList = new JSONArray();
		
		try {
			boolean isLock = false;
			if (type.equals("integrate")) {
				// TODO 返回检查错误的数据
			}
			
			IxPoiSelector poiSelector = new IxPoiSelector(conn);
			IxPoiNameSelector nameSelector = new IxPoiNameSelector(conn);
			IxPoiAddressSelector addressSelector = new IxPoiAddressSelector(conn);
			IxPoiColumnStatusSelector ixPoiDeepStatusSelector = new IxPoiColumnStatusSelector(conn);
			
			for (String rowId:rowIds) {
				IxPoi poi = (IxPoi) poiSelector.loadByRowId(rowId, isLock);
				List<IRow> nameList = nameSelector.loadRowsByParentId(poi.getPid(),isLock);
				poi.setNames(nameList);
				List<IRow> addressList = addressSelector.loadRowsByParentId(poi.getPid(), isLock);
				poi.setAddresses(addressList);
				poi.setPhotos(new AbstractSelector(IxPoiPhoto.class,conn).loadRowsByParentId(poi.getPid(), isLock));
				JSONObject status = ixPoiDeepStatusSelector.getStatus(rowId,secondWorkItem);
				
				JSONObject poiObj = poi.Serialize(null);
				poiObj.put("photoCount", poi.getPhotos().size());
				if (CHAINMAP.containsKey(poi.getChain())) {
					poiObj.put("chainName", CHAINMAP.get(poi.getChain()));
				} else {
					poiObj.put("chainName", poi.getChain());
				}
				if (KINDCODEMAP.containsKey(poi.getKindCode())) {
					poiObj.put("kindCodeName", KINDCODEMAP.get(poi.getKindCode()));
				} else {
					poiObj.put("kindCodeName", poi.getKindCode());
				}
				if (ADMINMAP.containsKey(Integer.toString(poi.getAdminReal()))) {
					poiObj.put("detailArea", ADMINMAP.get(Integer.toString(poi.getAdminReal())));
				} else {
					poiObj.put("detailArea", Integer.toString(poi.getAdminReal()));
				}
				poiObj.put("classifyRules", status.getString("workItemId"));
				poiObj.put("auditStatus", status.getInt("firstWorkStatus"));
				
				// 港澳作业,参考信息
				if (langCode.equals("CHT")) {
					List<String> msgList = getAddrrefMsg(addressList);
					poiObj.put("namerefMsg", msgList);
				}
				
				// 地址拼音作业，获取拼音组
				if (secondWorkItem.equals("addrPinyin")) {
					JSONArray addrArray = new JSONArray();
					for (int i=0;i<addressList.size();i++) {
						IRow temp = addressList.get(i);
						IxPoiAddress address = (IxPoiAddress) temp;
						JSONObject addrObj = address.Serialize(null);
						if (address.getLangCode().equals(langCode)) {
							if (address.getAddrname()!=null && !address.getAddrname().isEmpty()) {
								List<List<String>> addrNameMultiPinyin = pyConvertor(address.getAddrname());
								addrObj.put("addrNameMultiPinyin", addrNameMultiPinyin);
							}
							if (address.getRoadname()!=null && !address.getRoadname().isEmpty()) {
								List<List<String>> roadNameMultiPinyin = pyConvertor(address.getRoadname());
								addrObj.put("roadNameMultiPinyin", roadNameMultiPinyin);
							}
							if (address.getFullname()!=null && !address.getFullname().isEmpty()) {
								List<List<String>> fullNameMultiPinyin = pyConvertor(address.getFullname());
								addrObj.put("fullNameMultiPinyin", fullNameMultiPinyin);
							}
						}
						addrArray.add(addrObj);
					}
					poiObj.put("addresses", addrArray);
				}
				
				dataList.add(poiObj);
			}
			
			return dataList;
		} catch (Exception e) {
			throw e;
		}
	}
	
	
	/**
	 * poi_englishname作业项查询
	 * @param secondWorkItem
	 * @param rowIds
	 * @param type
	 * @param langCode
	 * @return
	 * @throws Exception
	 */
	private JSONArray getPoiEngnameData(String secondWorkItem, List<String> rowIds, String type, String langCode) throws Exception {
		
		JSONArray dataList = new JSONArray();
		
		try {
			boolean isLock = false;
			if (type.equals("integrate")) {
				// TODO 返回检查错误的数据
			}
			
			IxPoiSelector poiSelector = new IxPoiSelector(conn);
			IxPoiNameSelector nameSelector = new IxPoiNameSelector(conn);
			IxPoiColumnStatusSelector ixPoiDeepStatusSelector = new IxPoiColumnStatusSelector(conn);
			
			for (String rowId:rowIds) {
				IxPoi poi = (IxPoi) poiSelector.loadByRowId(rowId, isLock);
				List<IRow> nameList = nameSelector.loadRowsByParentId(poi.getPid(), isLock);
				poi.setNames(nameList);
				poi.setPhotos(new AbstractSelector(IxPoiPhoto.class,conn).loadRowsByParentId(poi.getPid(), isLock));
				JSONObject status = ixPoiDeepStatusSelector.getStatus(rowId,secondWorkItem);
				
				JSONObject poiObj = poi.Serialize(null);
				poiObj.put("photoCount", poi.getPhotos().size());
				if (CHAINMAP.containsKey(poi.getChain())) {
					poiObj.put("chainName", CHAINMAP.get(poi.getChain()));
				} else {
					poiObj.put("chainName", poi.getChain());
				}
				if (KINDCODEMAP.containsKey(poi.getKindCode())) {
					poiObj.put("kindCodeName", KINDCODEMAP.get(poi.getKindCode()));
				} else {
					poiObj.put("kindCodeName", poi.getKindCode());
				}
				if (ADMINMAP.containsKey(Integer.toString(poi.getAdminReal()))) {
					poiObj.put("detailArea", ADMINMAP.get(Integer.toString(poi.getAdminReal())));
				} else {
					poiObj.put("detailArea", Integer.toString(poi.getAdminReal()));
				}
				poiObj.put("classifyRules", status.getString("workItemId"));
				poiObj.put("auditStatus", status.getInt("firstWorkStatus"));
				
				dataList.add(poiObj);
			}
			
			return dataList;
		} catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * poi_englishaddress作业项查询
	 * @param secondWorkItem
	 * @param rowIds
	 * @param type
	 * @param langCode
	 * @return
	 */
	private JSONArray getPoiEngaddrData(String secondWorkItem, List<String> rowIds, String type, String langCode) throws Exception {
		JSONArray dataList = new JSONArray();
		
		try {
			boolean isLock = false;
			if (type.equals("integrate")) {
				// TODO 返回检查错误的数据
			}
			
			IxPoiSelector poiSelector = new IxPoiSelector(conn);
			IxPoiNameSelector nameSelector = new IxPoiNameSelector(conn);
			IxPoiAddressSelector addressSelector = new IxPoiAddressSelector(conn);
			IxPoiColumnStatusSelector ixPoiDeepStatusSelector = new IxPoiColumnStatusSelector(conn);
			
			for (String rowId:rowIds) {
				IxPoi poi = (IxPoi) poiSelector.loadByRowId(rowId, isLock);
				List<IRow> nameList = nameSelector.loadRowsByParentId(poi.getPid(), isLock);
				poi.setNames(nameList);
				List<IRow> addressList = addressSelector.loadRowsByParentId(poi.getPid(), isLock);
				poi.setAddresses(addressList);
				poi.setPhotos(new AbstractSelector(IxPoiPhoto.class,conn).loadRowsByParentId(poi.getPid(), isLock));
				JSONObject status = ixPoiDeepStatusSelector.getStatus(rowId,secondWorkItem);
				
				JSONObject poiObj = poi.Serialize(null);
				poiObj.put("photoCount", poi.getPhotos().size());
				if (CHAINMAP.containsKey(poi.getChain())) {
					poiObj.put("chainName", CHAINMAP.get(poi.getChain()));
				} else {
					poiObj.put("chainName", poi.getChain());
				}
				if (KINDCODEMAP.containsKey(poi.getKindCode())) {
					poiObj.put("kindCodeName", KINDCODEMAP.get(poi.getKindCode()));
				} else {
					poiObj.put("kindCodeName", poi.getKindCode());
				}
				if (ADMINMAP.containsKey(Integer.toString(poi.getAdminReal()))) {
					poiObj.put("detailArea", ADMINMAP.get(Integer.toString(poi.getAdminReal())));
				} else {
					poiObj.put("detailArea", Integer.toString(poi.getAdminReal()));
				}
				poiObj.put("classifyRules", status.getString("workItemId"));
				poiObj.put("auditStatus", status.getInt("firstWorkStatus"));
				
				List<String> addressesList = new ArrayList<String>();
				for (IRow temp:addressList) {
					IxPoiAddress addr = (IxPoiAddress) temp;
					if (addr.getLangCode().equals("ENG")) {
						String[] addrList = addr.getFullname().split(" ");
						for (String addrTemp:addrList) {
							if (ENGSHORTMAP.containsKey(addrTemp)) {
								addressesList.add(addrTemp + "&" + ENGSHORTMAP.get(addrTemp));
							}
						}
						break;
					}
				}
				poiObj.put("addressList", addressesList);
				
				dataList.add(poiObj);
			}
			return dataList;
		} catch (Exception e) {
			throw e;
		}
			
	}
	
	/**
	 * 获取poi_name作业，港澳作业中的参考信息
	 * @param secondWorkItem
	 * @param nameList
	 * @return
	 * @throws Exception
	 */
	private List<String> getNamerefMsg (String secondWorkItem,List<IRow> nameList) throws Exception {

		List<String> msgList = new ArrayList<String>();
		String name = "";
		try {
			for (IRow temp:nameList) {
				IxPoiName ixPoiName = (IxPoiName) temp;
				if (secondWorkItem.equals("nameUnify")) {
					if (ixPoiName.getLangCode().equals("CHT") && ixPoiName.getNameType()==1 && ixPoiName.getNameClass()==1) {
						name = ixPoiName.getName();
					}
				} else if (secondWorkItem.equals("shortName")) {
					if (ixPoiName.getLangCode().equals("CHT") && ixPoiName.getNameType()==1 && ixPoiName.getNameClass()==5) {
						name = ixPoiName.getName();
					}
				}
			}
			
			for (int i=0;i<name.length();i++) {
				if (CHARACTERMAP.containsKey(name.substring(i, i+1))) {
					String correct = CHARACTERMAP.getString(name.substring(i, i+1));
					if (correct.isEmpty()) {
						correct = "";
					} 
					msgList.add(name.substring(i, i+1) + "&" + correct);
				}
			}
			return msgList;
		} catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * 获取poi_address作业，港澳作业中的参考信息
	 * @param addressList
	 * @return
	 * @throws Exception
	 */
	private List<String> getAddrrefMsg(List<IRow>addressList) throws Exception {
		
		String strRoad = "省名|市名|区县名|街道名|小区名|街巷名";
		String strAddr = "标志物名|前缀|门牌号|类型名|子号|后缀|附属设施名|楼栋号|楼层|楼门号|房间号|附加信息";
		
		List<String> msgList = new ArrayList<String>();
		
		try {
			for (IRow temp:addressList) {
				IxPoiAddress addr = (IxPoiAddress) temp;
				if (addr.getLangCode().equals("CHT")) {
					String addrName = addr.getAddrname();
					if (!addrName.isEmpty()) {
						String[] addrNamelis = addrName.split("|");
						String[] strAddrlis = strAddr.split("|");
						for (int i=0;i<addrNamelis.length;i++) {
							String addrNameSingle =addrNamelis[i];
							if (!addrNameSingle.isEmpty()) {
								for (int j=0;j<addrNameSingle.length();j++) {
									if (CHARACTERMAP.containsKey(addrNameSingle.substring(i, i+1))) {
										String correct = CHARACTERMAP.getString(addrNameSingle.substring(i, i+1));
										if (correct.isEmpty()) {
											correct = "";
										} 
										msgList.add(strAddrlis[i] + "&"+ addrNameSingle.substring(j, j+1)+ "&" + correct);
									}
								}
							}
						}
					}
					
					String roadName = addr.getRoadname();
					if (!roadName.isEmpty()) {
						String[] roadNamelis = roadName.split("|");
						String[] strRoadlis = strRoad.split("|");
						for (int i=0;i<roadNamelis.length;i++) {
							String roadNameSingle =roadNamelis[i];
							if (!roadNameSingle.isEmpty()) {
								for (int j=0;j<roadNameSingle.length();j++) {
									if (CHARACTERMAP.containsKey(roadNameSingle.substring(i, i+1))) {
										String correct = CHARACTERMAP.getString(roadNameSingle.substring(i, i+1));
										if (correct.isEmpty()) {
											correct = "";
										} 
										msgList.add(strRoadlis[i] + "&"+ roadNameSingle.substring(j, j+1)+ "&" + correct);
									}
								}
							}
						}
					}
					
					break;
				}
			}
			
			return msgList;
		} catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * 获取多音字拼音组
	 * @param word
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private List<List<String>> pyConvertor(String word) throws Exception{
		List<List<String>> result = new ArrayList<List<String>>();
		try {
			word.replace(" ", "");
			for (int i=0;i<word.length();i++) {
				List<String> sigleWordList = new ArrayList<String>();
				if (NAVICOVPYMAP.containsKey(String.valueOf(word.charAt(i)))) {
					List<String> sigleWord = (List<String>) NAVICOVPYMAP.get(String.valueOf(word.charAt(i)));
					if (sigleWord.size()>1) {
						sigleWordList.add(Integer.toString(i));
						sigleWordList.add(String.valueOf(word.charAt(i)));
						for (String sigelTemp:sigleWord) {
							String tmpPinyin = String.valueOf(sigelTemp.charAt(0)).toUpperCase() + sigelTemp.substring(1);
							sigleWordList.add(tmpPinyin);
						}
						
						result.add(sigleWordList);
					}
				}
			}
			return result;
		} catch (Exception e) {
			throw e;
		}
	}
	
}
