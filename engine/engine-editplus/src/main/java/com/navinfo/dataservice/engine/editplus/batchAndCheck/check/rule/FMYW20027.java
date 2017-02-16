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
 * @ClassName FMYW20027
 * @author Han Shaoming
 * @date 2017年2月8日 上午10:54:45
 * @Description TODO
 * 检查条件：Lifecycle!=1（删除）
 * 检查原则：
 * 1、address中存在空格，且空格前后若为以下组合，将Err的情况，程序报出；---见空格规则表
 * 2、address前后空格检查：地址不能以空格开头或结尾；
 * 3、address多个空格检查：地址不能出现连续空格；
 * 4、address回车符检查：地址不能包含回车符；
 * 5、addressTab符检查：地址不能包含Tab符号；
 * 以上查出的问题有几种情况报几个log。
 * 充电桩（分类为230227）不参与检查。
 * 排除：空格前后的字或词一样时，不用报Log。
 */
public class FMYW20027 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			//充电桩（230227）不参与检查
			String kindCode = poi.getKindCode();
			if(kindCode == null || "230227".equals(kindCode)){return;}
			IxPoiAddress ixPoiAddress=poiObj.getCHAddress();
			if(ixPoiAddress == null){return;}
			String addressFullname = ixPoiAddress.getFullname();
			if(addressFullname == null){return;}
			//全角空格转半角空格
			String fullname = CheckUtil.strQ2B(addressFullname);
			List<String> errMsgList = new ArrayList<String>();
			//address中存在空格，且空格前后若为以下组合，将Err的情况，程序报出；---见空格规则表
			boolean blankRuleErrStr = CheckUtil.blankRuleTable(fullname);
			if(!blankRuleErrStr){
				errMsgList.add("地址中存在空格，请检查");
			}
			//address前后空格检查,多个空格检查,回车符检查,Tab符检查
			List<String> checkIllegalBlank = CheckUtil.checkIllegalBlank(fullname);
			if(checkIllegalBlank != null && !checkIllegalBlank.isEmpty()){
				for (String msg : checkIllegalBlank) {
					if("前后空格".equals(msg)){
						errMsgList.add("地址中存在空格，请检查");
					} 
					if("多个空格".equals(msg)){
						errMsgList.add("地址中存在连续空格，请检查");
					}
					if("回车符".equals(msg)){
						errMsgList.add("地址中存在回车符，请检查");
					}
					if("Tab符".equals(msg)){
						errMsgList.add("地址中存在Tab符号，请检查");
					}
				}
			}
			for (String errMsg : errMsgList) {
				setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), errMsg);
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
