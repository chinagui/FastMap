package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.CheckUtil;

/**
 * @ClassName FMYW20062
 * @author Han Shaoming
 * @date 2017年2月8日 下午5:16:27
 * @Description TODO
 * 检查条件：  Lifecycle！=1（删除）
 * 检查原则：
 *   1、检查POI地址（address）中“(”与“)”、中括号“［”和“］”、大括号“｛”、“｝”、书名号“《”、“》”应成对出现；
 *   2、括号“（”和“）”、中括号“［”和“］”、大括号“｛”、“｝”、书名号“《”、“》”中间必须有内容；
 *   3、不允许括号嵌套；
 *   否则报出：POI地址中“(”与“)”应成对出现
 *   充电桩（分类为230227）不参与检查。
 *   备注：都是半角或都是全角的括号，不能一边是半角一边是全角的
 */
public class FMYW20062 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			//存在IxPoiAddress修改
			IxPoiAddress ixPoiAddress=poiObj.getCHAddress();
			//错误数据
			if(ixPoiAddress==null){return;}
			String fullname = ixPoiAddress.getFullname();
			//地址(address)中存在全、半角字符“|”
			if(fullname != null){
				if((fullname.contains("(")&&fullname.contains("）"))
						||(fullname.contains("（")&&fullname.contains(")"))
						||(fullname.contains("[")&&fullname.contains("］"))
						||(fullname.contains("［")&&fullname.contains("]"))
						||(fullname.contains("{")&&fullname.contains("｝"))
						||(fullname.contains("｛")&&fullname.contains("}"))){
					setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "中文地址中存在一边是半角一边是全角的括号");
					return;
				}
				String errorMsg=CheckUtil.isRightKuohao(fullname,"(",")");
				if(errorMsg!=null){
					setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "中文地址中"+errorMsg);
					return;
				}
				errorMsg=CheckUtil.isRightKuohao(fullname,"[","]");
				if(errorMsg!=null){
					setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "中文地址中"+errorMsg);
					return;
				}
				errorMsg=CheckUtil.isRightKuohao(fullname,"{","}");
				if(errorMsg!=null){
					setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "中文地址中"+errorMsg);
					return;
				}
				errorMsg=CheckUtil.isRightKuohao(fullname,"《","》");
				if(errorMsg!=null){
					setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "中文地址中"+errorMsg);
					return;
				}
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
