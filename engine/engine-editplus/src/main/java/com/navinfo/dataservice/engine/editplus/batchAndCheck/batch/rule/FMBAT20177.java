package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
/**
 *查询条件：
 *(1)POI对象新增且存在中文别名：
 *(2)POI对象修改且中文别名修改；
 *(3)POI对象修改且改分类且存在中文别名：
 *(4)POI对象修改且存在中文别名且未变更且不存在别名英文；
 *以上查询条件满足其中一条，则进行如下批处理：
 *(a)若同组名称中，不存在别名原始英文名称，则新增一组别名原始名称(组号赋值同中文别名、name_class=3，lang_code=ENG)，若存在；
 *(b)若同组名称中，存在别名原始英文记录，则更新别名原始英文name的值，若存在别名标准化英文记录，则删除别名标准化英文记录；
 *(c)若同组中存在别名标准英文记录，则逻辑删除别名标准英文记录；
 *(d)别名原始英文记录统一处理No.中N和o的大小写问题：将“NO.”，“nO.”，“no.”修改成“No.”
 *(注：主要多个中文别名的情况，每一组中文别名均需要处理)
 * @author gaopengrong
 */
public class FMBAT20177 extends BasicBatchRule {

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
			List<IxPoiName> names=poiObj.getAliasCHIName();
			if(names.size()==0){return;}
			MetadataApi metadataApi=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
			for(IxPoiName name:names){
				long groupId= name.getNameGroupid();
				String nameStr= name.getName();
				if(nameStr.isEmpty()){continue;}
				IxPoiName originAliasENG= poiObj.getOriginAliasENGName(groupId);
				String newOriginAliasEngStr=metadataApi.convertEng(nameStr);
				//将“NO.”，“nO.”，“no.”修改成“No.”
				newOriginAliasEngStr=newOriginAliasEngStr.replace("NO.", "No.");
				newOriginAliasEngStr=newOriginAliasEngStr.replace("nO.", "No.");
				newOriginAliasEngStr=newOriginAliasEngStr.replace("no.", "No.");
				//若同组名称中，不存在别名原始英文名称，则新增一组别名原始名称(组号赋值同中文别名、name_class=3，lang_code=ENG)
				if(originAliasENG==null){
					IxPoiName newOriginAliasENG= poiObj.createIxPoiName();
					newOriginAliasENG.setLangCode("ENG");
					newOriginAliasENG.setNameClass(3);
					newOriginAliasENG.setNameType(2);
					newOriginAliasENG.setName(newOriginAliasEngStr);
					newOriginAliasENG.setNameGroupid(groupId);
				}else{
					//若同组名称中，存在别名原始英文记录，则更新别名原始英文name的值,将“NO.”，“nO.”，“no.”修改成“No.”
					originAliasENG.setName(newOriginAliasEngStr);
				}
				//若存在别名标准化英文记录，则删除别名标准化英文记录；
				IxPoiName standardAliasENG= poiObj.getStandardAliasENGName(groupId);
				if(standardAliasENG!=null){
					poiObj.deleteSubrow(standardAliasENG);
				}
				
			}
		}		
	}
	/**
	 * (1)POI对象新增且存在中文别名：
	 * (2)POI对象修改且中文别名修改；
	 * (3)POI对象修改且改分类且存在中文别名：
	 * (4)POI对象修改且存在中文别名且未变更且不存在别名英文；
	 * @param poiObj
	 * @return
	 */
	private boolean isBatch(IxPoiObj poiObj){
		IxPoi poi=(IxPoi) poiObj.getMainrow();
		if((!poi.getOpType().equals(OperationType.INSERT))&&(!poiObj.isChanged())){return false;}
		List<IxPoiName> names = poiObj.getIxPoiNames();
		boolean existAlias = false;
		boolean isAliasNameChange = false;
		boolean existAliasEng = false;
		for(IxPoiName name:names){
			if(name.getNameClass()==3&&(name.getLangCode().equals("CHI")||name.getLangCode().equals("CHT"))){
				existAlias=true;
				if(name.hisOldValueContains(IxPoiName.NAME)){
					String oldName=(String) name.getHisOldValue(IxPoiName.NAME);
					String newName=name.getName();
					if(!newName.equals(oldName)){
						isAliasNameChange=true;
					}
				}
			}
			if(name.getNameClass()==3&&name.getLangCode()=="ENG"){
				existAliasEng=true;
			}
		}
		//(1)POI对象新增且存在中文别名：
		if(poi.getOpType().equals(OperationType.INSERT)&&existAlias){return true;}
		//(2)POI对象修改且中文别名修改；
		if(poi.isChanged()&&isAliasNameChange){return true;}
		//(3)POI对象修改且改分类且存在中文别名：
		if(poi.hisOldValueContains(IxPoi.KIND_CODE)&&existAlias){return true;}
		//(4)POI对象修改且存在中文别名且未变更且不存在别名英文；
		if(existAlias==true&&isAliasNameChange==false){return true;}
		return false;
	}

}
