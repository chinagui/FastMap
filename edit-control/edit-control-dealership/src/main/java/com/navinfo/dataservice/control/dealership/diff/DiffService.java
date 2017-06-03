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




public class DiffService {
	
	private static Log log = LogFactory.getLog(DiffService.class.getName());
	
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

	public static List<IxDealershipResult> diff(List<IxDealershipSource> dealershipSources,List<IxDealershipResult> dealershipResult, String chain) throws Exception {
		log.info("Table Diff Begin");
		
		List<IxDealershipResult> resultList = new ArrayList<IxDealershipResult>();
		
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
//		Map<String, String> dkeyMap = new HashMap<String, String>();

		Map<String, List<IxDealershipSource>> editPart1 = new HashMap<String, List<IxDealershipSource>>();
		Map<String, List<IxDealershipSource>> editPart2 = new HashMap<String, List<IxDealershipSource>>();
		Map<String, List<IxDealershipSource>> editPart3 = new HashMap<String, List<IxDealershipSource>>();
		
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

			//上传一览表与全国一览表中分类、品牌和名称均相同，且地址相似或地址相同，且邮编相同但电话不同或邮编不同电话相同；
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
			
		}

		/**************** 一致,更新,新增  *******************/
		for (IxDealershipResult i : dealershipResult) {
			IxDealershipResult resultDpAttrDiff = new IxDealershipResult(i);

			boolean flag = false;
			String shortName = (null == i.getNameShort()? "":i.getNameShort());
			t = hash(i.getName().trim() + i.getAddress().trim()
					+ i.getTelephone().trim() + i.getPostCode().trim()
					+ i.getKindCode().trim() + shortName.trim());

			/**************** 新旧一致逻辑 *******************/
			if (mapMatchSame.get(t) != null) {
				resultDpAttrDiff.setDealSrcDiff(1);
//				dkeyMap.put(mapMatchSame.get(t).getUuid(), "");
				resultList.add(resultDpAttrDiff);
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
					if(i.getPostCode() == null || "".equals(i.getPostCode()))
						samePostCode = true;
					if(j.getPostCode() == null || "".equals(j.getPostCode()))
						samePostCode = true;
					
					if (checkAddrSim(i, j) && ((sameTel&&!samePostCode) || (!sameTel&&samePostCode))) {
						resultDpAttrDiff.setDealSrcDiff(4);
//						dkeyMap.put(j.getUuid(), "");
						resultList.add(resultDpAttrDiff);
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
					if(i.getPostCode() == null || "".equals(i.getPostCode()))
						samePostCode = true;
					if(j.getPostCode() == null || "".equals(j.getPostCode()))
						samePostCode = true;
					
					if ((!sameAddr&&samePostCode) || (sameAddr&&!samePostCode)) {
						resultDpAttrDiff.setDealSrcDiff(4);
//						dkeyMap.put(j.getUuid(), "");
						resultList.add(resultDpAttrDiff);
						flag = true;
						break;
					}
				}
				if (flag)
					continue;
			}

			//上传一览表与全国一览表中分类、品牌、地址、电话均相同，且名称相同但邮编不同或名称相同但电话不同
			t = hash(i.getAddress().trim() + i.getTelephone().trim() + i.getKindCode().trim() + i.getChain().trim());
			if (editPart3.get(t) != null) {
				for (IxDealershipSource j : editPart3.get(t)) {
					boolean sameName = (i.getName().equals(j.getTelephone())&&i.getNameShort().equals(j.getNameShort()));
					boolean samePostCode = i.getPostCode().equals(j.getPostCode());					
					if(i.getPostCode() == null || "".equals(i.getPostCode()))
						samePostCode = true;
					if(j.getPostCode() == null || "".equals(j.getPostCode()))
						samePostCode = true;
					
					if ((!sameName&&samePostCode) || (sameName&&!samePostCode)) {
						resultDpAttrDiff.setDealSrcDiff(4);
//						dkeyMap.put(j.getUuid(), "");
						resultList.add(resultDpAttrDiff);
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

			if (i.getPostCode() != null)
				if (dealershipPostCodeMap.get(i.getPostCode().trim()) != null)
					temp = false;
			if (i.getAddress() != null)
				if (dealershipAddrMap.get(i.getAddress().trim()) != null)
					temp = false;
			if (i.getTelephone() != null)
				if (dealershipTelMap.get(i.getTelephone().trim()) != null)
					temp = false;
			
			if(i.getKindCode() != null)
				if (dealershipKindMap.get(i.getKindCode().trim()) == null)
					temp = true;
			
			if (temp) {
				resultDpAttrDiff.setDealStatus(3);;
				resultList.add(resultDpAttrDiff);
				continue;
			}
			/**************** 新增逻辑 *******************/

			
			/**************** 其他逻辑 *******************/
			resultDpAttrDiff.setDealStatus(5);
			resultList.add(resultDpAttrDiff);
			/**************** 其他逻辑 *******************/
			
			

		}
		/**************** 一致,更新,新增  *******************/

		/***************** 删除逻辑  ****************/
		for (IxDealershipSource i : dealershipSources) {
			IxDealershipResult resultDpAttrDiff = new IxDealershipResult();
			resultDpAttrDiff.setChain(chain);
//			if ((dkeyMap.get(i.getUuid()) == null)) {
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
				resultList.add(resultDpAttrDiff);
//			}
		}
		log.info("Table Diff End");
		return resultList;

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
