package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.commons.util.ExcelReader;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpointaddress.IxPointaddress;
import com.navinfo.dataservice.dao.plus.model.ixpointaddress.IxPointaddressFlag;
import com.navinfo.dataservice.dao.plus.model.ixpointaddress.IxPointaddressName;
import com.navinfo.dataservice.dao.plus.model.ixpointaddress.IxPointaddressNameTone;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPointAddressObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

/**
 * @ClassName: FMDPA001
 * @author: zhangpengpeng
 * @date: 2017年9月21日
 * @Desc: FMDPA001.java 检查条件： 非删除点门牌 检查原则： DP_NAME与MEMORIE字段内容应该匹配
 *        若DP_NAME中存在"～"（全角）,MEMORIE中不包含"奇"或"偶"，则报log：
 *        外业门牌号存在“～”，外业LABEL中应包含"奇"或"偶";
 *        若DP_NAME中不存在"～"（全角）,MOMORIE存在"奇"或"偶"，则报log：外业门牌号不存在“～”，
 *        外业LABEL中不应包含"奇"或"偶";
 */
public class FMDPA001 extends BasicCheckRule {

	private String log1 = "外业门牌号存在“～”，外业LABEL中应包含\"奇\"或\"偶\"";

	private String log2 = "外业门牌号不存在“～”，外业LABEL中不应包含\"奇\"或\"偶\"";

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		// TODO Auto-generated method stub
		if (obj.objName().equals(ObjectName.IX_POINTADDRESS) && !obj.opType().equals(OperationType.DELETE)) {
			IxPointAddressObj ixPointaddressObj = (IxPointAddressObj) obj;
			IxPointaddress ixPonitaddress = (IxPointaddress) ixPointaddressObj.getMainrow();
			String dpName = ixPonitaddress.getDpName() == null ? "" : ixPonitaddress.getDpName();
			String memorie = ixPonitaddress.getMemoire() == null ? "" : ixPonitaddress.getMemoire();
			String dpNameFull = ExcelReader.h2f(dpName);

			if (dpNameFull.contains("～")) {
				if (!memorie.contains("奇") && !memorie.contains("偶")) {
					setCheckResult(ixPonitaddress.getGeometry(), ixPointaddressObj, ixPonitaddress.getMeshId(), log1);
				}
			} else {
				if (memorie.contains("奇") || memorie.contains("偶")) {
					setCheckResult(ixPonitaddress.getGeometry(), ixPointaddressObj, ixPonitaddress.getMeshId(), log2);
				}
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
