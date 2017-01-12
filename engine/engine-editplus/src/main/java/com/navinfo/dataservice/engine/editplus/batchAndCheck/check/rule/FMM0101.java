package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.api.metadata.model.ScPointSpecKindcodeNewObj;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAttraction;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChildren;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiHotel;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.CheckUtil;

/**
 * FM-M01-01	中文别名作业	DHM
 * 检查条件：
 * 新增POI对象、修改POI对象且修改内容为改名称或改分类;
 * 检查原则：
 * (1)POI的官方标准中文名称仅为全英文或仅为全阿拉伯数字或仅为英文和阿拉伯数字的组合时，字母和数字不区分全半角和大小写，不检查；
 * (5)如果该POI官方标准化中文名称中包含3个及以上连续中文数字(包含零，O，一，二、三、四、五、六、七、八、九、十)，则报log：别名需作业！
 * (2)如果poi的分类为停车场230210，且父分类为230103,230126,160105其中之一，且该POI的官方标准化名称中包含其父的官方标准化名称，则报log：别名需作业！
 * (3)如果poi的分类为风景名胜分类180400，且IX_POI_ATTRACTION.SIGHT_LEVEL in (3A,4A,5A),且子设施分类与父分类相同，
 * 且子的官方标准化名称包含父的官方标准化名称，则该poi和子poi均报log：别名需作业！
 * (4)如果poi的分类为“110101中餐馆(含饺子馆),160207科技馆,160209文工团、歌舞团、艺术团,180105高尔夫球场,190301专业性团体,
 * 180106高尔夫练习场,190302行业性团体,160101幼儿园｜托儿所,180101羽毛球场,160102小学,180102网球场,160103中学,160203图书馆,
 * 160109驾校,170100其他医疗机构,160104中专｜职高技校,170106卫生院及社区医疗,160100其他教育机构,160106教学楼,200101会议中心、展览中心,
 * 160107院｜系，160108学校报名处,240100科研机构及附属机构,160204档案馆,120201小区,160208文化活动中心,120202住宅楼”，
 * 且父分类为大学160105且父的状态为无变化，则该POI与父POI均报log：别名需作业！
 * (6)如果poi分类等于SC_POINT_SPEC_KINDCODE_NEW.poi_kind，且SC_POINT_SPEC_KINDCODE_NEW中type=2，且满足以下条件①和②或①③，则报log：
 *  ①如果ix_poi_hotel.rating不为空且等于SC_POINT_SPEC_KINDCODE_NEW.rating，如果ix_poi_hotel.rating为空，则不判断rating；
 *  ②如果SC_POINT_SPEC_KINDCODE_NEW.TOPCITY=1,且POI的region_id等于ad_admin.region_id,且ad_admin.admin_id截取长度后
 *  等于SC_POINT_DEEP_PLANAREA.admin_code；(截取长度后如果在(11,12,31,50),则截取2位，否则截取4位再判断)
 *  ③如果SC_POINT_SPEC_KINDCODE_NEW.TOPCITY=0；
 * (7)如果(6)报log，且该POI包含子POI，子设施不区分新增修改，子设施分类与父分类相同，且子的官方标准化名称包含父的官方标准化名称，
 * 则子POI也报log；别名需作业！如果(6)对应的POI的分类是160105大学，则对应的子的分类为“110101中餐馆(含饺子馆),160207科技馆,230210停车场,
 * 160209文工团、歌舞团、艺术团,180105高尔夫球场,190301专业性团体,180106高尔夫练习场,190302行业性团体,180210剧场、戏院、音乐厅,
 * 160105高等教育,180100体育场馆,160101幼儿园｜托儿所,180101羽毛球场,160102小学,180102网球场,160103中学,160203图书馆,160109驾校,
 * 170100其他医疗机构,160104中专｜职高技校,170106卫生院及社区医疗,160100其他教育机构,180306广场,160106教学楼,200101会议中心、展览中心,
 * 160107院｜系,160206美术馆,160108学校报名处,160205博物馆、纪念馆、展览馆、陈列馆,240100科研机构及附属机构,160204档案馆,120201小区,
 * 160208文化活动中心,120202住宅楼""直接报log：别名需作业！
 * @author zhangxiaoyi
 */
