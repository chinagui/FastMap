package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.api.metadata.model.ScPointNameckObj;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.ScPointNameckUtil;
/**
 * 查询条件：本次日编存在IX_POI_NAME官方原始中文名称，新增或者修改履历，或者存在主表改分类的修改履历，且修改后的分类为190100、190101、190102、
 * 190103、190104、190105、190106、190107、190108、190109、190110、190111、190112、190500、190501、190301、190200、190201、
 * 190202、190203、190204、230103、230111、230114、230105、230126、230127、230208、230128，则执行如下批处理：
 * 批处理：官方标准化中文名称（包括简体中文和繁体中文）中包含有“SC_POINT_NAMECK”配置表中的“TYPE”=1的“PRE_KEY”字段内容（不区分大小写）,
 * 则将官方标准中文名称包含内容修改为“RESULT_KEY”字段相应的内容，同时，需要维护“官方标准化中文名称”字段对应的“拼音”NAME_PHONETIC；
 * 生成批处理履历且IX_POI_NAME.U_RECORD=3（修改）
 * 批处理：如果官方标准化中文名称（包括简体中文和繁体中文）中包含有“SC_POINT_NAMECK”配置表中的“TYPE”=1的“PRE_KEY”字段内容（不区分大小写），
 * 且包含相应的“RESULT_KEY”字段内容，且“RESULT_KEY”字段内容包含相应的“PRE_KEY”字段内容（不区分大小写），则不处理；
 * 
 * 查询条件：本次日编存在IX_POI_NAME新增或者修改履历，或者存在主表改分类的修改履历，
 * 且修改后的分类为150101且品牌chain不能是9005和9006品牌chain不能是9005和9006，则执行如下批处理：
 * 批处理：官方标准化中文名称（包括简体中文和繁体中文）中开头包含有“SC_POINT_NAMECK”配置表中的“TYPE”=1的“PRE_KEY”字段内容（不区分大小写），则将官方标准中文名称包含内容修改为“RESULT_KEY”字段相应的内容，同时，需要维护“官方标准化中文名称”字段对应的“拼音”NAME_PHONETIC；生成批处理履历且IX_POI_NAME.U_RECORD=3（修改）
 * 批处理：如果官方标准化中文名称（包括简体中文和繁体中文）中包含有“SC_POINT_NAMECK”配置表中的“TYPE”=1的，“PRE_KEY”字段内容（不区分大小写），且包含相应的“RESULT_KEY”字段内容，且“RESULT_KEY”字段内容包含相应的“PRE_KEY”字段内容（不区分大小写），则不处理
 * @author zhangxiaoyi
 */
public class FMBAT20137 extends BasicBatchRule {

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void runBatch(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			if(!isBatch(poiObj)){return;}
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String newKindCode=poi.getKindCode();
			if(newKindCode.equals("190100") ||newKindCode.equals("190100")
			||newKindCode.equals("190101")||newKindCode.equals("190102")||newKindCode.equals("190103")
			||newKindCode.equals("190104")||newKindCode.equals("190105")||newKindCode.equals("190106")
			||newKindCode.equals("190107")||newKindCode.equals("190108")||newKindCode.equals("190109")
			||newKindCode.equals("190110")||newKindCode.equals("190111")||newKindCode.equals("190112")
			||newKindCode.equals("190500")||newKindCode.equals("190501")||newKindCode.equals("190301")
			||newKindCode.equals("190200")||newKindCode.equals("190201")||newKindCode.equals("190202")
			||newKindCode.equals("190203")||newKindCode.equals("190204")||newKindCode.equals("230103")
			||newKindCode.equals("230111")||newKindCode.equals("230114")||newKindCode.equals("230105")
			||newKindCode.equals("230126")||newKindCode.equals("230127")||newKindCode.equals("230208")
			||newKindCode.equals("230128")){
				IxPoiName br=poiObj.getOfficeStandardCHName();
				if(br==null){return;}
				String name=br.getName();
				MetadataApi metadataApi=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
				
				List<ScPointNameckObj> typeD1 = metadataApi.scPointNameckTypeD1();
				List<ScPointNameckObj> keyResult=ScPointNameckUtil.matchTypeD1(name, typeD1);
				String newName=name;
				for(ScPointNameckObj metaObj:keyResult){
					newName=newName.replace(metaObj.getPreKey(), metaObj.getResultKey());
				}
				br.setName(newName);
				//批拼音
				br.setNamePhonetic(metadataApi.pyConvert(newName)[0]);
			}
			//品牌chain不能是9005和9006
			if(newKindCode.equals("150101")){
				String chain=poi.getChain();
				if(!chain.equals("9005")&&!chain.equals("9006")){
					IxPoiName br=poiObj.getOfficeStandardCHName();
					if(br==null){return;}
					String name=br.getName();
					MetadataApi metadataApi=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
					List<ScPointNameckObj> typeD1 = metadataApi.scPointNameckTypeD1();
					List<ScPointNameckObj> keyResult=ScPointNameckUtil.matchTypeD1(name, typeD1);
					String newName=name;
					for(ScPointNameckObj metaObj:keyResult){
						if(newName.startsWith(metaObj.getPreKey())){
							newName=newName.replace(metaObj.getPreKey(), metaObj.getResultKey());}
						}
					br.setName(newName);
					//批拼音
					br.setNamePhonetic(metadataApi.pyConvert(newName)[0]);
				}
			}
			
		}		
	}
	/**
	 * 本次日编存在IX_POI_NAME官方原始中文名称，新增或者修改履历，或者存在主表改分类的修改履历
	 * @param poiObj
	 * @return
	 */
	private boolean isBatch(IxPoiObj poiObj){
		IxPoi poi=(IxPoi) poiObj.getMainrow();
		if(poi.hisOldValueContains(IxPoi.KIND_CODE)){
			String oldKindCode=(String) poi.getHisOldValue(IxPoi.KIND_CODE);
			String newKindCode=poi.getKindCode();
			if(!newKindCode.equals(oldKindCode)){
				return true;
			}
		}
		//存在IX_POI_NAME官方原始中文名称，新增或者修改履历
		IxPoiName br=poiObj.getOfficeOriginCHIName();
		if(br!=null){
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

}
