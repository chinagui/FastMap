package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.CheckUtil;
/**
 * FM-YW-20-122
 * 检查条件：
 *     Lifecycle=3（新增）或Lifecycle=2（更新）且name修改
 * 检查原则：
 * 1）主名称（name）只含有阿拉伯数字时，报log1；
 * 2）主名称（name）为长度为1时，报log2；
 * log1：名称只含有数字，请确认；
 * log2：名称只有1个字，请确认；
 * @author zhangxiaoyi
 */
public class FMYW20122 extends BasicCheckRule {
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			if(!isCheck(poiObj)){return;}
			IxPoiName nameObj = poiObj.getOfficeOriginCHName();
			if(nameObj==null){return;}
			String nameStr = nameObj.getName();
			if(nameStr==null||nameStr.isEmpty()){return;}
			if(nameStr.length()==1){
				setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "名称只有1个字，请确认");
			}
			String tmpStr=CheckUtil.strQ2B(nameStr);
			Pattern p = Pattern.compile("^[0123456789]+$");
			Matcher m = p.matcher(tmpStr);
			if(m.matches()){
				setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "名称只含有数字，请确认");
			}
		}
	}
	
	private boolean isCheck(IxPoiObj poiObj){
		//存在IX_POI_NAME新增或者修改履历
		IxPoiName br=poiObj.getOfficeOriginCHName();
		if(br!=null){
			if(br.getHisOpType().equals(OperationType.INSERT)){return true;}
			if(br.getHisOpType().equals(OperationType.UPDATE) && br.hisOldValueContains(IxPoiName.NAME)){
				String oldName=(String) br.getHisOldValue(IxPoiName.NAME);
				String newName=br.getName();
				if(newName !=null && !newName.equals(oldName)){
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {}

}