public class FMM0101 extends BasicCheckRule {
	private Map<Long, Long> parentMap=new HashMap<Long, Long>();
	private Map<Long, Long> adminMap=new HashMap<Long, Long>();
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			if(!isCheck(poiObj)){return;}
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String officeNameStr=getOfficeStandardCHNameStr(poiObj);
			if(officeNameStr==null){return;}
			String officeNameBstr=CheckUtil.strQ2B(officeNameStr);
			//(1)POI的官方标准中文名称仅为全英文或仅为全阿拉伯数字或仅为英文和阿拉伯数字的组合时，字母和数字不区分全半角和大小写，不检查；
			boolean onlyDigitLetter=true;
			for(char subchar:officeNameBstr.toCharArray()){
				String substr=String.valueOf(subchar);
				if(!CheckUtil.isDigit(substr)&&!CheckUtil.isLetter(substr)){
					onlyDigitLetter=false;
					break;
				}
			}
			if(onlyDigitLetter){return;}
			//(5)如果该POI官方标准化中文名称中包含3个及以上连续中文数字(包含零，O，一，二、三、四、五、六、七、八、九、十)，则报log：别名需作业！
			Pattern p = Pattern.compile(".*[零一二三四五六七八九十０]{3,}");
			Matcher m = p.matcher(officeNameStr);
			if (m.matches()) {
				setCheckResult(poi.getGeometry(),poiObj,poi.getMeshId(),null);
				return;
			}
			//(2)如果poi的分类为停车场230210，且父分类为230103,230126,160105其中之一，且该POI的官方标准化名称中包含其父的官方标准化名称，则报log：别名需作业！
			String kindCode=poi.getKindCode();
			IxPoiObj parentObj=getParentObj(poi.getPid());
			if(kindCode.equals("230210")&&parentObj!=null){
				IxPoi parentPoi=(IxPoi)parentObj.getMainrow();
				String parentKind=parentPoi.getKindCode();
				if(parentKind.equals("230103")||parentKind.equals("230126")||parentKind.equals("160105")){
					String parentOfficeNameStr=getOfficeStandardCHNameStr(parentObj);
					if(parentOfficeNameStr!=null&&officeNameStr.contains(parentOfficeNameStr)){
						setCheckResult(poi.getGeometry(),poiObj,poi.getMeshId(),null);
						return;
					}
				}
			}
			// * (3)如果poi的分类为风景名胜分类180400，且IX_POI_ATTRACTION.SIGHT_LEVEL in (3A,4A,5A),且子设施分类与父分类相同，
			//* 且子的官方标准化名称包含父的官方标准化名称，则该poi和子poi均报log：别名需作业！
			if(kindCode.equals("180400")&&parentObj!=null){
				List<IxPoiAttraction> attractions = poiObj.getIxPoiAttractions();
				if(attractions!=null&&attractions.size()>0){
					for(IxPoiAttraction attractionTmp:attractions){
						int sightLevel=attractionTmp.getSightLevel();
						IxPoi parentPoi=(IxPoi)parentObj.getMainrow();
						String parentKind=parentPoi.getKindCode();
						if((sightLevel==3||sightLevel==4||sightLevel==5)&&parentKind.equals(kindCode)){
							String parentOfficeNameStr=getOfficeStandardCHNameStr(parentObj);
							if(parentOfficeNameStr!=null&&officeNameStr.contains(parentOfficeNameStr)){
								setCheckResult(poi.getGeometry(),poiObj,poi.getMeshId(),null);
								setCheckResult(parentPoi.getGeometry(),parentObj,parentPoi.getMeshId(),null);
								return;
							}
						}
					}
				}
			}
//		 * (4)如果poi的分类为“110101中餐馆(含饺子馆),160207科技馆,160209文工团、歌舞团、艺术团,180105高尔夫球场,190301专业性团体,
//		 * 180106高尔夫练习场,190302行业性团体,160101幼儿园｜托儿所,180101羽毛球场,160102小学,180102网球场,160103中学,160203图书馆,
//		 * 160109驾校,170100其他医疗机构,160104中专｜职高技校,170106卫生院及社区医疗,160100其他教育机构,160106教学楼,200101会议中心、展览中心,
//		 * 160107院｜系，160108学校报名处,240100科研机构及附属机构,160204档案馆,120201小区,160208文化活动中心,120202住宅楼”，
//		 * 且父分类为大学160105且父的状态为无变化，则该POI与父POI均报log：别名需作业！
			String[] kindList1=new String[]{"110101","160207","160209","180105","190301","180106","190302","160101","180101",
					"160102","180102","160103","160203","160109","170100","160104","170106","160100","160106","200101",
					"160107","160108","240100","160204","120201","160208","120202"};
			List<String> kindList=Arrays.asList(kindList1);
			if(kindList.contains(kindCode)&&parentObj!=null){
				IxPoi parentPoi=(IxPoi)parentObj.getMainrow();
				String parentKind=parentPoi.getKindCode();
				if(parentKind.equals("160105")&&!parentObj.isChanged()){
					setCheckResult(poi.getGeometry(),poiObj,poi.getMeshId(),null);
					setCheckResult(parentPoi.getGeometry(),parentObj,parentPoi.getMeshId(),null);
					return;
				}
			}
//			* (6)如果poi分类等于SC_POINT_SPEC_KINDCODE_NEW.poi_kind，且SC_POINT_SPEC_KINDCODE_NEW中type=2，且满足以下条件①和②或①③，则报log：
//			 *  ①如果ix_poi_hotel.rating不为空且等于SC_POINT_SPEC_KINDCODE_NEW.rating，如果ix_poi_hotel.rating为空，则不判断rating；
//			 *  ②如果SC_POINT_SPEC_KINDCODE_NEW.TOPCITY=1,且POI的region_id等于ad_admin.region_id,且ad_admin.admin_id截取长度后
//			 *  等于SC_POINT_DEEP_PLANAREA.admin_code；(截取长度后如果在(11,12,31,50),则截取2位，否则截取4位再判断)
//			 *  ③如果SC_POINT_SPEC_KINDCODE_NEW.TOPCITY=0；
			MetadataApi metadataApi=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
			Map<String, ScPointSpecKindcodeNewObj> newList=metadataApi.ScPointSpecKindcodeNewType2();
			//是否满足条件①如果ix_poi_hotel.rating不为空且等于SC_POINT_SPEC_KINDCODE_NEW.rating，如果ix_poi_hotel.rating为空，则不判断rating；
			if(!newList.keySet().contains(kindCode)){return;}
			ScPointSpecKindcodeNewObj metaObj=newList.get(kindCode);
			List<IxPoiHotel> hotels = poiObj.getIxPoiHotels();
			boolean rightRating=false;
			if(hotels!=null&&hotels.size()>0){
				for(IxPoiHotel hotel:hotels){
					int rating=hotel.getRating();
					if(rating==metaObj.getRating()){rightRating=true;break;}
				}
				if(!rightRating){return;}
			}
			boolean log6=false;
			if(metaObj.getTopcity()==0){
//				 *  ③如果SC_POINT_SPEC_KINDCODE_NEW.TOPCITY=0；
				setCheckResult(poi.getGeometry(),poiObj,poi.getMeshId(),null);
				log6=true;
			}else{
//				 *  ②如果SC_POINT_SPEC_KINDCODE_NEW.TOPCITY=1,且POI的region_id等于ad_admin.region_id,且ad_admin.admin_id截取长度后
//				 *  等于SC_POINT_DEEP_PLANAREA.admin_code；(截取长度后如果在(11,12,31,50),则截取2位，否则截取4位再判断)
				if(!adminMap.containsKey(poi.getPid())){return;}
				Long adminId = adminMap.get(poi.getPid());
				String adminStr=adminId.toString();
				String adminSub=adminStr.substring(0, 4);
				if(adminStr.startsWith("11")||adminStr.startsWith("12")||adminStr.startsWith("31")
						||adminStr.startsWith("50")){
					adminSub=adminStr.substring(0, 2);
				}
				List<String> adminList=metadataApi.getDeepAdminCodeList();
				if(adminList.contains(adminSub)){
					setCheckResult(poi.getGeometry(),poiObj,poi.getMeshId(),null);
					log6=true;
				}
			}
			if(!log6){return;}
//			 * (7)如果(6)报log，且该POI包含子POI，子设施不区分新增修改，子设施分类与父分类相同，且子的官方标准化名称包含父的官方标准化名称，
//			 * 则子POI也报log；别名需作业！如果(6)对应的POI的分类是160105大学，则对应的子的分类为“110101中餐馆(含饺子馆),160207科技馆,230210停车场,
//			 * 160209文工团、歌舞团、艺术团,180105高尔夫球场,190301专业性团体,180106高尔夫练习场,190302行业性团体,180210剧场、戏院、音乐厅,
//			 * 160105高等教育,180100体育场馆,160101幼儿园｜托儿所,180101羽毛球场,160102小学,180102网球场,160103中学,160203图书馆,160109驾校,
//			 * 170100其他医疗机构,160104中专｜职高技校,170106卫生院及社区医疗,160100其他教育机构,180306广场,160106教学楼,200101会议中心、展览中心,
//			 * 160107院｜系,160206美术馆,160108学校报名处,160205博物馆、纪念馆、展览馆、陈列馆,240100科研机构及附属机构,160204档案馆,120201小区,
//			 * 160208文化活动中心,120202住宅楼""直接报log：别名需作业！
			List<IxPoiChildren> childList = poiObj.getIxPoiChildrens();
			if(childList==null||childList.size()==0){return;}
			Map<Long, BasicObj> poiMap = myReferDataMap.get(ObjectName.IX_POI);
			for(IxPoiChildren childTmp:childList){				
				if(!poiMap.containsKey(childTmp.getChildPoiPid())){continue;}
				IxPoiObj childObj = (IxPoiObj) poiMap.get(childTmp.getChildPoiPid());
				IxPoi childPoi=(IxPoi) childObj.getMainrow();
				String childKind=childPoi.getKindCode();
				if(childKind.equals(kindCode)){
					String childOfficeNameStr=getOfficeStandardCHNameStr(childObj);
					if(childOfficeNameStr!=null&&childOfficeNameStr.contains(officeNameStr)){
						setCheckResult(childPoi.getGeometry(),childObj,childPoi.getMeshId(),null);
						return;
					}
				}
				if(kindCode.equals("160105")&&kindList.contains(childKind)){
					setCheckResult(childPoi.getGeometry(),childObj,childPoi.getMeshId(),null);
					return;
				}
			}
		}
	}
	/**
	 * 获取官方标准中文名的 名称 name
	 * @param poiObj
	 * @return
	 */
	private String getOfficeStandardCHNameStr(IxPoiObj poiObj){
		IxPoiName office = poiObj.getOfficeStandardCHName();
		if(office==null){return null;}
		String officeNameStr=office.getName();
		if(officeNameStr!=null&&!officeNameStr.isEmpty()){return officeNameStr;}
		return null;
	}
	/**
	 * 获取父poi对象
	 * @param pid
	 * @return
	 */
	private IxPoiObj getParentObj(Long pid){
		if(!parentMap.containsKey(pid)){return null;}
		Long parentPid=parentMap.get(pid);
		Map<Long, BasicObj> poiMap = myReferDataMap.get(ObjectName.IX_POI);
		if(!poiMap.containsKey(parentPid)){return null;}
		IxPoiObj parentObj = (IxPoiObj) poiMap.get(parentPid);
		return parentObj;
	}
	
	/**
	 * 以下条件其中之一满足时，需要进行检查：
	 *  (1)存在IX_POI_NAME新增；
	 *  (2)存在IX_POI_NAME修改或修改分类存在；
	 * @param poiObj
	 * @return true满足检查条件，false不满足检查条件
	 * @throws Exception 
	 */
	private boolean isCheck(IxPoiObj poiObj) throws Exception{
		IxPoi poi=(IxPoi) poiObj.getMainrow();
		if(poi.hisOldValueContains(IxPoi.KIND_CODE)){
			String newKindCode=poi.getKindCode();
			String oldKindCode=(String) poi.getHisOldValue(IxPoi.KIND_CODE);
			if(!newKindCode.equals(oldKindCode)){return true;}
		}
		//(1)存在IX_POI_NAME的新增；(2)存在IX_POI_NAME的修改；
		List<IxPoiName> names = poiObj.getIxPoiNames();
		for (IxPoiName br:names){
			if(br.getHisOpType().equals(OperationType.INSERT)){return true;}
			if(br.getHisOpType().equals(OperationType.UPDATE) && br.hisOldValueContains(IxPoiName.NAME)){
				String oldName=(String) br.getHisOldValue(IxPoiName.NAME);
				String newName=br.getName();
				if(!newName.equals(oldName)){
					return true;
				}}
		}
		return false;
	}
	
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
		Set<Long> pidList=new HashSet<Long>();
		for(BasicObj obj:batchDataList){
			pidList.add(obj.objPid());
		}
		parentMap = IxPoiSelector.getParentPidsByChildrenPids(getCheckRuleCommand().getConn(), pidList);
		Set<String> referSubrow=new HashSet<String>();
		referSubrow.add("IX_POI_NAME");
		Map<Long, BasicObj> referObjs = getCheckRuleCommand().loadReferObjs(parentMap.values(), ObjectName.IX_POI, referSubrow, false);
		
		adminMap = IxPoiSelector.getAdminIdByPids(getCheckRuleCommand().getConn(), pidList);
		Set<Long> childPidList=new HashSet<Long>();
		for(BasicObj obj:batchDataList){
			IxPoiObj poiObj = (IxPoiObj) obj;
			List<IxPoiChildren> childList = poiObj.getIxPoiChildrens();
			if(childList==null||childList.size()==0){continue;}
			for(IxPoiChildren childTmp:childList){
				childPidList.add(childTmp.getChildPoiPid());
			}
		}
		Map<Long, BasicObj> referChildObjs = getCheckRuleCommand().loadReferObjs(childPidList, ObjectName.IX_POI, referSubrow, false);
		if(referObjs==null||referObjs.isEmpty()){
			referObjs=referChildObjs;
		}else if(referChildObjs!=null&&!referChildObjs.isEmpty()){
			referObjs.putAll(referChildObjs);}
		myReferDataMap.put(ObjectName.IX_POI, referObjs);		
	}

}