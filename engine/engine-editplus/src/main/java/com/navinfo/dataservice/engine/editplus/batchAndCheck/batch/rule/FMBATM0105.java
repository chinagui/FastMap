package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.util.Collection;
import java.util.Map;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
/**
 * 检查条件：
 * (1)非重要分类的POI数据：即不满足条件a也不满足条件b即为非重要分类。
 *  a,IX_POI.KIND_CODE=SC_POINT_SPEC_KINDCODE_NEW.POI_KIND 且SC_POINT_SPEC_KINDCODE_NEW.type=8 且SC_POINT_SPEC_KINDCODE_NEW.category=1；
 *  b,IX_POI.KIND_CODE=SC_POINT_SPEC_KINDCODE_NEW.POI_KIND 且SC_POINT_SPEC_KINDCODE_NEW.type=8 且SC_POINT_SPEC_KINDCODE_NEWcategory=3且IX_POI.CHAIN=SC_POINT_SPEC_KINDCODE_NEW.CHAIN；
 * (2)POI对象新增地址或修改地址的POI数据；
 * (3)简单地址POI数据
 *   IX_POI_ADDRESS中文记录中(STREET街巷名+HOUSENUM门牌号+TYPE类型)三个字段组合非空，其余名称字段PROVINCE,CITY,COUNTY,TOWN,PLACE,LANDMARK,PREFIX,SUBMUN,SURFIX,ESTAB,BUILDING, FLOOR,UNIT,ROOM,ADDONS均为空；
 * (4)IX_POI_ADDRESS.TYPE为空，或IX_POI_ADDRESS.TYPE不为空，且IX_POI_ADDRESS.TYPE的值在SC_POINT_ENGKEYWORDS.type=1对应的SC_POINT_ENGKEYWORDS.chikeywords中存在
 * 批处理原则：
 * 同时满足以上(1)(2)(3)(4)条件，则进行如下批处理翻译：
 * 仅翻译中文地址的类型、门牌号和+街巷名，存放在FULLNAME字段中，存放格式：类型翻译后+半角空格+门牌号翻译后+半角空格+街巷名翻译后，翻译首尾不能有空格，不能出现连续空格,且英文地址长度小于等于50个字符(包含空格)(翻译的英文地址长度大于50个字符(包含空格),
 *  则更新英文记录也不新增英文记录)
 * (i)若存在英文地址记录，则直接将翻译的英文更新英文记录IX_POI_ADDRESS.FULLNAME字段中；
 * (ii)若不存在英文记录，则新增一条英文记录，NAME_ID申请赋值，POI_PID赋值IX_POI.PID，FULLNAME存放翻译后的英文，NAME_GROUPID赋值与中文地址的NAME_GROUPID相同，LANG_CODE=ENG或POR(大陆英文赋值ENG，港澳英文赋值POR)；

 * a,类型翻译：    若IX_POI_ADDRESS.TYPE=SC_POINT_ENGKEYWORDS.chikeywords且SC_POINT_ENGKEYWORDS.type=1，则翻译后的类型英文为SC_POINT_ENGKEYWORDS.engkeywords的值，英文地址类型名以No.结尾时，且门牌号为阿拉伯数字时，
 *   类型与门牌号之间不应有空格；【举例：type=３弄，翻译后为：Lane 3；type=３号，翻译后为：No.3】
 * b,门牌号翻译：改为半角，原值翻译；【举例：门牌号=３,翻译后门牌号为：3】
 * c,街巷名翻译：
 *  通过IX_POI_ADDRESS.STREET与道路名RD_NAME表中LANG_CODE='CHI'(港澳数据为CHT)对应的name进行关联，
 *  若关联一条中文记录，则通过RD_NAME.name_groupid查询匹配的英文记录，则将英文RD_NAME.NAME赋值给IX_POI_ADDRESS.STREET；
 *  若关联多条中文记录，则通过IX_POI_ADDRESS.STREET_PHONETIC与中文对应的RD_NAME.NAME_PHONEETIC关联，如果拼音一致且只有一条，则将这条中文对应的英文name赋值给IX_POI_ADDRESS.STREET；
 *  如果拼音一致且多条记录，则取RD_NAME.SRC_FLAG=1的中文对应的英文name赋值给IX_POI_ADDRESS.STREET；若没有SRC_FLAG=1的，则取NAME_GROUPID最小的中文对应的英文name赋值给IX_POI_ADDRESS.STREET；
 *  如果拼音不一致，则取取NAME_GROUPID最小的中文对应的英文name赋值给IX_POI_ADDRESS.STREET；如果找不到匹配的英文记录，则不更新取IX_POI_ADDRESS.STREET，也不新增记录；

 * 【type=弄，HOUSENUM=３，STREET=望京路，翻译后英文记录fullname为：Lane 3 Wangjing Rd；
 * type=号，HOUSENUM=３，STREET=望京路，翻译后英文记录fullname为：No.3 Wangjing Rd】

 * @author jch
 */
