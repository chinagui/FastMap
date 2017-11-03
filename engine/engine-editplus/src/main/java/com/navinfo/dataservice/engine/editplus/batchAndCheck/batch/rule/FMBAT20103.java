package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.ExcelReader;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;

/**
 * 查询条件：存在IX_POI_ADDRESS新增或者修改,且IX_POI_ADDRESS.U_RECORD!=2（删除） 
 * 批处理：
 * （1）IX_POI_ADDRESS.FULLNAME转全角； 
 * （2）FULLRNAME转拼音赋值给FULLNAME_PHONETIC；
 * （3）以上批处理生成履历；
 * 
 * @author wangdongbin
 *
 */
public class FMBAT20103 extends BasicBatchRule {
	private Map<Long,Long> pidAdminId;
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		Set<Long> pidList=new HashSet<Long>();
		for(BasicObj obj:batchDataList){
			pidList.add(obj.objPid());
		}
		pidAdminId = IxPoiSelector.getAdminIdByPids(getBatchRuleCommand().getConn(), pidList);

	}

	@Override
	public void runBatch(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		IxPoi poi = (IxPoi) obj.getMainrow();
		if (poi.getHisOpType().equals(OperationType.DELETE)) {
			return;
		}
		MetadataApi apiService=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
		IxPoiAddress chiAddress = poiObj.getChiAddress();
		if (chiAddress == null) {
			return;
		}
		String adminCode=null;
		if(pidAdminId!=null&&pidAdminId.containsKey(poi.getPid())){
			adminCode=pidAdminId.get(poi.getPid()).toString();
		}
		if (chiAddress.getHisOpType().equals(OperationType.INSERT) || chiAddress.getHisOpType().equals(OperationType.UPDATE)) {
			chiAddress.setFullname(ExcelReader.h2f(chiAddress.getFullname()));
			//生产过程中，出现了名称不为空但是转出拼音为空的数据，特加此判断进行捕获调查
			String py=apiService.pyConvert(chiAddress.getFullname(),adminCode,null);
			if(StringUtils.isNotEmpty(chiAddress.getFullname())&&StringUtils.isEmpty(py)){
				throw new Exception("名称为:“"+chiAddress.getFullname()+"”的数据，对应转出的拼音为“"+py+"”，请调查转拼音是否正确");
			}
			chiAddress.setFullnamePhonetic(py);
		} 
	}

}
