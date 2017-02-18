package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.ExcelReader;
import com.navinfo.dataservice.dao.plus.innermodel.CiParaAdAdmin;
import com.navinfo.dataservice.dao.plus.innermodel.CiParaIxPoiAddressPlace;
import com.navinfo.dataservice.dao.plus.innermodel.CiParaIxPoiAddressStreet;
import com.navinfo.dataservice.dao.plus.innermodel.CiParaRdName;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;

import net.sf.json.JSONObject;

/**
 * @ClassName: FMBAT20110
 * @author: zhangpengpeng
 * @date: 2017年2月13日
 * @Desc: FMBAT20110.java poi地址拆分批处理
 */
public class FMBAT20110 extends BasicBatchRule {

	//private Connection conn;
	
	private String REGEXP_HOUSE_NUM = "([-~～/／－0-9０-９a-zA-Zａ-ｚＡ-Ｚ零一二三四五六七八九十])+";
	private String REGEXP_ESTAB = "(大厦|商厦)";
	private String REGEXP_BUILDING = "";
	private List<String> buildingKeys = new ArrayList<String>();
	private String REGEXP_UNIT = "";
	private List<String> unitKeys = new ArrayList<String>();
	private String REGEXP_FLOOR = "";
	private List<String> floorKeys = new ArrayList<String>();
	private String REGEXP_ROOMNUM = "";
	private List<String> roomKeys = new ArrayList<String>();
	private String REGEXP_TYPE = "";
	private List<String> type1Key = new ArrayList<String>();
	private List<String> type3Key = new ArrayList<String>();
	
	// select pre_key from sc_point_addrck t where t.type=11 
	private List<String> type11Key = new ArrayList<String>();
	private List<String> type12Key = new ArrayList<String>();
	
	public FMBAT20110() throws Exception {
		
		MetadataApi metadata = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
		buildingKeys = metadata.queryAdRack(6);
		if (buildingKeys.size() > 0) {
			this.REGEXP_BUILDING = "(" + StringUtils.join(buildingKeys, "|") + ")+";
		}
		unitKeys = metadata.queryAdRack(8);
		if (unitKeys.size() > 0) {
			this.REGEXP_UNIT = "(" + StringUtils.join(unitKeys, "|") + ")+";
		}
		floorKeys = metadata.queryAdRack(2);
		if (floorKeys.size() > 0) {
			this.REGEXP_FLOOR = "(" + StringUtils.join(floorKeys, "|") + ")+";
		}
		roomKeys = metadata.queryAdRack(7);
		if (roomKeys.size() > 0) {
			this.REGEXP_ROOMNUM = "(" + StringUtils.join(roomKeys, "|") + ")+";
		}
		type1Key = metadata.queryAdRack(1);
		if (type1Key.size() > 0) {
			this.REGEXP_TYPE = "((" + StringUtils.join(type1Key, "|") + ")+|$)";
		}
		type3Key = metadata.queryAdRack(3);
		type11Key = metadata.queryAdRack(11);
		type12Key = metadata.queryAdRack(12);
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
	}

