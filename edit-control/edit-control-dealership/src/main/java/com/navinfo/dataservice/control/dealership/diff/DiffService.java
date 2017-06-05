package com.navinfo.dataservice.control.dealership.diff;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.navinfo.dataservice.api.edit.model.IxDealershipResult;
import com.navinfo.dataservice.api.edit.model.IxDealershipSource;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;




public class DiffService {
	
	private static Log log = LogFactory.getLog(DiffService.class.getName());
	private static Map<String,Integer> provinceRegionIdMap = new HashMap<String,Integer>();
	
//	private Properties p = null;
//	
//	public void init(String propertiesName) {
////		InputStream inputStream = this.getClass().getClassLoader()
////				.getResourceAsStream(propertiesName);
//		File f = new File(propertiesName); 
//		p = new Properties();
//		try {
//			p.load(new FileInputStream(f));
//		} catch (IOException e1) {
//			e1.printStackTrace();
//		}
//	}
//	
//	public DiffService(String propertiesName){
//		init(propertiesName);
//	}
	
	
	public static String hash(String password) throws Exception {
		MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
		byte[] passBytes = password.getBytes();
		byte[] passHash = sha256.digest(passBytes);
		StringBuffer str = new StringBuffer();
		for (byte b : passHash) {
			String hexString = Integer.toHexString(b + 128);
			str.append(hexString);
		}
		return str.toString();
	}

