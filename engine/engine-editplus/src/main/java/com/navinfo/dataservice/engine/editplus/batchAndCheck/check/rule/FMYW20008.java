package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.CheckUtil;
/**
 * FM-YW-20-008	中文名称拼音格式检查	DHM	
 * 检查条件：
 * 该POI发生变更(新增或修改主子表、删除子表)；
 * 检查原则：18个地址拼音字段中若存在以下，则报出：
 * 1） 回车符检查：包含回车符的记录；
 * 2） Tab符检查：包含Tab符号的记录；
 * 3） 多个空格检查：两个及两个以上空格的记录；
 * 4） 前后空格检查：名称开始前或者结尾处包含空格的记录；
 * 提示：地址拼音中不能名存在“xx” （提示信息中的符号全部用中文名称）
 * @author gaopengrong
 *
 */
public class FMYW20008 extends BasicCheckRule {
	
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			List<IxPoiAddress> addresses = poiObj.getIxPoiAddresses();
			List<String> errorList=new ArrayList<String>();
			for(IxPoiAddress address:addresses){
				if(address.isCH()){
					String mergeAddrPhonetic = CheckUtil.getMergerAddrPhonetic(address);
					if(mergeAddrPhonetic==null||mergeAddrPhonetic.isEmpty()){continue;}
					errorList=CheckUtil.checkIllegalBlank(mergeAddrPhonetic);
					if(errorList.size()>0){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "地址拼音中不能名存在“"
								+errorList.toString().replace("[", "").replace("]", "")+"”");
						return;
					}
				}
			}			
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
	}

}