	@Override
	public void runBatch(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		IxPoi poi = (IxPoi) obj.getMainrow();
		List<IxPoiAddress> addresses = poiObj.getIxPoiAddresses();
		if (addresses == null || addresses.size() == 0) {return;}
		long adminCode = getAdminCode(super.getBatchRuleCommand().getConn(), poi.getRegionId());
		log.debug("adminCode=" + adminCode);
		if (adminCode == 0) {return;}
		boolean isChanged = false;
//		boolean isChanged = true;
		// 存在IX_POI_ADDRESS新增或者修改履历
		for (IxPoiAddress addr: addresses) {
			if (addr.getHisOpType().equals(OperationType.INSERT) || addr.getHisOpType().equals(OperationType.UPDATE)){
				isChanged = true;
				break;
			}
		}
		if (isChanged){
			// 开始执行批处理
			for (IxPoiAddress addressWrap: addresses){
				boolean streetInDb = false; //在库中找到道路名
				boolean placeInDb = false; //在库中找到place
				boolean placeWithkey = false; //存在place关键字
				boolean streetWithkey=false; //存在道路名关键字
				String langCode = addressWrap.getLangCode();
				if (!"CHI".equals(langCode) && !"CHT".equals(langCode)) {continue;}
				String addFull = addressWrap.getFullname();
				if (StringUtils.isEmpty(addFull)) {continue;}
	            log.debug("addFull="+addFull);
	            log.debug("addFull转全角");
				String sbc = ExcelReader.h2f(addFull);
				if (!addFull.equals(sbc)){
					addressWrap.setFullname(sbc);
					addFull = sbc;
				}
				addFull = addFull.replaceAll("－", "－").replaceAll("-", "－").replaceAll("—", "－");
				log.debug("addFull="+addFull);
				List<String> nameList = queryRdName(adminCode,addFull);
				log.debug("开始处理street名.....");
				String street = matchField(addFull, nameList);//街巷名
				log.debug("street:"+street);
		        String streetPre = ""; //道路名前
		        String streetPost = ""; //道路名后
		        String landMark = ""; //标志物字段
		        String province = ""; //行政区划：省
		        String city = ""; //行政区划：市
		        String county = ""; //行政区划：区县
		        String town = ""; //乡镇/街道办
		        String place = ""; //地名小区名
		        String prefix = ""; //前缀
		        String houseNum = ""; //门牌号
		        String type = ""; //类型
		        String subNum = ""; //子号
		        // surfix待实现，规格中没有点门牌的信息，所以无法实现
		        String surfix = ""; //后缀
		        String estab = ""; //附属设施
		        String building = ""; //楼栋号
		        String unit = ""; //楼门号
		        String floor = ""; //楼层
		        String roomNum = ""; //房间号
		        String addOns = ""; //附加信息
		        if (StringUtils.isNotEmpty(street)){
	                streetInDb = true;
	                int keyIdx = addFull.indexOf(street);
	                streetPre = addFull.substring(0, keyIdx);
	                log.debug("存在street，streetPre="+streetPre);
	                streetPost = addFull.substring(keyIdx+street.length());
	                log.debug("存在street，streetPost="+streetPost);
		        }
		        if (StringUtils.isEmpty(street)){
	                /*读取母库中“IX_POI_ADDRESS”表中“STREET”（街巷名字段）及该POI的行政区划（region_id），
                 		如果相同行政区划的地址与“STREET”匹配（优先取最长的），
                    	则将匹配的道路名存储到STREET（街巷名）字段，
                   		如果出现多个匹配相同的道路名，把多个道路名及之间的内容存放到街巷名字段中。
	                                            另：对于读取母库提取出的道路名，
	                                            如果其在配置表 “SC_POINT_ADDRCK，TYPE=11，街巷名”中存在，则不提取该拆分结果。
	                                         根据以上的二代实现原则,采用如下原则:在编辑池中查找admincode和当前poi相同的street*/
		        	log.debug("adminCode:"+adminCode);
	                List<String> streetList = queryStreetFromIxPoiAddress(adminCode,addFull);
	                street = matchField(addFull,streetList);
	                log.debug("street="+street);
	                if (StringUtils.isNotEmpty(street)){
	                	streetInDb = true;
	                    int keyIdx = addFull.indexOf(street);
	                    streetPre = addFull.substring(0, keyIdx);
	                    log.debug("streetPre="+streetPre);
	                    streetPost = addFull.substring(keyIdx + street.length());
	                    log.debug("streetPost="+streetPost);
	                } else {
	                    //匹配关键字①：包含（“路”“街”“道”“巷”“胡同”）。
	                    //A、对于地址中含有“路”“街”“道”“巷”“胡同”关键字，拆分成道路名前（包含关键字①）及道路名后。
	                	int kLuIdx = addFull.lastIndexOf("路");
	                	int kJieIdx = addFull.lastIndexOf("街");
	                	int kDaoIdx = addFull.lastIndexOf("道");
	                	int kXiangIdx = addFull.lastIndexOf("巷");
	                	int kHutongIdx = addFull.lastIndexOf("胡同");
	                	List<Integer> indxs = Arrays.asList(kLuIdx, kJieIdx, kDaoIdx, kXiangIdx, kHutongIdx);
	                	int kIdx = Collections.max(indxs);
	                	if (kIdx > -1){
	                		streetWithkey = true;
	                        String key = "";
	                        if (kIdx == kLuIdx){ key = "路";}
	                        if (kIdx == kJieIdx){ key = "街";}
	                        if (kIdx == kDaoIdx){ key = "道";}
	                        if (kIdx == kXiangIdx){ key = "巷";}
	                        if (kIdx == kHutongIdx){ key = "胡同";}
	                        int keyLen = key.length();
	                        streetPre = addFull.substring(0, kIdx + keyLen);
	                        streetPost = addFull.substring(kIdx + keyLen);
	                	}
	                	if (StringUtils.isEmpty(streetPre)){
	                		streetPre = "";
	                	}
	                    log.debug("streetPre="+streetPre);
	                    log.debug("streetPost="+streetPost);
	                    log.debug("streetWithkey="+streetWithkey);
	                }
		        }
		        log.debug("开始处理地区小区名.....");
	            String placePre = ""; //地名/小区名前
	            String placePost = ""; //地名/小区名后
	            CiParaIxPoiAddressPlace placeSelector = new CiParaIxPoiAddressPlace(super.getBatchRuleCommand().getConn());
	            if (StringUtils.isNotEmpty(street) && StringUtils.isNotEmpty(streetPre)){
	            	log.debug("A类逻辑");
	            	List<String> placeList = placeSelector.queryPlace(adminCode, streetPre, type12Key);
	            	place = matchField(streetPre, placeList);
	            	log.debug("place="+place);
	            	if (StringUtils.isNotEmpty(place)){
	            		placeInDb = true;
	            		int keyIdx = streetPre.indexOf(place);
	            		placePre = streetPre.substring(0, keyIdx);
	            		placePost = streetPre.substring(keyIdx + place.length());
	            		landMark = placePost;
	                    log.debug("streetPre="+streetPre);
	                    log.debug("placePre="+placePre);
	                    log.debug("streetPost="+streetPost);
	                    log.debug("landMark="+landMark);
	            	} else {
	                    //按照从右往左与地名关键字②字段值（小区、村、开发区、景区、高新区）进行匹配。
                        //如果存在匹配字样（以最后一个关键字②为准），则把匹配字样及前面内容归为“地名小区名前”，地名小区名后内容存放在标志物字段中。
	            		int kXqIdx = streetPre.lastIndexOf("小区");
	            		int kCunIdx = streetPre.lastIndexOf("村");
	            		int kKfqIdx = streetPre.lastIndexOf("开发区");
	            		int kJqIdx = streetPre.lastIndexOf("景区");
	            		int kGxqIdx = streetPre.lastIndexOf("高新区");
	                	List<Integer> indxs = Arrays.asList(kXqIdx, kCunIdx, kKfqIdx, kJqIdx, kGxqIdx);
	                	int kIdx = Collections.max(indxs);
	                	if (kIdx > -1){
	                		placeWithkey = true;
	                		String key = "";
	                		if (kIdx==kXqIdx){key="小区";}
	                		if (kIdx==kCunIdx){key="村";}
	                		if (kIdx==kKfqIdx){key="开发区";}
	                		if (kIdx==kJqIdx){key="景区";}
	                		if (kIdx==kGxqIdx){key="高新区";}
	                		int keyLen = key.length();
	                		placePre = streetPre.substring(0, kIdx + keyLen);
	                		placePost = streetPre.substring(kIdx + keyLen);
	                		if (StringUtils.isNotEmpty(placePost)){
	                			landMark = placePost;
	                		}
	                	}
	                    log.debug("placePre="+placePre);
	                    log.debug("placePost="+placePost);
	                    log.debug("landMark="+landMark);
	                    log.debug("placeWithkey="+placeWithkey);
	            	}
	            }
	            if (StringUtils.isEmpty(street) && StringUtils.isNotEmpty(streetPre) && streetWithkey){
	            	log.debug("B类逻辑");
	            	List<String> placeList = placeSelector.queryPlace(adminCode, streetPre, type12Key);
	            	place = matchField(streetPre,placeList);
	            	log.debug("place="+place);
	            	if (StringUtils.isNotEmpty(place)){
	            		placeInDb = true;
	            		int keyIdx = streetPre.indexOf(place);
	            		placePre = streetPre.substring(0, keyIdx);
	            		placePost = streetPre.substring(keyIdx + place.length());
	            		street = placePost;
	                    log.debug("streetPre="+streetPre);
	                    log.debug("streetPost="+streetPost);
	                    log.debug("street="+street);
	            	} else {
	                    //按照从右往左与地名关键字②字段值（小区、村、开发区、景区、高新区）进行匹配。
                        //如果存在匹配字样（以最后一个关键字②为准），则把匹配字样及前面内容归为“地名小区名前”，地名小区名后内容存放在街巷名字段中。
	            		int kXqIdx = streetPre.lastIndexOf("小区");
	            		int kCunIdx = streetPre.lastIndexOf("村");
	            		int kKfqIdx = streetPre.lastIndexOf("开发区");
	            		int kJqIdx = streetPre.lastIndexOf("景区");
	            		int kGxqIdx = streetPre.lastIndexOf("高新区");
	                	List<Integer> indxs = Arrays.asList(kXqIdx, kCunIdx, kKfqIdx, kJqIdx, kGxqIdx);
	                	int kIdx = Collections.max(indxs);
	                	if (kIdx > -1){
	                		placeWithkey = true;
	                		String key = "";
	                		if (kIdx==kXqIdx){key="小区";}
	                		if (kIdx==kCunIdx){key="村";}
	                		if (kIdx==kKfqIdx){key="开发区";}
	                		if (kIdx==kJqIdx){key="景区";}
	                		if (kIdx==kGxqIdx){key="高新区";}
	                		int keyLen = key.length();
	                		placePre = streetPre.substring(0, kIdx + keyLen);
	                		placePost = streetPre.substring(kIdx + keyLen);
	                		if (StringUtils.isNotEmpty(placePost)){
	                			street = placePost;
	                		}
	                	}
	                	if (StringUtils.isEmpty(placePre)){placePre = "";}
	                	if (StringUtils.isEmpty(placePost)){placePost = "";}
	                	if (StringUtils.isEmpty(town)){town = "";}
	                	if (StringUtils.isEmpty(street)){street = "";}
	                    log.debug("placePre="+placePre);
	                    log.debug("placePost="+placePost);
	                    log.debug("town="+town);
	                    log.debug("street="+street);
	                    log.debug("placeWithkey="+placeWithkey);
	            	}
	            }
	            //在库中找不到道路名匹配，也无法从关键字拆分出道路名的，暂不拆分。将全字段作为“道路名前地址”
	            if (StringUtils.isEmpty(street) && !streetWithkey){
	            	log.debug("C类逻辑");
	            	streetPre = addFull;
	            	log.debug("streetPre="+streetPre);
	            	List<String> placeList = placeSelector.queryPlace(adminCode, streetPre, type12Key);
	            	place = matchField(streetPre,placeList);
	            	log.debug("place="+place);
	            	if (StringUtils.isNotEmpty(place)){
	            		placeInDb = true;
	            		int keyIdx = streetPre.indexOf(place);
	            		placePre = streetPre.substring(0, keyIdx);
	            		streetPost = streetPre.substring(keyIdx + place.length());
	                    log.debug("placePre="+placePre);
	                    log.debug("streetPost="+streetPost);
	            	} else{
	            		//按照从右往左与地名关键字②字段值（小区、村、开发区、景区、高新区）进行匹配。
                        //如果存在匹配字样（以最后一个关键字②为准），则把匹配字样及前面内容归为“地名小区名前”，地名小区名后内容存放在街巷名字段中。
	            		int kXqIdx = streetPre.lastIndexOf("小区");
	            		int kCunIdx = streetPre.lastIndexOf("村");
	            		int kKfqIdx = streetPre.lastIndexOf("开发区");
	            		int kJqIdx = streetPre.lastIndexOf("景区");
	            		int kGxqIdx = streetPre.lastIndexOf("高新区");
	                	List<Integer> indxs = Arrays.asList(kXqIdx, kCunIdx, kKfqIdx, kJqIdx, kGxqIdx);
	                	int kIdx = Collections.max(indxs);
	                	if (kIdx > -1){
	                		placeWithkey = true;
	                		String key = "";
	                		if (kIdx==kXqIdx){key="小区";}
	                		if (kIdx==kCunIdx){key="村";}
	                		if (kIdx==kKfqIdx){key="开发区";}
	                		if (kIdx==kJqIdx){key="景区";}
	                		if (kIdx==kGxqIdx){key="高新区";}
	                		int keyLen = key.length();
	                		placePre = streetPre.substring(0, kIdx + keyLen);
	                		streetPost = streetPre.substring(kIdx + keyLen);
	                		if (StringUtils.isNotEmpty(streetPost)){
	                			town = placePost;
	                		}
	                	}
	                    log.debug("placePre="+placePre);
	                    log.debug("streetPost="+streetPost);
	                    log.debug("town="+town);
	                    log.debug("placeWithkey="+placeWithkey);
	            	}
	            }
	            if (StringUtils.isEmpty(place) && StringUtils.isEmpty(placePre)){
	            	placePre = streetPre;
	            }
	            log.debug("placePre="+placePre);
	            
	            log.debug("开始处理行政区划.....");
	            CiParaAdAdmin adAminSelector = new CiParaAdAdmin(super.getBatchRuleCommand().getConn());
	            List<ArrayList<String>> adAdminList = adAminSelector.queryAdAdmin(adminCode, streetPre);
	            if (adAdminList.isEmpty()){
	            	log.debug("在ci_para_ad_admin表中没有找到poi[pid=" + addressWrap.getPoiPid() + ",adminCode="+ adminCode + "]对应的行政区划信息，不执行行政区的拆分");
	            }else {
	            	//执行行政区划的拆分
	            	for(ArrayList<String> _adminList: adAdminList){
	            		String _prov = _adminList.get(0);
	            		String _city = _adminList.get(1);
	            		String _county = _adminList.get(2);
	            		String _wholeName = _adminList.get(3);
	            		// 省
	            		if (StringUtils.isNotEmpty(_prov) && (StringUtils.isEmpty(province) || province.length()<_prov.length()) && placePre.indexOf(_prov)>-1){
	            			province = _prov;
	            		}
	            		// 市
	            		//对于北京、上海、天津、重庆四个直辖市，只保存省名与县名，市名为空
	            		if (province.indexOf("北京")>-1 || province.indexOf("上海")>-1 || province.indexOf("天津")>-1 || province.indexOf("重庆")>-1){
	            			city = "";
	            		} else {
	            			if (StringUtils.isNotEmpty(_city) && (StringUtils.isEmpty(city) || city.length()<_city.length()) || placePre.indexOf(_city)>-1){
	            				city = _city;
	            			}
	            		}
	            		// 县
	            		if (StringUtils.isNotEmpty(_county) && (StringUtils.isEmpty(county) || county.length()<_county.length()) && placePre.indexOf(_county)>-1){
	            			county = _county;
	            		}
	            	}
	            }
	            if (StringUtils.isNotEmpty(province) && StringUtils.isNotEmpty(placePre) && placePre.startsWith(province)){
	            	placePre = placePre.replaceFirst(province, "");
	            }else {
	            	province = "";
	            }
	            if (StringUtils.isNotEmpty(city) && StringUtils.isNotEmpty(placePre) && placePre.startsWith(city)){
	            	placePre = placePre.replaceFirst(city, "");
	            }else {
	            	city = "";
	            }
	            if (StringUtils.isNotEmpty(county) && StringUtils.isNotEmpty(placePre) && placePre.startsWith(county)){
	            	placePre = placePre.replaceFirst(county, "");
	            }else {
	            	county = "";
	            }
	            log.debug("province:"+province);
	            log.debug("city:"+city);
	            log.debug("county:"+county);
	            log.debug("town:"+town);
	            log.debug("place:"+place);
	            log.debug("street:"+street);
	            log.debug("landMark:"+landMark);
	            log.debug("placePre="+placePre);
	            if (StringUtils.isNotEmpty(placePre)){
	            	//地址中出现关键字③“乡、镇、街道”字样时：把乡镇及前面部分内容存储到AU_IX_POI_ADDRESS表中的TOWN（乡镇/街道办）字段。出现多个关键字时，已最后一个关键字为准
	            	JSONObject xiangJson = parseByKey(placePre, "乡");
	            	String townXiangPre = xiangJson.getString("streetPre");
	            	String townXiangPost = xiangJson.getString("streetPost");
	            	int kXiangIdx = xiangJson.getInt("keyIdx");
	            	
	            	JSONObject zhenJson = parseByKey(placePre, "镇");
	            	String townZhenPre = zhenJson.getString("streetPre");
	            	String townZhenPost = zhenJson.getString("streetPost");
	            	int kZhenIdx = zhenJson.getInt("keyIdx");
	            	
	            	JSONObject jieJson = parseByKey(placePre, "街道");
	            	String townJiedaoPre = jieJson.getString("streetPre");
	            	String townJiedaoPost = jieJson.getString("streetPost");
	            	int kJieDaoIdx = jieJson.getInt("keyIdx");
                	List<Integer> indxs = Arrays.asList(kXiangIdx, kZhenIdx, kJieDaoIdx);
                	int maxIdx = Collections.max(indxs);
                	String _place = "";
                	String _landMark = "";
                	String _street = "";
                	String _steetPost = "";
                	if (-1 == maxIdx){
                		JSONObject placeLandMarkJson = setPlaceAndLandMark(streetInDb, placeInDb, placeWithkey, streetWithkey, placePre);
                		_place = placeLandMarkJson.getString("place");
                		_landMark = placeLandMarkJson.getString("landMark");
                		_street = placeLandMarkJson.getString("street");
                		_steetPost = placeLandMarkJson.getString("streetPost");
                	} else {
                		if (kXiangIdx == maxIdx){
                			town = townXiangPre.substring(0, maxIdx+1);
                    		JSONObject placeLandMarkJson = setPlaceAndLandMark(streetInDb, placeInDb, placeWithkey, streetWithkey, townXiangPost);
                    		_place = placeLandMarkJson.getString("place");
                    		_landMark = placeLandMarkJson.getString("landMark");
                    		_street = placeLandMarkJson.getString("street");
                    		_steetPost = placeLandMarkJson.getString("streetPost");
                		}else{
                			if (kZhenIdx==maxIdx){
                				town = townZhenPre.substring(0, maxIdx+1);
                        		JSONObject placeLandMarkJson = setPlaceAndLandMark(streetInDb, placeInDb, placeWithkey, streetWithkey, townZhenPost);
                        		_place = placeLandMarkJson.getString("place");
                        		_landMark = placeLandMarkJson.getString("landMark");
                        		_street = placeLandMarkJson.getString("street");
                        		_steetPost = placeLandMarkJson.getString("streetPost");
                			}else {
                				if (kJieDaoIdx==maxIdx){
                					// 因为是“街道”所以+2
                					town = townJiedaoPre.substring(0, maxIdx+2);
                            		JSONObject placeLandMarkJson = setPlaceAndLandMark(streetInDb, placeInDb, placeWithkey, streetWithkey, townJiedaoPost);
                            		_place = placeLandMarkJson.getString("place");
                            		_landMark = placeLandMarkJson.getString("landMark");
                            		_street = placeLandMarkJson.getString("street");
                            		_steetPost = placeLandMarkJson.getString("streetPost");
                				}
                			}
                		}
                	}
                	if (StringUtils.isNotEmpty(_place) && StringUtils.isEmpty(place)){ place = _place;}
                	if (StringUtils.isNotEmpty(_landMark)){
                		if (StringUtils.isEmpty(landMark)){
                			landMark = _landMark;
                		} else{
                			landMark = landMark + "-" + _landMark;
                		}
                	}
                	if (StringUtils.isNotEmpty(_street) && StringUtils.isEmpty(street)){ street = _street;}
                	if (StringUtils.isNotEmpty(_steetPost)){ streetPost = _steetPost;}
                	if (StringUtils.isEmpty(town)){ town = "";}
                	log.debug("town:"+town);
                	if (StringUtils.isEmpty(place)){ place = "";}
                	log.debug("place:"+place);
                	if (StringUtils.isEmpty(landMark)){ landMark = "";}
                	log.debug("landMark:"+landMark);
                	if (StringUtils.isEmpty(streetPost)){ streetPost = "";}
                	log.debug("streetPost:"+streetPost);
	            }
	            if (StringUtils.isEmpty(street) && StringUtils.isEmpty(place) && StringUtils.isEmpty(province) && StringUtils.isEmpty(city) && StringUtils.isEmpty(county) && StringUtils.isEmpty(town) && StringUtils.isEmpty(landMark)){
	            	streetPost = addFull;
	            }
	            log.debug("streetPost="+streetPost);
	            log.debug("根据道路名后地址 处理门牌号、类型、前缀");
	            log.debug("REGEXP_TYPE:"+REGEXP_TYPE);
	            if (StringUtils.isNotEmpty(streetPost)){
	            	// 如果数字串为地址结尾，即后面没有内容。则把数字存储到HOUSENUM（门牌号）字段
	            	Pattern houseNumPattern = Pattern.compile(REGEXP_HOUSE_NUM + "$");
	            	Matcher houseNumMatch = houseNumPattern.matcher(streetPost);
	            	if (houseNumMatch.find()){
	            		houseNum = houseNumMatch.group();
	            		log.debug("houseNum="+houseNum);
	            	}
	                //如果数字串后面有内容，判断紧跟在数字后面内容是否在《拆分参照表》类型字段中存在，如果存在，则把数字串存储到HOUSENUM（门牌号）字段；类型词存放在“类型”字段中
	            	Pattern houseNumTypePattern = Pattern.compile(REGEXP_HOUSE_NUM + REGEXP_TYPE);
	            	Matcher houseNumTypeMatch = houseNumTypePattern.matcher(streetPost);
	            	String subNumPost = streetPost;
	            	String houseNumWithType = "";
	            	if (houseNumTypeMatch.find()){
	            		houseNumWithType = houseNumTypeMatch.group();
	            		Pattern hsNumPattern = Pattern.compile(REGEXP_HOUSE_NUM);
	            		Matcher hsNumMatch = hsNumPattern.matcher(houseNumWithType);
	            		if (hsNumMatch.find()){
	            			houseNum = hsNumMatch.group();
	            			log.debug("houseNum="+houseNum);
	            			type = houseNumWithType.substring(houseNum.length());
	            			log.debug("type="+type);
	            			//TODO python的match.regs原则搞清楚  subNumPost = streetPost[numTypeMatch.regs[0][1]:]
	            			subNumPost = streetPost.substring(streetPost.indexOf(houseNumWithType)+houseNumWithType.length());
	            			log.debug("subNumPost="+subNumPost);
	            		}
	                    //如果“门牌号（变更前）”字段非空，门牌号字段值中存在“—”(中横杆)，则将第一个“—”及其后面内容+类型字段值存储到SUBNUM（子号）字段。第一个“—”前面内容保存在“门牌号（变更后）”字段 #FIXME
	            		int slashIdx = houseNumWithType.indexOf("-");
	            		if (slashIdx == -1){
	            			slashIdx = houseNumWithType.indexOf("－");
	            		}
	            		if (slashIdx != -1){
	            			subNum = houseNumWithType.substring(slashIdx);
	            			houseNum = houseNumWithType.substring(0, slashIdx);
	            			type = "";
	                        log.debug("subNum="+subNum);
	                        log.debug("houseNum="+houseNum);
	                        log.debug("type="+type);
	                        log.debug("subNumPost="+subNumPost);
	            		}
	            	}
	            	log.debug("subNumPost="+subNumPost);
	                //是门牌的，“门牌字符串”前的内容，都放入“前缀”
	            	if (StringUtils.isNotEmpty(houseNum) && StringUtils.isEmpty(houseNumWithType)){
	            		prefix = streetPost.substring(0, streetPost.indexOf(houseNum));
	            		log.debug("prefix="+prefix);
	            	} else {
	            		if (StringUtils.isNotEmpty(houseNumWithType)){
	            			// TODO python prefix=streetPost[0:numTypeMatch.regs[0][0]]
	            			prefix = streetPost.substring(0, streetPost.indexOf(houseNumWithType));
	            			log.debug("prefix="+prefix);
	            		}
	            	}
	            	
	                //【附属设施】——“字号后内容”
	            	String estabPost = subNumPost;
	            	if (StringUtils.isNotEmpty(subNumPost)){
	            		Pattern estabPattern = Pattern.compile(REGEXP_ESTAB);
	            		Matcher estabMatch = estabPattern.matcher(subNumPost);
	            		if (estabMatch.find()){
	            			String estabType = estabMatch.group();
	            			log.debug("estabType="+estabType);
	            			int estabIdx = subNumPost.indexOf(estabType);
	            			estab = subNumPost.substring(0, estabIdx+estabType.length());
	            			log.debug("estab="+estab);
	            			estabPost = subNumPost.substring(estabIdx+estabType.length());
	            			log.debug("estabPost="+estabPost);
	            		}
	            	}
	            	
	            	//【楼栋号】——“附属设施名后内容”
	            	String buildingPost = estabPost;
	            	log.debug("REGEXP_BUILDING="+REGEXP_BUILDING);
	            	if (StringUtils.isNotEmpty(estabPost) && StringUtils.isNotEmpty(REGEXP_BUILDING)){
	            		//附属设施名后内容与楼栋号关键字字段值进行匹配，如果存在匹配字样，则把匹配字样及前面内容存储到楼栋号字段
	            		Pattern buildPattern = Pattern.compile(REGEXP_BUILDING);
	            		Matcher buildMatch = buildPattern.matcher(estabPost);
	            		if (buildMatch.find()){
	            			String buildingKey = buildMatch.group();
	            			int bKeyIdx = estabPost.indexOf(buildingKey);
	            			building = estabPost.substring(0, bKeyIdx+buildingKey.length());
	            			log.debug("building="+building);
	            			buildingPost = estabPost.substring(bKeyIdx+buildingKey.length());
	            			log.debug("buildingPost="+buildingPost);
	            		}
	            	}
	            	
	            	//【楼门号】——“楼栋号后内容”
	                String unitPost = buildingPost;
                    log.debug("REGEXP_UNIT="+REGEXP_UNIT);
                    if (StringUtils.isNotEmpty(buildingPost) && StringUtils.isNotEmpty(REGEXP_UNIT)){
                        Pattern unitPattern = Pattern.compile(REGEXP_UNIT);
                        Matcher unitMatch = unitPattern.matcher(buildingPost);
                        if (unitMatch.find()){
                            String unitKey = unitMatch.group();
                            int unitKeyIdx = buildingPost.indexOf(unitKey);
                            unit = buildingPost.substring(0, unitKeyIdx+unitKey.length());
                            log.debug("unit="+unit);
                            unitPost = buildingPost.substring(unitKeyIdx+unitKey.length());
                            log.debug("unitPost="+unitPost);
                        }
                    }
                    
                    //【楼层】——“楼门号后内容”
                    String floorPost = unitPost;
                    log.debug("REGEXP_FLOOR="+REGEXP_FLOOR); 
                    if (StringUtils.isNotEmpty(unitPost)&& StringUtils.isNotEmpty(REGEXP_FLOOR)){
                        Pattern floorPattern = Pattern.compile(REGEXP_FLOOR);
                        Matcher floorMatch = floorPattern.matcher(unitPost);
                        if(floorMatch.find()){
                            String floorKey = floorMatch.group();
                            int floorKeyIdx = unitPost.indexOf(floorKey);
                            floor = unitPost.substring(0, floorKeyIdx+floorKey.length());
                            log.debug("floor="+floor);
                            floorPost = unitPost.substring(floorKeyIdx+floorKey.length());
                            log.debug("floorPost="+floorPost); 
                        }
                    }
                    
                    //【房间号】——“楼层后内容”
                    log.debug("REGEXP_ROOMNUM="+REGEXP_ROOMNUM);
                    if (StringUtils.isNotEmpty(floorPost) && StringUtils.isNotEmpty(REGEXP_ROOMNUM)){
                        Pattern roomPattern = Pattern.compile(REGEXP_ROOMNUM);
                        Matcher roomMatch = roomPattern.matcher(floorPost);
                        if (roomMatch.find()){
                        	String roomNumKey = roomMatch.group();
                        	int roomNumKeyIdx = floorPost.indexOf(roomNumKey);
                        	roomNum = floorPost.substring(0, roomNumKeyIdx+roomNumKey.length());
                        	log.debug("roomNum="+roomNum);
                        	// 剩下的部分存放在“其他附加信息”
                        	addOns = floorPost.substring(roomNumKeyIdx+roomNumKey.length());
                        	log.debug("addOns="+addOns);
                        } else{
                        	addOns = floorPost;
                        	log.debug("addOns="+addOns);
                        }
                    }
	            }
	            
	            if (StringUtils.isEmpty(province)){ province="";}
	            if (StringUtils.isEmpty(city)){ city="";}
	            if (StringUtils.isEmpty(county)){ county="";}
	            if (StringUtils.isEmpty(town)){ town="";}
	            if (StringUtils.isEmpty(place)){ place="";}
	            if (StringUtils.isEmpty(street)){ street="";}
	            
	            if (StringUtils.isEmpty(landMark)){ landMark = "";}
	            if (StringUtils.isEmpty(prefix)){ prefix = "";}
	            if (StringUtils.isEmpty(houseNum)){ houseNum = "";}
	            if (StringUtils.isEmpty(type)){ type = "";}
	            if (StringUtils.isEmpty(subNum)){ subNum = "";}
	            if (StringUtils.isEmpty(surfix)){ surfix = "";}
	            if (StringUtils.isEmpty(estab)){ estab = "";}
	            if (StringUtils.isEmpty(building)){ building = "";}
	            if (StringUtils.isEmpty(floor)){ floor = "";}
	            if (StringUtils.isEmpty(unit)){ unit = "";}
	            if (StringUtils.isEmpty(roomNum)){ roomNum = "";}
	            if (StringUtils.isEmpty(addOns)){ addOns = "";}
	            String stringPlus = prefix + houseNum + type + subNum + surfix + estab + building + floor + unit + roomNum + addOns;
	            if (stringPlus.length() > 0 && landMark.equals(streetPost)){
	            	landMark = "";
	            	log.debug("landMark="+landMark);
	            }
	            //这里加入特殊处理逻辑,需求来自FM地址拆分对比优化20150228.docx
	            JSONObject specialJson = doSpecially(prefix, houseNum, type, subNum, surfix, estab, building, unit, floor, roomNum, addOns);
	            prefix = specialJson.getString("prefix");
	            houseNum = specialJson.getString("houseNum");
	            type = specialJson.getString("type");
	            subNum = specialJson.getString("subNum");
	            surfix = specialJson.getString("surfix");
	            estab = specialJson.getString("estab");
	            building = specialJson.getString("building");
	            floor = specialJson.getString("floor");
	            unit = specialJson.getString("unit");
	            roomNum = specialJson.getString("roomNum");
	            addOns = specialJson.getString("addOns");
	            
	            // 将拆分出的18个字段依次set到address子表
	            MetadataApi metadataApi=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
	            // province
	            addressWrap.setProvince(province);
	            addressWrap.setProvPhonetic(metadataApi.pyConvertHz(province));
	            // city
	            addressWrap.setCity(city);
	            addressWrap.setCityPhonetic(metadataApi.pyConvertHz(city));
	            // county
	            addressWrap.setCounty(county);
	            addressWrap.setCountyPhonetic(metadataApi.pyConvertHz(county));
	            // town
	            addressWrap.setTown(town);
	            addressWrap.setTownPhonetic(metadataApi.pyConvertHz(town));
	            // place
	            addressWrap.setPlace(place);
	            addressWrap.setPlacePhonetic(metadataApi.pyConvertHz(place));
	            // street
	            addressWrap.setStreet(street);
	            addressWrap.setStreetPhonetic(metadataApi.pyConvertHz(street));
	            // landMark
	            addressWrap.setLandmark(landMark);
	            addressWrap.setLandmarkPhonetic(metadataApi.pyConvertHz(landMark));
	            // prefix
	            addressWrap.setPrefix(prefix);
	            addressWrap.setPrefixPhonetic(metadataApi.pyConvertHz(prefix));
	            // houseNum
	            addressWrap.setHousenum(houseNum);
	            addressWrap.setHousenumPhonetic(metadataApi.pyConvertHz(houseNum));
	            // type
	            addressWrap.setType(type);
	            addressWrap.setTypePhonetic(metadataApi.pyConvertHz(type));
	            // subNum
	            addressWrap.setSubnum(subNum);
	            addressWrap.setSubnumPhonetic(metadataApi.pyConvertHz(subNum));
	            // surfix
	            addressWrap.setSurfix(surfix);
	            addressWrap.setSurfixPhonetic(metadataApi.pyConvertHz(surfix));
	            // estab
	            addressWrap.setEstab(estab);
	            addressWrap.setEstabPhonetic(metadataApi.pyConvertHz(estab));
	            // building
	            addressWrap.setBuilding(building);
	            addressWrap.setBuildingPhonetic(metadataApi.pyConvertHz(building));
	            // floor
	            addressWrap.setFloor(floor);
	            addressWrap.setFloorPhonetic(metadataApi.pyConvertHz(floor));
	            // unit
	            addressWrap.setUnit(unit);
	            addressWrap.setUnitPhonetic(metadataApi.pyConvertHz(unit));
	            // roomNum
	            addressWrap.setRoom(roomNum);
	            addressWrap.setRoomPhonetic(metadataApi.pyConvertHz(roomNum));
	            // addOns
	            addressWrap.setAddons(addOns);
	            addressWrap.setAddonsPhonetic(metadataApi.pyConvertHz(addOns));
	            
			}
		}
	}
	