public class FMBATM0105 extends BasicBatchRule {

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void runBatch(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			if(isBatch(poiObj)){
				IxPoiAddress chiAddr=poiObj.getChiAddress();
				IxPoiAddress engAddr=poiObj.getENGAddress(chiAddr.getNameGroupid());
				String fullName=convertAddr(poiObj,chiAddr);
				if (engAddr!=null){
					engAddr.setFullname(fullName);
				}else{
					IxPoiAddress newEngAddr=poiObj.createIxPoiAddress();
					newEngAddr.setNameGroupid(chiAddr.getNameGroupid());
					newEngAddr.setLangCode("ENG");
					newEngAddr.setFullname(fullName);
				}
			}
		}		
	}
	/**
	 * 是否满足本批处理(1)(2)(3)(4)条件
	 * @param poiObj
	 * @return
	 * @throws Exception 
	 */
	private boolean isBatch(IxPoiObj poiObj) throws Exception{
		IxPoi poi=(IxPoi) poiObj.getMainrow();
		
		//满足(2)POI对象新增中文地址或修改中文地址的POI数据
		IxPoiAddress addr=poiObj.getChiAddress();
		if (addr==null){
			return false;
		}
				
		//满足(1)非重要分类的POI数据
		MetadataApi metadataApi=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
		if (!metadataApi.judgeScPointKind(poi.getKindCode(), poi.getChain())){
			return true;
		}
		
		if ((addr.getHisOpType().equals(OperationType.INSERT)||addr.getHisOpType().equals(OperationType.UPDATE))){
			return true;
		}
		//满足(3)简单中文地址POI数据
		if (!StringUtils.isEmpty(addr.getStreet()+addr.getHousenum()+addr.getType())&&StringUtils.isEmpty(addr.getProvince()+
					addr.getCity()+addr.getTown()+addr.getPlace()+addr.getLandmark()+addr.getPrefix()+addr.getSubnum()+addr.getSurfix()+addr.getEstab()+addr.getBuilding()+addr.getFloor()
					+addr.getUnit()+addr.getRoom()+addr.getAddons())){
				return true;
			}

		//满足(4)IX_POI_ADDRESS.TYPE为空，或IX_POI_ADDRESS.TYPE不为空，且IX_POI_ADDRESS.TYPE的值在SC_POINT_ENGKEYWORDS.type=1对应的SC_POINT_ENGKEYWORDS.chikeywords中存在
		Map<String, String> typeMap1 = metadataApi.scPointEngKeyWordsType1();
		if (StringUtils.isEmpty(addr.getType())){
			return true;
		}else if(typeMap1.containsKey(addr.getType())){
			return true;
		}
		
		return false;
	}

	private String convertAddr(IxPoiObj poiObj,IxPoiAddress chiAddr) throws Exception{
		String fullName="";
		MetadataApi metadataApi=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
		//类型翻译
		Map<String, String> typeMap1 = metadataApi.scPointEngKeyWordsType1();
		String strType=typeMap1.get(chiAddr.getType());
		//门牌号翻译
		String strHouseNum= "";
		if (!StringUtils.isEmpty(chiAddr.getHousenum())) {
			strHouseNum=metadataApi.convFull2Half(chiAddr.getHousenum());	
		}
		//街巷名翻译
		//通过IX_POI_ADDRESS.STREET与道路名RD_NAME表中LANG_CODE='CHI'(港澳数据为CHT)对应的name进行关联
		String strStreet= poiObj.getRdEngName(getBatchRuleCommand().getConn(), chiAddr.getNameGroupid(),chiAddr.getPoiPid());
		if (!StringUtils.isEmpty(strType)&&strType.endsWith("No.")){
			fullName=strType+strHouseNum+" "+strStreet;
		}else{
			fullName=strHouseNum+" "+strStreet;
		}
		return fullName;
	}
   
}