	public static Map<Integer,List<IxDealershipResult>> diff(List<IxDealershipSource> dealershipSources,List<IxDealershipResult> dealershipResult, String chain, Map<Integer, IxDealershipResult> dealershipResultsPreMap) throws Exception {
		log.info("Table Diff Begin");
		
		//加载cp_region_province
		ManApi manApi = (ManApi)ApplicationContextUtil.getBean("manApi");
		provinceRegionIdMap = manApi.getProvinceRegionIdMap();
		
		Map<Integer,List<IxDealershipResult>> resultMap = new HashMap<Integer,List<IxDealershipResult>>();
		List<IxDealershipResult> insertList = new ArrayList<IxDealershipResult>();
		List<IxDealershipResult> updateList = new ArrayList<IxDealershipResult>();
		List<IxDealershipResult> deleteList = new ArrayList<IxDealershipResult>();

		Map<String, Integer> dealershipNameMap = new HashMap<String, Integer>();
		Map<String, Integer> dealershipAddrMap = new HashMap<String, Integer>();
		Map<String, Integer> dealershipTelMap = new HashMap<String, Integer>();
		Map<String, Integer> dealershipPostCodeMap = new HashMap<String, Integer>();
		Map<String, Integer> dealershipKindMap = new HashMap<String, Integer>();
		Map<String, Integer> dealershipChainMap = new HashMap<String, Integer>();

		Map<String, Integer> sourceNameMap = new HashMap<String, Integer>();
		Map<String, Integer> sourceAddrMap = new HashMap<String, Integer>();
		Map<String, Integer> sourceTelMap = new HashMap<String, Integer>();		
		Map<String, Integer> sourcePostCodeMap = new HashMap<String, Integer>();
		Map<String, Integer> sourceKindMap = new HashMap<String, Integer>();
		Map<String, Integer> sourceChainMap = new HashMap<String, Integer>();

		
		for (IxDealershipResult i : dealershipResult) {
			sourceNameMap.put(i.getName().trim(), 1);
			sourceAddrMap.put(i.getAddress().trim(), 1);
			sourceTelMap.put(i.getTelephone().trim(), 1);
			sourcePostCodeMap.put(i.getPostCode().trim(), 1);
			sourceChainMap.put(i.getPostCode().trim(), 1);
			sourceKindMap.put(i.getKindCode(), 1);
		}

		//全国一览表中名称、地址、电话、邮编、分类、品牌hash:全国一览表中元素
		Map<String, IxDealershipSource> mapMatchSame = new HashMap<String, IxDealershipSource>();
		Map<Integer, String> dkeyMap = new HashMap<Integer, String>();

		Map<String, List<IxDealershipSource>> editPart1 = new HashMap<String, List<IxDealershipSource>>();
		Map<String, List<IxDealershipSource>> editPart2 = new HashMap<String, List<IxDealershipSource>>();
		Map<String, List<IxDealershipSource>> editPart3 = new HashMap<String, List<IxDealershipSource>>();
		Map<String, List<IxDealershipSource>> editPart4 = new HashMap<String, List<IxDealershipSource>>();
		
		String t;

		for (IxDealershipSource i : dealershipSources) {

			if (i.getName() != null)
				dealershipNameMap.put(i.getName().trim(), 1);
			if (i.getAddress() != null)
				dealershipAddrMap.put(i.getAddress().trim(), 1);
			if (i.getTelephone() != null)
				dealershipTelMap.put(i.getTelephone(), 1);
			if (i.getPostCode() != null)
				dealershipPostCodeMap.put(i.getPostCode(), 1);
			
			dealershipKindMap.put(i.getKindCode(), 1);
			dealershipChainMap.put(i.getChain(), 1);
			
			//名称、地址、电话、邮编、分类、品牌
			String shortName = (null == i.getNameShort()? "":i.getNameShort());
			t = hash(i.getName().trim() + i.getAddress().trim()
					+ i.getTelephone().trim() + i.getPostCode().trim()
					+ i.getKindCode().trim() + i.getChain() + shortName.trim());
			mapMatchSame.put(t, i);

			//上传一览表与全国一览表中分类、品牌和名称均相同，且地址相似，且邮编相同但电话不同或邮编不同电话相同；
			t = hash(i.getName().trim() + i.getKindCode().trim() + i.getChain().trim());
			if (editPart1.get(t) == null) {
				List<IxDealershipSource> dsList = new ArrayList<IxDealershipSource>();
				dsList.add(i);
				editPart1.put(t, dsList);
			} else {
				editPart1.get(t).add(i);
			}

			//上传一览表与全国一览表中分类、品牌、电话均相同，且地址相同但邮编不同或地址不同但邮编相同
			t = hash(i.getChain().trim() + i.getTelephone().trim() + i.getKindCode().trim());
			if (editPart2.get(t) == null) {
				List<IxDealershipSource> dsList = new ArrayList<IxDealershipSource>();
				dsList.add(i);
				editPart2.put(t, dsList);
			} else {
				editPart2.get(t).add(i);
			}

			//上传一览表与全国一览表中分类、品牌、地址、电话均相同，且名称相同但邮编不同或名称相同但电话不同
			t = hash(i.getAddress().trim() + i.getTelephone().trim() + i.getKindCode().trim() + i.getChain().trim());
			if (editPart3.get(t) == null) {
				List<IxDealershipSource> dsList = new ArrayList<IxDealershipSource>();
				dsList.add(i);
				editPart3.put(t, dsList);
			} else {
				editPart3.get(t).add(i);
			}
			
			//删除逻辑：分类和品牌均相同
			t = hash(i.getKindCode().trim() + i.getChain().trim());
			if (editPart4.get(t) == null) {
				List<IxDealershipSource> dsList = new ArrayList<IxDealershipSource>();
				dsList.add(i);
				editPart4.put(t, dsList);
			} else {
				editPart4.get(t).add(i);
			}
		}

		/**************** 一致,更新,新增  *******************/
		for (IxDealershipResult i : dealershipResult) {
			IxDealershipResult resultDpAttrDiff = new IxDealershipResult();

			boolean flag = false;
			String shortName = (null == i.getNameShort()? "":i.getNameShort());
			t = hash(i.getName().trim() + i.getAddress().trim()
					+ i.getTelephone().trim() + i.getPostCode().trim()
					+ i.getKindCode().trim() + shortName.trim());

			/**************** 新旧一致逻辑 *******************/
			if (mapMatchSame.get(t) != null) {
				IxDealershipSource j = mapMatchSame.get(t);
				if(dealershipResultsPreMap.containsKey(j.getSourceId())){
					resultDpAttrDiff = dealershipResultsPreMap.get(j.getSourceId());
					updateList.add(resultDpAttrDiff);
				}else{
					resultDpAttrDiff = new IxDealershipResult(i);
					insertList.add(resultDpAttrDiff);
				}

				resultDpAttrDiff.setDealSrcDiff(1);
				
				updateIxDealershipResultWithIxDealershipSource(resultDpAttrDiff,j);
				
				dkeyMap.put(mapMatchSame.get(t).getSourceId(), "");
				continue;
			}
			/**************** 新旧一致逻辑 *******************/

			/**************** 新版较旧版有变更逻辑 *******************/
			//上传一览表与全国一览表中分类、品牌和名称均相同，且地址相似或地址相同，且邮编相同但电话不同或邮编不同电话相同
			t = hash(i.getName().trim() + i.getKindCode().trim() + i.getChain().trim());
			if (editPart1.get(t) != null) {
				for (IxDealershipSource j : editPart1.get(t)) {
					boolean sameTel = i.getTelephone().equals(j.getTelephone());
					boolean samePostCode = false;	
					if(i.getPostCode()!=null&&j.getPoiKindCode()!=null){
						samePostCode = i.getPostCode().equals(j.getPostCode());	
					}
					if((i.getPostCode() == null || "".equals(i.getPostCode()))&&(j.getPostCode() == null || "".equals(j.getPostCode())))
						samePostCode = true;
					
					if (checkAddrSim(i, j) && ((sameTel&&!samePostCode) || (!sameTel&&samePostCode))) {
						if(dealershipResultsPreMap.containsKey(j.getSourceId())){
							resultDpAttrDiff = dealershipResultsPreMap.get(j.getSourceId());
							updateList.add(resultDpAttrDiff);
						}else{
							resultDpAttrDiff = new IxDealershipResult(i);
							insertList.add(resultDpAttrDiff);
						}
						resultDpAttrDiff.setDealSrcDiff(4);
						
						updateIxDealershipResultWithIxDealershipSource(resultDpAttrDiff,j);

						dkeyMap.put(j.getSourceId(), "");
						updateList.add(resultDpAttrDiff);
						flag = true;
						break;
					}
				}
				if (flag)
					continue;
			}
			//上传一览表与全国一览表中分类、品牌、电话均相同，且地址相同但邮编不同或地址不同但邮编相同
			t = hash(i.getChain().trim() + i.getTelephone().trim()+ i.getKindCode().trim());
			if (editPart2.get(t) != null) {
				for (IxDealershipSource j : editPart3.get(t)) {
					boolean sameAddr = i.getAddress().equals(j.getAddress());
					boolean samePostCode = false;	
					if(i.getPostCode()!=null&&j.getPoiKindCode()!=null){
						samePostCode = i.getPostCode().equals(j.getPostCode());	
					}				
					if((i.getPostCode() == null || "".equals(i.getPostCode()))&&(j.getPostCode() == null || "".equals(j.getPostCode())))
						samePostCode = true;
					
					if ((!sameAddr&&samePostCode) || (sameAddr&&!samePostCode)) {
						if(dealershipResultsPreMap.containsKey(j.getSourceId())){
							resultDpAttrDiff = dealershipResultsPreMap.get(j.getSourceId());
							updateList.add(resultDpAttrDiff);
						}else{
							resultDpAttrDiff = new IxDealershipResult(i);
							insertList.add(resultDpAttrDiff);
						}
						resultDpAttrDiff.setDealSrcDiff(4);
						
						updateIxDealershipResultWithIxDealershipSource(resultDpAttrDiff,j);

						dkeyMap.put(j.getSourceId(), "");
						updateList.add(resultDpAttrDiff);
						flag = true;
						break;
					}
				}
				if (flag)
					continue;
			}

			//上传一览表与全国一览表中分类、品牌、地址、电话均相同，且名称相同但邮编不同或名称相同但邮编不同
			t = hash(i.getAddress().trim() + i.getTelephone().trim() + i.getKindCode().trim() + i.getChain().trim());
			if (editPart3.get(t) != null) {
				for (IxDealershipSource j : editPart3.get(t)) {
					boolean sameName = (i.getName().equals(j.getTelephone())&&i.getNameShort().equals(j.getNameShort()));
					boolean samePostCode = false;					
					if(i.getPostCode()!=null&&j.getPoiKindCode()!=null){
						samePostCode = i.getPostCode().equals(j.getPostCode());	
					}				
					if((i.getPostCode() == null || "".equals(i.getPostCode()))&&(j.getPostCode() == null || "".equals(j.getPostCode())))
						samePostCode = true;

					if ((!sameName&&samePostCode) || (sameName&&!samePostCode)) {
						if(dealershipResultsPreMap.containsKey(j.getSourceId())){
							resultDpAttrDiff = dealershipResultsPreMap.get(j.getSourceId());
							updateList.add(resultDpAttrDiff);
						}else{
							resultDpAttrDiff = new IxDealershipResult(i);
							insertList.add(resultDpAttrDiff);
						}
						resultDpAttrDiff.setDealSrcDiff(4);
						updateIxDealershipResultWithIxDealershipSource(resultDpAttrDiff,j);

						dkeyMap.put(j.getSourceId(), "");
						updateList.add(resultDpAttrDiff);
						flag = true;
						break;
					}
				}
				if (flag)
					continue;
			}
			/**************** 新版较旧版有变更逻辑 *******************/

			/**************** 新增逻辑 *******************/
			boolean temp = true;
			t = hash(i.getKindCode().trim() + i.getChain().trim());
			if (editPart4.get(t) != null) {
				for (IxDealershipSource j : editPart4.get(t)) {
					boolean sameAddress = (i.getAddress().equals(j.getAddress()));
					boolean samePostCode = false;
					boolean sameTel = i.getTelephone().equals(j.getTelephone());
					
					if(i.getPostCode()!=null&&j.getPoiKindCode()!=null){
						samePostCode = i.getPostCode().equals(j.getPostCode());	
					}				
					if((i.getPostCode() == null || "".equals(i.getPostCode()))&&(j.getPostCode() == null || "".equals(j.getPostCode())))
						samePostCode = true;
					
					if ((!sameAddress&&!sameAddress&&!samePostCode)) {
						resultDpAttrDiff.setDealSrcDiff(4);
						insertList.add(resultDpAttrDiff);
						flag = true;
						break;
					}
				}
				if (flag)
					continue;
			}
			/**************** 新增逻辑 *******************/

			
			/**************** 其他逻辑 *******************/
			resultDpAttrDiff.setDealStatus(5);
			resultDpAttrDiff = new IxDealershipResult(i);
			insertList.add(resultDpAttrDiff);
			/**************** 其他逻辑 *******************/
			
			

		}
		/**************** 一致,更新,新增  *******************/

		/***************** 删除逻辑  ****************/
		for (IxDealershipSource i : dealershipSources) {
			IxDealershipResult resultDpAttrDiff = new IxDealershipResult();
			if(dealershipResultsPreMap.containsKey(i.getSourceId())){
				resultDpAttrDiff = dealershipResultsPreMap.get(i.getSourceId());
				updateList.add(resultDpAttrDiff);
			}else{
				insertList.add(resultDpAttrDiff);
			}
			resultDpAttrDiff.setChain(chain);
			if ((dkeyMap.get(i.getSourceId()) == null)) {
				/***************** 删除逻辑 ****************/
				if (((sourceNameMap.get(i.getName()) == null)
						&& (sourceAddrMap.get(i.getAddress().trim()) == null)
						&& (sourcePostCodeMap.get(i.getPostCode()) == null)
						&& (sourceTelMap.get(i.getTelephone().trim()) == null))
						&& (sourceKindMap.get(i.getKindCode().trim()) == null)) {
					resultDpAttrDiff.setDealStatus(2);
				} else
				/***************** 其他逻辑 ****************/
				{
					resultDpAttrDiff.setDealStatus(5);
				}
			}
		}
		log.info("Table Diff End");
		resultMap.put(1, insertList);
		resultMap.put(2, deleteList);
		resultMap.put(3, updateList);

		return resultMap;

	}