	public List<String> queryRdName(long adminCode, String addFull) throws Exception{
		CiParaRdName  ciParaRdNameSearch = new CiParaRdName(super.getBatchRuleCommand().getConn());
		return ciParaRdNameSearch.queryRdNameInAddress(adminCode, addFull);
	}
	
	public List<String> queryStreetFromIxPoiAddress(long adminCode, String addFull) throws Exception{
		CiParaIxPoiAddressStreet selector = new CiParaIxPoiAddressStreet(super.getBatchRuleCommand().getConn());
		return selector.queryStreet(adminCode, addFull, type11Key);
	}
	
	public long getAdminCode(Connection conn, long regionId) throws Exception{
		long adminCode = 0;
		String sql = "select admin_id from ad_admin where region_id=" + regionId;

		PreparedStatement pstmt = null;
		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);
			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				adminCode = resultSet.getLong("admin_id");
			}
			return adminCode;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
	}

	public JSONObject findMaxIdx(String estabPost, List<String> buildingKeys) {
		JSONObject res = new JSONObject();
		JSONObject keyMap = new JSONObject();
		for (String key : buildingKeys) {
			int idx = estabPost.lastIndexOf(key);
			keyMap.put(idx, key);
		}
		int maxIdx = Collections.max((List<Integer>) keyMap.keys());
		res.put("idx", maxIdx);
		res.put("key", keyMap.get(maxIdx));
		return res;
	}

	public String matchField(String addFull, List<String> nameList) {
		String street = "";
		// 如果出现多个匹配相同的道路名，把多个道路名及之间的内容存放到街巷名字段中
		if (nameList.size() > 0) {
			List<Integer> idxs = new ArrayList<Integer>();
			List<Integer> maxIdx = new ArrayList<Integer>();
			for (String name : nameList) {
				int length = name.length();
				int index = addFull.indexOf(name);
				idxs.add(index);
				maxIdx.add(index + length);

				int rindex = addFull.lastIndexOf(name);
				idxs.add(rindex);
				maxIdx.add(rindex + length);
			}
			int minIndex = Collections.min(idxs);
			int maxIndex = Collections.max(maxIdx);
			street = addFull.substring(minIndex, maxIndex);
		}
		return street;
	}

	public JSONObject parseByKey(String addFull, String key) {
		JSONObject res = new JSONObject();
		String streetPre = "";
		String streetPost = "";
		int keyIdx = addFull.indexOf(key);
		if (keyIdx > -1) {
			int keylen = key.length();
			streetPre = addFull.substring(0, keyIdx + keylen);
			if (streetPre.equals(key)) {
				streetPre = "";
				streetPost = addFull;
				keyIdx = 0;
			} else {
				streetPost = addFull.substring(keyIdx + keylen);
			}
		}
		res.put("streetPre", streetPre);
		res.put("streetPost", streetPost);
		res.put("keyIdx", keyIdx);
		return res;
	}

	public JSONObject setPlaceAndLandMark(boolean streetInDb, boolean placeInDb, boolean placeWithkey,
			boolean streetWithkey, String townXiangPost) {
		JSONObject res = new JSONObject();
		String place = "";
		String landMark = "";
		String street = "";
		String streetPost = "";
		// 在库中找到道路名，在库中找到place
		if (streetInDb && placeInDb) {
			landMark = townXiangPost;
		}
		// 在库中找到道路名；在库中没找到place但存在place关键字
		if (streetInDb && (!placeInDb) && placeWithkey) {
			place = townXiangPost;
		}
		// 在库中找到道路名，没找到place且无place关键字
		if (streetInDb && (!placeInDb) && (!placeWithkey)) {
			landMark = townXiangPost;
		}
		// 在库中没有找到道路名且存在道路名关键字；在库中找到place
		if ((!streetInDb) && streetWithkey && placeInDb) {
			landMark = townXiangPost;
		}
		// 在库中没有找到道路名且存在道路名关键字；在库中没有找到place且存在place关键字
		if ((!streetInDb) && streetWithkey && (!placeInDb) && placeWithkey) {
			place = townXiangPost;
		}
		// 在库中没有找到道路名且存在道路名关键字；在库中没有找到place且没有place关键字
		if ((!streetInDb) && streetWithkey && (!placeInDb) && (!placeWithkey)) {
			street = townXiangPost;
		}
		// 在库中没有找到道路名且不存在道路名关键字；在库中找到PLACE
		if ((!streetInDb) && (!streetWithkey) && placeInDb) {
			landMark = townXiangPost;
		}
		// 在库中没有找到道路名且不存在道路名关键字；在库中没有找到place但存在place关键字
		if ((!streetInDb) && (!streetWithkey) && (!placeInDb) && placeWithkey) {
			place = townXiangPost;
		}
		// 在库中没有找到道路名且不存在道路名关键字；在库中没有找到place且不存在place关键字
		if ((!streetInDb) && (!streetWithkey) && (!placeInDb) && (!placeWithkey)) {
			streetPost = townXiangPost;
		}

		res.put("place", place);
		res.put("landMark", landMark);
		res.put("street", street);
		res.put("streetPost", streetPost);
		return res;
	}
	
	public JSONObject doSpecially(String prefix, String houseNum, String type, String subNum, String surfix, String estab, String building, String unit, String floor, String roomNum, String addOns){
		JSONObject res = new JSONObject();
	    //A、    如果拆分结果中前缀PREFIX以数字（0~9）开头，则需要把“PREFIX、HOUSENUM、TYPE、SUBNUM”合并，然后从左到右把数字串放在“HOUSENUM”，其余部分放到“SUBNUM”
	    String NUM_REGEX = "^([０-９])+";
		Pattern pattern1 = Pattern.compile(NUM_REGEX);
		Matcher numMatch = pattern1.matcher(prefix);
		if (numMatch.find()){
			String _tmpStr = String.format("%s%s%s%s", prefix, houseNum, type, subNum);
			Matcher _matcher1 = pattern1.matcher(_tmpStr);
			if (_matcher1.find()){
				houseNum = _matcher1.group();
				subNum = _tmpStr.substring(houseNum.length());
				prefix = "";
				type = "";
			}
		}
		// 如果拆分结果中前缀以方位词+数字（方位词+0~9）开头，则需要把“PREFIX、HOUSENUM、TYPE、SUBNUM”合并，需要把方位词放置“PREFIX”，从左到右把数字串放在“HOUSENUM”，其余部分放到“SUBNUM”
		// 注：方位词：东、南、西、北、东南、西南、东北、西北
		String DIRECTION_NUM_REGEX = "^((东南)|(西南)|(东北)|(西北)|东|南|西|北)([０-９]+)";
		Pattern pattern2 = Pattern.compile(DIRECTION_NUM_REGEX);
		Matcher directNumMatch = pattern2.matcher(prefix);
		if (directNumMatch.find()){
			String _direct = directNumMatch.group(1);
			String _tmpStr = String.format("%s%s%s%", prefix, houseNum, type, subNum).substring(_direct.length());
			prefix = _direct;
			Matcher numMatcher = pattern1.matcher(_tmpStr);
			if (numMatcher.find()){
				houseNum = numMatcher.group();
			    subNum = _tmpStr.substring(houseNum.length());
			    type = "";
			}
		}
		//B、对于拆分结果中前缀PREFIX不以数字（0~9）且不以方位词+数字（方位词+0~9）开头，且前缀存放内容不等于“SC_POINT_ADDRCK”TYPE =3的内容，且附属设施ESTAB无内容的记录，则把前缀及其后面所有内容合并放到附加信息ADDONS字段中。
		if( (!numMatch.find()) && (!directNumMatch.find()) && StringUtils.isNotEmpty(prefix) && (!type3Key.contains(prefix)) && StringUtils.isEmpty(estab)){
			addOns = String.format("%s%s%s%s%s%s%s%s%s%s%s", prefix, houseNum, type, subNum, surfix, estab, building, floor, unit, roomNum, addOns);
			prefix = "";
			houseNum = "";
			type = "";
			subNum = "";
			surfix = "";
			estab = "";
			building = "";
			floor = "";
			unit = "";
			roomNum = "";
		}
		//C、 对于程序拆分结果TYPE字段为“号”，SUBNUM、SURFIX、ESTAB、BUILDING为空，FLOOR 字段为“楼”字的记录，需要把HOUSENUM、TYPE、FLOOR字段合并放到BUILDING字段中。
		if ("号".equals(type) && "楼".equals(floor) && StringUtils.isEmpty(subNum) && StringUtils.isEmpty(surfix) && StringUtils.isEmpty(estab) && StringUtils.isEmpty(building)){
			building = String.format("%s%s%s", houseNum, type, floor);
			houseNum = "";
			type = "";
			floor = "";
		}
		// 对于程序拆分结果TYPE字段为“号”，SUBNUM、SURFIX、ESTAB、BUILDING、FLOOR为空，UNIT字段为以“楼”开头的记录，需要把HOUSENUM、TYPE、及“楼”字合并放到BUILDING字段中，并删除UNIT字段开头的“楼”字
		if ("号".equals(type) && StringUtils.isEmpty(subNum) && StringUtils.isEmpty(surfix) && StringUtils.isEmpty(estab) && StringUtils.isEmpty(building) && StringUtils.isEmpty(floor) && StringUtils.isNotEmpty(unit) && unit.startsWith("楼")){
			building = String.format("%s%s楼", houseNum, type);
			unit = unit.substring(1);
			houseNum = "";
			type = "";
		}
		//如果UNIT字段以“楼”字开头且UNIT以门结尾，ROOM字段为空且ADDONS字段为“市”，则把UNIT字段（删除开头的“楼”字的内容）+“市”放到ROOM字段，并清空UNIT、ADDONS字段。
		if (StringUtils.isNotEmpty(unit) && unit.startsWith("楼") && unit.endsWith("门") && StringUtils.isEmpty(roomNum) && "市".equals(addOns)){
			roomNum = String.format("%s市", unit.substring(1));
			unit = "";
			addOns = "";
		}
		//如果UNIT字段不以“楼”字开头，且UNIT以门结尾，ROOM字段为空且ADDONS字段为“市”，则把UNIT字段+“市”放到ROOM字段，并清空UNIT、ADDONS字段。
		if (StringUtils.isNotEmpty(unit) && !unit.startsWith("楼") && unit.endsWith("门") && StringUtils.isEmpty(roomNum) && "市".equals(addOns)){
			roomNum = String.format("%s市", unit);
			unit = "";
			addOns = "";
		}
		// D、 类型后仅为数字或仅为数据和符号，将此部分放入子号。
		// 备注：符号指的是“~”、“/”、 “-”（中横杆）
		String _strAfterType = String.format("%s%s%s%s%s%s%s%s", subNum, surfix, estab, building, floor, unit, roomNum, addOns);
		String SIGNAL_NUM_REGEX = "^[-~～/／－0-9０-９]+$";
		Pattern patt = Pattern.compile(SIGNAL_NUM_REGEX);
		Matcher match = patt.matcher(_strAfterType);
		if (match.find()){
			subNum = _strAfterType;
			surfix = "";
			estab = "";
			building = "";
			floor = "";
			unit = "";
			roomNum = "";
			addOns = "";
		}
		res.put("prefix", prefix);
		res.put("houseNum", houseNum);
		res.put("type", type);
		res.put("subNum", subNum);
		res.put("surfix", surfix);
		res.put("estab", estab);
		res.put("building", building);
		res.put("unit", unit);
		res.put("floor", floor);
		res.put("roomNum", roomNum);
		res.put("addOns", addOns);
		return res;
	}

	public static void main(String[] args) {
//		List<String> test = Arrays.asList("1", "2", "3");
//		System.out.println(StringUtils.join(test, "|"));
//		long adminId = 32468L;
//		String adminCode = Long.toString(adminId);
//		System.out.println(adminCode);
//		System.out.println(adminCode.substring(0, 2));
//		String prefix = "１２３－５";
//		String houseNum = "";
//		String type = "";
//		String subNum = "";
//	    String NUM_REGEX = "^([０-９])+";
//		Pattern pattern = Pattern.compile(NUM_REGEX);
//		Matcher matcher = pattern.matcher(prefix);
//		if (matcher.find()){
//			String _tmpStr = String.format("%s%s%s%s", prefix, houseNum, type, subNum);
//			Matcher _matcher = pattern.matcher(_tmpStr);
//			if (_matcher.find()){
//				houseNum = _matcher.group();
//				subNum = _tmpStr.substring(houseNum.length());
//				prefix = "";
//				type = "";
//			}
//		}
//		System.out.println(houseNum );
//		System.out.println( subNum);
//		ArrayList<String> _adminList = new ArrayList<String>(Arrays.asList("Buenos Aires", "Córdoba", "La Plata"));
//		String _pro = _adminList.get(0);
//		System.out.println(_pro);
//		String test = "－４５０号";
//		Pattern pat = Pattern.compile("([０-９0-9])+");
//		Matcher mat = pat.matcher(test);
//		if (mat.find()){
//			String num = mat.group();
//			int numIdx = test.indexOf(num);
//			System.out.println(mat.group());
//		}
		List<String> test = Arrays.asList("1", "2", "3");
	}

}