	/**
	 * @param resultDpAttrDiff
	 * @param j
	 */
	private static void updateIxDealershipResultWithIxDealershipSource(IxDealershipResult resultDpAttrDiff,
			IxDealershipSource i) {
		//MATCH_METHOD:IX_DEALERSHIP_SOURCE表中cfm_poi_num有值，则赋值1，否则赋值0
		resultDpAttrDiff.setMatchMethod(null == i.getCfmPoiNum()? 0:1);
		//POI_NUM_1:IX_DEALERSHIP_SOURCE表中cfm_poi_num有值，则赋值IX_DEALERSHIP_SOURCE.cfm_poi_num
		resultDpAttrDiff.setPoiNum1(i.getCfmPoiNum());
		//cfm_poi_num赋值IX_DEALERSHIP_SOURCE.cfm_poi_num
		resultDpAttrDiff.setCfmPoiNum(i.getCfmPoiNum());
		//SOURCE_ID赋值IX_POIDEALERSHIP_SOURCE.source_id
		resultDpAttrDiff.setSourceId(i.getSourceId());
		//DEAL_CFM_DATE:赋值IX_POIDEALERSHIP_SOURCE.deal_cfm_date
		resultDpAttrDiff.setDealCfmDate(i.getDealCfmDate());
		//POI_KIND_CODE:IX_DEALERSHIP_SOURCE表中cfm_poi_num有值，赋值IX_DEALERSHIP_SOURCE.POI_KIND_CODE
		//POI_CHAIN:IX_DEALERSHIP_SOURCE表中cfm_poi_num有值，赋值IX_DEALERSHIP_SOURCE.POI_CHAIN
		//POI_NAME:IX_DEALERSHIP_SOURCE表中cfm_poi_num有值，赋值IX_DEALERSHIP_SOURCE.POI_NAME
		//POI_NAME_SHORT:IX_DEALERSHIP_SOURCE表中cfm_poi_num有值，赋值IX_DEALERSHIP_SOURCE.POI_NAME_SHORT
		//POI_ADDRESS:X_DEALERSHIP_SOURCE表中cfm_poi_num有值，赋值IX_DEALERSHIP_SOURCE.POI_ADDRESS
		//POI_TEL	IX_DEALERSHIP_SOURCE表中cfm_poi_num有值，赋值IX_DEALERSHIP_SOURCE.POI_TEL
		//POI_POST_CODE	IX_DEALERSHIP_SOURCE表中cfm_poi_num有值，赋值IX_DEALERSHIP_SOURCE.POI_POST_CODE
		//POI_X_DISPLAY	IX_DEALERSHIP_SOURCE表中cfm_poi_num有值，赋值IX_DEALERSHIP_SOURCE.POI_X_DISPLAY
		//POI_Y_DISPLAY	IX_DEALERSHIP_SOURCE表中cfm_poi_num有值，赋值IX_DEALERSHIP_SOURCE.POI_Y_DISPLAY
		//POI_X_GUIDE	IX_DEALERSHIP_SOURCE表中cfm_poi_num有值，赋值IX_DEALERSHIP_SOURCE.POI_X_GUIDE
		//POI_Y_GUIDE	IX_DEALERSHIP_SOURCE表中cfm_poi_num有值，赋值IX_DEALERSHIP_SOURCE.POI_Y_GUIDE
		if(i.getCfmPoiNum()!=null){
			resultDpAttrDiff.setPoiKindCode(i.getPoiKindCode());
			resultDpAttrDiff.setPoiChain(i.getPoiChain());
			resultDpAttrDiff.setPoiName(i.getPoiName());
			resultDpAttrDiff.setPoiNameShort(i.getPoiNameShort());
			resultDpAttrDiff.setPoiAddress(i.getPoiAddress());
			resultDpAttrDiff.setPoiTel(i.getPoiTel());
			resultDpAttrDiff.setPostCode(i.getPostCode());
			resultDpAttrDiff.setPoiXDisplay(i.getPoiXDisplay());
			resultDpAttrDiff.setPoiYDisplay(i.getPoiYDisplay());
			resultDpAttrDiff.setPoiXGuide(i.getPoiXGuide());
			resultDpAttrDiff.setPoiYGuide(i.getPoiYGuide());
		}
		//GEOMETRY	赋值IX_DEALERSHIP_SOURCE.GEOMETRY
		resultDpAttrDiff.setGeometry(i.getGeometry());
		//REGION_ID	根据IX_DEALERSHIP_RESULT.PROVINCE关联cp_region_province.province,查找对应的region_id赋值；针对差分结果为删除的记录即deal_src_diff＝2，取source.province关联
		resultDpAttrDiff.setRegionId(provinceRegionIdMap.get(resultDpAttrDiff.getProvince()));

		
	}

	/**
	 * @param i
	 * @param j
	 * @return
	 */
	private static boolean checkAddrSim(IxDealershipResult s, IxDealershipSource d) {
		try {
			String s_address = s.getAddress();
			String d_address = d.getAddress();
			if(s_address.equals(d_address)){
				return true;
			}
			
			if(s_address.contains(d_address)){
				return true;
			}
			if(d_address.contains(s_address)){
				return true;
			}
			return false;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	public boolean checkAddrSim(IxDealershipResult s, IxDealershipResult d) {
		try {
			String s_address = s.getAddress();
			String d_address = d.getAddress();
			if(s_address.equals(d_address)){
				return true;
			}
			
			if(s_address.contains(d_address)){
				return true;
			}
			if(d_address.contains(s_address)){
				return true;
			}
			return false;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

}
